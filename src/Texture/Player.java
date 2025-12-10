package Texture;

public class Player {
    private final int id;
    private String name;
    private int score;
    private String avatarImage;

    public Player(int id, String name, String avatarImage) {
        this.id = id;
        this.name = name;
        this.avatarImage = avatarImage;
        this.score = 0;
    }

    public int getId() { return id; }
    public String getName() { return name; }
    public int getScore() { return score; }
    public String getAvatarImage() { return avatarImage; }

    public void setName(String name) { this.name = name; }
    public void setScore(int score) { this.score = score; }
    public void setAvatarImage(String avatarImage) { this.avatarImage = avatarImage; }
    public void addScore(int points) { this.score += points; }
}
