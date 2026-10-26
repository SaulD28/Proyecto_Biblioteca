import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * Colores, fuentes y componentes con estilo propio para toda la aplicacion.
 */
public final class Tema {

    public static final Color FONDO = new Color(0xF3F5F9);
    public static final Color TARJETA = Color.WHITE;
    public static final Color LATERAL = new Color(0x172033);
    public static final Color LATERAL_HOVER = new Color(0x24304A);
    public static final Color PRIMARIO = new Color(0x0F766E);
    public static final Color PRIMARIO_OSCURO = new Color(0x0B5A54);
    public static final Color PELIGRO = new Color(0xDC2626);
    public static final Color PELIGRO_OSCURO = new Color(0xB91C1C);
    public static final Color TEXTO = new Color(0x1F2937);
    public static final Color TEXTO_SUAVE = new Color(0x6B7280);
    public static final Color BORDE = new Color(0xE2E6EE);
    public static final Color SELECCION = new Color(0xD7F0EC);
    public static final Color EXITO = new Color(0x047857);
    public static final Color AVISO = new Color(0xB45309);

    public static final Font FUENTE = new Font("SansSerif", Font.PLAIN, 14);
    public static final Font FUENTE_NEGRITA = FUENTE.deriveFont(Font.BOLD);
    public static final Font FUENTE_PEQUENA = FUENTE.deriveFont(12f);
    public static final Font TITULO = FUENTE.deriveFont(Font.BOLD, 24f);
    public static final Font SUBTITULO = FUENTE.deriveFont(Font.BOLD, 16f);

    private Tema() {
    }

    public static void instalar() {
        UIManager.put("Label.font", FUENTE);
        UIManager.put("TextField.font", FUENTE);
        UIManager.put("ComboBox.font", FUENTE);
        UIManager.put("Spinner.font", FUENTE);
        UIManager.put("Table.font", FUENTE);
        UIManager.put("List.font", FUENTE);
        UIManager.put("OptionPane.messageFont", FUENTE);
        UIManager.put("OptionPane.buttonFont", FUENTE);
        UIManager.put("ScrollBar.width", 12);
    }

    public static Border margen(int arriba, int izq, int abajo, int der) {
        return BorderFactory.createEmptyBorder(arriba, izq, abajo, der);
    }

