package view;

import i18n.I18n;
import i18n.LanguageListener;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;

public class LoginFrame extends JFrame implements LanguageListener {

    @FunctionalInterface
    public interface LoginListener {
        void onLogin(String username, String password);
    }

    private JTextField     usernameField;
    private JPasswordField passwordField;
    private JLabel         errorLabel;
    private JButton        loginBtn;
    private LoginListener  loginListener;

    // All text-bearing labels that must re-render on language switch
    private JLabel sidebarTitleLabel;   // left panel: app.title
    private JLabel sidebarSubLabel;     // left panel: login.headline
    private JLabel sidebarTagline;      // left panel: login.tagline
    private JLabel formHeading;         // right panel: login.greet
    private JLabel formSubheading;      // right panel: login.subline
    private JLabel usernameLabel;
    private JLabel passwordLabel;

    public LoginFrame() {
        setTitle(I18n.t("app.title"));
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(900, 520);
        setLocationRelativeTo(null);
        setResizable(false);
        setContentPane(buildUI());
        I18n.addListener(this);
    }

    @Override
    public void onLanguageChanged() {
        setTitle(I18n.t("app.title"));
        sidebarTitleLabel.setText(titleHtml());
        sidebarSubLabel.setText(headlineHtml());
        sidebarTagline.setText(taglineHtml());
        formHeading.setText(I18n.t("login.greet"));
        formSubheading.setText(I18n.t("login.subline"));
        usernameLabel.setText(I18n.t("login.username"));
        passwordLabel.setText(I18n.t("login.password"));
        loginBtn.setText(I18n.t("login.button"));
        errorLabel.setText(" ");
    }

    private JPanel buildUI() {
        JPanel root = new JPanel(new GridLayout(1, 2));
        root.add(buildLeftPanel());
        root.add(buildRightPanel());
        return root;
    }

    // Left: solid teal branding panel
    private JPanel buildLeftPanel() {
        JPanel left = new JPanel(new GridBagLayout()) {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setColor(new Color(255, 255, 255, 8));
                for (int i = -getHeight(); i < getWidth() + getHeight(); i += 40)
                    g2.drawLine(i, 0, i + getHeight(), getHeight());
                g2.dispose();
            }
        };
        left.setBackground(Theme.SIDEBAR_BG);

        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setOpaque(false);
        content.setBorder(new EmptyBorder(0, 32, 0, 32));

