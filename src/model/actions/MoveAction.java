package model.actions;

import model.Cell;
import model.Worker;

/**
 * Represents a move action in the game.
 */
public class MoveAction implements Action {
    private final Worker worker;
    private final Cell destination;

    /**
     * Creates a new move action.
     *
     * @param worker the worker performing the move
     * @param destination the destination cell
     */
    public MoveAction(Worker worker, Cell destination) {
        this.worker = worker;
        this.destination = destination;
    }

    @Override
    public boolean execute() {
        if (!validate()) {
            return false;
        }

        worker.setCurrentCell(destination);
        return true;
    }

    @Override
    public boolean validate() {
        return worker.canMoveTo(destination);
    }

    /**
     * Gets the worker performing this move.
     *
     * @return the worker
     */
    public Worker getWorker() {
        return worker;
    }

    /**
     * Gets the destination cell of this move.
     *
     * @return the destination cell
     */
    public Cell getDestination() {
        return destination;
    }
}