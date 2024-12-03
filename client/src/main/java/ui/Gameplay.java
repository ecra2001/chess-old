package ui;

import chess.ChessGame;
import chess.ChessMove;
import chess.ChessPiece;
import chess.ChessPosition;
import client.ServerFacade;
import model.GameData;
import websocket.messages.Error;
import websocket.messages.LoadGame;
import websocket.messages.Notification;
import websocket.messages.ServerMessage;
import websocket.commands.MakeMove;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Scanner;

import static java.lang.System.out;
import static ui.EscapeSequences.*;

public class Gameplay {

  ServerFacade server;
  public static BoardPrinter boardPrinter;
  ChessGame game;
  int gameID;
  public static ChessGame.TeamColor color;

  public Gameplay(ServerFacade server, GameData gameData, ChessGame.TeamColor color) {
    this.server = server;
    this.game = gameData.game();
    this.gameID = gameData.gameID();
    Gameplay.color = color;

    boardPrinter = new BoardPrinter(game);
  }

  public void run() {
    boolean inGame = true;
    out.print(RESET_TEXT_COLOR + RESET_BG_COLOR);
    while (inGame) {
      String[] input = getUserInput();
      switch (input[0]) {
        case "help":
          printHelpMenu();
          break;
        case "redraw":
          redraw();
          break;
        case "leave":
          inGame = false;
          server.leave(gameID);
          break;
        case "move":
          handleMakeMove(input);
          break;
        case "resign":
          out.println("Are you sure you want to forfeit? (yes/no)");
          String[] confirmation = getUserInput();
          if (confirmation.length == 1 && confirmation[0].equalsIgnoreCase("yes")) {
            server.resign(gameID);
          }
          else {
            out.println("Resignation cancelled");
          }
          break;
        case "highlight":
          if (input.length == 2 && input[1].matches("[a-h][1-8]")) {
            ChessPosition position = new ChessPosition(input[1].charAt(1) - '0', input[1].charAt(0) - ('a'-1));
            boardPrinter.printBoard(color, position);
          }
          else {
            out.println("Please provide a coordinate (ex: 'c3')");
            printHighlight();
          }
          break;
        default:
          out.println("Command not recognized, please try again");
          printHelpMenu();
          break;
      }
    }

    PostLogin postloginREPL = new PostLogin(server);
    postloginREPL.run();
  }

  private String[] getUserInput() {
    String prompt = "IN-GAME";
    out.printf("\n[%s] >>> ", prompt);
    Scanner scanner = new Scanner(System.in);
    return scanner.nextLine().split(" ");
  }

  private void printHelpMenu() {
    out.println("redraw - redraw the game board");
    out.println("leave - leave the current game");
    printMakeMove();
    out.println("resign - forfeit this game");
    printHighlight();
    out.println("help - show this menu");
  }

  private void printMakeMove() {
    out.println("move <from> <to> <promotion_piece> - make a move (Promotion piece should only be used when a move will promote a pawn)");
  }

  private void printHighlight() {
    out.println("highlight <coordinate> - highlight all legal moves for the given piece");
  }

  private void redraw() {
    boardPrinter.printBoard(color, null);
  }

  private void handleMakeMove(String[] input) {
    if (input.length >= 3 && input[1].matches("[a-h][1-8]") && input[2].matches("[a-h][1-8]")) {
      ChessPosition from = new ChessPosition(input[1].charAt(1) - '0', input[1].charAt(0) - ('a'-1));
      ChessPosition to = new ChessPosition(input[2].charAt(1) - '0',input[2].charAt(0) - ('a'-1));

      ChessPiece.PieceType promotion = null;
      if (input.length == 4) {
        promotion = getPieceType(input[3]);
        if (promotion == null) { // If it was improperly typed by the user
          out.println("Please provide proper promotion piece name (ex: 'knight')");
          printMakeMove();
        }
      }

      server.makeMove(gameID, new ChessMove(from, to, promotion));
    }
    else {
      out.println("Please provide a to and from coordinate (ex: 'c3 d5')");
      printMakeMove();
    }
  }

  public ChessPiece.PieceType getPieceType(String name) {
    return switch (name.toUpperCase()) {
      case "QUEEN" -> ChessPiece.PieceType.QUEEN;
      case "BISHOP" -> ChessPiece.PieceType.BISHOP;
      case "KNIGHT" -> ChessPiece.PieceType.KNIGHT;
      case "ROOK" -> ChessPiece.PieceType.ROOK;
      case "PAWN" -> ChessPiece.PieceType.PAWN;
      default -> null;
    };
  }

}