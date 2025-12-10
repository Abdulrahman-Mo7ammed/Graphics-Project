package InterFace;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import Texture.QuizGLEventListener;
import com.sun.opengl.util.FPSAnimator;

import javax.media.opengl.GLCanvas;

public class FeedingFrenzyMenu extends JFrame {

    // المتغيرات الخاصة بالصور واللوحات
    private ImageIcon newGameNormal, newGameHover;
    private ImageIcon gameOptionsNormal, gameOptionsHover;
    private ImageIcon exitNormal, exitHover;
    private ImageIcon player1Normal, player1Hover;
    private ImageIcon player2Normal, player2Hover;
    private ImageIcon menuIcon;
    private ImageIcon easyBefore2, easyAfter1, easyAfter2;
    private ImageIcon mediumBefore2, mediumAfter1, mediumAfter2;
    private ImageIcon hardBefore2, hardAfter1, hardAfter2;
    private Image menuBackground;
    private Image sharedBackground;

    private final JPanel mainPanel;
    private final CardLayout cardLayout;
    private int playerCount = 1;
    private QuizGLEventListener.Difficulty difficulty;
    private int selectedLevel = 1; // إضافة هذا المتغير
    private DifficultyButton selectedDifficultyButton = null;

    // متغيرات لتخزين اللوحات القابلة للتحديث
    private JPanel playerSelectionPanel;

    // ألوان مُحسَّنة
    private final Color PRIMARY_BLUE = new Color(0, 100, 150);
    private final Color HOVER_CYAN = new Color(0, 150, 200);
    private final Color ACCENT_YELLOW = new Color(255, 200, 0);

    public FeedingFrenzyMenu() {
        setTitle("Feeding Frenzy");
        setSize(800, 800);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);

