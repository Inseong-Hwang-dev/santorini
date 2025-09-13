package model.actions;

import model.Cell;
import model.Worker;
import model.blocks.Block;
import model.blocks.DomeBlock;
import model.blocks.LevelBlock;

/**
 * Represents a build action in the game.
 */
public class BuildAction implements Action {
    private final Worker worker;
    private final Cell destination;

    /**
     * Creates a new build action.
     *
     * @param worker the worker performing the build
     * @param destination the destination cell to build on
     */
    public BuildAction(Worker worker, Cell destination) {
        this.worker = worker;
        this.destination = destination;
    }

    @Override
    public boolean execute() {
        if (!validate()) {
            return false;
        }

        // Check if we need to build a dome or a level block
        int currentHeight = destination.getHeight();

        if (currentHeight == 3) {
            // Add a dome to complete the tower
            destination.addBlock(new DomeBlock());
            destination.setDome();
        } else {
            // Add a level block
            destination.addBlock(new LevelBlock(currentHeight + 1));
        }

        return true;
    }

    @Override
    public boolean validate() {
        // Check if worker's cell is adjacent to destination
        if (worker.getCurrentCell() == null || !worker.getCurrentCell().isAdjacentTo(destination)) {
            return false;
        }

        // Check if destination is buildable
        return destination.isBuildable();
    }

    /**
     * Gets the worker performing this build.
     *
     * @return the worker
     */
    public Worker getWorker() {
        return worker;
    }

    /**
     * Gets the destination cell of this build.
     *
     * @return the destination cell
     */
    public Cell getDestination() {
        return destination;
    }
}