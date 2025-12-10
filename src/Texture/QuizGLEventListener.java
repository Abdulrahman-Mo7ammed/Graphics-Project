package Texture;

import com.sun.opengl.util.GLUT;
import java.awt.event.*;
import java.io.File;
import java.io.IOException;
import javax.media.opengl.*;
import javax.media.opengl.glu.GLU;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;

public class QuizGLEventListener extends AnimListener {

    public QuizGLEventListener(Difficulty difficulty) {
        this.currentDifficulty = difficulty;
    }

    public void setLevel(int level) {
        currentLevel = level;
        switch(level) {
            case 1: currentBackgroundTextureIndex = textureNames.length - 3; break;
            case 2: currentBackgroundTextureIndex = textureNames.length - 2; break;
            case 3: currentBackgroundTextureIndex = textureNames.length - 1; break;
        }
        setDifficulty(currentDifficulty);
    }

    public void setDifficultyForLevel(Difficulty difficulty, int level) {
        setLevel(level);
        setDifficulty(difficulty);
    }

    public void setPlayerCount(int players) {
        fishes.clear();
        this.playerCount = players;

        // اللاعب الأول (دائماً موجود)
        Fish fish1 = new Fish(150, 0,
                KeyEvent.VK_LEFT, KeyEvent.VK_RIGHT,
                KeyEvent.VK_UP, KeyEvent.VK_DOWN);
        fish1.setSoundCallback(fishSoundCallback);
        fish1.setScoreCallback(() -> {
            if (!gameOver && !gamePaused) {
                fish1.score += 10;
                checkWinCondition();
            }
        });
        fishes.add(fish1);

        // اللاعب الثاني (إذا كان اللعبة ثنائية)
        if (players == 2) {
            Fish fish2 = new Fish(-150, 0,
                    KeyEvent.VK_A, KeyEvent.VK_D,
                    KeyEvent.VK_W, KeyEvent.VK_S);
            fish2.setSoundCallback(fishSoundCallback);
            fish2.setScoreCallback(() -> {
                if (!gameOver && !gamePaused) {
                    fish2.score += 10;
                    checkWinCondition();
                }
            });
            fishes.add(fish2);
        }

        // تعيين الحياة الابتدائية حسب الصعوبة
        for (Fish f : fishes) {
            f.Heart = this.initialLives;
        }
    }

    private static final String SOUNDS_PATH = System.getProperty("user.dir") + "\\Assets\\sounds\\";
    private int currentLevel = 1;
    private AudioManager audioManager;
    private int playerCount = 1; // عدد اللاعبين الحالي

    int maxWidth = 300, maxHeight = 200;
    int score = 0;
    int highScore = 0;
    String highScoreFile = "highscore.txt";

    GLU glu = new GLU();
    List<Fish> fishes = new ArrayList<>();
    List<Enemy> monsters = new ArrayList<>();

    double enemySpeedMultiplier = 1.0;
    int initialLives = 3;

    private Difficulty currentDifficulty;
    private int currentBackgroundTextureIndex;

    int spawnDelay = 20;
    int spawnCounter = 20;

    int scoreToWin = 100;
    boolean gameOver = false;
    boolean gamePaused = false;
    boolean showMenu = false;
    boolean showWinScreen = false;
    boolean showGameOverScreen = false;
    int winner = 0; // 0: لا أحد، 1: لاعب 1، 2: لاعب 2

    GLUT glut = new GLUT();

    String[] textureNames = {
            "fish1-sw11.png", "fish1-sw22.png", "fish1-eat1.png", "small_fish.png",
            "Green_Fish.png", "Green_eat1.png", "Green_eat2.png", "Green_eat3.png",
            "Lemon_fish.png", "Lemon_eat1.png", "Lemon_eat2.png", "Lemon_eat3.png", "Lemon_eat4.png",
            "Yellow_fish.png", "Yellow_eat1.png", "Yellow_eat2.png", "Yellow_eat3.png",
            "Whale.png", "Whale_eat1.png", "Whale_eat2.png", "Whale_eat3.png",
            "Shark.png", "Shark_eat1.png", "Shark_eat2.png", "Shark_eat3.png","heart1.png",
            "background Level 1.png" , "background Level 2.png" , "background Level 3.png"
    };

