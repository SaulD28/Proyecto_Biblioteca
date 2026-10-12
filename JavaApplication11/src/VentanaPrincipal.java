import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class VentanaPrincipal extends JFrame {

    private static final DateTimeFormatter FMT_FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter FMT_HORA = DateTimeFormatter.ofPattern("HH:mm");

    private final GestorSalas gestorSalas;
    private final GestorReservas gestorReservas;
    private DefaultTableModel modeloReservas;

    public VentanaPrincipal() {
        gestorSalas = new GestorSalas("data/salas.txt");
        gestorReservas = new GestorReservas("data/reservas.txt");

        setTitle("PointNow - Reserva de Working Points");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(700, 520);
        setLocationRelativeTo(null);

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Salas", crearPanelSalas());
        tabs.addTab("Registrar reserva", crearPanelRegistrar());
        tabs.addTab("Ver reservas", crearPanelVerReservas());
        tabs.addTab("Consultar disponibilidad", crearPanelConsultar());
        tabs.addTab("Cancelar reserva", crearPanelCancelar());
        tabs.addTab("Calcular duracion", crearPanelDuracion());

        add(tabs);
    }

    private JPanel crearPanelSalas() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        String[] columnas = {"Numero", "Ubicacion", "Capacidad", "Estado"};
        DefaultTableModel modelo = new DefaultTableModel(columnas, 0);
        for (Sala s : gestorSalas.listarSalas()) {
            modelo.addRow(new Object[]{s.getNumero(), s.getUbicacion(), s.getCapacidad(), s.getEstado()});
        }
        JTable tabla = new JTable(modelo);
        panel.add(new JScrollPane(tabla), BorderLayout.CENTER);

        JButton btnRefrescar = new JButton("Refrescar listado");
        btnRefrescar.addActionListener(e -> {
            modelo.setRowCount(0);
            for (Sala s : gestorSalas.listarSalas()) {
                modelo.addRow(new Object[]{s.getNumero(), s.getUbicacion(), s.getCapacidad(), s.getEstado()});
            }
        });
        panel.add(btnRefrescar, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel crearPanelRegistrar() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        GridBagConstraints c = baseConstraints();

        JComboBox<Integer> comboSala = new JComboBox<>();
        for (Sala s : gestorSalas.listarSalas()) {
            comboSala.addItem(s.getNumero());
        }
        JTextField txtDpi = new JTextField(15);
        JTextField txtCantUsuarios = new JTextField(5);
        JTextField txtFecha = new JTextField("dd/MM/yyyy");
        JTextField txtHoraInicio = new JTextField("HH:mm");
        JTextField txtHoraFin = new JTextField("HH:mm");
        JLabel lblResultado = new JLabel(" ");

        agregarFila(panel, c, 0, "DPI o Carne:", txtDpi);
        agregarFila(panel, c, 1, "Cantidad de usuarios:", txtCantUsuarios);
        agregarFila(panel, c, 2, "Sala:", comboSala);
        agregarFila(panel, c, 3, "Fecha (dd/MM/yyyy):", txtFecha);
        agregarFila(panel, c, 4, "Hora inicio (HH:mm):", txtHoraInicio);
        agregarFila(panel, c, 5, "Hora fin (HH:mm):", txtHoraFin);

        // === BLOQUE COMMIT 3: inicio ===
        JLabel lblNota = new JLabel("Maximo permitido por reserva: " + GestorReservas.HORAS_MAXIMAS_POR_RESERVA + " horas.");
        lblNota.setFont(lblNota.getFont().deriveFont(Font.ITALIC, 11f));
        c.gridx = 0; c.gridy = 6; c.gridwidth = 2; c.insets = new Insets(2, 5, 10, 5);
        panel.add(lblNota, c);
        // === BLOQUE COMMIT 3: fin ===

        JButton btnRegistrar = new JButton("Registrar reserva");
        btnRegistrar.addActionListener(e -> {
            try {
                int numeroSala = (int) comboSala.getSelectedItem();
                String dpi = txtDpi.getText().trim();
                String fechaTexto = txtFecha.getText().trim();
                String horaInicioTexto = txtHoraInicio.getText().trim();
                String horaFinTexto = txtHoraFin.getText().trim();
                String cantTexto = txtCantUsuarios.getText().trim();

                if (dpi.isEmpty()) {
                    mostrarError(lblResultado, "Debes ingresar el DPI o carne.");
                    return;
                }
                if (cantTexto.isEmpty() || !cantTexto.matches("\\d+")) {
                    mostrarError(lblResultado, "La cantidad de usuarios debe ser un numero entero.");
                    return;
                }

                int cantidadUsuarios = Integer.parseInt(cantTexto);

                // === BLOQUE COMMIT 2: inicio ===
                Sala salaSeleccionada = gestorSalas.buscarSalaPorNumero(numeroSala);
                if (cantidadUsuarios < 1 || cantidadUsuarios > salaSeleccionada.getCapacidad()) {
                    mostrarError(lblResultado, "La sala " + numeroSala + " tiene capacidad para "
                            + salaSeleccionada.getCapacidad() + " personas.");
                    return;
                }
                // === BLOQUE COMMIT 2: fin ===

                LocalDate fecha = LocalDate.parse(fechaTexto, FMT_FECHA);
                LocalTime horaInicio = LocalTime.parse(horaInicioTexto, FMT_HORA);
                LocalTime horaFin = LocalTime.parse(horaFinTexto, FMT_HORA);

                String id = "R" + System.currentTimeMillis() % 100000;
                Reserva nueva = new Reserva(id, numeroSala, dpi, cantidadUsuarios, fecha, horaInicio, horaFin, "Confirmada");
                boolean exito = gestorReservas.registrarReserva(nueva);

                if (exito) {
                    mostrarExito(lblResultado, "Reserva " + id + " registrada con exito.");
                    refrescarTablaReservas();
                } else {
                    mostrarError(lblResultado, "No se pudo registrar la reserva. Revisa el horario (cruce o maximo de horas).");
                }
            } catch (DateTimeParseException ex) {
                mostrarError(lblResultado, "Formato de fecha/hora invalido. Usa dd/MM/yyyy y HH:mm.");
            }
        });

        c.gridx = 0; c.gridy = 7; c.gridwidth = 2; c.insets = new Insets(10, 5, 5, 5);
        panel.add(btnRegistrar, c);
        c.gridy = 8;
        panel.add(lblResultado, c);

        return panel;
    }

    private JPanel crearPanelVerReservas() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        String[] columnas = {"ID", "Sala", "DPI/Carne", "Usuarios", "Fecha", "Inicio", "Fin", "Duracion (min)", "Estado"};
        modeloReservas = new DefaultTableModel(columnas, 0);
        JTable tabla = new JTable(modeloReservas);
        panel.add(new JScrollPane(tabla), BorderLayout.CENTER);

        JButton btnRefrescar = new JButton("Refrescar listado");
        btnRefrescar.addActionListener(e -> refrescarTablaReservas());
        panel.add(btnRefrescar, BorderLayout.SOUTH);

        refrescarTablaReservas();
        return panel;
    }

    private void refrescarTablaReservas() {
        if (modeloReservas == null) return;
        modeloReservas.setRowCount(0);
        for (Reserva r : gestorReservas.getReservas()) {
            modeloReservas.addRow(new Object[]{
                    r.getIdReserva(), r.getNumeroSala(), r.getDpiCarnet(), r.getCantidadUsuarios(),
                    r.getFecha().format(FMT_FECHA), r.getHoraInicio().format(FMT_HORA),
                    r.getHoraFin().format(FMT_HORA), r.calcularDuracionMinutos(), r.getEstado()
            });
        }
    }

    private JPanel crearPanelConsultar() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        GridBagConstraints c = baseConstraints();

        JComboBox<Integer> comboSala = new JComboBox<>();
        for (Sala s : gestorSalas.listarSalas()) {
            comboSala.addItem(s.getNumero());
        }
        JTextField txtFecha = new JTextField("dd/MM/yyyy");
        JTextField txtHoraInicio = new JTextField("HH:mm");
        JTextField txtHoraFin = new JTextField("HH:mm");
        JLabel lblResultado = new JLabel(" ");

        agregarFila(panel, c, 0, "Sala:", comboSala);
        agregarFila(panel, c, 1, "Fecha (dd/MM/yyyy):", txtFecha);
        agregarFila(panel, c, 2, "Hora inicio (HH:mm):", txtHoraInicio);
        agregarFila(panel, c, 3, "Hora fin (HH:mm):", txtHoraFin);

        JButton btnConsultar = new JButton("Consultar disponibilidad");
        btnConsultar.addActionListener(e -> {
            try {
                int numeroSala = (int) comboSala.getSelectedItem();
                LocalDate fecha = LocalDate.parse(txtFecha.getText().trim(), FMT_FECHA);
                LocalTime horaInicio = LocalTime.parse(txtHoraInicio.getText().trim(), FMT_HORA);
                LocalTime horaFin = LocalTime.parse(txtHoraFin.getText().trim(), FMT_HORA);

                boolean disponible = gestorSalas.consultarDisponibilidad(
                        numeroSala, fecha, horaInicio, horaFin, gestorReservas.getReservas());

                if (disponible) {
                    mostrarExito(lblResultado, "La sala " + numeroSala + " SI esta disponible en ese horario.");
                } else {
                    mostrarError(lblResultado, "La sala " + numeroSala + " NO esta disponible en ese horario.");
                }
            } catch (DateTimeParseException ex) {
                mostrarError(lblResultado, "Formato de fecha/hora invalido. Usa dd/MM/yyyy y HH:mm.");
            }
        });

        c.gridx = 0; c.gridy = 4; c.gridwidth = 2; c.insets = new Insets(15, 5, 5, 5);
        panel.add(btnConsultar, c);
        c.gridy = 5;
        panel.add(lblResultado, c);

        return panel;
    }

    private JPanel crearPanelCancelar() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        GridBagConstraints c = baseConstraints();

        JTextField txtId = new JTextField(15);
        JLabel lblResultado = new JLabel(" ");

        agregarFila(panel, c, 0, "ID de la reserva:", txtId);

        JButton btnCancelar = new JButton("Cancelar reserva");
        btnCancelar.addActionListener(e -> {
            String id = txtId.getText().trim();
            boolean exito = gestorReservas.cancelarReserva(id);
            if (exito) {
                mostrarExito(lblResultado, "Reserva " + id + " cancelada con exito.");
                refrescarTablaReservas();
            } else {
                mostrarError(lblResultado, "No se encontro la reserva o ya estaba cancelada.");
            }
        });

        c.gridx = 0; c.gridy = 1; c.gridwidth = 2; c.insets = new Insets(15, 5, 5, 5);
        panel.add(btnCancelar, c);
        c.gridy = 2;
        panel.add(lblResultado, c);

        return panel;
    }

    private JPanel crearPanelDuracion() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        GridBagConstraints c = baseConstraints();

        JTextField txtId = new JTextField(15);
        JLabel lblResultado = new JLabel(" ");

        agregarFila(panel, c, 0, "ID de la reserva:", txtId);

        JButton btnCalcular = new JButton("Calcular duracion");
        btnCalcular.addActionListener(e -> {
            String id = txtId.getText().trim();
            long minutos = gestorSalas.calcularDuracionReserva(id, gestorReservas.getReservas());
            if (minutos >= 0) {
                mostrarExito(lblResultado, "La reserva " + id + " dura " + minutos + " minutos.");
            } else {
                mostrarError(lblResultado, "No se encontro la reserva " + id + ".");
            }
        });

        c.gridx = 0; c.gridy = 1; c.gridwidth = 2; c.insets = new Insets(15, 5, 5, 5);
        panel.add(btnCalcular, c);
        c.gridy = 2;
        panel.add(lblResultado, c);

        return panel;
    }

    private void mostrarExito(JLabel lbl, String texto) {
        lbl.setForeground(new Color(0, 110, 0));
        lbl.setText(texto);
    }

    private void mostrarError(JLabel lbl, String texto) {
        lbl.setForeground(Color.RED);
        lbl.setText(texto);
    }

    private GridBagConstraints baseConstraints() {
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(6, 5, 6, 5);
        c.anchor = GridBagConstraints.WEST;
        c.fill = GridBagConstraints.HORIZONTAL;
        return c;
    }

    private void agregarFila(JPanel panel, GridBagConstraints c, int fila, String etiqueta, JComponent campo) {
        c.gridx = 0; c.gridy = fila; c.gridwidth = 1; c.weightx = 0;
        panel.add(new JLabel(etiqueta), c);
        c.gridx = 1; c.weightx = 1;
        panel.add(campo, c);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            VentanaPrincipal ventana = new VentanaPrincipal();
            ventana.setVisible(true);
        });
    }
}