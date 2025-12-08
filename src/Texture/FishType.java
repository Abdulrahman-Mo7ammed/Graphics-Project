package Texture;

public enum FishType {


    SMALL_FISH(3, new int[]{}, 0.4f, 3.0f, 100),

    LEMON_FISH(8, new int[]{9, 10, 11, 12}, 0.6f, 5.0f, 60),
    GREEN_FISH(4, new int[]{5, 6, 7}, 0.8f, 5.0f, 50),

    YELLOW_FISH(13, new int[]{14, 15, 16}, 1.0f, 6.0f, 40),

    SHARK(21, new int[]{22, 23, 24}, 1.5f, 8.0f, 10),

    WHALE(17, new int[]{18, 19, 20}, 2.0f, 2.0f, 5);


    public final int textureIndex;
    public final int[] eatTextures;
    public final float scale;
    public final float speed;
    public final int weight;

    FishType(int textureIndex, int[] eatTextures, float scale, float speed, int weight) {
        this.textureIndex = textureIndex;
        this.eatTextures = eatTextures;
        this.scale = scale;
        this.speed = speed;
        this.weight = weight;
    }

    public static FishType getRandomType() {
        int totalWeight = 0;
        for (FishType type : values()) totalWeight += type.weight;
        int randomValue = (int) (Math.random() * totalWeight);
        for (FishType type : values()) {
            randomValue -= type.weight;
            if (randomValue < 0) return type;
        }
        return SMALL_FISH;
    }
}