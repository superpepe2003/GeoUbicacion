package com.utopia.usuario.geoubicacion;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Icon;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Bundle;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.support.v7.widget.SearchView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
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

import static android.app.PendingIntent.getActivity;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private List<Marker> marcador = new ArrayList<Marker>();
    double lat = 0.0;
    double lng = 0.0;
    final Handler handler = new Handler();
    Timer timer = new Timer();

    final String NAMESPACE = "http://tempuri.org/";
    final String URL = "http://geolocaliza.ddns.net/WcfGeoLocation/WSGeoUbicacion.svc";
    final String METHOD_NAME = "Existe";
    final String METHOD_NAME2 = "Remove";
    final String SOAP_ACTION = "http://tempuri.org/IWSGeolizacion/Existe";
    final String SOAP_ACTION2 = "http://tempuri.org/IWSGeolizacion/Remove";

    
    ProgressDialog dialogo;


    private String TAG = "Vik";

    private List<Ubicacion> _ubicaciones = new ArrayList<Ubicacion>();
    private Usuario _user = new Usuario();

    MenuItem btnRemove;

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

        Toolbar toolbar = (Toolbar) findViewById(R.id.tool_bar);
        setSupportActionBar(toolbar);
        MapsActivity.this.setTitle(_user.nombre);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_mapa, menu);
        try {
            SearchView searchView = (SearchView) menu.findItem(R.id.menu_buscar).getActionView();
            searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                @Override
                public boolean onQueryTextSubmit(String query) {
                    //Toast.makeText(MapsActivity.this,"Buscar", Toast.LENGTH_LONG).show();
                    Ubicacion ubi = retornarUbicacion(query);
                    if(ubi!=null) {
                        new dialogFragmentMap(new LatLng(ubi.Latitud,ubi.Longitud), query).show(getSupportFragmentManager(), null);
                    }
                    else
                        Toast.makeText(MapsActivity.this,"Ubicacion no encontrada", Toast.LENGTH_LONG).show();
                    return false;
                }

                @Override
                public boolean onQueryTextChange(String newText) {
                    return false;
                }
            });
        }
        catch (Exception ex)
        {
            //Log.e("Error", ex.getMessage());
        }


        btnRemove = (MenuItem) findViewById(R.id.menu_elimina);
        return true;
    }

    public Ubicacion retornarUbicacion(String nom)
    {
        Ubicacion _ubicacion=null;
        for(Ubicacion ub:_user.Ubicaciones)
        {
            if(ub.Nombre.equalsIgnoreCase(nom))
            {
                _ubicacion=ub;
                break;
            }
        }
        return _ubicacion;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem opcMenu) {
        int id = opcMenu.getItemId();
        if (id == R.id.menu_actualiza) {
            AsyncCallWS miTarea= new AsyncCallWS(MapsActivity.this);
            miTarea.execute(1);
        }
        if (id == R.id.menu_elimina) {
            createDialog().show();
        }
        if (id == R.id.menu_buscar) {
            createDialog().show();
        }
        return true;
    }

    public AlertDialog createDialog(){
        AlertDialog.Builder builder=new AlertDialog.Builder(this);

        builder.setTitle("Borrar Ubicaciones")
                .setMessage("¿Seguro desea borrar las Ubicaciones?")
                .setPositiveButton("Confirmar",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                AsyncCallWS miTarea= new AsyncCallWS(MapsActivity.this);
                                miTarea.execute(2, _user.codigo);
                            }
                        })
                .setNegativeButton("Cancelar",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        });
        return builder.create();
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

        AsyncCallWS miTarea= new AsyncCallWS(this);
        miTarea.execute(1);

        //miUbicacion();

        // Add a marker in Sydney and move the camera
        //LatLng sydney = new LatLng(-34, 151);
        //mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
        //mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));

//        MapsActivity.AsyncCallWS task = new MapsActivity.AsyncCallWS();
//        task.execute();

