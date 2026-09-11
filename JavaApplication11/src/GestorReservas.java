import java.io.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class GestorReservas {

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

    /**
     * Requisito funcional: Validar cruce de horario.
     */
    public boolean validarCruceHorario(int numeroSala, LocalDate fecha, LocalTime horaInicio, LocalTime horaFin) {
        for (Reserva r : reservas) {
            boolean mismaSala = r.getNumeroSala() == numeroSala;
            boolean mismaFecha = r.getFecha().equals(fecha);
            boolean estaConfirmada = r.getEstado().equalsIgnoreCase("Confirmada");

            if (mismaSala && mismaFecha && estaConfirmada && r.seTraslapaCon(horaInicio, horaFin)) {
                System.out.println("Cruce de horario detectado con la reserva " + r.getIdReserva());
                return true;
            }
        }
        return false;
    }

    /**
     * Requisito funcional: Registrar reserva.
     */
    public boolean registrarReserva(Reserva nuevaReserva) {
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

        reservas.add(nuevaReserva);
        guardarEnArchivo();
        System.out.println("Reserva registrada con exito: " + nuevaReserva);
        return true;
    }

    /**
     * Requisito funcional: Cancelar una reserva.
     * Busca la reserva por su id; si existe y esta Confirmada, la marca
     * como Cancelada y actualiza el archivo.
     */
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