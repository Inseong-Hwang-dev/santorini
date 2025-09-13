package model.blocks;

/**
 * Implementation of Block that represents a level block in the Santorini game.
 * Level blocks from the levels 1-3 of the towers.
 */
public class LevelBlock implements Block
{
    private final int level;

    /**
     * Creates a new level block with the specified level.
     *
     * @param level the level of this block 1-3
     */
    public LevelBlock(int level) {
        this.level = level;
    }

    public LevelBlock() {
        this.level = 1;
    }

    @Override
    public boolean isDome() {
        return false;
    }

    @Override
    public String toString() {
        return "LevelBlock [level=" + level + "]";
    }
}
