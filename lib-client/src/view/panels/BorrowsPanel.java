package view.panels;

import i18n.I18n;
import i18n.LanguageListener;
import model.Book;
import model.Borrow;
import view.Theme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.text.MessageFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class BorrowsPanel extends JPanel implements LanguageListener {

    @FunctionalInterface public interface BorrowLender {
        void lend(int bookId, String borrowerName, String phone, String borrowDate, String dueDate,
                  String notes, Runnable onSuccess, Consumer<String> onError);
    }
    @FunctionalInterface public interface BorrowReturner {
        void returnBook(int borrowId, String returnDate, Runnable onSuccess, Consumer<String> onError);
    }

    private final List<Borrow> allBorrows = new ArrayList<>();
    private List<Book>   availableBooks = new ArrayList<>();
    private BorrowLender lender;
    private BorrowReturner returner;
    private Runnable refreshListener;

    private DefaultTableModel model;
    private JTable  table;
    private JButton lendBtn, returnBtn, refreshBtn;
    private JCheckBox activeOnlyBox;
    private boolean showActiveOnly = true;

    public BorrowsPanel() {
        setLayout(new BorderLayout(0, 12));
        setBorder(new EmptyBorder(16, 16, 16, 16));
        setBackground(Theme.BG);

        add(buildToolbar(), BorderLayout.NORTH);
        add(buildTable(),   BorderLayout.CENTER);
        add(buildActions(), BorderLayout.SOUTH);
    }

    public void setBorrows(List<Borrow> borrows)      { allBorrows.clear(); allBorrows.addAll(borrows); refresh(); }
    public void setAvailableBooks(List<Book> books)   { this.availableBooks = new ArrayList<>(books); }
    public void setLender(BorrowLender l)             { this.lender = l; }
    public void setReturner(BorrowReturner r)         { this.returner = r; }
    public void setRefreshListener(Runnable r)        { this.refreshListener = r; }
    public boolean isActiveOnly()                     { return showActiveOnly; }

    @Override
    public void onLanguageChanged() {
        model.setColumnIdentifiers(cols());
        lendBtn.setText(I18n.t("borrows.lend"));
        returnBtn.setText(I18n.t("borrows.return"));
        refreshBtn.setText(I18n.t("borrows.refresh"));
        activeOnlyBox.setText(I18n.t("borrows.filter.active"));
        refresh();
    }

    private JPanel buildToolbar() {
        JPanel bar = new JPanel(new BorderLayout(8, 0));
        bar.setOpaque(false);

        activeOnlyBox = new JCheckBox(I18n.t("borrows.filter.active"), true);
        activeOnlyBox.setFont(Theme.regular(13));
        activeOnlyBox.setForeground(Theme.TEXT_MUTED);
        activeOnlyBox.setOpaque(false);
        activeOnlyBox.setFocusPainted(false);
        activeOnlyBox.addActionListener(e -> {
            showActiveOnly = activeOnlyBox.isSelected();
            if (refreshListener != null) refreshListener.run();
        });

        lendBtn = Theme.primaryBtn(I18n.t("borrows.lend"));
        lendBtn.addActionListener(e -> openLendForm());

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        left.setOpaque(false);
        left.add(activeOnlyBox);

        bar.add(left,    BorderLayout.CENTER);
        bar.add(lendBtn, BorderLayout.EAST);
        return bar;
    }

    private JScrollPane buildTable() {
        model = new DefaultTableModel(cols(), 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(model);
        Theme.styleTable(table);

        table.getColumnModel().getColumn(0).setMinWidth(0);
        table.getColumnModel().getColumn(0).setMaxWidth(0);
        table.getColumnModel().getColumn(0).setPreferredWidth(0);

        // Colored rows: overdue = red tint, returned = muted
        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(
                    JTable t, Object v, boolean sel, boolean foc, int row, int col) {
                Component c = super.getTableCellRendererComponent(t, v, sel, foc, row, col);
                if (!sel) {
                    int id = (int) model.getValueAt(row, 0);
                    Borrow b = allBorrows.stream().filter(x -> x.getId() == id).findFirst().orElse(null);
                    if (b != null) {
                        if      (b.isOverdue())   c.setBackground(Theme.OVERDUE_BG);
                        else if (b.isReturned())  c.setBackground(Theme.RETURNED_BG);
                        else                      c.setBackground(row % 2 == 0 ? Theme.SURFACE : Theme.ROW_ALT);
                    }
                }
                setFont(Theme.regular(13));
                setBorder(new EmptyBorder(0, 12, 0, 12));
                return c;
            }
        });

        table.getSelectionModel().addListSelectionListener(e -> updateButtons());

        JScrollPane sp = new JScrollPane(table);
        sp.setBorder(BorderFactory.createLineBorder(Theme.BORDER, 1, true));
        Theme.modernScrollBar(sp);
        return sp;
    }

    private JPanel buildActions() {
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        bar.setOpaque(false);

        returnBtn  = Theme.secondaryBtn(I18n.t("borrows.return"));
        refreshBtn = Theme.secondaryBtn(I18n.t("borrows.refresh"));
        returnBtn.setEnabled(false);

        returnBtn.addActionListener(e -> {
            Borrow b = selected();
            if (b == null || b.isReturned()) return;
            String msg = MessageFormat.format(I18n.t("borrows.confirm.return"), b.getBorrowerName());
            if (JOptionPane.showConfirmDialog(this, msg, I18n.t("common.confirm"),
                    JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) return;
            if (returner != null)
                returner.returnBook(b.getId(), LocalDate.now().toString(), () -> {}, err -> showErr(err));
        });

        refreshBtn.addActionListener(e -> { if (refreshListener != null) refreshListener.run(); });

        bar.add(returnBtn);
        bar.add(refreshBtn);
        return bar;
    }

    private void updateButtons() {
        Borrow b = selected();
        returnBtn.setEnabled(b != null && !b.isReturned());
    }

    private Borrow selected() {
        int row = table.getSelectedRow();
        if (row < 0) return null;
        int id = (int) model.getValueAt(row, 0);
        return allBorrows.stream().filter(b -> b.getId() == id).findFirst().orElse(null);
    }

    private void refresh() {
        model.setRowCount(0);
        for (Borrow b : allBorrows) {
            String status = b.isReturned()  ? I18n.t("borrows.status.returned")
                          : b.isOverdue()   ? I18n.t("borrows.status.overdue")
                                            : I18n.t("borrows.status.active");
            model.addRow(new Object[]{
                b.getId(), b.getBookTitle(), b.getBorrowerName(),
                b.getBorrowerPhone() != null ? b.getBorrowerPhone() : "",
                b.getBorrowDate(), b.getDueDate(),
                b.isReturned() ? b.getReturnDate() : status,
                b.getNotes() != null ? b.getNotes() : ""
            });
        }
        updateButtons();
    }

    private void openLendForm() {
        JDialog dlg = new JDialog((Frame) SwingUtilities.getWindowAncestor(this),
                I18n.t("borrows.form.title"), true);
        dlg.setSize(420, 380);
        dlg.setLocationRelativeTo(this);

        JPanel form = new JPanel(new GridLayout(0, 2, 10, 10));
        form.setBorder(new EmptyBorder(20, 20, 12, 20));
        form.setBackground(Theme.SURFACE);

        DefaultComboBoxModel<String> bookModel = new DefaultComboBoxModel<>();
        List<Integer> bookIds = new ArrayList<>();
        for (Book bk : availableBooks) {
            if (bk.isAvailable()) {
                bookModel.addElement(bk.getTitle() + (bk.getIsbn() != null && !bk.getIsbn().isBlank()
                        ? " [" + bk.getIsbn() + "]" : ""));
                bookIds.add(bk.getId());
            }
        }
        JComboBox<String> bookCombo = new JComboBox<>(bookModel);
        bookCombo.setFont(Theme.regular(13));

        JTextField borrowerF = new JTextField();
        JTextField phoneF    = new JTextField();
        JTextField dateF     = new JTextField(LocalDate.now().toString());
        JTextField dueF      = new JTextField(LocalDate.now().plusDays(14).toString());
        JTextField notesF    = new JTextField();

        for (JTextField f : new JTextField[]{borrowerF, phoneF, dateF, dueF, notesF})
            Theme.styleField(f);

        addFormRow(form, I18n.t("borrows.form.book"),     bookCombo);
        addFormRow(form, I18n.t("borrows.form.borrower"), borrowerF);
        addFormRow(form, I18n.t("borrows.form.phone"),    phoneF);
        addFormRow(form, I18n.t("borrows.form.date"),     dateF);
        addFormRow(form, I18n.t("borrows.form.due"),      dueF);
        addFormRow(form, I18n.t("borrows.form.notes"),    notesF);

        JPanel btns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        btns.setBorder(new EmptyBorder(0, 16, 12, 16));
        btns.setBackground(Theme.SURFACE);
        JButton cancel = Theme.secondaryBtn(I18n.t("common.cancel"));
        JButton save   = Theme.primaryBtn(I18n.t("common.save"));
        cancel.addActionListener(e -> dlg.dispose());
        save.addActionListener(e -> {
            String borrower = borrowerF.getText().trim();
            if (borrower.isEmpty() || bookIds.isEmpty()) { showErr(I18n.t("common.error")); return; }
            int idx = bookCombo.getSelectedIndex();
            if (idx < 0) { showErr(I18n.t("common.error")); return; }
            int bookId = bookIds.get(idx);
            if (lender != null)
                lender.lend(bookId, borrower, phoneF.getText().trim(),
                        dateF.getText().trim(), dueF.getText().trim(), notesF.getText().trim(),
                        dlg::dispose,
                        msg -> JOptionPane.showMessageDialog(dlg, msg, I18n.t("common.error"), JOptionPane.ERROR_MESSAGE));
        });
        btns.add(cancel);
        btns.add(save);

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(Theme.SURFACE);
        root.add(form, BorderLayout.CENTER);
        root.add(btns, BorderLayout.SOUTH);
        dlg.setContentPane(root);
        dlg.setVisible(true);
    }

    private void addFormRow(JPanel form, String labelText, JComponent field) {
        JLabel lbl = new JLabel(labelText);
        lbl.setFont(Theme.regular(13));
        lbl.setForeground(Theme.TEXT_MUTED);
        form.add(lbl);
        form.add(field);
    }

    private void showErr(String msg) {
        JOptionPane.showMessageDialog(this, msg, I18n.t("common.error"), JOptionPane.ERROR_MESSAGE);
    }

    private Object[] cols() {
        return new Object[]{ "id",
            I18n.t("borrows.col.book"), I18n.t("borrows.col.borrower"),
            I18n.t("borrows.col.phone"), I18n.t("borrows.col.borrowed"),
            I18n.t("borrows.col.due"), I18n.t("borrows.col.returned"),
            I18n.t("borrows.col.notes") };
    }
}
