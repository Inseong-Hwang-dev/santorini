package model.blocks;

/**
 * Block that is a dome in the Santorini game.
 * Domes are the final level of a tower and cannot be built upon.
 */

public class DomeBlock implements Block
{
    /**
     * Creates a new dome block.
     */
    public DomeBlock() {
    }

    @Override
    public boolean isDome() {
        return true;
    }

    @Override
    public String toString() {
        return "DomeBlock()";
    }
}
