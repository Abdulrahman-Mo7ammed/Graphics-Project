package Texture;

import com.sun.opengl.util.GLUT;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.IOException;
import javax.media.opengl.*;
import javax.media.opengl.glu.GLU;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;

public class QuizGLEventListener extends AnimListener {

    // إضافة ثوابت للأيقونات
    int incEnymies=0;
    private static final int P_ICON_INDEX = 29; // P.png
    private static final int R_ICON_INDEX = 30; // R.png
    private static final int HEART_ICON_INDEX = 25; // heart1.png
    private static final int BACKGROUND_LEVEL1_INDEX = 26;
    private static final int BACKGROUND_LEVEL2_INDEX = 27;
    private static final int BACKGROUND_LEVEL3_INDEX = 28;

    private boolean menuIconState = false; // false = P, true = R

    public void setAudioManager(AudioManager audioManager) {
        this.audioManager = audioManager;
    }

    public QuizGLEventListener(Difficulty difficulty) {
        this.currentDifficulty = difficulty;
    }

    public void setLevel(int level) {
        currentLevel = level;
        switch(level) {
            case 1: currentBackgroundTextureIndex = BACKGROUND_LEVEL1_INDEX; break;
            case 2: currentBackgroundTextureIndex = BACKGROUND_LEVEL2_INDEX; break;
            case 3: currentBackgroundTextureIndex = BACKGROUND_LEVEL3_INDEX; break;
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

        // اللاعب الأول - التحكم بالسهام
        Fish fish1 = new Fish(100, 0,
                KeyEvent.VK_LEFT, KeyEvent.VK_RIGHT,
                KeyEvent.VK_UP, KeyEvent.VK_DOWN);
        fish1.setSoundCallback(fishSoundCallback);
        fish1.setScoreCallback(() -> {
            if (!gameOver && !gamePaused) {
                checkWinCondition();
            }
        });
        fishes.add(fish1);

        // اللاعب الثاني - التحكم بـ WASD
        if (players == 2) {
            Fish fish2 = new Fish(-100, 0,
                    KeyEvent.VK_A, KeyEvent.VK_D,
                    KeyEvent.VK_W, KeyEvent.VK_S);
            fish2.setSoundCallback(fishSoundCallback);
            fish2.setScoreCallback(() -> {
                if (!gameOver && !gamePaused) {
                    checkWinCondition();
                }
            });
            fishes.add(fish2);
        }

        // تعيين القيم الابتدائية
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

    int scoreToWin = 1000;
    boolean gameOver = false;
    boolean gamePaused = false;
    boolean showMenu = false;
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
            "Shark.png", "Shark_eat1.png", "Shark_eat2.png", "Shark_eat3.png","heart1.png",
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
        public void playEatSound() {
            audioManager.playSound("eat");
//            audioManager.playSound("bubble");
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

        @Override
        public void playWinSound() {
            audioManager.playSound("eat");
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

    }

    public void display(GLAutoDrawable gld) {
        GL gl = gld.getGL();
        gl.glClear(GL.GL_COLOR_BUFFER_BIT);
        gl.glLoadIdentity();
        incEnymies++;
        if (incEnymies==1000){
            spawnDelay--;
            incEnymies=0;
            if (spawnDelay==5) incEnymies=1001;
        }

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
                if (f.score >= 250) {
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
        gl.glMatrixMode(GL.GL_PROJECTION);
        gl.glPushMatrix();
        gl.glLoadIdentity();
        glu.gluOrtho2D(-maxWidth, maxWidth, -maxHeight, maxHeight);

        gl.glMatrixMode(GL.GL_MODELVIEW);
        gl.glPushMatrix();
        gl.glLoadIdentity();

        gl.glEnable(GL.GL_BLEND);
        gl.glBindTexture(GL.GL_TEXTURE_2D, textures[HEART_ICON_INDEX]);

        // عرض القلوب أعلى الشاشة على اليسار لكل لاعب
        for (int i = 0; i < fishes.size(); i++) {
            Fish f = fishes.get(i);
            for (int j = 0; j < f.Heart; j++) {
                gl.glPushMatrix();

                double x, y;
                double spacing = 25;
                double heartWidth = 15;
                double heartHeight = 12;

                // اللاعب الأول: أعلى اليسار
                if (i == 0) {
                    x = maxWidth - 30 - (j * spacing);
                    y = maxHeight - 40;
                }
                // اللاعب الثاني: أعلى اليمين
                else {
                    x = -maxWidth + 20 + (j * spacing);
                    y = maxHeight - 40;
                }

                gl.glTranslated(x, y, 0);

                // رسم القلب داخل دائرة
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

        // معلومات اللاعب الأول (P1) - أعلى اليسار
        if (fishes.size() > 0) {
            Fish fish1 = fishes.get(0);
            gl.glColor3f(1.0f, 1f, 1.0f);

            // P1 Score
            gl.glRasterPos2f(-maxWidth + 20, maxHeight - 20);
            String p1Text = "P1: " + fish1.score + " / " + 250;
            for (char c : p1Text.toCharArray())
                glut.glutBitmapCharacter(GLUT.BITMAP_TIMES_ROMAN_24, c); // <--- تم التغيير

                 }

        // معلومات اللاعب الثاني (P2) - أعلى اليمين
        if (playerCount == 2 && fishes.size() > 1) {
            Fish fish2 = fishes.get(1);
            gl.glColor3f(1.0f, 1f, 1f);

            // P2 Score
            gl.glRasterPos2f(maxWidth - 75, maxHeight - 20);
            String p2Text = "P2: " + fish2.score + " / " + 250;
            for (char c : p2Text.toCharArray())
                glut.glutBitmapCharacter(GLUT.BITMAP_TIMES_ROMAN_24, c); // <--- تم التغيير

                }


        gl.glEnable(GL.GL_TEXTURE_2D);
        gl.glPopMatrix();
        gl.glMatrixMode(GL.GL_PROJECTION);
        gl.glPopMatrix();
        gl.glMatrixMode(GL.GL_MODELVIEW);
    }

    private void drawMenuIcon(GL gl) {
        if (gameOver || showWinScreen || showGameOverScreen) return;

        gl.glMatrixMode(GL.GL_PROJECTION);
        gl.glPushMatrix();
        gl.glLoadIdentity();
        glu.gluOrtho2D(-maxWidth, maxWidth, -maxHeight, maxHeight);

        gl.glMatrixMode(GL.GL_MODELVIEW);
        gl.glPushMatrix();
        gl.glLoadIdentity();

        gl.glEnable(GL.GL_BLEND);
        gl.glBlendFunc(GL.GL_SRC_ALPHA, GL.GL_ONE_MINUS_SRC_ALPHA);

        // اختيار الأيقونة بناءً على حالة القائمة
        int iconIndex = menuIconState ? R_ICON_INDEX : P_ICON_INDEX;
        gl.glBindTexture(GL.GL_TEXTURE_2D, textures[iconIndex]);

        gl.glPushMatrix();
        // وضع الأيقونة في الزاوية اليمنى العليا
        gl.glTranslated(maxWidth - 30, maxHeight - 30, 0);

        // رسم خلفية دائرية للأيقونة
        gl.glDisable(GL.GL_TEXTURE_2D);
        if (showMenu) {
            gl.glColor4f(0.2f, 0.4f, 0.8f, 0.8f); // أزرق عند فتح القائمة
        } else {
            gl.glColor4f(0.8f, 0.2f, 0.2f, 0.8f); // أحمر عند إغلاقها
        }

        // رسم دائرة
        gl.glBegin(GL.GL_TRIANGLE_FAN);
        gl.glVertex2d(0, 0); // مركز الدائرة
        int segments = 32;
        double radius = 15;
        for (int i = 0; i <= segments; i++) {
            double angle = 2.0 * Math.PI * i / segments;
            gl.glVertex2d(Math.cos(angle) * radius, Math.sin(angle) * radius);
        }
        gl.glEnd();

        gl.glEnable(GL.GL_TEXTURE_2D);
        gl.glColor3f(1.0f, 1.0f, 1.0f);

        // رسم الأيقونة بداخل الدائرة
        double iconSize = 12;
        gl.glBegin(GL.GL_QUADS);
        gl.glTexCoord2f(0, 0); gl.glVertex2d(-iconSize, -iconSize);
        gl.glTexCoord2f(1, 0); gl.glVertex2d(iconSize, -iconSize);
        gl.glTexCoord2f(1, 1); gl.glVertex2d(iconSize, iconSize);
        gl.glTexCoord2f(0, 1); gl.glVertex2d(-iconSize, iconSize);
        gl.glEnd();

        gl.glPopMatrix();

        gl.glDisable(GL.GL_BLEND);
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
        gl.glColor4f(0, 0.5f, 0, 0.9f);
        gl.glBegin(GL.GL_QUADS);
        gl.glVertex2f(-300, -200);
        gl.glVertex2f(300, -200);
        gl.glVertex2f(300, 200);
        gl.glVertex2f(-300, 200);
        gl.glEnd();

        // إطار ذهبي
        gl.glColor3f(1.0f, 0.8f, 0.0f);
        gl.glLineWidth(5);
        gl.glBegin(GL.GL_LINE_LOOP);
        gl.glVertex2f(-280, -180);
        gl.glVertex2f(280, -180);
        gl.glVertex2f(280, 180);
        gl.glVertex2f(-280, 180);
        gl.glEnd();

        // تاج الفوز
        gl.glColor3f(1.0f, 0.9f, 0.0f);
        gl.glBegin(GL.GL_TRIANGLES);
        gl.glVertex2f(-40, 120);
        gl.glVertex2f(0, 160);
        gl.glVertex2f(40, 120);
        gl.glEnd();

        gl.glBegin(GL.GL_TRIANGLES);
        gl.glVertex2f(-60, 120);
        gl.glVertex2f(-20, 160);
        gl.glVertex2f(20, 120);
        gl.glEnd();

        gl.glBegin(GL.GL_TRIANGLES);
        gl.glVertex2f(20, 120);
        gl.glVertex2f(60, 160);
        gl.glVertex2f(100, 120);
        gl.glEnd();

        // نص الفوز
        gl.glColor3f(1.0f, 1.0f, 0.0f);
        gl.glRasterPos2f(-120, 80);
        String winText = "🎉 VICTORY! 🎉";
        for (char c : winText.toCharArray())
            glut.glutBitmapCharacter(GLUT.BITMAP_HELVETICA_18, c);

        // الفائز
        String winnerText;
        if (playerCount == 1) {
            winnerText = "🎯 Final Score: " + fishes.get(0).score + " points";
        } else {
            winnerText = "🏆 Player " + winner + " Wins!";
            if (winner == 1) {
                winnerText += " (" + fishes.get(0).score + " points)";
            } else {
                winnerText += " (" + fishes.get(1).score + " points)";
            }
        }

        gl.glColor3f(1.0f, 1.0f, 1.0f);
        gl.glRasterPos2f(-140, 30);
        for (char c : winnerText.toCharArray())
            glut.glutBitmapCharacter(GLUT.BITMAP_HELVETICA_18, c);

        // الخيارات مع أيقونات
        gl.glColor3f(0.5f, 1.0f, 0.5f);
        gl.glRasterPos2f(-200, -40);
        String againText = "🔄  1. Play Again (A)";
        for (char c : againText.toCharArray())
            glut.glutBitmapCharacter(GLUT.BITMAP_HELVETICA_18, c);

        gl.glColor3f(0.8f, 0.8f, 1.0f);
        gl.glRasterPos2f(-200, -80);
        String menuText = "🏠  2. Main Menu (M)";
        for (char c : menuText.toCharArray())
            glut.glutBitmapCharacter(GLUT.BITMAP_HELVETICA_18, c);

        gl.glColor3f(1.0f, 0.6f, 0.6f);
        gl.glRasterPos2f(-200, -120);
        String exitText = "🚪  3. Exit (ESC)";
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

        // خلفية داكنة
        gl.glEnable(GL.GL_BLEND);
        gl.glBlendFunc(GL.GL_SRC_ALPHA, GL.GL_ONE_MINUS_SRC_ALPHA);
        gl.glColor4f(0.3f, 0, 0, 0.9f);
        gl.glBegin(GL.GL_QUADS);
        gl.glVertex2f(-300, -200);
        gl.glVertex2f(300, -200);
        gl.glVertex2f(300, 200);
        gl.glVertex2f(-300, 200);
        gl.glEnd();

        // جمجمة
        gl.glColor3f(1.0f, 1.0f, 1.0f);
        gl.glBegin(GL.GL_LINE_LOOP);
        // دائرة الرأس
        for(int i = 0; i < 360; i++) {
            double angle = Math.toRadians(i);
            gl.glVertex2d(Math.cos(angle) * 40, Math.sin(angle) * 40 + 100);
        }
        gl.glEnd();

        // عينان
        gl.glBegin(GL.GL_QUADS);
        gl.glVertex2f(-15, 110); gl.glVertex2f(-5, 110); gl.glVertex2f(-5, 120); gl.glVertex2f(-15, 120);
        gl.glVertex2f(5, 110); gl.glVertex2f(15, 110); gl.glVertex2f(15, 120); gl.glVertex2f(5, 120);
        gl.glEnd();

        // فم
        gl.glBegin(GL.GL_LINES);
        gl.glVertex2f(-20, 80); gl.glVertex2f(20, 80);
        gl.glVertex2f(-15, 70); gl.glVertex2f(15, 70);
        gl.glVertex2f(-10, 60); gl.glVertex2f(10, 60);
        gl.glEnd();

        // نص Game Over
        gl.glColor3f(1.0f, 0.0f, 0.0f);
        gl.glRasterPos2f(-80, 30);
        String gameOverText = "💀 GAME OVER 💀";
        for (char c : gameOverText.toCharArray())
            glut.glutBitmapCharacter(GLUT.BITMAP_HELVETICA_18, c);

        // النتيجة النهائية
        String scoreText;
        if (playerCount == 1) {
            scoreText = "📊 Final Score: " + fishes.get(0).score;
        } else {
            scoreText = "P1: " + fishes.get(0).score + " | P2: " +
                    (fishes.size() > 1 ? fishes.get(1).score : 0);
        }

        gl.glColor3f(1.0f, 1.0f, 1.0f);
        gl.glRasterPos2f(-140, -20);
        for (char c : scoreText.toCharArray())
            glut.glutBitmapCharacter(GLUT.BITMAP_HELVETICA_18, c);

        // الخيارات
        gl.glColor3f(0.5f, 1.0f, 0.5f);
        gl.glRasterPos2f(-200, -60);
        String againText = "🔄  1. Try Again (A)";
        for (char c : againText.toCharArray())
            glut.glutBitmapCharacter(GLUT.BITMAP_HELVETICA_18, c);

        gl.glColor3f(0.8f, 0.8f, 1.0f);
        gl.glRasterPos2f(-200, -100);
        String menuText = "🏠  2. Main Menu (M)";
        for (char c : menuText.toCharArray())
            glut.glutBitmapCharacter(GLUT.BITMAP_HELVETICA_18, c);

        gl.glColor3f(1.0f, 0.6f, 0.6f);
        gl.glRasterPos2f(-200, -140);
        String exitText = "🚪  3. Exit (ESC)";
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

        // خلفية زرقاء شفافة تغطي معظم الشاشة العلوية
        gl.glEnable(GL.GL_BLEND);
        gl.glBlendFunc(GL.GL_SRC_ALPHA, GL.GL_ONE_MINUS_SRC_ALPHA);
        gl.glColor4f(0, 0.2f, 0.4f, 0.95f);
        gl.glBegin(GL.GL_QUADS);
        gl.glVertex2f(-maxWidth, maxHeight - 200);
        gl.glVertex2f(maxWidth, maxHeight - 200);
        gl.glVertex2f(maxWidth, maxHeight);
        gl.glVertex2f(-maxWidth, maxHeight);
        gl.glEnd();

        // إطار
        gl.glColor3f(0, 0.8f, 1.0f);
        gl.glLineWidth(3);
        gl.glBegin(GL.GL_LINE_LOOP);
        gl.glVertex2f(-maxWidth + 10, maxHeight - 190);
        gl.glVertex2f(maxWidth - 10, maxHeight - 190);
        gl.glVertex2f(maxWidth - 10, maxHeight - 10);
        gl.glVertex2f(-maxWidth + 10, maxHeight - 10);
        gl.glEnd();

        // عنوان القائمة
        gl.glColor3f(1.0f, 1.0f, 0.0f);
        gl.glRasterPos2f(-50, maxHeight - 40);
        String menuTitle = "⚙️ GAME MENU ⚙️";
        for (char c : menuTitle.toCharArray())
            glut.glutBitmapCharacter(GLUT.BITMAP_HELVETICA_18, c);

        // خيارات القائمة مع أيقونات - مرتبة أفقياً
        String[] menuItems = {
                "▶️ Resume (P)",
                "📊 Level (L)",
                "⚡ Difficulty (D)",
                "👥 Players: " + playerCount,
                "🔄 Restart (R)",
                "🏠 Menu (M)",
                "🚪 Exit (ESC)"
        };

        float startX = -maxWidth + 50;
        for (int i = 0; i < menuItems.length; i++) {
            // ألوان مختلفة لكل خيار
            if (i == 0) gl.glColor3f(0.5f, 1.0f, 0.5f); // أخضر
            else if (i == 1 || i == 2) gl.glColor3f(1.0f, 1.0f, 0.5f); // أصفر
            else if (i == 3) gl.glColor3f(0.8f, 0.5f, 1.0f); // بنفسجي
            else if (i == 4) gl.glColor3f(1.0f, 0.8f, 0.5f); // برتقالي
            else if (i == 5) gl.glColor3f(0.5f, 0.8f, 1.0f); // أزرق
            else gl.glColor3f(1.0f, 0.5f, 0.5f); // أحمر

            gl.glRasterPos2f(startX + (i * 150), maxHeight - 100);
            for (char c : menuItems[i].toCharArray())
                glut.glutBitmapCharacter(GLUT.BITMAP_HELVETICA_12, c);
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
        gl.glColor4f(0, 0, 0, 0.7f);
        gl.glBegin(GL.GL_QUADS);
        gl.glVertex2f(-120, -30);
        gl.glVertex2f(120, -30);
        gl.glVertex2f(120, 30);
        gl.glVertex2f(-120, 30);
        gl.glEnd();

        gl.glColor3f(1.0f, 1.0f, 0.0f);
        gl.glRasterPos2f(-40, 0);
        String pauseText = "⏸️ PAUSED ⏸️";
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
                break;
            case MEDIUM:
                this.spawnDelay = 20;
                this.enemySpeedMultiplier = 1.85;
                this.initialLives = 3;
                break;
            case HARD:
                this.spawnDelay = 10;
                this.enemySpeedMultiplier = 2.5;
                this.initialLives = 3;
                break;
        }

        for (Fish f : fishes) {
            f.Heart = this.initialLives;
        }
    }

    public void mouseClicked(MouseEvent e) {
        int mouseX = e.getX();
        int mouseY = e.getY();

        // تحويل إحداثيات النافذة إلى إحداثيات اللعبة
        int windowWidth = 1000;
        int windowHeight = 1000;

        double gameX = (mouseX - windowWidth/2.0) * (maxWidth * 2.0) / windowWidth;
        double gameY = (windowHeight/2.0 - mouseY) * (maxHeight * 2.0) / windowHeight;

        // التحقق من النقر على أيقونة القائمة (P/R)
        if (!gameOver && !showWinScreen && !showGameOverScreen) {
            double iconX = maxWidth - 30;
            double iconY = maxHeight - 30;
            double iconRadius = 15;

            double distX = gameX - iconX;
            double distY = gameY - iconY;
            double distance = Math.sqrt(distX * distX + distY * distY);

            if (distance <= iconRadius) {
                // تبديل حالة القائمة
                showMenu = !showMenu;
                gamePaused = showMenu;
                menuIconState = showMenu;
                return;
            }
        }

        // إذا كانت القائمة مفتوحة، التحقق من النقر على الخيارات
        if (showMenu) {
            float startX = -maxWidth + 50;
            float spacing = 150;
            float menuY = maxHeight - 100;

            // خيارات القائمة
            String[] menuOptions = {"Resume", "Level", "Difficulty", "Players", "Restart", "Main Menu", "Exit"};

            for (int i = 0; i < menuOptions.length; i++) {
                float optionX = startX + (i * spacing);

                // تقريباً عرض كل خيار 140 بكسل، ارتفاع 20 بكسل
                if (gameX >= optionX && gameX <= optionX + 140 &&
                        gameY >= menuY - 10 && gameY <= menuY + 10) {

                    handleMenuSelection(i);
                    break;
                }
            }
        }
    }

    private void handleMenuSelection(int option) {
        switch (option) {
            case 0: // Resume
                showMenu = false;
                gamePaused = false;
                menuIconState = false; // P
                break;
            case 1: // Change Level
                changeLevel();
                showMenu = false;
                gamePaused = false;
                menuIconState = false;
                break;
            case 2: // Change Difficulty
                changeDifficulty();
                showMenu = false;
                gamePaused = false;
                menuIconState = false;
                break;
            case 3: // Toggle Players
                togglePlayers();
                break;
            case 4: // Restart
                restartGame();
                break;
            case 5: // Main Menu
                returnToMainMenu();
                break;
            case 6: // Exit
                System.exit(0);
                break;
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
            case KeyEvent.VK_ESCAPE:
                showMenu = false;
                gamePaused = false;
                menuIconState = false;
                break;
            case KeyEvent.VK_2:
            case KeyEvent.VK_L:
                changeLevel();
                showMenu = false;
                gamePaused = false;
                menuIconState = false;
                break;
            case KeyEvent.VK_3:
            case KeyEvent.VK_D:
                changeDifficulty();
                showMenu = false;
                gamePaused = false;
                menuIconState = false;
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
                System.exit(0);
                break;
        }
    }

    private void handleGameInput(int keyCode) {
        switch (keyCode) {
            case KeyEvent.VK_P:
            case KeyEvent.VK_ESCAPE:
            case KeyEvent.VK_M:
                showMenu = !showMenu;
                gamePaused = showMenu;
                menuIconState = showMenu;
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
            f.isAlive = true;
            f.scale = 0.45;
        }

        spawnCounter = spawnDelay;

        audioManager.stopBackgroundMusic();
        audioManager.playBackgroundMusic("background");
    }

    private void returnToMainMenu() {
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