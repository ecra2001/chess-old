package dataaccess;
import chess.ChessGame;
import model.AuthData;
import model.GameData;
import java.sql.SQLException;
import java.util.HashSet;
public class SQLGame implements GameRep {
  public SQLGame() {
    try (var conn = DatabaseManager.getConnection()) {
      conn.setCatalog("chess");
      var createTestTable = """            
                    CREATE TABLE if NOT EXISTS game (
                                    gameID INT NOT NULL,
                                    whiteUsername VARCHAR(255),
                                    blackUsername VARCHAR(255),
                                    gameName VARCHAR(255),
                                    chessGame TEXT,
                                    PRIMARY KEY (gameID)
                                    )""";
      try (var createTableStatement = conn.prepareStatement(createTestTable)) {
        createTableStatement.executeUpdate();
      }
    } catch (SQLException | DataAccessException e) {
      throw new RuntimeException(e);
    }
  }
  @Override
  public HashSet<GameData> listGames() {
    HashSet<GameData> games = HashSet.newHashSet(16);
    try (var conn = DatabaseManager.getConnection()) {
      try (var statement = conn.prepareStatement("SELECT gameID, whiteUsername, blackUsername, gameName, chessGame FROM game")) {
        try (var results = statement.executeQuery()) {
          while (results.next()) {
            var gameID = results.getInt("gameID");
            var whiteUsername = results.getString("whiteUsername");
            var blackUsername = results.getString("blackUsername");
            var gameName = results.getString("gameName");
            var chessGame = results.getString("chessGame"); //TODO: deserialize
            games.add(new GameData(gameID, whiteUsername, blackUsername, gameName, new ChessGame()));
          }
        }
      }
    } catch (SQLException | DataAccessException e) {
      return null;
    }
    return games;
  }
  @Override
  public void createGame(GameData game) {
    try (var conn = DatabaseManager.getConnection()) {
      try (var statement = conn.prepareStatement("INSERT INTO game (gameID, whiteUsername, blackUsername, gameName, chessGame) VALUES(?, ?, ?, ?, ?)")) {
        statement.setInt(1, game.gameID());
        statement.setString(2, game.whiteUsername());
        statement.setString(3, game.blackUsername());
        statement.setString(4, game.gameName());
        statement.setString(5, game.game() != null ? game.game().toString() : null); //TODO: serialize game instead of toString()
        statement.executeUpdate();
      }
    } catch (SQLException | DataAccessException e) {
      return;
    }
  }
  @Override
  public GameData getGame(int gameID) throws DataAccessException {
    try (var conn = DatabaseManager.getConnection()) {
      try (var statement = conn.prepareStatement("SELECT whiteUsername, blackUsername, gameName, chessGame FROM game WHERE gameID=?")) {
        statement.setInt(1, gameID);
        try (var results = statement.executeQuery()) {
          results.next();
          var whiteUsername = results.getString("whiteUsername");
          var blackUsername = results.getString("blackUsername");
          var gameName = results.getString("gameName");
          var chessGame = results.getString("chessGame"); //TODO: deserialize
          return new GameData(gameID, whiteUsername, blackUsername, gameName, new ChessGame());
        }
      }
    } catch (SQLException e) {
      throw new DataAccessException("Game not found, id: " + gameID);
    }
  }
  @Override
  public boolean gameExists(int gameID) {
    try (var conn = DatabaseManager.getConnection()) {
      try (var statement = conn.prepareStatement("SELECT gameID FROM game WHERE gameID=?")) {
        statement.setInt(1, gameID);
        try (var results = statement.executeQuery()) {
          return results.next();
        }
      }
    } catch (SQLException | DataAccessException e) {
      return false;
    }
  }
  @Override
  public void updateGame(GameData game) {
    try (var conn = DatabaseManager.getConnection()) {
      try (var statement = conn.prepareStatement("UPDATE game SET whiteUsername=?, blackUsername=?, gameName=?, chessGame=? WHERE gameID=?")) {
        statement.setString(1, game.whiteUsername());
        statement.setString(2, game.blackUsername());
        statement.setString(3, game.gameName());
        statement.setString(4, game.game().toString()); //TODO: serialize game instead of toString()
        statement.setInt(5, game.gameID());
        statement.executeUpdate();
      }
    } catch (SQLException | DataAccessException e) {
      return;
    }
  }
  @Override
  public void clear() {
    try (var conn = DatabaseManager.getConnection()) {
      try (var statement = conn.prepareStatement("TRUNCATE game")) {
        statement.executeUpdate();
      } catch (SQLException e) {
        throw new RuntimeException(e);
      }
    } catch (SQLException | DataAccessException e) {
      return;
    }
  }
}