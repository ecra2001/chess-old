package server;

import dataaccess.GameRep;
import service.Service.GameService;

public class Handler {
  public static class GameHandler {
    GameService gameService;
    public GameHandler(GameService gameService) {
      this.gameService = gameService;
    }
  }

  public static class UserHandler {
  }
}