    TextureReader.Texture[] texture = new TextureReader.Texture[textureNames.length];
    int[] textures = new int[textureNames.length];

    BitSet keyBits = new BitSet(256);

    public enum Difficulty {
        EASY, MEDIUM, HARD
    }

    // دالة استدعاء الأصوات
    private Fish.SoundCallback fishSoundCallback = new Fish.SoundCallback() {
        @Override
        public void playEatSound() {
            audioManager.playSound("eat");
            audioManager.playSound("bubble");
        }

        @Override
        public void playGrowthSound() {
            audioManager.playSound("growth");
        }

        @Override
        public void playCollisionSound() {
            audioManager.playSound("collision");
        }

        @Override
        public void playGameOverSound() {
            audioManager.playSound("gameover");
            audioManager.stopBackgroundMusic();
        }


        public void playWinSound() {
            audioManager.playSound("eat"); // صوت الفوز
            audioManager.playSound("growth");
        }
    };

    public void init(GLAutoDrawable gld) {
        setDifficulty(currentDifficulty);
        GL gl = gld.getGL();
        gl.glClearColor(1,1,1,1);
        gl.glEnable(GL.GL_TEXTURE_2D);
        gl.glBlendFunc(GL.GL_SRC_ALPHA, GL.GL_ONE_MINUS_SRC_ALPHA);

        gl.glGenTextures(textureNames.length, textures, 0);

        // تحميل الأصوات
        File soundsFolder = new File(SOUNDS_PATH);
        if (!soundsFolder.exists()) {
            System.err.println("ERROR: Sounds folder not found!");
            System.err.println("Path: " + SOUNDS_PATH);
        } else {
            System.out.println("Sounds folder found!");
        }

        audioManager = new AudioManager();

        String[] sounds = {
                "background .wav",
                "Bubbles.wav",
                "collision.wav",
                "fish eats.wav",
                "fish growth.wav",
                "game-over.wav",
                "zapsplat_cartoon.wav"
        };

        String[] soundNames = {
                "background", "bubble", "collision",
                "eat", "growth", "gameover", "zap"
        };

        for (int i = 0; i < sounds.length; i++) {
            String fullPath = SOUNDS_PATH + sounds[i];
            System.out.print("Loading " + soundNames[i] + "... ");
            if (new File(fullPath).exists()) {
                boolean loaded = audioManager.loadSound(soundNames[i], fullPath);
                System.out.println(loaded ? "✓" : "✗");
            } else {
                System.out.println("✗ (File not found)");
            }
        }

        try {
            audioManager.playBackgroundMusic("background");
            System.out.println("Background music started ✓");
        } catch (Exception e) {
            System.out.println("Could not play background music: " + e.getMessage());
        }

        // تحميل القوام
        for (int i = 0; i < textureNames.length; i++) {
            try {
                texture[i] = TextureReader.readTexture(
                        assetsFolderName + File.separator +
                                "Fish_game" + File.separator +
                                textureNames[i], true);

                gl.glBindTexture(GL.GL_TEXTURE_2D, textures[i]);
                GLU glu = new GLU();
                glu.gluBuild2DMipmaps(
                        GL.GL_TEXTURE_2D, GL.GL_RGBA,
                        texture[i].getWidth(), texture[i].getHeight(),
                        GL.GL_RGBA, GL.GL_UNSIGNED_BYTE, texture[i].getPixels()
                );
            } catch (IOException e) {
                System.out.println("Error loading texture " + textureNames[i]);
            }
        }

        loadHighScore();
        setPlayerCount(1);
    }

    public void display(GLAutoDrawable gld) {
        GL gl = gld.getGL();
        gl.glClear(GL.GL_COLOR_BUFFER_BIT);
        gl.glLoadIdentity();

        drawBackground(gl);

        if (!gamePaused && !showMenu && !showWinScreen && !showGameOverScreen) {
            updateGame(gl);
        }

        drawScoreAndInfo(gl);
        drawLives(gl);

        if (gameOver && showGameOverScreen) {
            drawGameOverScreen(gl);
        } else if (showWinScreen) {
            drawWinScreen(gl);
        } else if (showMenu) {
            drawInGameMenu(gl);
        } else if (gamePaused && !showMenu) {
            drawPauseText(gl);
        }
    }

