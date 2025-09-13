package model.cards;

import model.turns.Turn;
import model.Cell;
import model.Worker;
import model.actions.MoveAction;

/**
 * Artemis God Card implementation.
 * Power: Your Worker may move one additional time, but not back to its initial space.
 */
public class ArtemisCard implements GodCard {
    private static final String NAME = "Artemis";
    private static final String DESCRIPTION =
            "Your Worker may move one additional time, but not back to its initial space.";

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public String getDescription() {
        return DESCRIPTION;
    }

    /**
     * Modifies move rules for Artemis' power, allowing a second move if possible.
     *
     * @param moveAction the move action to be modified
     * @return true if the worker can make an additional move
     */
    public boolean canMoveSecondTime(MoveAction moveAction) {
        Worker worker = moveAction.getWorker();
        Cell destination = moveAction.getDestination();

        // Cannot move back to initial position
        return worker.getInitialPosition() == null ||
                !worker.getInitialPosition().equals(destination.getPosition());
    }

    /**
     * Applies Artemis' power to modify move rules.
     * This replaces the logic from Turn.applyGodCardMoveRules.
     *
     * @param turn the current turn
     * @param moveAction the move action to be modified
     * @return the possibly modified MoveAction, or null if not allowed
     */
    public MoveAction applyPower(Turn turn, MoveAction moveAction) {
        // If this is the second move, check Artemis' restriction
        if (!turn.getMoves().isEmpty()) {
            if (canMoveSecondTime(moveAction)) {
                return moveAction;
            } else {
                return null;
            }
        } else {
            // First move: set initial position for Artemis logic
            Worker worker = moveAction.getWorker();
            worker.setInitialPosition(worker.getCurrentCell().getPosition());
            return moveAction;
        }
    }
}