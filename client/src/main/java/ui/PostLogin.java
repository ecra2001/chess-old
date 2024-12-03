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
//          int gameID = server.createGame(input[1]);
//          out.printf("Created game");
//          refreshGames();
//          break;
          server.createGame(input[1]);
          out.printf("Created game");
          break;
        case "join":
          handleJoin(input);
          break;
        case "observe":
          handleObserve(input);
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
    printJoin();
    printObserve();
    out.println("logout - log out of current user");
    // out.println("quit - stop playing");
    out.println("help - show this menu");
  }
  private void printCreate() {
    out.println("create <NAME> - create a new game");
  }
  private void printJoin() {
    out.println("join <ID> [WHITE|BLACK] - join a game as color");
  }
  private void printObserve() {
    out.println("observe <ID> - observe a game");
  }
  private void refreshGames() {
    games = new ArrayList<>();
    List<GameData> gameList = server.listGames();
    Collections.reverse(gameList);  // Reverse the list to maintain correct order
    games.addAll(gameList);
  }
  private void printGames() {
    for (int i = 0; i < games.size(); i++) {
      GameData game = games.get(i);
      String whiteUser = game.whiteUsername() != null ? game.whiteUsername() : "open";
      String blackUser = game.blackUsername() != null ? game.blackUsername() : "open";
      out.printf("%d -- Game Name: %s  |  White User: %s  |  Black User: %s %n", i+1, game.gameName(), whiteUser, blackUser);
    }
  }

  // Need to Combine Join and Observe to CONNECT
  private void handleJoin(String[] input) {
    if (input.length != 3 || !input[1].matches("\\d") || !input[2].toUpperCase().matches("WHITE|BLACK")) {
      out.println("Please provide a game ID and color choice");
      printJoin();
      return;
    }
    int gameNum = Integer.parseInt(input[1]);
    if (games.isEmpty() || games.size() <= gameNum) {
      refreshGames();
      if (games.isEmpty()) {
        out.println("Error: please first create a game");
        return;
      }
      if (games.size() <= gameNum) {
        out.println("Error: that Game ID does not exist");
        printGames();
        return;
      }
    }
    GameData joinGame = games.get(gameNum);
    ChessGame.TeamColor color = input[2].equalsIgnoreCase("WHITE") ? ChessGame.TeamColor.WHITE : ChessGame.TeamColor.BLACK;
    if (server.joinGame(joinGame.gameID(), input[2].toUpperCase())) {
      out.println("You have joined the game");
      inGame = true;
      server.connectWS();
      server.joinPlayer(joinGame.gameID(), color);
      Gameplay gameplayREPL = new Gameplay(server, joinGame, color);
      gameplayREPL.run();
    } else {
      out.println("Game does not exist or color taken");
      printJoin();
    }
  }

  private void handleObserve(String[] input) {
    if (input.length != 2 || !input[1].matches("\\d")) {
      out.println("Please provide a game ID");
      printObserve();
      return;
    }
    int gameObservedNum = Integer.parseInt(input[1]);
    if (games.isEmpty() || games.size() <= gameObservedNum) {
      refreshGames();
      if (games.isEmpty()) {
        out.println("Error: please first create a game");
        return;
      }
      if (games.size() <= gameObservedNum) {
        out.println("Error: that Game ID does not exist");
        printGames();
        return;
      }
    }
    GameData observeGame = games.get(gameObservedNum);
    if (server.joinGame(observeGame.gameID(), null)) {
      out.println("You have joined the game as an observer");
      inGame = true;
      server.connectWS();
      server.joinObserver(observeGame.gameID());
      Gameplay gameplayREPL = new Gameplay(server, observeGame, null);
      gameplayREPL.run();
      return;
    } else {
      out.println("Game does not exist");
      printObserve();
      return;
    }
  }
}