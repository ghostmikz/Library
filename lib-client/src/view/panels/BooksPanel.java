package view.panels;

import i18n.I18n;
import i18n.LanguageListener;
import model.Book;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class BooksPanel extends JPanel implements LanguageListener {

    @FunctionalInterface public interface BookSaver {
        void save(Book b, boolean isNew, Runnable onSuccess, Consumer<String> onError);
    }
    @FunctionalInterface public interface BookDeleter {
        void delete(int id, Runnable onSuccess, Consumer<String> onError);
    }

    private final List<Book> allBooks = new ArrayList<>();
    private BookSaver   bookSaver;
    private BookDeleter bookDeleter;

    private JTextField searchField;
    private JComboBox<String> filterCombo;
    private DefaultTableModel model;
    private JTable table;
    private JButton addBtn, editBtn, deleteBtn;
    private JLabel searchLabel;
    private String searchQuery = "";
    private String filterMode  = "ALL";

    public BooksPanel() {
        setLayout(new BorderLayout(8, 8));
        setBorder(new EmptyBorder(12, 12, 12, 12));

        add(buildToolbar(), BorderLayout.NORTH);
        add(buildTable(),   BorderLayout.CENTER);
        add(buildActions(), BorderLayout.SOUTH);
    }

    public void setBooks(List<Book> books)    { allBooks.clear(); allBooks.addAll(books); refresh(); }
    public void setBookSaver(BookSaver s)     { this.bookSaver = s; }
    public void setBookDeleter(BookDeleter d) { this.bookDeleter = d; }

    @Override
    public void onLanguageChanged() {
        model.setColumnIdentifiers(cols());
        filterCombo.removeAllItems();
        filterCombo.addItem(I18n.t("books.filter.all"));
        filterCombo.addItem(I18n.t("books.filter.available"));
        filterCombo.addItem(I18n.t("books.filter.unavailable"));
        addBtn.setText(I18n.t("books.add"));
        searchLabel.setText(I18n.t("books.search") + " ");
        editBtn.setText(I18n.t("books.edit"));
        deleteBtn.setText(I18n.t("books.delete"));
        refresh();
    }

    private JPanel buildToolbar() {
        JPanel bar = new JPanel(new BorderLayout(8, 0));

        searchField = new JTextField(20);
        searchField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e)  { searchQuery = searchField.getText(); refresh(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e)  { searchQuery = searchField.getText(); refresh(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) {}
        });

        filterCombo = new JComboBox<>(new String[]{
            I18n.t("books.filter.all"), I18n.t("books.filter.available"), I18n.t("books.filter.unavailable")
        });
        filterCombo.addActionListener(e -> {
            filterMode = switch (filterCombo.getSelectedIndex()) {
                case 1 -> "AVAILABLE"; case 2 -> "UNAVAILABLE"; default -> "ALL";
            };
            refresh();
        });

        addBtn = new JButton(I18n.t("books.add"));
        addBtn.setFocusPainted(false);
        addBtn.addActionListener(e -> openForm(null));

        searchLabel = new JLabel(I18n.t("books.search") + " ");
        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        left.add(searchLabel);
        left.add(searchField);
        left.add(filterCombo);

        bar.add(left,   BorderLayout.CENTER);
        bar.add(addBtn, BorderLayout.EAST);
        return bar;
    }

    private JScrollPane buildTable() {
        model = new DefaultTableModel(cols(), 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(model);
        table.setRowHeight(26);
        table.getTableHeader().setReorderingAllowed(false);
        // hide id column
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
        editBtn   = new JButton(I18n.t("books.edit"));
        deleteBtn = new JButton(I18n.t("books.delete"));
        deleteBtn.setForeground(new Color(0xDC2626));
        editBtn.setFocusPainted(false);
        deleteBtn.setFocusPainted(false);
        editBtn.setEnabled(false);
        deleteBtn.setEnabled(false);

        editBtn.addActionListener(e -> { Book b = selected(); if (b != null) openForm(b); });
        deleteBtn.addActionListener(e -> {
            Book b = selected();
            if (b == null) return;
            String msg = MessageFormat.format(I18n.t("books.confirm.delete"), b.getTitle());
            if (JOptionPane.showConfirmDialog(this, msg, I18n.t("common.confirm"),
                    JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) return;
            if (bookDeleter != null)
                bookDeleter.delete(b.getId(), () -> {}, err -> showErr(err));
        });

        bar.add(editBtn);
        bar.add(deleteBtn);
        return bar;
    }

    private void updateButtons() {
        boolean sel = table.getSelectedRow() >= 0;
        editBtn.setEnabled(sel); deleteBtn.setEnabled(sel);
    }

    private Book selected() {
        int row = table.getSelectedRow();
        if (row < 0) return null;
        int id = (int) model.getValueAt(row, 0);
        return allBooks.stream().filter(b -> b.getId() == id).findFirst().orElse(null);
    }

    private void refresh() {
        model.setRowCount(0);
        String q = searchQuery.toLowerCase();
        for (Book b : allBooks) {
            if (!q.isEmpty() && !b.getTitle().toLowerCase().contains(q)
                    && !b.getAuthor().toLowerCase().contains(q)
                    && (b.getIsbn() == null || !b.getIsbn().toLowerCase().contains(q))) continue;
            if ("AVAILABLE".equals(filterMode)   && !b.isAvailable()) continue;
            if ("UNAVAILABLE".equals(filterMode) &&  b.isAvailable()) continue;
            model.addRow(new Object[]{ b.getId(), b.getTitle(), b.getAuthor(),
                b.getGenre() != null ? b.getGenre() : "",
                b.getIsbn()  != null ? b.getIsbn()  : "",
                b.getTotalQuantity(), b.getAvailableQuantity(),
                b.isAvailable() ? I18n.t("books.status.available") : I18n.t("books.status.unavailable") });
        }
    }

    private void openForm(Book existing) {
        boolean isNew = existing == null;
        JDialog dlg = new JDialog((Frame) SwingUtilities.getWindowAncestor(this),
                I18n.t(isNew ? "books.form.add" : "books.form.edit"), true);
        dlg.setSize(380, 340);
        dlg.setLocationRelativeTo(this);

        JPanel form = new JPanel(new GridLayout(0, 2, 8, 8));
        form.setBorder(new EmptyBorder(16, 16, 8, 16));

        JTextField titleF  = new JTextField(existing != null ? existing.getTitle()  : "");
        JTextField authorF = new JTextField(existing != null ? existing.getAuthor() : "");
        JTextField genreF  = new JTextField(existing != null && existing.getGenre() != null ? existing.getGenre() : "");
        JTextField isbnF   = new JTextField(existing != null && existing.getIsbn()  != null ? existing.getIsbn()  : "");
        JSpinner   totalSp = new JSpinner(new SpinnerNumberModel(existing != null ? existing.getTotalQuantity()     : 1, 0, 9999, 1));
        JSpinner   availSp = new JSpinner(new SpinnerNumberModel(existing != null ? existing.getAvailableQuantity() : 1, 0, 9999, 1));

        form.add(new JLabel(I18n.t("books.form.title")));    form.add(titleF);
        form.add(new JLabel(I18n.t("books.form.author")));   form.add(authorF);
        form.add(new JLabel(I18n.t("books.form.genre")));    form.add(genreF);
        form.add(new JLabel(I18n.t("books.form.isbn")));     form.add(isbnF);
        form.add(new JLabel(I18n.t("books.form.quantity"))); form.add(totalSp);
        form.add(new JLabel(I18n.t("books.form.available"))); form.add(availSp);

        JPanel btns = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton cancel = new JButton(I18n.t("common.cancel"));
        JButton save   = new JButton(I18n.t("common.save"));
        cancel.setFocusPainted(false);
        save.setFocusPainted(false);
        cancel.addActionListener(e -> dlg.dispose());
        save.addActionListener(e -> {
            String t = titleF.getText().trim(), a = authorF.getText().trim();
            if (t.isEmpty() || a.isEmpty()) { showErr(I18n.t("common.error")); return; }
            Book b = existing != null ? existing : new Book();
            b.setTitle(t); b.setAuthor(a); b.setGenre(genreF.getText().trim());
            b.setIsbn(isbnF.getText().trim());
            b.setTotalQuantity((int) totalSp.getValue());
            b.setAvailableQuantity((int) availSp.getValue());
            if (bookSaver != null)
                bookSaver.save(b, isNew, dlg::dispose,
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
        return new Object[]{ "id", I18n.t("books.col.title"), I18n.t("books.col.author"),
            I18n.t("books.col.genre"), I18n.t("books.col.isbn"),
            I18n.t("books.col.total"), I18n.t("books.col.available"), I18n.t("books.col.status") };
    }
}
