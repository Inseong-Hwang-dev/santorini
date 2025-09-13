package model.turns;

import model.*;
import model.enums.TurnState;
import model.players.Player;

public class TurnManager {
    private Turn currentTurn;
    private final Game game;
    private Player winner;

    public TurnManager(Game game) {
        this.game = game;
    }

    public Turn createTurn(Player player) {
        this.currentTurn = new Turn(player, game.getBoard());
        return currentTurn;
    }

    public Turn getCurrentTurn() {
        return currentTurn;
    }

    public boolean selectWorker(Player player, String workerId) {
        if (currentTurn != null && currentTurn.getState() == TurnState.WORKER_SELECTION) {
            Worker worker = player.getWorkers()[Integer.parseInt(workerId)];
            if (worker != null) {
                currentTurn.selectWorker(worker);
                return true;
            }
        }
        return false;
    }

    /**
     * Unselects the currently selected worker for the current turn.
     * @return true if worker was successfully unselected
     */
    public boolean unselectWorker() {
        if (currentTurn != null && currentTurn.getSelectedWorker() != null) {
            currentTurn.unselectWorker();
            return true;
        }
        return false;
    }

    public boolean checkWinner() {
        if (currentTurn == null) return false;

        Worker worker = currentTurn.getSelectedWorker();
        if (worker == null) return false;

        Cell currentCell = worker.getCurrentCell();
        if (currentCell.getHeight() == 3) {
            winner = worker.getOwner();
            return true;
        }

        Player opponent = (worker.getOwner() == game.getPlayer1())
                ? game.getPlayer2() : game.getPlayer1();

        return !hasValidMoves(opponent);
    }

    private boolean hasValidMoves(Player player) {
        for (Worker worker : player.getWorkers()) {
            Cell currentCell = worker.getCurrentCell();
            for (int dx = -1; dx <= 1; dx++) {
                for (int dy = -1; dy <= 1; dy++) {
                    if (dx == 0 && dy == 0) continue;

                    Position newPos = new Position(
                            currentCell.getPosition().getX() + dx,
                            currentCell.getPosition().getY() + dy
                    );

                    if (newPos.getX() < 0 || newPos.getX() >= 5 ||
                            newPos.getY() < 0 || newPos.getY() >= 5) {
                        continue;
                    }

                    Cell targetCell = game.getBoard().getCell(newPos);
                    if (!targetCell.isOccupied() &&
                            !targetCell.hasDome() &&
                            targetCell.getHeight() <= currentCell.getHeight() + 1) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public Player getWinner() {
        return winner;
    }
}
