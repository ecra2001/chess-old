package dataaccess;

import model.GameData;
import java.util.HashSet;

public interface GameRep {
  HashSet<GameData> listGames();
  void createGame(GameData game) throws DataAccessException;
  GameData getGame(int gameID) throws DataAccessException;
  boolean gameExists(int gameID);
  void updateGame(GameData game) throws DataAccessException;
  void clear();
}