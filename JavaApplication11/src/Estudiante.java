/**
 * Representa a un estudiante que puede reservar un Working Point.
 * Se identifica por su carne (o DPI) y lleva la cuenta de sus inasistencias.
 */
public class Estudiante {

    private String carne;
    private String nombre;
    private String correo;
    private int inasistencias;

    public Estudiante() {
    }

    public Estudiante(String carne, String nombre, String correo) {
        this(carne, nombre, correo, 0);
    }

    public Estudiante(String carne, String nombre, String correo, int inasistencias) {
        this.carne = carne;
        this.nombre = nombre;
        this.correo = correo;
        this.inasistencias = inasistencias;
    }

    public String getCarne() { return carne; }
    public void setCarne(String carne) { this.carne = carne; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getCorreo() { return correo; }
    public void setCorreo(String correo) { this.correo = correo; }

    public int getInasistencias() { return inasistencias; }
    public void setInasistencias(int inasistencias) { this.inasistencias = inasistencias; }

    /**
     * Convierte el estudiante a una linea de texto separada por comas,
     * lista para guardarse en el fichero estudiantes.txt
     */
    public String toCsv() {
        return carne + "," + nombre + "," + correo + "," + inasistencias;
    }

    /**
     * Crea un Estudiante a partir de una linea leida de estudiantes.txt.
     * Acepta tambien el formato anterior de 3 columnas (sin inasistencias).
     */
    public static Estudiante fromCsv(String linea) {
        String[] p = linea.split(",");
        int inasistencias = 0;
        if (p.length >= 4) {
            try {
                inasistencias = Integer.parseInt(p[3].trim());
            } catch (NumberFormatException e) {
                inasistencias = 0;
            }
        }
        return new Estudiante(p[0].trim(), p[1].trim(), p[2].trim(), inasistencias);
    }

    @Override
    public String toString() {
        return carne + " - " + nombre + " (" + correo + ")";
    }
}