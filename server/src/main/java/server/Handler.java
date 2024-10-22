package server;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import dataaccess.DataAccessException;
import dataaccess.UnauthorizedException;
import dataaccess.BadRequestException;
import model.GameData;
import model.UserData;
import model.AuthData;
import service.Service.GameService;
import spark.Request;
import spark.Response;
import java.util.HashSet;


public class Handler {
  public static class GameHandler {
    GameService gameService;
    public GameHandler(GameService gameService) {
      this.gameService = gameService;
    }

    public Object listGames(Request req, Response resp) throws UnauthorizedException {
      String authToken=req.headers("authorization");
      HashSet<GameData> games=gameService.listGames(authToken);
      resp.status(200);
      return "{ \"games\": %s}".formatted(new Gson().toJson(games));
    }

    public Object createGame(Request req, Response resp) throws BadRequestException, UnauthorizedException {

      if (!req.body().contains("\"gameName\":")) {
        throw new BadRequestException("No gameName provided");
      }
      String authToken = req.headers("authorization");
      int gameID =  gameService.createGame(authToken);

      resp.status(200);
      return "{ \"gameID\": %d }".formatted(gameID);
    }

    public Object joinGame(Request req, Response resp) throws BadRequestException, UnauthorizedException {

      if (!req.body().contains("\"gameID\":")) {
        throw new BadRequestException("No gameID provided");
      }
      String authToken=req.headers("authorization");
      record JoinGameData(String playerColor, int gameID) {
      }
      JoinGameData joinData=new Gson().fromJson(req.body(), JoinGameData.class);
      boolean joinSuccess=gameService.joinGame(authToken, joinData.gameID(), joinData.playerColor());

      if (!joinSuccess) {
        resp.status(403);
        return "{ \"message\": \"Error: already taken\" }";
      }

      resp.status(200);
      return "{}";
    }
  }

  public static class UserHandler {
    service.Service.UserService userService;

    public UserHandler(service.Service.UserService userService) {
      this.userService = userService;
    }

    public Object register(Request req, Response resp) throws BadRequestException {
      UserData userData = new Gson().fromJson(req.body(), UserData.class);
      if (userData.username() == null || userData.password() == null) {
        throw new BadRequestException("No username and/or password given");
      }

      try {
        AuthData authData = userService.createUser(userData);
        RegisterResponse registerResponse = new RegisterResponse(userData.getUsername(), authData.getAuthToken());

        resp.status(200);
        return new Gson().toJson(registerResponse);
      } catch (BadRequestException e) {
        resp.status(403);
        return "{ \"message\": \"Error: already taken\" }";
      }
    }

    public Object login(Request req, Response resp) throws UnauthorizedException {
      UserData userData = new Gson().fromJson(req.body(), UserData.class);
      AuthData authData = userService.loginUser(userData);

      LoginResponse loginResponse =new LoginResponse(userData.getUsername(), authData.getAuthToken());

      resp.status(200);
      return new Gson().toJson(loginResponse);
    }

    public Object logout(Request req, Response resp) throws UnauthorizedException {
      String authToken = req.headers("authorization");

      userService.logoutUser(authToken);

      resp.status(200);
      return "{}";
    }
    public static class LoginResponse {
      private String username;
      private String authToken;

      // Constructor
      public LoginResponse(String username, String authToken) {
        this.username = username;
        this.authToken = authToken;
      }

      // Getter for username
      public String getUsername() {
        return username;
      }

      // Setter for username
      public void setUsername(String username) {
        this.username = username;
      }

      // Getter for authToken
      public String getAuthToken() {
        return authToken;
      }

      // Setter for authToken
      public void setAuthToken(String authToken) {
        this.authToken = authToken;
      }
    }
    public class RegisterResponse {
      private String username;
      private String authToken;

      public RegisterResponse(String username, String authToken) {
        this.username = username;
        this.authToken = authToken;
      }

      // Getters and setters...
      public String getUsername() {
        return username;
      }

      public void setUsername(String username) {
        this.username = username;
      }

      public String getAuthToken() {
        return authToken;
      }

      public void setAuthToken(String authToken) {
        this.authToken = authToken;
      }
    }
  }
}