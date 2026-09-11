import java.time.LocalDate;
import java.time.LocalTime;
import java.time.Duration;
import java.time.format.DateTimeFormatter;

public class Reserva {
    
    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter FORMATO_HORA = DateTimeFormatter.ofPattern("HH:mm");
    
    private String idReserva;
    private int numeroSala;
    private String carnetEstudiante;
    private LocalDate fecha;
    private LocalTime horaInicio;
    private LocalTime horaFin;
    private String estado;
    
    public Reserva(){
        
    }
    
    public Reserva(String idReserva, int numeroSala, String carnetEstudiante,
            LocalDate fecha, LocalTime horaInicio, LocalTime horaFin, String estado){
        this.idReserva =idReserva;
        this.numeroSala = numeroSala;
        this.carnetEstudiante = carnetEstudiante;
        this.fecha = fecha;
        this.horaInicio = horaInicio;
        this.horaFin = horaFin;
        this.estado = estado;
    }

    // ... (todos tus getters y setters se quedan igual) ...

    /**
     * Requisito funcional: Calcular duracion de una reserva (en minutos).
     */
    public long calcularDuracionMinutos() {
        return Duration.between(horaInicio, horaFin).toMinutes();
    }

    public boolean seTraslapaCon(LocalTime otraInicio, LocalTime otraFin) {
        return horaInicio.isBefore(otraFin) && horaFin.isAfter(otraInicio);
    }

    // ... (toCsv, fromCsv, toString se quedan igual) ...
}