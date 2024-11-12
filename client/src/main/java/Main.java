import chess.*;
import client.ServerFacade;

public class Main {
    public static void main(String[] args) {
        var piece = new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.PAWN);
        System.out.println("♕ 240 Chess Client: " + piece);

        ServerFacade server = new ServerFacade();
        System.out.println(server.register("username", "password", "email"));
        System.out.println(server.login("username", "password"));
    }
}