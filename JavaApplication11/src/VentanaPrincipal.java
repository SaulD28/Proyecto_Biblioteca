import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Pattern;

public class VentanaPrincipal extends JFrame {

    private static final DateTimeFormatter FMT_FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter FMT_HORA = DateTimeFormatter.ofPattern("HH:mm");

    private final GestorSalas gestorSalas;
    private final GestorReservas gestorReservas;
    private final GestorEstudiantes gestorEstudiantes;

    private DefaultTableModel modeloReservas;
    private DefaultTableModel modeloEstudiantes;
    private final DefaultListModel<Reserva> listaInasistencias = new DefaultListModel<>();
    private final DefaultListModel<Reserva> listaCheckIn = new DefaultListModel<>();

    // Historial de un estudiante
    private final DefaultComboBoxModel<Estudiante> modeloComboHistorial = new DefaultComboBoxModel<>();
    private final JComboBox<Estudiante> comboHistorial = new JComboBox<>(modeloComboHistorial);
    private final JLabel lblResumenHistorial = new JLabel(" ");
    private final DefaultTableModel modeloHistorial = new DefaultTableModel(
            new String[]{"Sala", "Fecha", "Inicio", "Fin", "Duracion (min)", "Estado"}, 0) {
        @Override
        public boolean isCellEditable(int r, int c) {
            return false;
        }
    };
    private final DefaultListModel<Reserva> listaCancelar = new DefaultListModel<>();
    private final DefaultListModel<Reserva> listaDuracion = new DefaultListModel<>();

    private final CardLayout cartas = new CardLayout();
    private final JPanel contenido = new JPanel(cartas);
    private final List<JButton> botonesMenu = new ArrayList<>();

    public VentanaPrincipal() {
        Tema.instalar();
        gestorSalas = new GestorSalas("data/salas.txt");
        gestorReservas = new GestorReservas("data/reservas.txt");
        gestorEstudiantes = new GestorEstudiantes("data/estudiantes.txt");

        setTitle("PointNow - Reserva de Working Points");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1060, 660);
        setMinimumSize(new Dimension(900, 580));
        setLocationRelativeTo(null);

        JPanel raiz = new JPanel(new BorderLayout());
        raiz.setBackground(Tema.FONDO);
        raiz.add(crearMenuLateral(), BorderLayout.WEST);

        contenido.setBackground(Tema.FONDO);
        contenido.add(crearPanelSalas(), "salas");
        contenido.add(crearPanelEstudiantes(), "estudiantes");
        contenido.add(crearPanelHistorial(), "historial");
        contenido.add(crearPanelRegistrar(), "registrar");
        contenido.add(crearPanelVerReservas(), "reservas");
        contenido.add(crearPanelCancelar(), "cancelar");
        contenido.add(crearPanelInasistencias(), "inasistencias");
        contenido.add(crearPanelDuracion(), "duracion");
        contenido.add(crearPanelCheckIn(), "checkin");
        raiz.add(contenido, BorderLayout.CENTER);

        setContentPane(raiz);
        refrescarReservas();
        mostrar("salas", 0);
        
