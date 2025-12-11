package Texture;

import com.sun.opengl.util.FPSAnimator;
import com.sun.opengl.util.GLUT;
import java.awt.event.*;
import java.io.File;
import java.io.IOException;
import javax.media.opengl.*;
import javax.media.opengl.glu.GLU;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;
import javax.swing.*;
import java.awt.*;

public class FeedingFrenzy extends AnimListener {

    int incEnymies = 0;
    private static final int HEART_ICON_INDEX = 25;
    private static final int BACKGROUND_LEVEL1_INDEX = 26;
    private static final int BACKGROUND_LEVEL2_INDEX = 27;
    private static final int BACKGROUND_LEVEL3_INDEX = 28;

    private JFrame gameFrame;
    private FPSAnimator animator;
    private FeedingFrenzyMenu mainMenu;
    private JDialog pauseDialog;
    private JDialog endScreenDialog;
    private boolean menuOpen = false;

    public void setAudioManager(AudioManager audioManager) {
        this.audioManager = audioManager;
    }

    public FeedingFrenzy(Difficulty difficulty, FeedingFrenzyMenu menu) {
        this.currentDifficulty = difficulty;
        this.mainMenu = menu;
    }

    public void setLevel(int level) {
        currentLevel = level;
    }

    public void setDifficultyForLevel(Difficulty difficulty, int level) {
        setLevel(level);
        setDifficulty(difficulty);
    }

    public void setGameFrame(JFrame frame) { this.gameFrame = frame; }
    public void setAnimator(FPSAnimator anim) { this.animator = anim; }

    public void setPlayerCount(int players) {
        fishes.clear();
        this.playerCount = players;

        Fish fish1 = new Fish(100, 0,
                KeyEvent.VK_LEFT, KeyEvent.VK_RIGHT,
                KeyEvent.VK_UP, KeyEvent.VK_DOWN);
        fish1.setSoundCallback(fishSoundCallback);
        fish1.setScoreCallback(() -> {
            if (!gameOver && !gamePaused) checkWinCondition();
        });
        fishes.add(fish1);

        if (players == 2) {
            Fish fish2 = new Fish(-100, 0,
                    KeyEvent.VK_A, KeyEvent.VK_D,
                    KeyEvent.VK_W, KeyEvent.VK_S);
            fish2.setSoundCallback(fishSoundCallback);
            fish2.setScoreCallback(() -> {
                if (!gameOver && !gamePaused) checkWinCondition();
            });
            fishes.add(fish2);
        }

        for (Fish f : fishes) {
            f.Heart = this.initialLives;
            f.scale = 0.45;
            f.isAlive = true;
            f.score = 0;
        }
    }

    private static final String SOUNDS_PATH = System.getProperty("user.dir") + "\\Assets\\sounds\\";
    private int currentLevel = 1;
    private AudioManager audioManager;
    private int playerCount = 1;

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

    int spawnDelay = 10;
    int spawnCounter = 10;

    int scoreToWin = 250;
    boolean gameOver = false;
    boolean gamePaused = false;
    boolean showWinScreen = false;
    boolean showGameOverScreen = false;
    int winner = 0;

    GLUT glut = new GLUT();

    String[] textureNames = {
            "fish1-sw11.png", "fish1-sw22.png", "fish1-eat1.png", "small_fish.png",
            "Green_Fish.png", "Green_eat1.png", "Green_eat2.png", "Green_eat3.png",
            "Lemon_fish.png", "Lemon_eat1.png", "Lemon_eat2.png", "Lemon_eat3.png", "Lemon_eat4.png",
            "Yellow_fish.png", "Yellow_eat1.png", "Yellow_eat2.png", "Yellow_eat3.png",
            "Whale.png", "Whale_eat1.png", "Whale_eat2.png", "Whale_eat3.png",
            "Shark.png", "Shark_eat1.png", "Shark_eat2.png", "Shark_eat3.png", "heart1.png",
            "background Level 1.png", "background Level 2.png", "background Level 3.png",
            "P.png", "R.png"
    };

