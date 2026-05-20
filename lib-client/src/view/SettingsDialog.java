package view;

import i18n.I18n;
import util.AppSettings;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class SettingsDialog extends JDialog {

    public SettingsDialog(Frame parent) {
        super(parent, "Тохиргоо / Settings", true);
        setSize(340, 240);
        setLocationRelativeTo(parent);
        setResizable(false);
        setContentPane(buildUI());
    }

    private JPanel buildUI() {
        JPanel root = new JPanel(new BorderLayout(8, 8));
        root.setBorder(new EmptyBorder(16, 20, 16, 20));

        JPanel form = new JPanel(new GridLayout(0, 2, 8, 10));

        JTextField hostField = new JTextField(AppSettings.getServerHost());
        JTextField portField = new JTextField(String.valueOf(AppSettings.getServerPort()));

        String lang = AppSettings.getLanguage();
        JRadioButton mnBtn = new JRadioButton("Монгол", "mn".equals(lang));
        JRadioButton enBtn = new JRadioButton("English", "en".equals(lang));
        ButtonGroup bg = new ButtonGroup();
        bg.add(mnBtn); bg.add(enBtn);

        form.add(new JLabel("Хэл / Language"));
        JPanel langRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        langRow.add(mnBtn); langRow.add(enBtn);
        form.add(langRow);
        form.add(new JLabel("Host"));   form.add(hostField);
        form.add(new JLabel("Port"));   form.add(portField);

        JPanel btns = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton cancel = new JButton(I18n.t("common.cancel"));
        JButton save   = new JButton(I18n.t("common.save"));
        cancel.setFocusPainted(false);
        save.setFocusPainted(false);
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
        btns.add(cancel); btns.add(save);

        root.add(form, BorderLayout.CENTER);
        root.add(btns, BorderLayout.SOUTH);
        return root;
    }
}
