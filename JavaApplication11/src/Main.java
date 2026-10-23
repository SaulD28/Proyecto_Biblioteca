import javax.swing.SwingUtilities;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class Main {

    public static void main(String[] args) {
        inicializarDatosSiNoExisten();

        SwingUtilities.invokeLater(() -> {
            VentanaPrincipal ventana = new VentanaPrincipal();
            ventana.setVisible(true);
        });
    }

    private static void inicializarDatosSiNoExisten() {
        File carpetaData = new File("data");
        if (!carpetaData.exists()) {
            carpetaData.mkdirs();
            System.out.println("Carpeta 'data' creada en: " + carpetaData.getAbsolutePath());
        }

        File archivoSalas = new File("data/salas.txt");
        if (!archivoSalas.exists()) {
            try (FileWriter fw = new FileWriter(archivoSalas)) {
                fw.write("1,Nivel 1 - Ala Norte,6,Disponible\n");
                fw.write("2,Nivel 1 - Ala Norte,6,Disponible\n");
                fw.write("3,Nivel 1 - Ala Sur,6,Disponible\n");
                fw.write("4,Nivel 1 - Ala Sur,6,Disponible\n");
                fw.write("5,Nivel 2 - Ala Norte,6,Disponible\n");
                fw.write("6,Nivel 2 - Ala Norte,6,Disponible\n");
                fw.write("7,Nivel 2 - Ala Sur,6,Disponible\n");
                fw.write("8,Nivel 2 - Ala Sur,6,Disponible\n");
                fw.write("9,Nivel 3 - Ala Norte,6,Disponible\n");
                fw.write("10,Nivel 3 - Ala Norte,6,Disponible\n");
                fw.write("11,Nivel 3 - Ala Sur,6,Disponible\n");
                fw.write("12,Nivel 3 - Ala Sur,6,Disponible\n");
                System.out.println("Archivo salas.txt creado con 12 salas de prueba.");
            } catch (IOException e) {
                System.out.println("Error al crear salas.txt: " + e.getMessage());
            }
        }

        File archivoReservas = new File("data/reservas.txt");
        if (!archivoReservas.exists() || tieneFormatoViejo(archivoReservas)) {
            try (FileWriter fw = new FileWriter(archivoReservas)) {
                fw.write("R001,3,1234567890101,4,20/08/2026,14:00,16:00,Confirmada\n");
                fw.write("R002,5,9876543210101,2,20/08/2026,09:00,10:30,Confirmada\n");
                System.out.println("Archivo reservas.txt creado con datos de prueba.");
            } catch (IOException e) {
                System.out.println("Error al crear reservas.txt: " + e.getMessage());
            }
        }
        File archivoEstudiantes = new File("data/estudiantes.txt");
        if (!archivoEstudiantes.exists()) {
            try (FileWriter fw = new FileWriter(archivoEstudiantes)) {
                fw.write("1234567890101,Ana Lopez,ana.lopez@correo.com\n");
                fw.write("9876543210101,Carlos Perez,carlos.perez@correo.com\n");
                System.out.println("Archivo estudiantes.txt creado con datos de prueba.");
            } catch (IOException e) {
                System.out.println("Error al crear estudiantes.txt: " + e.getMessage());
            }
        }
    }

    /**
     * El formato nuevo tiene 8 columnas por linea. Si la primera linea
     * con datos no tiene 8, el archivo es de la version anterior.
     */
    private static boolean tieneFormatoViejo(File archivo) {
        try (BufferedReader br = new BufferedReader(new FileReader(archivo))) {
            String linea;
            while ((linea = br.readLine()) != null) {
                if (!linea.isBlank()) {
                    return linea.split(",").length != 8;
                }
            }
        } catch (IOException e) {
            return true;
        }
        return false;
    }
}