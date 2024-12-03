package ui;

import chess.ChessGame;
import model.GameData;
import client.ServerFacade;

import java.util.*;
import static java.lang.System.out;
import static ui.EscapeSequences.*;

public class PostLogin {
  ServerFacade server;
  List<GameData> games;
  boolean inGame;

  public PostLogin(ServerFacade server) {
    this.server = server;
    games = new ArrayList<>();
  }

  public void run() {
    boolean loggedIn = true;
    out.print(RESET_TEXT_COLOR + RESET_BG_COLOR);
    while (loggedIn) {
      String[] input = getUserInput();
      switch (input[0]) {
        case "quit":
          return;
        case "help":
          printHelpMenu();
          break;
        case "logout":
          server.logout();
          loggedIn = false;
          break;
        case "list":
          refreshGames();
          printGames();
          break;
        case "create":
          if (input.length != 2) {
            out.println("Please provide a name");
            printCreate();
            break;
          }
          server.createGame(input[1]);
          out.println("Game created successfully");
          break;
        case "connect":
          handleConnect(input);
          break;
        default:
          out.println("Command not recognized, please try again");
          printHelpMenu();
          break;
      }
    }

    if (!loggedIn) {
      PreLogin preloginREPL = new PreLogin(server);
      preloginREPL.run();
    }
  }

  private String[] getUserInput() {
    out.print("\n[LOGGED IN] >>> ");
    Scanner scanner = new Scanner(System.in);
    return scanner.nextLine().split(" ");
  }

  private void printHelpMenu() {
    printCreate();
    out.println("list - list all games");
    printConnect();
    out.println("logout - log out of current user");
    out.println("help - show this menu");
  }

  private void printCreate() {
    out.println("create <NAME> - create a new game");
  }

  private void printConnect() {
    out.println("connect <ID> [WHITE|BLACK] - connect to a game as a player or observer");
  }

  private void refreshGames() {
    games = new ArrayList<>();
    List<GameData> gameList = server.listGames();
    Collections.reverse(gameList); // Reverse the list to maintain correct order
    games.addAll(gameList);
  }

  private void printGames() {
    for (int i = 0; i < games.size(); i++) {
      GameData game = games.get(i);
      String whiteUser = game.whiteUsername() != null ? game.whiteUsername() : "open";
      String blackUser = game.blackUsername() != null ? game.blackUsername() : "open";
      out.printf("%d -- Game Name: %s  |  White User: %s  |  Black User: %s %n", i + 1, game.gameName(), whiteUser, blackUser);
    }
  }

  private void handleConnect(String[] input) {
    if (input.length < 2 || !input[1].matches("\\d")) {
      out.println("Please provide a valid game ID");
      printConnect();
      return;
    }

    int gameNum = Integer.parseInt(input[1]) - 1; // Convert to zero-based index
    if (games.isEmpty() || gameNum < 0 || gameNum >= games.size()) {
      refreshGames();
      if (games.isEmpty() || gameNum < 0 || gameNum >= games.size()) {
        out.println("Error: invalid Game ID");
        printGames();
        return;
      }
    }

    GameData selectedGame = games.get(gameNum);

    ChessGame.TeamColor color = null;
    if (input.length == 3) {
      if (!input[2].equalsIgnoreCase("WHITE") && !input[2].equalsIgnoreCase("BLACK")) {
        out.println("Invalid color. Please specify WHITE or BLACK.");
        printConnect();
        return;
      }
      color = input[2].equalsIgnoreCase("WHITE") ? ChessGame.TeamColor.WHITE : ChessGame.TeamColor.BLACK;
      if (!server.joinGame(selectedGame.gameID(), input[2].toUpperCase())) {
        out.println("Failed to join game: color may be taken or game may not exist.");
        return;
      }
    }

    server.connectWS();
    server.connectGame(selectedGame.gameID());
    inGame = true;

    Gameplay gameplayREPL = new Gameplay(server, selectedGame, color);
    gameplayREPL.run();
  }
}