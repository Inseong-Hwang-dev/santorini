import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import model.cards.ArtemisCard;
import model.cards.DemeterCard;
import model.cards.TritonCard;
import model.cards.GodCard;
import model.enums.GameMode;

/**
 * The main menu screen for the Santorini game.
 */
public class MenuUI extends JFrame {

    public MenuUI() {
        setTitle("Santorini - Main Menu");
        setSize(500, 400);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        setLayout(new BorderLayout());

        JLabel title = new JLabel("Santorini", SwingConstants.CENTER);
        title.setFont(new Font("Georgia", Font.BOLD, 32));
        title.setBorder(BorderFactory.createEmptyBorder(30, 0, 10, 0));
        add(title, BorderLayout.NORTH);

        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.Y_AXIS));
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 100, 50, 100));
        buttonPanel.setOpaque(false);

        JButton startBtn = createMenuButton("Start Game");
        startBtn.addActionListener(e -> {
            // Show mode selection dialog
            String[] modeOptions = {"Single Player", "Multiplayer"};
            int modeChoice = JOptionPane.showOptionDialog(
                this,
                "Select Game Mode:",
                "Game Mode Selection",
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,
                modeOptions,
                modeOptions[0]
            );
            if (modeChoice == -1) return;
            GameMode selectedMode = (modeChoice == 0) ? GameMode.SINGLE_PLAYER : GameMode.MULTIPLAYER;

            GodCard[] godCards = {new ArtemisCard(), new DemeterCard(), new TritonCard()};
            String[] godNames = {godCards[0].getName(), godCards[1].getName(), godCards[2].getName()};
            GodCard p1Card = null;
            GodCard p2Card = null;

            // Player 1 selection
            String p1Choice = (String) JOptionPane.showInputDialog(
                this,
                "Player 1: Choose your God Card",
                "God Card Selection",
                JOptionPane.PLAIN_MESSAGE,
                null,
                godNames,
                godNames[0]);
            if (p1Choice == null) return;
            for (GodCard card : godCards) {
                if (card.getName().equals(p1Choice)) {
                    p1Card = card;
                    break;
                }
            }

            if (selectedMode == GameMode.SINGLE_PLAYER) {
                // Computer player GodCard selection (remove already chosen card)
                List<String> p2Options = new ArrayList<>(Arrays.asList(godNames));
                p2Options.remove(p1Choice);
                String p2Choice = (String) JOptionPane.showInputDialog(
                    this,
                    "Computer: Choose your God Card",
                    "God Card Selection",
                    JOptionPane.PLAIN_MESSAGE,
                    null,
                    p2Options.toArray(),
                    p2Options.get(0));
                if (p2Choice == null) return;
                for (GodCard card : godCards) {
                    if (card.getName().equals(p2Choice)) {
                        p2Card = card;
                        break;
                    }
                }
            } else {
                // Player 2 selection (remove already chosen card)
                List<String> p2Options = new ArrayList<>(Arrays.asList(godNames));
                p2Options.remove(p1Choice);
                String p2Choice = (String) JOptionPane.showInputDialog(
                    this,
                    "Player 2: Choose your God Card",
                    "God Card Selection",
                    JOptionPane.PLAIN_MESSAGE,
                    null,
                    p2Options.toArray(),
                    p2Options.get(0));
                if (p2Choice == null) return;
                for (GodCard card : godCards) {
                    if (card.getName().equals(p2Choice)) {
                        p2Card = card;
                        break;
                    }
                }
            }

            final GodCard finalP1Card = p1Card;
            final GodCard finalP2Card = p2Card;
            dispose();
            // Pass selected GodCards and mode to GUI
            SwingUtilities.invokeLater(() -> new GUI(finalP1Card, finalP2Card, selectedMode));
        });

        JButton exitBtn = createMenuButton("Exit");
        exitBtn.setBackground(new Color(200, 50, 50));
        exitBtn.setForeground(Color.WHITE);
        exitBtn.setOpaque(true);
        exitBtn.setContentAreaFilled(true);
        exitBtn.setBorderPainted(true);
        exitBtn.setUI(new javax.swing.plaf.basic.BasicButtonUI());
        exitBtn.addActionListener(e -> System.exit(0));

        buttonPanel.add(Box.createVerticalStrut(15));
        buttonPanel.add(startBtn);
        buttonPanel.add(Box.createVerticalStrut(20));
        buttonPanel.add(exitBtn);

        add(buttonPanel, BorderLayout.CENTER);
        setVisible(true);
    }

    private JButton createMenuButton(String text) {
        JButton button = new JButton(text);
        button.setFont(new Font("Georgia", Font.PLAIN, 16));
        button.setFocusPainted(false);
        button.setAlignmentX(Component.CENTER_ALIGNMENT);
        button.setPreferredSize(new Dimension(200, 40));
        button.setOpaque(true);
        button.setContentAreaFilled(true);
        button.setBorderPainted(true);
        return button;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(MenuUI::new);
    }
}