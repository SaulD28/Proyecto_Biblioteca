import java.io.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class GestorReservas {

    public static final int HORAS_MAXIMAS_POR_RESERVA = 3; 

    /** El check-in se abre estos minutos antes de la hora de inicio. */
    public static final int MINUTOS_CHECKIN_ANTES = 15;

    /** Pasados estos minutos del inicio sin check-in, la sala se libera sola. */
    public static final int MINUTOS_TOLERANCIA = 15;

    private List<Reserva> reservas;
    private String rutaArchivo;

    public GestorReservas(String rutaArchivo) {
        this.reservas = new ArrayList<>();
        this.rutaArchivo = rutaArchivo;
        cargarDesdeArchivo();
    }

    public List<Reserva> getReservas() {
        return reservas;
    }

    public boolean validarCruceHorario(int numeroSala, LocalDate fecha, LocalTime horaInicio, LocalTime horaFin) {
        for (Reserva r : reservas) {
            boolean mismaSala = r.getNumeroSala() == numeroSala;
            boolean mismaFecha = r.getFecha().equals(fecha);
            boolean ocupaSala = r.ocupaSala();

            if (mismaSala && mismaFecha && ocupaSala && r.seTraslapaCon(horaInicio, horaFin)) {
                System.out.println("Cruce de horario detectado con la reserva " + r.getIdReserva());
                return true;
            }
        }
        return false;
    }


    public boolean usuarioTieneCruce(String dpiCarnet, LocalDate fecha, LocalTime horaInicio, LocalTime horaFin) {
        for (Reserva r : reservas) {
            boolean mismoUsuario = r.getDpiCarnet().equals(dpiCarnet);
            boolean mismaFecha = r.getFecha().equals(fecha);
            boolean ocupaSala = r.ocupaSala();

            if (mismoUsuario && mismaFecha && ocupaSala && r.seTraslapaCon(horaInicio, horaFin)) {
                System.out.println("El usuario " + dpiCarnet + " ya tiene la reserva "
                        + r.getIdReserva() + " en ese horario.");
                return true;
            }
        }
        return false;
    }


    public boolean registrarReserva(Reserva nuevaReserva) {
        long duracionMin = nuevaReserva.calcularDuracionMinutos();

        if (duracionMin <= 0) {
            System.out.println("La hora de fin debe ser posterior a la hora de inicio.");
            return false;
        }

        if (duracionMin > HORAS_MAXIMAS_POR_RESERVA * 60) {
            System.out.println("No se pudo registrar: la reserva supera el maximo de "
                    + HORAS_MAXIMAS_POR_RESERVA + " horas permitidas.");
            return false;
        }

        boolean hayCruce = validarCruceHorario(
                nuevaReserva.getNumeroSala(),
                nuevaReserva.getFecha(),
                nuevaReserva.getHoraInicio(),
                nuevaReserva.getHoraFin()
        );

        if (hayCruce) {
            System.out.println("No se pudo registrar la reserva " + nuevaReserva.getIdReserva()
                    + ": la sala ya esta reservada en ese horario.");
            return false;
        }

        if (usuarioTieneCruce(nuevaReserva.getDpiCarnet(), nuevaReserva.getFecha(),
                nuevaReserva.getHoraInicio(), nuevaReserva.getHoraFin())) {
            System.out.println("No se pudo registrar la reserva " + nuevaReserva.getIdReserva()
                    + ": el usuario ya tiene otra reserva en ese horario.");
            return false;
        }

        reservas.add(nuevaReserva);
        guardarEnArchivo();
        System.out.println("Reserva registrada con exito: " + nuevaReserva);
        return true;
    }

    public boolean cancelarReserva(String idReserva) {
        for (Reserva r : reservas) {
            if (r.getIdReserva().equals(idReserva) && r.getEstado().equalsIgnoreCase("Confirmada")) {
                r.setEstado("Cancelada");
                guardarEnArchivo();
                System.out.println("Reserva " + idReserva + " cancelada con exito.");
                return true;
            }
        }
        System.out.println("No se encontro la reserva " + idReserva + " o ya estaba cancelada.");
        return false;
    }

    /**
     * Requisito funcional: Consultar historial de reservas de un estudiante.
     * Devuelve todas sus reservas (confirmadas, canceladas y con inasistencia).
     */
    public List<Reserva> reservasDe(String dpiCarnet) {
        List<Reserva> resultado = new ArrayList<>();
        for (Reserva r : reservas) {
            if (r.getDpiCarnet().equalsIgnoreCase(dpiCarnet)) {
                resultado.add(r);
            }
        }
        return resultado;
    }

    /** Indica si la reserva ya empezo en el momento recibido. */
    public boolean yaComenzo(Reserva r, LocalDateTime ahora) {
        return !LocalDateTime.of(r.getFecha(), r.getHoraInicio()).isAfter(ahora);
    }


    public String registrarInasistencia(String idReserva, LocalDateTime ahora) {
        for (Reserva r : reservas) {
            if (r.getIdReserva().equals(idReserva)) {
                boolean liberada = r.getEstado().equalsIgnoreCase("Liberada");
                if (!liberada && !r.getEstado().equalsIgnoreCase("Confirmada")) {
                    return "Solo se puede registrar inasistencia en reservas sin check-in (confirmadas o liberadas).";
                }
                if (!yaComenzo(r, ahora)) {
                    return "La reserva aun no ha comenzado.";
                }
                r.setEstado("Inasistencia");
                guardarEnArchivo();
                System.out.println("Inasistencia registrada en la reserva " + idReserva);
                return null;
            }
        }
        return "No se encontro la reserva " + idReserva + ".";
    }


    public String registrarCheckIn(String idReserva, LocalDateTime ahora) {
        for (Reserva r : reservas) {
            if (r.getIdReserva().equals(idReserva)) {
                if (!r.getEstado().equalsIgnoreCase("Confirmada")) {
                    return "Solo se puede hacer check-in en reservas confirmadas.";
                }
                LocalDateTime inicio = LocalDateTime.of(r.getFecha(), r.getHoraInicio());
                if (ahora.isBefore(inicio.minusMinutes(MINUTOS_CHECKIN_ANTES))) {
                    return "Aun es pronto: el check-in abre " + MINUTOS_CHECKIN_ANTES
                            + " minutos antes de las " + r.getHoraInicio() + ".";
                }
                if (ahora.isAfter(inicio.plusMinutes(MINUTOS_TOLERANCIA))) {
                    return "Ya paso la tolerancia de " + MINUTOS_TOLERANCIA + " minutos: la sala se libera.";
                }
                r.setEstado("Asistio");
                guardarEnArchivo();
                System.out.println("Check-in registrado en la reserva " + idReserva);
                return null;
            }
        }
        return "No se encontro la reserva " + idReserva + ".";
    }


    public List<Reserva> liberarVencidas(LocalDateTime ahora) {
        List<Reserva> liberadas = new ArrayList<>();
        for (Reserva r : reservas) {
            LocalDateTime limite = LocalDateTime.of(r.getFecha(), r.getHoraInicio()).plusMinutes(MINUTOS_TOLERANCIA);
            if (r.getEstado().equalsIgnoreCase("Confirmada") && ahora.isAfter(limite)) {
                r.setEstado("Liberada");
                liberadas.add(r);
            }
        }
        if (!liberadas.isEmpty()) {
            guardarEnArchivo();
            System.out.println("Salas liberadas automaticamente: " + liberadas.size());
        }
        return liberadas;
    }

    private void cargarDesdeArchivo() {
        File archivo = new File(rutaArchivo);
        if (!archivo.exists()) {
            return;
        }
        try (BufferedReader br = new BufferedReader(new FileReader(archivo))) {
            String linea;
            while ((linea = br.readLine()) != null) {
                if (!linea.isBlank()) {
                    reservas.add(Reserva.fromCsv(linea));
                }
            }
        } catch (IOException e) {
            System.out.println("Error al leer reservas.txt: " + e.getMessage());
        }
    }

    private void guardarEnArchivo() {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(rutaArchivo))) {
            for (Reserva r : reservas) {
                bw.write(r.toCsv());
                bw.newLine();
            }
        } catch (IOException e) {
            System.out.println("Error al guardar reservas.txt: " + e.getMessage());
        }
    }
}