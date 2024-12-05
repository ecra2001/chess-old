package server;

import chess.ChessGame;
import chess.InvalidMoveException;
import com.google.gson.Gson;
import dataaccess.BadRequestException;
import dataaccess.DataAccessException;
import dataaccess.UnauthorizedException;
import model.AuthData;
import model.GameData;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketClose;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketConnect;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import websocket.messages.Error;
import websocket.messages.LoadGame;
import websocket.messages.Notification;
import websocket.messages.ServerMessage;
import websocket.commands.*;

import java.io.IOException;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

@WebSocket
public class WebsocketHandler {

  @OnWebSocketConnect
  public void onConnect(Session session) throws Exception {
    Server.gameSessions.put(session, 0);
  }

  @OnWebSocketClose
  public void onClose(Session session, int statusCode, String reason) {
    Server.gameSessions.remove(session);
  }

  @OnWebSocketMessage
  public void onMessage(Session session, String message) throws Exception {
    System.out.printf("Received: %s\n", message);

    if (message.contains("\"commandType\":\"CONNECT\"")) {
      Connect command = new Gson().fromJson(message, Connect.class);
      Server.gameSessions.replace(session, command.getGameID());
      handleConnect(session, command);
    }
    else if (message.contains("\"commandType\":\"MAKE_MOVE\"")) {
      MakeMove command = new Gson().fromJson(message, MakeMove.class);
      handleMakeMove(session, command);
    }
    else if (message.contains("\"commandType\":\"LEAVE\"")) {
      Leave command = new Gson().fromJson(message, Leave.class);
      handleLeave(session, command);
    }
    else if (message.contains("\"commandType\":\"RESIGN\"")) {
      Resign command = new Gson().fromJson(message, Resign.class);
      handleResign(session, command);
    }
  }

  private void handleConnect(Session session, Connect command) throws IOException {
    try {
      AuthData auth = Server.userService.getAuth(command.getAuthToken());
      GameData game = Server.gameService.getGameData(command.getAuthToken(), command.getGameID());

      String role;
      if (Objects.equals(auth.username(), game.whiteUsername())) {
        role = "player (white)";
      } else if (Objects.equals(auth.username(), game.blackUsername())) {
        role = "player (black)";
      } else {
        role = "observer";
      }

      Notification notif = new Notification("%s has connected to the game as %s".formatted(auth.username(), role));
      broadcastMessage(session, notif);

      LoadGame load = new LoadGame(game.game());
      sendMessage(session, load);
    }
    catch (UnauthorizedException e) {
      sendError(session, new Error("Error: Not authorized"));
    } catch (BadRequestException e) {
      sendError(session, new Error("Error: Not a valid game"));
    }
  }

  private void handleMakeMove(Session session, MakeMove command) throws IOException {
    try {
      AuthData auth = Server.userService.getAuth(command.getAuthToken());
      GameData game = Server.gameService.getGameData(command.getAuthToken(), command.getGameID());
      ChessGame.TeamColor userColor = getTeamColor(auth.username(), game);
      if (userColor == null) {
        sendError(session, new Error("Error: You are observing this game"));
        return;
      }

      if (game.game().getGameOver()) {
        sendError(session, new Error("Error: can not make a move, game is over"));
        return;
      }

      if (game.game().getTeamTurn().equals(userColor)) {
        game.game().makeMove(command.getMove());

        Notification notif;
        ChessGame.TeamColor opponentColor = userColor == ChessGame.TeamColor.WHITE ? ChessGame.TeamColor.BLACK : ChessGame.TeamColor.WHITE;

        if (game.game().isInCheckmate(opponentColor)) {
          notif = new Notification("Checkmate! %s wins!".formatted(auth.username()));
          game.game().setGameOver(true);
        }
        else if (game.game().isInStalemate(opponentColor)) {
          notif = new Notification("Stalemate caused by %s's move! It's a tie!".formatted(auth.username()));
          game.game().setGameOver(true);
        }
        else if (game.game().isInCheck(opponentColor)) {
          notif = new Notification("A move has been made by %s, %s is now in check!".formatted(auth.username(), opponentColor.toString()));
        }
        else {
          notif = new Notification("A move has been made by %s".formatted(auth.username()));
        }
        broadcastMessage(session, notif);

        Server.gameService.updateGame(auth.authToken(), game);

        LoadGame load = new LoadGame(game.game());
        broadcastMessage(session, load, true);
      }
      else {
        sendError(session, new Error("Error: it is not your turn"));
      }
    }
    catch (UnauthorizedException e) {
      sendError(session, new Error("Error: Not authorized"));
    } catch (BadRequestException e) {
      sendError(session, new Error("Error: invalid game"));
    } catch (InvalidMoveException e) {
      System.out.println("****** error: " + e.getMessage() + "  " + command.getMove().toString());
      sendError(session, new Error("Error: invalid move (you might need to specify a promotion piece)"));
    }
  }

