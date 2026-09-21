import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

public class VentanaPrincipal extends JFrame {

    private static final DateTimeFormatter FMT_FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter FMT_HORA = DateTimeFormatter.ofPattern("HH:mm");

    private final GestorSalas gestorSalas;
    private final GestorReservas gestorReservas;

    public VentanaPrincipal() {
        gestorSalas = new GestorSalas("data/salas.txt");
        gestorReservas = new GestorReservas("data/reservas.txt");

        setTitle("PointNow - Reserva de Working Points");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(650, 500);
        setLocationRelativeTo(null);

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Salas", crearPanelSalas());
        tabs.addTab("Registrar reserva", crearPanelRegistrar());
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
        JTextField txtCarnet = new JTextField(15);
        JTextField txtFecha = new JTextField("dd/MM/yyyy");
        JTextField txtHoraInicio = new JTextField("HH:mm");
        JTextField txtHoraFin = new JTextField("HH:mm");
        JLabel lblResultado = new JLabel(" ");
        lblResultado.setForeground(new Color(0, 110, 0));

        agregarFila(panel, c, 0, "Sala:", comboSala);
        agregarFila(panel, c, 1, "Carnet estudiante:", txtCarnet);
        agregarFila(panel, c, 2, "Fecha (dd/MM/yyyy):", txtFecha);
        agregarFila(panel, c, 3, "Hora inicio (HH:mm):", txtHoraInicio);
        agregarFila(panel, c, 4, "Hora fin (HH:mm):", txtHoraFin);

        JButton btnRegistrar = new JButton("Registrar reserva");
        btnRegistrar.addActionListener(e -> {
            try {
                int numeroSala = (int) comboSala.getSelectedItem();
                String carnet = txtCarnet.getText().trim();
                LocalDate fecha = LocalDate.parse(txtFecha.getText().trim(), FMT_FECHA);
                LocalTime horaInicio = LocalTime.parse(txtHoraInicio.getText().trim(), FMT_HORA);
                LocalTime horaFin = LocalTime.parse(txtHoraFin.getText().trim(), FMT_HORA);

                if (carnet.isEmpty()) {
                    lblResultado.setForeground(Color.RED);
                    lblResultado.setText("Debes ingresar el carnet del estudiante.");
                    return;
                }

                String id = "R" + System.currentTimeMillis() % 100000;
                Reserva nueva = new Reserva(id, numeroSala, carnet, fecha, horaInicio, horaFin, "Confirmada");
                boolean exito = gestorReservas.registrarReserva(nueva);

                if (exito) {
                    lblResultado.setForeground(new Color(0, 110, 0));
                    lblResultado.setText("Reserva " + id + " registrada con exito.");
                } else {
                    lblResultado.setForeground(Color.RED);
                    lblResultado.setText("No se pudo registrar: la sala ya esta reservada en ese horario.");
                }
            } catch (DateTimeParseException ex) {
                lblResultado.setForeground(Color.RED);
                lblResultado.setText("Formato de fecha/hora invalido. Usa dd/MM/yyyy y HH:mm.");
            }
        });

        c.gridx = 0; c.gridy = 5; c.gridwidth = 2; c.insets = new Insets(15, 5, 5, 5);
        panel.add(btnRegistrar, c);
        c.gridy = 6;
        panel.add(lblResultado, c);

        return panel;
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
                    lblResultado.setForeground(new Color(0, 110, 0));
                    lblResultado.setText("La sala " + numeroSala + " SI esta disponible en ese horario.");
                } else {
                    lblResultado.setForeground(Color.RED);
                    lblResultado.setText("La sala " + numeroSala + " NO esta disponible en ese horario.");
                }
            } catch (DateTimeParseException ex) {
                lblResultado.setForeground(Color.RED);
                lblResultado.setText("Formato de fecha/hora invalido. Usa dd/MM/yyyy y HH:mm.");
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
                lblResultado.setForeground(new Color(0, 110, 0));
                lblResultado.setText("Reserva " + id + " cancelada con exito.");
            } else {
                lblResultado.setForeground(Color.RED);
                lblResultado.setText("No se encontro la reserva o ya estaba cancelada.");
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
                lblResultado.setForeground(new Color(0, 110, 0));
                lblResultado.setText("La reserva " + id + " dura " + minutos + " minutos.");
            } else {
                lblResultado.setForeground(Color.RED);
                lblResultado.setText("No se encontro la reserva " + id + ".");
            }
        });

        c.gridx = 0; c.gridy = 1; c.gridwidth = 2; c.insets = new Insets(15, 5, 5, 5);
        panel.add(btnCalcular, c);
        c.gridy = 2;
        panel.add(lblResultado, c);

        return panel;
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