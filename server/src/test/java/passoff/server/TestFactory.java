package passoff.server;

import com.google.gson.GsonBuilder;
import chess.*;
import org.junit.jupiter.api.Assertions;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class TestFactory {
    // chess functions
    public static ChessBoard getNewBoard() {
        return new ChessBoard();
    }

    public static ChessGame getNewGame() {
        return new ChessGame();
    }

    public static ChessPiece getNewPiece(ChessGame.TeamColor pieceColor, ChessPiece.PieceType type) {
        return new ChessPiece(pieceColor, type);
    }

    public static ChessPosition getNewPosition(int row, int col) {
        return new ChessPosition(row, col);
    }

    public static ChessMove getNewMove(ChessPosition startPosition, ChessPosition endPosition,
                                       ChessPiece.PieceType promotionPiece) {
        return new ChessMove(startPosition, endPosition, promotionPiece);
    }
    // Websocket tests
    public static Long getMessageTime() {
        /*
         * Changing this will change how long tests will wait for the server to send messages.
         * 3000 Milliseconds (3 seconds) will be enough for most computers. Feel free to change as you see fit,
         * just know increasing it can make tests take longer to run.
         * (On the flip side, if you've got a good computer feel free to decrease it)
         *
         * WHILE DEBUGGING the websocket tests, it might be useful to increase this time to give the tests
         * enough time to receive messages you send while debugging. Just make sure to decrease it when you
         * stop debugging and start running the tests again.
         */
        return 3000L;
    }

    static public void validateMoves(ChessBoard board, ChessPiece testPiece, ChessPosition startPosition, Set<ChessMove> validMoves) {
        var pieceMoves = new HashSet<>(testPiece.pieceMoves(board, startPosition));
        Assertions.assertEquals(validMoves, pieceMoves, "Wrong moves");
    }

    final static Map<Character, ChessPiece.PieceType> CHARTOTYPEMAP = Map.of(
            'p', ChessPiece.PieceType.PAWN,
            'n', ChessPiece.PieceType.KNIGHT,
            'r', ChessPiece.PieceType.ROOK,
            'q', ChessPiece.PieceType.QUEEN,
            'k', ChessPiece.PieceType.KING,
            'b', ChessPiece.PieceType.BISHOP);

    public static ChessBoard loadBoard(String boardText) {
        var board = getNewBoard();
        int row = 8;
        int column = 1;
        for (var c : boardText.toCharArray()) {
            switch (c) {
                case '\n' -> {
                    column = 1;
                    row--;
                }
                case ' ' -> column++;
                case '|' -> {
                }
                default -> {
                    ChessGame.TeamColor color = Character.isLowerCase(c) ? ChessGame.TeamColor.BLACK
                            : ChessGame.TeamColor.WHITE;
                    var type = CHARTOTYPEMAP.get(Character.toLowerCase(c));
                    var position = TestFactory.getNewPosition(row, column);
                    var piece = TestFactory.getNewPiece(color, type);
                    board.addPiece(position, piece);
                    column++;
                }
            }
        }
        return board;
    }

    public static Set<ChessMove> loadMoves(ChessPosition startPosition, int[][] endPositions) {
        var validMoves = new HashSet<ChessMove>();
        for (var endPosition : endPositions) {
            validMoves.add(TestFactory.getNewMove(startPosition,
                    TestFactory.getNewPosition(endPosition[0], endPosition[1]), null));
        }
        return validMoves;
    }

    public static GsonBuilder getGsonBuilder() {
        /*                  **NOT APPLICABLE TO MOST STUDENTS**
         * If you would like to change the way the web socket test cases serialize
         * or deserialize chess objects like ChessMove, you may add type adapters here.
         */
        GsonBuilder builder = new GsonBuilder();
        // builder.registerTypeAdapter(ChessMove.class, /*type adapter or json serializer */);
        return builder;
    }

}
