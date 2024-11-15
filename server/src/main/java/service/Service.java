package service;

import chess.ChessBoard;
import chess.ChessGame;
import dataaccess.GameRep;
import dataaccess.AuthRep;
import dataaccess.UserRep;
import dataaccess.DataAccessException;
import dataaccess.UnauthorizedException;
import dataaccess.*;
import model.AuthData;
import model.GameData;
import model.UserData;
import java.util.HashSet;
import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;
import java.util.UUID;
public class Service {
  public static class GameService {
    GameRep gameDAO;
    AuthRep authDAO;

    public GameService(GameRep gameDAO, AuthRep authDAO) {
      this.gameDAO = gameDAO;
      this.authDAO = authDAO;
    }

    public HashSet<GameData> listGames(String authToken) throws UnauthorizedException {
      try {
        authDAO.getAuth(authToken);
      } catch (DataAccessException e) {
        throw new UnauthorizedException();
      }
      return gameDAO.listGames();
    }

    public int createGame(String authToken, String gameName) throws UnauthorizedException, BadRequestException {
      try {
        authDAO.getAuth(authToken);
      } catch (DataAccessException e) {
        throw new UnauthorizedException();
      }

      int gameID;
      do { // Get random gameIDs until the gameID is not already in use
        gameID = ThreadLocalRandom.current().nextInt(1, 10000);
      } while (gameDAO.gameExists(gameID));
      try {
        ChessGame game = new ChessGame();
        ChessBoard board = new ChessBoard();
        board.resetBoard();
        game.setBoard(board);
        gameDAO.createGame(new GameData(gameID, null, null, gameName, null));
      } catch (DataAccessException e) {
        throw new BadRequestException(e.getMessage());
      }
      return gameID;
    }

    /***
     * @param authToken authToken of user
     * @param gameID gameID of the game to join
     * @param color (nullable) team color to join as
     * @return boolean of success
     * @throws UnauthorizedException invalid authToken
     * @throws BadRequestException bad request
     */
    public boolean joinGame(String authToken, int gameID, String color) throws UnauthorizedException, BadRequestException {
      AuthData authData;
      GameData gameData;
      try {
        authData = authDAO.getAuth(authToken);
      } catch (DataAccessException e) {
        throw new UnauthorizedException();
      }

      try {
        gameData = gameDAO.getGame(gameID);
      } catch (DataAccessException e) {
        throw new BadRequestException(e.getMessage());
      }

      String whiteUser = gameData.whiteUsername();
      String blackUser = gameData.blackUsername();

      if (!"WHITE".equalsIgnoreCase(color) && !"BLACK".equalsIgnoreCase(color)) {
        throw new BadRequestException("%s is not a valid team color".formatted(color));
      }

      if ("WHITE".equalsIgnoreCase(color)) {
        if (whiteUser != null) {return false;} // Spot taken
        whiteUser = authData.username();
      } else if ("BLACK".equalsIgnoreCase(color)) {
        if (blackUser != null) {return false;} // Spot taken
        blackUser = authData.username();
      }

      // Update the game with the new user
      try {
        gameDAO.updateGame(new GameData(gameID, whiteUser, blackUser, gameData.gameName(), gameData.game()));
      } catch (DataAccessException e) {
        throw new BadRequestException(e.getMessage());
      }
      return true;
    }

    public static void clear(GameRep gameDAO) {
      gameDAO.clear();
    }
  }

  public static class UserService {
    UserRep userDAO;
    AuthRep authDAO;

    public UserService(UserRep userDAO, AuthRep authDAO) {
      this.userDAO = userDAO;
      this.authDAO = authDAO;
    }

    public AuthData createUser(UserData userData) throws BadRequestException {

      try {
        userDAO.createUser(userData);
      } catch (DataAccessException e) {
        throw new BadRequestException(e.getMessage());
      }
      String authToken = UUID.randomUUID().toString();
      AuthData authData = new AuthData(userData.username(), authToken);
      authDAO.addAuth(authData);

      return authData;
    }

    // throws DataAccessException if the username does not exist or the password is incorrect
    public AuthData loginUser(UserData userData) throws UnauthorizedException {
      boolean userAuthenticated;
      try {
        userAuthenticated = userDAO.authenticateUser(userData.username(), userData.password());
      } catch (DataAccessException e) {
        throw new UnauthorizedException();
      }

      if (userAuthenticated) {
        String authToken = UUID.randomUUID().toString();
        AuthData authData = new AuthData(userData.username(), authToken);
        authDAO.addAuth(authData);
        return authData;
      }
      else {
        throw new UnauthorizedException();
      }
    }

    public void logoutUser(String authToken) throws UnauthorizedException {
      try {
        authDAO.getAuth(authToken);
      } catch (DataAccessException e) {
        throw new UnauthorizedException();
      }
      authDAO.deleteAuth(authToken);
    }


    public void clear() {
      userDAO.clear();
      authDAO.clear();
    }
  }
}