package model.enums;

/**
 * Defines the finite state machine stages of a player's turn
 */
public enum TurnState {
    WORKER_SELECTION,  // Select a worker
    MOVING,            // First move
    SECOND_MOVE,       // Optional Artemis move
    BUILDING,          // First build
    SECOND_BUILD,      // Optional Demeter build
    COMPLETED          // Turn ends
}
