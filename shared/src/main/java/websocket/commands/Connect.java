package websocket.commands;

public class Connect extends UserGameCommand {

  private int gameID;

  public Connect(String authToken, int gameID) {
    super(authToken);
    this.commandType = CommandType.CONNECT;
    this.gameID = gameID;
  }

  public int getGameID() {
    return gameID;
  }
}