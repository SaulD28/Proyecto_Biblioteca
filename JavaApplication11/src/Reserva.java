import java.time.LocalDate;
import java.time.LocalTime;
import java.time.Duration;
import java.time.format.DateTimeFormatter;

public class Reserva {

    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter FORMATO_HORA = DateTimeFormatter.ofPattern("HH:mm");

    private String idReserva;
    private int numeroSala;
    private String dpiCarnet;
    private int cantidadUsuarios;
    private LocalDate fecha;
    private LocalTime horaInicio;
    private LocalTime horaFin;
    private String estado;

    public Reserva() {
    }

    public Reserva(String idReserva, int numeroSala, String dpiCarnet, int cantidadUsuarios,
                    LocalDate fecha, LocalTime horaInicio, LocalTime horaFin, String estado) {
        this.idReserva = idReserva;
        this.numeroSala = numeroSala;
        this.dpiCarnet = dpiCarnet;
        this.cantidadUsuarios = cantidadUsuarios;
        this.fecha = fecha;
        this.horaInicio = horaInicio;
        this.horaFin = horaFin;
        this.estado = estado;
    }

    public String getIdReserva() { return idReserva; }
    public void setIdReserva(String idReserva) { this.idReserva = idReserva; }

    public int getNumeroSala() { return numeroSala; }
    public void setNumeroSala(int numeroSala) { this.numeroSala = numeroSala; }

    public String getDpiCarnet() { return dpiCarnet; }
    public void setDpiCarnet(String dpiCarnet) { this.dpiCarnet = dpiCarnet; }

    public int getCantidadUsuarios() { return cantidadUsuarios; }
    public void setCantidadUsuarios(int cantidadUsuarios) { this.cantidadUsuarios = cantidadUsuarios; }

    public LocalDate getFecha() { return fecha; }
    public void setFecha(LocalDate fecha) { this.fecha = fecha; }

    public LocalTime getHoraInicio() { return horaInicio; }
    public void setHoraInicio(LocalTime horaInicio) { this.horaInicio = horaInicio; }

    public LocalTime getHoraFin() { return horaFin; }
    public void setHoraFin(LocalTime horaFin) { this.horaFin = horaFin; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public long calcularDuracionMinutos() {
        return Duration.between(horaInicio, horaFin).toMinutes();
    }
    
    public boolean ocupaSala() {
        return estado.equalsIgnoreCase("Confirmada") || estado.equalsIgnoreCase("Asistio");
    }

    public boolean seTraslapaCon(LocalTime otraInicio, LocalTime otraFin) {
        return horaInicio.isBefore(otraFin) && horaFin.isAfter(otraInicio);
    }

    public String toCsv() {
        return idReserva + "," + numeroSala + "," + dpiCarnet + "," + cantidadUsuarios + ","
                + fecha.format(FORMATO_FECHA) + "," + horaInicio.format(FORMATO_HORA) + ","
                + horaFin.format(FORMATO_HORA) + "," + estado;
    }

    public static Reserva fromCsv(String linea) {
        String[] p = linea.split(",");
        String idReserva = p[0].trim();
        int numeroSala = Integer.parseInt(p[1].trim());
        String dpiCarnet = p[2].trim();
        int cantidadUsuarios = Integer.parseInt(p[3].trim());
        LocalDate fecha = LocalDate.parse(p[4].trim(), FORMATO_FECHA);
        LocalTime inicio = LocalTime.parse(p[5].trim(), FORMATO_HORA);
        LocalTime fin = LocalTime.parse(p[6].trim(), FORMATO_HORA);
        String estado = p[7].trim();
        return new Reserva(idReserva, numeroSala, dpiCarnet, cantidadUsuarios, fecha, inicio, fin, estado);
    }

    @Override
    public String toString() {
        return "Reserva " + idReserva + " - Sala " + numeroSala + " - " + dpiCarnet
                + " (" + cantidadUsuarios + " usuarios) - " + fecha.format(FORMATO_FECHA) + " "
                + horaInicio.format(FORMATO_HORA) + " a " + horaFin.format(FORMATO_HORA) + " - " + estado;
    }
}