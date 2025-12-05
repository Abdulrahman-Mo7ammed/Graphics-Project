package Texture;

import java.awt.event.*;
import java.io.IOException;
import javax.media.opengl.*;
import javax.media.opengl.glu.GLU;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;

public class QuizGLEventListener extends AnimListener {

    int maxWidth = 300, maxHeight = 200;

    List<Fish> fishes = new ArrayList<>();
    List<Enemy> monsters = new ArrayList<>();

    int spawnDelay = 20;
    int spawnCounter = 20;

    String[] textureNames = {
            "Fish1.png",       // 0
            "Fish2.png",       // 1
            "eat.png",         // 2
            "small_fish.png",  // 3
            "Green_Fish.png",  // 4
            "Green_eat1.png",  // 5
            "Green_eat2.png",  // 6
            "Green_eat3.png",  // 7
            "Lemon_fish.png",  // 8
            "Lemon_eat1.png",  // 9
            "Lemon_eat2.png",  // 10
            "Lemon_eat3.png",  // 11
            "Lemon_eat4.png",  // 12
            "Yellow_fish.png", // 13
            "Yellow_eat1.png", // 14
            "Yellow_eat2.png", // 15
            "Yellow_eat3.png", // 16
            "Whale.png",       // 17
            "Whale_eat1.png",  // 18
            "Whale_eat2.png",  // 19
            "Whale_eat3.png",  // 20
            "Shark.png",       // 21
            "Shark_eat1.png",  // 22
            "Shark_eat2.png",  // 23
            "Shark_eat3.png",  // 24
            "sea.png"          // 25
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
                texture[i] = TextureReader.readTexture(assetsFolderName + "//Fish_game//" + textureNames[i], true);

                gl.glBindTexture(GL.GL_TEXTURE_2D, textures[i]);
                new GLU().gluBuild2DMipmaps(
                        GL.GL_TEXTURE_2D, GL.GL_RGBA,
                        texture[i].getWidth(), texture[i].getHeight(),
                        GL.GL_RGBA, GL.GL_UNSIGNED_BYTE, texture[i].getPixels()
                );
            } catch (IOException e) {
                System.out.println("Error loading texture " + textureNames[i] + ": " + e.getMessage());
            }
        }

        fishes.add(new Fish(150, 0, KeyEvent.VK_LEFT, KeyEvent.VK_RIGHT, KeyEvent.VK_UP, KeyEvent.VK_DOWN));
        fishes.add(new Fish(-150, 0, KeyEvent.VK_A, KeyEvent.VK_D, KeyEvent.VK_W, KeyEvent.VK_S));
    }

    // ===========================================================
    // DISPLAY
    // ===========================================================
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
            e.update();
            e.draw(gl, textures);

            if (e.isOutOfBounds(maxWidth)) {
                monsters.remove(i);
                i--;
            }
        }

        for (Fish f : fishes) {
            f.updateMovement(keyBits, maxWidth, maxHeight);
            f.checkCollision(monsters);
            f.draw(gl, textures);


        }
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