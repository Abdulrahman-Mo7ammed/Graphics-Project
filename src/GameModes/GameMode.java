package GameModes;

public class GameMode {
    private int playerCount;
    private String difficulty; // "EASY", "MEDIUM", "HARD"

    public GameMode(int playerCount, String difficulty) {
        this.playerCount = playerCount;
        this.difficulty = difficulty;
    }

    public int getPlayerCount() {
        return playerCount;
    }

    public String getDifficulty() {
        return difficulty;
    }
}
