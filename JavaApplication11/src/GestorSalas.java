import java.io.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Clase de control responsable de:
 *  - Requisito: Gestionar salas (registrar y listar)
 *  - Requisito: Consultar disponibilidad de una sala
 *
 * Responsable: Edison
 */
public class GestorSalas {

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
     * Verifica si la sala existe y si no tiene ninguna reserva confirmada
     * que se traslape con el rango de fecha/hora solicitado.
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
            boolean estaConfirmada = r.getEstado().equalsIgnoreCase("Confirmada");

            if (mismaSala && mismaFecha && estaConfirmada) {
                if (r.seTraslapaCon(horaInicio, horaFin)) {
                    return false; // hay cruce de horario, la sala NO esta disponible
                }
            }
        }
        return true; // ninguna reserva existente cruza con ese horario
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
