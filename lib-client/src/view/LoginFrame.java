package view;

import i18n.I18n;
import i18n.LanguageListener;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class LoginFrame extends JFrame implements LanguageListener {

    private static final Color TEAL = new Color(0x0F766E);

    @FunctionalInterface
    public interface LoginListener {
        void onLogin(String username, String password);
    }

    private JTextField     usernameField;
    private JPasswordField passwordField;
    private JLabel         errorLabel;
    private JButton        loginBtn;
    private LoginListener  loginListener;

    private JLabel titleLabel, usernameLabel, passwordLabel;

    public LoginFrame() {
        setTitle(I18n.t("app.title"));
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(360, 320);
        setLocationRelativeTo(null);
        setResizable(false);
        setContentPane(buildUI());
        I18n.addListener(this);
    }

    @Override
    public void onLanguageChanged() {
        setTitle(I18n.t("app.title"));
        titleLabel.setText(I18n.t("app.title"));
        usernameLabel.setText(I18n.t("login.username"));
        passwordLabel.setText(I18n.t("login.password"));
        loginBtn.setText(I18n.t("login.button"));
        errorLabel.setText(" ");
    }

    private JPanel buildUI() {
        JPanel root = new JPanel(new GridBagLayout());
        root.setBackground(Color.WHITE);

        JPanel form = new JPanel();
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setBackground(Color.WHITE);
        form.setBorder(new EmptyBorder(24, 32, 24, 32));

        titleLabel = new JLabel(I18n.t("app.title"));
        titleLabel.setFont(new Font("Dialog", Font.BOLD, 20));
        titleLabel.setForeground(TEAL);
        titleLabel.setAlignmentX(CENTER_ALIGNMENT);

        usernameLabel = label(I18n.t("login.username"));
        passwordLabel = label(I18n.t("login.password"));

        usernameField = new JTextField(20);
        usernameField.addActionListener(e -> fireLogin());

        passwordField = new JPasswordField(20);
        passwordField.addActionListener(e -> fireLogin());

        loginBtn = new JButton(I18n.t("login.button"));
        loginBtn.setBackground(TEAL);
        loginBtn.setForeground(Color.WHITE);
        loginBtn.setFocusPainted(false);
        loginBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        loginBtn.setAlignmentX(CENTER_ALIGNMENT);
        loginBtn.addActionListener(e -> fireLogin());

        errorLabel = new JLabel(" ");
        errorLabel.setForeground(new Color(0xDC2626));
        errorLabel.setFont(new Font("Dialog", Font.PLAIN, 12));
        errorLabel.setAlignmentX(CENTER_ALIGNMENT);

        form.add(titleLabel);
        form.add(Box.createVerticalStrut(24));
        form.add(usernameLabel);
        form.add(Box.createVerticalStrut(4));
        form.add(usernameField);
        form.add(Box.createVerticalStrut(12));
        form.add(passwordLabel);
        form.add(Box.createVerticalStrut(4));
        form.add(passwordField);
        form.add(Box.createVerticalStrut(16));
        form.add(loginBtn);
        form.add(Box.createVerticalStrut(8));
        form.add(errorLabel);

        root.add(form);
        return root;
    }

    private JLabel label(String text) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("Dialog", Font.PLAIN, 13));
        l.setAlignmentX(LEFT_ALIGNMENT);
        return l;
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
