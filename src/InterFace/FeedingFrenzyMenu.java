package InterFace;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;

public class FeedingFrenzyMenu extends JFrame {

    // المتغيرات الخاصة بالصور واللوحات
    private ImageIcon newGameNormal, newGameHover;
    private ImageIcon gameOptionsNormal, gameOptionsHover;
    private ImageIcon exitNormal, exitHover;
    private ImageIcon player1Normal, player1Hover; // تم تغيير الأسماء
    private ImageIcon player2Normal, player2Hover; // تم تغيير الأسماء
    private ImageIcon menuIcon;
    private ImageIcon easyBefore2, easyAfter1, easyAfter2;
    private ImageIcon mediumBefore2, mediumAfter1, mediumAfter2;
    private ImageIcon hardBefore2, hardAfter1, hardAfter2;
    private Image menuBackground;
    private Image sharedBackground;

    private final JPanel mainPanel;
    private final CardLayout cardLayout;
    private int playerCount = 1;
    private String difficulty = null;
    private DifficultyButton selectedDifficultyButton = null;
    private JButton difficultyNextBtn;

    // متغيرات لتخزين اللوحات القابلة للتحديث لضمان التحديث الصحيح
    private JPanel playerSelectionPanel;
    private JPanel gameScreenPanel;

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
        mainPanel.add(createDifficultySelection(), "DIFFICULTY_SELECT");

        // إنشاء اللوحات القابلة للتحديث وتخزينها
        playerSelectionPanel = createPlayerSelection();
        mainPanel.add(playerSelectionPanel, "PLAYER_SELECT");

        gameScreenPanel = createGameScreen();
        mainPanel.add(gameScreenPanel, "GAME_SCREEN");

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
            menuBackground = ImageIO.read(new File("src/Menu background.png"));
            sharedBackground = ImageIO.read(new File("src/sharedBackground.png"));

            try {
                // الأبعاد الأصلية: 150, 150
                newGameNormal = resizeImageIcon(new ImageIcon("src/New Game 1.png"), 150, 150);
                newGameHover = resizeImageIcon(new ImageIcon("src/New Game 2.png"), 150, 150);
                gameOptionsNormal = resizeImageIcon(new ImageIcon("src/Game Options 1.png"), 150, 150);
                gameOptionsHover = resizeImageIcon(new ImageIcon("src/Game Options 2.png"), 150, 150);
                exitNormal = resizeImageIcon(new ImageIcon("src/Exit 1.png"), 150, 150);
                exitHover = resizeImageIcon(new ImageIcon("src/Exit 2.png"), 150, 150);
            } catch (Exception e) {
                System.out.println("Warning: Button images not found. Using default text icons.");
                createDefaultImages(); // يُنشئ الأيقونات النصية الافتراضية
                return;
            }

            // أيقونات اختيار اللاعبين - تم تغييرها لاستخدام الصور الجديدة
            try {
                // تحميل صور 1 Player
                player1Normal = resizeImageIcon(new ImageIcon("src/1 Player 1.png"), 120, 120);
                player1Hover = resizeImageIcon(new ImageIcon("src/1 Player 2.png"), 120, 120);

                // تحميل صور 2 Players
                player2Normal = resizeImageIcon(new ImageIcon("src/Multi Player 1.png"), 120, 120);
                player2Hover = resizeImageIcon(new ImageIcon("src/Multi Player 2.png"), 120, 120);
            } catch (Exception e) {
                System.out.println("Warning: Player images not found. Using default player icons.");
                // في حالة عدم وجود الصور، نستخدم الأيقونات الافتراضية
                player1Normal = createTextIcon("1 PLAYER", Color.CYAN, 120, 120);
                player1Hover = createTextIcon("1 PLAYER", ACCENT_YELLOW, 120, 120);
                player2Normal = createTextIcon("2 PLAYERS", Color.ORANGE, 120, 120);
                player2Hover = createTextIcon("2 PLAYERS", ACCENT_YELLOW, 120, 120);
            }

