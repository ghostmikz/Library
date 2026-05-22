package view;

import i18n.I18n;
import util.AppSettings;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class SettingsDialog extends JDialog {

    public SettingsDialog(Frame parent) {
        super(parent, I18n.t("settings.title"), true);
        setSize(360, 260);
        setLocationRelativeTo(parent);
        setResizable(false);
        setContentPane(buildUI());
    }

    private JPanel buildUI() {
        JPanel root = new JPanel(new BorderLayout(0, 0));
        root.setBackground(Theme.SURFACE);

        JPanel form = new JPanel(new GridLayout(0, 2, 10, 12));
        form.setBackground(Theme.SURFACE);
        form.setBorder(new EmptyBorder(24, 24, 16, 24));

        JTextField hostField = new JTextField(AppSettings.getServerHost());
        JTextField portField = new JTextField(String.valueOf(AppSettings.getServerPort()));
        Theme.styleField(hostField);
        Theme.styleField(portField);

        String lang = AppSettings.getLanguage();
        JRadioButton mnBtn = new JRadioButton("Монгол", "mn".equals(lang));
        JRadioButton enBtn = new JRadioButton("English", "en".equals(lang));
        mnBtn.setFont(Theme.regular(13));
        enBtn.setFont(Theme.regular(13));
        mnBtn.setOpaque(false);
        enBtn.setOpaque(false);
        mnBtn.setFocusPainted(false);
        enBtn.setFocusPainted(false);
        ButtonGroup bg = new ButtonGroup();
        bg.add(mnBtn);
        bg.add(enBtn);

        JLabel langLbl = new JLabel(I18n.t("settings.language"));
        langLbl.setFont(Theme.regular(13));
        langLbl.setForeground(Theme.TEXT_MUTED);
        JPanel langRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        langRow.setOpaque(false);
        langRow.add(mnBtn);
        langRow.add(enBtn);

        JLabel hostLbl = new JLabel(I18n.t("settings.host"));
        JLabel portLbl = new JLabel(I18n.t("settings.port"));
        hostLbl.setFont(Theme.regular(13));
        portLbl.setFont(Theme.regular(13));
        hostLbl.setForeground(Theme.TEXT_MUTED);
        portLbl.setForeground(Theme.TEXT_MUTED);

        form.add(langLbl);  form.add(langRow);
        form.add(hostLbl);  form.add(hostField);
        form.add(portLbl);  form.add(portField);

        JSeparator sep = new JSeparator();
        sep.setForeground(Theme.BORDER);

        JPanel btns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        btns.setBackground(Theme.SURFACE);
        btns.setBorder(new EmptyBorder(8, 16, 16, 16));
        JButton cancel = Theme.secondaryBtn(I18n.t("common.cancel"));
        JButton save   = Theme.primaryBtn(I18n.t("common.save"));
        cancel.addActionListener(e -> dispose());
        save.addActionListener(e -> {
            if (mnBtn.isSelected()) I18n.setLocale("mn");
            else                    I18n.setLocale("en");
            AppSettings.setServerHost(hostField.getText().trim());
            try { AppSettings.setServerPort(Integer.parseInt(portField.getText().trim())); }
            catch (NumberFormatException ignored) {}
            AppSettings.save();
            dispose();
        });
        btns.add(cancel);
        btns.add(save);

        root.add(form, BorderLayout.CENTER);
        root.add(btns, BorderLayout.SOUTH);
        return root;
    }
}
