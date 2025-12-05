package Texture;

import javax.media.opengl.GL;
import java.util.BitSet;
import java.util.List;

public class Fish {

    double x, y;

    double scale = 0.45; // حجم أولي
    int dir = 1;
    int reflection = 1;
    int animationIndex = 0;

    boolean isAlive = true;
    boolean isEating = false;
    int eatCounter = 0;

    int LEFT, RIGHT, UP, DOWN;

    // ========== NEW: Score Callback ==========
    Runnable scoreCallback;

    public void setScoreCallback(Runnable r) {
        this.scoreCallback = r;
    }
    // ==========================================

    public Fish(double x, double y, int LEFT, int RIGHT, int UP, int DOWN) {
        this.x = x;
        this.y = y;
        this.LEFT = LEFT;
        this.RIGHT = RIGHT;
        this.UP = UP;
        this.DOWN = DOWN;
    }

    public void startEating() {
        isEating = true;
        eatCounter = 0;
    }

    public void updateMovement(BitSet keys, int maxW, int maxH) {
        if (!isAlive) return;

        boolean l = keys.get(LEFT);
        boolean r = keys.get(RIGHT);
        boolean u = keys.get(UP);
        boolean d = keys.get(DOWN);

        if (l && u) { move(-5, 5, maxW, maxH); dir=5; reflection=-1; animationIndex++; }
        else if (r && u) { move( 5, 5, maxW, maxH); dir=4; reflection= 1; animationIndex++; }
        else if (l && d) { move(-5,-5, maxW, maxH); dir=7; reflection=-1; animationIndex++; }
        else if (r && d) { move( 5,-5, maxW, maxH); dir=6; reflection= 1; animationIndex++; }
        else if (l)     { move(-5, 0, maxW, maxH); dir=3; reflection=-1; animationIndex++; }
        else if (r)     { move( 5, 0, maxW, maxH); dir=1; reflection= 1; animationIndex++; }
        else if (u)     { move( 0, 5, maxW, maxH); dir=0; animationIndex++; }
        else if (d)     { move( 0,-5, maxW, maxH); dir=2; animationIndex++; }

        if (animationIndex >= Integer.MAX_VALUE - 10)
            animationIndex = 0;
    }

    private void move(int dx, int dy, int maxW, int maxH){
        x += dx;
        y += dy;
        x = Math.max(-maxW+15, Math.min(maxW-15, x));
        y = Math.max(-maxH+15, Math.min(maxH-20, y));
    }

    // ======================================================
    // منطق الإصطدام بعد الدمج — يشمل زيادة السكور
    // ======================================================
    public void checkCollision(List<Enemy> enemies) {
        if (!isAlive) return;

        for (int i = 0; i < enemies.size(); i++) {
            Enemy enemy = enemies.get(i);

            double combinedRadius = (this.scale + enemy.type.scale) * 20;
            double dx = this.x - enemy.x;
            double dy = this.y - enemy.y;
            double distSq = dx*dx + dy*dy;

            if (distSq < combinedRadius * combinedRadius) {

                // لو أنا أكبر → آكل العدو
                if (this.scale > enemy.type.scale) {

                    enemies.remove(i);
                    i--;

                    this.startEating();
                    this.scale += 0.05;
                    if (this.scale > 2.5) this.scale = 2.5;

                    // ========== NEW: Increase Score ==========
                    if (scoreCallback != null) scoreCallback.run();
                    // ==========================================

                } else {
                    // لو العدو أكبر → Game Over
                    enemy.eat();
                    this.isAlive = false;
                    System.out.println("GAME OVER! Eaten by " + enemy.type);
                }
            }
        }
    }

    public void draw(GL gl, int[] textures) {
        if (!isAlive) return;

        gl.glEnable(GL.GL_BLEND);
        int textureToBind;

        if (isEating) {
            textureToBind = textures[2]; // صورة الأكل
            eatCounter++;
            if (eatCounter > 10) isEating = false;
        } else {
            textureToBind = textures[animationIndex % 2]; // Fish1 / Fish2
        }

        gl.glBindTexture(GL.GL_TEXTURE_2D, textureToBind);

        double X = x / 300.0, Y = y / 200.0;
        double angle = (dir == 4 || dir == 5) ? 30 : 0;
        if (dir == 6 || dir == 7) angle = -30;

        gl.glPushMatrix();
        gl.glTranslated(X, Y, 0);
        gl.glScaled(0.2 * reflection * scale, 0.2 * scale, 1);
        gl.glRotated(angle, 0, 0, 1);

        gl.glBegin(GL.GL_QUADS);
        gl.glTexCoord2f(0,0); gl.glVertex3f(-1,-1,-1);
        gl.glTexCoord2f(1,0); gl.glVertex3f( 1,-1,-1);
        gl.glTexCoord2f(1,1); gl.glVertex3f( 1, 1,-1);
        gl.glTexCoord2f(0,1); gl.glVertex3f(-1, 1,-1);
        gl.glEnd();

        gl.glPopMatrix();
        gl.glDisable(GL.GL_BLEND);
    }
}
