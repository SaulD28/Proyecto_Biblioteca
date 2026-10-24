import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class GestorEstudiantes {

    /** Con esta cantidad de inasistencias el estudiante ya no puede reservar. */
    public static final int LIMITE_INASISTENCIAS = 3;

    private List<Estudiante> estudiantes;
    private String rutaArchivo;

    public GestorEstudiantes(String rutaArchivo) {
        this.estudiantes = new ArrayList<>();
        this.rutaArchivo = rutaArchivo;
        cargarDesdeArchivo();
    }

    /**
     * Requisito funcional: Gestionar estudiantes (listar).
     */
    public List<Estudiante> listarEstudiantes() {
        return estudiantes;
    }

    public Estudiante buscarPorCarne(String carne) {
        for (Estudiante e : estudiantes) {
            if (e.getCarne().equalsIgnoreCase(carne)) {
                return e;
            }
        }
        return null;
    }

    /**
     * Requisito funcional: Gestionar estudiantes (registrar).
     * Valida los datos y, si todo esta bien, guarda al estudiante.
     *
     * @return null si se registro correctamente; si no, el motivo del rechazo.
     */
    public String registrarEstudiante(Estudiante nuevo) {
        String carne = nuevo.getCarne() == null ? "" : nuevo.getCarne().trim();
        String nombre = nuevo.getNombre() == null ? "" : nuevo.getNombre().trim();
        String correo = nuevo.getCorreo() == null ? "" : nuevo.getCorreo().trim();

        if (!carne.matches("[A-Za-z0-9]{6,13}")) {
            return "El DPI o carne debe tener de 6 a 13 caracteres (solo letras y numeros).";
        }
        if (nombre.isEmpty()) {
            return "Debes ingresar el nombre del estudiante.";
        }
        if (nombre.contains(",") || nombre.length() > 60) {
            return "El nombre no puede tener comas ni mas de 60 caracteres.";
        }
        if (!correo.matches("[^@\\s,]+@[^@\\s,]+\\.[^@\\s,]+")) {
            return "El correo no tiene un formato valido (ejemplo: nombre@correo.com).";
        }
        if (buscarPorCarne(carne) != null) {
            return "Ya existe un estudiante con el carne " + carne + ".";
        }

        nuevo.setCarne(carne);
        nuevo.setNombre(nombre);
        nuevo.setCorreo(correo);
        estudiantes.add(nuevo);
        guardarEnArchivo();
        System.out.println("Estudiante registrado: " + nuevo);
        return null;
    }

    /**
     * Requisito funcional: Registrar inasistencia (no-show).
     * Suma una inasistencia al estudiante y guarda el cambio.
     *
     * @return el total de inasistencias acumuladas, o -1 si el estudiante no existe.
     */
    public int registrarInasistencia(String carne) {
        Estudiante e = buscarPorCarne(carne);
        if (e == null) {
            return -1;
        }
        e.setInasistencias(e.getInasistencias() + 1);
        guardarEnArchivo();
        return e.getInasistencias();
    }

    /**
     * Requisito funcional: Restringir reservas a estudiantes con inasistencias acumuladas.
     *
     * @return null si puede reservar; si no, el motivo de la restriccion.
     *         Un DPI o carne que no esta registrado como estudiante no se restringe.
     */
    public String motivoRestriccion(String carne) {
        Estudiante e = buscarPorCarne(carne);
        if (e != null && e.getInasistencias() >= LIMITE_INASISTENCIAS) {
            return e.getNombre() + " tiene " + e.getInasistencias() + " inasistencias (limite "
                    + LIMITE_INASISTENCIAS + ") y no puede reservar.";
        }
        return null;
    }

    private void cargarDesdeArchivo() {
        File archivo = new File(rutaArchivo);
        if (!archivo.exists()) {
            return;
        }
        try (BufferedReader br = new BufferedReader(new FileReader(archivo))) {
            String linea;
            while ((linea = br.readLine()) != null) {
                if (linea.isBlank()) {
                    continue;
                }
                try {
                    estudiantes.add(Estudiante.fromCsv(linea));
                } catch (ArrayIndexOutOfBoundsException e) {
                    System.out.println("Linea invalida en estudiantes.txt, se ignora: " + linea);
                }
            }
        } catch (IOException e) {
            System.out.println("Error al leer estudiantes.txt: " + e.getMessage());
        }
    }

    private void guardarEnArchivo() {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(rutaArchivo))) {
            for (Estudiante e : estudiantes) {
                bw.write(e.toCsv());
                bw.newLine();
            }
        } catch (IOException e) {
            System.out.println("Error al guardar estudiantes.txt: " + e.getMessage());
        }
    }
}