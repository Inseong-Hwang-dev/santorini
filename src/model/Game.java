package model;

import model.cards.ArtemisCard;
import model.cards.DemeterCard;
import model.cards.GodCard;
import model.enums.GameState;
import model.players.ComputerPlayer;
import model.players.Player;
import model.turns.Turn;
import model.turns.TurnManager;
import model.enums.TurnState;
import model.enums.GameMode;

/**
 * Main class that manages the Santorini game
 */
public class Game {
    private final Board board;
    private Player player1;
    private Player player2;
    private GameState state;
    private Player currentPlayer;
    private Turn currentTurn;
    private final TurnManager turnManager;
    // Timer fields for each player
    private Timer player1Timer;
    private Timer player2Timer;
    private static final long INITIAL_TIME_MILLIS = 10 * 60 * 1000; // 10 minutes
    private GameMode gameMode; // Game mode (single or multi player)

    public Game(GameMode mode) {
        this.gameMode = mode;
        this.board = new Board();
        this.state = GameState.SETUP;
        this.turnManager = new TurnManager(this);
        this.player1Timer = new Timer(INITIAL_TIME_MILLIS);
        this.player2Timer = new Timer(INITIAL_TIME_MILLIS);
    }

    public void initializeGame(String player1Name, String player2Name) {
        // Create players
        this.player1 = new Player(player1Name);
        if (gameMode == GameMode.SINGLE_PLAYER) {
            this.player2 = new ComputerPlayer(player2Name, this);
        } else {
            this.player2 = new Player(player2Name);
        }
        
        // Set initial current player
        this.currentPlayer = player1;
        
        // Set game state to PLAYING after initialization
        this.state = GameState.PLAYING;
        
        // Initialize first turn
        this.currentTurn = turnManager.createTurn(currentPlayer);
        player1Timer.reset(INITIAL_TIME_MILLIS);
        player2Timer.reset(INITIAL_TIME_MILLIS);
        player1Timer.start(); // Player 1 starts with their timer running
        player2Timer.pause();
    }

    /**
     * Switches turn to the next player
     * @return true if turn was successfully switched
     */
    public boolean switchTurn() {
        // 1. Verify current turn is complete
        Turn turn = turnManager.getCurrentTurn();
        if (!turn.isComplete()) {
            return false;
        }

        // 2. Check for win/lose conditions
        if (turnManager.checkWinner()) {
            state = GameState.GAME_OVER;
            return true;
        }

        // 3. Update current player
        updateCurrentPlayer();

        // 4. Create new turn for current player and ensure it's properly initialized
        currentTurn = turnManager.createTurn(currentPlayer);
        currentTurn.setState(TurnState.WORKER_SELECTION);
        if (currentTurn.getSelectedWorker() != null) {
            currentTurn.unselectWorker(); // Reset any remaining state from previous turn
        }

        // 5. Apply god card powers
        if (currentPlayer.getGodCard() != null) {
            // Reset any god card specific state
            if (currentPlayer.getGodCard() instanceof DemeterCard) {
                ((DemeterCard) currentPlayer.getGodCard()).resetTurn();
            }
//            currentPlayer.getGodCard().applyPower(currentTurn);
        }

        // Timer switching logic (must be after currentPlayer is updated)
        if (currentPlayer == player1) {
            player2Timer.pause();
            player1Timer.start();
        } else {
            player1Timer.pause();
            player2Timer.start();
        }

        return true;
    }

    private void updateCurrentPlayer() {
        currentPlayer = (currentPlayer == player1) ? player2 : player1;
    }

    public void assignGodCard(Player player, GodCard card) {
        player.setGodCard(card);
    }

    /**
     * Selects a worker for the current player's turn
     * @param workerId the ID of the worker to select (0 or 1)
     * @return true if worker was successfully selected
     */
    public boolean selectWorker(String workerId) {
        if (currentTurn != null && state == GameState.PLAYING) {
            return turnManager.selectWorker(currentPlayer, workerId);
        }
        return false;
    }

    /**
     * Assigns God Cards to players according to the game requirements.
     */
    public void assignGodCards() {
        // For Sprint 2, assign Artemis to Player 1 and Demeter to Player 2
        player1.setGodCard(new ArtemisCard());
        player2.setGodCard(new DemeterCard());
    }

    /**
     * Checks if the current player can take additional actions due to God Card powers.
     *
     * @return true if additional actions are available
     */
    public boolean canTakeAdditionalAction() {
        return currentTurn != null && currentTurn.canTakeAdditionalAction();
    }

    public boolean skipSecondAction() {
        Turn currentTurn = getTurnManager().getCurrentTurn();
        if (currentTurn != null) {
            GodCard godCard = currentPlayer.getGodCard();
    
            if (godCard instanceof ArtemisCard) {
                System.out.println("Skipping Artemis second move -> go to build phase");
                currentTurn.setState(TurnState.BUILDING);
                return true;
            }
    
            if (godCard instanceof DemeterCard) {
                System.out.println("Skipping Demeter second build -> end turn");
                // Ensure turn is properly completed
                currentTurn.setState(TurnState.COMPLETED);
                currentTurn.complete();
                
                // Reset any god card specific state
                if (godCard instanceof DemeterCard) {
                    ((DemeterCard) godCard).resetTurn();
                }
                
                // Switch to next player's turn
                boolean switched = switchTurn();
                System.out.println("Turn switch result: " + switched);
                
                if (switched) {
                    // Ensure new turn is properly initialized
                    System.out.println("New turn state: " + getCurrentTurn().getState());
                }
                
                return switched;
            }
        }
        return false;
    }

    /**
     * Check if any player's timer has run out and eliminate them if so.
     */
    public void checkTimeAndEliminate() {
        if (player1Timer.isPlayerTimerExpired()) {
            player1.setEliminated(true);
            state = GameState.GAME_OVER;
        }
        if (player2Timer.isPlayerTimerExpired()) {
            player2.setEliminated(true);
            state = GameState.GAME_OVER;
        }
    }

    // Getters and setters
    public Board getBoard() {
        return board;
    }

    public Player getCurrentPlayer() {
        return currentPlayer;
    }

    public GameState getState() {
        return state;
    }

    public Turn getCurrentTurn() {
        return currentTurn;
    }

    public TurnManager getTurnManager() {
        return turnManager;
    }

    public Player getPlayer1() {
        return player1;
    }

    public Player getPlayer2() {
        return player2;
    }

    // Timer getters for UI or other logic
    public Timer getPlayer1Timer() {
        return player1Timer;
    }

    public Timer getPlayer2Timer() {
        return player2Timer;
    }
} 