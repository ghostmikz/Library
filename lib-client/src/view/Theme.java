package view;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.plaf.basic.BasicScrollBarUI;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.util.Arrays;
import java.util.HashSet;

public final class Theme {

    // Palette
    public static final Color BG                  = new Color(0xF1F5F9);
    public static final Color SURFACE             = Color.WHITE;
    public static final Color BORDER              = new Color(0xE2E8F0);
    public static final Color TEXT                = new Color(0x0F172A);
    public static final Color TEXT_MUTED          = new Color(0x64748B);
    public static final Color PRIMARY             = new Color(0x0F766E);
    public static final Color PRIMARY_HOVER       = new Color(0x0D9488);
    public static final Color SIDEBAR_BG          = new Color(0x134E4A);
    public static final Color SIDEBAR_ITEM_HOVER  = new Color(0x1C6560);
    public static final Color SIDEBAR_ITEM_ACTIVE = new Color(0x0F766E);
    public static final Color SIDEBAR_TEXT        = new Color(0xCCFBF1);
    public static final Color SIDEBAR_ACCENT      = new Color(0x5EEAD4);
    public static final Color DANGER              = new Color(0xDC2626);
    public static final Color DANGER_HOVER        = new Color(0xFEF2F2);
    public static final Color SUCCESS_LIGHT       = new Color(0xD1FAE5);
    public static final Color INFO_LIGHT          = new Color(0xDBEAFE);
    public static final Color OVERDUE_BG          = new Color(0xFEE2E2);
    public static final Color RETURNED_BG         = new Color(0xF8FAFC);
    public static final Color ROW_ALT             = new Color(0xF8FAFC);
    public static final Color HEADER_BG           = new Color(0xF1F5F9);
    public static final Color SELECTION           = new Color(0xCCFBF1);
    public static final Color SELECTION_FG        = new Color(0x134E4A);

    // Fonts
    private static final String[] FONT_CANDIDATES = {
        "Segoe UI", ".AppleSystemUIFont", "Ubuntu", "Noto Sans", "Dialog"
    };
    private static String SYS_FONT;

    public static String sysFont() {
        if (SYS_FONT != null) return SYS_FONT;
        HashSet<String> avail = new HashSet<>(Arrays.asList(
            GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames()));
        for (String f : FONT_CANDIDATES) {
            if (avail.contains(f)) { SYS_FONT = f; return f; }
        }
        SYS_FONT = "Dialog";
        return SYS_FONT;
    }

    public static Font regular(int size) { return new Font(sysFont(), Font.PLAIN, size); }
    public static Font bold(int size)    { return new Font(sysFont(), Font.BOLD,  size); }

    // Buttons
    public static JButton primaryBtn(String text)   { return makeBtn(text, PRIMARY,  Color.WHITE, PRIMARY_HOVER,       PRIMARY); }
    public static JButton secondaryBtn(String text) { return makeBtn(text, SURFACE,  TEXT,        new Color(0xF1F5F9), BORDER); }
    public static JButton dangerBtn(String text)    { return makeBtn(text, SURFACE,  DANGER,      DANGER_HOVER,        new Color(0xFCA5A5)); }

    private static JButton makeBtn(String text, Color bg, Color fg, Color hover, Color borderCol) {
        JButton b = new JButton(text) {
            private boolean ov;
            { addMouseListener(new MouseAdapter() {
                public void mouseEntered(MouseEvent e) { ov = true;  repaint(); }
                public void mouseExited (MouseEvent e) { ov = false; repaint(); }
            }); }
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(isEnabled() ? (ov ? hover : bg) : new Color(0xE2E8F0));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2.setColor(isEnabled() ? borderCol : BORDER);
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 8, 8);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        b.setFont(bold(13));
        b.setForeground(fg);
        b.setOpaque(false);
        b.setContentAreaFilled(false);
        b.setBorderPainted(false);
        b.setFocusPainted(false);
        b.setBorder(new EmptyBorder(7, 16, 7, 16));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return b;
    }

    // Input fields
    public static void styleField(JTextField f) {
        f.setFont(regular(13));
        f.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER, 1, true),
            new EmptyBorder(7, 10, 7, 10)));
        f.addFocusListener(new FocusAdapter() {
            public void focusGained(FocusEvent e) {
                f.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(PRIMARY, 1, true),
                    new EmptyBorder(7, 10, 7, 10)));
            }
            public void focusLost(FocusEvent e) {
                f.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(BORDER, 1, true),
                    new EmptyBorder(7, 10, 7, 10)));
            }
        });
    }

    // Table
    public static void styleTable(JTable t) {
        t.setFont(regular(13));
        t.setRowHeight(34);
        t.setShowHorizontalLines(true);
        t.setShowVerticalLines(false);
        t.setGridColor(BORDER);
        t.setSelectionBackground(SELECTION);
        t.setSelectionForeground(SELECTION_FG);
        t.setBackground(SURFACE);
        t.setFillsViewportHeight(true);

        JTableHeader h = t.getTableHeader();
        h.setFont(bold(12));
        h.setBackground(HEADER_BG);
        h.setForeground(TEXT_MUTED);
        h.setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, BORDER));
        h.setPreferredSize(new Dimension(h.getWidth(), 38));
        ((DefaultTableCellRenderer) h.getDefaultRenderer())
            .setHorizontalAlignment(SwingConstants.LEFT);

        t.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(
                    JTable tbl, Object v, boolean sel, boolean foc, int row, int col) {
                Component c = super.getTableCellRendererComponent(tbl, v, sel, foc, row, col);
                if (!sel) c.setBackground(row % 2 == 0 ? SURFACE : ROW_ALT);
                setFont(regular(13));
                setBorder(new EmptyBorder(0, 12, 0, 12));
                return c;
            }
        });
    }

    // Scroll bar
    public static void modernScrollBar(JScrollPane sp) {
        JScrollBar sb = sp.getVerticalScrollBar();
        sb.setPreferredSize(new Dimension(6, 0));
        sb.setUI(new BasicScrollBarUI() {
            @Override protected void configureScrollBarColors() {
                thumbColor = new Color(0xCBD5E1); trackColor = SURFACE;
            }
            @Override protected JButton createDecreaseButton(int o) { return zero(); }
            @Override protected JButton createIncreaseButton(int o) { return zero(); }
            private JButton zero() {
                JButton b = new JButton();
                b.setPreferredSize(new Dimension(0, 0));
                return b;
            }
            @Override protected void paintThumb(Graphics g, JComponent c, Rectangle r) {
                if (r.isEmpty()) return;
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(isThumbRollover() ? new Color(0x94A3B8) : thumbColor);
                g2.fillRoundRect(r.x + 1, r.y + 2, r.width - 2, r.height - 4, 6, 6);
                g2.dispose();
            }
        });
    }

    // Section header label (replaces TitledBorder)
    public static JLabel sectionLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(bold(13));
        l.setForeground(TEXT_MUTED);
        l.setBorder(new EmptyBorder(0, 0, 8, 0));
        return l;
    }

    private Theme() {}
}
