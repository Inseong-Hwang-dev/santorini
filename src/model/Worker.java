package model;

import model.players.Player;

/**
 * Represents a worker controlled by a player.
 */
public class Worker {

    private final Player owner;
    private Cell currentCell;
    private final String id;
    private Position initialPosition;

    public Worker(Player owner, String id) {
        this.owner = owner;
        this.id = id;
        this.currentCell = null;
    }

    public Player getOwner() {
        return owner;
    }

    public Cell getCurrentCell() {
        return currentCell;
    }

    public void setCurrentCell(Cell cell) {
        if (this.currentCell != null) {
            this.currentCell.removeWorker();
        }
        this.currentCell = cell;
        if (cell != null) {
            cell.setWorker(this);
        }
    }

    public String getId() {
        return id;
    }

    /**
     * Checks if this worker can move to the specified cell.
     *
     * @param destination the target cell
     * @return true if the worker can move there, false otherwise
     */
    public boolean canMoveTo(Cell destination) {
        if (currentCell == null || destination == null) {
            return false;
        }
        if (destination.isOccupied() || destination.hasDome()) {
            return false;
        }
        if (!currentCell.isAdjacentTo(destination)) {
            return false;
        }

        int heightDiff = destination.getHeight() - currentCell.getHeight();
        return heightDiff <= 1;
    }

    /**
     * Gets the initial position of this worker at the start of its move.
     *
     * @return the initial position or null if not set
     */
    public Position getInitialPosition() {
        return initialPosition;
    }

    /**
     * Sets the initial position of this worker for the current turn.
     * Used for God Card abilities.
     *
     * @param position the initial position
     */
    public void setInitialPosition(Position position) {
        this.initialPosition = position;
    }
}