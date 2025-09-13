package model.ai;

import model.Board;
import model.Cell;
import model.players.ComputerPlayer;
import model.Worker;
import model.turns.Turn;
import model.Position;
import model.enums.TurnState;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * Random strategy for early turns.
 */
public class RandomStrategy implements AIStrategy {
    private final Random random = new Random();

    /**
     * Make a random move for the computer player.
     * @param player The computer player
     * @param turn The current turn
     * @param board The game board
     */
    public void calculateMove(ComputerPlayer player, Turn turn, Board board) {
        Worker selectedWorker;

        // 1. Find all workers that can move
        Worker[] workers = player.getWorkers();
        List<Worker> movableWorkers = new ArrayList<>();
        for (Worker worker : workers) {
            // Check if this worker has at least one valid move
            Cell currentCell = worker.getCurrentCell();
            if (currentCell == null) continue;
            boolean canMove = false;
            for (int dx = -1; dx <= 1; dx++) {
                for (int dy = -1; dy <= 1; dy++) {
                    if (dx == 0 && dy == 0) continue;
                    int nx = currentCell.getPosition().getX() + dx;
                    int ny = currentCell.getPosition().getY() + dy;
                    Position pos = new Position(nx, ny);
                    Cell dest = board.getCell(pos);
                    if (dest != null && !dest.isOccupied() && !dest.hasDome() && dest.getHeight() <= currentCell.getHeight() + 1) {
                        canMove = true;
                        break;
                    }
                }
                if (canMove) break;
            }
            if (canMove) movableWorkers.add(worker);
        }
        if (movableWorkers.isEmpty()) {
            turn.setState(TurnState.COMPLETED);
            return;
        }
        // Only select a worker if the state is WORKER_SELECTION
        if (turn.getState() == TurnState.WORKER_SELECTION) {
            selectedWorker = movableWorkers.get(random.nextInt(movableWorkers.size()));
            turn.selectWorker(selectedWorker);
        } else {
            selectedWorker = turn.getSelectedWorker();
            if (selectedWorker == null) {
                turn.setState(TurnState.COMPLETED);
                return;
            }
        }

        // 2. Check all available cells to move
        List<Cell> validMoves = new ArrayList<>();
        Cell currentCell = selectedWorker.getCurrentCell();
        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = -1; dy <= 1; dy++) {
                if (dx == 0 && dy == 0) continue;
                int nx = currentCell.getPosition().getX() + dx;
                int ny = currentCell.getPosition().getY() + dy;
                Position pos = new Position(nx, ny);
                Cell dest = board.getCell(pos);
                if (dest != null && !dest.isOccupied() && !dest.hasDome() && dest.getHeight() <= currentCell.getHeight() + 1) {
                    validMoves.add(dest);
                }
            }
        }
        if (validMoves.isEmpty()) {
            turn.setState(TurnState.COMPLETED);
            return;
        }
        // 3. Choose one and move
        Cell moveDest = validMoves.get(random.nextInt(validMoves.size()));
        boolean moveSuccess = turn.executeMove(moveDest);

        // 3.5. If the GodCard allows SECOND_MOVE (Artemis, Triton), randomly decide to use it or skip after the first move
        if (moveSuccess && (turn.getState() == TurnState.SECOND_MOVE)) {
            boolean usePower = random.nextBoolean();
            if (!usePower) {
                // Skip GodCard action, go to build phase
                turn.setState(TurnState.BUILDING);
            } else {
                // Try to perform the second move immediately
                List<Cell> secondMoveOptions = new ArrayList<>();
                Cell secondMoveFrom = selectedWorker.getCurrentCell();
                for (int dx = -1; dx <= 1; dx++) {
                    for (int dy = -1; dy <= 1; dy++) {
                        if (dx == 0 && dy == 0) continue;
                        int nx = secondMoveFrom.getPosition().getX() + dx;
                        int ny = secondMoveFrom.getPosition().getY() + dy;
                        Position pos = new Position(nx, ny);
                        Cell dest = board.getCell(pos);
                        if (dest != null && !dest.isOccupied() && !dest.hasDome() && dest.getHeight() <= secondMoveFrom.getHeight() + 1) {
                            // ArtemisCard: cannot move back to initial position
                            if (player.getGodCard() instanceof model.cards.ArtemisCard && selectedWorker.getInitialPosition() != null && selectedWorker.getInitialPosition().equals(pos)) {
                                continue;
                            }
                            secondMoveOptions.add(dest);
                        }
                    }
                }
                if (!secondMoveOptions.isEmpty()) {
                    Cell secondMoveDest = secondMoveOptions.get(random.nextInt(secondMoveOptions.size()));
                    boolean secondMoveSuccess = turn.executeMove(secondMoveDest);
                } else {
                    // After second move, state should be BUILDING
                    turn.setState(TurnState.BUILDING);
                }
            }
        }

        // 4. Only if move succeeded and state is BUILDING, check all available cells to build
        if (!moveSuccess || turn.getState() != TurnState.BUILDING) {
            return;
        }

        // 5. Check all available cells to build
        List<Cell> buildOptions = new ArrayList<>();
        Cell buildFrom = selectedWorker.getCurrentCell();
        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = -1; dy <= 1; dy++) {
                if (dx == 0 && dy == 0) continue;
                int nx = buildFrom.getPosition().getX() + dx;
                int ny = buildFrom.getPosition().getY() + dy;
                Position pos = new Position(nx, ny);
                Cell dest = board.getCell(pos);
                if (dest != null && !dest.isOccupied() && !dest.hasDome()) {
                    buildOptions.add(dest);
                }
            }
        }
        if (buildOptions.isEmpty()) {
            turn.setState(TurnState.COMPLETED);
            return;
        }
        // 6. Choose one and build
        List<Cell> shuffled = new ArrayList<>(buildOptions);
        Collections.shuffle(shuffled, random);
        boolean buildSuccess = false;
        for (Cell buildDest : shuffled) {
            if (turn.executeBuild(buildDest)) {
                buildSuccess = true;
                break;
            } else {
            }
        }

        // 6.5. If the GodCard is Demeter and state is SECOND_BUILD, randomly decide to use it or skip
        if (buildSuccess && turn.getState() == TurnState.SECOND_BUILD && player.getGodCard() instanceof model.cards.DemeterCard) {
            boolean useDemeter = random.nextBoolean();
            if (!useDemeter) {
                // Skip Demeter power, end turn
                turn.setState(TurnState.COMPLETED);
                return;
            } else {
                // Try to perform the second build immediately
                List<Cell> secondBuildOptions = new ArrayList<>();
                Cell secondBuildFrom = selectedWorker.getCurrentCell();
                Cell firstBuildTarget = turn.getLastBuild().getDestination();
                for (int dx = -1; dx <= 1; dx++) {
                    for (int dy = -1; dy <= 1; dy++) {
                        if (dx == 0 && dy == 0) continue;
                        int nx = secondBuildFrom.getPosition().getX() + dx;
                        int ny = secondBuildFrom.getPosition().getY() + dy;
                        Position pos = new Position(nx, ny);
                        Cell dest = board.getCell(pos);
                        // Demeter cannot build on the same space twice
                        if (dest != null && !dest.isOccupied() && !dest.hasDome() && !dest.getPosition().equals(firstBuildTarget.getPosition())) {
                            secondBuildOptions.add(dest);
                        }
                    }
                }
                if (!secondBuildOptions.isEmpty()) {
                    Cell secondBuildDest = secondBuildOptions.get(random.nextInt(secondBuildOptions.size()));
                    boolean secondBuildSuccess = turn.executeBuild(secondBuildDest);
                    System.out.println("Second build success: " + secondBuildSuccess);
                }
                // End turn after second build
                turn.setState(TurnState.COMPLETED);
                return;
            }
        }
        System.out.println("[AI] Turn completed.");
        turn.setState(TurnState.COMPLETED);
    }
} 