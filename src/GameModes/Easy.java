package GameModes;

import javax.swing.*;

public class Easy {
    private int playerCount;
    private String difficulty; // جديد

    public Easy(int playerCount, String difficulty) {
        this.playerCount = playerCount;
        this.difficulty = difficulty;
    }

    public JPanel getGamePanel() {
        JPanel panel = new JPanel();
        panel.add(new JLabel(difficulty + " Mode | Players: " + playerCount));
        return panel;
    }
}
