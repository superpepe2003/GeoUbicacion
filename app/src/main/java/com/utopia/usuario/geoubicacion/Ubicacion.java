package com.utopia.usuario.geoubicacion;

import java.io.Serializable;

public class Ubicacion implements Serializable {
    public int id;
    public String Nombre;
    public double Longitud;
    public double Latitud;
    public int UsuarioId;
}
