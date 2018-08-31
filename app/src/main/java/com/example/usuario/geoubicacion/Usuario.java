package com.example.usuario.geoubicacion;

import java.util.ArrayList;
import java.util.List;

public class Usuario {
    public int id;
    public String usuario;
    public String pass;
    public String nombre;
    public int codigo;
    public List<Ubicacion> Ubicaciones= new ArrayList<Ubicacion>();
}
