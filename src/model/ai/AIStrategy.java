package model.ai;

import model.Board;
import model.players.ComputerPlayer;
import model.turns.Turn;

/**
 * Interface for AI strategies.
 */
public interface AIStrategy {
    /**
     * Calculate a move for the computer player.
     * @param player The computer player
     * @param turn The current turn
     * @param board The game board
     */
    void calculateMove(ComputerPlayer player, Turn turn, Board board);
} 