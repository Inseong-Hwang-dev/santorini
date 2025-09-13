package model.ai;

import model.Board;
import model.Cell;
import model.players.ComputerPlayer;
import model.players.Player;
import model.Worker;
import model.turns.Turn;
import model.enums.TurnState;
import model.Position;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Minimax strategy for mid-game turns.
 * Prefers moving to and building on the highest level cells.
 */
public class MinimaxStrategy implements AIStrategy {
    private int depth;
    private final Random random = new Random();

    public MinimaxStrategy(int depth) {
        this.depth = depth;
    }

    @Override
    public void calculateMove(ComputerPlayer player, Turn turn, Board board) {
        Worker selectedWorker;
        // Only proceed if in WORKER_SELECTION state
        if (turn.getState() != TurnState.WORKER_SELECTION) {
            // If a worker is already selected, forcibly execute move/build
            selectedWorker = turn.getSelectedWorker();
            if (selectedWorker == null) {
                turn.setState(TurnState.COMPLETED);
                return;
            }
        } else {
            // 1. Detect all cells from level 3 down to 0 (descending)
            List<Cell> targetCells = new ArrayList<>();
            for (int level = 3; level >= 0; level--) {
                for (int i = 0; i < 5; i++) {
                    for (int j = 0; j < 5; j++) {
                        Cell cell = board.getCell(new Position(i, j));
                        if (cell.getHeight() == level && !cell.isOccupied() && !cell.hasDome()) {
                            targetCells.add(cell);
                        }
                    }
                }
                if (!targetCells.isEmpty()) break; // Only consider the highest available level
            }

            Worker[] workers = player.getWorkers();
            selectedWorker = null;
            Cell targetCell = null;
            int minDist = Integer.MAX_VALUE;
            // 2. Find the closest worker to any highest cell
            for (Cell cell : targetCells) {
                for (Worker worker : workers) {
                    if (worker.getCurrentCell() == null) continue;
                    int dist = Math.abs(worker.getCurrentCell().getPosition().getX() - cell.getPosition().getX())
                            + Math.abs(worker.getCurrentCell().getPosition().getY() - cell.getPosition().getY());
                    if (dist < minDist) {
                        minDist = dist;
                        selectedWorker = worker;
                        targetCell = cell;
                    }
                }
            }

            // 3. If a worker can move to a level 3 cell this turn, do it and win
            if (selectedWorker != null && targetCell != null && targetCell.getHeight() == 3) {
                // Only select if needed
                if (turn.getState() == TurnState.WORKER_SELECTION) {
                    turn.selectWorker(selectedWorker);
                }
                // Check if the worker can move to the target level 3 cell
                Cell currentCell = selectedWorker.getCurrentCell();
                for (int dx = -1; dx <= 1; dx++) {
                    for (int dy = -1; dy <= 1; dy++) {
                        if (dx == 0 && dy == 0) continue;
                        int nx = currentCell.getPosition().getX() + dx;
                        int ny = currentCell.getPosition().getY() + dy;
                        if (nx == targetCell.getPosition().getX() && ny == targetCell.getPosition().getY()) {
                            // Check move validity
                            if (!targetCell.isOccupied() && !targetCell.hasDome() && targetCell.getHeight() <= currentCell.getHeight() + 1) {
                                boolean moveResult = turn.executeMove(targetCell);
                                // Set winner for GUI (Game state will be set to GAME_OVER in switchTurn)
                                player.getGame().getTurnManager().checkWinner();
                                turn.setState(TurnState.COMPLETED); // End turn (AI wins)
                                turn.complete(); // Mark turn as fully complete
                                return;
                            }
                        }
                    }
                }
            }
            if (selectedWorker == null) {
                turn.setState(TurnState.COMPLETED);
                return;
            }
            // Select the chosen worker
            if (turn.getState() == TurnState.WORKER_SELECTION) {
                turn.selectWorker(selectedWorker);
            }

            // 4. Move the worker toward the target cell (preferably directly if possible)
            List<Cell> validMoves = new ArrayList<>();
            Cell currentCell = selectedWorker.getCurrentCell();
            int currentHeight = currentCell.getHeight();
            int maxMoveHeight = -1;
            for (int dx = -1; dx <= 1; dx++) {
                for (int dy = -1; dy <= 1; dy++) {
                    if (dx == 0 && dy == 0) continue;
                    int nx = currentCell.getPosition().getX() + dx;
                    int ny = currentCell.getPosition().getY() + dy;
                    Position pos = new Position(nx, ny);
                    Cell dest = board.getCell(pos);
                    if (dest != null && !dest.isOccupied() && !dest.hasDome() && dest.getHeight() <= currentCell.getHeight() + 1) {
                        validMoves.add(dest);
                        if (dest.getHeight() > maxMoveHeight) {
                            maxMoveHeight = dest.getHeight();
                        }
                    }
                }
            }

            // Prefer moving to the highest possible cell (that is higher than current)
            Cell moveDest = null;
            for (Cell cell : validMoves) {
                if (cell.getHeight() > currentHeight && cell.getHeight() == maxMoveHeight) {
                    moveDest = cell;
                    break;
                }
            }
            // If no higher cell is available, move to any valid cell (fallback: highest among equals or random)
            if (moveDest == null && !validMoves.isEmpty()) {
                // Prefer the highest among valid moves
                for (Cell cell : validMoves) {
                    if (cell.getHeight() == maxMoveHeight) {
                        moveDest = cell;
                        break;
                    }
                }
                // If still null, just pick the first
                if (moveDest == null) {
                    moveDest = validMoves.get(0);
                }
            }
            if (moveDest == null) {
                turn.setState(TurnState.COMPLETED);
                return;
            }
            boolean moveResult = turn.executeMove(moveDest);
            // Ensure turn state is BUILDING before build phase
            if (turn.getState() != TurnState.BUILDING && turn.getState() != TurnState.SECOND_BUILD) {
                turn.setState(TurnState.BUILDING);
            }
        }

        // Always check for a winning move for all workers, regardless of turn state
        for (Worker worker : player.getWorkers()) {
            if (worker.getCurrentCell() == null) continue;
            Cell currentCell = worker.getCurrentCell();
            for (int dx = -1; dx <= 1; dx++) {
                for (int dy = -1; dy <= 1; dy++) {
                    if (dx == 0 && dy == 0) continue;
                    int nx = currentCell.getPosition().getX() + dx;
                    int ny = currentCell.getPosition().getY() + dy;
                    Position pos = new Position(nx, ny);
                    Cell dest = board.getCell(pos);
                    if (dest != null && !dest.isOccupied() && !dest.hasDome() && dest.getHeight() == 3 && dest.getHeight() <= currentCell.getHeight() + 1) {
                        // Select this worker if not already selected
                        if (turn.getSelectedWorker() != worker) {
                            if (turn.getState() != TurnState.WORKER_SELECTION) {
                                turn.unselectWorker();
                            }
                            if (turn.getState() == TurnState.WORKER_SELECTION) {
                                turn.selectWorker(worker);
                            }
                        }
                        // Move directly to level 3 cell and win
                        boolean moveResult = turn.executeMove(dest);
                        // Set winner for GUI (Game state will be set to GAME_OVER in switchTurn)
                        player.getGame().getTurnManager().checkWinner();
                        turn.setState(TurnState.COMPLETED); // End turn (AI wins)
                        turn.complete(); // Mark turn as fully complete
                        return;
                    }
                }
            }
        }

        // Execute build (prefer highest cell)
        List<Cell> buildOptions = new ArrayList<>();
        Cell buildFrom = selectedWorker.getCurrentCell();
        int maxBuildHeight = -1;
        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = -1; dy <= 1; dy++) {
                if (dx == 0 && dy == 0) continue;
                int nx = buildFrom.getPosition().getX() + dx;
                int ny = buildFrom.getPosition().getY() + dy;
                Position pos = new Position(nx, ny);
                Cell dest = board.getCell(pos);
                if (dest != null && !dest.isOccupied() && !dest.hasDome() && dest.getHeight() < 4) {
                    buildOptions.add(dest);
                    if (dest.getHeight() > maxBuildHeight) {
                        maxBuildHeight = dest.getHeight();
                    }
                }
            }
        }

        // Filter out build options: do not build on level 2 if an opponent worker is adjacent
        Player opponent = (player == player.getGame().getPlayer1()) ? player.getGame().getPlayer2() : player.getGame().getPlayer1();
        List<Cell> filteredBuildOptions = new ArrayList<>();
        for (Cell buildCell : buildOptions) {
            boolean skip = false;
            if (buildCell.getHeight() == 2) {
                // Check if any opponent worker is adjacent
                for (Worker oppWorker : opponent.getWorkers()) {
                    Cell oppCell = oppWorker.getCurrentCell();
                    if (oppCell == null) continue;
                    for (int dx = -1; dx <= 1; dx++) {
                        for (int dy = -1; dy <= 1; dy++) {
                            if (dx == 0 && dy == 0) continue;
                            int nx = buildCell.getPosition().getX() + dx;
                            int ny = buildCell.getPosition().getY() + dy;
                            if (nx == oppCell.getPosition().getX() && ny == oppCell.getPosition().getY()) {
                                skip = true;
                                break;
                            }
                        }
                        if (skip) break;
                    }
                    if (skip) break;
                }
            }
            if (!skip) filteredBuildOptions.add(buildCell);
        }
        if (filteredBuildOptions.isEmpty()) {
            filteredBuildOptions = buildOptions; // fallback: if all are filtered, use original
        }

        // Block opponent from winning by placing a dome on level 3 if possible
        List<Cell> level3Cells = new ArrayList<>();
        for (Cell cell : filteredBuildOptions) {
            if (cell.getHeight() == 3) {
                level3Cells.add(cell);
            }
        }
        Cell blockCell = null;
        for (Cell level3 : level3Cells) {
            // For each opponent worker, check if they can move to this level 3 cell next turn
            for (Worker oppWorker : opponent.getWorkers()) {
                Cell oppCell = oppWorker.getCurrentCell();
                if (oppCell == null) continue;
                for (int dx = -1; dx <= 1; dx++) {
                    for (int dy = -1; dy <= 1; dy++) {
                        if (dx == 0 && dy == 0) continue;
                        int nx = oppCell.getPosition().getX() + dx;
                        int ny = oppCell.getPosition().getY() + dy;
                        if (nx == level3.getPosition().getX() && ny == level3.getPosition().getY()) {
                            // Check if opponent can move up to level 3
                            if (!level3.isOccupied() && !level3.hasDome() && level3.getHeight() <= oppCell.getHeight() + 1) {
                                blockCell = level3;
                                break;
                            }
                        }
                    }
                    if (blockCell != null) break;
                }
                if (blockCell != null) break;
            }
            if (blockCell != null) break;
        }
        boolean buildSuccess = false;
        if (blockCell != null) {
            // Try to build (dome) on the level 3 cell to block opponent
            buildSuccess = turn.executeBuild(blockCell);
            if (!buildSuccess) {
                System.out.println("Build failed at blockCell. Trying other options.");
            } else {
                return;
            }
        }
        // Default: build on the highest cell
        List<Cell> bestBuilds = new ArrayList<>();
        for (Cell buildDest : filteredBuildOptions) {
            if (buildDest.getHeight() == maxBuildHeight) {
                bestBuilds.add(buildDest);
            }
        }
        // Try all bestBuilds until one succeeds
        for (Cell buildDest : bestBuilds) {
            if (turn.executeBuild(buildDest)) {
                buildSuccess = true;
                break;
            }
        }
        // If all bestBuilds fail or none exist, try any random adjacent buildable cell
        if (!buildSuccess) {
            List<Cell> adjacentBuilds = new ArrayList<>();
            for (int dx = -1; dx <= 1; dx++) {
                for (int dy = -1; dy <= 1; dy++) {
                    if (dx == 0 && dy == 0) continue;
                    int nx = buildFrom.getPosition().getX() + dx;
                    int ny = buildFrom.getPosition().getY() + dy;
                    Position pos = new Position(nx, ny);
                    Cell dest = board.getCell(pos);
                    if (dest != null && !dest.isOccupied() && !dest.hasDome() && dest.getHeight() < 4) {
                        adjacentBuilds.add(dest);
                    }
                }
            }
            if (!adjacentBuilds.isEmpty()) {
                Cell randomAdj = adjacentBuilds.get(random.nextInt(adjacentBuilds.size()));
                boolean fallbackSuccess = turn.executeBuild(randomAdj);
                buildSuccess = fallbackSuccess;
            }
        }
        if (!buildSuccess) {
            turn.setState(TurnState.COMPLETED);
            return;
        }

        // Before any build, check again if any worker can move to a level 3 cell (winning move)
        for (Worker worker : player.getWorkers()) {
            if (worker.getCurrentCell() == null) continue;
            Cell currentCell = worker.getCurrentCell();
            for (int dx = -1; dx <= 1; dx++) {
                for (int dy = -1; dy <= 1; dy++) {
                    if (dx == 0 && dy == 0) continue;
                    int nx = currentCell.getPosition().getX() + dx;
                    int ny = currentCell.getPosition().getY() + dy;
                    Position pos = new Position(nx, ny);
                    Cell dest = board.getCell(pos);
                    if (dest != null && !dest.isOccupied() && !dest.hasDome() && dest.getHeight() == 3 && dest.getHeight() <= currentCell.getHeight() + 1) {
                        // Always select this worker if not already selected
                        if (turn.getSelectedWorker() != worker) {
                            if (turn.getState() != TurnState.WORKER_SELECTION) {
                                // Unselect current worker if needed
                                turn.unselectWorker();
                            }
                            if (turn.getState() == TurnState.WORKER_SELECTION) {
                                turn.selectWorker(worker);
                            }
                        }
                        // Move directly to level 3 cell and win
                        boolean moveResult = turn.executeMove(dest);
                        // Set winner for GUI (Game state will be set to GAME_OVER in switchTurn)
                        player.getGame().getTurnManager().checkWinner();
                        turn.setState(TurnState.COMPLETED); // End turn (AI wins)
                        turn.complete(); // Mark turn as fully complete
                        return;
                    }
                }
            }
        }

        // Explicitly complete the turn
        turn.setState(TurnState.COMPLETED);
    }

    // Santorini-specific minimax evaluation
    private int minimax(Board board, int depth, boolean isMaximizing, ComputerPlayer aiPlayer, Player opponent) {
        // 1. Check for immediate win/loss
        int aiMax = getMaxWorkerHeight(aiPlayer);
        int oppMax = getMaxWorkerHeight(opponent);
        if (aiMax == 3) return 1000;
        if (oppMax == 3) return -1000;
        if (depth == 0) return aiMax - oppMax;

        if (isMaximizing) {
            int maxEval = Integer.MIN_VALUE;
            // For each possible move for aiPlayer
            for (Worker worker : aiPlayer.getWorkers()) {
                Cell currentCell = worker.getCurrentCell();
                if (currentCell == null) continue;
                for (int dx = -1; dx <= 1; dx++) {
                    for (int dy = -1; dy <= 1; dy++) {
                        if (dx == 0 && dy == 0) continue;
                        Position pos = new Position(currentCell.getPosition().getX() + dx, currentCell.getPosition().getY() + dy);
                        Cell dest = board.getCell(pos);
                        if (dest != null && !dest.isOccupied() && !dest.hasDome() && dest.getHeight() <= currentCell.getHeight() + 1) {
                            // Simulate move (deep copy needed in real implementation)
                            int eval = minimax(board, depth - 1, false, aiPlayer, opponent);
                            maxEval = Math.max(maxEval, eval);
                        }
                    }
                }
            }
            return maxEval;
        } else {
            int minEval = Integer.MAX_VALUE;
            // For each possible move for opponent
            for (Worker worker : opponent.getWorkers()) {
                Cell currentCell = worker.getCurrentCell();
                if (currentCell == null) continue;
                for (int dx = -1; dx <= 1; dx++) {
                    for (int dy = -1; dy <= 1; dy++) {
                        if (dx == 0 && dy == 0) continue;
                        Position pos = new Position(currentCell.getPosition().getX() + dx, currentCell.getPosition().getY() + dy);
                        Cell dest = board.getCell(pos);
                        if (dest != null && !dest.isOccupied() && !dest.hasDome() && dest.getHeight() <= currentCell.getHeight() + 1) {
                            // Simulate move (deep copy needed in real implementation)
                            int eval = minimax(board, depth - 1, true, aiPlayer, opponent);
                            minEval = Math.min(minEval, eval);
                        }
                    }
                }
            }
            return minEval;
        }
    }

    // Helper to get the max height of a player's workers
    private int getMaxWorkerHeight(Player player) {
        int max = 0;
        for (Worker w : player.getWorkers()) {
            if (w.getCurrentCell() != null) {
                max = Math.max(max, w.getCurrentCell().getHeight());
            }
        }
        return max;
    }

} 