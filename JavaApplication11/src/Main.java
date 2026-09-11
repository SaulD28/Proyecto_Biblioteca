import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public class Main {

    public static void main(String[] args) {

        GestorSalas gestorSalas = new GestorSalas("data/salas.txt");
        GestorReservas gestorReservas = new GestorReservas("data/reservas.txt");

        System.out.println("========================================");
        System.out.println(" PointNow - Sistema de reserva de salas");
        System.out.println("========================================\n");

        System.out.println("--- Salas cargadas desde salas.txt ---");
        List<Sala> salas = gestorSalas.listarSalas();
        for (Sala s : salas) {
            System.out.println(s);
        }

        System.out.println("\n--- Consultar disponibilidad de la Sala 3, 20/08/2026, 15:00-16:00 ---");
        boolean disponible1 = gestorSalas.consultarDisponibilidad(
                3, LocalDate.of(2026, 8, 20), LocalTime.of(15, 0), LocalTime.of(16, 0),
                gestorReservas.getReservas()
        );
        System.out.println("Disponible: " + disponible1);

        System.out.println("\n--- Registrar reserva SIN cruce (Sala 3, 17:00-18:00) ---");
        Reserva nuevaReservaValida = new Reserva(
                "R003", 3, "20231234",
                LocalDate.of(2026, 8, 20), LocalTime.of(17, 0), LocalTime.of(18, 0),
                "Confirmada"
        );
        gestorReservas.registrarReserva(nuevaReservaValida);

        System.out.println("\n--- Registrar reserva CON cruce (Sala 3, 15:00-15:30) ---");
        Reserva nuevaReservaConCruce = new Reserva(
                "R004", 3, "20235555",
                LocalDate.of(2026, 8, 20), LocalTime.of(15, 0), LocalTime.of(15, 30),
                "Confirmada"
        );
        gestorReservas.registrarReserva(nuevaReservaConCruce);

        System.out.println("\n--- Calcular duracion de la reserva R003 ---");
        long duracion = gestorSalas.calcularDuracionReserva("R003", gestorReservas.getReservas());
        System.out.println("Duracion: " + duracion + " minutos");

        System.out.println("\n--- Cancelar la reserva R003 ---");
        gestorReservas.cancelarReserva("R003");
    }
}