  private void handleLeave(Session session, Leave command) throws IOException {
    try {
      // Authenticate the user
      AuthData auth = Server.userService.getAuth(command.getAuthToken());

      // Retrieve the game from the database
      GameData gameData = Server.gameService.getGameData(command.getAuthToken(), command.getGameID());

      // Check if the user is one of the players
      if (auth.username().equals(gameData.whiteUsername())) {
        // If the user is the white player, create a new GameData with whiteUsername set to null
        gameData = new GameData(gameData.gameID(), null, gameData.blackUsername(), gameData.gameName(), gameData.game());
      } else if (auth.username().equals(gameData.blackUsername())) {
        // If the user is the black player, create a new GameData with blackUsername set to null
        gameData = new GameData(gameData.gameID(), gameData.whiteUsername(), null, gameData.gameName(), gameData.game());
      } else {
        // If the user is neither white nor black, treat them as an observer
        // Since you're not tracking observers in GameData, just send a notification and close the session
        Notification notif = new Notification("%s has left the game".formatted(auth.username()));
        broadcastMessage(session, notif);
        session.close();
        return; // Do not modify game data for observers
      }

      // Persist the updated game state if a player left
      Server.gameService.updateGameData(gameData);

      // Notify others about the player leaving
      Notification notif = new Notification("%s has left the game".formatted(auth.username()));
      broadcastMessage(session, notif);

      // Close the WebSocket session
      session.close();
    } catch (UnauthorizedException e) {
      sendError(session, new Error("Error: Not authorized"));
    } catch (BadRequestException | DataAccessException e) {
      sendError(session, new Error("Error: " + e.getMessage()));
    }
  }

  private void handleResign(Session session, Resign command) throws IOException {
    try {
      AuthData auth = Server.userService.getAuth(command.getAuthToken());
      GameData game = Server.gameService.getGameData(command.getAuthToken(), command.getGameID());
      ChessGame.TeamColor userColor = getTeamColor(auth.username(), game);

      String opponentUsername = userColor == ChessGame.TeamColor.WHITE ? game.blackUsername() : game.whiteUsername();

      if (userColor == null) {
        sendError(session, new Error("Error: You are observing this game"));
        return;
      }

      if (game.game().getGameOver()) {
        sendError(session, new Error("Error: The game is already over!"));
        return;
      }

      game.game().setGameOver(true);
      Server.gameService.updateGame(auth.authToken(), game);
      Notification notif = new Notification("%s has forfeited, %s wins!".formatted(auth.username(), opponentUsername));
      broadcastMessage(session, notif, true);
    } catch (UnauthorizedException e) {
      sendError(session, new Error("Error: Not authorized"));
    } catch (BadRequestException e) {
      sendError(session, new Error("Error: invalid game"));
    }
  }

  // Send the notification to all clients on the current game except the currSession
  public void broadcastMessage(Session currSession, ServerMessage message) throws IOException {
    broadcastMessage(currSession, message, false);
  }

  public void broadcastMessage(Session currSession, ServerMessage message, boolean toSelf) throws IOException {
    System.out.printf("Broadcasting (toSelf: %s): %s%n", toSelf, new Gson().toJson(message));
    for (Session session : Server.gameSessions.keySet()) {
      boolean inAGame = Server.gameSessions.get(session) != 0;
      boolean sameGame = Server.gameSessions.get(session).equals(Server.gameSessions.get(currSession));
      boolean isSelf = session == currSession;
      if ((toSelf || !isSelf) && inAGame && sameGame) {
        sendMessage(session, message);
      }
    }
  }

  public void sendMessage(Session session, ServerMessage message) throws IOException {
    session.getRemote().sendString(new Gson().toJson(message));
  }

  private void sendError(Session session, Error error) throws IOException {
    System.out.printf("Error: %s%n", new Gson().toJson(error));
    session.getRemote().sendString(new Gson().toJson(error));
  }

  private ChessGame.TeamColor getTeamColor(String username, GameData game) {
    if (username.equals(game.whiteUsername())) {
      return ChessGame.TeamColor.WHITE;
    }
    else if (username.equals(game.blackUsername())) {
      return ChessGame.TeamColor.BLACK;
    }
    else return null;
  }

}