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

    int maxWidth = 300, maxHeight = 200;
    int score = 0;
    int highScore = 0; // أعلى سكور تم الوصول له
    String highScoreFile = "highscore.txt"; // اسم ملف حفظ الرقم القياسي


    GLU glu = new GLU();
    List<Fish> fishes = new ArrayList<>();
    List<Enemy> monsters = new ArrayList<>();

    double enemySpeedMultiplier = 1.0;
    int initialLives = 3;

    int spawnDelay = 20;
    int spawnCounter = 20;

    int scoreToWin = 100;
    boolean gameOver = false;


    GLUT glut = new GLUT();

    String[] textureNames = {
            "fish1-sw11.png", "fish1-sw22.png", "fish1-eat1.png", "small_fish.png",
            "Green_Fish.png", "Green_eat1.png", "Green_eat2.png", "Green_eat3.png",
            "Lemon_fish.png", "Lemon_eat1.png", "Lemon_eat2.png", "Lemon_eat3.png", "Lemon_eat4.png",
            "Yellow_fish.png", "Yellow_eat1.png", "Yellow_eat2.png", "Yellow_eat3.png",
            "Whale.png", "Whale_eat1.png", "Whale_eat2.png", "Whale_eat3.png",
            "Shark.png", "Shark_eat1.png", "Shark_eat2.png", "Shark_eat3.png","heart1.png",
            "sea.png"
    };

    TextureReader.Texture[] texture = new TextureReader.Texture[textureNames.length];
    int[] textures = new int[textureNames.length];

    BitSet keyBits = new BitSet(256);

    public enum Difficulty {
        EASY, MEDIUM, HARD
    }

    private Difficulty currentDifficulty = Difficulty.EASY;

    public void init(GLAutoDrawable gld) {
        GL gl = gld.getGL();
        gl.glClearColor(1,1,1,1);
        gl.glEnable(GL.GL_TEXTURE_2D);
        gl.glBlendFunc(GL.GL_SRC_ALPHA, GL.GL_ONE_MINUS_SRC_ALPHA);

        gl.glGenTextures(textureNames.length, textures, 0);

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

        fishes.add(new Fish(150, 0, KeyEvent.VK_LEFT, KeyEvent.VK_RIGHT, KeyEvent.VK_UP, KeyEvent.VK_DOWN));
        fishes.add(new Fish(-150, 0, KeyEvent.VK_A, KeyEvent.VK_D, KeyEvent.VK_W, KeyEvent.VK_S));
        loadHighScore();


        setDifficulty(Difficulty.EASY);

        for (Fish f : fishes)
            f.setScoreCallback(() -> f.score += 10);

        for (Fish f : fishes)
            f.setScoreCallback(() -> f.score += 10);


    }

    public void display(GLAutoDrawable gld) {
        GL gl = gld.getGL();
        gl.glClear(GL.GL_COLOR_BUFFER_BIT);
        gl.glLoadIdentity();

        drawBackground(gl);

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

        drawScore(gl);
        drawLives(gl);

        drawDifficultyBanner(gl);

        for (Fish f : fishes) {
            if (f.score > highScore) {
                highScore = f.score;
                saveHighScore();
            }
        }
        if (!gameOver) {
            for (int i = 0; i < fishes.size(); i++) {
                Fish f = fishes.get(i);
                if (f.score >= scoreToWin) {
                    gameOver = true;
                    System.out.println("Player " + (i+1) + " Wins!");
                }
            }
        }

    }
    public void drawLives(GL gl) {
        GLU glu = new GLU();

        // 1. تجهيز الشاشة للرسم ثنائي الأبعاد (UI)
        gl.glMatrixMode(GL.GL_PROJECTION);
        gl.glPushMatrix();
        gl.glLoadIdentity();
        glu.gluOrtho2D(-maxWidth, maxWidth, -maxHeight, maxHeight);

        gl.glMatrixMode(GL.GL_MODELVIEW);
        gl.glPushMatrix();
        gl.glLoadIdentity();

        gl.glEnable(GL.GL_BLEND);

        int heartIndex = textures.length - 2;
        gl.glBindTexture(GL.GL_TEXTURE_2D, textures[heartIndex]);

        // 3. اللف على اللاعبين
        for (int i = 0; i < fishes.size(); i++) {
            Fish f = fishes.get(i);

            // لو اللاعب ميت وعدد قلوبه صفر، مش هنرسم حاجة
            // بس اللوب دي هترسم عدد القلوب المتبقية
            for (int j = 0; j < f.Heart; j++) {
                gl.glPushMatrix();

                double x, y;
                double heartSize = 15; // حجم القلب
                double spacing = 35;   // المسافة بين كل قلب والتاني

                if (i == 1) {
                    x = -maxWidth + 30 + (j * spacing);
                    y = maxHeight - 50; // تحت السقف شوية عشان السكور
                } else {
                    x = maxWidth - 30 - (j * spacing);
                    y = maxHeight - 50;
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

    public void drawScore(GL gl) {
        gl.glMatrixMode(GL.GL_PROJECTION);
        gl.glPushMatrix();
        gl.glLoadIdentity();
        glu.gluOrtho2D(-maxWidth, maxWidth, -maxHeight, maxHeight);

        gl.glMatrixMode(GL.GL_MODELVIEW);
        gl.glPushMatrix();
        gl.glLoadIdentity();

        gl.glDisable(GL.GL_TEXTURE_2D);
        gl.glColor3f(1,1,1);

        // Player 1 Score LEFT
        gl.glRasterPos2f(-maxWidth + 20, maxHeight - 20);
        String text1 = "P2 Score: " + fishes.get(1).score;
        for (char c : text1.toCharArray())
            glut.glutBitmapCharacter(GLUT.BITMAP_HELVETICA_18, c);


        gl.glRasterPos2f(maxWidth - 150, maxHeight - 20);
        String text2 = "P1 Score: " + fishes.get(0).score;
        for (char c : text2.toCharArray())
            glut.glutBitmapCharacter(GLUT.BITMAP_HELVETICA_18, c);

        gl.glRasterPos2f(-50, maxHeight - 50);
        String highText = "High Score: " + highScore;
        for (char c : highText.toCharArray())
            glut.glutBitmapCharacter(GLUT.BITMAP_HELVETICA_18, c);


        gl.glEnable(GL.GL_TEXTURE_2D);

        gl.glPopMatrix();
        gl.glMatrixMode(GL.GL_PROJECTION);
        gl.glPopMatrix();
        gl.glMatrixMode(GL.GL_MODELVIEW);
    }


    public void drawBackground(GL gl){
        gl.glEnable(GL.GL_BLEND);
        gl.glBindTexture(GL.GL_TEXTURE_2D, textures[textures.length - 1]);

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
    public void drawDifficultyBanner(GL gl) {
        gl.glMatrixMode(GL.GL_PROJECTION);
        gl.glPushMatrix();
        gl.glLoadIdentity();
        glu.gluOrtho2D(-maxWidth, maxWidth, -maxHeight, maxHeight);

        gl.glMatrixMode(GL.GL_MODELVIEW);
        gl.glPushMatrix();
        gl.glLoadIdentity();

        gl.glDisable(GL.GL_TEXTURE_2D);
        gl.glColor3f(1.0f, 1.0f, 1.0f);

        String difficultyText = "Difficulty: " + currentDifficulty.toString();

        float xPos = -50;

        float yPos = maxHeight - 20;

        gl.glRasterPos2f(xPos, yPos);

        for (char c : difficultyText.toCharArray()) {
            glut.glutBitmapCharacter(GLUT.BITMAP_HELVETICA_18, c);
        }

        gl.glEnable(GL.GL_TEXTURE_2D);

        gl.glPopMatrix();
        gl.glMatrixMode(GL.GL_PROJECTION);
        gl.glPopMatrix();
        gl.glMatrixMode(GL.GL_MODELVIEW);
    }
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

    public void keyPressed(KeyEvent e) {
        int keyCode = e.getKeyCode();

        keyBits.set(keyCode);

        if (keyCode == KeyEvent.VK_E) {
            setDifficulty(Difficulty.EASY);
        } else if (keyCode == KeyEvent.VK_M) {
            setDifficulty(Difficulty.MEDIUM);
        } else if (keyCode == KeyEvent.VK_H) {
            setDifficulty(Difficulty.HARD);
        }
    }
    public void keyReleased(KeyEvent e) { keyBits.clear(e.getKeyCode()); }
    public void keyTyped(KeyEvent e) {}

    public void reshape(GLAutoDrawable d, int x, int y, int w, int h) {}
    public void displayChanged(GLAutoDrawable d, boolean m, boolean d2) {}
    public void mouseDragged(MouseEvent e) {}
    public void mouseMoved(MouseEvent e) {}
}
