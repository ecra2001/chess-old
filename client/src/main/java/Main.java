import chess.*;
import client.ServerFacade;
import ui.PreLogin;

public class Main {
    public static void main(String[] args) {
        var piece = new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.PAWN);
        System.out.println("♕ 240 Chess Client: " + piece);

        ServerFacade server = new ServerFacade();
        PreLogin prelogin = new PreLogin(server);
        prelogin.run();
//        System.out.println(server.register("username", "password", "email"));
//
//        System.out.println(server.login("username", "password"));
//
//        System.out.println(server.logout());
//
//        int gameID = server.createGame("game1");
//
//        System.out.println(gameID);
//
//        System.out.println(server.listGames());
//
//        System.out.println(server.joinGame(gameID, "WHITE"));
//
//        System.out.println(server.listGames());
    }
}