         new javax.swing.Timer(30000, e -> {
            if (!gestorReservas.liberarVencidas(LocalDateTime.now()).isEmpty()) {
                refrescarReservas();
            }
        }).start();
    }

    // ------------------------------------------------------------ Navegacion

    private JPanel crearMenuLateral() {
        JPanel menu = new JPanel();
        menu.setLayout(new BoxLayout(menu, BoxLayout.Y_AXIS));
        menu.setBackground(Tema.LATERAL);
        menu.setBorder(Tema.margen(24, 14, 24, 14));
        menu.setPreferredSize(new Dimension(230, 0));

        JLabel logo = new JLabel("PointNow");
        logo.setFont(Tema.TITULO.deriveFont(26f));
        logo.setForeground(Color.WHITE);
        logo.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel sub = new JLabel("Working Points de la biblioteca");
        sub.setFont(Tema.FUENTE_PEQUENA);
        sub.setForeground(new Color(0x9AA6BF));
        sub.setAlignmentX(Component.LEFT_ALIGNMENT);
        JPanel cab = new JPanel(new GridLayout(2, 1, 0, 2));
        cab.setOpaque(false);
        cab.setBorder(Tema.margen(0, 8, 24, 0));
        cab.add(logo);
        cab.add(sub);
        cab.setAlignmentX(Component.LEFT_ALIGNMENT);
        cab.setMaximumSize(new Dimension(Integer.MAX_VALUE, 70));
        menu.add(cab);

        String[][] items = {
                {"Salas", "salas"},
                {"Estudiantes", "estudiantes"},
                {"Historial", "historial"},
                {"Nueva reserva", "registrar"},
                {"Reservas", "reservas"},
                {"Cancelar reserva", "cancelar"},
                {"Check-in", "checkin"},
                {"Inasistencias", "inasistencias"},
                {"Duracion de reserva", "duracion"}
                
        };
        for (int i = 0; i < items.length; i++) {
            final int indice = i;
            final String clave = items[i][1];
            JButton b = new JButton(items[i][0]);
            b.setFont(Tema.FUENTE);
            b.setForeground(new Color(0xD5DBEA));
            b.setHorizontalAlignment(SwingConstants.LEFT);
            b.setFocusPainted(false);
            b.setBorderPainted(false);
            b.setContentAreaFilled(false);
            b.setOpaque(true);
            b.setBackground(Tema.LATERAL);
            b.setBorder(Tema.margen(12, 16, 12, 16));
            b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            b.setAlignmentX(Component.LEFT_ALIGNMENT);
            b.setMaximumSize(new Dimension(Integer.MAX_VALUE, 46));
            b.addActionListener(e -> mostrar(clave, indice));
            botonesMenu.add(b);
            menu.add(b);
            menu.add(Box.createVerticalStrut(4));
        }
        menu.add(Box.createVerticalGlue());
        return menu;
    }

    private void mostrar(String clave, int indice) {
        cartas.show(contenido, clave);
        for (int i = 0; i < botonesMenu.size(); i++) {
            JButton b = botonesMenu.get(i);
            boolean activo = i == indice;
            b.setBackground(activo ? Tema.PRIMARIO : Tema.LATERAL);
            b.setForeground(activo ? Color.WHITE : new Color(0xD5DBEA));
            b.setFont(activo ? Tema.FUENTE_NEGRITA : Tema.FUENTE);
        }
        if (!clave.equals("registrar")) {
            refrescarReservas();
        }
    }

    /** Estructura comun de cada pantalla: titulo, descripcion y cuerpo. */
    private JPanel pagina(String titulo, String descripcion, JComponent cuerpo) {
        JPanel p = new JPanel(new BorderLayout(0, 18));
        p.setBackground(Tema.FONDO);
        p.setBorder(Tema.margen(26, 30, 26, 30));

        JLabel t = new JLabel(titulo);
        t.setFont(Tema.TITULO);
        t.setForeground(Tema.TEXTO);
        JLabel d = new JLabel(descripcion);
        d.setFont(Tema.FUENTE);
        d.setForeground(Tema.TEXTO_SUAVE);
        JPanel cab = new JPanel(new GridLayout(2, 1, 0, 4));
        cab.setOpaque(false);
        cab.add(t);
        cab.add(d);

        p.add(cab, BorderLayout.NORTH);
        p.add(cuerpo, BorderLayout.CENTER);
        return p;
    }

    // ----------------------------------------------------------------- Salas

    private JPanel crearPanelSalas() {
        String[] columnas = {"Numero", "Ubicacion", "Capacidad", "Estado"};
        DefaultTableModel modelo = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };
        for (Sala s : gestorSalas.listarSalas()) {
            modelo.addRow(new Object[]{s.getNumero(), s.getUbicacion(), s.getCapacidad(), s.getEstado()});
        }
        JTable tabla = new JTable(modelo);
        Tema.estilizarTabla(tabla);
        if (modelo.getRowCount() > 0) {
            tabla.setRowSelectionInterval(0, 0);
        }

        // --- Consulta de disponibilidad (usa la sala seleccionada en la tabla)
        JLabel lblSala = new JLabel();
        lblSala.setFont(Tema.SUBTITULO);
        lblSala.setForeground(Tema.PRIMARIO);
        JSpinner spFecha = crearSpinnerFecha();
        JSpinner spInicio = crearSpinnerHora(9, 0);
        JSpinner spFin = crearSpinnerHora(10, 0);
        JLabel lblResultado = new JLabel(" ");
        lblResultado.setFont(Tema.FUENTE_NEGRITA);

        Runnable actualizarTitulo = () -> {
            int fila = tabla.getSelectedRow();
            lblSala.setText(fila < 0 ? "Selecciona una sala en la tabla"
                    : "Disponibilidad de la sala " + modelo.getValueAt(fila, 0));
            lblResultado.setText(" ");
        };
        tabla.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                actualizarTitulo.run();
            }
        });
        actualizarTitulo.run();

        JButton btnConsultar = Tema.botonPrimario("Consultar disponibilidad");
        btnConsultar.addActionListener(e -> {
            int fila = tabla.getSelectedRow();
            if (fila < 0) {
                mostrarError(lblResultado, "Selecciona una sala de la tabla.");
                return;
            }
            int numeroSala = (int) modelo.getValueAt(fila, 0);
            LocalDate fecha = leerFecha(spFecha);
            LocalTime inicio = leerHora(spInicio);
            LocalTime fin = leerHora(spFin);
            if (!fin.isAfter(inicio)) {
                mostrarError(lblResultado, "La hora de fin debe ser posterior a la de inicio.");
                return;
            }
            boolean libre = gestorSalas.consultarDisponibilidad(
                    numeroSala, fecha, inicio, fin, gestorReservas.getReservas());
            if (libre) {
                mostrarExito(lblResultado, "La sala " + numeroSala + " esta DISPONIBLE en ese horario.");
            } else {
                mostrarError(lblResultado, "La sala " + numeroSala + " esta OCUPADA en ese horario.");
            }
        });

        JPanel filaCampos = new JPanel(new GridBagLayout());
        filaCampos.setOpaque(false);
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(0, 0, 0, 14);
        c.anchor = GridBagConstraints.SOUTHWEST;
        agregarCampoVertical(filaCampos, c, 0, "Fecha", spFecha);
        agregarCampoVertical(filaCampos, c, 1, "Hora inicio", spInicio);
        agregarCampoVertical(filaCampos, c, 2, "Hora fin", spFin);
        c.gridx = 3; c.gridy = 1; c.weightx = 0;
        filaCampos.add(btnConsultar, c);
        c.gridx = 4; c.weightx = 1; c.fill = GridBagConstraints.HORIZONTAL;
        filaCampos.add(lblResultado, c);

        JPanel consulta = Tema.tarjeta(new BorderLayout(0, 12));
        consulta.add(lblSala, BorderLayout.NORTH);
        consulta.add(filaCampos, BorderLayout.CENTER);

        JPanel cuerpo = new JPanel(new BorderLayout(0, 16));
        cuerpo.setOpaque(false);
        cuerpo.add(Tema.scroll(tabla), BorderLayout.CENTER);
        cuerpo.add(consulta, BorderLayout.SOUTH);

        return pagina("Salas", "Selecciona una sala y consulta si esta libre en la fecha y horario que necesitas.", cuerpo);
    }

    private void agregarCampoVertical(JPanel panel, GridBagConstraints c, int col, String texto, JComponent campo) {
        c.gridx = col; c.gridy = 0; c.weightx = 0; c.fill = GridBagConstraints.NONE;
        JLabel l = new JLabel(texto);
        l.setFont(Tema.FUENTE_PEQUENA);
        l.setForeground(Tema.TEXTO_SUAVE);
        panel.add(l, c);
        c.gridy = 1;
        panel.add(campo, c);
    }

    // ------------------------------------------------------------ Estudiantes

    private JPanel crearPanelEstudiantes() {
        String[] columnas = {"Carne / DPI", "Nombre", "Correo", "Inasistencias"};
        modeloEstudiantes = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };
        DefaultTableModel modelo = modeloEstudiantes;
        refrescarEstudiantes();

        JTable tabla = new JTable(modelo);
        Tema.estilizarTabla(tabla);
        int[] anchos = {150, 250, 260, 110};
        for (int i = 0; i < anchos.length; i++) {
            tabla.getColumnModel().getColumn(i).setPreferredWidth(anchos[i]);
        }

        JTextField txtCarne = new JTextField(18);
        Tema.estilizarCampo(txtCarne);
        JTextField txtNombre = new JTextField(18);
        Tema.estilizarCampo(txtNombre);
        JTextField txtCorreo = new JTextField(18);
        Tema.estilizarCampo(txtCorreo);
        JLabel lblResultado = new JLabel(" ");
        lblResultado.setFont(Tema.FUENTE_NEGRITA);

        JButton btnRegistrar = Tema.botonPrimario("Registrar estudiante");
        btnRegistrar.addActionListener(e -> {
            String error = gestorEstudiantes.registrarEstudiante(
                    new Estudiante(txtCarne.getText(), txtNombre.getText(), txtCorreo.getText()));
            if (error == null) {
                mostrarExito(lblResultado, "Estudiante registrado con exito.");
                txtCarne.setText("");
                txtNombre.setText("");
                txtCorreo.setText("");
                refrescarEstudiantes();
            } else {
                mostrarError(lblResultado, error);
            }
        });

        JPanel form = Tema.tarjeta(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(8, 6, 8, 6);
        c.anchor = GridBagConstraints.WEST;
        c.fill = GridBagConstraints.HORIZONTAL;
        agregarFila(form, c, 0, "DPI o carne", txtCarne);
        agregarFila(form, c, 1, "Nombre completo", txtNombre);
        agregarFila(form, c, 2, "Correo", txtCorreo);

        c.gridx = 1; c.gridy = 3; c.weightx = 1; c.fill = GridBagConstraints.NONE;
        form.add(btnRegistrar, c);
        // ancho minimo para que un mensaje largo no cambie el tamano de los campos
        lblResultado.setPreferredSize(new Dimension(10, 24));
        c.gridy = 4; c.fill = GridBagConstraints.HORIZONTAL;
        form.add(lblResultado, c);

        JPanel listado = new JPanel(new BorderLayout(0, 12));
        listado.setOpaque(false);
        listado.add(crearBarraFiltro("Buscar por carne:", tabla, modelo, 0), BorderLayout.NORTH);
        listado.add(Tema.scroll(tabla), BorderLayout.CENTER);

        JPanel cuerpo = new JPanel(new BorderLayout(0, 16));
        cuerpo.setOpaque(false);
        cuerpo.add(form, BorderLayout.NORTH);
        cuerpo.add(listado, BorderLayout.CENTER);

        return pagina("Estudiantes", "Registra a los estudiantes y consulta el listado.", cuerpo);
    }

    // -------------------------------------------------------------- Historial

    private JPanel crearPanelHistorial() {
        comboHistorial.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> l, Object v, int i, boolean sel, boolean foco) {
                super.getListCellRendererComponent(l, v, i, sel, foco);
                if (v instanceof Estudiante) {
                    Estudiante e = (Estudiante) v;
                    setText(e.getNombre() + "  -  " + e.getCarne());
                }
                setBorder(Tema.margen(5, 10, 5, 10));
                return this;
            }
        });
        Tema.estilizarCombo(comboHistorial);
        comboHistorial.addActionListener(e -> mostrarHistorial());

        lblResumenHistorial.setFont(Tema.FUENTE_NEGRITA);
        lblResumenHistorial.setForeground(Tema.PRIMARIO);

        JPanel seleccion = Tema.tarjeta(new BorderLayout(0, 12));
        JPanel fila = new JPanel(new BorderLayout(12, 0));
        fila.setOpaque(false);
        fila.add(Tema.etiqueta("Estudiante:"), BorderLayout.WEST);
        fila.add(comboHistorial, BorderLayout.CENTER);
        seleccion.add(fila, BorderLayout.NORTH);
        seleccion.add(lblResumenHistorial, BorderLayout.CENTER);

        JTable tabla = new JTable(modeloHistorial);
        Tema.estilizarTabla(tabla);

        JPanel cuerpo = new JPanel(new BorderLayout(0, 16));
        cuerpo.setOpaque(false);
        cuerpo.add(seleccion, BorderLayout.NORTH);
        cuerpo.add(Tema.scroll(tabla), BorderLayout.CENTER);

        return pagina("Historial", "Elige a un estudiante para ver todas sus reservas.", cuerpo);
    }

    /** Recarga la lista de estudiantes del historial conservando la seleccion. */
    private void refrescarHistorial() {
        Estudiante seleccionado = (Estudiante) comboHistorial.getSelectedItem();
        String carneSeleccionado = seleccionado == null ? null : seleccionado.getCarne();
        Estudiante volver = null;
        modeloComboHistorial.removeAllElements();
        for (Estudiante e : gestorEstudiantes.listarEstudiantes()) {
            modeloComboHistorial.addElement(e);
            if (e.getCarne().equals(carneSeleccionado)) {
                volver = e;
            }
        }
        if (volver != null) {
            modeloComboHistorial.setSelectedItem(volver);
        }
        mostrarHistorial();
    }

    private void mostrarHistorial() {
        modeloHistorial.setRowCount(0);
        Estudiante e = (Estudiante) comboHistorial.getSelectedItem();
        if (e == null) {
            lblResumenHistorial.setText("No hay estudiantes registrados.");
            return;
        }
        int confirmadas = 0;
        int asistidas = 0;
        int liberadas = 0;
        int canceladas = 0;
        int inasistencias = 0;
        List<Reserva> reservas = gestorReservas.reservasDe(e.getCarne());
        for (Reserva r : reservas) {
            modeloHistorial.addRow(new Object[]{
                    r.getNumeroSala(), r.getFecha().format(FMT_FECHA), r.getHoraInicio().format(FMT_HORA),
                    r.getHoraFin().format(FMT_HORA), r.calcularDuracionMinutos(), r.getEstado()
            });
            if (r.getEstado().equalsIgnoreCase("Confirmada")) {
                confirmadas++;
            } else if (r.getEstado().equalsIgnoreCase("Asistio")) {
                asistidas++;
            } else if (r.getEstado().equalsIgnoreCase("Liberada")) {
                liberadas++;
            } else if (r.getEstado().equalsIgnoreCase("Cancelada")) {
                canceladas++;
            } else if (r.getEstado().equalsIgnoreCase("Inasistencia")) {
                inasistencias++;
            }
        }
        
        
        
        
        StringBuilder resumen = new StringBuilder( plural(reservas.size(), "reserva", "reservas"));
        
        agregarSiHay(resumen, confirmadas, "confirmada", "confirmadas");
        agregarSiHay(resumen, asistidas, "con check-in", "con check-in");
        agregarSiHay(resumen, liberadas, "liberada", "liberadas");
        agregarSiHay(resumen, canceladas, "cancelada", "canceladas");
        resumen.append("  -  inasistencias: ").append(inasistencias)
                .append(" de ").append(GestorEstudiantes.LIMITE_INASISTENCIAS);
        lblResumenHistorial.setText(resumen.toString());
    }
    
     private JPanel crearPanelCheckIn() {
        JList<Reserva> lista = crearListaReservas(listaCheckIn);
        JLabel lblResultado = new JLabel(" ");
        lblResultado.setFont(Tema.FUENTE_NEGRITA);

        JButton btnCheckIn = Tema.botonPrimario("Registrar check-in");
        btnCheckIn.addActionListener(e -> {
            Reserva r = lista.getSelectedValue();
            if (r == null) {
                mostrarError(lblResultado, "Selecciona una reserva de la lista.");
                return;
            }
            String error = gestorReservas.registrarCheckIn(r.getIdReserva(), LocalDateTime.now());
            if (error != null) {
                mostrarError(lblResultado, error);
                refrescarReservas();
                return;
            }
            mostrarExito(lblResultado, "Check-in registrado: sala " + r.getNumeroSala() + ", "
                    + r.getDpiCarnet() + ".");
            refrescarReservas();
        });

        return pagina("Check-in", "Desde " + GestorReservas.MINUTOS_CHECKIN_ANTES + " min antes hasta "
                        + GestorReservas.MINUTOS_TOLERANCIA + " min despues del inicio. Luego la sala se libera sola.",
                crearCuerpoLista(lista, btnCheckIn, lblResultado));
    }

    // ---------------------------------------------------------- Inasistencias

    private JPanel crearPanelInasistencias() {
        JList<Reserva> lista = crearListaReservas(listaInasistencias);
        JLabel lblResultado = new JLabel(" ");
        lblResultado.setFont(Tema.FUENTE_NEGRITA);

        JButton btnRegistrar = Tema.botonPeligro("Registrar inasistencia");
        btnRegistrar.addActionListener(e -> {
            Reserva r = lista.getSelectedValue();
            if (r == null) {
                mostrarError(lblResultado, "Selecciona una reserva de la lista.");
                return;
            }
            Estudiante estudiante = gestorEstudiantes.buscarPorCarne(r.getDpiCarnet());
            if (estudiante == null) {
                mostrarError(lblResultado, "El DPI o carne " + r.getDpiCarnet()
                        + " no esta registrado. Registralo en Estudiantes.");
                return;
            }
            int op = JOptionPane.showConfirmDialog(this,
                    estudiante.getNombre() + " no asistio a la sala " + r.getNumeroSala() + " del "
                            + r.getFecha().format(FMT_FECHA) + " (" + r.getHoraInicio().format(FMT_HORA)
                            + " a " + r.getHoraFin().format(FMT_HORA) + ").\nRegistrar la inasistencia?",
                    "Confirmar inasistencia", JOptionPane.YES_NO_OPTION);
            if (op != JOptionPane.YES_OPTION) {
                return;
            }
            String error = gestorReservas.registrarInasistencia(r.getIdReserva(), LocalDateTime.now());
            if (error != null) {
                mostrarError(lblResultado, error);
                return;
            }
            int total = gestorEstudiantes.registrarInasistencia(estudiante.getCarne());
            String mensaje = "Inasistencia registrada: " + estudiante.getNombre() + " (" + total + " de "
                    + GestorEstudiantes.LIMITE_INASISTENCIAS + ").";
            if (total >= GestorEstudiantes.LIMITE_INASISTENCIAS) {
                mostrarError(lblResultado, mensaje + " Queda sin reservas.");
            } else {
                mostrarExito(lblResultado, mensaje);
            }
            refrescarReservas();
        });

        return pagina("Inasistencias", "Reservas sin check-in (en curso o liberadas). Marca las que no se usaron.",                crearCuerpoLista(lista, btnRegistrar, lblResultado));
    }

    // ------------------------------------------------------- Nueva reserva

    private JPanel crearPanelRegistrar() {
        JComboBox<Sala> comboSala = new JComboBox<>();
        for (Sala s : gestorSalas.listarSalas()) {
            comboSala.addItem(s);
        }
        comboSala.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> l, Object v, int i, boolean sel, boolean foco) {
                super.getListCellRendererComponent(l, v, i, sel, foco);
                if (v instanceof Sala) {
                    Sala s = (Sala) v;
                    setText("Sala " + s.getNumero() + "  -  " + s.getUbicacion() + "  (hasta " + s.getCapacidad() + " personas)");
                }
                setBorder(Tema.margen(5, 10, 5, 10));
                return this;
            }
        });
        Tema.estilizarCombo(comboSala);

        JTextField txtDpi = new JTextField(18);
        Tema.estilizarCampo(txtDpi);
        JTextField txtCantidad = new JTextField(6);
        Tema.estilizarCampo(txtCantidad);
        JSpinner spFecha = crearSpinnerFecha();
        JSpinner spInicio = crearSpinnerHora(9, 0);
        JSpinner spFin = crearSpinnerHora(10, 0);
        JLabel lblResultado = new JLabel(" ");
        lblResultado.setFont(Tema.FUENTE_NEGRITA);

        JPanel form = Tema.tarjeta(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(8, 6, 8, 6);
        c.anchor = GridBagConstraints.WEST;
        c.fill = GridBagConstraints.HORIZONTAL;

        agregarFila(form, c, 0, "DPI o carne", txtDpi);
        agregarFila(form, c, 1, "Cantidad de usuarios", txtCantidad);
        agregarFila(form, c, 2, "Sala", comboSala);
        agregarFila(form, c, 3, "Fecha", spFecha);
        agregarFila(form, c, 4, "Hora de inicio", spInicio);
        agregarFila(form, c, 5, "Hora de fin", spFin);

        JLabel nota = new JLabel("Maximo permitido por reserva: " + GestorReservas.HORAS_MAXIMAS_POR_RESERVA + " horas.");
        nota.setFont(Tema.FUENTE_PEQUENA);
        nota.setForeground(Tema.TEXTO_SUAVE);
        c.gridx = 1; c.gridy = 6; c.weightx = 1;
        form.add(nota, c);

        JButton btnRegistrar = Tema.botonPrimario("Registrar reserva");
        btnRegistrar.addActionListener(e -> {
            Sala sala = (Sala) comboSala.getSelectedItem();
            String dpi = txtDpi.getText().trim();
            String cantTexto = txtCantidad.getText().trim();

            if (dpi.isEmpty()) {
                mostrarError(lblResultado, "Debes ingresar el DPI o carne.");
                return;
            }
            if (!dpi.matches("[A-Za-z0-9]{6,13}")) {
                mostrarError(lblResultado, "El DPI o carne debe tener de 6 a 13 caracteres (solo letras y numeros).");
                return;
            }
            String restriccion = gestorEstudiantes.motivoRestriccion(dpi);
            if (restriccion != null) {
                mostrarError(lblResultado, restriccion);
                return;
            }
            if (!cantTexto.matches("\\d+")) {
                mostrarError(lblResultado, "La cantidad de usuarios debe ser un numero entero.");
                return;
            }
            int cantidad = Integer.parseInt(cantTexto);
            if (sala == null) {
                mostrarError(lblResultado, "No hay salas registradas.");
                return;
            }
            if (cantidad < 1 || cantidad > sala.getCapacidad()) {
                mostrarError(lblResultado, "La sala " + sala.getNumero() + " tiene capacidad para "
                        + sala.getCapacidad() + " personas.");
                return;
            }

            LocalDate fecha = leerFecha(spFecha);
            LocalTime inicio = leerHora(spInicio);
            LocalTime fin = leerHora(spFin);

            String id = siguienteId();
            Reserva nueva = new Reserva(id, sala.getNumero(), dpi, cantidad, fecha, inicio, fin, "Confirmada");
            if (gestorReservas.registrarReserva(nueva)) {
                mostrarExito(lblResultado, "Reserva registrada con exito (sala " + sala.getNumero() + ", "
                        + fecha.format(FMT_FECHA) + ", " + inicio.format(FMT_HORA) + " a " + fin.format(FMT_HORA) + ").");
                txtDpi.setText("");
                txtCantidad.setText("");
                refrescarReservas();
            } else {
                mostrarError(lblResultado, "No se pudo registrar. Revisa el horario (cruce o maximo de horas).");
            }
        });

        JPanel pie = new JPanel(new BorderLayout(16, 0));
        pie.setOpaque(false);
        pie.add(btnRegistrar, BorderLayout.WEST);
        pie.add(lblResultado, BorderLayout.CENTER);

        pie.setBorder(Tema.margen(0, 2, 0, 0));
        JPanel cuerpo = new JPanel(new BorderLayout(0, 16));
        cuerpo.setOpaque(false);
        cuerpo.add(form, BorderLayout.NORTH);
        cuerpo.add(pie, BorderLayout.SOUTH);
        JPanel envoltura = new JPanel(new BorderLayout());
        envoltura.setOpaque(false);
        envoltura.add(cuerpo, BorderLayout.NORTH);

        return pagina("Nueva reserva", "Completa los datos para reservar un Working Point.", envoltura);
    }

    /** Genera el siguiente id consecutivo (R001, R002, ...) sin que el usuario lo escriba. */
    private String siguienteId() {
        int max = 0;
        for (Reserva r : gestorReservas.getReservas()) {
            String digitos = r.getIdReserva().replaceAll("\\D", "");
            if (!digitos.isEmpty()) {
                try {
                    max = Math.max(max, Integer.parseInt(digitos));
                } catch (NumberFormatException ignorada) {
                    // id demasiado largo, se ignora
                }
            }
        }
        return String.format("R%03d", max + 1);
    }

    // -------------------------------------------------------------- Reservas

    private JPanel crearPanelVerReservas() {
        String[] columnas = {"Sala", "DPI / Carne", "Usuarios", "Fecha", "Inicio", "Fin", "Duracion (min)", "Estado"};
        modeloReservas = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };
        JTable tabla = new JTable(modeloReservas);
        Tema.estilizarTabla(tabla);
        int[] anchos = {62, 150, 100, 125, 65, 65, 130, 110};
        for (int i = 0; i < anchos.length; i++) {
            tabla.getColumnModel().getColumn(i).setPreferredWidth(anchos[i]);
        }

        JPanel barra = crearBarraFiltro("Filtrar por DPI o carne:", tabla, modeloReservas, 1);

        JPanel cuerpo = new JPanel(new BorderLayout(0, 14));
        cuerpo.setOpaque(false);
        cuerpo.add(barra, BorderLayout.NORTH);
        cuerpo.add(Tema.scroll(tabla), BorderLayout.CENTER);

        return pagina("Reservas", "Todas las reservas registradas, confirmadas y canceladas.", cuerpo);
    }

    /** Barra con un campo de texto que filtra las filas de la tabla por una columna. */
    private JPanel crearBarraFiltro(String etiqueta, JTable tabla, DefaultTableModel modelo, int columna) {
        TableRowSorter<DefaultTableModel> orden = new TableRowSorter<>(modelo);
        tabla.setRowSorter(orden);
        JTextField txtFiltro = new JTextField(20);
        Tema.estilizarCampo(txtFiltro);
        txtFiltro.getDocument().addDocumentListener(new DocumentListener() {
            private void filtrar() {
                String t = txtFiltro.getText().trim();
                orden.setRowFilter(t.isEmpty() ? null
                        : RowFilter.regexFilter("(?i)" + Pattern.quote(t), columna));
            }

            @Override
            public void insertUpdate(DocumentEvent e) { filtrar(); }

            @Override
            public void removeUpdate(DocumentEvent e) { filtrar(); }

            @Override
            public void changedUpdate(DocumentEvent e) { filtrar(); }
        });

        JPanel barra = new JPanel(new BorderLayout(12, 0));
        barra.setOpaque(false);
        barra.add(Tema.etiqueta(etiqueta), BorderLayout.WEST);
        barra.add(txtFiltro, BorderLayout.CENTER);
        return barra;
    }

    // -------------------------------------------------------------- Cancelar

    private JPanel crearPanelCancelar() {
        JList<Reserva> lista = crearListaReservas(listaCancelar);
        JLabel lblResultado = new JLabel(" ");
        lblResultado.setFont(Tema.FUENTE_NEGRITA);

        JButton btnCancelar = Tema.botonPeligro("Cancelar reserva seleccionada");
        btnCancelar.addActionListener(e -> {
            Reserva r = lista.getSelectedValue();
            if (r == null) {
                mostrarError(lblResultado, "Selecciona una reserva de la lista.");
                return;
            }
            int op = JOptionPane.showConfirmDialog(this,
                    "Cancelar la reserva de la sala " + r.getNumeroSala() + " del "
                            + r.getFecha().format(FMT_FECHA) + " (" + r.getHoraInicio().format(FMT_HORA)
                            + " a " + r.getHoraFin().format(FMT_HORA) + ")?",
                    "Confirmar cancelacion", JOptionPane.YES_NO_OPTION);
            if (op != JOptionPane.YES_OPTION) {
                return;
            }
            if (gestorReservas.cancelarReserva(r.getIdReserva())) {
                mostrarExito(lblResultado, "Reserva cancelada con exito.");
                refrescarReservas();
            } else {
                mostrarError(lblResultado, "No se pudo cancelar la reserva.");
            }
        });

        return pagina("Cancelar reserva", "Elige de la lista la reserva que quieres cancelar.",
                crearCuerpoLista(lista, btnCancelar, lblResultado));
    }

    // -------------------------------------------------------------- Duracion

    private JPanel crearPanelDuracion() {
        JList<Reserva> lista = crearListaReservas(listaDuracion);
        JLabel lblResultado = new JLabel(" ");
        lblResultado.setFont(Tema.FUENTE_NEGRITA);

        JButton btnCalcular = Tema.botonPrimario("Calcular duracion");
        btnCalcular.addActionListener(e -> {
            Reserva r = lista.getSelectedValue();
            if (r == null) {
                mostrarError(lblResultado, "Selecciona una reserva de la lista.");
                return;
            }
            long minutos = gestorSalas.calcularDuracionReserva(r.getIdReserva(), gestorReservas.getReservas());
            if (minutos >= 0) {
                mostrarExito(lblResultado, "La reserva dura " + minutos + " minutos ("
                        + minutos / 60 + " h " + minutos % 60 + " min).");
            } else {
                mostrarError(lblResultado, "No se encontro la reserva.");
            }
        });

        return pagina("Duracion de reserva", "Elige una reserva de la lista para calcular cuanto dura.",
                crearCuerpoLista(lista, btnCalcular, lblResultado));
    }

    private JPanel crearCuerpoLista(JList<Reserva> lista, JButton boton, JLabel resultado) {
        JPanel pie = new JPanel(new BorderLayout(16, 0));
        pie.setOpaque(false);
        pie.add(boton, BorderLayout.WEST);
        pie.add(resultado, BorderLayout.CENTER);

        JPanel cuerpo = new JPanel(new BorderLayout(0, 16));
        cuerpo.setOpaque(false);
        cuerpo.add(Tema.scroll(lista), BorderLayout.CENTER);
        cuerpo.add(pie, BorderLayout.SOUTH);
        return cuerpo;
    }

    /** Lista de una sola columna: cada reserva es una fila de dos lineas. */
    private JList<Reserva> crearListaReservas(DefaultListModel<Reserva> modelo) {
        JList<Reserva> lista = new JList<>(modelo);
        lista.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        lista.setFixedCellHeight(68);
        lista.setCellRenderer(new ListCellRenderer<Reserva>() {
            @Override
            public Component getListCellRendererComponent(JList<? extends Reserva> l, Reserva r, int i,
                                                           boolean sel, boolean foco) {
                JPanel fila = new JPanel(new BorderLayout(0, 3));
                fila.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(0xEEF0F5)),
                        Tema.margen(10, 16, 10, 16)));
                fila.setBackground(sel ? Tema.SELECCION : Color.WHITE);

                JLabel l1 = new JLabel("Sala " + r.getNumeroSala() + "   -   " + r.getFecha().format(FMT_FECHA)
                        + "   " + r.getHoraInicio().format(FMT_HORA) + " a " + r.getHoraFin().format(FMT_HORA));
                l1.setFont(Tema.FUENTE_NEGRITA);
                l1.setForeground(Tema.TEXTO);

                JLabel l2 = new JLabel(r.getDpiCarnet() + "   -   " + r.getCantidadUsuarios() + " usuarios   -   "
                        + r.getEstado());
                l2.setFont(Tema.FUENTE_PEQUENA);
                l2.setForeground(r.getEstado().equalsIgnoreCase("Cancelada") ? Tema.PELIGRO : Tema.TEXTO_SUAVE);

                fila.add(l1, BorderLayout.NORTH);
                fila.add(l2, BorderLayout.CENTER);
                return fila;
            }
        });
        return lista;
    }

    // ------------------------------------------------------------- Utilidades

        private void refrescarReservas() {
        LocalDateTime ahora = LocalDateTime.now();
        gestorReservas.liberarVencidas(ahora);
        if (modeloReservas != null) {
            modeloReservas.setRowCount(0);
            for (Reserva r : gestorReservas.getReservas()) {
                modeloReservas.addRow(new Object[]{
                        r.getNumeroSala(), r.getDpiCarnet(), r.getCantidadUsuarios(),
                        r.getFecha().format(FMT_FECHA), r.getHoraInicio().format(FMT_HORA),
                        r.getHoraFin().format(FMT_HORA), r.calcularDuracionMinutos(), r.getEstado()
                });
            }
        }
        listaCancelar.clear();
        listaDuracion.clear();
        listaInasistencias.clear();
        listaCheckIn.clear();
        for (Reserva r : gestorReservas.getReservas()) {
            if (r.getEstado().equalsIgnoreCase("Confirmada")) {
                listaCancelar.addElement(r);
                listaCheckIn.addElement(r);
                if (gestorReservas.yaComenzo(r, ahora)) {
                    listaInasistencias.addElement(r);
                }
            } else if (r.getEstado().equalsIgnoreCase("Liberada")) {
                listaInasistencias.addElement(r);
            }
            listaDuracion.addElement(r);
        }
        refrescarEstudiantes();
        refrescarHistorial();
    }

    private void refrescarEstudiantes() {
        if (modeloEstudiantes == null) {
            return;
        }
        modeloEstudiantes.setRowCount(0);
        for (Estudiante e : gestorEstudiantes.listarEstudiantes()) {
            modeloEstudiantes.addRow(new Object[]{e.getCarne(), e.getNombre(), e.getCorreo(), e.getInasistencias()});
        }
    }

    private JSpinner crearSpinnerFecha() {
        JSpinner sp = new JSpinner(new SpinnerDateModel(new Date(), null, null, java.util.Calendar.DAY_OF_MONTH));
        sp.setEditor(new JSpinner.DateEditor(sp, "dd/MM/yyyy"));
        Tema.estilizarSpinner(sp);
        sp.setPreferredSize(new Dimension(150, 38));
        return sp;
    }

    private JSpinner crearSpinnerHora(int hora, int minuto) {
        java.util.Calendar cal = java.util.Calendar.getInstance();
        cal.set(java.util.Calendar.HOUR_OF_DAY, hora);
        cal.set(java.util.Calendar.MINUTE, minuto);
        cal.set(java.util.Calendar.SECOND, 0);
        JSpinner sp = new JSpinner(new SpinnerDateModel(cal.getTime(), null, null, java.util.Calendar.MINUTE));
        sp.setEditor(new JSpinner.DateEditor(sp, "HH:mm"));
        Tema.estilizarSpinner(sp);
        sp.setPreferredSize(new Dimension(110, 38));
        return sp;
    }

    private LocalDate leerFecha(JSpinner sp) {
        return ((Date) sp.getValue()).toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    }

    private LocalTime leerHora(JSpinner sp) {
        return ((Date) sp.getValue()).toInstant().atZone(ZoneId.systemDefault()).toLocalTime()
                .withSecond(0).withNano(0);
    }

    private void agregarFila(JPanel panel, GridBagConstraints c, int fila, String etiqueta, JComponent campo) {
        c.gridx = 0; c.gridy = fila; c.gridwidth = 1; c.weightx = 0;
        JLabel l = Tema.etiqueta(etiqueta);
        l.setPreferredSize(new Dimension(180, 24));
        panel.add(l, c);
        c.gridx = 1; c.weightx = 1;
        c.fill = (campo instanceof JSpinner) ? GridBagConstraints.NONE : GridBagConstraints.HORIZONTAL;
        panel.add(campo, c);
        c.fill = GridBagConstraints.HORIZONTAL;
    }
    
    private void agregarSiHay(StringBuilder texto, int cantidad, String singular, String plural) {
        if (cantidad > 0) {
            texto.append("  -  ").append(plural(cantidad, singular, plural));
        }
    }
    private String plural(int cantidad, String singular, String plural) {
        return cantidad + " " + (cantidad == 1 ? singular : plural);
    }

    private void mostrarExito(JLabel lbl, String texto) {
        lbl.setForeground(Tema.EXITO);
        lbl.setText(texto);
    }

    private void mostrarError(JLabel lbl, String texto) {
        lbl.setForeground(Tema.PELIGRO);
        lbl.setText(texto);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            VentanaPrincipal ventana = new VentanaPrincipal();
            ventana.setVisible(true);
        });
    }
}