        // Brand mark — teal circle with "L"
        JPanel mark = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Theme.PRIMARY);
                g2.fillOval(0, 0, 56, 56);
                g2.setColor(Color.WHITE);
                g2.setFont(Theme.bold(26));
                FontMetrics fm = g2.getFontMetrics();
                String letter = "L";
                int tx = (56 - fm.stringWidth(letter)) / 2;
                int ty = (56 + fm.getAscent() - fm.getDescent()) / 2;
                g2.drawString(letter, tx, ty);
                g2.dispose();
            }
        };
        mark.setOpaque(false);
        mark.setPreferredSize(new Dimension(56, 56));
        mark.setMaximumSize(new Dimension(56, 56));
        mark.setAlignmentX(LEFT_ALIGNMENT);

        sidebarTitleLabel = new JLabel(titleHtml());
        sidebarTitleLabel.setFont(Theme.bold(24));
        sidebarTitleLabel.setForeground(Color.WHITE);
        sidebarTitleLabel.setAlignmentX(LEFT_ALIGNMENT);

        sidebarSubLabel = new JLabel(headlineHtml());
        sidebarSubLabel.setFont(Theme.regular(14));
        sidebarSubLabel.setForeground(new Color(0xCCFBF1));
        sidebarSubLabel.setAlignmentX(LEFT_ALIGNMENT);

        JPanel divider = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setColor(new Color(255, 255, 255, 40));
                g2.fillRect(0, 0, 48, 2);
                g2.dispose();
            }
        };
        divider.setOpaque(false);
        divider.setMaximumSize(new Dimension(48, 2));
        divider.setPreferredSize(new Dimension(48, 2));
        divider.setAlignmentX(LEFT_ALIGNMENT);

        sidebarTagline = new JLabel(taglineHtml());
        sidebarTagline.setFont(Theme.regular(13));
        sidebarTagline.setForeground(new Color(0x99F6E4));
        sidebarTagline.setAlignmentX(LEFT_ALIGNMENT);

        content.add(mark);
        content.add(Box.createVerticalStrut(20));
        content.add(sidebarTitleLabel);
        content.add(Box.createVerticalStrut(6));
        content.add(sidebarSubLabel);
        content.add(Box.createVerticalStrut(24));
        content.add(divider);
        content.add(Box.createVerticalStrut(20));
        content.add(sidebarTagline);

        left.add(content);
        return left;
    }

    // Right: white login form
    private JPanel buildRightPanel() {
        JPanel right = new JPanel(new GridBagLayout());
        right.setBackground(Theme.SURFACE);

        JPanel form = new JPanel();
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setOpaque(false);
        form.setBorder(new EmptyBorder(0, 48, 0, 48));
        form.setPreferredSize(new Dimension(360, 420));

        formHeading = new JLabel(I18n.t("login.greet"));
        formHeading.setFont(Theme.bold(26));
        formHeading.setForeground(Theme.TEXT);
        formHeading.setAlignmentX(LEFT_ALIGNMENT);

        formSubheading = new JLabel(I18n.t("login.subline"));
        formSubheading.setFont(Theme.regular(13));
        formSubheading.setForeground(Theme.TEXT_MUTED);
        formSubheading.setAlignmentX(LEFT_ALIGNMENT);

        usernameLabel = fieldLabel(I18n.t("login.username"));
        passwordLabel = fieldLabel(I18n.t("login.password"));

        usernameField = new JTextField();
        passwordField = new JPasswordField();
        styleInput(usernameField);
        styleInput(passwordField);
        usernameField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        passwordField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        usernameField.setAlignmentX(LEFT_ALIGNMENT);
        passwordField.setAlignmentX(LEFT_ALIGNMENT);
        usernameField.addActionListener(e -> fireLogin());
        passwordField.addActionListener(e -> fireLogin());

        loginBtn = Theme.primaryBtn(I18n.t("login.button"));
        loginBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        loginBtn.setAlignmentX(LEFT_ALIGNMENT);
        loginBtn.addActionListener(e -> fireLogin());

        errorLabel = new JLabel(" ");
        errorLabel.setFont(Theme.regular(12));
        errorLabel.setForeground(new Color(0xDC2626));
        errorLabel.setAlignmentX(LEFT_ALIGNMENT);

        form.add(formHeading);
        form.add(Box.createVerticalStrut(6));
        form.add(formSubheading);
        form.add(Box.createVerticalStrut(32));
        form.add(usernameLabel);
        form.add(Box.createVerticalStrut(6));
        form.add(usernameField);
        form.add(Box.createVerticalStrut(18));
        form.add(passwordLabel);
        form.add(Box.createVerticalStrut(6));
        form.add(passwordField);
        form.add(Box.createVerticalStrut(8));
        form.add(errorLabel);
        form.add(Box.createVerticalStrut(8));
        form.add(loginBtn);

        right.add(form);
        return right;
    }

    private String taglineHtml() {
        return "<html><body style='width:240px'>" + I18n.t("login.tagline") + "</body></html>";
    }

    private String titleHtml() {
        return "<html><body style='width:260px'>" + I18n.t("app.title") + "</body></html>";
    }

    private String headlineHtml() {
        return "<html><body style='width:260px'>" + I18n.t("login.headline") + "</body></html>";
    }

    private JLabel fieldLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(Theme.bold(12));
        l.setForeground(Theme.TEXT_MUTED);
        l.setAlignmentX(LEFT_ALIGNMENT);
        return l;
    }

    private void styleInput(JTextField f) {
        f.setFont(Theme.regular(14));
        f.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Theme.BORDER, 1, true),
            new EmptyBorder(8, 12, 8, 12)));
        f.addFocusListener(new FocusAdapter() {
            public void focusGained(FocusEvent e) {
                f.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(Theme.PRIMARY, 1, true),
                    new EmptyBorder(8, 12, 8, 12)));
            }
            public void focusLost(FocusEvent e) {
                f.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(Theme.BORDER, 1, true),
                    new EmptyBorder(8, 12, 8, 12)));
            }
        });
    }

    public void setLoginListener(LoginListener listener) { this.loginListener = listener; }

    public void setLoading(boolean loading) {
        loginBtn.setEnabled(!loading);
        loginBtn.setText(loading ? I18n.t("common.loading") : I18n.t("login.button"));
    }

    public void showError(String msg) { errorLabel.setText(msg); }

    public void close() {
        I18n.removeListener(this);
        dispose();
    }

    private void fireLogin() {
        if (loginListener != null)
            loginListener.onLogin(usernameField.getText().trim(), new String(passwordField.getPassword()));
    }
}
