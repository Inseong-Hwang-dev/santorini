package model.cards;

import model.turns.Turn;
import model.Cell;
import model.Worker;
import model.actions.MoveAction;
import model.Board;
import model.Position;

/**
 * Triton God Card implementation.
 * Power: Each time your Worker moves into a perimeter space, it may immediately move again.
 */
public class TritonCard implements GodCard {
    private static final String NAME = "Triton";
    private static final String DESCRIPTION =
            "Each time your Worker moves into a perimeter space, it may immediately move again.";

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public String getDescription() {
        return DESCRIPTION;
    }

    /**
     * Checks if the worker can move again after moving to a perimeter cell.
     *
     * @param moveAction the move action to be checked
     * @param board the game board
     * @return true if the worker is on a perimeter cell
     */
    public boolean canMoveAgain(MoveAction moveAction, Board board) {
        Cell destination = moveAction.getDestination();
        Position pos = destination.getPosition();
        // Board size is fixed to 5 (see Board.java)
        int size = 5;
        // Perimeter: x == 0, y == 0, x == size-1, y == size-1
        return pos.getX() == 0 || pos.getY() == 0 ||
               pos.getX() == size - 1 || pos.getY() == size - 1;
    }

    /**
     * Applies Triton's power: allows any move after the first, but only allows another move if the destination is perimeter.
     * Returns true if the move was to a perimeter cell, false otherwise.
     *
     * @param turn the current turn
     * @param moveAction the move action to be checked
     * @return true if the move was to perimeter, false otherwise
     */
    public boolean applyPower(Turn turn, MoveAction moveAction) {
        // First move: set initial position for Artemis logic
        if (turn.getMoves().isEmpty()) {
            Worker worker = moveAction.getWorker();
            worker.setInitialPosition(worker.getCurrentCell().getPosition());
            return canMoveAgain(moveAction, turn.getBoard());
        } else {
            // After the first move, allow any move, but check if destination is perimeter
            return canMoveAgain(moveAction, turn.getBoard());
        }
    }
} 