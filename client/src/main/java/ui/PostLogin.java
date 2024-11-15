package ui;
import chess.ChessBoard;
import chess.ChessGame;
import model.GameData;
import client.ServerFacade;
import ui.EscapeSequences.*;
import java.util.*;
import static java.lang.System.out;
import static ui.EscapeSequences.*;
public class PostLogin {
  ServerFacade server;
  List<GameData> games;
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
          int gameID = server.createGame(input[1]);
          out.printf("Created game");
          refreshGames();
          break;
        case "join":
          if (input.length != 3) {
            out.println("Please provide a game ID and color choice");
            printJoin();
            break;
          }
          int gameIndexToJoin = Integer.parseInt(input[1]) - 1;  // Subtract 1 to match the actual list index
          if (gameIndexToJoin >= 0 && gameIndexToJoin < games.size()) {
            GameData joinGame = games.get(gameIndexToJoin);
            if (server.joinGame(joinGame.gameID(), input[2].toUpperCase())) {
              out.println("You have joined the game");
              new BoardPrinter(new ChessGame().getBoard()).printBoard();
            } else {
              out.println("Game does not exist or color taken");
              printJoin();
            }
          } else {
            out.println("Invalid game ID");
            printJoin();
          }
          break;
        case "observe":
          if (input.length != 2) {
            out.println("Please provide a game ID");
            printObserve();
            break;
          }
          int gameIndexToObserve = Integer.parseInt(input[1]) - 1;  // Subtract 1 to match the actual list index
          if (gameIndexToObserve >= 0 && gameIndexToObserve < games.size()) {
            GameData observeGame = games.get(gameIndexToObserve);
            if (server.joinGame(observeGame.gameID(), null)) {
              out.println("You have joined the game as an observer");
              new BoardPrinter(observeGame.game().getBoard()).printBoard();
            } else {
              out.println("Game does not exist");
              printObserve();
            }
          } else {
            out.println("Invalid game ID");
            printObserve();
          }
          break;
      }
    }
    PreLogin preloginREPL = new PreLogin(server);
    preloginREPL.run();
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
    HashSet<GameData> gameList = server.listGames();
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
}