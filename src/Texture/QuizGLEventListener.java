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

    int maxWidth = 300, maxHeight = 200 , score = 0;

    List<Fish> fishes = new ArrayList<>();
    List<Enemy> monsters = new ArrayList<>();

    int spawnDelay = 20;
    int spawnCounter = 20;

    GLUT glut = new GLUT(); // <<< تعريف GLUT مرة واحدة

    String[] textureNames = {
            "Fish1.png", "Fish2.png", "eat.png", "small_fish.png",
            "Green_Fish.png", "Green_eat1.png", "Green_eat2.png", "Green_eat3.png",
            "Lemon_fish.png", "Lemon_eat1.png", "Lemon_eat2.png", "Lemon_eat3.png", "Lemon_eat4.png",
            "Yellow_fish.png", "Yellow_eat1.png", "Yellow_eat2.png", "Yellow_eat3.png",
            "Whale.png", "Whale_eat1.png", "Whale_eat2.png", "Whale_eat3.png",
            "Shark.png", "Shark_eat1.png", "Shark_eat2.png", "Shark_eat3.png",
            "sea.png"
    };

    TextureReader.Texture[] texture = new TextureReader.Texture[textureNames.length];
    int[] textures = new int[textureNames.length];

    BitSet keyBits = new BitSet(256);

    // ===========================================================
    // INIT
    // ===========================================================
    public void init(GLAutoDrawable gld) {
        GL gl = gld.getGL();
        gl.glClearColor(1,1,1,1);
        gl.glEnable(GL.GL_TEXTURE_2D);
        gl.glBlendFunc(GL.GL_SRC_ALPHA, GL.GL_ONE_MINUS_SRC_ALPHA);

        gl.glGenTextures(textureNames.length, textures, 0);

        for (int i = 0; i < textureNames.length; i++) {
            try {
                texture[i] = TextureReader.readTexture(assetsFolderName + File.separator + "Fish_game" + File.separator + textureNames[i], true);
                if (texture[i] == null) {
                    System.out.println("TextureReader returned null for " + textureNames[i]);
                    textures[i] = 0;
                    continue;
                }

                gl.glBindTexture(GL.GL_TEXTURE_2D, textures[i]);
                GLU glu = new GLU();
                glu.gluBuild2DMipmaps(
                        GL.GL_TEXTURE_2D, GL.GL_RGBA,
                        texture[i].getWidth(), texture[i].getHeight(),
                        GL.GL_RGBA, GL.GL_UNSIGNED_BYTE, texture[i].getPixels()
                );
            } catch (IOException e) {
                System.out.println("Error loading texture " + textureNames[i] + ": " + e.getMessage());
                textures[i] = 0;
            }
        }

        // إضافة Fish
        fishes.add(new Fish(150, 0, KeyEvent.VK_LEFT, KeyEvent.VK_RIGHT, KeyEvent.VK_UP, KeyEvent.VK_DOWN));
        fishes.add(new Fish(-150, 0, KeyEvent.VK_A, KeyEvent.VK_D, KeyEvent.VK_W, KeyEvent.VK_S));

        // إضافة score callback لكل Fish
        for (Fish f : fishes) {
            f.setScoreCallback(() -> score++);
        }
    }

    // ===========================================================
    // DISPLAY
    // ===========================================================
    public void display(GLAutoDrawable gld) {
        GL gl = gld.getGL();
        gl.glClear(GL.GL_COLOR_BUFFER_BIT);
        gl.glLoadIdentity();

        drawBackground(gl);

        // spawn monsters
        spawnCounter--;
        if (spawnCounter <= 0) {
            boolean startFromRight = Math.random() > 0.5;
            double startX = startFromRight ? maxWidth : -maxWidth;
            double startY = (Math.random() * (maxHeight * 2)) - maxHeight;

            FishType randomType = FishType.getRandomType();
            monsters.add(new Enemy(startX, startY, randomType));

            spawnCounter = spawnDelay;
        }

        // تحديث ورسم كل Enemy
        for (int i = 0; i < monsters.size(); i++) {
            Enemy e = monsters.get(i);
            e.update();
            e.draw(gl, textures);

            if (e.isOutOfBounds(maxWidth)) {
                monsters.remove(i);
                i--;
            }
        }

        // تحديث ورسم كل Fish
        for (Fish f : fishes) {
            f.updateMovement(keyBits, maxWidth, maxHeight);
            f.checkCollision(monsters); // هنا ممكن يحصل زيادة في score
            f.draw(gl, textures);
        }

        // =======================================================
        // عرض Score
        // =======================================================
        GLU glu = new GLU();
        // حفظ Projection القديم
        gl.glMatrixMode(GL.GL_PROJECTION);
        gl.glPushMatrix();
        gl.glLoadIdentity();
        glu.gluOrtho2D(-maxWidth, maxWidth, -maxHeight, maxHeight);

        gl.glMatrixMode(GL.GL_MODELVIEW);
        gl.glPushMatrix();
        gl.glLoadIdentity();

        // رسم النص
        gl.glDisable(GL.GL_TEXTURE_2D);
        gl.glColor4f(1f, 1f, 1f, 1f);

        gl.glRasterPos2f(-maxWidth + 20, maxHeight - 20);
        String text = "Score: " + score;
        for (char c : text.toCharArray()) {
            glut.glutBitmapCharacter(GLUT.BITMAP_HELVETICA_18, c);
        }
        gl.glEnable(GL.GL_TEXTURE_2D);

        // استعادة Projection القديم
        gl.glPopMatrix();
        gl.glMatrixMode(GL.GL_PROJECTION);
        gl.glPopMatrix();
        gl.glMatrixMode(GL.GL_MODELVIEW);
    }

    // ===========================================================
    // BACKGROUND
    // ===========================================================
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

    // ===========================================================
    // KEYBOARD
    // ===========================================================
    public void keyPressed(KeyEvent e) { keyBits.set(e.getKeyCode()); }
    public void keyReleased(KeyEvent e) { keyBits.clear(e.getKeyCode()); }
    public void keyTyped(KeyEvent e) {}

    public void reshape(GLAutoDrawable d, int x, int y, int w, int h) {}
    public void displayChanged(GLAutoDrawable d, boolean m, boolean d2) {}
    public void mouseDragged(MouseEvent e) {}
    public void mouseMoved(MouseEvent e) {}
}
