package view.panels;

import i18n.I18n;
import i18n.LanguageListener;
import model.Book;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.border.Border;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class DashboardPanel extends JPanel implements LanguageListener {

    private JLabel totalVal, availVal, unavailVal;
    private JLabel totalLbl, availLbl, unavailLbl;
    private JButton refreshBtn;
    private DefaultTableModel recentModel;
    private Runnable refreshListener;
    private TitledBorder recentBorder;
    private JPanel recentPanel;

    public DashboardPanel() {
        setLayout(new BorderLayout(12, 12));
        setBorder(new EmptyBorder(16, 16, 16, 16));
        setBackground(new Color(0xF9FAFB));

        add(buildStats(),  BorderLayout.NORTH);
        add(buildRecent(), BorderLayout.CENTER);
        add(buildBottom(), BorderLayout.SOUTH);
    }

    public void setRefreshListener(Runnable r) { this.refreshListener = r; }

    public void setStats(int total, int available, int unavailable) {
        totalVal.setText(String.valueOf(total));
        availVal.setText(String.valueOf(available));
        unavailVal.setText(String.valueOf(unavailable));
    }

    public void setRecentBooks(List<Book> books) {
        recentModel.setRowCount(0);
        for (Book b : books)
            recentModel.addRow(new Object[]{ b.getTitle(), b.getAuthor(), b.getCreatedAt(),
                b.isAvailable() ? I18n.t("books.status.available") : I18n.t("books.status.unavailable") });
    }

    @Override
    public void onLanguageChanged() {
        totalLbl.setText(I18n.t("dashboard.total"));
        availLbl.setText(I18n.t("dashboard.available"));
        unavailLbl.setText(I18n.t("dashboard.unavailable"));
        refreshBtn.setText(I18n.t("dashboard.refresh"));
        recentModel.setColumnIdentifiers(cols());
        recentBorder.setTitle(I18n.t("dashboard.recent"));
        recentPanel.repaint();
    }

    private JPanel buildStats() {
        JPanel row = new JPanel(new GridLayout(1, 3, 12, 0));
        row.setBackground(new Color(0xF9FAFB));

        totalVal   = bigNum(); availVal = bigNum(); unavailVal = bigNum();
        totalLbl   = new JLabel(I18n.t("dashboard.total"),   SwingConstants.CENTER);
        availLbl   = new JLabel(I18n.t("dashboard.available"),  SwingConstants.CENTER);
        unavailLbl = new JLabel(I18n.t("dashboard.unavailable"), SwingConstants.CENTER);

        row.add(statBox(totalLbl, totalVal, new Color(0xD1FAE5)));
        row.add(statBox(availLbl, availVal, new Color(0xDBEAFE)));
        row.add(statBox(unavailLbl, unavailVal, new Color(0xFEE2E2)));
        return row;
    }

    private JPanel statBox(JLabel lbl, JLabel val, Color bg) {
        JPanel p = new JPanel(new BorderLayout(4, 4));
        p.setBackground(bg);
        p.setBorder(new EmptyBorder(16, 20, 16, 20));
        lbl.setFont(new Font("Dialog", Font.PLAIN, 12));
        val.setFont(new Font("Dialog", Font.BOLD, 32));
        val.setHorizontalAlignment(SwingConstants.CENTER);
        p.add(lbl, BorderLayout.NORTH);
        p.add(val, BorderLayout.CENTER);
        return p;
    }

    private JLabel bigNum() {
        JLabel l = new JLabel("—", SwingConstants.CENTER);
        l.setFont(new Font("Dialog", Font.BOLD, 32));
        return l;
    }

    private JPanel buildRecent() {
        recentModel = new DefaultTableModel(cols(), 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable table = new JTable(recentModel);
        table.setRowHeight(28);
        table.getTableHeader().setReorderingAllowed(false);

        JScrollPane sp = new JScrollPane(table);
        view.MainFrame.modernScrollBar(sp);

        recentBorder = new TitledBorder(I18n.t("dashboard.recent"));
        recentPanel  = new JPanel(new BorderLayout());
        recentPanel.setBorder(recentBorder);
        recentPanel.add(sp);
        return recentPanel;
    }

    private JPanel buildBottom() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        p.setBackground(new Color(0xF9FAFB));
        refreshBtn = new JButton(I18n.t("dashboard.refresh"));
        refreshBtn.setFocusPainted(false);
        refreshBtn.addActionListener(e -> { if (refreshListener != null) refreshListener.run(); });
        p.add(refreshBtn);
        return p;
    }

    private Object[] cols() {
        return new Object[]{ I18n.t("dashboard.col.title"), I18n.t("dashboard.col.author"),
                             I18n.t("dashboard.col.date"),  I18n.t("dashboard.col.status") };
    }
}
