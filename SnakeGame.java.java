import java.util.*;
import java.io.*;

class Position {
    int row, col;

    public Position(int row, int col) {
        this.row = row;
        this.col = col;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Position)) return false;
        Position other = (Position) o;
        return this.row == other.row && this.col == other.col;
    }

    @Override
    public int hashCode() {
        return Objects.hash(row, col);
    }
}

public class SnakeGame {
    Deque<Position> snake;
    Queue<Position> food;
    Set<Position> visited;

    int rows, cols;
    boolean gameOver;

    char direction;

    public SnakeGame(int rows, int cols) {
        gameOver = false;
        snake = new LinkedList<>();
        food = new LinkedList<>();
        visited = new HashSet<>();

        this.rows = rows;
        this.cols = cols;

        Position start = new Position(0, 0);
        snake.addFirst(start);
        visited.add(start);

        direction = 'R';
        generateFood();
    }

    public void move(char input) {
        direction = input;

        Position head = snake.peekFirst();
        int row = head.row;
        int col = head.col;

        if (input == 'U') row--;
        else if (input == 'D') row++;
        else if (input == 'L') col--;
        else if (input == 'R') col++;

        Position newHead = new Position(row, col);

        if (!validMove(newHead)) {
            System.out.println("💀 Game Over!");
            gameOver = true;
            return;
        }

        snake.addFirst(newHead);
        visited.add(newHead);

        if (!food.isEmpty() && food.peek().equals(newHead)) {
            food.poll(); // eat food
            generateFood(); // place new food
        } else {
            Position tail = snake.removeLast(); // move forward
            visited.remove(tail);
        }

        printBoard();
    }

    public void generateFood() {
        Random rand = new Random();
        Position pos;
        do {
            pos = new Position(rand.nextInt(rows), rand.nextInt(cols));
        } while (visited.contains(pos));
        food.add(pos);
    }

    public boolean validMove(Position position) {
        int r = position.row;
        int c = position.col;
        if (r < 0 || r >= rows || c < 0 || c >= cols || visited.contains(position)) {
            return false;
        }
        return true;
    }

    private void printBoard() {
        char[][] board = new char[rows][cols];
        for (char[] row : board) Arrays.fill(row, '.');

        for (Position p : snake) board[p.row][p.col] = 'S';
        if (!food.isEmpty()) {
            Position f = food.peek();
            board[f.row][f.col] = 'F';
        }

        for (char[] row : board) {
            for (char c : row) System.out.print(c + " ");
            System.out.println();
        }
        System.out.println();
    }

    public static void main(String[] args) throws IOException {
        SnakeGame game = new SnakeGame(10, 10);
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

        System.out.println("🟢 Snake Game Started! Use U/D/L/R to move.");
        game.printBoard();

        while (!game.gameOver) {
            System.out.print("Move: ");
            String input = br.readLine();
            if (input.isEmpty()) continue;
            game.move(input.charAt(0));
        }
    }
}
