package Texture;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;

import com.sun.opengl.util.FPSAnimator;
import javax.media.opengl.GLCanvas;

public class FeedingFrenzyMenu extends JFrame {

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
    private FeedingFrenzy.Difficulty difficulty;
    private DifficultyButton selectedDifficultyButton = null;

    private JPanel playerSelectionPanel;

    private final Color PRIMARY_BLUE = new Color(0, 100, 150);
    private final Color HOVER_CYAN = new Color(0, 150, 200);
    private final Color ACCENT_YELLOW = new Color(255, 200, 0);

    private AudioManager audioManager;
    private static final String SOUNDS_PATH = System.getProperty("user.dir") + "\\Assets\\sounds\\";

    private static FeedingFrenzyMenu instance;

    public FeedingFrenzyMenu() {
        setTitle("Feeding Frenzy");
        setSize(800, 800);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);

        loadImages();
        initializeAudio();

        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);

        mainPanel.add(createMainMenu(), "MAIN_MENU");

        playerSelectionPanel = createPlayerSelection();
        mainPanel.add(playerSelectionPanel, "PLAYER_SELECT");

        mainPanel.add(createOptionsScreen(), "OPTIONS_SCREEN");

        add(mainPanel);
        cardLayout.show(mainPanel, "MAIN_MENU");

        setVisible(true);
        instance = this;
    }

    public static void relaunchMenu(JFrame previousFrame) {
        if (instance != null) {
            if (previousFrame != null) previousFrame.dispose();
            instance.setVisible(true);
            instance.audioManager.playMenuBackgroundMusic();
            instance.handleMainMenu("MAIN_MENU");
        } else {
            if (previousFrame != null) previousFrame.dispose();
            SwingUtilities.invokeLater(FeedingFrenzyMenu::new);
        }
    }

    private void initializeAudio() {
        audioManager = new AudioManager();
        try {
            String menuMusicPath = SOUNDS_PATH + "game-background.wav";
            if (new File(menuMusicPath).exists()) {
                audioManager.loadSound("menu_background", menuMusicPath);
            } else {
                audioManager.ensureSoundLoaded("background", "background .wav");
            }

            String gameMusicPath = SOUNDS_PATH + "background.wav";
            if (new File(gameMusicPath).exists()) {
                audioManager.loadSound("game_music", gameMusicPath);
            } else {
                audioManager.ensureSoundLoaded("background", "background .wav");
            }

            String buttonSoundPath = SOUNDS_PATH + "button-click.wav";
            if (new File(buttonSoundPath).exists()) {
                audioManager.loadSound("button_click", buttonSoundPath);
            } else {
                audioManager.ensureSoundLoaded("zap", "zapsplat_cartoon.wav");
            }
            audioManager.playMenuBackgroundMusic();

        } catch (Exception e) {
            System.out.println("Audio initialization error: " + e.getMessage());
        }
    }

    private void playButtonClickSound() {
        if (audioManager != null) audioManager.playButtonClick();
    }

    private void playSelectionSound() {
        if (audioManager != null) audioManager.playSpecialSound("bubble");
    }

    private void stopMenuMusic() {
        if (audioManager != null) audioManager.stopBackgroundMusic();
    }

    private void loadImages() {
        try {
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
                createDefaultImages();
            }

            try {
                player1Normal = resizeImageIcon(new ImageIcon("src/InterFace/1 Player 1.png"), 150, 150);
                player1Hover = resizeImageIcon(new ImageIcon("src/InterFace/1 Player 2.png"), 150, 150);
                player2Normal = resizeImageIcon(new ImageIcon("src/InterFace/Multi Player 1.png"), 150, 150);
                player2Hover = resizeImageIcon(new ImageIcon("src/InterFace/Multi Player 2.png"), 150, 150);
            } catch (Exception e) {
                player1Normal = createTextIcon("1 PLAYER", Color.CYAN, 150, 150);
                player1Hover = createTextIcon("1 PLAYER", ACCENT_YELLOW, 150, 150);
                player2Normal = createTextIcon("2 PLAYERS", Color.ORANGE, 150, 150);
                player2Hover = createTextIcon("2 PLAYERS", ACCENT_YELLOW, 150, 150);
            }

            try {
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
                createDefaultImages();
            }

        } catch (Exception e) {
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

    // 🌟🌟 الإضافة لحل مشكلة الـ Error 🌟🌟
    private void createDefaultImages() {
        int size = 150;

        newGameNormal = createTextIcon("NEW GAME", PRIMARY_BLUE, size, size);
        newGameHover = createTextIcon("NEW GAME", ACCENT_YELLOW, size, size);

        gameOptionsNormal = createTextIcon("OPTIONS", PRIMARY_BLUE, size, size);
        gameOptionsHover = createTextIcon("OPTIONS", ACCENT_YELLOW, size, size);

        exitNormal = createTextIcon("EXIT", Color.RED, size, size);
        exitHover = createTextIcon("EXIT", ACCENT_YELLOW, size, size);

        player1Normal = createTextIcon("1 PLAYER", Color.CYAN, size, size);
        player1Hover = createTextIcon("1 PLAYER", ACCENT_YELLOW, size, size);
        player2Normal = createTextIcon("2 PLAYERS", Color.ORANGE, size, size);
        player2Hover = createTextIcon("2 PLAYERS", ACCENT_YELLOW, size, size);

        // أيقونة افتراضية للقائمة (سمكة)
        menuIcon = createFishIcon(400, 400);

        // أيقونات افتراضية لدرجات الصعوبة
        ImageIcon defaultDiffIcon = createTextIcon("LEVEL", Color.WHITE, 120, 50);
        easyBefore2 = easyAfter1 = easyAfter2 = defaultDiffIcon;
        mediumBefore2 = mediumAfter1 = mediumAfter2 = defaultDiffIcon;
        hardBefore2 = hardAfter1 = hardAfter2 = defaultDiffIcon;
    }
    // 🌟🌟 نهاية الإضافة 🌟🌟

    private ImageIcon createTextIcon(String text, Color color, int width, int height) {
        BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = img.createGraphics();
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
        g2d.setColor(Color.ORANGE);
        g2d.fillOval(20, 35, width * 60 / 100, height * 30 / 100);
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

        MenuButton btnNewGame = new MenuButton(newGameNormal, newGameHover, "NEW_GAME", audioManager);
        btnNewGame.addActionListener(e -> {
            playButtonClickSound();
            handleMainMenu("NEW_GAME");
        });

        MenuButton btnOptions = new MenuButton(gameOptionsNormal, gameOptionsHover, "OPTIONS", audioManager);
        btnOptions.addActionListener(e -> {
            playButtonClickSound();
            handleMainMenu("OPTIONS");
        });

        MenuButton btnExit = new MenuButton(exitNormal, exitHover, "EXIT", audioManager);
        btnExit.addActionListener(e -> {
            playButtonClickSound();
            handleMainMenu("EXIT");
        });

        buttonContainer.add(btnNewGame);
        buttonContainer.add(btnOptions);
        buttonContainer.add(btnExit);

        panel.add(buttonContainer, BorderLayout.CENTER);
        return panel;
    }

    static class MenuButton extends JButton {
        private final ImageIcon normalIcon;
        private final ImageIcon hoverIcon;
        private AudioManager audioManager;

        public MenuButton(ImageIcon normal, ImageIcon hover, String action, AudioManager audioManager) {
            super(normal);
            this.normalIcon = normal;
            this.hoverIcon = hover;
            this.audioManager = audioManager;
            setActionCommand(action);
            setBorderPainted(false);
            setContentAreaFilled(false);
            setFocusPainted(false);
            setOpaque(false);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    setIcon(hoverIcon);
                    if (audioManager != null) audioManager.playSpecialSound("bubble");
                }
                @Override
                public void mouseExited(MouseEvent e) { setIcon(normalIcon); }
            });
        }
    }

    private void handleMainMenu(String command) {
        switch (command) {
            case "NEW_GAME":
                difficulty = null;
                playerCount = 1;
                selectedDifficultyButton = null;
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
                audioManager.playMenuBackgroundMusic();
                cardLayout.show(mainPanel, "MAIN_MENU");
                break;
        }
    }

    private JPanel createPlayerSelection() {
        JPanel panel = createBackgroundPanel(sharedBackground, new Color(0, 50, 100));
        panel.setLayout(new BorderLayout());

        JLabel titleLabel = new JLabel("SELECT NUMBER OF PLAYERS", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 36));
        titleLabel.setForeground(ACCENT_YELLOW);
        titleLabel.setBorder(BorderFactory.createEmptyBorder(50,0,50,0));
        panel.add(titleLabel, BorderLayout.NORTH);

        JPanel playersPanel = new JPanel(new GridBagLayout());
        playersPanel.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(10, 0, 10, 0);

        JButton btn1Player = createPlayerButton("SINGLE PLAYER", "Challenge yourself", player1Normal, player1Hover);
        btn1Player.addActionListener(e -> {
            playButtonClickSound();
            playerCount = 1;
            showDifficultyForLevel(1);
        });

        JButton btn2Players = createPlayerButton("MULTI PLAYER", "Play with a friend", player2Normal, player2Hover);
        btn2Players.addActionListener(e -> {
            playButtonClickSound();
            playerCount = 2;
            showDifficultyForLevel(1);
        });

        playersPanel.add(btn1Player, gbc);
        playersPanel.add(Box.createRigidArea(new Dimension(0, 30)), gbc);
        playersPanel.add(btn2Players, gbc);

        panel.add(playersPanel, BorderLayout.CENTER);

        JPanel navPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 40, 20));
        navPanel.setOpaque(false);
        JButton backBtn = createStyledNavButton("← BACK", new Color(100, 100, 200), e -> handleMainMenu("MAIN_MENU"));
        navPanel.add(backBtn);
        panel.add(navPanel, BorderLayout.SOUTH);

        return panel;
    }

    private JButton createPlayerButton(String title, String description, ImageIcon normalIcon, ImageIcon hoverIcon) {
        JPanel buttonPanel = new JPanel(new BorderLayout(10, 5));
        buttonPanel.setOpaque(false);
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));
        buttonPanel.setBackground(new Color(0, 0, 0, 100));

        JLabel iconLabel = new JLabel(normalIcon);
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titleLabel.setForeground(Color.WHITE);

        JPanel textPanel = new JPanel(new BorderLayout());
        textPanel.setOpaque(false);
        textPanel.add(titleLabel, BorderLayout.CENTER);

        buttonPanel.add(iconLabel, BorderLayout.WEST);
        buttonPanel.add(textPanel, BorderLayout.CENTER);

        JButton button = new JButton() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setColor(new Color(0, 0, 0, 100));
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
                g2d.dispose();
                super.paintComponent(g);
            }
        };

        button.setLayout(new BorderLayout());
        button.add(buttonPanel);
        button.setBorderPainted(false);
        button.setContentAreaFilled(false);
        button.setFocusPainted(false);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                playSelectionSound();
                iconLabel.setIcon(hoverIcon);
                buttonPanel.setBackground(new Color(255, 255, 255, 50));
            }
            @Override
            public void mouseExited(MouseEvent e) {
                iconLabel.setIcon(normalIcon);
                buttonPanel.setBackground(new Color(0, 0, 0, 100));
                buttonPanel.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));
            }
        });
        return button;
    }

    class DifficultyButton extends JButton {
        private final FeedingFrenzy.Difficulty level;
        private final ImageIcon before2, after1, after2;
        private boolean selected = false;

        public DifficultyButton(FeedingFrenzy.Difficulty level, ImageIcon before2, ImageIcon after1, ImageIcon after2) {
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
                public void mouseEntered(MouseEvent e) { if (!selected) setIcon(after2); }
                @Override
                public void mouseExited(MouseEvent e) { setIcon(selected ? after1 : before2); }
                @Override
                public void mouseClicked(MouseEvent e) {
                    playButtonClickSound();
                    selectButton();
                }
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

        JLabel titleLabel = new JLabel("SELECT DIFFICULTY", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Serif", Font.BOLD, 32));
        titleLabel.setForeground(ACCENT_YELLOW);
        titleLabel.setBorder(BorderFactory.createEmptyBorder(50,0,100,0));
        panel.add(titleLabel, BorderLayout.NORTH);

        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 40, 50));
        buttonsPanel.setOpaque(false);

        DifficultyButton btnEasy = new DifficultyButton(FeedingFrenzy.Difficulty.EASY, easyBefore2, easyAfter1, easyAfter2);
        DifficultyButton btnMedium = new DifficultyButton(FeedingFrenzy.Difficulty.MEDIUM, mediumBefore2, mediumAfter1, mediumAfter2);
        DifficultyButton btnHard = new DifficultyButton(FeedingFrenzy.Difficulty.HARD, hardBefore2, hardAfter1, hardAfter2);

        btnEasy.deselect(); btnMedium.deselect(); btnHard.deselect();
        selectedDifficultyButton = null;
        difficulty = null;

        buttonsPanel.add(createDifficultyButtonContainer("EASY", Color.GREEN, btnEasy));
        buttonsPanel.add(createDifficultyButtonContainer("MEDIUM", Color.ORANGE, btnMedium));
        buttonsPanel.add(createDifficultyButtonContainer("HARD", Color.RED, btnHard));

        panel.add(buttonsPanel, BorderLayout.CENTER);

        JPanel navPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 40, 20));
        navPanel.setOpaque(false);

        JButton backBtn = createStyledNavButton("← BACK", new Color(100, 100, 200),
                e -> cardLayout.show(mainPanel, "PLAYER_SELECT"));

        JButton startBtn = createStyledNavButton("START GAME →", new Color(0, 150, 0), e -> {
            if (difficulty != null) {
                startGameWithLevelAndDifficulty(level, difficulty);
            }
        });
        startBtn.setEnabled(false);
        startBtn.setBackground(Color.GRAY.darker());

        ActionListener difficultySelectListener = e -> {
            startBtn.setEnabled(true);
            startBtn.setBackground(new Color(0, 150, 0));
        };
        btnEasy.addActionListener(difficultySelectListener);
        btnMedium.addActionListener(difficultySelectListener);
        btnHard.addActionListener(difficultySelectListener);

        navPanel.add(backBtn);
        navPanel.add(startBtn);
        panel.add(navPanel, BorderLayout.SOUTH);

        mainPanel.add(panel, "LEVEL_DIFFICULTY");
        cardLayout.show(mainPanel, "LEVEL_DIFFICULTY");
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

    private void startGameWithLevelAndDifficulty(int level, FeedingFrenzy.Difficulty difficulty) {
        stopMenuMusic();
        dispose();

        FeedingFrenzy listener = new FeedingFrenzy(difficulty, this);
        listener.setPlayerCount(playerCount);
        listener.setLevel(level);
        listener.setDifficulty(difficulty);
        if (audioManager != null) listener.setAudioManager(audioManager);

        SwingUtilities.invokeLater(() -> {
            if (audioManager != null) audioManager.playGameBackgroundMusic();
        });

        JFrame gameFrame = new JFrame("Feeding Frenzy");
        GLCanvas canvas = new GLCanvas();
        canvas.addGLEventListener(listener);
        canvas.addKeyListener(listener);
        canvas.addMouseMotionListener(listener);
        canvas.setFocusable(true);
        gameFrame.getContentPane().add(canvas, BorderLayout.CENTER);

        FPSAnimator animator = new FPSAnimator(canvas, 15, true);
        listener.setAnimator(animator);
        listener.setGameFrame(gameFrame);
        animator.start();
        gameFrame.setSize(1000, 1000);
        gameFrame.setLocationRelativeTo(null);
        gameFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        gameFrame.setVisible(true);
        SwingUtilities.invokeLater(canvas::requestFocusInWindow);
    }

    private JButton createStyledNavButton(String text, Color bgColor, ActionListener listener) {
        JButton button = new JButton(text);
        button.setFont(new Font("Arial", Font.BOLD, 18));
        button.setForeground(Color.WHITE);
        button.setBackground(bgColor);
        button.setBorder(BorderFactory.createEmptyBorder(12, 40, 12, 40));
        button.setFocusPainted(false);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        button.addActionListener(e -> {
            playButtonClickSound();
            listener.actionPerformed(e);
        });

        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) { if (button.isEnabled()) button.setBackground(bgColor.brighter()); }
            @Override
            public void mouseExited(MouseEvent e) { if (button.isEnabled()) button.setBackground(bgColor); }
        });
        return button;
    }

    private JPanel createOptionsScreen() {
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

        JPanel soundControlPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        soundControlPanel.setOpaque(false);
        JLabel soundLabel = new JLabel("🔊 Sound: ");
        soundLabel.setForeground(Color.WHITE);
        soundLabel.setFont(new Font("Arial", Font.BOLD, 20));
        soundControlPanel.add(soundLabel);

        JToggleButton soundToggle = new JToggleButton("ON", true) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                if (isSelected()) {
                    g2d.setColor(new Color(0, 200, 0));
                } else {
                    g2d.setColor(Color.RED);
                }
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
                g2d.setColor(Color.WHITE);
                g2d.setFont(getFont());
                FontMetrics fm = g2d.getFontMetrics();
                int x = (getWidth() - fm.stringWidth(getText())) / 2;
                int y = (getHeight() - fm.getHeight()) / 2 + fm.getAscent();
                g2d.drawString(getText(), x, y);
                g2d.dispose();
            }
        };
        soundToggle.setFont(new Font("Arial", Font.BOLD, 16));
        soundToggle.setFocusPainted(false);
        soundToggle.setOpaque(false);
        soundToggle.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        soundToggle.addActionListener(e -> {
            playButtonClickSound();
            boolean enabled = soundToggle.isSelected();
            soundToggle.setText(enabled ? "ON" : "OFF");
            if (audioManager != null) audioManager.toggleMute();
        });
        soundControlPanel.add(soundToggle);
        contentPanel.add(soundControlPanel);
        contentPanel.add(Box.createRigidArea(new Dimension(0, 20)));

        JPanel volumePanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        volumePanel.setOpaque(false);
        JLabel volumeLabel = new JLabel("📢 Volume: ");
        volumeLabel.setForeground(Color.WHITE);
        volumeLabel.setFont(new Font("Arial", Font.BOLD, 20));
        volumePanel.add(volumeLabel);

        JSlider volumeSlider = new JSlider(0, 100, 70);
        volumeSlider.setPreferredSize(new Dimension(200, 40));
        volumeSlider.setPaintTicks(false);
        volumeSlider.setPaintLabels(false);

        volumeSlider.setUI(new javax.swing.plaf.basic.BasicSliderUI(volumeSlider) {
            @Override
            public void paintTrack(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setColor(new Color(200, 200, 200));
                g2d.fillRoundRect(trackRect.x, trackRect.y + trackRect.height / 2 - 5, trackRect.width, 10, 5, 5);
                g2d.dispose();
            }
            @Override
            public void paintThumb(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setColor(new Color(0, 200, 0));
                g2d.fillOval(thumbRect.x, thumbRect.y, thumbRect.width, thumbRect.height);
                g2d.dispose();
            }
        });

        volumeSlider.addChangeListener(e -> {
            if (!volumeSlider.getValueIsAdjusting() && audioManager != null) {
                float volume = volumeSlider.getValue() / 100.0f;
                audioManager.setVolume(volume);
                playButtonClickSound();
            }
        });
        volumePanel.add(volumeSlider);
        contentPanel.add(volumePanel);
        contentPanel.add(Box.createRigidArea(new Dimension(0, 30)));

        JButton howToPlayBtn = createStyledNavButton("HOW TO PLAY", new Color(255, 140, 0), e -> {
            showHowToPlay();
        });
        howToPlayBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        contentPanel.add(howToPlayBtn);
        contentPanel.add(Box.createRigidArea(new Dimension(0, 50)));

        JButton backBtn = createStyledNavButton("← BACK TO MAIN MENU", PRIMARY_BLUE.darker(),
                e -> handleMainMenu("MAIN_MENU"));
        backBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        contentPanel.add(backBtn);

        JPanel panel = createBackgroundPanel(sharedBackground, new Color(0, 50, 100));
        panel.add(contentPanel);
        return panel;
    }

    private void showHowToPlay() {
        JPanel howToPlayPanel = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                try {
                    ImageIcon icon = new ImageIcon("src/InterFace/how to play.png");
                    Image img = icon.getImage();
                    g.drawImage(img, 0, 0, getWidth(), getHeight(), this);
                } catch (Exception e) {
                    g.setColor(new Color(0, 50, 100));
                    g.fillRect(0, 0, getWidth(), getHeight());
                }
            }
        };
        howToPlayPanel.setOpaque(true);

        JButton backBtn = createStyledNavButton("← BACK", PRIMARY_BLUE.darker(),
                e -> cardLayout.show(mainPanel, "OPTIONS_SCREEN"));
        JPanel navPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 40, 20));
        navPanel.setOpaque(false);
        navPanel.add(backBtn);

        howToPlayPanel.add(navPanel, BorderLayout.SOUTH);

        mainPanel.add(howToPlayPanel, "HOW_TO_PLAY");
        cardLayout.show(mainPanel, "HOW_TO_PLAY");
    }


    private void exitGame() {
        int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to exit?", "Exit Game", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        if (confirm == JOptionPane.YES_OPTION) System.exit(0);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(FeedingFrenzyMenu::new);
    }
}