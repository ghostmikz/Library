package view;

import i18n.I18n;
import i18n.LanguageListener;
import model.User;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.basic.BasicScrollBarUI;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class MainFrame extends JFrame implements LanguageListener {

    private final User user;
    private JTabbedPane tabs;
    private final List<LanguageListener> panelListeners = new ArrayList<>();
    private final List<String>           tabKeys        = new ArrayList<>();
    private Runnable logoutListener;

    private JLabel  statusLabel;
    private JButton settingsBtn;
    private JButton logoutBtn;

    public MainFrame(User user) {
        this.user = user;
        setTitle(I18n.t("app.title") + "  —  " + user.getFullName());
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1200, 750);
        setLocationRelativeTo(null);
        setContentPane(buildUI());
        I18n.addListener(this);
    }

    @Override
    public void onLanguageChanged() {
        setTitle(I18n.t("app.title") + "  —  " + user.getFullName());
        statusLabel.setText(user.getFullName() + "  ·  " +
                ("admin".equals(user.getRole()) ? I18n.t("users.role.admin") : I18n.t("users.role.librarian")));
        settingsBtn.setText(I18n.t("nav.settings"));
        logoutBtn.setText(I18n.t("nav.logout"));
        for (int i = 0; i < tabKeys.size(); i++) tabs.setTitleAt(i, tabLabel(tabKeys.get(i)));
        for (LanguageListener l : panelListeners) l.onLanguageChanged();
    }

    private JPanel buildUI() {
        JPanel root = new JPanel(new BorderLayout());

        tabs = new JTabbedPane();
        tabs.setFont(new Font("Dialog", Font.PLAIN, 13));

        JPanel bottom = new JPanel(new BorderLayout());
        bottom.setBorder(new EmptyBorder(4, 8, 4, 8));

        statusLabel = new JLabel(user.getFullName() + "  ·  " +
                ("admin".equals(user.getRole()) ? I18n.t("users.role.admin") : I18n.t("users.role.librarian")));
        statusLabel.setFont(new Font("Dialog", Font.PLAIN, 12));
        statusLabel.setForeground(new Color(0x6B7280));

        settingsBtn = new JButton(I18n.t("nav.settings"));
        settingsBtn.setFocusPainted(false);
        settingsBtn.addActionListener(e -> new SettingsDialog(this).setVisible(true));

        logoutBtn = new JButton(I18n.t("nav.logout"));
        logoutBtn.setFocusPainted(false);
        logoutBtn.addActionListener(e -> {
            if (JOptionPane.showConfirmDialog(this, I18n.t("nav.logout") + "?",
                    I18n.t("common.confirm"), JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION
                    && logoutListener != null)
                logoutListener.run();
        });

        JPanel rightBtns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        rightBtns.add(settingsBtn);
        rightBtns.add(logoutBtn);

        bottom.add(statusLabel, BorderLayout.WEST);
        bottom.add(rightBtns,   BorderLayout.EAST);

        root.add(tabs,   BorderLayout.CENTER);
        root.add(bottom, BorderLayout.SOUTH);
        return root;
    }

    public void addPanel(JPanel panel, String key) {
        tabs.addTab(tabLabel(key), panel);
        tabKeys.add(key);
        if (panel instanceof LanguageListener ll) panelListeners.add(ll);
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

    public void showPanel(String key) {
        // tabs show in add order; no explicit switch needed
    }

    public void setLogoutListener(Runnable r) { this.logoutListener = r; }

    public void close() {
        I18n.removeListener(this);
        dispose();
    }

    public static void modernScrollBar(JScrollPane sp) {
        JScrollBar sb = sp.getVerticalScrollBar();
        sb.setPreferredSize(new Dimension(6, 0));
        sb.setUI(new BasicScrollBarUI() {
            @Override protected void configureScrollBarColors() {
                thumbColor = new Color(0xCBD5E1); trackColor = Color.WHITE;
            }
            @Override protected JButton createDecreaseButton(int o) { return tiny(); }
            @Override protected JButton createIncreaseButton(int o) { return tiny(); }
            private JButton tiny() { JButton b = new JButton(); b.setPreferredSize(new Dimension(0,0)); return b; }
            @Override protected void paintThumb(Graphics g, JComponent c, Rectangle r) {
                if (r.isEmpty()) return;
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(isThumbRollover() ? new Color(0x94A3B8) : thumbColor);
                g2.fillRoundRect(r.x+1, r.y+2, r.width-2, r.height-4, 6, 6);
                g2.dispose();
            }
        });
    }
}
