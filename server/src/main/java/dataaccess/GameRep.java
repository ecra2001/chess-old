package dataaccess;

import model.GameData;
import chess.ChessGame;
import java.util.HashSet;

public interface GameRep {
  HashSet<GameData> listGames();
  void createGame(int gameID, String whiteUsername, String blackUsername, String gameName, ChessGame game);
  void createGame(GameData game);
  GameData getGame(int gameID) throws DataAccessException;
  boolean gameExists(int gameID);
  void updateGame(GameData game);
  void clear();
}