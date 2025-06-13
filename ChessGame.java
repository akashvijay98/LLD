import java.util.*;

enum Color {
    WHITE, BLACK
}

class Position {
    int row, col;

    public Position(int row, int col) {
        this.row = row;
        this.col = col;
    }
}

abstract class Piece {
    Color color;
    Position position;

    public Piece(Color color, Position position) {
        this.position = position;
        this.color = color;
    }

    abstract List<Position> getLegalMoves(Board board);
}

class Pawn extends Piece {
    public Pawn(Position position, Color color) {
        super(color, position);
    }

    @Override
    public List<Position> getLegalMoves(Board board) {
        List<Position> moves = new ArrayList<>();

        int direction = (this.color == Color.WHITE) ? -1 : 1;
        int newRow = position.row + direction;

        if (board.inBounds(newRow, position.col) && board.getPieceAt(newRow, position.col) == null) {
            moves.add(new Position(newRow, position.col));
        }

        for (int dc = -1; dc <= 1; dc += 2) {
            int newCol = position.col + dc;

            if (board.inBounds(newRow, newCol)) {
                Piece target = board.getPieceAt(newRow, newCol);

                if (target != null && target.color != this.color) {
                    moves.add(new Position(newRow, newCol));
                }
            }
        }

        return moves;
    }
}

class Board {
    Piece[][] grid = new Piece[8][8];

    public Piece getPieceAt(int row, int col) {
        return grid[row][col];
    }

    public boolean inBounds(int row, int col) {
        return row >= 0 && row < 8 && col >= 0 && col < 8;
    }

    public void movePiece(Position source, Position target) {
        Piece curPiece = grid[source.row][source.col];

        if (curPiece == null) {
            throw new RuntimeException("No piece at position");
        }

        List<Position> validMoves = curPiece.getLegalMoves(this);

        for (Position move : validMoves) {
            if (target.row == move.row && target.col == move.col) {
                grid[target.row][target.col] = curPiece;
                grid[source.row][source.col] = null;
                curPiece.position = target;
                return;
            }
        }

        throw new RuntimeException("Invalid move");
    }

    public void placePiece(Piece piece) {
        grid[piece.position.row][piece.position.col] = piece;
    }
}

class ChessGame {
    Board board = new Board();
    Color currentTurn = Color.WHITE;

    public void startGame() {
        for (int i = 0; i < 8; i++) {
            board.placePiece(new Pawn(new Position(1, i), Color.BLACK));
            board.placePiece(new Pawn(new Position(6, i), Color.WHITE));
        }
    }

    public boolean makeMove(Position from, Position to) {
        Piece piece = board.getPieceAt(from.row, from.col);

        if (piece == null || piece.color != currentTurn) {
            return false;
        }

        try {
            board.movePiece(from, to);
            currentTurn = (currentTurn == Color.WHITE) ? Color.BLACK : Color.WHITE;
            return true;
        } catch (RuntimeException e) {
            return false;
        }
    }
}

// change class Name to fileName
public class ChessGame {
    public static void main(String[] args) {
        ChessGame game = new ChessGame();
        game.startGame();

        System.out.println("Initial board:");
        printBoard(game.board);

        // Example: Move white pawn from (6, 4) to (5, 4)
        Position from = new Position(6, 4);
        Position to = new Position(5, 4);
        System.out.println("\nWhite moves from (6,4) to (5,4): " + game.makeMove(from, to));
        printBoard(game.board);

        // Example: Move black pawn from (1, 3) to (2, 3)
        from = new Position(1, 3);
        to = new Position(2, 3);
        System.out.println("\nBlack moves from (1,3) to (2,3): " + game.makeMove(from, to));
        printBoard(game.board);
    }

    static void printBoard(Board board) {
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                Piece p = board.getPieceAt(row, col);
                if (p == null) {
                    System.out.print(". ");
                } else if (p instanceof Pawn) {
                    System.out.print(p.color == Color.WHITE ? "P " : "p ");
                } else {
                    System.out.print("? ");
                }
            }
            System.out.println();
        }
    }
}
