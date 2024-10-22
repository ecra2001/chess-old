package service;

import dataaccess.GameRep;
public class Service {
  public static class GameService {
    GameRep gameDAO;

    public GameService(GameRep gameDAO) {
      this.gameDAO = gameDAO;
    }

    public static void clear(GameRep gameDAO) {
      gameDAO.clear();
    }
  }

  public static class UserService {
  }
}