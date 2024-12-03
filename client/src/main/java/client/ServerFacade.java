//package client;
//import com.google.gson.Gson;
//import model.GameData;
//import model.GamesList;
//import java.io.IOException;
//import java.io.InputStream;
//import java.io.InputStreamReader;
//import java.net.HttpURLConnection;
//import java.net.URI;
//import java.net.URISyntaxException;
//import webSocketMessages.serverMessages.ServerMessage;
//import webSocketMessages.userCommands.*;
//import java.util.*;
//public class ServerFacade {
//  HTTPCommunicator http;
//  WebsocketCommunicator ws;
//  String serverDomain;
//  String authToken;
//
//  public ServerFacade() throws Exception {
//    this("localhost:8080");
//  }
//
//  public ServerFacade(String serverDomain) throws Exception {
//    this.serverDomain = serverDomain;
//    http = new HTTPCommunicator(this, serverDomain);
//  }
//
//  protected String getAuthToken() {
//    return authToken;
//  }
//
//  protected void setAuthToken(String authToken) {
//    this.authToken = authToken;
//  }
//
//  public boolean register(String username, String password, String email) {
//    return http.register(username, password, email);
//  }
//
//  public boolean login(String username, String password) {
//    return http.login(username, password);
//  }
//
//  public boolean logout() {
//    return http.logout();
//  }
//
//  public int createGame(String gameName) {
//    return http.createGame(gameName);
//  }
//
//  public List<GameData> listGames() {
//    return http.listGames();
//  }
//
//  public boolean joinGame(int gameId, String playerColor) {
//    return http.joinGame(gameId, playerColor);
//  }
//
//  public void connectWS() {
//    try {
//      ws = new WebsocketCommunicator(serverDomain);
//    }
//    catch (Exception e) {
//      System.out.println("Failed to make connection with server");
//    }
//  }
//
//  public void sendCommand(UserGameCommand command) {
//    String message = new Gson().toJson(command);
//    ws.sendMessage(message);
//  }
//
//  // Make CONNECT
////  public void joinPlayer(int gameID, ChessGame.TeamColor color) {
////    sendCommand(new JoinPlayer(authToken, gameID, color));
////  }
////
////  public void joinObserver(int gameID) {
////    sendCommand(new JoinObserver(authToken, gameID));
////  }
//
//  public void makeMove(int gameID, ChessMove move) {
//    sendCommand(new MakeMove(authToken, gameID, move));
//  }
//
//  public void leave(int gameID) {
//    sendCommand(new Leave(authToken, gameID));
//  }
//
//  public void resign(int gameID) {
//    sendCommand(new Resign(authToken, gameID));
//  }
//}