    private void updateGame(GL gl) {
        spawnCounter--;
        if (spawnCounter <= 0) {
            boolean startFromRight = Math.random() > 0.5;
            double startX = startFromRight ? maxWidth : -maxWidth;
            double startY = (Math.random() * (maxHeight * 2)) - maxHeight;

            FishType randomType = FishType.getRandomType();
            monsters.add(new Enemy(startX, startY, randomType));

            spawnCounter = spawnDelay;
        }

        for (int i = 0; i < monsters.size(); i++) {
            Enemy e = monsters.get(i);
            e.update(this.enemySpeedMultiplier);
            e.draw(gl, textures);

            if (e.isOutOfBounds(maxWidth)) {
                monsters.remove(i);
                i--;
            }
        }

        for (Fish f : fishes) {
            f.updateMovement(keyBits, maxWidth, maxHeight);
            f.updateInvincible();
            f.checkCollision(monsters);
            f.draw(gl, textures);
        }

        for (Fish f : fishes) {
            if (f.score > highScore) {
                highScore = f.score;
                saveHighScore();
            }
        }
    }

    private void checkWinCondition() {
        if (!gameOver && !showWinScreen) {
            for (int i = 0; i < fishes.size(); i++) {
                Fish f = fishes.get(i);
                if (f.score >= scoreToWin) {
                    gameOver = true;
                    showWinScreen = true;
                    winner = i + 1;
                    fishSoundCallback.playWinSound();
                    audioManager.stopBackgroundMusic();
                    return;
                }
            }

            // تحقق إذا ماتت كل الأسماك
            boolean allDead = true;
            for (Fish f : fishes) {
                if (f.Heart > 0) {
                    allDead = false;
                    break;
                }
            }

            if (allDead && !gameOver) {
                gameOver = true;
                showGameOverScreen = true;
                fishSoundCallback.playGameOverSound();
            }
        }
    }

    public void drawLives(GL gl) {
        GLU glu = new GLU();

        gl.glMatrixMode(GL.GL_PROJECTION);
        gl.glPushMatrix();
        gl.glLoadIdentity();
        glu.gluOrtho2D(-maxWidth, maxWidth, -maxHeight, maxHeight);

        gl.glMatrixMode(GL.GL_MODELVIEW);
        gl.glPushMatrix();
        gl.glLoadIdentity();

        gl.glEnable(GL.GL_BLEND);
        int heartIndex = textures.length - 4;
        gl.glBindTexture(GL.GL_TEXTURE_2D, textures[heartIndex]);

        for (int i = 0; i < fishes.size(); i++) {
            Fish f = fishes.get(i);
            for (int j = 0; j < f.Heart; j++) {
                gl.glPushMatrix();

                double x, y;
                double spacing = 35;

                if (i == 1) {
                    x = -maxWidth + 30 + (j * spacing);
                    y = maxHeight - 120;
                } else {
                    x = maxWidth - 30 - (j * spacing);
                    y = maxHeight - 120;
                }

                gl.glTranslated(x, y, 0);
                double heartWidth = 20;
                double heartHeight = 15;

                gl.glBegin(GL.GL_QUADS);
                gl.glTexCoord2f(0, 0); gl.glVertex2d(-heartWidth, -heartHeight);
                gl.glTexCoord2f(1, 0); gl.glVertex2d(heartWidth, -heartHeight);
                gl.glTexCoord2f(1, 1); gl.glVertex2d(heartWidth, heartHeight);
                gl.glTexCoord2f(0, 1); gl.glVertex2d(-heartWidth, heartHeight);
                gl.glEnd();

                gl.glPopMatrix();
            }
        }

        gl.glDisable(GL.GL_BLEND);
        gl.glMatrixMode(GL.GL_PROJECTION);
        gl.glPopMatrix();
        gl.glMatrixMode(GL.GL_MODELVIEW);
        gl.glPopMatrix();
    }

