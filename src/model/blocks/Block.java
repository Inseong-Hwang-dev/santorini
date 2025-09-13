package model.blocks;

/**
 * Interface for a block in the Santorini game.
 * Blocks are used to build towers on the game board.
 */

public interface Block {

    /**
     * Checks if this block is a dome.
     * Domes represent the final level of a tower that cannot be built upon.
     *
     * @return true if this block is a dome, false otherwise
     */
    boolean isDome();
}
