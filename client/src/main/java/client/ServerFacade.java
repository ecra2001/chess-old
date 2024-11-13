package client;
import com.google.gson.Gson;
import model.GameData;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
public class ServerFacade {
  String baseURL = "http://localhost:8080";
  String authToken;
  public ServerFacade() {
  }
  public boolean register(String username, String password, String email) {
    var body = Map.of("username", username, "password", password, "email", email);
    var jsonBody = new Gson().toJson(body);
    Map resp = request("POST", "/user", jsonBody);
    if (resp.containsKey("Error")) {
      return false;
    }
    authToken = (String) resp.get("authToken");
    return true;
  }
  public boolean login(String username, String password) {
    var body = Map.of("username", username, "password", password);
    var jsonBody = new Gson().toJson(body);
    Map resp = request("POST", "/session", jsonBody);
    if (resp.containsKey("Error")) {
      return false;
    }
    authToken = (String) resp.get("authToken");
    return true;
  }
  public boolean logout() {
    Map resp = request("DELETE", "/session");
    if (resp.containsKey("Error")) {
      return false;
    }
    authToken = null;
    return true;
  }
  public boolean createGame(String gameName) {
    var body = Map.of("gameName", gameName);
    var jsonBody = new Gson().toJson(body);
    Map resp = request("POST", "/game", jsonBody);
    return !resp.containsKey("Error");
  }
  public List<GameData> listGames() {
    Map resp = request("GET", "/game");
    if (resp.containsKey("Error")) {
      return new ArrayList<>();
    }
    return (List<GameData>) (resp.get("games"));
  }
  public boolean joinGame(int gameId, String playerColor) {
    var body = Map.of("gameID", gameId, "playerColor", playerColor);
    var jsonBody = new Gson().toJson(body);
    Map resp = request("Update", "/game", jsonBody);
    return !resp.containsKey("Error");
  }
  public Map request (String method, String endpoint) {
    return request(method, endpoint, null);
  }
  public Map request(String method, String endpoint, String body) {
    Map respMap;
    try {
      URI uri = new URI(baseURL + endpoint);
      HttpURLConnection http = (HttpURLConnection) uri.toURL().openConnection();
      http.setRequestMethod(method);
      if (authToken != null) {
        http.addRequestProperty("authorization", authToken);
      }
      if (!Objects.equals(body, null)) {
        http.setDoOutput(true);
        http.addRequestProperty("Content-Type", "application/json");
        try (var outputStream = http.getOutputStream()) {
          outputStream.write(body.getBytes());
        }
      }
      http.connect();
      try {
        if (http.getResponseCode() == 401) {
          return Map.of("Error", 401);
        }
      } catch (IOException e) {
        return Map.of("Error", 401);
      }
      try (InputStream respBody = http.getInputStream()) {
        InputStreamReader inputStreamReader = new InputStreamReader(respBody);
        respMap = new Gson().fromJson(inputStreamReader, Map.class);
      }
    } catch (URISyntaxException | IOException e) {
      return Map.of("Error", e.getMessage());
    }
    return respMap;
  }
}
