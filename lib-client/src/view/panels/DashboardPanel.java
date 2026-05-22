package view.panels;

import i18n.I18n;
import i18n.LanguageListener;
import model.Book;
import view.Theme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class DashboardPanel extends JPanel implements LanguageListener {

    private JLabel totalVal, availVal, unavailVal;
    private JLabel totalLbl, availLbl, unavailLbl;
    private JButton refreshBtn;
    private DefaultTableModel recentModel;
    private Runnable refreshListener;
    private JLabel recentHeader;

    public DashboardPanel() {
        setLayout(new BorderLayout(16, 16));
        setBorder(new EmptyBorder(20, 20, 20, 20));
        setBackground(Theme.BG);

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
            recentModel.addRow(new Object[]{
                b.getTitle(), b.getAuthor(), b.getCreatedAt(),
                b.isAvailable() ? I18n.t("books.status.available") : I18n.t("books.status.unavailable")
            });
    }

    @Override
    public void onLanguageChanged() {
        totalLbl.setText(I18n.t("dashboard.total"));
        availLbl.setText(I18n.t("dashboard.available"));
        unavailLbl.setText(I18n.t("dashboard.unavailable"));
        refreshBtn.setText(I18n.t("dashboard.refresh"));
        recentModel.setColumnIdentifiers(cols());
        recentHeader.setText(I18n.t("dashboard.recent"));
    }

    private JPanel buildStats() {
        JPanel row = new JPanel(new GridLayout(1, 3, 16, 0));
        row.setOpaque(false);

        totalVal   = statNum();
        availVal   = statNum();
        unavailVal = statNum();

        totalLbl   = new JLabel(I18n.t("dashboard.total"),       SwingConstants.LEFT);
        availLbl   = new JLabel(I18n.t("dashboard.available"),   SwingConstants.LEFT);
        unavailLbl = new JLabel(I18n.t("dashboard.unavailable"), SwingConstants.LEFT);

        row.add(statCard(totalLbl,   totalVal,   Theme.SURFACE, Theme.PRIMARY,             new Color(0xD1FAE5)));
        row.add(statCard(availLbl,   availVal,   Theme.SURFACE, new Color(0x2563EB),        Theme.INFO_LIGHT));
        row.add(statCard(unavailLbl, unavailVal, Theme.SURFACE, new Color(0xDC2626),        new Color(0xFEE2E2)));
        return row;
    }

    private JPanel statCard(JLabel lbl, JLabel val, Color bg, Color accentColor, Color lightBg) {
        JPanel p = new JPanel(new BorderLayout(4, 8)) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(0, 0, 0, 12));
                g2.fillRoundRect(2, 3, getWidth() - 2, getHeight() - 2, 12, 12);
                g2.setColor(bg);
                g2.fillRoundRect(0, 0, getWidth() - 2, getHeight() - 3, 12, 12);
                g2.setColor(lightBg);
                g2.fillRoundRect(0, 0, getWidth() - 2, getHeight() - 3, 12, 12);
                g2.setColor(accentColor);
                g2.fillRoundRect(0, 0, getWidth() - 2, 5, 4, 4);
                g2.fillRect(0, 2, getWidth() - 2, 3);
                g2.dispose();
            }
        };
        p.setOpaque(false);
        p.setBorder(new EmptyBorder(20, 20, 16, 20));

        lbl.setFont(Theme.regular(12));
        lbl.setForeground(Theme.TEXT_MUTED);

        val.setFont(Theme.bold(36));
        val.setForeground(Theme.TEXT);
        val.setHorizontalAlignment(SwingConstants.LEFT);

        p.add(lbl, BorderLayout.NORTH);
        p.add(val, BorderLayout.CENTER);
        return p;
    }

    private JLabel statNum() {
        JLabel l = new JLabel("—", SwingConstants.LEFT);
        l.setFont(Theme.bold(36));
        l.setForeground(Theme.TEXT);
        return l;
    }

    private JPanel buildRecent() {
        recentModel = new DefaultTableModel(cols(), 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable table = new JTable(recentModel);
        Theme.styleTable(table);

        JScrollPane sp = new JScrollPane(table);
        sp.setBorder(BorderFactory.createLineBorder(Theme.BORDER, 1, true));
        Theme.modernScrollBar(sp);

        recentHeader = Theme.sectionLabel(I18n.t("dashboard.recent"));

        JPanel panel = new JPanel(new BorderLayout(0, 8));
        panel.setOpaque(false);
        panel.add(recentHeader, BorderLayout.NORTH);
        panel.add(sp, BorderLayout.CENTER);
        return panel;
    }

    private JPanel buildBottom() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        p.setOpaque(false);
        refreshBtn = Theme.secondaryBtn(I18n.t("dashboard.refresh"));
        refreshBtn.addActionListener(e -> { if (refreshListener != null) refreshListener.run(); });
        p.add(refreshBtn);
        return p;
    }

    private Object[] cols() {
        return new Object[]{
            I18n.t("dashboard.col.title"), I18n.t("dashboard.col.author"),
            I18n.t("dashboard.col.date"),  I18n.t("dashboard.col.status")
        };
    }
}