        loadImages();

        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);

        mainPanel.add(createMainMenu(), "MAIN_MENU");

        // إنشاء اللوحات الأساسية
        playerSelectionPanel = createPlayerSelection();
        mainPanel.add(playerSelectionPanel, "PLAYER_SELECT");

        mainPanel.add(createOptionsScreen(), "OPTIONS_SCREEN");

        add(mainPanel);
        cardLayout.show(mainPanel, "MAIN_MENU");

        setVisible(true);
    }

    // =========================================================================
    // 1. إدارة الصور والأيقونات (Load Images)
    // =========================================================================

    private void loadImages() {
        try {
            // تحميل الخلفيات
            menuBackground = ImageIO.read(new File("src/InterFace/Menu background.png"));
            sharedBackground = ImageIO.read(new File("src/InterFace/sharedBackground.png"));

            try {
                newGameNormal = resizeImageIcon(new ImageIcon("src/InterFace/New Game 1.png"), 150, 150);
                newGameHover = resizeImageIcon(new ImageIcon("src/InterFace/New Game 2.png"), 150, 150);
                gameOptionsNormal = resizeImageIcon(new ImageIcon("src/InterFace/Game Options 1.png"), 150, 150);
                gameOptionsHover = resizeImageIcon(new ImageIcon("src/InterFace/Game Options 2.png"), 150, 150);
                exitNormal = resizeImageIcon(new ImageIcon("src/InterFace/Exit 1.png"), 150, 150);
                exitHover = resizeImageIcon(new ImageIcon("src/InterFace/Exit 2.png"), 150, 150);
            } catch (Exception e) {
                System.out.println("Warning: Button images not found. Using default text icons.");
                createDefaultImages();
                return;
            }

            try {
                player1Normal = resizeImageIcon(new ImageIcon("src/InterFace/1 Player 1.png"), 150, 150);
                player1Hover = resizeImageIcon(new ImageIcon("src/InterFace/1 Player 2.png"), 150, 150);
                player2Normal = resizeImageIcon(new ImageIcon("src/InterFace/Multi Player 1.png"), 150, 150);
                player2Hover = resizeImageIcon(new ImageIcon("src/InterFace/Multi Player 2.png"), 150, 150);
            } catch (Exception e) {
                System.out.println("Warning: Player images not found. Using default player icons.");
                player1Normal = createTextIcon("1 PLAYER", Color.CYAN, 150, 150);
                player1Hover = createTextIcon("1 PLAYER", ACCENT_YELLOW, 150, 150);
                player2Normal = createTextIcon("2 PLAYERS", Color.ORANGE, 150, 150);
                player2Hover = createTextIcon("2 PLAYERS", ACCENT_YELLOW, 150, 150);
            }

            menuIcon = resizeImageIcon(new ImageIcon("src/InterFace/icon.png"), 400, 400);

            easyBefore2 = resizeImageIcon(new ImageIcon("src/InterFace/before 2.png"), 120, 50);
            easyAfter1 = resizeImageIcon(new ImageIcon("src/InterFace/after 1.png"), 120, 50);
            easyAfter2 = resizeImageIcon(new ImageIcon("src/InterFace/after 2.png"), 120, 50);
            mediumBefore2 = resizeImageIcon(new ImageIcon("src/InterFace/before 2.png"), 120, 50);
            mediumAfter1 = resizeImageIcon(new ImageIcon("src/InterFace/after 1.png"), 120, 50);
            mediumAfter2 = resizeImageIcon(new ImageIcon("src/InterFace/after 2.png"), 120, 50);
            hardBefore2 = resizeImageIcon(new ImageIcon("src/InterFace/before 2.png"), 120, 50);
            hardAfter1 = resizeImageIcon(new ImageIcon("src/InterFace/after 1.png"), 120, 50);
            hardAfter2 = resizeImageIcon(new ImageIcon("src/InterFace/after 2.png"), 120, 50);

        } catch (Exception e) {
            System.out.println("Error loading images: " + e.getMessage());
            menuBackground = null;
            sharedBackground = null;
            createDefaultImages();
        }
    }

    private ImageIcon resizeImageIcon(ImageIcon icon, int width, int height) {
        if (icon.getImage() == null) return icon;
        Image img = icon.getImage();
        Image resizedImg = img.getScaledInstance(width, height, Image.SCALE_SMOOTH);
        return new ImageIcon(resizedImg);
    }

    private void createDefaultImages() {
        newGameNormal = createTextIcon("NEW GAME", Color.WHITE, 150, 50);
        newGameHover = createTextIcon("NEW GAME", ACCENT_YELLOW, 150, 50);
        gameOptionsNormal = createTextIcon("OPTIONS", Color.WHITE, 150, 50);
        gameOptionsHover = createTextIcon("OPTIONS", ACCENT_YELLOW, 150, 50);
        exitNormal = createTextIcon("EXIT", Color.WHITE, 150, 50);
        exitHover = createTextIcon("EXIT", ACCENT_YELLOW, 150, 50);

        player1Normal = createTextIcon("1 PLAYER", Color.CYAN, 120, 120);
        player1Hover = createTextIcon("1 PLAYER", ACCENT_YELLOW, 120, 120);
        player2Normal = createTextIcon("2 PLAYERS", Color.ORANGE, 120, 120);
        player2Hover = createTextIcon("2 PLAYERS", ACCENT_YELLOW, 120, 120);

        menuIcon = createFishIcon(400, 400);

        easyBefore2 = createTextIcon("EASY", Color.GREEN, 120, 50);
        easyAfter1 = createTextIcon("✓ EASY", Color.GREEN.brighter(), 120, 50);
        easyAfter2 = createTextIcon("✓ EASY", Color.YELLOW, 120, 50);

        mediumBefore2 = createTextIcon("MEDIUM", Color.ORANGE, 120, 50);
        mediumAfter1 = createTextIcon("✓ MEDIUM", Color.ORANGE.brighter(), 120, 50);
        mediumAfter2 = createTextIcon("✓ MEDIUM", Color.YELLOW, 120, 50);

        hardBefore2 = createTextIcon("HARD", Color.RED, 120, 50);
        hardAfter1 = createTextIcon("✓ HARD", Color.RED.brighter(), 120, 50);
        hardAfter2 = createTextIcon("✓ HARD", Color.YELLOW, 120, 50);
    }

    private ImageIcon createTextIcon(String text, Color color, int width, int height) {
        BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = img.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setColor(new Color(0, 0, 0, 0));
        g2d.fillRect(0, 0, width, height);

        g2d.setColor(color);
        g2d.setFont(new Font("Arial", Font.BOLD, 16));
        FontMetrics fm = g2d.getFontMetrics();
        int x = (width - fm.stringWidth(text)) / 2;
        int y = (height - fm.getHeight()) / 2 + fm.getAscent();
        g2d.drawString(text, x, y);
        g2d.dispose();
        return new ImageIcon(img);
    }

    private ImageIcon createFishIcon(int width, int height) {
        BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = img.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        int fishWidth = width * 60 / 100;
        int fishHeight = height * 30 / 100;
        g2d.setColor(Color.ORANGE);
        g2d.fillOval(20, 35, fishWidth, fishHeight);
        g2d.setColor(Color.RED);
        int[] tailX = {20 + fishWidth, 20 + fishWidth + 15, 20 + fishWidth};
        int[] tailY = {35 + 5, 35 + fishHeight/2, 35 + fishHeight - 5};
        g2d.fillPolygon(tailX, tailY, 3);
        g2d.setColor(Color.WHITE);
        g2d.fillOval(35, 45, 10, 10);
        g2d.setColor(Color.BLACK);
        g2d.fillOval(37, 47, 6, 6);
        g2d.dispose();
        return new ImageIcon(img);
    }

    private JPanel createBackgroundPanel(Image bgImage, Color defaultColor) {
        return new JPanel(new GridBagLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (bgImage != null) {
                    g.drawImage(bgImage, 0, 0, getWidth(), getHeight(), this);
                } else {
                    g.setColor(defaultColor);
                    g.fillRect(0, 0, getWidth(), getHeight());
                }
            }
        };
    }

    // =========================================================================
    // 2. القائمة الرئيسية (MAIN_MENU)
    // =========================================================================

    private JPanel createMainMenu() {
        JPanel panel = createBackgroundPanel(menuBackground, new Color(10, 60, 120));
        panel.setLayout(new BorderLayout());

        JLabel iconLabel = new JLabel(menuIcon);
        iconLabel.setHorizontalAlignment(SwingConstants.CENTER);
        iconLabel.setBorder(BorderFactory.createEmptyBorder(50, 0, 20, 0));
        panel.add(iconLabel, BorderLayout.NORTH);

        JPanel buttonContainer = new JPanel();
        buttonContainer.setLayout(new FlowLayout(FlowLayout.CENTER, 30, 20));
        buttonContainer.setOpaque(false);

        MenuButton btnNewGame = new MenuButton(newGameNormal, newGameHover, "NEW_GAME");
        MenuButton btnOptions = new MenuButton(gameOptionsNormal, gameOptionsHover, "OPTIONS");
        MenuButton btnExit = new MenuButton(exitNormal, exitHover, "EXIT");

        btnNewGame.addActionListener(e -> handleMainMenu("NEW_GAME"));
        btnOptions.addActionListener(e -> handleMainMenu("OPTIONS"));
        btnExit.addActionListener(e -> handleMainMenu("EXIT"));

        buttonContainer.add(btnNewGame);
        buttonContainer.add(btnOptions);
        buttonContainer.add(btnExit);

        panel.add(buttonContainer, BorderLayout.CENTER);

        return panel;
    }

    static class MenuButton extends JButton {
        private final ImageIcon normalIcon;
        private final ImageIcon hoverIcon;

        public MenuButton(ImageIcon normal, ImageIcon hover, String action) {
            super(normal);
            this.normalIcon = normal;
            this.hoverIcon = hover;
            setActionCommand(action);
            setBorderPainted(false);
            setContentAreaFilled(false);
            setFocusPainted(false);
            setOpaque(false);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) { setIcon(hoverIcon); }
                @Override
                public void mouseExited(MouseEvent e) { setIcon(normalIcon); }
            });
        }
    }

    private void handleMainMenu(String command) {
        switch (command) {
            case "NEW_GAME":
                // إعادة ضبط جميع الاختيارات
                difficulty = null;
                selectedLevel = 1;
                playerCount = 1;
                selectedDifficultyButton = null;

                // إعادة إنشاء لوحة اللاعبين
                mainPanel.remove(playerSelectionPanel);
                playerSelectionPanel = createPlayerSelection();
                mainPanel.add(playerSelectionPanel, "PLAYER_SELECT");

                cardLayout.show(mainPanel, "PLAYER_SELECT");
                break;

            case "OPTIONS":
                cardLayout.show(mainPanel, "OPTIONS_SCREEN");
                break;

            case "EXIT":
                exitGame();
                break;

            case "MAIN_MENU":
                difficulty = null;
                selectedLevel = 1;
                selectedDifficultyButton = null;
                cardLayout.show(mainPanel, "MAIN_MENU");
                break;
        }
    }

    // =========================================================================
    // 3. شاشة اختيار اللاعبين (PLAYER_SELECT)
    // =========================================================================

    private JPanel createPlayerSelection() {
        JPanel panel = createBackgroundPanel(sharedBackground, new Color(0, 50, 100));
        panel.setLayout(new BorderLayout());

        JLabel titleLabel = new JLabel("SELECT PLAYERS", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Serif", Font.BOLD, 36));
        titleLabel.setForeground(ACCENT_YELLOW);
        titleLabel.setBorder(BorderFactory.createEmptyBorder(50,0,100,0));
        panel.add(titleLabel, BorderLayout.NORTH);

        JPanel playersPanel = new JPanel();
        playersPanel.setLayout(new BoxLayout(playersPanel, BoxLayout.Y_AXIS));
        playersPanel.setOpaque(false);
        playersPanel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JButton btn1Player = new JButton(player1Normal);
        btn1Player.setBorderPainted(false);
        btn1Player.setContentAreaFilled(false);
        btn1Player.setFocusPainted(false);
        btn1Player.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn1Player.setAlignmentX(Component.CENTER_ALIGNMENT);
        btn1Player.addActionListener(e -> {
            playerCount = 1;
            showLevelsAfterPlayerSelection();
        });
        btn1Player.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                btn1Player.setIcon(player1Hover);
                btn1Player.setBorder(BorderFactory.createLineBorder(Color.CYAN, 3));
            }
            @Override
            public void mouseExited(MouseEvent e) {
                btn1Player.setIcon(player1Normal);
                btn1Player.setBorder(BorderFactory.createEmptyBorder());
            }
        });

        JButton btn2Players = new JButton(player2Normal);
        btn2Players.setBorderPainted(false);
        btn2Players.setContentAreaFilled(false);
        btn2Players.setFocusPainted(false);
        btn2Players.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn2Players.setAlignmentX(Component.CENTER_ALIGNMENT);
        btn2Players.addActionListener(e -> {
            playerCount = 2;
            showLevelsAfterPlayerSelection();
        });
        btn2Players.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                btn2Players.setIcon(player2Hover);
                btn2Players.setBorder(BorderFactory.createLineBorder(Color.MAGENTA, 3));
            }
            @Override
            public void mouseExited(MouseEvent e) {
                btn2Players.setIcon(player2Normal);
                btn2Players.setBorder(BorderFactory.createEmptyBorder());
            }
        });

        playersPanel.add(btn1Player);
        playersPanel.add(Box.createRigidArea(new Dimension(0, 80)));
        playersPanel.add(btn2Players);

        panel.add(playersPanel, BorderLayout.CENTER);

        JPanel navPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 40, 20));
        navPanel.setOpaque(false);
        JButton backBtn = createStyledNavButton("← BACK", new Color(100, 100, 200),
                e -> handleMainMenu("MAIN_MENU"));
        navPanel.add(backBtn);
        panel.add(navPanel, BorderLayout.SOUTH);

        return panel;
    }

    // =========================================================================
    // 4. شاشة اختيار المستوى (SELECT_LEVEL)
    // =========================================================================

    private void showLevelsAfterPlayerSelection() {
        JPanel fullPanel = createBackgroundPanel(sharedBackground, new Color(0, 50, 100));
        fullPanel.setLayout(new BorderLayout());

        JLabel titleLabel = new JLabel("SELECT LEVEL", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Serif", Font.BOLD, 40));
        titleLabel.setForeground(ACCENT_YELLOW);
        titleLabel.setBorder(BorderFactory.createEmptyBorder(50,0,50,0));
        fullPanel.add(titleLabel, BorderLayout.NORTH);

        JPanel levelsPanel = new JPanel();
        levelsPanel.setLayout(new BoxLayout(levelsPanel, BoxLayout.Y_AXIS));
        levelsPanel.setOpaque(false);
        levelsPanel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JButton level1 = new JButton("LEVEL 1");
        level1.setBackground(Color.decode("#4CAF50"));
        level1.setForeground(Color.WHITE);
        level1.setFont(new Font("Arial", Font.BOLD, 24));
        level1.setFocusPainted(false);
        level1.setAlignmentX(Component.CENTER_ALIGNMENT);
        level1.setMaximumSize(new Dimension(250, 80));
        level1.addActionListener(e -> {
            selectedLevel = 1;
            showDifficultyForLevel(1);
        });

        JButton level2 = new JButton("LEVEL 2");
        level2.setBackground(Color.decode("#FF9800"));
        level2.setForeground(Color.WHITE);
        level2.setFont(new Font("Arial", Font.BOLD, 24));
        level2.setFocusPainted(false);
        level2.setAlignmentX(Component.CENTER_ALIGNMENT);
        level2.setMaximumSize(new Dimension(250, 80));
        level2.addActionListener(e -> {
            selectedLevel = 2;
            showDifficultyForLevel(2);
        });

        JButton level3 = new JButton("LEVEL 3");
        level3.setBackground(Color.decode("#F44336"));
        level3.setForeground(Color.WHITE);
        level3.setFont(new Font("Arial", Font.BOLD, 24));
        level3.setFocusPainted(false);
        level3.setAlignmentX(Component.CENTER_ALIGNMENT);
        level3.setMaximumSize(new Dimension(250, 80));
        level3.addActionListener(e -> {
            selectedLevel = 3;
            showDifficultyForLevel(3);
        });

        levelsPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        levelsPanel.add(level1);
        levelsPanel.add(Box.createRigidArea(new Dimension(0, 30)));
        levelsPanel.add(level2);
        levelsPanel.add(Box.createRigidArea(new Dimension(0, 30)));
        levelsPanel.add(level3);
        levelsPanel.add(Box.createRigidArea(new Dimension(0, 20)));

        fullPanel.add(levelsPanel, BorderLayout.CENTER);

        JPanel navPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 40, 20));
        navPanel.setOpaque(false);
        JButton backBtn = createStyledNavButton("← BACK", new Color(100, 100, 200),
                e -> cardLayout.show(mainPanel, "PLAYER_SELECT"));
        navPanel.add(backBtn);
        fullPanel.add(navPanel, BorderLayout.SOUTH);

        mainPanel.add(fullPanel, "SELECT_LEVEL");
        cardLayout.show(mainPanel, "SELECT_LEVEL");
    }

    // =========================================================================
    // 5. شاشة اختيار الصعوبة للمستوى (LEVEL_DIFFICULTY)
    // =========================================================================

    class DifficultyButton extends JButton {
        private final QuizGLEventListener.Difficulty level;
        private final ImageIcon before2, after1, after2;
        private boolean selected = false;

        public DifficultyButton(
                QuizGLEventListener.Difficulty level,
                ImageIcon before2,
                ImageIcon after1,
                ImageIcon after2
        ) {
            super(before2);
            this.level = level;
            this.before2 = before2;
            this.after1 = after1;
            this.after2 = after2;

            setBorderPainted(false);
            setContentAreaFilled(false);
            setFocusPainted(false);
            setOpaque(false);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    if (!selected) setIcon(after2);
                }
                @Override
                public void mouseExited(MouseEvent e) {
                    setIcon(selected ? after1 : before2);
                }
                @Override
                public void mouseClicked(MouseEvent e) { selectButton(); }
            });
        }

        public void selectButton() {
            if (selectedDifficultyButton != null && selectedDifficultyButton != this)
                selectedDifficultyButton.deselect();
            selected = true;
            selectedDifficultyButton = this;
            difficulty = level;
            setIcon(after1);
        }

        public void deselect() {
            selected = false;
            setIcon(before2);
        }
    }

    private void showDifficultyForLevel(int level) {
        JPanel panel = createBackgroundPanel(sharedBackground, new Color(0, 50, 100));
        panel.setLayout(new BorderLayout());

        JLabel titleLabel = new JLabel("LEVEL " + level + " - SELECT DIFFICULTY", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Serif", Font.BOLD, 32));
        titleLabel.setForeground(ACCENT_YELLOW);
        titleLabel.setBorder(BorderFactory.createEmptyBorder(50,0,100,0));
        panel.add(titleLabel, BorderLayout.NORTH);

        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 40, 50));
        buttonsPanel.setOpaque(false);

        DifficultyButton btnEasy = new DifficultyButton(QuizGLEventListener.Difficulty.EASY,
                easyBefore2, easyAfter1, easyAfter2);

        DifficultyButton btnMedium = new DifficultyButton(QuizGLEventListener.Difficulty.MEDIUM,
                mediumBefore2, mediumAfter1, mediumAfter2);

        DifficultyButton btnHard = new DifficultyButton(QuizGLEventListener.Difficulty.HARD,
                hardBefore2, hardAfter1, hardAfter2);

        // إعادة ضبط الاختيار
        btnEasy.deselect();
        btnMedium.deselect();
        btnHard.deselect();
        selectedDifficultyButton = null;
        difficulty = null;

        buttonsPanel.add(createDifficultyButtonContainer("EASY", Color.GREEN, btnEasy));
        buttonsPanel.add(createDifficultyButtonContainer("MEDIUM", Color.ORANGE, btnMedium));
        buttonsPanel.add(createDifficultyButtonContainer("HARD", Color.RED, btnHard));

        panel.add(buttonsPanel, BorderLayout.CENTER);

        JPanel navPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 40, 20));
        navPanel.setOpaque(false);

        JButton backBtn = createStyledNavButton("← BACK", new Color(100, 100, 200),
                e -> cardLayout.show(mainPanel, "SELECT_LEVEL"));

        JButton startBtn = createStyledNavButton("START GAME →", new Color(0, 150, 0), e -> {
            if (difficulty != null) {
                startGameWithLevelAndDifficulty(level, difficulty);
            }
        });

        startBtn.setEnabled(false);
        startBtn.setBackground(Color.GRAY.darker());

        // تحديث حالة زر البدء عند اختيار الصعوبة
        ActionListener difficultySelectListener = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                startBtn.setEnabled(true);
                startBtn.setBackground(new Color(0, 150, 0));
            }
        };

        btnEasy.addActionListener(difficultySelectListener);
        btnMedium.addActionListener(difficultySelectListener);
        btnHard.addActionListener(difficultySelectListener);

        navPanel.add(backBtn);
        navPanel.add(startBtn);
        panel.add(navPanel, BorderLayout.SOUTH);

        mainPanel.add(panel, "LEVEL_DIFFICULTY_" + level);
        cardLayout.show(mainPanel, "LEVEL_DIFFICULTY_" + level);
    }

    private JPanel createDifficultyButtonContainer(String text, Color color, DifficultyButton button) {
        JPanel container = new JPanel();
        container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));
        container.setOpaque(false);
        container.setPreferredSize(new Dimension(150, 150));

        JLabel label = new JLabel(text, SwingConstants.CENTER);
        label.setFont(new Font("Arial", Font.BOLD, 22));
        label.setForeground(color);
        label.setAlignmentX(Component.CENTER_ALIGNMENT);
        label.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));

        button.setAlignmentX(Component.CENTER_ALIGNMENT);

        container.add(label);
        container.add(button);
        return container;
    }

    // =========================================================================
    // 6. بدء اللعبة
    // =========================================================================

    private void startGameWithLevelAndDifficulty(int level, QuizGLEventListener.Difficulty difficulty) {
        dispose();

        QuizGLEventListener listener = new QuizGLEventListener(difficulty);
        listener.setPlayerCount(playerCount);
        listener.setLevel(level);
        listener.setDifficulty(difficulty);

        JFrame gameFrame = new JFrame("Feeding Frenzy - Level " + level + " - " + difficulty);
        GLCanvas canvas = new GLCanvas();
        canvas.addGLEventListener(listener);
        canvas.addKeyListener(listener);
        canvas.addMouseMotionListener(listener);
        canvas.setFocusable(true);

        gameFrame.getContentPane().add(canvas, BorderLayout.CENTER);

        FPSAnimator animator = new FPSAnimator(canvas, 15, true);
        animator.start();

        gameFrame.setSize(1000, 1000);
        gameFrame.setLocationRelativeTo(null);
        gameFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        gameFrame.setVisible(true);

        SwingUtilities.invokeLater(() -> canvas.requestFocusInWindow());
    }

    // =========================================================================
    // 7. الدوال المساعدة
    // =========================================================================

    private Color getDifficultyColor() {
        if (difficulty == null) return Color.WHITE;
        switch (difficulty) {
            case EASY: return Color.GREEN;
            case MEDIUM: return Color.ORANGE;
            case HARD: return Color.RED;
            default: return Color.WHITE;
        }
    }

    private JButton createStyledNavButton(String text, Color bgColor, ActionListener listener) {
        JButton button = new JButton(text);
        button.setFont(new Font("Arial", Font.BOLD, 18));
        button.setForeground(Color.WHITE);
        button.setBackground(bgColor);
        button.setBorder(BorderFactory.createEmptyBorder(12, 40, 12, 40));
        button.setFocusPainted(false);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.addActionListener(listener);

        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                if (button.isEnabled()) button.setBackground(bgColor.brighter());
            }
            @Override
            public void mouseExited(MouseEvent e) {
                if (button.isEnabled()) button.setBackground(bgColor);
            }
        });
        return button;
    }

    private JPanel createOptionsScreen() {
        JPanel panel = createBackgroundPanel(sharedBackground, new Color(0, 50, 100));

        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setOpaque(false);
        contentPanel.setBorder(BorderFactory.createEmptyBorder(80, 50, 50, 50));

        JLabel titleLabel = new JLabel("GAME OPTIONS", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 36));
        titleLabel.setForeground(ACCENT_YELLOW);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        contentPanel.add(titleLabel);
        contentPanel.add(Box.createRigidArea(new Dimension(0, 50)));

        JLabel opt1 = new JLabel("Sound Volume: 80%");
        opt1.setForeground(Color.WHITE); opt1.setFont(new Font("Arial", Font.PLAIN, 18));
        opt1.setAlignmentX(Component.CENTER_ALIGNMENT);
        contentPanel.add(opt1);
        contentPanel.add(Box.createRigidArea(new Dimension(0, 15)));

        JLabel opt2 = new JLabel("Music Volume: 50%");
        opt2.setForeground(Color.WHITE); opt2.setFont(new Font("Arial", Font.PLAIN, 18));
        opt2.setAlignmentX(Component.CENTER_ALIGNMENT);
        contentPanel.add(opt2);
        contentPanel.add(Box.createRigidArea(new Dimension(0, 50)));

        JButton backBtn = createStyledNavButton("← BACK TO MAIN MENU", PRIMARY_BLUE.darker(),
                e -> handleMainMenu("MAIN_MENU"));
        backBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        contentPanel.add(backBtn);

        panel.add(contentPanel);
        return panel;
    }

    private void exitGame() {
        int confirm = JOptionPane.showConfirmDialog(this,
                "هل أنت متأكد أنك تريد الخروج من لعبة Feeding Frenzy؟",
                "خروج من اللعبة",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            System.exit(0);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(FeedingFrenzyMenu::new);
    }
}