            menuIcon = resizeImageIcon(new ImageIcon("src/icon.png"), 400, 400);

            easyBefore2 = resizeImageIcon(new ImageIcon("src/before 2.png"), 120, 50);
            easyAfter1 = resizeImageIcon(new ImageIcon("src/after 1.png"), 120, 50);
            easyAfter2 = resizeImageIcon(new ImageIcon("src/after 2.png"), 120, 50);
            mediumBefore2 = resizeImageIcon(new ImageIcon("src/before 2.png"), 120, 50);
            mediumAfter1 = resizeImageIcon(new ImageIcon("src/after 1.png"), 120, 50);
            mediumAfter2 = resizeImageIcon(new ImageIcon("src/after 2.png"), 120, 50);
            hardBefore2 = resizeImageIcon(new ImageIcon("src/before 2.png"), 120, 50);
            hardAfter1 = resizeImageIcon(new ImageIcon("src/after 1.png"), 120, 50);
            hardAfter2 = resizeImageIcon(new ImageIcon("src/after 2.png"), 120, 50);

        } catch (Exception e) {
            System.out.println("Error loading images: " + e.getMessage());
            menuBackground = null;
            sharedBackground = null;
            createDefaultImages();
        }
    }

    // دالة مساعدة لتغيير حجم الأيقونة (بدون تغيير)
    private ImageIcon resizeImageIcon(ImageIcon icon, int width, int height) {
        if (icon.getImage() == null) return icon;
        Image img = icon.getImage();
        Image resizedImg = img.getScaledInstance(width, height, Image.SCALE_SMOOTH);
        return new ImageIcon(resizedImg);
    }

    // إنشاء الأيقونات النصية الافتراضية (بدون تغيير)
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

    // دالة مساعدة لإنشاء أيقونة نصية (بدون تغيير)
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

    // دالة مساعدة لإنشاء أيقونة سمكة افتراضية (Fish Icon) (بدون تغيير)
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

    // دالة مساعدة لإنشاء لوحة خلفية (بدون تغيير)
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

        // لوحة الأزرار في الجزء السفلي (أفقية)
        JPanel buttonContainer = new JPanel();
        buttonContainer.setLayout(new FlowLayout(FlowLayout.CENTER, 30, 20)); // FlowLayout أفقي مع مسافة 30
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

    // MenuButton (بدون تغيير)
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
                difficulty = null; // نصفر الصعوبة
                selectedDifficultyButton = null; // نصفر الزر المحدد
                enableDifficultyNext(false);
                // نعيد إنشاء لوحة اختيار الصعوبة من جديد
                mainPanel.remove(playerSelectionPanel); // تأكد من إزالة أي لوحات
                playerSelectionPanel = createPlayerSelection();
                mainPanel.add(playerSelectionPanel, "PLAYER_SELECT");

                // إنشاء لوحة اختيار الصعوبة جديدة
                mainPanel.remove(mainPanel.getComponent(1)); // افترض أن index 1 هي DIFFICULTY_SELECT
                mainPanel.add(createDifficultySelection(), "DIFFICULTY_SELECT");
                cardLayout.show(mainPanel, "DIFFICULTY_SELECT");
                break;

            case "MAIN_MENU":
                difficulty = null;
                selectedDifficultyButton = null;
                cardLayout.show(mainPanel, "MAIN_MENU");
                break;

            case "OPTIONS":
                cardLayout.show(mainPanel, "OPTIONS_SCREEN");
                break;

            case "EXIT":
                exitGame();
                break;
        }
    }


    // =========================================================================
    // 3. شاشة اختيار الصعوبة (DIFFICULTY_SELECT)
    // =========================================================================

    // DifficultyButton (بدون تغيير)
    class DifficultyButton extends JButton {
        private final String level;
        private final ImageIcon before2, after1, after2;
        private boolean selected = false;

        public DifficultyButton(String level, ImageIcon before2, ImageIcon after1, ImageIcon after2) {
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
                    if (!selected) setIcon(after2); // لو مش متحدد فقط
                }
                @Override
                public void mouseExited(MouseEvent e) {
                    setIcon(selected ? after1 : before2); // لو متحدد يبقى after1
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
            difficulty = level; // تعيين مستوى الصعوبة
            setIcon(after1); // تعرض الأيقونة المختارة فقط
            enableDifficultyNext(true);
        }

        public void deselect() {
            selected = false;
            setIcon(before2);
        }
    }


    // دالة للتحكم في تفعيل زر NEXT (بدون تغيير)
    private void enableDifficultyNext(boolean enable) {
        if (difficultyNextBtn != null) {
            difficultyNextBtn.setEnabled(enable);
            if (enable) {
                difficultyNextBtn.setBackground(new Color(0, 180, 0));
                difficultyNextBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            } else {
                difficultyNextBtn.setBackground(Color.GRAY.darker());
                difficultyNextBtn.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
            }
        }
    }

    private JPanel createDifficultySelection() {
        JPanel panel = createBackgroundPanel(sharedBackground, new Color(0, 50, 100));
        panel.setLayout(new BorderLayout());

        JLabel titleLabel = new JLabel("SELECT DIFFICULTY", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 36));
        titleLabel.setForeground(ACCENT_YELLOW);
        titleLabel.setBorder(BorderFactory.createEmptyBorder(50,0,20,0));
        panel.add(titleLabel, BorderLayout.NORTH);

        // استخدام FlowLayout لتنظيم الأزرار أفقياً
        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 40, 20));
        buttonsPanel.setOpaque(false);

        DifficultyButton btnEasy = new DifficultyButton("EASY", easyBefore2, easyAfter1, easyAfter2);
        DifficultyButton btnMedium = new DifficultyButton("MEDIUM", mediumBefore2, mediumAfter1, mediumAfter2);
        DifficultyButton btnHard = new DifficultyButton("HARD", hardBefore2, hardAfter1, hardAfter2);

        // يتم التأكد من عدم تحديد أي زر إذا كان difficulty = null
        if (difficulty != null) {
            if ("EASY".equals(difficulty)) btnEasy.selectButton();
            else if ("MEDIUM".equals(difficulty)) btnMedium.selectButton();
            else if ("HARD".equals(difficulty)) btnHard.selectButton();
        }

        // لوحة لتجميع الزر والعنوان
        buttonsPanel.add(createDifficultyButtonContainer("EASY", Color.GREEN, btnEasy));
        buttonsPanel.add(createDifficultyButtonContainer("MEDIUM", Color.ORANGE, btnMedium));
        buttonsPanel.add(createDifficultyButtonContainer("HARD", Color.RED, btnHard));

        panel.add(buttonsPanel, BorderLayout.CENTER);

        JPanel navPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 40, 20));
        navPanel.setOpaque(false);

        difficultyNextBtn = createStyledNavButton("NEXT →", new Color(0, 150, 0), e -> {
            if (difficulty != null) {
                // إزالة اللوحة القديمة وإعادة بناء لوحة اللاعبين بقيمة difficulty المُحدثة
                mainPanel.remove(playerSelectionPanel);
                playerSelectionPanel = createPlayerSelection();
                mainPanel.add(playerSelectionPanel, "PLAYER_SELECT");

                cardLayout.show(mainPanel, "PLAYER_SELECT");
            }
        });

        enableDifficultyNext(difficulty != null);

        JButton backBtn = createStyledNavButton("← BACK", new Color(100, 100, 200), e -> handleMainMenu("MAIN_MENU"));

        navPanel.add(backBtn);
        navPanel.add(difficultyNextBtn);

        panel.add(navPanel, BorderLayout.SOUTH);

        return panel;
    }

    // دالة مساعدة لإنشاء حاوية زر الصعوبة مع العنوان
    private JPanel createDifficultyButtonContainer(String text, Color color, DifficultyButton button) {
        JPanel container = new JPanel();
        container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));
        container.setOpaque(false);
        container.setPreferredSize(new Dimension(150, 150)); // لتحديد حجم مناسب

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
    // 4. شاشة اختيار اللاعبين (PLAYER_SELECT)
    // =========================================================================

    private JPanel createPlayerSelection() {
        JPanel panel = createBackgroundPanel(sharedBackground, new Color(0, 50, 100));
        panel.setLayout(new BorderLayout());

        // إظهار الصعوبة باللون الصحيح
        String displayDifficulty = (difficulty != null) ? difficulty : "N/A";
        JLabel difficultyLabel = new JLabel("DIFFICULTY: " + displayDifficulty, SwingConstants.CENTER);
        difficultyLabel.setFont(new Font("Arial", Font.BOLD, 28));
        difficultyLabel.setForeground(getDifficultyColor());
        difficultyLabel.setBorder(BorderFactory.createEmptyBorder(50,0,20,0));
        panel.add(difficultyLabel, BorderLayout.NORTH);

        // لوحة لاختيار اللاعبين (لتنظيم الأزرار أفقياً وإضافة المسافة)
        JPanel playersPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 60, 50)); // مسافة أفقية 60
        playersPanel.setOpaque(false);

        // زر اللاعب الواحد مع تأثير Hover
        JButton btn1Player = new JButton(player1Normal);
        btn1Player.setBorderPainted(false);
        btn1Player.setContentAreaFilled(false);
        btn1Player.setFocusPainted(false);
        btn1Player.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn1Player.addActionListener(e -> { playerCount = 1; startGame(); });

        // إضافة تأثير Hover لزر اللاعب الواحد
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

        // زر اللاعبين مع تأثير Hover
        JButton btn2Players = new JButton(player2Normal);
        btn2Players.setBorderPainted(false);
        btn2Players.setContentAreaFilled(false);
        btn2Players.setFocusPainted(false);
        btn2Players.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn2Players.addActionListener(e -> { playerCount = 2; startGame(); });

        // إضافة تأثير Hover لزر اللاعبين
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
        playersPanel.add(btn2Players);

        panel.add(playersPanel, BorderLayout.CENTER);

        JPanel navPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 40, 20));
        navPanel.setOpaque(false);
        JButton backBtn = createStyledNavButton("← BACK", new Color(100, 100, 200), e -> cardLayout.show(mainPanel, "DIFFICULTY_SELECT"));
        navPanel.add(backBtn);
        panel.add(navPanel, BorderLayout.SOUTH);

        return panel;
    }

    private Color getDifficultyColor() {
        if (difficulty == null) return Color.WHITE;
        switch (difficulty) {
            case "EASY": return Color.GREEN;
            case "MEDIUM": return Color.ORANGE;
            case "HARD": return Color.RED;
            default: return Color.WHITE;
        }
    }

    // دالة لإنشاء زر تنقل موحد الشكل (بدون تغيير)
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

    // =========================================================================
    // 5. شاشة اللعب والخيارات (GAME_SCREEN, OPTIONS_SCREEN)
    // =========================================================================

    private JPanel createGameScreen() {
        JPanel panel = createBackgroundPanel(sharedBackground, new Color(0, 50, 100));
        panel.setLayout(null);

        JLabel gameLabel = new JLabel("GAME IN PROGRESS...", SwingConstants.CENTER);
        gameLabel.setFont(new Font("Arial", Font.BOLD, 40));
        gameLabel.setForeground(Color.WHITE);
        gameLabel.setBounds(200, 300, 400, 50);
        panel.add(gameLabel);

        // إضافة معلومات اللعبة
        JLabel infoLabel = new JLabel("Players: " + playerCount + " | Difficulty: " + (difficulty != null ? difficulty : "Not Set"), SwingConstants.CENTER);
        infoLabel.setFont(new Font("Arial", Font.PLAIN, 20));
        infoLabel.setForeground(ACCENT_YELLOW);
        infoLabel.setBounds(200, 370, 400, 30);
        panel.add(infoLabel);

        JButton pauseBtn = new JButton("PAUSE");
        pauseBtn.setFont(new Font("Arial", Font.BOLD, 14));
        pauseBtn.setBackground(new Color(255, 165, 0));
        pauseBtn.setForeground(Color.WHITE);
        pauseBtn.setBounds(650, 20, 100, 35);
        pauseBtn.addActionListener(e -> showPauseMenu());
        panel.add(pauseBtn);

        return panel;
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

        // خيارات وهمية
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

        // تغيير شكل زر العودة هنا
        JButton backBtn = createStyledNavButton("← BACK TO MAIN MENU", PRIMARY_BLUE.darker(), e -> handleMainMenu("MAIN_MENU"));
        backBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        contentPanel.add(backBtn);

        panel.add(contentPanel);
        return panel;
    }

    private void startGame() {
        System.out.println("Starting game with " + playerCount + " player(s), Difficulty: " + difficulty);

        // إزالة وإعادة إضافة لوحة اللعبة لضمان تحديث محتواها
        mainPanel.remove(gameScreenPanel);
        gameScreenPanel = createGameScreen();
        mainPanel.add(gameScreenPanel, "GAME_SCREEN");

        cardLayout.show(mainPanel, "GAME_SCREEN");
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

    // شاشة الإيقاف المؤقت (Pause Menu)
    private void showPauseMenu() {
        JDialog pauseDialog = new JDialog(this, "Paused", true);
        pauseDialog.setUndecorated(true);
        pauseDialog.setBackground(new Color(0, 0, 0, 180));
        pauseDialog.setSize(350, 400);
        pauseDialog.setLocationRelativeTo(this);

        JPanel menuPanel = new JPanel();
        menuPanel.setLayout(new BoxLayout(menuPanel, BoxLayout.Y_AXIS));
        menuPanel.setOpaque(false);
        menuPanel.setBorder(BorderFactory.createEmptyBorder(40, 40, 40, 40));

        JLabel title = new JLabel("GAME PAUSED", SwingConstants.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 32));
        title.setForeground(ACCENT_YELLOW);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        menuPanel.add(title);
        menuPanel.add(Box.createRigidArea(new Dimension(0, 40)));

        JButton resumeBtn = createStyledNavButton("RESUME", new Color(0, 150, 0), e -> {
            pauseDialog.dispose();
        });
        resumeBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        menuPanel.add(resumeBtn);
        menuPanel.add(Box.createRigidArea(new Dimension(0, 15)));

        JButton optionsBtn = createStyledNavButton("OPTIONS", PRIMARY_BLUE.darker(), e -> {
            pauseDialog.dispose();
            cardLayout.show(mainPanel, "OPTIONS_SCREEN");
        });
        optionsBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        menuPanel.add(optionsBtn);
        menuPanel.add(Box.createRigidArea(new Dimension(0, 15)));

        JButton menuBtn = createStyledNavButton("MAIN MENU", new Color(100, 100, 200), e -> {
            pauseDialog.dispose();
            // 🌟 التعديل: تصفير الصعوبة عند العودة إلى القائمة الرئيسية من الإيقاف المؤقت
            difficulty = null;
            selectedDifficultyButton = null;
            cardLayout.show(mainPanel, "MAIN_MENU");
        });
        menuBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        menuPanel.add(menuBtn);
        menuPanel.add(Box.createRigidArea(new Dimension(0, 15)));

        JButton exitBtn = createStyledNavButton("EXIT GAME", Color.RED.darker(), e -> exitGame());
        exitBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        menuPanel.add(exitBtn);

        pauseDialog.setContentPane(menuPanel);
        pauseDialog.setVisible(true);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(FeedingFrenzyMenu::new);
    }
}