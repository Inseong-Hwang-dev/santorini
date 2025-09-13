package model.cards;

import model.turns.Turn;
import model.Cell;
import model.actions.BuildAction;

/**
 * Demeter God Card implementation.
 * Power: Your Worker may build one additional time, but not on the same space.
 */
public class DemeterCard implements GodCard {
    private static final String NAME = "Demeter";
    private static final String DESCRIPTION =
            "Your Worker may build one additional time, but not on the same space.";
    private Cell lastBuildLocation;

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public String getDescription() {
        return DESCRIPTION;
    }

    /**
     * Checks if a worker can build a second time with Demeter's power.
     *
     * @param buildAction the build action to be modified
     * @return true if an additional build is allowed, false otherwise
     */
    public boolean canBuildSecondTime(BuildAction buildAction) {
        Cell destination = buildAction.getDestination();

        // Cannot build on the same space twice
        if (lastBuildLocation != null && lastBuildLocation.equals(destination)) {
            return false;
        }

        // Record the last build location
        lastBuildLocation = destination;
        return true;
    }

    /**
     * Resets the last build location at the end of a turn.
     */
    public void resetTurn() {
        lastBuildLocation = null;
    }

    /**
     * Applies Demeter's power to modify build rules.
     * This replaces the logic from Turn.applyGodCardBuildRules.
     *
     * @param turn the current turn
     * @param buildAction the build action to be modified
     * @return the possibly modified BuildAction, or null if not allowed
     */
    public BuildAction applyPower(Turn turn, BuildAction buildAction) {
        if (!turn.getBuilds().isEmpty()) {
            if (canBuildSecondTime(buildAction)) {
                return buildAction;
            } else {
                return null;
            }
        }
        return buildAction;
    }
}