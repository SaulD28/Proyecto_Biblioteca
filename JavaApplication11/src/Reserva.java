
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

/**
 *
 * @author picor
 */
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

    public String getIdReserva() {
        return idReserva;
    }

    public void setIdReserva(String idReserva) {
        this.idReserva = idReserva;
    }

    public int getNumeroSala() {
        return numeroSala;
    }

    public void setNumeroSala(int numeroSala) {
        this.numeroSala = numeroSala;
    }

    public String getCarnetEstudiante() {
        return carnetEstudiante;
    }

    public void setCarnetEstudiante(String carnetEstudiante) {
        this.carnetEstudiante = carnetEstudiante;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public void setFecha(LocalDate fecha) {
        this.fecha = fecha;
    }

    public LocalTime getHoraInicio() {
        return horaInicio;
    }

    public void setHoraInicio(LocalTime horaInicio) {
        this.horaInicio = horaInicio;
    }

    public LocalTime getHoraFin() {
        return horaFin;
    }

    public void setHoraFin(LocalTime horaFin) {
        this.horaFin = horaFin;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }
    
    public boolean seTraslapaCon(LocalTime otraInicio, LocalTime otraFin) {
        return horaInicio.isBefore(otraFin) && horaFin.isAfter(otraInicio);
    }

    public String toCsv() {
        return idReserva + "," + numeroSala + "," + carnetEstudiante + ","
                + fecha.format(FORMATO_FECHA) + "," + horaInicio.format(FORMATO_HORA) + ","
                + horaFin.format(FORMATO_HORA) + "," + estado;
    }

    public static Reserva fromCsv(String linea) {
        String[] p = linea.split(",");
        String idReserva = p[0].trim();
        int numeroSala = Integer.parseInt(p[1].trim());
        String carnet = p[2].trim();
        LocalDate fecha = LocalDate.parse(p[3].trim(), FORMATO_FECHA);
        LocalTime inicio = LocalTime.parse(p[4].trim(), FORMATO_HORA);
        LocalTime fin = LocalTime.parse(p[5].trim(), FORMATO_HORA);
        String estado = p[6].trim();
        return new Reserva(idReserva, numeroSala, carnet, fecha, inicio, fin, estado);
    }

    @Override
    public String toString() {
        return "Reserva " + idReserva + " - Sala " + numeroSala + " - " + carnetEstudiante
                + " - " + fecha.format(FORMATO_FECHA) + " " + horaInicio.format(FORMATO_HORA)
                + " a " + horaFin.format(FORMATO_HORA) + " - " + estado;
    }
    
    
}