//        TimerTask task = new TimerTask() {
//            @Override
//            public void run() {
//                handler.post(new Runnable() {
//                    public void run() {
//                        try {
//                            //Ejecuta tu AsyncTask!
//                            //AgregarMarcadores();
//                            AsyncCallWS miTarea= new AsyncCallWS();
//                            miTarea.execute();
//
//                        } catch (Exception e) {
//                            Log.e("error", e.getMessage());
//                        }
//                    }
//                });
//            }
//        };
//
//        timer.schedule(task, 0, 15000);  //ejecutar en intervalo de 3 segundos.
    }


    private class AsyncCallWS extends AsyncTask<Integer, Void, Boolean> {

        Context ctx;
        int param=0;

        public AsyncCallWS(Context ctx)
        {
            this.ctx=ctx;
        }
        @Override
        protected Boolean doInBackground(Integer... params) {
            param = params[0];
            //Log.i(TAG, "doInBackground");
            //Permisos();
            if(param==1)
                CargarDatos();
            if(param==2) {
                return EliminarDatos(params[1]);
            }
            return false;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            dialogo.dismiss();
            if(param==1) {
                AgregarMarcadores();
//                if(_user.Ubicaciones.isEmpty())
//                    btnRemove.setIcon(R.drawable.ic_eliminardisable);
//                else
//                    btnRemove.setIcon(R.drawable.ic_eliminar);
            }
            if(param==2) {
                if(result) {
                    Toast.makeText(MapsActivity.this, "Las Ubicaciones fueron eliminados", Toast.LENGTH_LONG).show();
                    BorrarMarcadores();
                    //btnRemove.setIcon(ctx.getResources().getDrawable(R.drawable.ic_eliminardisable));
                    //btnRemove.setIcon(R.drawable.ic_eliminardisable);
                }
                else
                    Toast.makeText(MapsActivity.this, "Las Ubicaciones no fueron eliminados", Toast.LENGTH_LONG).show();
            }
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
            //Log.i(TAG, "onProgressUpdate");
        }

    }

    private Marker agregarMarcador(double lat, double lng, String nom) {
        LatLng coordenadas = new LatLng(lat, lng);
        CameraUpdate miUbicacion = CameraUpdateFactory.newLatLngZoom(coordenadas, 14);

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

    public Boolean EliminarDatos(int codigo) {
        try {

            // Modelo el request
            SoapObject request = new SoapObject(NAMESPACE, METHOD_NAME2);
            request.addProperty("codigo", codigo);
            //request.addProperty("Param", "valor"); // Paso parametros al WS

            // Modelo el Sobre
            SoapSerializationEnvelope sobre = new SoapSerializationEnvelope(SoapEnvelope.VER10);
            sobre.dotNet = true;
            sobre.setOutputSoapObject(request);

            // Modelo el transporte
            HttpTransportSE transporte = new HttpTransportSE(URL);

            // Llamada
            transporte.call(SOAP_ACTION2, sobre);

            // Resultado
            Boolean resultado = Boolean.parseBoolean(sobre.getResponse().toString());
            //CargarResultados(resultado);
            return resultado;

        } catch (Exception e) {
            //Log.e("ERROR", e.getMessage());
            return false;
        }
    }

    public void CargarDatos() {
        try {

            // Modelo el request
            SoapObject request = new SoapObject(NAMESPACE, METHOD_NAME);
            request.addProperty("usuario", _user.usuario);
            request.addProperty("pass", _user.pass);
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
            //Log.e("ERROR", e.getMessage());
        }
    }

    public void CargarResultados(SoapObject resultado) {
        _user.Ubicaciones.clear();

        int cant= resultado.getPropertyCount();
        if(cant>0) {
            _user.Ubicaciones.clear();

            SoapObject miUbicacion = (SoapObject) resultado.getProperty(4);

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
        }
    }

    public void AgregarMarcadores() {
        BorrarMarcadores();

        for (Ubicacion ubi : _user.Ubicaciones)
            marcador.add(agregarMarcador(ubi.Latitud, ubi.Longitud, ubi.Nombre));
    }

    public void BorrarMarcadores(){
        if (!marcador.isEmpty()) {
            for(Marker mar:marcador)
                mar.remove();
            marcador.clear();
        }
    }

    public Marker BuscarMarcador(String nombre)
    {
        for(Marker mar:marcador)
        {
            if(mar.getTitle().toString()==nombre)
                return mar;
        }
        return null;
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
