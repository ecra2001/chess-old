package chess;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;

/**
 * Represents a single chess piece
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessPiece {
    private PieceType pieceType;
    private ChessGame.TeamColor  teamColor;
    public ChessPiece(ChessGame.TeamColor pieceColor, ChessPiece.PieceType type) {
        this.pieceType = type;
        this.teamColor = pieceColor;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ChessPiece that=(ChessPiece) o;
        return pieceType == that.pieceType && teamColor == that.teamColor;
    }

    @Override
    public int hashCode() {
        return Objects.hash(pieceType, teamColor);
    }

    @Override
    public String toString() {
        return "ChessPiece{" +
                "pieceType=" + pieceType +
                ", teamColor=" + teamColor +
                '}';
    }

    /**
     * The various different chess piece options
     */
    public enum PieceType {
        KING,
        QUEEN,
        BISHOP,
        KNIGHT,
        ROOK,
        PAWN
    }

    /**
     * @return Which team this chess piece belongs to
     */
    public ChessGame.TeamColor getTeamColor() {
        return this.teamColor;
    }

    /**
     * @return which type of chess piece this piece is
     */
    public PieceType getPieceType() {
        return this.pieceType;
    }

    /**
     * Calculates all the positions a chess piece can move to
     * Does not take into account moves that are illegal due to leaving the king in
     * danger
     *
     * @return Collection of valid moves
     */
    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition myPosition) {
        Collection<ChessMove> moves = new ArrayList<>();
        switch (this.pieceType) {
            case PAWN:
                addPawnMoves(board, myPosition, moves);
                break;
            case ROOK:
                addLinearMoves(board, myPosition, moves, new int[][]{{1, 0}, {-1, 0}, {0, 1}, {0, -1}});
                break;
            case KNIGHT:
                addKnightMoves(board, myPosition, moves);
                break;
            case BISHOP:
                addLinearMoves(board, myPosition, moves, new int[][]{{1, 1}, {-1, -1}, {1, -1}, {-1, 1}});
                break;
            case QUEEN:
                addLinearMoves(board, myPosition, moves, new int[][]{{1, 0}, {-1, 0}, {0, 1}, {0, -1}, {1, 1}, {-1, -1}, {1, -1}, {-1, 1}});
                break;
            case KING:
                addKingMoves(board, myPosition, moves);
                break;
        }
        return moves;
    }
    private void addPawnMoves(ChessBoard board, ChessPosition myPosition, Collection<ChessMove> moves) {
        int direction = (this.teamColor == ChessGame.TeamColor.WHITE) ? 1 : -1;
        int row = myPosition.getRow();
        int col = myPosition.getColumn();

        // Regular move forward
        ChessPosition oneStepForward = new ChessPosition(row + direction, col);
        if (board.getPiece(oneStepForward) == null) {
            moves.add(new ChessMove(myPosition, oneStepForward, null));

            // Two steps forward if in the starting row
            if ((this.teamColor == ChessGame.TeamColor.WHITE && row == 1) || (this.teamColor == ChessGame.TeamColor.BLACK && row == 6)) {
                ChessPosition twoStepsForward = new ChessPosition(row + 2 * direction, col);
                if (board.getPiece(twoStepsForward) == null) {
                    moves.add(new ChessMove(myPosition, twoStepsForward, null));
                }
            }
        }

        // Capture diagonally
        ChessPosition[] diagonalMoves = {
                new ChessPosition(row + direction, col - 1),
                new ChessPosition(row + direction, col + 1)
        };
        for (ChessPosition diagMove : diagonalMoves) {
            ChessPiece target = board.getPiece(diagMove);
            if (target != null && target.getTeamColor() != this.teamColor) {
                moves.add(new ChessMove(myPosition, diagMove, null)); // Add a capture move
            }
        }
    }
    private void addLinearMoves(ChessBoard board, ChessPosition myPosition, Collection<ChessMove> moves, int[][] directions) {
        int row = myPosition.getRow();
        int col = myPosition.getColumn();

        for (int[] direction : directions) {
            int dRow = direction[0];
            int dCol = direction[1];

            int newRow = row + dRow;
            int newCol = col + dCol;

            while (isValidPosition(newRow, newCol)) {
                ChessPosition newPosition = new ChessPosition(newRow, newCol);
                ChessPiece target = board.getPiece(newPosition);
                if (target == null) {
                    moves.add(new ChessMove(myPosition, newPosition, null));  // Add regular move
                } else {
                    if (target.getTeamColor() != this.teamColor) {
                        moves.add(new ChessMove(myPosition, newPosition, null));  // Add capture move
                    }
                    break;  // Stop once we hit another piece
                }
                newRow += dRow;
                newCol += dCol;
            }
        }
    }

    private boolean isValidPosition(int row, int col) {
        return row >= 0 && row < 8 && col >= 0 && col < 8;
    }
    private void addKnightMoves(ChessBoard board, ChessPosition myPosition, Collection<ChessMove> moves) {
        int row = myPosition.getRow();
        int col = myPosition.getColumn();
        int[][] knightMoves = {
                {2, 1}, {2, -1}, {-2, 1}, {-2, -1}, {1, 2}, {1, -2}, {-1, 2}, {-1, -2}
        };

        for (int[] move : knightMoves) {
            int newRow = row + move[0];
            int newCol = col + move[1];
            if (isValidPosition(newRow, newCol)) {
                ChessPosition newPosition = new ChessPosition(newRow, newCol);
                ChessPiece target = board.getPiece(newPosition);
                if (target == null || target.getTeamColor() != this.teamColor) {
                    moves.add(new ChessMove(myPosition, newPosition, null));
                }
            }
        }
    }
    private void addKingMoves(ChessBoard board, ChessPosition myPosition, Collection<ChessMove> moves) {
        int row = myPosition.getRow();
        int col = myPosition.getColumn();
        int[][] kingMoves = {
                {1, 0}, {-1, 0}, {0, 1}, {0, -1}, {1, 1}, {-1, -1}, {1, -1}, {-1, 1}
        };

        for (int[] move : kingMoves) {
            int newRow = row + move[0];
            int newCol = col + move[1];
            if (isValidPosition(newRow, newCol)) {
                ChessPosition newPosition = new ChessPosition(newRow, newCol);
                ChessPiece target = board.getPiece(newPosition);
                if (target == null || target.getTeamColor() != this.teamColor) {
                    moves.add(new ChessMove(myPosition, newPosition, null));
                }
            }
        }
    }
}
