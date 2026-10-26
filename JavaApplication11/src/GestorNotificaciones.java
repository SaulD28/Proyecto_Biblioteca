import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Requisito funcional: Notificacion de confirmacion de reserva.
 * Arma el mensaje de confirmacion y deja una copia en data/notificaciones.txt,
 * como si fuera el correo enviado al estudiante.
 */
public class GestorNotificaciones {

    private static final DateTimeFormatter FMT_FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter FMT_HORA = DateTimeFormatter.ofPattern("HH:mm");
    private static final DateTimeFormatter FMT_ENVIO = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

    private final String rutaArchivo;

    public GestorNotificaciones(String rutaArchivo) {
        this.rutaArchivo = rutaArchivo;
    }

    /** Texto de la confirmacion, en varias lineas (para mostrar en una ventana). */
    public String armarMensaje(Reserva r, Estudiante estudiante) {
        String nombre = estudiante == null ? r.getDpiCarnet() : estudiante.getNombre();
        return "Hola " + nombre + ", tu reserva fue confirmada.\n\n"
                + "Reserva: " + r.getIdReserva() + "\n"
                + "Sala: " + r.getNumeroSala() + "\n"
                + "Fecha: " + r.getFecha().format(FMT_FECHA) + "\n"
                + "Horario: " + r.getHoraInicio().format(FMT_HORA) + " a " + r.getHoraFin().format(FMT_HORA) + "\n"
                + "Personas: " + r.getCantidadUsuarios() + "\n\n"
                + "Haz tu check-in entre " + GestorReservas.MINUTOS_CHECKIN_ANTES + " minutos antes y "
                + GestorReservas.MINUTOS_TOLERANCIA + " minutos despues de la hora de inicio, "
                + "o la sala se liberara.";
    }

    /**
     * Registra la notificacion en el archivo (una linea por envio):
     * fecha de envio,idReserva,destinatario,mensaje resumido.
     *
     * @return el correo del destinatario, o un aviso si el estudiante no esta registrado.
     */
    public String enviarConfirmacion(Reserva r, Estudiante estudiante) {
        String destino = estudiante == null ? "(sin correo registrado)" : estudiante.getCorreo();
        String resumen = "Reserva confirmada: sala " + r.getNumeroSala() + ", "
                + r.getFecha().format(FMT_FECHA) + " " + r.getHoraInicio().format(FMT_HORA)
                + " a " + r.getHoraFin().format(FMT_HORA);
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(rutaArchivo, true))) {
            bw.write(LocalDateTime.now().format(FMT_ENVIO) + "," + r.getIdReserva() + ","
                    + destino + "," + resumen);
            bw.newLine();
        } catch (IOException e) {
            System.out.println("Error al guardar notificaciones.txt: " + e.getMessage());
        }
        return destino;
    }
}