    public void drawScoreAndInfo(GL gl) {
        gl.glMatrixMode(GL.GL_PROJECTION);
        gl.glPushMatrix();
        gl.glLoadIdentity();
        glu.gluOrtho2D(-maxWidth, maxWidth, -maxHeight, maxHeight);

        gl.glMatrixMode(GL.GL_MODELVIEW);
        gl.glPushMatrix();
        gl.glLoadIdentity();

        gl.glDisable(GL.GL_TEXTURE_2D);
        gl.glColor3f(1,1,1);

        // معلومات الأعلى
        // Player 1
        gl.glRasterPos2f(maxWidth - 200, maxHeight - 20);
        String p1Text = "P1: " + (fishes.size() > 0 ? fishes.get(0).score : 0) + " / " + scoreToWin;
        for (char c : p1Text.toCharArray())
            glut.glutBitmapCharacter(GLUT.BITMAP_HELVETICA_18, c);

        // Player 2
        if (playerCount == 2 && fishes.size() > 1) {
            gl.glRasterPos2f(-maxWidth + 20, maxHeight - 20);
            String p2Text = "P2: " + fishes.get(1).score + " / " + scoreToWin;
            for (char c : p2Text.toCharArray())
                glut.glutBitmapCharacter(GLUT.BITMAP_HELVETICA_18, c);
        }

        // High Score
        gl.glRasterPos2f(-50, maxHeight - 20);
        String highText = "High Score: " + highScore;
        for (char c : highText.toCharArray())
            glut.glutBitmapCharacter(GLUT.BITMAP_HELVETICA_18, c);

        // Level & Difficulty
        gl.glRasterPos2f(-maxWidth + 20, maxHeight - 50);
        String levelText = "Level: " + currentLevel + " | " + currentDifficulty;
        for (char c : levelText.toCharArray())
            glut.glutBitmapCharacter(GLUT.BITMAP_HELVETICA_18, c);

        // Players Count
        gl.glRasterPos2f(maxWidth - 150, maxHeight - 50);
        String playersText = "Players: " + playerCount;
        for (char c : playersText.toCharArray())
            glut.glutBitmapCharacter(GLUT.BITMAP_HELVETICA_18, c);

        gl.glEnable(GL.GL_TEXTURE_2D);
        gl.glPopMatrix();
        gl.glMatrixMode(GL.GL_PROJECTION);
        gl.glPopMatrix();
        gl.glMatrixMode(GL.GL_MODELVIEW);
    }

    public void drawBackground(GL gl){
        gl.glEnable(GL.GL_BLEND);
        gl.glBindTexture(GL.GL_TEXTURE_2D, textures[currentBackgroundTextureIndex]);

        gl.glPushMatrix();
        gl.glBegin(GL.GL_QUADS);
        gl.glTexCoord2f(0,0); gl.glVertex3f(-1,-1,-1);
        gl.glTexCoord2f(1,0); gl.glVertex3f(1,-1,-1);
        gl.glTexCoord2f(1,1); gl.glVertex3f(1, 1,-1);
        gl.glTexCoord2f(0,1); gl.glVertex3f(-1, 1,-1);
        gl.glEnd();
        gl.glPopMatrix();
        gl.glDisable(GL.GL_BLEND);
    }

