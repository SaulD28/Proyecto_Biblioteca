/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

/**
 *
 * @author picor
 */
public class Sala {
    
    private int numero;
    private String ubicacion;
    private int  capacidad;
    private String estado;
    
    public Sala(){
        
    }
    
    public Sala(int numero, String ubicacion, int capacidad, String estado){
        this.numero = numero;
        this.ubicacion = ubicacion;
        this.capacidad = capacidad;
        this.estado = estado;
    }
    
    public int getNumero() {
        return numero;
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
    
    
    public static Sala FromCsv(String linea){
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
