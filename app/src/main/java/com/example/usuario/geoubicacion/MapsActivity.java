package com.example.usuario.geoubicacion;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private List<Marker> marcador = new ArrayList<Marker>();
    double lat = 0.0;
    double lng = 0.0;
    final Handler handler = new Handler();
    Timer timer = new Timer();

    final String NAMESPACE = "http://tempuri.org/";
    final String URL = "http://geolocaliza.ddns.net/WcfGeoLocation/WSGeoUbicacion.svc";
    final String METHOD_NAME = "GetLista";
    final String SOAP_ACTION = "http://tempuri.org/IWSGeolizacion/GetLista";

    ProgressDialog dialogo;

    private String TAG = "Vik";

    private List<Ubicacion> _ubicaciones = new ArrayList<Ubicacion>();
    private Usuario _user = new Usuario();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        Intent in = getIntent();
        _user = (Usuario) in.getSerializableExtra("User");

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        //miUbicacion();

        // Add a marker in Sydney and move the camera
        //LatLng sydney = new LatLng(-34, 151);
        //mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
        //mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));

//        MapsActivity.AsyncCallWS task = new MapsActivity.AsyncCallWS();
//        task.execute();

        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                handler.post(new Runnable() {
                    public void run() {
                        try {
                            //Ejecuta tu AsyncTask!
                            AgregarMarcadores();
                        } catch (Exception e) {
                            Log.e("error", e.getMessage());
                        }
                    }
                });
            }
        };

        timer.schedule(task, 0, 15000);  //ejecutar en intervalo de 3 segundos.
    }

    /*private class AsyncCallWS extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... params) {
            Log.i(TAG, "doInBackground");
            //Permisos();
            CargarDatos();
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            dialogo.dismiss();
            AgregarMarcadores();
        }

        @Override
        protected void onPreExecute() {
            dialogo = new ProgressDialog(MapsActivity.this);
            dialogo.setMessage("Cargando datos...");
            dialogo.setIndeterminate(false);
            dialogo.setCancelable(false);
            dialogo.show();
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            Log.i(TAG, "onProgressUpdate");
        }

    }*/

    private Marker agregarMarcador(double lat, double lng, String nom) {
        LatLng coordenadas = new LatLng(lat, lng);
        CameraUpdate miUbicacion = CameraUpdateFactory.newLatLngZoom(coordenadas, 20);

        //mMap.clear();
        Marker miMarcador = mMap.addMarker(new MarkerOptions()
                .position(coordenadas)
                .title(nom)
                .icon(BitmapDescriptorFactory.fromBitmap(resizeMapIcons(R.drawable.imageubi, 100, 100))));
        mMap.animateCamera(miUbicacion);
        return miMarcador;
    }

    public Bitmap resizeMapIcons(int iconName, int width, int height) {
        BitmapDrawable b = (BitmapDrawable) getResources().getDrawable(iconName);
        Bitmap imageBitmap = b.getBitmap();
        Bitmap resizedBitmap = Bitmap.createScaledBitmap(imageBitmap, width, height, false);
        return resizedBitmap;
    }

    private void actualizarUbicacion(Location location) {
        if (location != null) {
            lat = location.getLatitude();
            lng = location.getLongitude();
            agregarMarcador(lat, lng, "hello");
        }
    }

    /*LocationListener locListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            actualizarUbicacion(location);
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
    };*/

    @TargetApi(23)
    /*private void Permisos() {

       *//* if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }*//*

        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        //actualizarUbicacion(location);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 15000, 0, locListener);

    }*/


    /*public void CargarDatos() {
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
            CargarResultados(resultado);

        } catch (Exception e) {
            Log.e("ERROR", e.getMessage());
        }
    }*/

    /*public void CargarResultados(SoapObject soapobject) {
        _user.Ubicaciones.clear();

        SoapObject userResult = (SoapObject) soapobject.getProperty(0);
        SoapObject userUbi = (SoapObject) userResult.getProperty("Ubicaciones");
        SoapObject userUbiModel = (SoapObject) userUbi.getProperty("UbicacionModel");

        _user.id = Integer.parseInt(userResult.getProperty(1).toString());
        _user.codigo = Integer.parseInt(userResult.getProperty(0).toString());
        _user.nombre = userResult.getProperty(2).toString();
        _user.pass = userResult.getProperty(3).toString();
        _user.usuario = userResult.getProperty(5).toString();

        SoapObject miUbicacion = (SoapObject) userResult.getProperty(4);

        for (int j = 0; j < miUbicacion.getPropertyCount(); j++) {
            SoapObject u = (SoapObject) miUbicacion.getProperty(j);
            Ubicacion ubi = new Ubicacion();
            ubi.id = Integer.parseInt(u.getProperty(0).toString());
            ubi.Latitud = Double.parseDouble(u.getProperty(1).toString());
            ubi.Longitud = Double.parseDouble(u.getProperty(2).toString());
            ubi.Nombre = u.getProperty(3).toString();
            ubi.UsuarioId = _user.id;
            _user.Ubicaciones.add(ubi);
        }

    }*/

    public void AgregarMarcadores() {
        if (!marcador.isEmpty()) {
            for(Marker mar:marcador)
                mar.remove();
            marcador.clear();
        }



        for (Ubicacion ubi : _user.Ubicaciones)
            marcador.add(agregarMarcador(ubi.Latitud, ubi.Longitud, ubi.Nombre));
    }

//        }
//        if(marcador.isEmpty()) {
//            for (Ubicacion ubi : _user.Ubicaciones) {
//                Boolean f = false;
//                for (Marker mac : marcador) {
//                    if (mac.getTitle() == ubi.Nombre) {
//                        mac.setPosition(new LatLng(ubi.Latitud, ubi.Longitud));
//                        f = true;
//                    }
//                }
//                if (!f)
//                    marcador.add(agregarMarcador(ubi.Latitud, ubi.Longitud, ubi.Nombre));
//            }
//        }
//        else {


}
