package com.utopia.usuario.geoubicacion;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;

import java.io.ByteArrayOutputStream;
import java.io.File;

public class cConeccion {

    public static final String NAMESPACE = "http://tempuri.org/";
    public static final String URL="http://geolocaliza.ddns.net/WcfGeoLocation/WSGeoUbicacion.svc";
    //public static final String URL="http://utopiasoft.duckdns.org/WcfGeoLocation/WSGeoUbicacion.svc";
    public static final String METHOD_NAME_EXISTE = "Existe";
    public static final String SOAP_ACTION_EXISTE = "http://tempuri.org/IWSGeolizacion/Existe";
    public static final String METHOD_NAME_GRABAR_USUARIO = "GrabarU";
    public static final String SOAP_ACTION_GRABAR_USUARIO = "http://tempuri.org/IWSGeolizacion/GrabarU";
    public static final String METHOD_NAME_GET_SUPERVISADOS = "GetSupervisado";
    public static final String SOAP_ACTION_GET_SUPERVISADOS ="http://tempuri.org/IWSGeolizacion/GetSupervisado";
    public static final String METHOD_NAME_GRABAR_UBICACION = "Grabar";
    public static final String SOAP_ACTION_GRABAR_UBICACION = "http://tempuri.org/IWSGeolizacion/Grabar";
    public static final String METHOD_NAME_GET_UBICACION = "GetUbicaciones";
    public static final String SOAP_ACTION_GET_UBICACION = "http://tempuri.org/IWSGeolizacion/GetUbicaciones";

    public static String foto="";

    public static Bitmap ConvertiABitmap(String compress)
    {
        String base64String = compress;
        //String base64Image = base64String.split(",")[1];

        byte[] decodedString = Base64.decode(base64String, Base64.DEFAULT);
        Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
        /*ByteArrayOutputStream out = new ByteArrayOutputStream();
        decodedByte.compress(Bitmap.CompressFormat.PNG, 100, out);
        Bitmap decoded = BitmapFactory.decodeStream(new ByteArrayInputStream(out.toByteArray()));*/

        return decodedByte;
    }

    public static String imagenToByte(String foto)
    {
        File file= new File(foto);
        if(file.exists()) {
            Bitmap bm = BitmapFactory.decodeFile(foto);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bm.compress(Bitmap.CompressFormat.JPEG, 75, baos);
            byte[] b = baos.toByteArray();
            return Base64.encodeToString(b, Base64.DEFAULT);
        }
        return null;
    }
}
