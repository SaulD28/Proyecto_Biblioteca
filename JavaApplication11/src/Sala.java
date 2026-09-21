import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Representa un Working Point (sala de estudio grupal) de la biblioteca.
 * Responsable de conocer su propia informacion y su estado actual.
 */
public class Sala {

    private int numero;
    private String ubicacion;
    private int capacidad;
    private String estado; // "Disponible" u "Ocupada"

    public Sala() {
    }

    public Sala(int numero, String ubicacion, int capacidad, String estado) {
        this.numero = numero;
        this.ubicacion = ubicacion;
        this.capacidad = capacidad;
        this.estado = estado;
    }

    public int getNumero() {
        return numero;
    }

    public void setNumero(int numero) {
        this.numero = numero;
    }

    public String getUbicacion() {
        return ubicacion;
    }

    public void setUbicacion(String ubicacion) {
        this.ubicacion = ubicacion;
    }

    public int getCapacidad() {
        return capacidad;
    }

    public void setCapacidad(int capacidad) {
        this.capacidad = capacidad;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    /**
     * Convierte la sala a una linea de texto separada por comas,
     * lista para guardarse en el fichero salas.txt
     */
    public String toCsv() {
        return numero + "," + ubicacion + "," + capacidad + "," + estado;
    }

    /**
     * Crea una Sala a partir de una linea leida del fichero salas.txt
     */
    public static Sala fromCsv(String linea) {
        String[] partes = linea.split(",");
        int numero = Integer.parseInt(partes[0].trim());
        String ubicacion = partes[1].trim();
        int capacidad = Integer.parseInt(partes[2].trim());
        String estado = partes[3].trim();
        return new Sala(numero, ubicacion, capacidad, estado);
    }

    @Override
    public String toString() {
        return "Sala " + numero + " (" + ubicacion + ") - capacidad: " + capacidad
                + " personas - estado: " + estado;
    }
}