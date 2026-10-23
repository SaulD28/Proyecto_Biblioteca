
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

/**
 *
 * @author picor
 */
public class GestorEstudiantes {
    
    private List<Estudiante> estudiantes;
    private String rutaArchivo;
    
    public GestorEstudiantes(String rutaArchivo){
        this.estudiantes = new ArrayList<>();
        this.rutaArchivo = rutaArchivo;
        cargarDesdeArchivo();
    }
    
    public List<Estudiante> listarEstudiantes(){
        return estudiantes;
    }
    
    public Estudiante buscarPorCarne(String carne){
        for(Estudiante e : estudiantes){
            if(e.getCarne().equalsIgnoreCase(carne)){
                return e;
            }
        }
        return null;
    }
    
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
    
    private void cargarDesdeArchivo(){
        File archivo = new File(rutaArchivo);
        if(!archivo.exists()){
            return;
        }
        
        try(BufferedReader br = new BufferedReader(new FileReader(archivo))){
            String linea;
            while((linea = br.readLine()) != null ){
                if(linea.isBlank()){
                    continue;
                }
                try{
                    estudiantes.add(Estudiante.fromCsv(linea));
                    
                }catch (ArrayIndexOutOfBoundsException e){
                    System.out.println("Linea invalida en estudiantes.txt, se ignora: " + linea);
                }
            }
                
            }catch(IOException e){
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
