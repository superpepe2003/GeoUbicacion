package com.utopia.usuario.geoubicacion;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("serial")
public class Usuario implements Serializable {
    public int id;
    public String usuario;
    public Integer pass;
    public String nombre;
    public String usuarioAdmin;
    public List<Ubicacion> Ubicaciones= new ArrayList<Ubicacion>();
}
