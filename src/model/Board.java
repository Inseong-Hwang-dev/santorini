package model;

/**
 * The game board with a 5x5 grid of cells.
 */
public class Board {

    private static final int BOARD_SIZE = 5;
    private final Cell[][] cells;

    public Board() {
        cells = new Cell[BOARD_SIZE][BOARD_SIZE];
        initializeBoard();
    }

    private void initializeBoard() {
        // Initialize each cell in the board
        for (int row = 0; row < BOARD_SIZE; row++) {
            for (int col = 0; col < BOARD_SIZE; col++) {
                cells[row][col] = new Cell(new Position(row, col));
            }
        }
    }

    public Cell getCell(Position position) {
        if (isValidPosition(position)) {
            return cells[position.getX()][position.getY()];
        }
        return null;
    }

    public boolean isValidPosition(Position position) {
        int row = position.getX();
        int col = position.getY();
        return row >= 0 && row < BOARD_SIZE && col >= 0 && col < BOARD_SIZE;
    }

    public void placeWorker(Worker worker, Position position) {
        Cell cell = getCell(position);
        if (cell != null && !cell.isOccupied()) {
            worker.setCurrentCell(cell);
        }
    }
}