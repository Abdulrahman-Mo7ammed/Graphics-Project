package Texture;
import javax.media.opengl.GL;

public class Enemy {
    double x, y;
    FishType type;
    int direction;

    boolean isEating = false;
    int animIndex = 0;
    int delayCounter = 0;

    public Enemy(double startX, double startY, FishType type) {
        this.x = startX;
        this.y = startY;
        this.type = type;
        this.direction = (startX > 0) ? -1 : 1;
    }

    public void update() {
        x += (type.speed * direction);
    }

    public void eat() {
         if (type.eatTextures.length > 0) {
            isEating = true;
            animIndex = 0;
        }
    }

    public void draw(GL gl, int[] textures) {
        gl.glEnable(GL.GL_BLEND);
        int texID;

        if (isEating && type.eatTextures.length > 0) {
            texID = textures[type.eatTextures[animIndex]];
            delayCounter++;
            if (delayCounter > 5) {
                animIndex++;
                delayCounter = 0;
            }
            if (animIndex >= type.eatTextures.length) {
                isEating = false;
                animIndex = 0;
                texID = textures[type.textureIndex];
            }
        } else {
            texID = textures[type.textureIndex];
        }

        gl.glBindTexture(GL.GL_TEXTURE_2D, texID);
        gl.glPushMatrix();
        gl.glTranslated(x / 300.0, y / 200.0, 0);
        gl.glScaled(0.2 * type.scale * -direction, 0.2 * type.scale, 1);

        gl.glBegin(GL.GL_QUADS);
        gl.glTexCoord2f(0,0); gl.glVertex3f(-1,-1,-1);
        gl.glTexCoord2f(1,0); gl.glVertex3f(1,-1,-1);
        gl.glTexCoord2f(1,1); gl.glVertex3f(1,1,-1);
        gl.glTexCoord2f(0,1); gl.glVertex3f(-1,1,-1);
        gl.glEnd();
        gl.glPopMatrix();
        gl.glDisable(GL.GL_BLEND);
    }

    public boolean isOutOfBounds(int maxW) {
        return Math.abs(x) > maxW + 50;
    }
}