    private void drawWinScreen(GL gl) {
        gl.glMatrixMode(GL.GL_PROJECTION);
        gl.glPushMatrix();
        gl.glLoadIdentity();
        glu.gluOrtho2D(-maxWidth, maxWidth, -maxHeight, maxHeight);

        gl.glMatrixMode(GL.GL_MODELVIEW);
        gl.glPushMatrix();
        gl.glLoadIdentity();

        gl.glDisable(GL.GL_TEXTURE_2D);

        // خلفية شفافة
        gl.glEnable(GL.GL_BLEND);
        gl.glBlendFunc(GL.GL_SRC_ALPHA, GL.GL_ONE_MINUS_SRC_ALPHA);
        gl.glColor4f(0, 0.5f, 0, 0.8f); // أخضر شفاف
        gl.glBegin(GL.GL_QUADS);
        gl.glVertex2f(-250, -150);
        gl.glVertex2f(250, -150);
        gl.glVertex2f(250, 150);
        gl.glVertex2f(-250, 150);
        gl.glEnd();

        // نص الفوز
        gl.glColor3f(1.0f, 1.0f, 0.0f); // أصفر
        gl.glRasterPos2f(-100, 80);
        String winText = "VICTORY!";
        for (char c : winText.toCharArray())
            glut.glutBitmapCharacter(GLUT.BITMAP_HELVETICA_18, c);

        // الفائز
        String winnerText;
        if (playerCount == 1) {
            winnerText = "Score: " + fishes.get(0).score;
        } else {
            winnerText = "Player " + winner + " Wins!";
            if (winner == 1) {
                winnerText += " (" + fishes.get(0).score + " points)";
            } else {
                winnerText += " (" + fishes.get(1).score + " points)";
            }
        }

        gl.glColor3f(1.0f, 1.0f, 1.0f);
        gl.glRasterPos2f(-120, 30);
        for (char c : winnerText.toCharArray())
            glut.glutBitmapCharacter(GLUT.BITMAP_HELVETICA_18, c);

        // الخيارات
        gl.glColor3f(0.8f, 1.0f, 0.8f);
        gl.glRasterPos2f(-180, -20);
        String againText = "1. Play Again (A)";
        for (char c : againText.toCharArray())
            glut.glutBitmapCharacter(GLUT.BITMAP_HELVETICA_18, c);

        gl.glColor3f(1.0f, 0.8f, 0.8f);
        gl.glRasterPos2f(-180, -50);
        String menuText = "2. Main Menu (M)";
        for (char c : menuText.toCharArray())
            glut.glutBitmapCharacter(GLUT.BITMAP_HELVETICA_18, c);

        gl.glColor3f(1.0f, 0.6f, 0.6f);
        gl.glRasterPos2f(-180, -80);
        String exitText = "3. Exit (ESC)";
        for (char c : exitText.toCharArray())
            glut.glutBitmapCharacter(GLUT.BITMAP_HELVETICA_18, c);

        gl.glDisable(GL.GL_BLEND);
        gl.glEnable(GL.GL_TEXTURE_2D);
        gl.glPopMatrix();
        gl.glMatrixMode(GL.GL_PROJECTION);
        gl.glPopMatrix();
        gl.glMatrixMode(GL.GL_MODELVIEW);
    }

    private void drawGameOverScreen(GL gl) {
        gl.glMatrixMode(GL.GL_PROJECTION);
        gl.glPushMatrix();
        gl.glLoadIdentity();
        glu.gluOrtho2D(-maxWidth, maxWidth, -maxHeight, maxHeight);

        gl.glMatrixMode(GL.GL_MODELVIEW);
        gl.glPushMatrix();
        gl.glLoadIdentity();

        gl.glDisable(GL.GL_TEXTURE_2D);

        // خلفية شفافة
        gl.glEnable(GL.GL_BLEND);
        gl.glBlendFunc(GL.GL_SRC_ALPHA, GL.GL_ONE_MINUS_SRC_ALPHA);
        gl.glColor4f(0.5f, 0, 0, 0.8f); // أحمر شفاف
        gl.glBegin(GL.GL_QUADS);
        gl.glVertex2f(-250, -150);
        gl.glVertex2f(250, -150);
        gl.glVertex2f(250, 150);
        gl.glVertex2f(-250, 150);
        gl.glEnd();

        // نص Game Over
        gl.glColor3f(1.0f, 0.0f, 0.0f);
        gl.glRasterPos2f(-70, 80);
        String gameOverText = "GAME OVER";
        for (char c : gameOverText.toCharArray())
            glut.glutBitmapCharacter(GLUT.BITMAP_HELVETICA_18, c);

        // النتيجة النهائية
        String scoreText;
        if (playerCount == 1) {
            scoreText = "Final Score: " + fishes.get(0).score;
        } else {
            scoreText = "P1: " + fishes.get(0).score + " | P2: " +
                    (fishes.size() > 1 ? fishes.get(1).score : 0);
        }

        gl.glColor3f(1.0f, 1.0f, 1.0f);
        gl.glRasterPos2f(-120, 30);
        for (char c : scoreText.toCharArray())
            glut.glutBitmapCharacter(GLUT.BITMAP_HELVETICA_18, c);

        // الخيارات
        gl.glColor3f(0.8f, 1.0f, 0.8f);
        gl.glRasterPos2f(-180, -20);
        String againText = "1. Try Again (A)";
        for (char c : againText.toCharArray())
            glut.glutBitmapCharacter(GLUT.BITMAP_HELVETICA_18, c);

        gl.glColor3f(1.0f, 0.8f, 0.8f);
        gl.glRasterPos2f(-180, -50);
        String menuText = "2. Main Menu (M)";
        for (char c : menuText.toCharArray())
            glut.glutBitmapCharacter(GLUT.BITMAP_HELVETICA_18, c);

        gl.glColor3f(1.0f, 0.6f, 0.6f);
        gl.glRasterPos2f(-180, -80);
        String exitText = "3. Exit (ESC)";
        for (char c : exitText.toCharArray())
            glut.glutBitmapCharacter(GLUT.BITMAP_HELVETICA_18, c);

        gl.glDisable(GL.GL_BLEND);
        gl.glEnable(GL.GL_TEXTURE_2D);
        gl.glPopMatrix();
        gl.glMatrixMode(GL.GL_PROJECTION);
        gl.glPopMatrix();
        gl.glMatrixMode(GL.GL_MODELVIEW);
    }

