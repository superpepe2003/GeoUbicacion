package com.utopia.usuario.geoubicacion;

import android.annotation.TargetApi;
import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;

@SuppressWarnings("serial")
public class Ubicacion implements Parcelable {
    public int id;
    public double Longitud;
    public double Latitud;

    public Ubicacion(){

    }

    protected Ubicacion(Parcel in) {
        id = in.readInt();
        Longitud = in.readDouble();
        Latitud = in.readDouble();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeDouble(Longitud);
        dest.writeDouble(Latitud);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<Ubicacion> CREATOR = new Creator<Ubicacion>() {
        @Override
        public Ubicacion createFromParcel(Parcel in) {
            return new Ubicacion(in);
        }

        @Override
        public Ubicacion[] newArray(int size) {
            return new Ubicacion[size];
        }
    };
}
