import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.awt.image.BufferedImage;

public class CalculadoraSeba extends JFrame {

    // ─── Paleta futurista ──────────────────────────────────────────────────────
    private static final Color BG_DARK        = new Color(0x07, 0x09, 0x14);
    private static final Color BG_PANEL       = new Color(0x0D, 0x12, 0x24);
    private static final Color NEON_CYAN      = new Color(0x00, 0xE5, 0xFF);
    private static final Color NEON_BLUE      = new Color(0x00, 0x7A, 0xFF);
    private static final Color NEON_PURPLE    = new Color(0x8A, 0x2B, 0xE2);
    private static final Color NEON_GREEN     = new Color(0x00, 0xFF, 0xAA);
    private static final Color BTN_DEFAULT    = new Color(0x0F, 0x17, 0x2E);
    private static final Color BTN_OPERATOR   = new Color(0x0A, 0x1A, 0x3A);
    private static final Color BTN_EQUAL      = new Color(0x00, 0x3A, 0x6E);
    private static final Color BTN_CLEAR      = new Color(0x2A, 0x06, 0x1A);
    private static final Color TEXT_PRIMARY   = new Color(0xE0, 0xF7, 0xFF);
    private static final Color TEXT_DIM       = new Color(0x60, 0x90, 0xAA);

    // ─── Estado de la calculadora ─────────────────────────────────────────────
    private double operand1    = 0;
    private double operand2    = 0;
    private String operator    = "";
    private boolean newInput   = true;
    private boolean hasResult  = false;

    // ─── Componentes UI ──────────────────────────────────────────────────────
    private JLabel lblExpression;
    private JLabel lblDisplay;
    private JPanel buttonPanel;

    // ─── Fuentes ─────────────────────────────────────────────────────────────
    private Font fontDisplay;
    private Font fontExpression;
    private Font fontBtn;
    private Font fontBtnSmall;

    // ─────────────────────────────────────────────────────────────────────────

    public CalculadoraSeba() {
        loadFonts();
        initUI();
    }

    private void loadFonts() {
        // Fuentes del sistema como fallback estilizado
        fontDisplay    = new Font("Courier New", Font.BOLD, 42);
        fontExpression = new Font("Courier New", Font.PLAIN, 14);
        fontBtn        = new Font("Courier New", Font.BOLD, 20);
        fontBtnSmall   = new Font("Courier New", Font.BOLD, 15);
    }

    private void initUI() {
        setTitle("CALCULADORA · SEBA v2.0");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);
        setBackground(BG_DARK);

        // Icono de la ventana (circulo neon)
        setIconImage(createIcon());

