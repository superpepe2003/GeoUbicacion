package com.utopia.usuario.geoubicacion;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("serial")
public class Usuario implements Serializable {
    public int id;
    public String usuario;
    public String pass;
    public String nombre;
    public int codigo;
    public List<Ubicacion> Ubicaciones= new ArrayList<Ubicacion>();
}
