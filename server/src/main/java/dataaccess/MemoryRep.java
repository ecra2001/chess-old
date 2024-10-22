package dataaccess;

import model.AuthData;
import model.UserData;
import chess.ChessGame;
import model.GameData;
import javax.xml.crypto.Data;
import java.util.HashSet;
import javax.swing.text.SimpleAttributeSet;

public class MemoryRep {

  public static class MemoryAuth implements AuthRep {
    HashSet<AuthData> db;

    public MemoryAuth() {
      db = HashSet.newHashSet(16);
    }

    @Override
    public void addAuth(String authToken, String username) {
      db.add(new AuthData(username, authToken));
    }
    @Override
    public void addAuth(AuthData authData) {
      db.add(authData);
    }
    @Override
    public void deleteAuth(String authToken) {
      for (AuthData authData : db) {
        if (authData.authToken().equals(authToken)) {
          db.remove(authData);
          break;
        }
      }
    }
    @Override
    public AuthData getAuth(String authToken) throws DataAccessException {
      for (AuthData authData : db) {
        if (authData.authToken().equals(authToken)) {
          return authData;
        }
      }
      throw new DataAccessException("Auth Token does not exist: " + authToken);
    }
    @Override
    public void clear() {
      db = HashSet.newHashSet(16);
    }
  }

  public static class MemoryGame implements GameRep {
    HashSet<GameData> db;

    public MemoryGame() {
      db = HashSet.newHashSet(16);
    }

    @Override
    public HashSet<GameData> listGames() {
      return db;
    }
    @Override
    public void createGame(int gameID, String whiteUsername, String blackUsername, String gameName, ChessGame game) {
      db.add(new GameData(gameID, whiteUsername, blackUsername, gameName, game));
    }
    @Override
    public void createGame(GameData game) {
      db.add(game);
    }
    @Override
    public GameData getGame(int gameID) throws DataAccessException {
      for (GameData game : db) {
        if (game.gameID() == gameID) {
          return game;
        }
      }
      throw new DataAccessException("Game not found, id: " + gameID);
    }
    @Override
    public boolean gameExists(int gameID) {
      for (GameData game : db) {
        if (game.gameID() == gameID) {
          return true;
        }
      }
      return false;
    }
    @Override
    public void updateGame(GameData game) {
      try {
        db.remove(getGame(game.gameID()));
        db.add(game);
      } catch (DataAccessException e) {
        db.add(game);
      }
    }
    @Override
    public void clear() {
      db = HashSet.newHashSet(16);
    }
  }

  public static class MemoryUser implements UserRep {
    private HashSet<UserData> db;

    public MemoryUser() {
      db = HashSet.newHashSet(16);
    }

    @Override
    public UserData getUser(String username) throws DataAccessException {
      for (UserData user : db) {
        if (user.username().equals(username)) {
          return user;
        }
      }
      throw new DataAccessException("User not found: " + username);
    }
    @Override
    public void createUser(String username, String password, String email) throws DataAccessException {
      try {
        getUser(username);
      }
      catch (DataAccessException e) {
        db.add(new UserData(username, password, email));
        return;
      }

      throw new DataAccessException("User already exists: " + username);

    }
    @Override
    public void createUser(UserData user) throws DataAccessException {
      try {
        getUser(user.username());
      }
      catch (DataAccessException e) {
        db.add(user);
        return;
      }

      throw new DataAccessException("User already exists: " + user.username());
    }
    @Override
    public boolean authenticateUser(String username, String password) throws DataAccessException {
      boolean userExists = false;
      for (UserData user : db) {
        if (user.username().equals(username)) {
          userExists = true;
        }
        if (user.username().equals(username) &&
                user.password().equals(password)) {
          return true;
        }
      }
      if (userExists) {
        return false;
      }
      else {
        throw new DataAccessException("User does not exist: " + username);
      }
    }
    @Override
    public void clear() {
      db = HashSet.newHashSet(16);
    }
  }
}