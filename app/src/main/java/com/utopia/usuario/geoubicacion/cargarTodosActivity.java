package com.utopia.usuario.geoubicacion;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import org.ksoap2.*;
import org.ksoap2.serialization.Marshal;
import org.ksoap2.serialization.PropertyInfo;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapPrimitive;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;
import org.w3c.dom.Element;
import org.w3c.dom.Text;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;

import java.io.IOException;
import java.util.List;
import java.util.Timer;

import static android.app.PendingIntent.getActivity;


public class cargarTodosActivity extends AppCompatActivity {

    final String NAMESPACE = "http://tempuri.org/";
    final String URL="http://geolocaliza.ddns.net/WcfGeoLocation/WSGeoUbicacion.svc";
    final String METHOD_NAME = "Grabar";
    final String SOAP_ACTION = "http://tempuri.org/IWSGeolizacion/Grabar";

    ProgressDialog dialogo;
    public TextView txtCarga;

    String _user;
    int _codigo;
    Double _long;
    Double _Lat;

    private String TAG ="Vik";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cargar_todos);

        Intent in= getIntent();
        Bundle b = in.getExtras();
        _codigo=b.getInt("codigo");
        _user=b.getString("user");

        txtCarga=(TextView)findViewById(R.id.txtCarga);

        CargarUbicacion();
        Cargar();

    }

    @Override
    protected void onDestroy() {
        locListener=null;
        super.onDestroy();
    }

    private class AsyncCallWS extends AsyncTask<Void, Void, String> {
        @Override
        protected String doInBackground(Void... params) {
            //Log.i(TAG, "doInBackground");
            String element = ejecutar();
            return element;
        }

        @Override
        protected void onPostExecute(String result) {
            txtCarga.setText(result);
            /*String s= result.toString();
            getActivity()..txtCarga.setText(s.toString() + "");*/

        }

        @Override
        protected void onPreExecute() {
            /*dialogo = new ProgressDialog(cargarTodosActivity.this);
            dialogo.setMessage("Cargando datos...");
            dialogo.setIndeterminate(false);
            dialogo.setCancelable(false);
            dialogo.show();*/
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            //Log.i(TAG, "onProgressUpdate");
        }

    }

    public String ejecutar()
    {
        try {
            //cargarTodosActivity.this.txtCarga.setText("Conectando");
            // Modelo el request
            SoapObject request = new SoapObject(NAMESPACE, METHOD_NAME);

            request.addProperty("codigo",_codigo);
            request.addProperty("lat",_Lat);
            request.addProperty("lng",_long);
            request.addProperty("nombre", _user + "");

            // Modelo el Sobre
            SoapSerializationEnvelope sobre = new SoapSerializationEnvelope(SoapEnvelope.VER11);

            sobre.implicitTypes = true;
            sobre.dotNet = true;
            sobre.encodingStyle = SoapSerializationEnvelope.XSD;

            sobre.setOutputSoapObject(request);

            MarshalDouble md = new MarshalDouble();
            md.register(sobre);

            // Modelo el transporte
            HttpTransportSE transporte = new HttpTransportSE(URL);

            // Llamada
            transporte.call(SOAP_ACTION, sobre);

            // Resultado
            String resultado = sobre.getResponse().toString();


            //Log.i("Resultado", resultado.toString());

            return "Ubicacion Enviada!";

        } catch (Exception e) {
            //Log.e("ERROR", e.getMessage());
            return "Error al enviar Ubicacion!";
        }
    }


    @TargetApi(23)
    private void CargarUbicacion() {

       /* if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }*/

        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        if (    ActivityCompat.checkSelfPermission( this, android.Manifest.permission.ACCESS_FINE_LOCATION ) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission( this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        if(location!=null) {
            _long = location.getLongitude();
            _Lat = location.getLatitude();
            Cargar();
        }
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,10000,0,locListener);

    }

    LocationListener locListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            _long=location.getLongitude();
            _Lat=location.getLatitude();
            txtCarga.setText("");
            Cargar();
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {

        }

        @Override
        public void onProviderEnabled(String provider) {

        }

        @Override
        public void onProviderDisabled(String provider) {

        }
    };

    public void Cargar()
    {
        AsyncCallWS task = new AsyncCallWS();
        task.execute();
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        txtCarga.setText("");
    }

    public void ejecutar10000()
    {
        try {

            // Modelo el request
            SoapObject request = new SoapObject(NAMESPACE, METHOD_NAME);
            //request.addProperty("Param", "valor"); // Paso parametros al WS

            // Modelo el Sobre
            SoapSerializationEnvelope sobre = new SoapSerializationEnvelope(SoapEnvelope.VER10);
            sobre.dotNet = true;
            sobre.setOutputSoapObject(request);

            // Modelo el transporte
            HttpTransportSE transporte = new HttpTransportSE(URL);

            // Llamada
            transporte.call(SOAP_ACTION, sobre);

            // Resultado
            SoapObject resultado = (SoapObject) sobre.getResponse();
            SoapObject userResult = (SoapObject) resultado.getProperty("UsuarioModel");
            SoapObject userUbi = (SoapObject) userResult.getProperty("Ubicaciones");
            SoapObject userUbiModel = (SoapObject) userUbi.getProperty("UbicacionModel");

            String[] cargardatos= new String[resultado.getPropertyCount() * 5];
            int fila=0;
            for (int i=0; i<resultado.getPropertyCount();i++){
                SoapObject datos=(SoapObject) resultado.getProperty(i);
                //Log.e("Valor de codigo: ", datos.getProperty(0).toString());
                //Log.e("Valor de id: ", datos.getProperty(1).toString());
               // Log.e("Valor de nombre: ", datos.getProperty(2).toString());
                //Log.e("Valor de pass: ", datos.getProperty(3).toString());

                //Log.e("Valor de user: ", datos.getProperty(5).toString());

                SoapObject miUbicacion= (SoapObject)datos.getProperty(4);

                for(int j=0;j<miUbicacion.getPropertyCount();j++)
                {
                    SoapObject u=(SoapObject) miUbicacion.getProperty(j);
                  //  Log.e("Nombre de la Ubi: ", u.getProperty(3).toString());
                }

            }


            //Log.i("Resultado", resultado.toString());
            //Log.i("User Ubicaciones", userUbi.toString());


        } catch (Exception e) {
            //Log.e("ERROR", e.getMessage());
        }
    }
}
//
//class MarshalDouble implements Marshal {
//    public Object readInstance(XmlPullParser parser, String namespace, String name,
//                               PropertyInfo expected) throws IOException, XmlPullParserException {
//
//        return Double.parseDouble(parser.nextText());
//    }
//
//
//    public void register(SoapSerializationEnvelope cm)  {
//        cm.addMapping(cm.xsd, "double", Double.class, this);
//    }
//
//
//    public void writeInstance(XmlSerializer writer, Object obj) throws IOException {
//        writer.text(obj.toString());
//    }
//}