    TextureReader.Texture[] texture = new TextureReader.Texture[textureNames.length];
    int[] textures = new int[textureNames.length];

    BitSet keyBits = new BitSet(256);

    public enum Difficulty {
        EASY, MEDIUM, HARD
    }

    private Fish.SoundCallback fishSoundCallback = new Fish.SoundCallback() {
        @Override
        public void playEatSound() { audioManager.playSound("eat"); }
        @Override
        public void playGrowthSound() { audioManager.playSound("growth"); }
        @Override
        public void playCollisionSound() { audioManager.playSound("collision"); }
        @Override
        public void playGameOverSound() {
            audioManager.playSound("gameover");
            audioManager.stopBackgroundMusic();
        }
        @Override
        public void playWinSound() {
            audioManager.playSound("eat");
            audioManager.playSound("growth");
        }
    };

    private void playButtonClickSound() {
        if (audioManager != null) audioManager.playButtonClick();
    }


    public void init(GLAutoDrawable gld) {
        setDifficulty(currentDifficulty);
        GL gl = gld.getGL();
        gl.glClearColor(1, 1, 1, 1);
        gl.glEnable(GL.GL_TEXTURE_2D);
        gl.glBlendFunc(GL.GL_SRC_ALPHA, GL.GL_ONE_MINUS_SRC_ALPHA);

        gl.glGenTextures(textureNames.length, textures, 0);

        try {
            audioManager.playBackgroundMusic("background");
        } catch (Exception e) {
            System.out.println("Could not play background music: " + e.getMessage());
        }

        for (int i = 0; i < textureNames.length; i++) {
            try {
                texture[i] = TextureReader.readTexture(assetsFolderName + File.separator + "Fish_game" + File.separator + textureNames[i], true);
                gl.glBindTexture(GL.GL_TEXTURE_2D, textures[i]);
                GLU glu = new GLU();
                glu.gluBuild2DMipmaps(GL.GL_TEXTURE_2D, GL.GL_RGBA, texture[i].getWidth(), texture[i].getHeight(), GL.GL_RGBA, GL.GL_UNSIGNED_BYTE, texture[i].getPixels());
            } catch (IOException e) {
                System.out.println("Error loading texture " + textureNames[i]);
            }
        }
        loadHighScore();
    }

    public void display(GLAutoDrawable gld) {
        GL gl = gld.getGL();
        gl.glClear(GL.GL_COLOR_BUFFER_BIT);
        gl.glLoadIdentity();
        incEnymies++;
        if (incEnymies == 1000) {
            spawnDelay--;
            incEnymies = 0;
            if (spawnDelay == 5) incEnymies = 1001;
        }

        drawBackground(gl);

        if (!gamePaused && !showWinScreen && !showGameOverScreen && !menuOpen) {
            updateGame(gl);
        }

        drawScoreAndInfo(gl);
        drawLives(gl);

        if (gamePaused || menuOpen || showWinScreen || showGameOverScreen) {
            drawPauseOverlay(gl);
        }
    }

    private void drawPauseOverlay(GL gl) {
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
        gl.glColor4f(0, 0, 0, 0.6f);
        gl.glBegin(GL.GL_QUADS);
        gl.glVertex2f(-maxWidth, -maxHeight);
        gl.glVertex2f(maxWidth, -maxHeight);
        gl.glVertex2f(maxWidth, maxHeight);
        gl.glVertex2f(-maxWidth, maxHeight);
        gl.glEnd();

        if (gamePaused && !menuOpen && !showWinScreen && !showGameOverScreen) {
            gl.glColor3f(1.0f, 1.0f, 1.0f);
            gl.glRasterPos2f(-40, 0);
            String pauseText = "PAUSED";
            for (char c : pauseText.toCharArray())
                glut.glutBitmapCharacter(GLUT.BITMAP_TIMES_ROMAN_24, c);
        }

        gl.glDisable(GL.GL_BLEND);
        gl.glEnable(GL.GL_TEXTURE_2D);
        gl.glPopMatrix();
        gl.glMatrixMode(GL.GL_PROJECTION);
        gl.glPopMatrix();
        gl.glMatrixMode(GL.GL_MODELVIEW);
    }

