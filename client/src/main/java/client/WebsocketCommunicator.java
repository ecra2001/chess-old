//package client;
//
//import chess.ChessGame;
//import com.google.gson.Gson;
//import ui.BoardPrinter;
//import ui.Gameplay;
//import webSocketMessages.serverMessages.Error;
//import webSocketMessages.serverMessages.LoadGame;
//import webSocketMessages.serverMessages.Notification;
//import webSocketMessages.serverMessages.ServerMessage;
//
//import javax.websocket.*;
//import java.io.IOException;
//import java.net.URI;
//import java.net.URISyntaxException;
//import java.util.LinkedList;
//
//import static ui.EscapeSequences.ERASE_LINE;
//import static ui.EscapeSequences.moveCursorToLocation;
//
//public class WebsocketCommunicator extends Endpoint {
//
//  Session session;
//
//  public WebsocketCommunicator(String serverDomain) throws Exception {
//    try {
//      URI uri = new URI("ws://" + serverDomain + "/connect");
//
//      WebSocketContainer container = ContainerProvider.getWebSocketContainer();
//      this.session = container.connectToServer(this, uri);
//
//      //set message handler
//      this.session.addMessageHandler(new MessageHandler.Whole<String>() {
//        @Override
//        public void onMessage(String message) {
//          handleMessage(message);
//        }
//      });
//
//    } catch (DeploymentException | IOException | URISyntaxException ex) {
//      throw new Exception();
//    }
//
//  }
//
//  @Override
//  public void onOpen(Session session, EndpointConfig config) {
//  }
//
//  private void handleMessage(String message) {
//    if (message.contains("\"serverMessageType\":\"NOTIFICATION\"")) {
//      Notification notif = new Gson().fromJson(message, Notification.class);
//      printNotification(notif.getMessage());
//    }
//    else if (message.contains("\"serverMessageType\":\"ERROR\"")) {
//      Error error = new Gson().fromJson(message, Error.class);
//      printNotification(error.getMessage());
//    }
//    else if (message.contains("\"serverMessageType\":\"LOAD_GAME\"")) {
//      LoadGame loadGame = new Gson().fromJson(message, LoadGame.class);
//      printLoadedGame(loadGame.getGame());
//    }
//  }
//
//  private void printNotification(String message) {
//    System.out.print(ERASE_LINE + '\r');
//    System.out.printf("\n%s\n[IN-GAME] >>> ", message);
//  }
//
//  private void printLoadedGame(ChessGame game) {
//    System.out.print(ERASE_LINE + "\r\n");
//    Gameplay.boardPrinter.updateGame(game);
//    Gameplay.boardPrinter.printBoard(Gameplay.color, null);
//    System.out.print("[IN-GAME] >>> ");
//  }
//
//  public void sendMessage(String message) {
//    this.session.getAsyncRemote().sendText(message);
//  }
//}