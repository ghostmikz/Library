package view;

import i18n.I18n;
import i18n.LanguageListener;
import model.User;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class MainFrame extends JFrame implements LanguageListener {

    private final User   user;
    private final JPanel navPanel;
    private final JPanel content;
    private final CardLayout cards;
    private final Map<String, NavItem> navItems       = new LinkedHashMap<>();
    private final List<LanguageListener> panelListeners = new ArrayList<>();
    private final List<String>           tabKeys        = new ArrayList<>();
    private Runnable logoutListener;
    private String   activeKey;

    private JLabel  sidebarTitleLabel;
    private JLabel  userRoleLabel;
    private JButton settingsBtn;
    private JButton logoutBtn;

    public MainFrame(User user) {
        this.user     = user;
        this.cards    = new CardLayout();
        this.content  = new JPanel(cards);
        this.navPanel = new JPanel();
        navPanel.setLayout(new BoxLayout(navPanel, BoxLayout.Y_AXIS));
        navPanel.setOpaque(false);

        setTitle(I18n.t("app.title") + "  —  " + user.getFullName());
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1200, 750);
        setMinimumSize(new Dimension(900, 600));
        setLocationRelativeTo(null);
        setContentPane(buildUI());
        I18n.addListener(this);
    }

    @Override
    public void onLanguageChanged() {
        setTitle(I18n.t("app.title") + "  —  " + user.getFullName());
        sidebarTitleLabel.setText(I18n.t("app.title"));
        userRoleLabel.setText(roleText());
        settingsBtn.setText(I18n.t("nav.settings"));
        logoutBtn.setText(I18n.t("nav.logout"));
        navItems.forEach((k, v) -> v.setText(tabLabel(k)));
        panelListeners.forEach(LanguageListener::onLanguageChanged);
    }

    private JPanel buildUI() {
        JPanel root = new JPanel(new BorderLayout());
        content.setBackground(Theme.BG);
        root.add(buildSidebar(), BorderLayout.WEST);
        root.add(content, BorderLayout.CENTER);
        return root;
    }

    private JPanel buildSidebar() {
        JPanel sidebar = new JPanel(new BorderLayout());
        sidebar.setBackground(Theme.SIDEBAR_BG);
        sidebar.setPreferredSize(new Dimension(220, Integer.MAX_VALUE));

        sidebar.add(buildSidebarHeader(), BorderLayout.NORTH);
        sidebar.add(buildSidebarCenter(), BorderLayout.CENTER);
        sidebar.add(buildSidebarBottom(), BorderLayout.SOUTH);
        return sidebar;
    }

    private JPanel buildSidebarHeader() {
        JPanel h = new JPanel(new BorderLayout());
        h.setOpaque(false);
        h.setPreferredSize(new Dimension(220, 60));
        h.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(0x0D5F58)),
            new EmptyBorder(0, 20, 0, 20)));
        sidebarTitleLabel = new JLabel(I18n.t("app.title"));
        sidebarTitleLabel.setFont(Theme.bold(15));
        sidebarTitleLabel.setForeground(Color.WHITE);
        h.add(sidebarTitleLabel, BorderLayout.CENTER);
        return h;
    }

    private JPanel buildSidebarCenter() {
        JPanel mid = new JPanel();
        mid.setOpaque(false);
        mid.setLayout(new BoxLayout(mid, BoxLayout.Y_AXIS));
        mid.add(Box.createVerticalStrut(8));
        mid.add(navPanel);
        mid.add(Box.createVerticalGlue());
        return mid;
    }

    private JPanel buildSidebarBottom() {
        JPanel bot = new JPanel();
        bot.setOpaque(false);
        bot.setLayout(new BoxLayout(bot, BoxLayout.Y_AXIS));

        JSeparator sep = new JSeparator();
        sep.setForeground(new Color(0x1A6560));
        sep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        bot.add(sep);
        bot.add(Box.createVerticalStrut(12));

        JLabel nameLabel = new JLabel(user.getFullName());
        nameLabel.setFont(Theme.bold(13));
        nameLabel.setForeground(Color.WHITE);
        nameLabel.setBorder(new EmptyBorder(0, 20, 2, 20));
        nameLabel.setAlignmentX(LEFT_ALIGNMENT);
        nameLabel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 20));

        userRoleLabel = new JLabel(roleText());
        userRoleLabel.setFont(Theme.regular(11));
        userRoleLabel.setForeground(Theme.SIDEBAR_TEXT);
        userRoleLabel.setBorder(new EmptyBorder(0, 20, 8, 20));
        userRoleLabel.setAlignmentX(LEFT_ALIGNMENT);
        userRoleLabel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 16));

        settingsBtn = sidebarActionBtn(I18n.t("nav.settings"));
        settingsBtn.addActionListener(e -> new SettingsDialog(this).setVisible(true));

        logoutBtn = sidebarActionBtn(I18n.t("nav.logout"));
        logoutBtn.setForeground(new Color(0xFCA5A5));
        logoutBtn.addActionListener(e -> {
            if (JOptionPane.showConfirmDialog(this, I18n.t("nav.logout") + "?",
                    I18n.t("common.confirm"), JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION
                    && logoutListener != null)
                logoutListener.run();
        });

        bot.add(nameLabel);
        bot.add(userRoleLabel);
        bot.add(settingsBtn);
        bot.add(logoutBtn);
        bot.add(Box.createVerticalStrut(8));
        return bot;
    }

    private JButton sidebarActionBtn(String text) {
        JButton b = new JButton(text) {
            private boolean hov;
            { addMouseListener(new MouseAdapter() {
                public void mouseEntered(MouseEvent e) { hov = true;  repaint(); }
                public void mouseExited (MouseEvent e) { hov = false; repaint(); }
            }); }
            @Override protected void paintComponent(Graphics g) {
                if (hov) { g.setColor(Theme.SIDEBAR_ITEM_HOVER); g.fillRect(0, 0, getWidth(), getHeight()); }
                super.paintComponent(g);
            }
        };
        b.setFont(Theme.regular(13));
        b.setForeground(Theme.SIDEBAR_TEXT);
        b.setOpaque(false);
        b.setContentAreaFilled(false);
        b.setBorderPainted(false);
        b.setFocusPainted(false);
        b.setHorizontalAlignment(SwingConstants.LEFT);
        b.setBorder(new EmptyBorder(10, 20, 10, 20));
        b.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return b;
    }

    public void addPanel(JPanel panel, String key) {
        tabKeys.add(key);
        content.add(panel, key);

        NavItem item = new NavItem(tabLabel(key));
        navItems.put(key, item);
        item.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) { selectNav(key); }
        });
        navPanel.add(item);
        navPanel.revalidate();

        if (panel instanceof LanguageListener ll) panelListeners.add(ll);
        if (activeKey == null) selectNav(key);
    }

    private void selectNav(String key) {
        activeKey = key;
        cards.show(content, key);
        navItems.forEach((k, v) -> v.setSelected(k.equals(key)));
    }

    private String tabLabel(String key) {
        return switch (key) {
            case "DASHBOARD"  -> I18n.t("nav.dashboard");
            case "BOOKS"      -> I18n.t("nav.books");
            case "LIBRARIANS" -> I18n.t("nav.librarians");
            case "BORROWS"    -> I18n.t("nav.borrows");
            default -> key;
        };
    }

    private String roleText() {
        return "admin".equals(user.getRole()) ? I18n.t("users.role.admin") : I18n.t("users.role.librarian");
    }

    public void showPanel(String key) { selectNav(key); }
    public void setLogoutListener(Runnable r) { this.logoutListener = r; }
    public void close() { I18n.removeListener(this); dispose(); }

    public static void modernScrollBar(JScrollPane sp) { Theme.modernScrollBar(sp); }

    // Sidebar nav item
    private static final class NavItem extends JPanel {
        private boolean selected;
        private boolean hovered;
        private final JLabel label;

        NavItem(String text) {
            setLayout(new BorderLayout());
            setOpaque(false);
            setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
            setPreferredSize(new Dimension(220, 44));

            label = new JLabel(text);
            label.setFont(Theme.regular(14));
            label.setForeground(new Color(0xCCFBF1));
            label.setBorder(new EmptyBorder(0, 24, 0, 24));
            add(label, BorderLayout.CENTER);

            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            addMouseListener(new MouseAdapter() {
                @Override public void mouseEntered(MouseEvent e) { hovered = true;  repaint(); }
                @Override public void mouseExited (MouseEvent e) { hovered = false; repaint(); }
            });
        }

        void setSelected(boolean s) {
            selected = s;
            label.setFont(s ? Theme.bold(14) : Theme.regular(14));
            label.setForeground(s ? Color.WHITE : new Color(0xCCFBF1));
            repaint();
        }

        void setText(String t) { label.setText(t); }

        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            if (selected) {
                // Inset pill — not full-width, clearly distinct from flat-rect POS style
                g2.setColor(Theme.SIDEBAR_ITEM_ACTIVE);
                g2.fillRoundRect(8, 4, getWidth() - 16, getHeight() - 8, 10, 10);
                // Accent dot on right edge
                g2.setColor(Theme.SIDEBAR_ACCENT);
                g2.fillOval(getWidth() - 20, getHeight() / 2 - 3, 6, 6);
            } else if (hovered) {
                g2.setColor(new Color(255, 255, 255, 18));
                g2.fillRoundRect(8, 4, getWidth() - 16, getHeight() - 8, 10, 10);
            }
            g2.dispose();
            super.paintComponent(g);
        }
    }
}
