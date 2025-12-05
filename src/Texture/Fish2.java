package Texture;

import com.sun.opengl.util.Animator;
import com.sun.opengl.util.FPSAnimator;
import javax.media.opengl.GLCanvas;
import javax.swing.*;
import java.awt.*;
    public class Fish2 extends JFrame {

        public Fish2() {
            GLCanvas glcanvas;
            Animator animator;

            AnimListener listener = new FishGLEventListener();
            glcanvas = new GLCanvas();
            glcanvas.addGLEventListener(listener);
            glcanvas.addKeyListener(listener);
            getContentPane().add(glcanvas, BorderLayout.CENTER);
            animator = new FPSAnimator(10);
            animator.add(glcanvas);
            animator.start();
            setTitle("Fish game");
            setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            setSize(700, 700);
            setLocationRelativeTo(null);
            setVisible(true);
            setFocusable(true);
            glcanvas.requestFocus();
        }
    }

