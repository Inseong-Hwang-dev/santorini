package model.actions;

/**
 * Interface for game actions.
 */
public interface Action {
    /**
     * Executes this action.
     *
     * @return true if execution was successful
     */
    boolean execute();

    /**
     * Validates if this action can be executed.
     *
     * @return true if action is valid
     */
    boolean validate();
}