    public static Border bordeCampo() {
        return BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDE, 1, true),
                margen(6, 10, 6, 10));
    }

    // ---------------------------------------------------------------- Boton

    public static JButton boton(String texto, Color base, Color hover) {
        BotonRedondo b = new BotonRedondo(texto, base, hover);
        return b;
    }

    public static JButton botonPrimario(String texto) {
        return boton(texto, PRIMARIO, PRIMARIO_OSCURO);
    }

    public static JButton botonPeligro(String texto) {
        return boton(texto, PELIGRO, PELIGRO_OSCURO);
    }

    private static class BotonRedondo extends JButton {
        private final Color base;
        private final Color hover;
        private boolean encima;

        BotonRedondo(String texto, Color base, Color hover) {
            super(texto);
            this.base = base;
            this.hover = hover;
            setFont(FUENTE_NEGRITA);
            setForeground(Color.WHITE);
            setFocusPainted(false);
            setBorderPainted(false);
            setContentAreaFilled(false);
            setOpaque(false);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setBorder(margen(10, 20, 10, 20));
            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    encima = true;
                    repaint();
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    encima = false;
                    repaint();
                }
            });
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            Color c = !isEnabled() ? new Color(0xB8BFCC) : (encima ? hover : base);
            g2.setColor(c);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
            g2.dispose();
            super.paintComponent(g);
        }
    }

    // ------------------------------------------------------------- Tarjeta

    /** Panel blanco con bordes redondeados y un borde suave. */
    public static JPanel tarjeta(LayoutManager layout) {
        JPanel p = new JPanel(layout) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(TARJETA);
                g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 16, 16);
                g2.setColor(BORDE);
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 16, 16);
                g2.dispose();
            }
        };
        p.setOpaque(false);
        p.setBorder(margen(18, 20, 18, 20));
        return p;
    }

    // ------------------------------------------------------------- Campos

    public static void estilizarCampo(JComponent c) {
        c.setFont(FUENTE);
        c.setForeground(TEXTO);
        c.setBorder(bordeCampo());
        if (c instanceof JTextField) {
            c.setBackground(Color.WHITE);
        }
    }

    public static void estilizarSpinner(JSpinner s) {
        s.setBorder(BorderFactory.createLineBorder(BORDE, 1, true));
        JComponent editor = s.getEditor();
        if (editor instanceof JSpinner.DefaultEditor) {
            JTextField tf = ((JSpinner.DefaultEditor) editor).getTextField();
            tf.setFont(FUENTE);
            tf.setBorder(margen(6, 8, 6, 8));
        }
    }

    public static void estilizarCombo(JComboBox<?> combo) {
        combo.setFont(FUENTE);
        combo.setBackground(Color.WHITE);
        combo.setPreferredSize(new Dimension(combo.getPreferredSize().width, 36));
    }

    public static JLabel etiqueta(String texto) {
        JLabel l = new JLabel(texto);
        l.setFont(FUENTE_NEGRITA);
        l.setForeground(TEXTO);
        return l;
    }

    // -------------------------------------------------------------- Tabla

    public static void estilizarTabla(JTable tabla) {
        tabla.setFont(FUENTE);
        tabla.setForeground(TEXTO);
        tabla.setRowHeight(34);
        tabla.setShowVerticalLines(false);
        tabla.setShowHorizontalLines(true);
        tabla.setGridColor(new Color(0xEEF0F5));
        tabla.setIntercellSpacing(new Dimension(0, 1));
        tabla.setSelectionBackground(SELECCION);
        tabla.setSelectionForeground(TEXTO);
        tabla.setFillsViewportHeight(true);
        tabla.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        DefaultTableCellRenderer celda = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object v, boolean sel,
                                                            boolean foco, int fila, int col) {
                super.getTableCellRendererComponent(t, v, sel, false, fila, col);
                setBorder(margen(0, 10, 0, 10));
                if (!sel) {
                    setBackground(fila % 2 == 0 ? Color.WHITE : new Color(0xF9FAFC));
                }
                setForeground(TEXTO);
                String texto = v == null ? "" : v.toString();
                 if (texto.equals("Confirmada") || texto.equals("Disponible") || texto.equals("Asistio")) {
                    setForeground(EXITO);
                    setFont(FUENTE_NEGRITA);
                } else if (texto.equals("Liberada")) {
                    setForeground(AVISO);
                    setFont(FUENTE_NEGRITA);
                } else if (texto.equals("Cancelada") || texto.equals("Ocupada") || texto.equals("Inasistencia")) {
                    setForeground(PELIGRO);
                    setFont(FUENTE_NEGRITA);
                } else {
                    setFont(FUENTE);
                }
                return this;
            }
        };
        tabla.setDefaultRenderer(Object.class, celda);

        JTableHeader h = tabla.getTableHeader();
        h.setReorderingAllowed(false);
        h.setPreferredSize(new Dimension(0, 38));
        TableCellRenderer base = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object v, boolean sel,
                                                            boolean foco, int fila, int col) {
                JLabel l = (JLabel) super.getTableCellRendererComponent(t, v, false, false, fila, col);
                l.setBackground(new Color(0xEDF1F7));
                l.setForeground(TEXTO_SUAVE);
                l.setFont(FUENTE_PEQUENA.deriveFont(Font.BOLD));
                l.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createMatteBorder(0, 0, 1, 0, BORDE), margen(0, 10, 0, 10)));
                l.setText(v == null ? "" : v.toString().toUpperCase());
                return l;
            }
        };
        h.setDefaultRenderer(base);
    }

    public static JScrollPane scroll(JComponent c) {
        JScrollPane sp = new JScrollPane(c);
        sp.setBorder(BorderFactory.createLineBorder(BORDE, 1, true));
        sp.getViewport().setBackground(Color.WHITE);
        return sp;
    }
}