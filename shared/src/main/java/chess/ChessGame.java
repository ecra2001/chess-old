package chess;

import java.util.Collection;
import java.util.Objects;
import java.util.ArrayList;

/**
 * For a class that can manage a chess game, making moves on a board
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessGame {
    private TeamColor teamTurn;
    private ChessBoard board;
    private boolean gameOver;
    public ChessGame() {
        teamTurn = TeamColor.WHITE;
        board = new ChessBoard();
        board.resetBoard();
    }

    /**
     * @return Which team's turn it is
     */
    public TeamColor getTeamTurn() {
        return teamTurn;
    }

    /**
     * Set's which teams turn it is
     *
     * @param team the team whose turn it is
     */
    public void setTeamTurn(TeamColor team) {
        teamTurn = team;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {return true;}
        if (o == null || getClass() != o.getClass()) {return false;}
        ChessGame chessGame=(ChessGame) o;
        return teamTurn == chessGame.teamTurn && Objects.equals(board, chessGame.board);
    }

    @Override
    public int hashCode() {
        return Objects.hash(teamTurn, board);
    }

    @Override
    public String toString() {
        return "ChessGame{" +
                "teamTurn=" + teamTurn +
                ", board=" + board +
                '}';
    }

    /**
     * Enum identifying the 2 possible teams in a chess game
     */
    public enum TeamColor {
        WHITE,
        BLACK
    }

    /**
     * Gets a valid moves for a piece at the given location
     *
     * @param startPosition the piece to get valid moves for
     * @return Set of valid moves for requested piece, or null if no piece at
     * startPosition
     */
    public Collection<ChessMove> validMoves(ChessPosition startPosition) {
        ChessPiece piece = board.getPiece(startPosition);
        if (piece == null) {
            return null;
        }

        Collection<ChessMove> potentialMoves = piece.pieceMoves(board, startPosition);
        Collection<ChessMove> validMoves = new ArrayList<>();

        // Simulation
        for (ChessMove move : potentialMoves) {
            ChessBoard tempBoard = simulateMove(board, move);
            ChessGame tempGame = new ChessGame();
            tempGame.setBoard(tempBoard);

            if (!tempGame.isInCheck(piece.getTeamColor())) {
                validMoves.add(move);
            }
        }

        return validMoves;
    }

    /**
     * Makes a move in a chess game
     *
     * @param move chess move to preform
     * @throws InvalidMoveException if move is invalid
     */
    public void makeMove(ChessMove move) throws InvalidMoveException {
        Collection<ChessMove> validMoves = validMoves(move.getStartPosition());
        ChessPiece piece = board.getPiece(move.getStartPosition());
        if (piece == null || piece.getTeamColor() != teamTurn) {
            throw new InvalidMoveException("Invalid move: not team's turn");
        }
        if (validMoves == null || !validMoves.contains(move) || isInCheck(teamTurn)) {
            throw new InvalidMoveException("Invalid move");
        }

        board.addPiece(move.getEndPosition(), piece);
        board.addPiece(move.getStartPosition(), null);  // Remove piece from start position

        // Pawn promotion
        if (piece.getPieceType() == ChessPiece.PieceType.PAWN) {
            int promotionRow = (piece.getTeamColor() == TeamColor.WHITE) ? 8 : 1;
            if (move.getEndPosition().getRow() == promotionRow && move.getPromotionPiece() != null) {
                board.addPiece(move.getEndPosition(), new ChessPiece(piece.getTeamColor(), move.getPromotionPiece()));
            }
        }

        // Handle turn switching after move
        teamTurn = (teamTurn == TeamColor.WHITE) ? TeamColor.BLACK : TeamColor.WHITE;
    }

    /**
     * Determines if the given team is in check
     *
     * @param teamColor which team to check for check
     * @return True if the specified team is in check
     */
    public boolean isInCheck(TeamColor teamColor) {
        ChessPosition kingPosition = findKing(teamColor);
        for (int row = 1; row < 9; row++) {
            for (int col = 1; col < 9; col++) {
                if (canAttackKing(teamColor, kingPosition, row, col)) {
                    return true;
                }
            }
        }
        return false; // The king is not in check
    }

    private boolean canAttackKing(TeamColor teamColor, ChessPosition kingPosition, int row, int col) {
        ChessPiece opponentPiece = board.getPiece(new ChessPosition(row, col));
        if (opponentPiece == null || opponentPiece.getTeamColor() == teamColor) {
            return false;
        }

        Collection<ChessMove> moves = opponentPiece.pieceMoves(board, new ChessPosition(row, col));
        for (ChessMove move : moves) {
            if (move.getEndPosition().equals(kingPosition)) {
                return true;
            }
        }
        return false;
    }

    private ChessPosition findKing(TeamColor teamColor) {
        for (int row = 1; row < 9; row++) {
            for (int col = 1; col < 9; col++) {
                ChessPiece piece = board.getPiece(new ChessPosition(row, col));
                if (piece != null && piece.getPieceType() == ChessPiece.PieceType.KING && piece.getTeamColor() == teamColor) {
                    return new ChessPosition(row, col);
                }
            }
        }
        return null;
    }

    /**
     * Determines if the given team is in checkmate
     *
     * @param teamColor which team to check for checkmate
     * @return True if the specified team is in checkmate
     */
    public boolean isInCheckmate(TeamColor teamColor) {
        if (!isInCheck(teamColor)) {
            return false;
        }

        return scanBoard(teamColor);
    }

    private ChessBoard simulateMove(ChessBoard originalBoard, ChessMove move) {
        ChessBoard simulatedBoard = new ChessBoard();

        for (int row = 1; row <= 8; row++) {
            for (int col = 1; col <= 8; col++) {
                ChessPosition position = new ChessPosition(row, col);
                ChessPiece piece = originalBoard.getPiece(position);
                if (piece != null) {simulatedBoard.addPiece(position, piece);}
            }
        }

        ChessPiece pieceToMove = simulatedBoard.getPiece(move.getStartPosition());
        simulatedBoard.addPiece(move.getEndPosition(), pieceToMove);
        simulatedBoard.addPiece(move.getStartPosition(), null);

        return simulatedBoard;
    }

    /**
     * Determines if the given team is in stalemate, which here is defined as having
     * no valid moves
     *
     * @param teamColor which team to check for stalemate
     * @return True if the specified team is in stalemate, otherwise false
     */
    public boolean isInStalemate(TeamColor teamColor) {
        if (isInCheck(teamColor)) {
            return false;
        }

        return scanBoard(teamColor);
    }

    public boolean scanBoard(TeamColor teamColor) {
        for (int row = 1; row <= 8; row++) {
            for (int col = 1; col <= 8; col++) {
                if (canAvoidCheck(teamColor, row, col)) {
                    return false;
                }
            }
        }
        return true;
    }

    private boolean canAvoidCheck(TeamColor teamColor, int row, int col) {
        ChessPiece piece = board.getPiece(new ChessPosition(row, col));
        if (piece == null || piece.getTeamColor() != teamColor) {
            return false;
        }

        Collection<ChessMove> validMoves = piece.pieceMoves(board, new ChessPosition(row, col));
        for (ChessMove move : validMoves) {
            if (isSafeAfterMove(teamColor, move)) {
                return true;
            }
        }
        return false;
    }

    private boolean isSafeAfterMove(TeamColor teamColor, ChessMove move) {
        ChessBoard tempBoard = simulateMove(board, move);
        ChessGame tempGame = new ChessGame();
        tempGame.setBoard(tempBoard);
        tempGame.setTeamTurn(teamColor);
        return !tempGame.isInCheck(teamColor);
    }


    /**
     * Sets this game's chessboard with a given board
     *
     * @param board the new board to use
     */
    public void setBoard(ChessBoard board) {
        this.board = board;
    }

    /**
     * Gets the current chessboard
     *
     * @return the chessboard
     */
    public ChessBoard getBoard() {
        return board;
    }

    public void setGameOver(boolean gameOver) {
        this.gameOver = gameOver;
    }
    public boolean getGameOver() {
        return gameOver;
    }
}