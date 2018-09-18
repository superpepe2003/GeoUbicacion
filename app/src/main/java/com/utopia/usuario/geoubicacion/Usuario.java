package com.utopia.usuario.geoubicacion;

import android.annotation.TargetApi;
import android.graphics.Bitmap;
import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("serial")
public class Usuario implements Parcelable {
    public int id;
    public String usuario;
    public Integer pass;
    public String nombre;
    public Usuario uAdmin;
    public String usuarioAdmin;
    public List<Usuario> Supervisados;
    public Bitmap foto;
    public Ubicacion Ubicaciones;

    public Usuario(){
        if (Supervisados==null)
            Supervisados= new ArrayList<Usuario>();
    }

    protected Usuario(Parcel in) {
        if (Supervisados==null)
            Supervisados= new ArrayList<Usuario>();
        id = in.readInt();
        usuario = in.readString();
        if (in.readByte() == 0) {
            pass = null;
        } else {
            pass = in.readInt();
        }
        nombre = in.readString();
        uAdmin = in.readParcelable(Usuario.class.getClassLoader());
        usuarioAdmin = in.readString();
        Supervisados = in.createTypedArrayList(Usuario.CREATOR);
        foto = in.readParcelable(Bitmap.class.getClassLoader());
        Ubicaciones = in.readParcelable(Ubicacion.class.getClassLoader());
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeString(usuario);
        if (pass == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeInt(pass);
        }
        dest.writeString(nombre);
        dest.writeParcelable(uAdmin, flags);
        dest.writeString(usuarioAdmin);
        dest.writeTypedList(Supervisados);
        dest.writeParcelable(foto, flags);
        dest.writeParcelable(Ubicaciones, flags);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<Usuario> CREATOR = new Creator<Usuario>() {
        @Override
        public Usuario createFromParcel(Parcel in) {
            return new Usuario(in);
        }

        @Override
        public Usuario[] newArray(int size) {
            return new Usuario[size];
        }
    };
}
