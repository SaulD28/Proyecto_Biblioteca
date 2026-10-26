import java.io.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

public class GestorSalas {
    
    public static final int HORA_APERTURA = 8;
    public static final int HORA_CIERRE = 20;
    private List<Sala> salas;
    private String rutaArchivo;

    public GestorSalas(String rutaArchivo) {
        this.salas = new ArrayList<>();
        this.rutaArchivo = rutaArchivo;
        cargarDesdeArchivo();
    }

    /**
     * Requisito funcional: Gestionar salas (registrar).
     */
    public void registrarSala(Sala sala) {
        salas.add(sala);
        guardarEnArchivo();
        System.out.println("Sala registrada: " + sala);
    }

    /**
     * Requisito funcional: Gestionar salas (listar).
     */
    public List<Sala> listarSalas() {
        return salas;
    }

    public Sala buscarSalaPorNumero(int numero) {
        for (Sala s : salas) {
            if (s.getNumero() == numero) {
                return s;
            }
        }
        return null;
    }

    /**
     * Requisito funcional: Consultar disponibilidad de una sala.
     */
    public boolean consultarDisponibilidad(int numeroSala, LocalDate fecha, LocalTime horaInicio,
                                            LocalTime horaFin, List<Reserva> reservasExistentes) {
        Sala sala = buscarSalaPorNumero(numeroSala);
        if (sala == null) {
            System.out.println("La sala " + numeroSala + " no existe.");
            return false;
        }

        for (Reserva r : reservasExistentes) {
            boolean mismaSala = r.getNumeroSala() == numeroSala;
            boolean mismaFecha = r.getFecha().equals(fecha);
            boolean ocupaSala = r.ocupaSala();

            if (mismaSala && mismaFecha && ocupaSala) {
                if (r.seTraslapaCon(horaInicio, horaFin)) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Requisito funcional: Calcular duracion de una reserva.
     * Busca la reserva por su id dentro de la lista recibida y usa
     * el metodo calcularDuracionMinutos() que ya existe en Reserva.
     */
    public long calcularDuracionReserva(String idReserva, List<Reserva> reservas) {
        for (Reserva r : reservas) {
            if (r.getIdReserva().equals(idReserva)) {
                return r.calcularDuracionMinutos();
            }
        }
        System.out.println("No se encontro la reserva " + idReserva);
        return -1;
    }
    /** Disponibilidad en tiempo real: reserva que tiene la sala ocupada ahora, o null si esta libre. */
    public Reserva reservaEnCurso(int numeroSala, List<Reserva> reservas, LocalDateTime ahora) {
        LocalTime hora = ahora.toLocalTime();
        for (Reserva r : reservas) {
            if (r.getNumeroSala() == numeroSala && r.ocupaSala() && r.getFecha().equals(ahora.toLocalDate())
                    && !hora.isBefore(r.getHoraInicio()) && hora.isBefore(r.getHoraFin())) {
                return r;
            }
        }
        return null;
    }

    /** La siguiente reserva de hoy que empieza despues de este momento, o null si no hay. */
    public Reserva proximaReserva(int numeroSala, List<Reserva> reservas, LocalDateTime ahora) {
        LocalTime hora = ahora.toLocalTime();
        Reserva proxima = null;
        for (Reserva r : reservas) {
            if (r.getNumeroSala() == numeroSala && r.ocupaSala() && r.getFecha().equals(ahora.toLocalDate())
                    && r.getHoraInicio().isAfter(hora)
                    && (proxima == null || r.getHoraInicio().isBefore(proxima.getHoraInicio()))) {
                proxima = r;
            }
        }
        return proxima;
    }

    /**
     * Reporte de ocupacion por sala (una fila por sala, entre dos fechas inclusive):
     * {numero, ubicacion, reservas, con check-in, liberadas, inasistencias, canceladas,
     *  tiempo reservado, % de ocupacion}.
     */
    public List<Object[]> reporteOcupacion(List<Reserva> reservas, LocalDate desde, LocalDate hasta) {
        List<Object[]> filas = new ArrayList<>();
        long dias = ChronoUnit.DAYS.between(desde, hasta) + 1;
        double minutosDisponibles = Math.max(dias, 1) * (HORA_CIERRE - HORA_APERTURA) * 60.0;

        for (Sala s : salas) {
            int total = 0, asistieron = 0, liberadas = 0, inasistencias = 0, canceladas = 0;
            long minutos = 0;
            for (Reserva r : reservas) {
                if (r.getNumeroSala() != s.getNumero() || r.getFecha().isBefore(desde)
                        || r.getFecha().isAfter(hasta)) {
                    continue;
                }
                total++;
                String estado = r.getEstado();
                if (estado.equalsIgnoreCase("Asistio")) {
                    asistieron++;
                } else if (estado.equalsIgnoreCase("Liberada")) {
                    liberadas++;
                } else if (estado.equalsIgnoreCase("Inasistencia")) {
                    inasistencias++;
                } else if (estado.equalsIgnoreCase("Cancelada")) {
                    canceladas++;
                }
                if (r.ocupaSala()) {
                    minutos += r.calcularDuracionMinutos();
                }
            }
            double porcentaje = Math.min(100.0, minutos * 100.0 / minutosDisponibles);
            filas.add(new Object[]{
                    s.getNumero(), s.getUbicacion(), total, asistieron, liberadas, inasistencias, canceladas,
                    (minutos / 60) + " h " + (minutos % 60) + " min",
                    String.format("%.1f%%", porcentaje)
            });
        }
        return filas;
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
                    salas.add(Sala.fromCsv(linea));
                }
            }
        } catch (IOException e) {
            System.out.println("Error al leer salas.txt: " + e.getMessage());
        }
    }

    private void guardarEnArchivo() {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(rutaArchivo))) {
            for (Sala s : salas) {
                bw.write(s.toCsv());
                bw.newLine();
            }
        } catch (IOException e) {
            System.out.println("Error al guardar salas.txt: " + e.getMessage());
        }
    }
}
