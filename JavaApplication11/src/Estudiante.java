/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

/**
 *
 * @author picor
 */
public class Estudiante {

    private String carne;
    private String nombre;
    private String correo;

    public Estudiante() {
    }

    public Estudiante(String carne, String nombre, String correo) {
        this.carne = carne;
        this.nombre = nombre;
        this.correo = correo;
    }

    public String getCarne() { return carne; }
    public void setCarne(String carne) { this.carne = carne; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getCorreo() { return correo; }
    public void setCorreo(String correo) { this.correo = correo; }

    /**
     * Convierte el estudiante a una linea de texto separada por comas,
     * lista para guardarse en el fichero estudiantes.txt
     */
    public String toCsv() {
        return carne + "," + nombre + "," + correo;
    }

    /**
     * Crea un Estudiante a partir de una linea leida de estudiantes.txt
     */
    public static Estudiante fromCsv(String linea) {
        String[] p = linea.split(",");
        return new Estudiante(p[0].trim(), p[1].trim(), p[2].trim());
    }

    @Override
    public String toString() {
        return carne + " - " + nombre + " (" + correo + ")";
    }
}