    private void drawInGameMenu(GL gl) {
        gl.glMatrixMode(GL.GL_PROJECTION);
        gl.glPushMatrix();
        gl.glLoadIdentity();
        glu.gluOrtho2D(-maxWidth, maxWidth, -maxHeight, maxHeight);

        gl.glMatrixMode(GL.GL_MODELVIEW);
        gl.glPushMatrix();
        gl.glLoadIdentity();

        gl.glDisable(GL.GL_TEXTURE_2D);

        // خلفية القائمة
        gl.glEnable(GL.GL_BLEND);
        gl.glBlendFunc(GL.GL_SRC_ALPHA, GL.GL_ONE_MINUS_SRC_ALPHA);
        gl.glColor4f(0, 0, 0.5f, 0.8f); // أزرق شفاف
        gl.glBegin(GL.GL_QUADS);
        gl.glVertex2f(-250, -150);
        gl.glVertex2f(250, -150);
        gl.glVertex2f(250, 150);
        gl.glVertex2f(-250, 150);
        gl.glEnd();

        // عنوان القائمة
        gl.glColor3f(1.0f, 0.8f, 0.0f);
        gl.glRasterPos2f(-60, 100);
        String menuTitle = "GAME MENU";
        for (char c : menuTitle.toCharArray())
            glut.glutBitmapCharacter(GLUT.BITMAP_HELVETICA_18, c);

        // خيارات القائمة
        String[] menuItems = {
                "1. Resume (P)",
                "2. Change Level (L)",
                "3. Change Difficulty (D)",
                "4. Change Players (" + playerCount + "→" + (playerCount == 1 ? "2" : "1") + ")",
                "5. Restart Game (R)",
                "6. Main Menu (M)",
                "7. Exit (ESC)"
        };

        float startY = 50;
        for (int i = 0; i < menuItems.length; i++) {
            gl.glColor3f(1.0f, 1.0f, 1.0f);
            gl.glRasterPos2f(-200, startY - (i * 30));
            for (char c : menuItems[i].toCharArray())
                glut.glutBitmapCharacter(GLUT.BITMAP_HELVETICA_18, c);
        }

        gl.glDisable(GL.GL_BLEND);
        gl.glEnable(GL.GL_TEXTURE_2D);
        gl.glPopMatrix();
        gl.glMatrixMode(GL.GL_PROJECTION);
        gl.glPopMatrix();
        gl.glMatrixMode(GL.GL_MODELVIEW);
    }