        JPanel root = new JPanel(new BorderLayout(0, 0)) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(BG_DARK);
                g2.fillRect(0, 0, getWidth(), getHeight());
                // líneas de grid futuristas
                g2.setStroke(new BasicStroke(0.4f));
                g2.setColor(new Color(0x00, 0x80, 0xFF, 18));
                for (int x = 0; x < getWidth(); x += 28)
                    g2.drawLine(x, 0, x, getHeight());
                for (int y = 0; y < getHeight(); y += 28)
                    g2.drawLine(0, y, getWidth(), y);
                g2.dispose();
            }
        };
        root.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

        // ── Display ──────────────────────────────────────────────────────────
        JPanel displayPanel = createDisplayPanel();
        root.add(displayPanel, BorderLayout.NORTH);

        // ── Botones ───────────────────────────────────────────────────────────
        buttonPanel = createButtonPanel();
        root.add(buttonPanel, BorderLayout.CENTER);

        // ── Firma ─────────────────────────────────────────────────────────────
        JLabel sig = new JLabel("◈  SEBA · FUTURIST CALC  ◈", SwingConstants.CENTER);
        sig.setFont(new Font("Courier New", Font.PLAIN, 11));
        sig.setForeground(new Color(NEON_CYAN.getRed(), NEON_CYAN.getGreen(), NEON_CYAN.getBlue(), 90));
        sig.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
        root.add(sig, BorderLayout.SOUTH);

        add(root);
        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }

    private JPanel createDisplayPanel() {
        JPanel panel = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                // fondo panel
                g2.setColor(BG_PANEL);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 16, 16);
                // borde neon
                g2.setStroke(new BasicStroke(1.5f));
                GradientPaint gp = new GradientPaint(0, 0, NEON_CYAN, getWidth(), getHeight(), NEON_PURPLE);
                g2.setPaint(gp);
                g2.drawRoundRect(1, 1, getWidth() - 2, getHeight() - 2, 16, 16);
                // línea separadora inferior
                g2.setColor(new Color(NEON_CYAN.getRed(), NEON_CYAN.getGreen(), NEON_CYAN.getBlue(), 60));
                g2.drawLine(20, getHeight() - 2, getWidth() - 20, getHeight() - 2);
                g2.dispose();
            }
        };
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createEmptyBorder(14, 20, 14, 20));
        panel.setPreferredSize(new Dimension(380, 110));

        // Expresión (operando anterior + operador)
        lblExpression = new JLabel("", SwingConstants.RIGHT);
        lblExpression.setFont(fontExpression);
        lblExpression.setForeground(TEXT_DIM);
        lblExpression.setPreferredSize(new Dimension(340, 22));

        // Display principal
        lblDisplay = new JLabel("0", SwingConstants.RIGHT) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                // efecto glow en el texto
                String text = getText();
                FontMetrics fm = g2.getFontMetrics(getFont());
                int tw = fm.stringWidth(text);
                int x  = getWidth() - tw - 2;
                int y  = getHeight() - fm.getDescent() - 4;
                g2.setFont(getFont());
                // sombra glow
                for (int i = 6; i >= 1; i--) {
                    g2.setColor(new Color(NEON_CYAN.getRed(), NEON_CYAN.getGreen(), NEON_CYAN.getBlue(), 12 * i));
                    g2.drawString(text, x - i, y);
                    g2.drawString(text, x + i, y);
                    g2.drawString(text, x, y - i);
                    g2.drawString(text, x, y + i);
                }
                g2.setColor(TEXT_PRIMARY);
                g2.drawString(text, x, y);
                g2.dispose();
            }
        };
        lblDisplay.setFont(fontDisplay);
        lblDisplay.setForeground(TEXT_PRIMARY);
        lblDisplay.setPreferredSize(new Dimension(340, 60));

        panel.add(lblExpression, BorderLayout.NORTH);
        panel.add(lblDisplay,    BorderLayout.CENTER);
        return panel;
    }

    private JPanel createButtonPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createEmptyBorder(12, 0, 0, 0));
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(5, 5, 5, 5);
        c.fill   = GridBagConstraints.BOTH;
        c.weightx = 1;
        c.weighty = 1;

        // Layout: fila x col
        // [ C ]  [ CE ]  [ ← ]  [ ÷ ]
        // [ 7 ]  [ 8  ]  [ 9 ]  [ × ]
        // [ 4 ]  [ 5  ]  [ 6 ]  [ − ]
        // [ 1 ]  [ 2  ]  [ 3 ]  [ + ]
        // [ ± ]  [ 0  ]  [ . ]  [ = ]

        Object[][] layout = {
            { "C",   BTN_CLEAR,    NEON_PURPLE, 1 },
            { "CE",  BTN_CLEAR,    NEON_PURPLE, 1 },
            { "←",   BTN_CLEAR,    NEON_PURPLE, 1 },
            { "÷",   BTN_OPERATOR, NEON_CYAN,   1 },

            { "7",   BTN_DEFAULT,  TEXT_PRIMARY, 1 },
            { "8",   BTN_DEFAULT,  TEXT_PRIMARY, 1 },
            { "9",   BTN_DEFAULT,  TEXT_PRIMARY, 1 },
            { "×",   BTN_OPERATOR, NEON_CYAN,    1 },

            { "4",   BTN_DEFAULT,  TEXT_PRIMARY, 1 },
            { "5",   BTN_DEFAULT,  TEXT_PRIMARY, 1 },
            { "6",   BTN_DEFAULT,  TEXT_PRIMARY, 1 },
            { "−",   BTN_OPERATOR, NEON_CYAN,    1 },

            { "1",   BTN_DEFAULT,  TEXT_PRIMARY, 1 },
            { "2",   BTN_DEFAULT,  TEXT_PRIMARY, 1 },
            { "3",   BTN_DEFAULT,  TEXT_PRIMARY, 1 },
            { "+",   BTN_OPERATOR, NEON_CYAN,    1 },

            { "±",   BTN_DEFAULT,  NEON_GREEN,   1 },
            { "0",   BTN_DEFAULT,  TEXT_PRIMARY, 1 },
            { ".",   BTN_DEFAULT,  TEXT_PRIMARY, 1 },
            { "=",   BTN_EQUAL,    NEON_GREEN,   1 },
        };

        int col = 0, row = 0;
        for (Object[] btn : layout) {
            String label  = (String) btn[0];
            Color  bg     = (Color)  btn[1];
            Color  fg     = (Color)  btn[2];

            c.gridx      = col;
            c.gridy      = row;
            c.gridwidth  = 1;
            c.gridheight = 1;

            panel.add(makeButton(label, bg, fg), c);

            col++;
            if (col == 4) { col = 0; row++; }
        }
        return panel;
    }

    private JButton makeButton(String label, Color bgColor, Color fgColor) {
        JButton btn = new JButton(label) {
            private boolean hovered = false;
            private boolean pressed = false;

            {
                addMouseListener(new MouseAdapter() {
                    @Override public void mouseEntered(MouseEvent e) { hovered = true;  repaint(); }
                    @Override public void mouseExited (MouseEvent e) { hovered = false; repaint(); }
                    @Override public void mousePressed(MouseEvent e) { pressed = true;  repaint(); }
                    @Override public void mouseReleased(MouseEvent e){ pressed = false; repaint(); }
                });
            }

            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                Color base = bgColor;
                if (pressed)       base = base.brighter().brighter();
                else if (hovered)  base = base.brighter();

                // fondo redondeado
                g2.setColor(base);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);

                // borde superior brillante (efecto bevel futurista)
                g2.setStroke(new BasicStroke(1f));
                if (hovered || pressed) {
                    GradientPaint gp = new GradientPaint(
                        0, 0, fgColor,
                        getWidth(), getHeight(), new Color(fgColor.getRed(), fgColor.getGreen(), fgColor.getBlue(), 40)
                    );
                    g2.setPaint(gp);
                } else {
                    g2.setColor(new Color(fgColor.getRed(), fgColor.getGreen(), fgColor.getBlue(), 70));
                }
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 12, 12);

                // línea highlight top
                g2.setStroke(new BasicStroke(1f));
                g2.setColor(new Color(255, 255, 255, hovered ? 40 : 18));
                g2.drawLine(6, 1, getWidth() - 6, 1);

                // texto con glow
                FontMetrics fm = g2.getFontMetrics(getFont());
                int tx = (getWidth()  - fm.stringWidth(getText())) / 2;
                int ty = (getHeight() + fm.getAscent() - fm.getDescent()) / 2;
                if (hovered) {
                    for (int i = 3; i >= 1; i--) {
                        g2.setColor(new Color(fgColor.getRed(), fgColor.getGreen(), fgColor.getBlue(), 30 * i));
                        g2.setFont(getFont());
                        g2.drawString(getText(), tx - i, ty);
                        g2.drawString(getText(), tx + i, ty);
                        g2.drawString(getText(), tx, ty - i);
                        g2.drawString(getText(), tx, ty + i);
                    }
                }
                g2.setColor(fgColor);
                g2.setFont(getFont());
                g2.drawString(getText(), tx, ty);
                g2.dispose();
            }
        };

        boolean isSmall = label.length() > 1 && !label.equals("CE") ? true : false;
        btn.setFont(label.length() > 1 ? fontBtnSmall : fontBtn);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setOpaque(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(82, 72));

        btn.addActionListener(e -> onButtonClick(label));
        return btn;
    }

    // ─── Lógica de la calculadora ─────────────────────────────────────────────

    private void onButtonClick(String cmd) {
        switch (cmd) {
            case "C"  -> clearAll();
            case "CE" -> clearEntry();
            case "←"  -> backspace();
            case "±"  -> toggleSign();
            case "."  -> appendDecimal();
            case "+"  -> setOperator("+");
            case "−"  -> setOperator("-");
            case "×"  -> setOperator("*");
            case "÷"  -> setOperator("/");
            case "="  -> compute();
            default   -> appendDigit(cmd);
        }
    }

    private String getDisplay() { return lblDisplay.getText(); }

    private void setDisplay(String val) {
        // ajuste de fuente si el número es muy largo
        if (val.length() > 12)      lblDisplay.setFont(new Font("Courier New", Font.BOLD, 26));
        else if (val.length() > 9)  lblDisplay.setFont(new Font("Courier New", Font.BOLD, 34));
        else                        lblDisplay.setFont(fontDisplay);
        lblDisplay.setText(val);
    }

    private void clearAll() {
        operand1 = 0; operand2 = 0; operator = "";
        newInput = true; hasResult = false;
        setDisplay("0");
        lblExpression.setText("");
    }

    private void clearEntry() {
        setDisplay("0");
        newInput = true;
    }

    private void backspace() {
        String cur = getDisplay();
        if (newInput || cur.equals("Error")) { setDisplay("0"); return; }
        if (cur.length() <= 1 || (cur.length() == 2 && cur.startsWith("-"))) {
            setDisplay("0"); newInput = true;
        } else {
            setDisplay(cur.substring(0, cur.length() - 1));
        }
    }

    private void toggleSign() {
        String cur = getDisplay();
        if (cur.equals("0") || cur.equals("Error")) return;
        if (cur.startsWith("-")) setDisplay(cur.substring(1));
        else                     setDisplay("-" + cur);
    }

    private void appendDecimal() {
        if (newInput) { setDisplay("0."); newInput = false; return; }
        if (!getDisplay().contains(".")) setDisplay(getDisplay() + ".");
    }

    private void appendDigit(String digit) {
        if (newInput) { setDisplay(digit); newInput = false; }
        else {
            String cur = getDisplay();
            if (cur.equals("0")) setDisplay(digit);
            else                 setDisplay(cur + digit);
        }
        hasResult = false;
    }

    private void setOperator(String op) {
        if (!operator.isEmpty() && !newInput) compute();
        operand1 = parseDisplay();
        operator = op;
        newInput = true;
        String dispOp = switch(op) { case "+" -> "+"; case "-" -> "−"; case "*" -> "×"; default -> "÷"; };
        lblExpression.setText(formatNumber(operand1) + "  " + dispOp);
    }

    private void compute() {
        if (operator.isEmpty()) return;
        operand2 = parseDisplay();
        String dispOp = switch(operator) { case "+" -> "+"; case "-" -> "−"; case "*" -> "×"; default -> "÷"; };
        lblExpression.setText(formatNumber(operand1) + "  " + dispOp + "  " + formatNumber(operand2) + "  =");

        double result;
        if (operator.equals("/") && operand2 == 0) {
            setDisplay("Error");
            operator = ""; newInput = true; hasResult = true;
            return;
        }
        result = switch (operator) {
            case "+" -> operand1 + operand2;
            case "-" -> operand1 - operand2;
            case "*" -> operand1 * operand2;
            case "/" -> operand1 / operand2;
            default  -> 0;
        };

        operand1  = result;
        operator  = "";
        newInput  = true;
        hasResult = true;
        setDisplay(formatNumber(result));
    }

    private double parseDisplay() {
        try { return Double.parseDouble(getDisplay()); }
        catch (NumberFormatException e) { return 0; }
    }

    private String formatNumber(double n) {
        if (n == Math.floor(n) && !Double.isInfinite(n) && Math.abs(n) < 1e15)
            return String.valueOf((long) n);
        // notación si es muy grande
        if (Math.abs(n) >= 1e15 || (Math.abs(n) < 1e-6 && n != 0))
            return String.format("%.4e", n);
        // quitar ceros finales
        String s = String.format("%.10f", n);
        s = s.replaceAll("0+$", "").replaceAll("\\.$", "");
        return s;
    }

    // ─── Icono de la app ──────────────────────────────────────────────────────

    private Image createIcon() {
        BufferedImage img = new BufferedImage(64, 64, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = img.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(BG_DARK);
        g2.fillOval(0, 0, 64, 64);
        g2.setStroke(new BasicStroke(3f));
        g2.setColor(NEON_CYAN);
        g2.drawOval(2, 2, 60, 60);
        g2.setFont(new Font("Courier New", Font.BOLD, 30));
        g2.setColor(NEON_CYAN);
        g2.drawString("∑", 14, 44);
        g2.dispose();
        return img;
    }

    // ─── Main ──────────────────────────────────────────────────────────────────

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
        } catch (Exception ignored) {}

        SwingUtilities.invokeLater(() -> new CalculadoraSeba());
    }
}
