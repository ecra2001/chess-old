//package server;
//
//import chess.ChessGame;
//import chess.InvalidMoveException;
//import com.google.gson.Gson;
//import dataaccess.BadRequestException;
//import dataaccess.UnauthorizedException;
//import model.AuthData;
//import model.GameData;
//import org.eclipse.jetty.websocket.api.Session;
//import org.eclipse.jetty.websocket.api.annotations.OnWebSocketClose;
//import org.eclipse.jetty.websocket.api.annotations.OnWebSocketConnect;
//import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
//import org.eclipse.jetty.websocket.api.annotations.WebSocket;
//import websocket.messages.Error;
//import websocket.messages.LoadGame;
//import websocket.messages.Notification;
//import websocket.messages.ServerMessage;
//import websocket.commands.*;
//
//import java.io.IOException;
//import java.util.Map;
//import java.util.Objects;
//import java.util.concurrent.ConcurrentHashMap;
//
//@WebSocket
//public class WebsocketHandler {
//
//  @OnWebSocketConnect
//  public void onConnect(Session session) throws Exception {
//    Server.gameSessions.put(session, 0);
//  }
//
//  @OnWebSocketClose
//  public void onClose(Session session, int statusCode, String reason) {
//    Server.gameSessions.remove(session);
//  }
//
//  @OnWebSocketMessage
//  public void onMessage(Session session, String message) throws Exception {
//    System.out.printf("Received: %s\n", message);
//
//    // Combine joinplayer and joinobserver to connect
//    if (message.contains("\"commandType\":\"JOIN_PLAYER\"")) {
//      JoinPlayer command = new Gson().fromJson(message, JoinPlayer.class);
//      Server.gameSessions.replace(session, command.getGameID());
//      handleJoinPlayer(session, command);
//    }
//    else if (message.contains("\"commandType\":\"JOIN_OBSERVER\"")) {
//      JoinObserver command = new Gson().fromJson(message, JoinObserver.class);
//      Server.gameSessions.replace(session, command.getGameID());
//      handleJoinObserver(session, command);
//    }
//    else if (message.contains("\"commandType\":\"MAKE_MOVE\"")) {
//      MakeMove command = new Gson().fromJson(message, MakeMove.class);
//      handleMakeMove(session, command);
//    }
//    else if (message.contains("\"commandType\":\"LEAVE\"")) {
//      Leave command = new Gson().fromJson(message, Leave.class);
//      handleLeave(session, command);
//    }
//    else if (message.contains("\"commandType\":\"RESIGN\"")) {
//      Resign command = new Gson().fromJson(message, Resign.class);
//      handleResign(session, command);
//    }
//  }
//
//  private void handleJoinPlayer(Session session, JoinPlayer command) throws IOException {
//
//    try {
//      AuthData auth = Server.userService.getAuth(command.getAuthString());
//      GameData game = Server.gameService.getGameData(command.getAuthString(), command.getGameID());
//
//      ChessGame.TeamColor joiningColor = command.getColor().toString().equalsIgnoreCase("white") ? ChessGame.TeamColor.WHITE : ChessGame.TeamColor.BLACK;
//
//      boolean correctColor;
//      if (joiningColor == ChessGame.TeamColor.WHITE) {
//        correctColor = Objects.equals(game.whiteUsername(), auth.username());
//      }
//      else {
//        correctColor = Objects.equals(game.blackUsername(), auth.username());
//      }
//
//      if (!correctColor) {
//        Error error = new Error("Error: attempting to join with wrong color");
//        sendError(session, error);
//        return;
//      }
//
//      Notification notif = new Notification("%s has joined the game as %s".formatted(auth.username(), command.getColor().toString()));
//      broadcastMessage(session, notif);
//
//      LoadGame load = new LoadGame(game.game());
//      sendMessage(session, load);
//    }
//    catch (UnauthorizedException e) {
//      sendError(session, new Error("Error: Not authorized"));
//    } catch (BadRequestException e) {
//      sendError(session, new Error("Error: Not a valid game"));
//    }
//
//  }
//
//  private void handleJoinObserver(Session session, JoinObserver command) throws IOException {
//    try {
//      AuthData auth = Server.userService.getAuth(command.getAuthString());
//      GameData game = Server.gameService.getGameData(command.getAuthString(), command.getGameID());
//
//      Notification notif = new Notification("%s has joined the game as an observer".formatted(auth.username()));
//      broadcastMessage(session, notif);
//
//      LoadGame load = new LoadGame(game.game());
//      sendMessage(session, load);
//    }
//    catch (UnauthorizedException e) {
//      sendError(session, new Error("Error: Not authorized"));
//    } catch (BadRequestException e) {
//      sendError(session, new Error("Error: Not a valid game"));
//    }
//  }
//
//  private void handleMakeMove(Session session, MakeMove command) throws IOException {
//    try {
//      AuthData auth = Server.userService.getAuth(command.getAuthString());
//      GameData game = Server.gameService.getGameData(command.getAuthString(), command.getGameID());
//      ChessGame.TeamColor userColor = getTeamColor(auth.username(), game);
//      if (userColor == null) {
//        sendError(session, new Error("Error: You are observing this game"));
//        return;
//      }
//
//      if (game.game().getGameOver()) {
//        sendError(session, new Error("Error: can not make a move, game is over"));
//        return;
//      }
//
//      if (game.game().getTeamTurn().equals(userColor)) {
//        game.game().makeMove(command.getMove());
//
//        Notification notif;
//        ChessGame.TeamColor opponentColor = userColor == ChessGame.TeamColor.WHITE ? ChessGame.TeamColor.BLACK : ChessGame.TeamColor.WHITE;
//
//        if (game.game().isInCheckmate(opponentColor)) {
//          notif = new Notification("Checkmate! %s wins!".formatted(auth.username()));
//          game.game().setGameOver(true);
//        }
//        else if (game.game().isInStalemate(opponentColor)) {
//          notif = new Notification("Stalemate caused by %s's move! It's a tie!".formatted(auth.username()));
//          game.game().setGameOver(true);
//        }
//        else if (game.game().isInCheck(opponentColor)) {
//          notif = new Notification("A move has been made by %s, %s is now in check!".formatted(auth.username(), opponentColor.toString()));
//        }
//        else {
//          notif = new Notification("A move has been made by %s".formatted(auth.username()));
//        }
//        broadcastMessage(session, notif);
//
//        Server.gameService.updateGame(auth.authToken(), game);
//
//        LoadGame load = new LoadGame(game.game());
//        broadcastMessage(session, load, true);
//      }
//      else {
//        sendError(session, new Error("Error: it is not your turn"));
//      }
//    }
//    catch (UnauthorizedException e) {
//      sendError(session, new Error("Error: Not authorized"));
//    } catch (BadRequestException e) {
//      sendError(session, new Error("Error: invalid game"));
//    } catch (InvalidMoveException e) {
//      System.out.println("****** error: " + e.getMessage() + "  " + command.getMove().toString());
//      sendError(session, new Error("Error: invalid move (you might need to specify a promotion piece)"));
//    }
//  }
//
//  private void handleLeave(Session session, Leave command) throws IOException {
//    try {
//      AuthData auth = Server.userService.getAuth(command.getAuthString());
//
//      Notification notif = new Notification("%s has left the game".formatted(auth.username()));
//      broadcastMessage(session, notif);
//
//      session.close();
//    } catch (UnauthorizedException e) {
//      sendError(session, new Error("Error: Not authorized"));
//    }
//  }
//
//  private void handleResign(Session session, Resign command) throws IOException {
//    try {
//      AuthData auth = Server.userService.getAuth(command.getAuthString());
//      GameData game = Server.gameService.getGameData(command.getAuthString(), command.getGameID());
//      ChessGame.TeamColor userColor = getTeamColor(auth.username(), game);
//
//      String opponentUsername = userColor == ChessGame.TeamColor.WHITE ? game.blackUsername() : game.whiteUsername();
//
//      if (userColor == null) {
//        sendError(session, new Error("Error: You are observing this game"));
//        return;
//      }
//
//      if (game.game().getGameOver()) {
//        sendError(session, new Error("Error: The game is already over!"));
//        return;
//      }
//
//      game.game().setGameOver(true);
//      Server.gameService.updateGame(auth.authToken(), game);
//      Notification notif = new Notification("%s has forfeited, %s wins!".formatted(auth.username(), opponentUsername));
//      broadcastMessage(session, notif, true);
//    } catch (UnauthorizedException e) {
//      sendError(session, new Error("Error: Not authorized"));
//    } catch (BadRequestException e) {
//      sendError(session, new Error("Error: invalid game"));
//    }
//  }
//
//  // Send the notification to all clients on the current game except the currSession
//  public void broadcastMessage(Session currSession, ServerMessage message) throws IOException {
//    broadcastMessage(currSession, message, false);
//  }
//
//  // Send the notification to all clients on the current game
//  public void broadcastMessage(Session currSession, ServerMessage message, boolean toSelf) throws IOException {
//    System.out.printf("Broadcasting (toSelf: %s): %s%n", toSelf, new Gson().toJson(message));
//    for (Session session : Server.gameSessions.keySet()) {
//      boolean inAGame = Server.gameSessions.get(session) != 0;
//      boolean sameGame = Server.gameSessions.get(session).equals(Server.gameSessions.get(currSession));
//      boolean isSelf = session == currSession;
//      if ((toSelf || !isSelf) && inAGame && sameGame) {
//        sendMessage(session, message);
//      }
//    }
//  }
//
//  public void sendMessage(Session session, ServerMessage message) throws IOException {
//    session.getRemote().sendString(new Gson().toJson(message));
//  }
//
//  private void sendError(Session session, Error error) throws IOException {
//    System.out.printf("Error: %s%n", new Gson().toJson(error));
//    session.getRemote().sendString(new Gson().toJson(error));
//  }
//
//  private ChessGame.TeamColor getTeamColor(String username, GameData game) {
//    if (username.equals(game.whiteUsername())) {
//      return ChessGame.TeamColor.WHITE;
//    }
//    else if (username.equals(game.blackUsername())) {
//      return ChessGame.TeamColor.BLACK;
//    }
//    else return null;
//  }
//
//}