    private void drawPauseText(GL gl) {
        gl.glMatrixMode(GL.GL_PROJECTION);
        gl.glPushMatrix();
        gl.glLoadIdentity();
        glu.gluOrtho2D(-maxWidth, maxWidth, -maxHeight, maxHeight);

        gl.glMatrixMode(GL.GL_MODELVIEW);
        gl.glPushMatrix();
        gl.glLoadIdentity();

        gl.glDisable(GL.GL_TEXTURE_2D);

        gl.glEnable(GL.GL_BLEND);
        gl.glBlendFunc(GL.GL_SRC_ALPHA, GL.GL_ONE_MINUS_SRC_ALPHA);
        gl.glColor4f(0, 0, 0, 0.5f);
        gl.glBegin(GL.GL_QUADS);
        gl.glVertex2f(-100, -20);
        gl.glVertex2f(100, -20);
        gl.glVertex2f(100, 20);
        gl.glVertex2f(-100, 20);
        gl.glEnd();

        gl.glColor3f(1.0f, 1.0f, 0.0f);
        gl.glRasterPos2f(-30, 0);
        String pauseText = "PAUSED";
        for (char c : pauseText.toCharArray())
            glut.glutBitmapCharacter(GLUT.BITMAP_HELVETICA_18, c);

        gl.glDisable(GL.GL_BLEND);
        gl.glEnable(GL.GL_TEXTURE_2D);
        gl.glPopMatrix();
        gl.glMatrixMode(GL.GL_PROJECTION);
        gl.glPopMatrix();
        gl.glMatrixMode(GL.GL_MODELVIEW);
    }

    public void setDifficulty(Difficulty difficulty) {
        this.currentDifficulty = difficulty;

        switch (difficulty) {
            case EASY:
                this.spawnDelay = 30;
                this.enemySpeedMultiplier = 1;
                this.initialLives = 3;
                this.scoreToWin = 100;
                break;
            case MEDIUM:
                this.spawnDelay = 20;
                this.enemySpeedMultiplier = 1.85;
                this.initialLives = 3;
                this.scoreToWin = 150;
                break;
            case HARD:
                this.spawnDelay = 10;
                this.enemySpeedMultiplier = 2.5;
                this.initialLives = 3;
                this.scoreToWin = 200;
                break;
        }

        switch(currentLevel) {
            case 1:
                this.currentBackgroundTextureIndex = textureNames.length - 3;
                break;
            case 2:
                this.currentBackgroundTextureIndex = textureNames.length - 2;
                break;
            case 3:
                this.currentBackgroundTextureIndex = textureNames.length - 1;
                break;
        }

        for (Fish f : fishes) {
            f.Heart = this.initialLives;
        }
    }

    public void keyPressed(KeyEvent e) {
        int keyCode = e.getKeyCode();

        if (showWinScreen) {
            handleWinScreenInput(keyCode);
        } else if (showGameOverScreen) {
            handleGameOverScreenInput(keyCode);
        } else if (showMenu) {
            handleMenuInput(keyCode);
        } else if (gameOver) {
            handleGameOverInput(keyCode);
        } else {
            handleGameInput(keyCode);
        }

        keyBits.set(keyCode);
    }

    private void handleWinScreenInput(int keyCode) {
        switch (keyCode) {
            case KeyEvent.VK_1:
            case KeyEvent.VK_A:
                restartGame();
                break;
            case KeyEvent.VK_2:
            case KeyEvent.VK_M:
                returnToMainMenu();
                break;
            case KeyEvent.VK_3:
            case KeyEvent.VK_ESCAPE:
                System.exit(0);
                break;
        }
    }

    private void handleGameOverScreenInput(int keyCode) {
        switch (keyCode) {
            case KeyEvent.VK_1:
            case KeyEvent.VK_A:
                restartGame();
                break;
            case KeyEvent.VK_2:
            case KeyEvent.VK_M:
                returnToMainMenu();
                break;
            case KeyEvent.VK_3:
            case KeyEvent.VK_ESCAPE:
                System.exit(0);
                break;
        }
    }

    private void handleGameOverInput(int keyCode) {
        switch (keyCode) {
            case KeyEvent.VK_R:
                restartGame();
                break;
            case KeyEvent.VK_ESCAPE:
                System.exit(0);
                break;
        }
    }

