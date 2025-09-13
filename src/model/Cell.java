package model;

import model.blocks.Block;
import model.blocks.LevelBlock;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Represents a cell on the Santorini game board.
 * A cell has a position, can contain blocks that form a tower, and may have a worker on it.
 */
public class Cell {

    private final Position position;
    private Worker worker;
    private final List<Block> blocks;
    private int height;
    private boolean hasDome;

    /**
     * Creates a new cell at the specified position.
     *
     * @param position the position of this cell on the board
     */
    public Cell(Position position) {
        this.position = position;
        this.blocks = new ArrayList<>();
        this.worker = null;
        this.height = 0;
        this.hasDome = false;
    }

    /**
     * Returns the position of this cell.
     *
     * @return the position of this cell
     */
    public Position getPosition() {
        return position;
    }

    /**
     * Returns the worker on this cell, or null if there is no worker.
     *
     * @return the worker on this cell, or null
     */
    public Worker getWorker() {
        return worker;
    }

    /**
     * Places a worker on this cell.
     *
     * @param worker the worker to place on this cell
     */
    public void setWorker(Worker worker) {
        this.worker = worker;
    }

    /**
     * Removes any worker from this cell.
     */
    public void removeWorker() {
        this.worker = null;
    }

    /**
     * Checks if this cell has a worker on it.
     *
     * @return true if there is a worker on this cell, false otherwise
     */
    public boolean hasWorker() {
        return worker != null;
    }

    /**
     * Adds a block to this cell's tower.
     *
     * @param block the block to add
     */
    public void addBlock(Block block) {
        blocks.add(block);
        this.height = blocks.size();
        if (block.isDome()) {
            this.hasDome = true;
        }
    }

    /**
     * Checks if this cell has a dome on top.
     *
     * @return true if the top block is a dome, false otherwise
     */
    public boolean hasDome() {
        return hasDome;
    }

    /**
     * Checks if this cell is buildable (has no worker and no dome).
     *
     * @return true if a block can be built on this cell, false otherwise
     */
    public boolean isBuildable() {
        return !hasWorker() && !hasDome();
    }

    /**
     * Checks if this cell is adjacent to another cell.
     *
     * @param otherCell the cell to check adjacency with
     * @return true if this cell is adjacent to the other cell, false otherwise
     */
    public boolean isAdjacentTo(Cell otherCell) {
        List<Position> adjacentPositions = position.getAdjacentPositions();
        return adjacentPositions.contains(otherCell.getPosition());
    }

    /**
     * Checks if this cell is occupied by a worker.
     *
     * @return true if this cell has a worker, false otherwise
     */
    public boolean isOccupied() {
        return hasWorker();
    }

    /**
     * Returns the list of all blocks in this cell's tower.
     *
     * @return an unmodifiable view of the blocks in this cell
     */
    public List<Block> getBlocks() {
        return List.copyOf(blocks);
    }

    public int getHeight() {
        return blocks.size();
    }

    public void setDome() {
        this.hasDome = true;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Cell cell = (Cell) o;
        return Objects.equals(position, cell.position);
    }

    @Override
    public int hashCode() {
        return Objects.hash(position);
    }

    @Override
    public String toString() {
        return "Cell[position=" + position
                + ", height=" + getHeight()
                + ", hasDome=" + hasDome()
                + ", hasWorker=" + hasWorker() + "]";
    }
}