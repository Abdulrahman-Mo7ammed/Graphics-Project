package Texture;

import javax.media.opengl.GL;
import java.util.BitSet;
import java.util.List;
import javax.media.opengl.glu.GLU;

public class Fish {

    public interface SoundCallback {
        void playEatSound();
        void playGrowthSound();
        void playCollisionSound();
        void playGameOverSound();
        void playWinSound();
    }

    private SoundCallback soundCallback;
    private Runnable scoreCallback;

    public double x, y;
    public double scale = 0.45;
    public int Heart = 3;
    public int score = 0;

    public boolean invincible = false;
    public int invincibleTimer = 0;

    int dir = 1;
    int reflection = 1;
    int animationIndex = 0;

    public boolean isAlive = true;
    public boolean isEating = false;
    int eatCounter = 0;

    int LEFT, RIGHT, UP, DOWN;

    public Fish(double x, double y, int LEFT, int RIGHT, int UP, int DOWN) {
        this.x = x;
        this.y = y;
        this.LEFT = LEFT;
        this.RIGHT = RIGHT;
        this.UP = UP;
        this.DOWN = DOWN;
    }

    public SoundCallback getSoundCallback() {
        return soundCallback;
    }

    public void setSoundCallback(SoundCallback callback) {
        this.soundCallback = callback;
    }

    public void setScoreCallback(Runnable r) {
        this.scoreCallback = r;
    }

    public void startEating() {
        isEating = true;
        eatCounter = 0;
    }

    public void updateInvincible() {
        if (invincible) {
            invincibleTimer--;
            if (invincibleTimer <= 0) {
                invincible = false;
            }
        }
    }

    public void updateMovement(BitSet keys, int maxW, int maxH) {
        if (!isAlive) return;

        boolean l = keys.get(LEFT);
        boolean r = keys.get(RIGHT);
        boolean u = keys.get(UP);
        boolean d = keys.get(DOWN);

        int speed = 6; // سرعة ثابتة

        if (l && u) {
            move(-speed, speed, maxW, maxH);
            dir = 5;
            reflection = 1;
            animationIndex++;
        }
        else if (r && u) {
            move(speed, speed, maxW, maxH);
            dir = 4;
            reflection = -1;
            animationIndex++;
        }
        else if (l && d) {
            move(-speed, -speed, maxW, maxH);
            dir = 7;
            reflection = 1;
            animationIndex++;
        }
        else if (r && d) {
            move(speed, -speed, maxW, maxH);
            dir = 6;
            reflection = -1;
            animationIndex++;
        }
        else if (l) {
            move(-speed, 0, maxW, maxH);
            dir = 3;
            reflection = 1;
            animationIndex++;
        }
        else if (r) {
            move(speed, 0, maxW, maxH);
            dir = 1;
            reflection = -1;
            animationIndex++;
        }
        else if (u) {
            move(0, speed, maxW, maxH);
            dir = 0;
            animationIndex++;
        }
        else if (d) {
            move(0, -speed, maxW, maxH);
            dir = 2;
            animationIndex++;
        }

        if (animationIndex >= Integer.MAX_VALUE - 10)
            animationIndex = 0;
    }

    private void move(int dx, int dy, int maxW, int maxH){
        x += dx;
        y += dy;
        x = Math.max(-maxW+15, Math.min(maxW-15, x));
        y = Math.max(-maxH+15, Math.min(maxH-20, y));
    }

    public void checkCollision(List<Enemy> enemies) {
        if (!isAlive) return;

        for (int i = 0; i < enemies.size(); i++) {
            Enemy enemy = enemies.get(i);

            double combinedRadius = (this.scale + enemy.type.scale) * 20;
            double dx = this.x - enemy.x;
            double dy = this.y - enemy.y;
            double distSq = dx*dx + dy*dy;

            if (distSq < combinedRadius * combinedRadius) {
                // صوت التصادم
                if (soundCallback != null) {
                    soundCallback.playCollisionSound();
                }

                if (this.scale > enemy.type.scale) {
                    // السمكة تأكل العدو
                    enemies.remove(i);
                    i--;

                    this.startEating();

                    // زيادة الحجم حسب نوع السمكة المأكولة
                    switch (enemy.type){
                        case SMALL_FISH:
                            scale += 0.04;score+=10;
                            break;
                        case GREEN_FISH:
                            scale += 0.05;score+=20;
                            break;
                        case LEMON_FISH:
                            scale += 0.06;score+=15;
                            break;
                        case YELLOW_FISH:
                            scale += 0.07;score+=30;
                            break;
                        case SHARK:
                            scale += 0.08;score+=40;
                            break;
                        case WHALE:
                            scale += 0.09;score+=50;
                            break;
                    }

                    // تحديد الحد الأقصى للحجم
                    if (scale > 3) scale = 3;

                    // زيادة النقاط
                    if (scoreCallback != null) {
                        scoreCallback.run();
                    }

                    // أصوات الأكل والنمو
                    if (soundCallback != null) {
                        soundCallback.playEatSound();
                     //   soundCallback.playGrowthSound();
                    }
                } else {
                    if (!invincible) {
                        enemy.eat();
                        Heart--;

                        if (Heart <= 0) {
                            isAlive = false;

                            if (soundCallback != null) {
                                soundCallback.playGameOverSound();
                            }
                        } else {
                            score = 0;

                            invincible = true;
                            invincibleTimer = 50;
                            x = 0;
                            y = 0;
                            scale = 0.45;
                        }
                    }
                }
            }
        }
    }

    public void draw(GL gl, int[] textures) {
        if (!isAlive) return;

        // تأثير الوامض عند كونها غير قابلة للإصابة
        if (invincible) {
            if ((invincibleTimer / 5) % 2 == 0) return;
        }

        gl.glEnable(GL.GL_BLEND);

        int textureToBind;

        if (isEating) {
            // استخدام إطار الأكل
            textureToBind = textures[2];
            eatCounter++;
            if (eatCounter > 5) isEating = false;
        } else {
            // تبديل بين إطارين للحركة
            textureToBind = textures[animationIndex % 2];
        }

        gl.glBindTexture(GL.GL_TEXTURE_2D, textureToBind);

        // تحويل الإحداثيات للعرض الصحيح
        double X = x / 300.0, Y = y / 200.0;
        double angle = (dir == 4 || dir == 5) ? -30 : 0;
        if (dir == 6 || dir == 7) angle = 30;

        gl.glPushMatrix();
        gl.glTranslated(X, Y, 0);
        gl.glScaled(0.2 * reflection * scale, 0.2 * scale, 1);
        gl.glRotated(angle, 0, 0, 1);

        gl.glBegin(GL.GL_QUADS);
        gl.glTexCoord2f(0,0); gl.glVertex3f(-1,-1,-1);
        gl.glTexCoord2f(1,0); gl.glVertex3f(1,-1,-1);
        gl.glTexCoord2f(1,1); gl.glVertex3f(1,1,-1);
        gl.glTexCoord2f(0,1); gl.glVertex3f(-1,1,-1);
        gl.glEnd();

        gl.glPopMatrix();
        gl.glDisable(GL.GL_BLEND);
    }
}