    private void handleMenuInput(int keyCode) {
        switch (keyCode) {
            case KeyEvent.VK_1:
            case KeyEvent.VK_P:
                showMenu = false;
                gamePaused = false;
                break;
            case KeyEvent.VK_2:
            case KeyEvent.VK_L:
                changeLevel();
                showMenu = false;
                gamePaused = false;
                break;
            case KeyEvent.VK_3:
            case KeyEvent.VK_D:
                changeDifficulty();
                showMenu = false;
                gamePaused = false;
                break;
            case KeyEvent.VK_4:
                togglePlayers();
                break;
            case KeyEvent.VK_5:
            case KeyEvent.VK_R:
                restartGame();
                break;
            case KeyEvent.VK_6:
            case KeyEvent.VK_M:
                returnToMainMenu();
                break;
            case KeyEvent.VK_7:
            case KeyEvent.VK_ESCAPE:
                System.exit(0);
                break;
        }
    }

    private void handleGameInput(int keyCode) {
        switch (keyCode) {
            case KeyEvent.VK_P:
            case KeyEvent.VK_ESCAPE:
                gamePaused = !gamePaused;
                if (!gamePaused) {
                    showMenu = false;
                }
                break;
            case KeyEvent.VK_M:
                showMenu = true;
                gamePaused = true;
                break;
            case KeyEvent.VK_L:
                changeLevel();
                break;
            case KeyEvent.VK_D:
                changeDifficulty();
                break;
            case KeyEvent.VK_R:
                restartGame();
                break;
        }
    }

    private void changeLevel() {
        currentLevel++;
        if (currentLevel > 3) {
            currentLevel = 1;
        }
        setLevel(currentLevel);
    }

    private void changeDifficulty() {
        switch (currentDifficulty) {
            case EASY:
                currentDifficulty = Difficulty.MEDIUM;
                break;
            case MEDIUM:
                currentDifficulty = Difficulty.HARD;
                break;
            case HARD:
                currentDifficulty = Difficulty.EASY;
                break;
        }
        setDifficulty(currentDifficulty);
    }

    private void togglePlayers() {
        if (playerCount == 1) {
            setPlayerCount(2);
        } else {
            setPlayerCount(1);
        }
        showMenu = false;
        gamePaused = false;
    }

    private void restartGame() {
        gameOver = false;
        gamePaused = false;
        showMenu = false;
        showWinScreen = false;
        showGameOverScreen = false;
        winner = 0;
        monsters.clear();

        for (Fish f : fishes) {
            f.score = 0;
            f.Heart = initialLives;
        }

        spawnCounter = spawnDelay;

        audioManager.stopBackgroundMusic();
        audioManager.playBackgroundMusic("background");
    }

    private void returnToMainMenu() {
        // هذه الدالة تحتاج لتطبيق حسب نظامك
        // حالياً نعيد تشغيل اللعبة مع الإعدادات الافتراضية
        gameOver = false;
        gamePaused = false;
        showMenu = false;
        showWinScreen = false;
        showGameOverScreen = false;
        winner = 0;
        currentLevel = 1;
        currentDifficulty = Difficulty.EASY;
        setPlayerCount(1);
        restartGame();
    }

    public void keyReleased(KeyEvent e) {
        keyBits.clear(e.getKeyCode());
    }

    public void keyTyped(KeyEvent e) {}

    public void loadHighScore() {
        try (java.io.BufferedReader br = new java.io.BufferedReader(new java.io.FileReader(highScoreFile))) {
            highScore = Integer.parseInt(br.readLine());
        } catch (Exception e) {
            highScore = 0;
        }
    }

    public void saveHighScore() {
        try (java.io.FileWriter fw = new java.io.FileWriter(highScoreFile)) {
            fw.write(Integer.toString(highScore));
        } catch (java.io.IOException e) {
            e.printStackTrace();
        }
    }

    public void reshape(GLAutoDrawable d, int x, int y, int w, int h) {}
    public void displayChanged(GLAutoDrawable d, boolean m, boolean d2) {}
    public void mouseDragged(MouseEvent e) {}
    public void mouseMoved(MouseEvent e) {}
}