    private void updateGame(GL gl) {
        if (spawnCounter == spawnDelay) {
            boolean startFromRight = Math.random() > 0.5;
            double startX = startFromRight ? maxWidth : -maxWidth;
            double startY = (Math.random() * (maxHeight * 2)) - maxHeight;
            FishType randomType = FishType.getRandomType();
            monsters.add(new Enemy(startX, startY, randomType));
            spawnCounter = 0;
        }
        spawnCounter++;

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
            if (f.Heart > 0) {
                f.updateMovement(keyBits, maxWidth, maxHeight);
                f.updateInvincible();
                f.checkCollision(monsters);
            }
            f.draw(gl, textures);
        }

        for (Fish f : fishes) {
            if (f.score > highScore) {
                highScore = f.score;
                saveHighScore();
            }
        }
        checkWinCondition();
    }

    private void checkWinCondition() {
        if (!gameOver && !showWinScreen) {
            if (playerCount == 2) {
                int deadCount = 0;
                Fish winningFish = null;
                int winnerIndex = -1;
                for (int i = 0; i < fishes.size(); i++) {
                    Fish f = fishes.get(i);
                    if (f.Heart > 0) {
                        winningFish = f;
                        winnerIndex = i + 1;
                    } else {
                        deadCount++;
                    }
                }
                if (deadCount == 1 && fishes.size() == 2) {
                    gameOver = true;
                    showWinScreen = true;
                    winner = winnerIndex;
                    fishSoundCallback.playWinSound();
                    audioManager.stopBackgroundMusic();
                    showEndScreen(true);
                    return;
                }
            }

            for (int i = 0; i < fishes.size(); i++) {
                Fish f = fishes.get(i);
                if (f.score >= scoreToWin) {
                    gameOver = true;
                    showWinScreen = true;
                    winner = i + 1;
                    fishSoundCallback.playWinSound();
                    audioManager.stopBackgroundMusic();
                    showEndScreen(true);
                    return;
                }
            }

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
                showEndScreen(false);
            }
        }
    }

    public void drawLives(GL gl) {
        gl.glMatrixMode(GL.GL_PROJECTION);
        gl.glPushMatrix();
        gl.glLoadIdentity();
        glu.gluOrtho2D(-maxWidth, maxWidth, -maxHeight, maxHeight);
        gl.glMatrixMode(GL.GL_MODELVIEW);
        gl.glPushMatrix();
        gl.glLoadIdentity();
        gl.glEnable(GL.GL_BLEND);
        gl.glBindTexture(GL.GL_TEXTURE_2D, textures[HEART_ICON_INDEX]);

        for (int i = 0; i < fishes.size(); i++) {
            Fish f = fishes.get(i);
            for (int j = 0; j < f.Heart; j++) {
                gl.glPushMatrix();
                double x, y;
                double spacing = 25;
                if (i == 0) {
                    x = maxWidth - 30 - (j * spacing);
                    y = maxHeight - 40;
                } else {
                    x = -maxWidth + 20 + (j * spacing);
                    y = maxHeight - 40;
                }
                gl.glTranslated(x, y, 0);
                gl.glBegin(GL.GL_QUADS);
                gl.glTexCoord2f(0, 0); gl.glVertex2d(-15, -12);
                gl.glTexCoord2f(1, 0); gl.glVertex2d(15, -12);
                gl.glTexCoord2f(1, 1); gl.glVertex2d(15, 12);
                gl.glTexCoord2f(0, 1); gl.glVertex2d(-15, 12);
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

        if (fishes.size() > 0) {
            Fish fish1 = fishes.get(0);
            gl.glColor3f(0.0f, 1.0f, 0.0f);
            gl.glRasterPos2f(maxWidth - 75, maxHeight - 20);
            String p1Text = "P1: " + fish1.score + " / " + scoreToWin;
            for (char c : p1Text.toCharArray())
                glut.glutBitmapCharacter(GLUT.BITMAP_TIMES_ROMAN_24, c);
        }

        String highScoreText = "HIGH SCORE: " + highScore;
        gl.glColor3f(1.0f, 1.0f, 0.0f);

        float textWidthApprox = highScoreText.length() * 9;
        gl.glRasterPos2f(-textWidthApprox / 2.0f, maxHeight - 20);

        for (char c : highScoreText.toCharArray())
            glut.glutBitmapCharacter(GLUT.BITMAP_TIMES_ROMAN_24, c);

        if (playerCount == 2 && fishes.size() > 1) {
            Fish fish2 = fishes.get(1);
            gl.glColor3f(0.0f, 1.0f, 1.0f);
            gl.glRasterPos2f(-maxWidth + 20, maxHeight - 20);
            String p2Text = "P2: " + fish2.score + " / " + scoreToWin;
            for (char c : p2Text.toCharArray())
                glut.glutBitmapCharacter(GLUT.BITMAP_TIMES_ROMAN_24, c);
        }

        gl.glColor3f(1.0f, 1.0f, 1.0f);

        gl.glEnable(GL.GL_TEXTURE_2D);
        gl.glPopMatrix();
        gl.glMatrixMode(GL.GL_PROJECTION);
        gl.glPopMatrix();
        gl.glMatrixMode(GL.GL_MODELVIEW);
    }

    public void drawBackground(GL gl) {
        gl.glEnable(GL.GL_BLEND);
        gl.glBindTexture(GL.GL_TEXTURE_2D, textures[currentBackgroundTextureIndex]);
        gl.glPushMatrix();
        gl.glBegin(GL.GL_QUADS);
        gl.glTexCoord2f(0, 0); gl.glVertex3f(-1, -1, -1);
        gl.glTexCoord2f(1, 0); gl.glVertex3f(1, -1, -1);
        gl.glTexCoord2f(1, 1); gl.glVertex3f(1, 1, -1);
        gl.glTexCoord2f(0, 1); gl.glVertex3f(-1, 1, -1);
        gl.glEnd();
        gl.glPopMatrix();
        gl.glDisable(GL.GL_BLEND);
    }

    private void showEndScreen(boolean isWin) {
        if (animator.isAnimating()) animator.stop();

        String titleText = isWin ? "VICTORY!" : "GAME OVER";
        Color titleColor = isWin ? Color.YELLOW : new Color(255, 100, 100);
        Color bgColor = isWin ? new Color(0, 50, 100, 230) : new Color(100, 0, 0, 230);
        String actionText = isWin ? "PLAY AGAIN" : "RETRY";
        Color actionColor = isWin ? new Color(0, 150, 0) : new Color(150, 150, 0);

        String scoreDetail;
        if (playerCount == 1) {
            scoreDetail = "Final Score: " + fishes.get(0).score;
        } else if (isWin) {
            scoreDetail = "Winner: Player " + winner;
        } else {
            scoreDetail = "P1: " + fishes.get(0).score + " | P2: " +
                    (fishes.size() > 1 ? fishes.get(1).score : 0);
        }

        if (endScreenDialog == null) {
            endScreenDialog = new JDialog(gameFrame, "", true);
            endScreenDialog.setUndecorated(true);
            endScreenDialog.setSize(400, 350);
            endScreenDialog.setLocationRelativeTo(gameFrame);
            endScreenDialog.setLayout(new GridBagLayout());
        }

        endScreenDialog.getContentPane().removeAll();
        endScreenDialog.getContentPane().setBackground(bgColor);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.insets = new Insets(15, 10, 15, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel title = new JLabel(titleText, SwingConstants.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 40));
        title.setForeground(titleColor);
        endScreenDialog.add(title, gbc);

        JLabel scoreLabel = new JLabel(scoreDetail, SwingConstants.CENTER);
        scoreLabel.setFont(new Font("Arial", Font.PLAIN, 20));
        scoreLabel.setForeground(Color.WHITE);
        endScreenDialog.add(scoreLabel, gbc);

        JButton retryBtn = createPauseMenuButton(actionText, actionColor);
        retryBtn.addActionListener(e -> {
            playButtonClickSound();
            endScreenDialog.dispose();
            restartGame();
        });
        endScreenDialog.add(retryBtn, gbc);

        JButton homeBtn = createPauseMenuButton("HOME", new Color(0, 100, 150));
        homeBtn.addActionListener(e -> {
            playButtonClickSound();
            endScreenDialog.dispose();
            returnToMainMenu();
        });
        endScreenDialog.add(homeBtn, gbc);


        JButton exitBtn = createPauseMenuButton("EXIT", new Color(50, 50, 50));
        exitBtn.addActionListener(e -> {
            playButtonClickSound();
            System.exit(0);
        });
        endScreenDialog.add(exitBtn, gbc);

        endScreenDialog.revalidate();
        endScreenDialog.repaint();
        endScreenDialog.setVisible(true);
    }


    public void setDifficulty(Difficulty difficulty) {
        this.currentDifficulty = difficulty;

        switch (difficulty) {
            case EASY:
                this.spawnDelay = 30;
                this.enemySpeedMultiplier = 1;
                this.initialLives = 3;
                this.currentBackgroundTextureIndex = BACKGROUND_LEVEL1_INDEX;
                break;
            case MEDIUM:
                this.spawnDelay = 20;
                this.enemySpeedMultiplier = 1.85;
                this.initialLives = 3;
                this.currentBackgroundTextureIndex = BACKGROUND_LEVEL2_INDEX;
                break;
            case HARD:
                this.spawnDelay = 10;
                this.enemySpeedMultiplier = 2.5;
                this.initialLives = 3;
                this.currentBackgroundTextureIndex = BACKGROUND_LEVEL3_INDEX;
                break;
        }

        for (Fish f : fishes) {
            f.Heart = this.initialLives;
        }
    }

    public void mouseClicked(MouseEvent e) {}

    public void keyPressed(KeyEvent e) {
        int keyCode = e.getKeyCode();

        if (!showWinScreen && !showGameOverScreen) {
            handleGameInput(keyCode);
        }
        keyBits.set(keyCode);
    }

    private void handleGameInput(int keyCode) {
        switch (keyCode) {
            case KeyEvent.VK_P:
                if (!menuOpen) {
                    gamePaused = true;
                    showPauseMenu();
                }
                break;
            case KeyEvent.VK_ESCAPE: System.exit(0); break;
        }
    }

    private void showPauseMenu() {
        if (pauseDialog == null) {
            pauseDialog = new JDialog(gameFrame, "Game Paused", true);
            pauseDialog.setUndecorated(true);
            pauseDialog.setLayout(new GridBagLayout());
            pauseDialog.setSize(300, 300);
            pauseDialog.setLocationRelativeTo(gameFrame);
            pauseDialog.getContentPane().setBackground(new Color(0, 50, 100, 230));

            GridBagConstraints gbc = new GridBagConstraints();
            gbc.gridwidth = GridBagConstraints.REMAINDER;
            gbc.insets = new Insets(15, 10, 15, 10);
            gbc.fill = GridBagConstraints.HORIZONTAL;

            JLabel title = new JLabel("PAUSED", SwingConstants.CENTER);
            title.setFont(new Font("Arial", Font.BOLD, 30));
            title.setForeground(Color.YELLOW);
            pauseDialog.add(title, gbc);

            JButton resumeBtn = createPauseMenuButton("RESUME", new Color(0, 150, 0));
            resumeBtn.addActionListener(e -> {
                playButtonClickSound();
                resumeGame();
            });
            pauseDialog.add(resumeBtn, gbc);

            JButton homeBtn = createPauseMenuButton("HOME", new Color(0, 100, 150));
            homeBtn.addActionListener(e -> {
                playButtonClickSound();
                returnToMainMenu();
            });
            pauseDialog.add(homeBtn, gbc);

            JButton exitBtn = createPauseMenuButton("EXIT", new Color(150, 0, 0));
            exitBtn.addActionListener(e -> {
                playButtonClickSound();
                System.exit(0);
            });
            pauseDialog.add(exitBtn, gbc);
        }

        menuOpen = true;
        if (animator.isAnimating()) animator.stop(); // 🛑 إيقاف الرسوم
        pauseDialog.setVisible(true);
    }

    // دالة مساعدة لإنشاء أزرار القائمة
    private JButton createPauseMenuButton(String text, Color bgColor) {
        JButton button = new JButton(text);
        button.setFont(new Font("Arial", Font.BOLD, 18));
        button.setForeground(Color.WHITE);
        button.setBackground(bgColor);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(10, 50, 10, 50));
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setOpaque(true);
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) { button.setBackground(bgColor.brighter()); }
            @Override
            public void mouseExited(MouseEvent e) { button.setBackground(bgColor); }
        });
        return button;
    }

    private void resumeGame() {
        if (pauseDialog != null) pauseDialog.dispose();
        gamePaused = false;
        menuOpen = false;

        // 1. إعادة تشغيل Animator
        if (!animator.isAnimating()) {
            animator.start();
        }

        // 2. 🚨 إعادة التركيز على GLCanvas لضمان ظهور اللعبة والتحكم 🚨
        SwingUtilities.invokeLater(() -> {
            if (gameFrame != null) {
                Component[] components = gameFrame.getContentPane().getComponents();
                for (Component comp : components) {
                    if (comp instanceof GLCanvas) {
                        comp.requestFocusInWindow();
                        break;
                    }
                }
            }
        });
    }

    private void returnToMainMenu() {
        if (pauseDialog != null) pauseDialog.dispose();
        if (endScreenDialog != null) endScreenDialog.dispose();
        if (animator.isAnimating()) animator.stop();
        audioManager.stopBackgroundMusic();

        FeedingFrenzyMenu.relaunchMenu(gameFrame);
    }

    private void restartGame() {
        if (endScreenDialog != null) endScreenDialog.dispose();

        gameOver = false;
        gamePaused = false;
        showWinScreen = false;
        showGameOverScreen = false;
        winner = 0;
        monsters.clear();
        for (Fish f : fishes) {
            f.score = 0;
            f.Heart = initialLives;
            f.isAlive = true;
            f.scale = 0.45;
        }
        spawnCounter = spawnDelay;
        if (!animator.isAnimating()) animator.start();
        audioManager.stopBackgroundMusic();
        audioManager.playBackgroundMusic("background");
    }

    public void keyReleased(KeyEvent e) { keyBits.clear(e.getKeyCode()); }
    public void keyTyped(KeyEvent e) {}
    public void loadHighScore() {
        try (java.io.BufferedReader br = new java.io.BufferedReader(new java.io.FileReader(highScoreFile))) {
            highScore = Integer.parseInt(br.readLine());
        } catch (Exception e) { highScore = 0; }
    }
    public void saveHighScore() {
        try (java.io.FileWriter fw = new java.io.FileWriter(highScoreFile)) {
            fw.write(Integer.toString(highScore));
        } catch (java.io.IOException e) { e.printStackTrace(); }
    }
    public void reshape(GLAutoDrawable d, int x, int y, int w, int h) {}
    public void displayChanged(GLAutoDrawable d, boolean m, boolean d2) {}
    public void mouseDragged(MouseEvent e) {}
    public void mouseMoved(MouseEvent e) {}

    private String player1Name = "Player1";
    private String player2Name = "Player2";
    private String assetsFolderName = "Assets";

    public void setPlayerNames(String p1, String p2) {
        this.player1Name = p1;
        this.player2Name = p2;
    }
}