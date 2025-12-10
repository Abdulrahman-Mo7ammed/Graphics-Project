//package Texture;
//
//import com.sun.opengl.util.Animator;
//import com.sun.opengl.util.FPSAnimator;
//import javax.media.opengl.GLCanvas;
//import javax.swing.*;
//import java.awt.*;
//public class Quizz extends JFrame {
//
//    public Quizz() {
//        GLCanvas glcanvas;
//        Animator animator;
//
//        QuizGLEventListener listener = new QuizGLEventListener(QuizGLEventListener.Difficulty.EASY);
//        glcanvas = new GLCanvas();
//        glcanvas.addGLEventListener(listener);
//        glcanvas.addKeyListener(listener);
//        glcanvas.setFocusable(true); // دي مهمة
//        glcanvas.addMouseMotionListener(listener);
//        getContentPane().add(glcanvas, BorderLayout.CENTER);
//        animator = new FPSAnimator(15);
//        animator.add(glcanvas);
//        animator.start();
//        setTitle("Fish game");
//        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
//        setSize(1000, 1000);
//        setLocationRelativeTo(null);
//        setFocusable(true);
//        setVisible(true);
//        glcanvas.requestFocusInWindow();
//        glcanvas.requestFocus();
//
//    }
//}
//
