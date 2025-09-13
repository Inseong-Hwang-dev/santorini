package model.players;

import model.Worker;
import model.cards.GodCard;

/**
 * Represents a player in the game.
 */
public class Player {

    private final String name;
    private final Worker[] workers;
    private GodCard godCard;
    private boolean isEliminated = false;

    public Player(String name) {
        this.name = name;
        this.workers = new Worker[2];
        initializeWorkers();
    }

    private void initializeWorkers() {
        workers[0] = new Worker(this, "1");
        workers[1] = new Worker(this, "2");
    }

    public String getName() {
        return name;
    }

    public Worker[] getWorkers() {
        return workers;
    }

    public Worker getWorker(int index) {
        if (index >= 0 && index < workers.length) {
            return workers[index];
        }
        return null;
    }

    public GodCard getGodCard() {
        return godCard;
    }

    public void setGodCard(GodCard godCard) {
        this.godCard = godCard;
    }

    /**
     * Check if the player is eliminated.
     * @return true if eliminated, false otherwise
     */
    public boolean isEliminated() {
        return isEliminated;
    }

    /**
     * Set the eliminated status of the player.
     * @param eliminated true if the player is eliminated
     */
    public void setEliminated(boolean eliminated) {
        this.isEliminated = eliminated;
    }
}