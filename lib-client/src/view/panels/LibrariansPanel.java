package view.panels;

import i18n.I18n;
import i18n.LanguageListener;
import model.User;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class LibrariansPanel extends JPanel implements LanguageListener {

    @FunctionalInterface public interface LibrarianSaver {
        void save(User u, String password, boolean isNew, Runnable onSuccess, Consumer<String> onError);
    }
    @FunctionalInterface public interface ActiveToggler {
        void toggle(int id, boolean active, Runnable onSuccess, Consumer<String> onError);
    }
    @FunctionalInterface public interface LibrarianDeleter {
        void delete(int id, Runnable onSuccess, Consumer<String> onError);
    }

    private final List<User> allUsers = new ArrayList<>();
    private LibrarianSaver   saver;
    private ActiveToggler    toggler;
    private LibrarianDeleter deleter;

    private DefaultTableModel model;
    private JTable table;
    private JButton addBtn, editBtn, toggleBtn, deleteBtn;

    public LibrariansPanel() {
        setLayout(new BorderLayout(8, 8));
        setBorder(new EmptyBorder(12, 12, 12, 12));

        addBtn = new JButton(I18n.t("librarians.add"));
        addBtn.setFocusPainted(false);
        addBtn.addActionListener(e -> openForm(null));

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        top.add(addBtn);

        add(top,          BorderLayout.NORTH);
        add(buildTable(), BorderLayout.CENTER);
        add(buildActions(), BorderLayout.SOUTH);
    }

    public void setLibrarians(List<User> users) { allUsers.clear(); allUsers.addAll(users); refresh(); }
    public void setSaver(LibrarianSaver s)       { this.saver   = s; }
    public void setToggler(ActiveToggler t)      { this.toggler = t; }
    public void setDeleter(LibrarianDeleter d)   { this.deleter = d; }

    @Override
    public void onLanguageChanged() {
        model.setColumnIdentifiers(cols());
        addBtn.setText(I18n.t("librarians.add"));
        editBtn.setText(I18n.t("librarians.ctx.edit"));
        deleteBtn.setText(I18n.t("librarians.ctx.delete"));
        refresh();
    }

    private JScrollPane buildTable() {
        model = new DefaultTableModel(cols(), 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(model);
        table.setRowHeight(26);
        table.getTableHeader().setReorderingAllowed(false);
        table.getColumnModel().getColumn(0).setMinWidth(0);
        table.getColumnModel().getColumn(0).setMaxWidth(0);
        table.getColumnModel().getColumn(0).setPreferredWidth(0);
        table.getSelectionModel().addListSelectionListener(e -> updateButtons());

        JScrollPane sp = new JScrollPane(table);
        view.MainFrame.modernScrollBar(sp);
        return sp;
    }

    private JPanel buildActions() {
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        editBtn   = new JButton(I18n.t("librarians.ctx.edit"));
        toggleBtn = new JButton(I18n.t("librarians.ctx.activate"));
        deleteBtn = new JButton(I18n.t("librarians.ctx.delete"));
        deleteBtn.setForeground(new Color(0xDC2626));

        editBtn.setFocusPainted(false);
        toggleBtn.setFocusPainted(false);
        deleteBtn.setFocusPainted(false);
        editBtn.setEnabled(false);
        toggleBtn.setEnabled(false);
        deleteBtn.setEnabled(false);

        editBtn.addActionListener(e -> { User u = selected(); if (u != null) openForm(u); });
        toggleBtn.addActionListener(e -> {
            User u = selected();
            if (u == null) return;
            String key = u.isActive() ? "librarians.confirm.deactivate" : "librarians.confirm.activate";
            if (JOptionPane.showConfirmDialog(this,
                    MessageFormat.format(I18n.t(key), u.getFullName()),
                    I18n.t("common.confirm"), JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) return;
            if (toggler != null)
                toggler.toggle(u.getId(), !u.isActive(), () -> {}, err -> showErr(err));
        });
        deleteBtn.addActionListener(e -> {
            User u = selected();
            if (u == null) return;
            if (JOptionPane.showConfirmDialog(this,
                    MessageFormat.format(I18n.t("librarians.confirm.delete"), u.getFullName()),
                    I18n.t("common.confirm"), JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) return;
            if (deleter != null)
                deleter.delete(u.getId(), () -> {}, err -> showErr(err));
        });

        bar.add(editBtn); bar.add(toggleBtn); bar.add(deleteBtn);
        return bar;
    }

    private void updateButtons() {
        User u = selected();
        boolean sel = u != null;
        editBtn.setEnabled(sel);
        toggleBtn.setEnabled(sel);
        deleteBtn.setEnabled(sel);
        if (sel) toggleBtn.setText(u.isActive() ? I18n.t("librarians.ctx.deactivate") : I18n.t("librarians.ctx.activate"));
    }

    private User selected() {
        int row = table.getSelectedRow();
        if (row < 0) return null;
        int id = (int) model.getValueAt(row, 0);
        return allUsers.stream().filter(u -> u.getId() == id).findFirst().orElse(null);
    }

    private void refresh() {
        model.setRowCount(0);
        for (User u : allUsers)
            model.addRow(new Object[]{ u.getId(), u.getFullName(), u.getUsername(),
                u.isActive() ? I18n.t("common.active") : I18n.t("common.inactive") });
    }

    private void openForm(User existing) {
        boolean isNew = existing == null;
        JDialog dlg = new JDialog((Frame) SwingUtilities.getWindowAncestor(this),
                I18n.t(isNew ? "librarians.form.add" : "librarians.form.edit"), true);
        dlg.setSize(340, 240);
        dlg.setLocationRelativeTo(this);

        JPanel form = new JPanel(new GridLayout(0, 2, 8, 8));
        form.setBorder(new EmptyBorder(16, 16, 8, 16));

        JTextField nameF = new JTextField(existing != null ? existing.getFullName() : "");
        JTextField userF = new JTextField(existing != null ? existing.getUsername() : "");
        userF.setEnabled(isNew);
        JPasswordField passF = new JPasswordField();

        form.add(new JLabel(I18n.t("librarians.form.name")));     form.add(nameF);
        form.add(new JLabel(I18n.t("librarians.form.username"))); form.add(userF);
        form.add(new JLabel(I18n.t("librarians.form.password"))); form.add(passF);

        JPanel btns = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton cancel = new JButton(I18n.t("common.cancel"));
        JButton save   = new JButton(I18n.t("common.save"));
        cancel.setFocusPainted(false);
        save.setFocusPainted(false);
        cancel.addActionListener(e -> dlg.dispose());
        save.addActionListener(e -> {
            String name = nameF.getText().trim(), user = userF.getText().trim();
            String pass = new String(passF.getPassword());
            if (name.isEmpty() || (isNew && (user.isEmpty() || pass.isEmpty()))) {
                showErr(I18n.t("common.error")); return;
            }
            User u = existing != null ? existing : new User();
            u.setFullName(name);
            if (isNew) u.setUsername(user);
            if (saver != null)
                saver.save(u, pass.isEmpty() ? null : pass, isNew, dlg::dispose,
                    msg -> JOptionPane.showMessageDialog(dlg, msg, I18n.t("common.error"), JOptionPane.ERROR_MESSAGE));
        });
        btns.add(cancel); btns.add(save);

        JPanel root = new JPanel(new BorderLayout());
        root.add(form, BorderLayout.CENTER);
        root.add(btns, BorderLayout.SOUTH);
        dlg.setContentPane(root);
        dlg.setVisible(true);
    }

    private void showErr(String msg) {
        JOptionPane.showMessageDialog(this, msg, I18n.t("common.error"), JOptionPane.ERROR_MESSAGE);
    }

    private Object[] cols() {
        return new Object[]{ "id", I18n.t("librarians.col.name"),
            I18n.t("librarians.col.username"), I18n.t("librarians.col.status") };
    }
}
