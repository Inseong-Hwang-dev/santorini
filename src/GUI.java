import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.util.List;
import model.*;
import model.cards.*;
import model.enums.GameState;
import model.enums.TurnState;
import model.enums.GameMode;
import model.Board;
import model.Cell;
import model.Worker;
import model.players.ComputerPlayer;
import model.players.Player;

/**
 * The main GUI for the Santorini game.
 */
public class GUI extends JFrame {

    private final Game game;
    private final JPanel boardPanel;
    private final JLabel statusLabel;
    private Worker selectedWorker;
    private Position selectedPosition;
    private boolean isMovePhase = true;
    private final Random random = new Random();
    private final List<Position> validMovePositions;
    private final JButton skipButton;
    private final boolean useGodCards;
    private final JLabel timerLabel1;
    private final JLabel timerLabel2;
    private javax.swing.Timer uiTimer; // Swing timer for UI updates

    /**
     * GUI constructor for custom GodCard assignment.
     * @param p1Card Player 1's GodCard
     * @param p2Card Player 2's GodCard
     */
    public GUI(GodCard p1Card, GodCard p2Card) {
        this.useGodCards = true;

        setTitle("Santorini");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(600, 680);
        setLayout(new BorderLayout());

        // Prompt for game mode selection
        GameMode mode = promptGameMode();
        game = new Game(mode);
        game.initializeGame("Player1", "Player2");
        // Assign selected GodCards to each player
        game.assignGodCard(game.getPlayer1(), p1Card);
        game.assignGodCard(game.getPlayer2(), p2Card);

        validMovePositions = new ArrayList<>();
        placeWorkersRandomly();

        boardPanel = new JPanel(new GridLayout(5, 5));
        boardPanel.setPreferredSize(new Dimension(600, 600));
        initializeBoard();

        JPanel statusPanel = new JPanel(new BorderLayout());
        statusPanel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));

        statusLabel = new JLabel();
        statusLabel.setFont(new Font("Arial", Font.BOLD, 14));
        updateStatusWithGodInfo("Player1's turn - Select a worker");

        skipButton = new JButton("Skip GodCard Action");
        skipButton.setFont(new Font("Arial", Font.BOLD, 12));
        skipButton.addActionListener(e -> handleSkip());
        skipButton.setVisible(false);

        statusPanel.add(statusLabel, BorderLayout.WEST);
        statusPanel.add(skipButton, BorderLayout.EAST);

        timerLabel1 = new JLabel("Player1 Time: 15:00");
        timerLabel2 = new JLabel("Player2 Time: 15:00");
        timerLabel1.setFont(new Font("Arial", Font.BOLD, 14));
        timerLabel2.setFont(new Font("Arial", Font.BOLD, 14));

        JPanel timerPanel = new JPanel(new GridLayout(1, 2));
        timerPanel.add(timerLabel1);
        timerPanel.add(timerLabel2);

        add(timerPanel, BorderLayout.NORTH);

        add(boardPanel, BorderLayout.CENTER);
        add(statusPanel, BorderLayout.SOUTH);

        // UI timer: update every 0.5 seconds
        uiTimer = new javax.swing.Timer(500, e -> updateTimerLabels());
        uiTimer.start();

        setVisible(true);
    }

    /**
     * GUI constructor for custom GodCard assignment and game mode.
     * @param p1Card Player 1's GodCard
     * @param p2Card Player 2's GodCard
     * @param mode Selected game mode
     */
    public GUI(GodCard p1Card, GodCard p2Card, GameMode mode) {
        this.useGodCards = true;

        setTitle("Santorini");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(600, 680);
        setLayout(new BorderLayout());

        // Use the selected game mode
        game = new Game(mode);
        game.initializeGame("Player1", "Player2");
        // Assign selected GodCards to each player
        game.assignGodCard(game.getPlayer1(), p1Card);
        game.assignGodCard(game.getPlayer2(), p2Card);

        validMovePositions = new ArrayList<>();
        placeWorkersRandomly();

        boardPanel = new JPanel(new GridLayout(5, 5));
        boardPanel.setPreferredSize(new Dimension(600, 600));
        initializeBoard();

        JPanel statusPanel = new JPanel(new BorderLayout());
        statusPanel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));

        statusLabel = new JLabel();
        statusLabel.setFont(new Font("Arial", Font.BOLD, 14));
        updateStatusWithGodInfo("Player1's turn - Select a worker");

        skipButton = new JButton("Skip GodCard Action");
        skipButton.setFont(new Font("Arial", Font.BOLD, 12));
        skipButton.addActionListener(e -> handleSkip());
        skipButton.setVisible(false);

        statusPanel.add(statusLabel, BorderLayout.WEST);
        statusPanel.add(skipButton, BorderLayout.EAST);

        timerLabel1 = new JLabel("Player1 Time: 15:00");
        timerLabel2 = new JLabel("Player2 Time: 15:00");
        timerLabel1.setFont(new Font("Arial", Font.BOLD, 14));
        timerLabel2.setFont(new Font("Arial", Font.BOLD, 14));

        JPanel timerPanel = new JPanel(new GridLayout(1, 2));
        timerPanel.add(timerLabel1);
        timerPanel.add(timerLabel2);

        add(timerPanel, BorderLayout.NORTH);

        add(boardPanel, BorderLayout.CENTER);
        add(statusPanel, BorderLayout.SOUTH);

        // UI timer: update every 0.5 seconds
        uiTimer = new javax.swing.Timer(500, e -> updateTimerLabels());
        uiTimer.start();

        setVisible(true);
    }

    private static GameMode promptGameMode() {
        String[] options = {"Single Player", "Multiplayer"};
        int choice = JOptionPane.showOptionDialog(
            null,
            "Select Game Mode:",
            "Game Mode Selection",
            JOptionPane.DEFAULT_OPTION,
            JOptionPane.QUESTION_MESSAGE,
            null,
            options,
            options[0]
        );
        return (choice == 0) ? GameMode.SINGLE_PLAYER : GameMode.MULTIPLAYER;
    }

    private void updateStatusWithGodInfo(String baseStatus) {
        Player currentPlayer = game.getCurrentPlayer();
        String godCardInfo =
                currentPlayer.getGodCard() != null
                        ? " (" + currentPlayer.getGodCard().getName() + ")"
                        : "";
        statusLabel.setText(baseStatus + godCardInfo);
    }

    private void placeWorkersRandomly() {
        Board board = game.getBoard();
        Player player1 = game.getCurrentPlayer();
        Player player2 = game.getPlayer2();

        placeWorkerRandomly(board, player1.getWorkers()[0]);
        placeWorkerRandomly(board, player1.getWorkers()[1]);
        placeWorkerRandomly(board, player2.getWorkers()[0]);
        placeWorkerRandomly(board, player2.getWorkers()[1]);
    }

    private void placeWorkerRandomly(Board board, Worker worker) {
        while (true) {
            int x = random.nextInt(5);
            int y = random.nextInt(5);
            Position pos = new Position(x, y);
            Cell cell = board.getCell(pos);
            if (!cell.isOccupied()) {
                board.placeWorker(worker, pos);
                break;
            }
        }
    }

    private void initializeBoard() {
        boardPanel.removeAll();
        Board board = game.getBoard();

        for (int i = 0; i < 5; i++) {
            for (int j = 0; j < 5; j++) {
                Position pos = new Position(i, j);
                Cell cell = board.getCell(pos);
                JButton button = new JButton();
                updateCellAppearance(button, cell);
                button.addActionListener(e -> handleCellClick(pos));
                boardPanel.add(button);
            }
        }

        boardPanel.revalidate();
        boardPanel.repaint();
    }

    private void updateCellAppearance(JButton button, Cell cell) {
        button.setMargin(new Insets(0, 0, 0, 0));
        UIManager.put("Button.font", new Font("Segoe UI", Font.BOLD, 14));
        button.setForeground(Color.BLACK);
        button.setOpaque(true);
        button.setBorder(BorderFactory.createLineBorder(Color.BLACK));

        int height = cell.getHeight();
        Color[] heightColors = {
                new Color(240, 240, 240),
                new Color(200, 220, 255),
                new Color(150, 190, 255),
                new Color(100, 150, 255)
        };

        if (cell.hasDome()) {
            button.setBackground(Color.DARK_GRAY);
            button.setText("🏛");
            button.setForeground(Color.WHITE);
        } else {
            button.setBackground(heightColors[Math.min(height, 3)]);
            if (cell.isOccupied()) {
                Worker worker = cell.getWorker();
                String label =
                        (worker.getOwner().getName().equals("Player1") ? "A" : "B") + worker.getId();
                button.setText("<html><center>" + label + "<br>(" + height + ")</center></html>");
                button.setForeground(Color.WHITE);
                button.setBackground(
                        worker.getOwner().getName().equals("Player1") ? Color.RED : Color.BLUE);
            } else {
                button.setText(String.valueOf(height));
            }
        }

        if (validMovePositions.contains(cell.getPosition())) {
            button.setBackground(Color.GREEN);
            button.setForeground(Color.BLACK);
        }
    }

    private void calculateValidMovePositions() {
        validMovePositions.clear();
        Position currentPos = selectedWorker.getCurrentCell().getPosition();

        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = -1; dy <= 1; dy++) {
                if (dx == 0 && dy == 0) {
                    continue;
                }
                int nx = currentPos.getX() + dx;
                int ny = currentPos.getY() + dy;

                if (nx >= 0 && nx < 5 && ny >= 0 && ny < 5) {
                    Position newPos = new Position(nx, ny);
                    Cell newCell = game.getBoard().getCell(newPos);

                    // ArtemisCard: cannot move back to initial position on second move
                    if (useGodCards
                            && game.getCurrentPlayer().getGodCard() instanceof ArtemisCard
                            && !game.getCurrentTurn().getMoves().isEmpty()
                            && selectedWorker.getInitialPosition() != null
                            && selectedWorker.getInitialPosition().equals(newPos)) {
                        continue;
                    }

                    // TritonCard: allow any valid move after the first, including to non-perimeter and initial position
                    // No extra restriction needed for TritonCard

                    if (!newCell.isOccupied()
                            && !newCell.hasDome()
                            && newCell.getHeight() <= selectedWorker.getCurrentCell().getHeight() + 1) {
                        validMovePositions.add(newPos);
                    }
                }
            }
        }
    }

    private void calculateValidBuildPositions() {
        validMovePositions.clear();
        Position currentPos = selectedWorker.getCurrentCell().getPosition();

        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = -1; dy <= 1; dy++) {
                if (dx == 0 && dy == 0) {
                    continue;
                }
                int nx = currentPos.getX() + dx;
                int ny = currentPos.getY() + dy;

                if (nx >= 0 && nx < 5 && ny >= 0 && ny < 5) {
                    Position newPos = new Position(nx, ny);
                    Cell newCell = game.getBoard().getCell(newPos);

                    if (useGodCards
                            && game.getCurrentPlayer().getGodCard() instanceof DemeterCard
                            && !game.getCurrentTurn().getBuilds().isEmpty()) {
                        Cell lastBuild = game.getCurrentTurn().getLastBuild().getDestination();
                        if (lastBuild.getPosition().equals(newPos)) {
                            continue;
                        }
                    }

                    if (!newCell.isOccupied() && !newCell.hasDome() && newCell.getHeight() < 4) {
                        validMovePositions.add(newPos);
                    }
                }
            }
        }
    }

    private void handleCellClick(Position pos) {
        // Prevent user actions if it's ComputerPlayer's turn
        if (game.getCurrentPlayer() instanceof ComputerPlayer) {
            // Ignore clicks during computer's turn
            return;
        }
        Board board = game.getBoard();
        Cell cell = board.getCell(pos);

        // Phase 1: Worker Selection
        if (isMovePhase && selectedWorker == null) {
            if (cell.isOccupied() && cell.getWorker().getOwner() == game.getCurrentPlayer()) {
                selectedWorker = cell.getWorker();
                int workerId = Integer.parseInt(selectedWorker.getId()) - 1;
                if (game.selectWorker(String.valueOf(workerId))) {
                    calculateValidMovePositions();
                    updateStatusWithGodInfo(game.getCurrentPlayer().getName() + " - Select move position");
                    initializeBoard();
                } else {
                    selectedWorker = null;
                    updateStatusWithGodInfo("Failed to select worker. Try again.");
                }
            }
        }

        // Phase 2: Movement
        else if (isMovePhase && selectedWorker != null) {
            // For TritonCard, do not allow unselect after action has started
            if (useGodCards && game.getCurrentPlayer().getGodCard() instanceof TritonCard) {
                // If a worker is already selected, do not allow unselect
                if (cell.isOccupied() && cell.getWorker() == selectedWorker) {
                    // Do nothing (unselect disabled for Triton)
                    return;
                }
            }
            if (cell.isOccupied() && cell.getWorker() == selectedWorker) {
                if (useGodCards
                        && game.getCurrentPlayer().getGodCard() instanceof ArtemisCard
                        && !game.getCurrentTurn().getMoves().isEmpty()) {
                    return;
                }

                if (game.getTurnManager().unselectWorker()) {
                    selectedWorker = null;
                    validMovePositions.clear();
                    updateStatusWithGodInfo(
                            game.getCurrentPlayer().getName() + "'s turn - Select a worker");
                    initializeBoard();
                }
            } else if (validMovePositions.contains(pos)) {
                if (game.getCurrentTurn().executeMove(cell)) {
                    if (game.getCurrentTurn().isComplete()) {
                        if (game.switchTurn()) {
                            if (game.getState() == GameState.GAME_OVER) {
                                Player winner = game.getTurnManager().getWinner();
                                JOptionPane.showMessageDialog(
                                        this,
                                        "🏆 Game Over!\nWinner: " + winner.getName()
                                                + "\nWinning condition: Reached level 3");
                                showMainMenu();
                            }
                        }
                        return;
                    }

                    // ArtemisCard: allow second move
                    if (game.canTakeAdditionalAction()
                            && game.getCurrentPlayer().getGodCard() instanceof ArtemisCard) {
                        statusLabel.setText(
                                game.getCurrentPlayer().getName() + " - Can move again (Artemis power) or skip");
                        skipButton.setVisible(true);
                        calculateValidMovePositions();
                        initializeBoard();
                        return;
                    }

                    // TritonCard: allow second move if on perimeter
                    if (game.getCurrentPlayer().getGodCard() instanceof TritonCard
                            && game.getCurrentTurn().getState() == TurnState.SECOND_MOVE) {
                        statusLabel.setText(
                                game.getCurrentPlayer().getName() + " - Can move again (Triton power) or skip");
                        skipButton.setVisible(true);
                        calculateValidMovePositions();
                        initializeBoard();
                        return;
                    }

                    // Otherwise, go to build phase
                    calculateValidBuildPositions();
                    updateStatusWithGodInfo(game.getCurrentPlayer().getName() + " - Select build position");
                    selectedPosition = pos;
                    isMovePhase = false;
                    skipButton.setVisible(false);
                    initializeBoard();
                }
            }
        }

        // Phase 3: Building
        else if (selectedWorker != null) {
            if (validMovePositions.contains(pos)) {
                if (game.getCurrentTurn().executeBuild(cell)) {
                    if (game.canTakeAdditionalAction()
                            && game.getCurrentPlayer().getGodCard() instanceof DemeterCard) {
                        statusLabel.setText(
                                game.getCurrentPlayer().getName() + " - Can build again (Demeter power) or skip");
                        skipButton.setVisible(true);
                        calculateValidBuildPositions();
                        initializeBoard();
                        return;
                    }

                    validMovePositions.clear();
                    skipButton.setVisible(false);
                    if (game.switchTurn()) {
                        if (game.getState() == GameState.GAME_OVER) {
                            Player winner = game.getTurnManager().getWinner();
                            JOptionPane.showMessageDialog(
                                    this,
                                    "🏆 Game Over!\nWinner: " + winner.getName()
                                            + "\nWinning condition: Reached level 3 or opponent stuck");
                            showMainMenu();
                        } else {
                            selectedWorker = null;
                            selectedPosition = null;
                            isMovePhase = true;
                            skipButton.setVisible(false);
                            updateStatusWithGodInfo(
                                    game.getCurrentPlayer().getName() + "'s turn - Select a worker");
                            initializeBoard();
                        }
                    }
                }
            }
        }

        // Call handleComputerTurnIfNeeded after each turn switch and after user actions
        handleComputerTurnIfNeeded();
    }

    private void handleSkip() {
        skipButton.setVisible(false);
        if (useGodCards
                && game.getCurrentPlayer().getGodCard() instanceof ArtemisCard
                && isMovePhase
                && !game.getCurrentTurn().getMoves().isEmpty()) {
            isMovePhase = false;
            game.getCurrentTurn().setState(TurnState.BUILDING);
            calculateValidBuildPositions();
            updateStatusWithGodInfo(game.getCurrentPlayer().getName() + " - Select build position");
            initializeBoard();
        } else if (useGodCards
                && game.getCurrentPlayer().getGodCard() instanceof TritonCard
                && isMovePhase
                && !game.getCurrentTurn().getMoves().isEmpty()) {
            isMovePhase = false;
            game.getCurrentTurn().setState(TurnState.BUILDING);
            calculateValidBuildPositions();
            updateStatusWithGodInfo(game.getCurrentPlayer().getName() + " - Select build position");
            initializeBoard();
        } else if (useGodCards
                && game.getCurrentPlayer().getGodCard() instanceof DemeterCard
                && !isMovePhase
                && !game.getCurrentTurn().getBuilds().isEmpty()) {
            if (game.skipSecondAction()) {
                selectedWorker = null;
                selectedPosition = null;
                isMovePhase = true;
                validMovePositions.clear();
                if (game.getState() == GameState.GAME_OVER) {
                    handleGameOver();
                } else {
                    updateStatusWithGodInfo(game.getCurrentPlayer().getName() + "'s turn - Select a worker");
                    initializeBoard();
                }
            }
        }

        // Call handleComputerTurnIfNeeded after each turn switch and after user actions
        handleComputerTurnIfNeeded();
    }

    private void handleGameOver() {
        Player winner = game.getTurnManager().getWinner();
        String message =
                "Game Over!\nWinner: "
                        + winner.getName()
                        + "\nWinning condition: "
                        + (selectedWorker.getCurrentCell().getHeight() == 3
                        ? "Reached level 3"
                        : "Opponent has no valid moves");

        String[] options = {"Play Again", "Main Menu", "Exit"};
        int choice =
                JOptionPane.showOptionDialog(
                        this,
                        message,
                        "Victory!",
                        JOptionPane.DEFAULT_OPTION,
                        JOptionPane.INFORMATION_MESSAGE,
                        null,
                        options,
                        options[0]);

        dispose();

        if (choice == 0) {
            new GUI(null, null);
        } else if (choice == 1) {
            MenuUI.main(null);
        } else {
            System.exit(0);
        }
    }

    /** Returns to the main menu screen. */
    private void showMainMenu() {
        SwingUtilities.invokeLater(
                () -> {
                    dispose();
                    new MenuUI();
                });
    }

    /**
     * Update the timer labels for both players and check for time over.
     */
    private void updateTimerLabels() {
        long millis1 = game.getPlayer1Timer().getRemainingTime();
        long millis2 = game.getPlayer2Timer().getRemainingTime();
        timerLabel1.setText("Player1 Time: " + formatMillis(millis1));
        timerLabel2.setText("Player2 Time: " + formatMillis(millis2));

        // Check for time over and handle elimination
        game.checkTimeAndEliminate();
        if (game.getPlayer1().isEliminated() || game.getPlayer2().isEliminated()) {
            uiTimer.stop();
            String loser, winner;
            if (game.getPlayer1().isEliminated()) {
                loser = game.getPlayer1().getName();
                winner = game.getPlayer2().getName();
            } else {
                loser = game.getPlayer2().getName();
                winner = game.getPlayer1().getName();
            }
            JOptionPane.showMessageDialog(
                this,
                loser + " ran out of time!\nWinner: " + winner + "\nGame Over."
            );
            showMainMenu();
        }
    }

    /**
     * Format milliseconds to mm:ss string.
     */
    private String formatMillis(long millis) {
        if (millis < 0) millis = 0;
        long seconds = millis / 1000;
        long min = seconds / 60;
        long sec = seconds % 60;
        return String.format("%02d:%02d", min, sec);
    }

    private void handleComputerTurnIfNeeded() {
        if (game.getState() == GameState.GAME_OVER) {
            // Show winning message if game is over after AI move
            Player winner = game.getTurnManager().getWinner();
            JOptionPane.showMessageDialog(
                this,
                "🏆 Game Over!\nWinner: " + winner.getName()
                        + "\nWinning condition: Reached level 3 or opponent stuck");
            showMainMenu();
            return;
        }
        if (game.getCurrentPlayer() instanceof ComputerPlayer) {
            ComputerPlayer ai = (ComputerPlayer) game.getCurrentPlayer();
            // Only proceed if we're in WORKER_SELECTION state and the turn is not complete
            if (game.getCurrentTurn().getState() != TurnState.WORKER_SELECTION || 
                game.getCurrentTurn().isComplete()) {
                return;
            }
            // Execute AI turn exactly once
            ai.makeMove(game.getBoard());
            // If the turn is now complete, handle turn switching
            if (game.getCurrentTurn().isComplete()) {
                // Increment moveCount before switching turn
                ai.incrementMoveCount();
                // Switch turn and handle next player
                if (game.switchTurn()) {
                    if (game.getState() == GameState.GAME_OVER) {
                        // Show winning message if game is over after AI move
                        Player winner = game.getTurnManager().getWinner();
                        JOptionPane.showMessageDialog(
                            this,
                            "🏆 Game Over!\nWinner: " + winner.getName()
                                    + "\nWinning condition: Reached level 3 or opponent stuck");
                        showMainMenu();
                    } else if (game.getCurrentPlayer() instanceof ComputerPlayer) {
                        // Schedule next AI turn
                        SwingUtilities.invokeLater(this::handleComputerTurnIfNeeded);
                    } else {
                        updateStatusWithGodInfo(game.getCurrentPlayer().getName() + "'s turn - Select a worker");
                        initializeBoard();
                    }
                }
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new GUI(null, null));
    }
}