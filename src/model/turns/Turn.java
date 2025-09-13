package model.turns;

import model.*;
import model.actions.MoveAction;
import model.actions.BuildAction;
import model.cards.ArtemisCard;
import model.cards.DemeterCard;
import model.cards.TritonCard;
import model.enums.TurnState;
import model.players.Player;

import java.util.ArrayList;
import java.util.List;

public class Turn {
    private final Player player;
    private final Board board;
    private Worker selectedWorker;
    private final List<MoveAction> moves;
    private final List<BuildAction> builds;
    private TurnState state;

    private Cell firstMoveOrigin;
    private Cell firstBuildTarget;

    public Turn(Player player, Board board) {
        this.player = player;
        this.board = board;
        this.moves = new ArrayList<>();
        this.builds = new ArrayList<>();
        this.state = TurnState.WORKER_SELECTION;
    }

    public Player getPlayer() {
        return player;
    }

    public Worker getSelectedWorker() {
        return selectedWorker;
    }

    public void selectWorker(Worker worker) {
        if (state != TurnState.WORKER_SELECTION) {
            throw new IllegalStateException("Cannot select worker in current state: " + state);
        }
        this.selectedWorker = worker;
        this.state = TurnState.MOVING;
        this.firstMoveOrigin = worker.getCurrentCell();
    }

    public void unselectWorker() {
        if (selectedWorker != null) {
            this.selectedWorker = null;
            this.state = TurnState.WORKER_SELECTION;
            this.firstMoveOrigin = null;
            this.moves.clear();
            this.builds.clear();
        }
    }

    public List<MoveAction> getMoves() {
        return moves;
    }

    public List<BuildAction> getBuilds() {
        return builds;
    }

    public MoveAction getLastMove() {
        return moves.isEmpty() ? null : moves.get(moves.size() - 1);
    }

    public BuildAction getLastBuild() {
        return builds.isEmpty() ? null : builds.get(builds.size() - 1);
    }

    public TurnState getState() {
        return state;
    }

    public void setState(TurnState newState) {
        this.state = newState;
    }

    public boolean isComplete() {
        return state == TurnState.COMPLETED;
    }

    public boolean validateMove(Cell destination) {
        if (selectedWorker == null) return false;
        if (state != TurnState.MOVING && state != TurnState.SECOND_MOVE) return false;

        // Only ArtemisCard: can't go back to where she started
        if (state == TurnState.SECOND_MOVE
            && player.getGodCard() instanceof ArtemisCard
            && firstMoveOrigin.equals(destination)) {
            return false;
        }

        MoveAction moveAction = new MoveAction(selectedWorker, destination);
        return moveAction.validate(); // This ensures adjacent + up <= 1
    }

    public boolean executeMove(Cell destination) {
        if (!validateMove(destination)) return false;

        MoveAction moveAction = new MoveAction(selectedWorker, destination);

        // Special handling for TritonCard: allow any move, but only allow extra move if on perimeter
        if (player.getGodCard() instanceof TritonCard tritonCard) {
            boolean isPerimeter = tritonCard.applyPower(this, moveAction);
            moveAction.execute();
            moves.add(moveAction);

            // 🚨 Instant win if first move hits level 3
            if (selectedWorker.getCurrentCell().getHeight() == 3) {
                state = TurnState.COMPLETED;
                return true;
            }

            if (moves.size() == 1) {
                // After first move, check if perimeter for possible extra move
                if (isPerimeter) {
                    state = TurnState.SECOND_MOVE;
                    return true;
                } else {
                    state = TurnState.BUILDING;
                    return true;
                }
            } else {
                // After first move, allow any move, but only allow another move if destination is perimeter
                if (isPerimeter) {
                    state = TurnState.SECOND_MOVE;
                    return true;
                } else {
                    state = TurnState.BUILDING;
                    return true;
                }
            }
        }

        // For other GodCards, use the standard move rules
        moveAction = applyGodCardMoveRules(moveAction);
        if (moveAction == null) return false;

        moveAction.execute();
        moves.add(moveAction);

        // 🚨 Instant win if first move hits level 3
        if (selectedWorker.getCurrentCell().getHeight() == 3) {
            state = TurnState.COMPLETED;
            return true;
        }

        // MoveAction GodCards second move trigger (Artemis etc)
        if (canTakeAdditionalAction() &&
                moveActionGodCard() &&
                moves.size() == 1) {
            state = TurnState.SECOND_MOVE;
            return true;
        }

        state = TurnState.BUILDING;
        return true;
    }

    public boolean validateBuild(Cell destination) {
        if (selectedWorker == null) {
            return false;
        }
        if (state != TurnState.BUILDING && state != TurnState.SECOND_BUILD) {
            return false;
        }
        if (state == TurnState.SECOND_BUILD &&
                firstBuildTarget != null &&
                firstBuildTarget.equals(destination)) {
            return false; // Demeter can't build on the same space twice
        }
        BuildAction buildAction = new BuildAction(selectedWorker, destination);
        boolean valid = buildAction.validate();
        if (!valid) {
        }
        return valid;
    }

    public boolean executeBuild(Cell destination) {
        if (!validateBuild(destination)) return false;

        BuildAction buildAction = new BuildAction(selectedWorker, destination);
        buildAction = applyGodCardBuildRules(buildAction);

        if (buildAction == null) return false;

        buildAction.execute();
        builds.add(buildAction);

        if (builds.size() == 1) {
            firstBuildTarget = destination;
        }

        if (canTakeAdditionalAction() &&
                buildActionGodCard() &&
                builds.size() == 1) {
            state = TurnState.SECOND_BUILD;
            return true;
        }

        state = TurnState.COMPLETED;
        return true;
    }

    public MoveAction applyGodCardMoveRules(MoveAction moveAction) {
        // Use ArtemisCard's applyPower method for move modification
        if (player.getGodCard() instanceof ArtemisCard artemisCard) {
            return artemisCard.applyPower(this, moveAction);
        }
        // Do NOT handle TritonCard here; handled directly in executeMove
        return moveAction;
    }

    public BuildAction applyGodCardBuildRules(BuildAction buildAction) {
        // Use DemeterCard's applyPower method for build modification
        if (player.getGodCard() instanceof DemeterCard demeterCard) {
            return demeterCard.applyPower(this, buildAction);
        }
        return buildAction;
    }

    public boolean canTakeAdditionalAction() {
        if (player.getGodCard() instanceof TritonCard tritonCard && moves.size() >= 1) {
            MoveAction lastMove = getLastMove();
            return lastMove != null && tritonCard.canMoveAgain(lastMove, board);
        }
        return switch (player.getGodCard()) {
            case null -> false;
            case ArtemisCard artemis when moves.size() == 1 -> true;
            case DemeterCard demeter when builds.size() == 1 -> true;
            default -> false;
        };
    }

    public boolean moveActionGodCard() {
        if (player.getGodCard() instanceof ArtemisCard || player.getGodCard() instanceof TritonCard) {
            return true;
        }
        return false;
    }

    public boolean buildActionGodCard() {
        if (player.getGodCard() instanceof DemeterCard) {
            return true;
        }
        return false;
    }

    public void complete() {
        this.state = TurnState.COMPLETED;
    }

    public Board getBoard() {
        return board;
    }
}
