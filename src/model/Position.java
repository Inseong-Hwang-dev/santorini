package model;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


/**
 * Position on the game board defined by a row and column coordinate.
 * Provide methods for getting adjacent positions and comparing positions.
 */
public class Position {
    private final int x;
    private final int y;

    /**
     * Creates a new position with the specified row and column coordinates.
     *
     * @param x the x coordinate (zero-based)
     * @param y the y coordinate (zero-based)
     */

    public Position(int x, int y) {
        this.x = x;
        this.y = y;
    }

    /**
     * Returns the x coordinate of this position.
     *
     * @return the x coordinate
     */
    public int getX() {
        return x;
    }

    /**
     * Returns the y coordinate of this position.
     *
     * @return the y coordinate
     */
    public int getY() {
        return y;
    }

    /**
     * Returns a list of all positions adjacent to this position, including diagonals.
     * Does not filter for valid board positions; that should be done by the caller.
     *
     * @return list of all eight adjacent positions
     */

    public List<Position> getAdjacentPositions() {
        List<Position> adjacentPositions = new ArrayList<>();
        for (int i = -1; i <= 1; i++) {
            for (int j = -1; j <= 1; j++) {
                if (i == 0 && j == 0) {
                    continue;
                }
                adjacentPositions.add(new Position(x + i, y + j));
            }
        }
        return adjacentPositions;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Position position = (Position) o;
        return getX() == position.getX() && getY() == position.getY();
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y);
    }

    @Override
    public String toString() {
        return "(" + x + "," + y + ")";
    }
}


