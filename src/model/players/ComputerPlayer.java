package model.players;

import model.Board;
import model.Game;
import model.ai.AIStrategy;
import model.ai.RandomStrategy;
import model.ai.MinimaxStrategy;

/**
 * Computer-controlled player for single player mode.
 */
public class ComputerPlayer extends Player
{
    private AIStrategy currentStrategy;
    private int moveCount;
    private Game game; // Reference to the game instance

    /**
     * Constructor for ComputerPlayer.
     * @param name The name of the computer player
     * @param game The game instance
     */
    public ComputerPlayer(String name, Game game) {
        super(name);
        this.moveCount = 0;
        this.currentStrategy = new RandomStrategy();
        this.game = game;
    }

    /**
     * Get the game instance.
     * @return the Game object
     */
    public Game getGame() {
        return game;
    }

    public int getMoveCount()
    {
        return moveCount;
    }

    /**
     * Make a move using the appropriate AI strategy based on the move count.
     * @param board The game board
     */
    public void makeMove(Board board) {
        // Select strategy based on moveCount
        if (moveCount < 3) {
            currentStrategy = new RandomStrategy();
        } else {
            currentStrategy = new MinimaxStrategy(2);
        }
        // Use the new calculateMove signature
        currentStrategy.calculateMove(this, game.getCurrentTurn(), board);
    }

    /**
     * Increment the move count after a full AI turn.
     */
    public void incrementMoveCount() {
        moveCount++;
    }
} 