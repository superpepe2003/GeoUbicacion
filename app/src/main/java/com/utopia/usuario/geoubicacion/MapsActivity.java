package com.utopia.usuario.geoubicacion;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.Icon;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.ThumbnailUtils;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.support.v7.widget.SearchView;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.common.util.concurrent.HandlerExecutor;
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
import org.ksoap2.serialization.Marshal;
import org.ksoap2.serialization.PropertyInfo;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import static android.app.PendingIntent.getActivity;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private List<Marker> marcador = new ArrayList<Marker>();
    double lat = 0.0;
    double lng = 0.0;
    final Handler handler = new Handler();
    Timer timer = new Timer();

    private static final String PREF_NAME = "reg";
    public static final String KEY_USER = "User";
    public static final String KEY_PASS = "Id";

    private DrawerLayout dl;
    private ActionBarDrawerToggle abdt;
    private NavigationView nav_view;
    ImageView img;
    
    ProgressDialog dialogo;

    double _Lat,_Long;

    private String TAG = "Vik";

    private List<Ubicacion> _ubicaciones = new ArrayList<Ubicacion>();
    private Usuario _user = new Usuario();

    MenuItem btnRemove;
    TimerTask task;
    LocationManager locationManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        Intent in = getIntent();
        //_user = (Usuario) in.getSerializableExtra("User");
        _user= in.getParcelableExtra("User");



        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        //TOOLBAR
        Toolbar toolbar = (Toolbar) findViewById(R.id.tool_bar);
        setSupportActionBar(toolbar);

        //CREO LA BARRA DE NAVEGACION
        dl=findViewById(R.id.dl);
        abdt = new ActionBarDrawerToggle(this,dl,R.string.nav_open, R.string.nav_close);
        abdt.setDrawerIndicatorEnabled(true);
        dl.addDrawerListener(abdt);
        abdt.syncState();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //MENU
        nav_view = findViewById(R.id.nav_view);
        nav_view.bringToFront();

        nav_view.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id= item.getItemId();

                switch (id)
                {
                    case R.id.navSalir :
                        BorrarUsuario();
                        locationManager.removeUpdates(locListener);
                        task.cancel();
                        finish();
                        break;
                }
                return false;
            }
        });

        //REDONDEO LA FOTO Y LA PONGO
        View hView =  nav_view.getHeaderView(0);
        img= (ImageView)hView.findViewById(R.id.imgNavegador);
        if(_user.foto==null)
            _user.foto=BitmapFactory.decodeResource(getResources(), R.drawable.imageubi);
        RoundedBitmapDrawable roundDrawable = RoundedBitmapDrawableFactory.create(getResources(), _user.foto);
        roundDrawable.setCornerRadius(_user.foto.getHeight());
        img.setImageDrawable(roundDrawable);

        //OBTENGO LOS SUPERVISADOS

        AsyncCallWS miTarea= new AsyncCallWS(this);
        miTarea.execute(1);

        cargarMiUbicacion();

//        Handler handler = new Handler();
//        handler.postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                TraerUbicaciones();
//            }
//        },10000);

        task = new TimerTask() {
            @Override
            public void run() {
                handler.post(new Runnable() {
                    public void run() {
                        try {
                            //Ejecuta tu AsyncTask!
                            //AgregarMarcadores();
                            AsyncCallWS miTarea= new AsyncCallWS(MapsActivity.this);
                            miTarea.execute(3);

                        } catch (Exception e) {
                            Log.e("error", e.getMessage());
                        }
                    }
                });
            }
        };

        timer.schedule(task, 0, 15000);  //ejecutar en intervalo de 3 segundos.
    }

    private void BorrarUsuario()
    {
        SharedPreferences pref = getSharedPreferences(PREF_NAME,MODE_PRIVATE);
        SharedPreferences.Editor editor= pref.edit();

        editor.putString(KEY_USER,"");
        editor.putInt(KEY_PASS,-1);
        editor.apply();
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem opcMenu) {
        switch (opcMenu.getItemId())
        {
            case android.R.id.home:
                dl.openDrawer(GravityCompat.START);
                return true;
        }
        return super.onOptionsItemSelected(opcMenu);
    }

    @TargetApi(23)
    private void cargarMiUbicacion() {

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

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        if (    ActivityCompat.checkSelfPermission( this, android.Manifest.permission.ACCESS_FINE_LOCATION ) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission( this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        Location location= locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        if(location!=null) {
            _Long = location.getLongitude();
            _Lat = location.getLatitude();
            Grabar_mi_Ubicacion();
        }
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 20000, 5, locListener);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,20000,5,locListener);

    }


    LocationListener locListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            _Long=location.getLongitude();
            _Lat=location.getLatitude();
            Grabar_mi_Ubicacion();
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

    public void Grabar_mi_Ubicacion()
    {
        AsyncCallWS task = new AsyncCallWS(this);
        task.execute(2);
    }

        /*int id = opcMenu.getItemId();
        if (id == R.id.menu_actualiza) {
            AsyncCallWS miTarea= new AsyncCallWS(MapsActivity.this);
            miTarea.execute(1);
        }
        if (id == R.id.menu_elimina) {
            createDialog().show();
        }
        if (id == R.id.menu_buscar) {
            createDialog().show();
        }*/
       //return abdt.onOptionsItemSelected(opcMenu) || super.onOptionsItemSelected(opcMenu);



    //public AlertDialog createDialog(){
    //    AlertDialog.Builder builder=new AlertDialog.Builder(this);
   //     return null;
//        builder.setTitle("Borrar Ubicaciones")
//                .setMessage("¿Seguro desea borrar las Ubicaciones?")
//                .setPositiveButton("Confirmar",
//                        new DialogInterface.OnClickListener() {
//                            @Override
//                            public void onClick(DialogInterface dialog, int which) {
//                                AsyncCallWS miTarea= new AsyncCallWS(MapsActivity.this);
//                                miTarea.execute(2, _user.codigo);
//                            }
//                        })
//                .setNegativeButton("Cancelar",
//                        new DialogInterface.OnClickListener() {
//                            @Override
//                            public void onClick(DialogInterface dialog, int which) {
//
//                            }
//                        });
//        return builder.create();
   // }


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

        AsyncCallWS task = new AsyncCallWS(this);
        task.execute(1);

        //miUbicacion();

        // Add a marker in Sydney and move the camera
        //LatLng sydney = new LatLng(-34, 151);
        //mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
        //mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));

//        MapsActivity.AsyncCallWS task = new MapsActivity.AsyncCallWS();
//        task.execute();

    }

    public void onBackPressed()
    {
        locationManager.removeUpdates(locListener);
        task.cancel();
        finish();
    }


    private class AsyncCallWS extends AsyncTask<Integer, Void, Boolean> {

        Context ctx;
        int param=0;
        String r;

        public AsyncCallWS(Context ctx)
        {
            this.ctx=ctx;
        }

        @Override
        protected Boolean doInBackground(Integer... params) {
            param = params[0];
            //Log.i(TAG, "doInBackground");
            //Permisos();
            if(param==1) {
                CargarDatosSupervisados();
            }
            else if(param==2)
            {
                Enviar_mi_Ubicacion();
            }
            else if(param==3)
            {
                TraerUbicaciones();
            }
            return false;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            //dialogo.dismiss();
            if(param==1) {
                CargarUbicacionesEnMapa(true);
            }
            else if(param==3)
            {
                if(!_user.Supervisados.isEmpty())
                    CargarUbicacionesEnMapa(false);
            }
        }

        @Override
        protected void onPreExecute() {
//            dialogo = new ProgressDialog(MapsActivity.this);
//            dialogo.setMessage("Cargando datos...");
//            dialogo.setIndeterminate(false);
//            dialogo.setCancelable(false);
//            dialogo.show();
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            //Log.i(TAG, "onProgressUpdate");
        }

    }

    private void Enviar_mi_Ubicacion()
    {
        int a= 0;
        try {
            SoapObject request = new SoapObject(cConeccion.NAMESPACE, cConeccion.METHOD_NAME_GRABAR_UBICACION);

            request.addProperty("id", _user.id);
            request.addProperty("lat",_Lat);
            request.addProperty("lng",_Long);

            // Modelo el Sobre
            SoapSerializationEnvelope sobre = new SoapSerializationEnvelope(SoapEnvelope.VER11);

            sobre.implicitTypes = true;
            sobre.dotNet = true;
            sobre.encodingStyle = SoapSerializationEnvelope.XSD;

            sobre.setOutputSoapObject(request);

            MarshalDouble md = new MarshalDouble();
            md.register(sobre);

            // Modelo el transporte
            HttpTransportSE transporte = new HttpTransportSE(cConeccion.URL);

            // Llamada
            transporte.call(cConeccion.SOAP_ACTION_GRABAR_UBICACION, sobre);

            // Resultado
            String resultado = sobre.getResponse().toString();

            String p = resultado;

            //Log.i("Resultado", resultado.toString());


        } catch (Exception e) {
            //Log.e("ERROR", e.getMessage());
            String Mensaje = e.getMessage();
        }
    }

    private void TraerUbicaciones(){
        try {
            SoapObject request = new SoapObject(cConeccion.NAMESPACE, cConeccion.METHOD_NAME_GET_UBICACION);

            request.addProperty("id", _user.id);

            // Modelo el Sobre
            SoapSerializationEnvelope sobre = new SoapSerializationEnvelope(SoapEnvelope.VER11);

            sobre.implicitTypes = true;
            sobre.dotNet = true;
            sobre.encodingStyle = SoapSerializationEnvelope.XSD;

            sobre.setOutputSoapObject(request);

            // Modelo el transporte
            HttpTransportSE transporte = new HttpTransportSE(cConeccion.URL);

            // Llamada
            transporte.call(cConeccion.SOAP_ACTION_GET_UBICACION, sobre);

            // Resultado
            SoapObject resultado = (SoapObject) sobre.getResponse();

            CargarUbicacionesEnSupervisados(resultado);

            //Log.i("Resultado", resultado.toString());


        } catch (Exception e) {
            //Log.e("ERROR", e.getMessage());
            String mensaje = e.getMessage().toString();
        }
    }

    private void CargarUbicacionesEnSupervisados(SoapObject soapObject)
    {
        if(!_user.Supervisados.isEmpty()) {
            for (int j = 0; j < soapObject.getPropertyCount(); j++) {
                SoapObject responso = (SoapObject) soapObject.getProperty(j);
                for (Usuario u : _user.Supervisados) {
                    if (u.id == Integer.parseInt(responso.getProperty(0).toString())) {
                        u.Ubicaciones.Latitud = Double.parseDouble(responso.getProperty(1).toString());
                        u.Ubicaciones.Longitud = Double.parseDouble(responso.getProperty(2).toString());
                    }
                }
            }
        }
    }

    private void CargarDatosSupervisados()
    {
        SoapObject request = new SoapObject(cConeccion.NAMESPACE, cConeccion.METHOD_NAME_GET_SUPERVISADOS);
        request.addProperty("id",_user.id);

        SoapSerializationEnvelope sobre = new SoapSerializationEnvelope(SoapEnvelope.VER11);
        sobre.dotNet=true;
        sobre.setOutputSoapObject(request);

        HttpTransportSE transporte = new HttpTransportSE(cConeccion.URL);
        try
        {
            transporte.call(cConeccion.SOAP_ACTION_GET_SUPERVISADOS,sobre);
            SoapObject respuesta= (SoapObject)sobre.getResponse();

            //SUPERVISADOS

            for(int j=0; j < respuesta.getPropertyCount();j++)
            {
                SoapObject supervisado = (SoapObject) respuesta.getProperty(j);
                Usuario _u=new Usuario();
                _u.id = Integer.parseInt(supervisado.getProperty(1).toString());
                _u.foto = ConvertiABitmap(supervisado.getProperty(0).toString());
                _u.pass =  Integer.parseInt(supervisado.getProperty(3).toString());
                _u.usuario = supervisado.getProperty(7).toString();
                _u.nombre = supervisado.getProperty(2).toString();

                SoapObject supervisado_Ubicacion = (SoapObject) supervisado.getProperty(6);

                if(supervisado_Ubicacion!=null) {
                    if (supervisado_Ubicacion.getPropertyCount() > 0) {
                        Ubicacion _ub = new Ubicacion();

                        _ub.Latitud = Double.parseDouble(supervisado_Ubicacion.getProperty(1).toString());
                        _ub.Longitud = Double.parseDouble(supervisado_Ubicacion.getProperty(2).toString());

                        _u.Ubicaciones = _ub;
                    }
                }
                else
                    _u.Ubicaciones=null;
                _user.Supervisados.add(_u);
            }

            //Log.e("mira:", respuesta.toString());
        }
        catch (Exception ex)
        {
            //Log.e("ERROR", ex.getMessage());
        }
    }

    public void CargarUbicacionesEnMapa(Boolean animated)
    {
        for(Marker m:marcador)
            m.remove();

        for(Usuario _u: _user.Supervisados) {
            if(_u.Ubicaciones!=null)
                marcador.add(agregarMarcador(_u.Ubicaciones.Latitud, _u.Ubicaciones.Longitud, _u.nombre, _u.foto, animated));
        }
    }

    private Marker agregarMarcador(double lat, double lng, String nom, Bitmap foto, boolean animated) {
        LatLng coordenadas = new LatLng(lat, lng);
        CameraUpdate miUbicacion = CameraUpdateFactory.newLatLngZoom(coordenadas, 18);
        if(foto==null)
            foto=BitmapFactory.decodeResource(getResources(), R.drawable.imageubi);
        Drawable circleDrawable= resizeMapIcons(foto, 200,200);
        BitmapDescriptor markerIcon= getMarkerIconFromDrawable(circleDrawable,10);

        //BitmapDescriptor markerIcon = resizeMapIcons(Bitmap, 100, 100);
        //mMap.clear();
        Marker miMarcador = mMap.addMarker(new MarkerOptions()
                .position(coordenadas)
                .title(nom)
                .icon(markerIcon));
                //.icon(BitmapDescriptorFactory.fromBitmap(resizeMapIcons(foto,150,150))));
        if(animated)
            mMap.animateCamera(miUbicacion);
        return miMarcador;
    }

    private BitmapDescriptor getMarkerIconFromDrawable(Drawable drawable, int margen)
    {
        Canvas canvas= new Canvas();
        Bitmap bitmap= Bitmap.createBitmap(drawable.getIntrinsicWidth(),drawable.getIntrinsicHeight(),Bitmap.Config.ARGB_8888);
        canvas.setBitmap(bitmap);
        drawable.setBounds(margen,margen,drawable.getIntrinsicWidth()-margen, drawable.getIntrinsicHeight()-margen);
        Paint p = new Paint();
        p.setColor(Color.WHITE);
        p.setStyle(Paint.Style.FILL);
        canvas.drawCircle(drawable.getIntrinsicWidth()/2,drawable.getIntrinsicHeight()/2,drawable.getIntrinsicWidth()/2,p);
        drawable.draw(canvas);
        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }

    public Drawable resizeMapIcons(Bitmap bitmap, int width, int height)
    {
        //bitmap = ThumbnailUtils.extractThumbnail(bitmap,width,height);
        bitmap = Bitmap.createScaledBitmap(bitmap,width,height,false);
        RoundedBitmapDrawable roundDrawable = RoundedBitmapDrawableFactory.create(getResources(),bitmap);
        roundDrawable.setCornerRadius(height);
        return roundDrawable;
    }

    public Bitmap resizeMapIcons(int iconName, int width, int height) {
        BitmapDrawable b = (BitmapDrawable) getResources().getDrawable(iconName);
        Bitmap imageBitmap = b.getBitmap();
        Bitmap resizedBitmap = Bitmap.createScaledBitmap(imageBitmap, width, height, false);
        return resizedBitmap;
    }



    public Bitmap ConvertiABitmap(String compress)
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


    /*public Bitmap redonderFoto(Bitmap foto)
    {
        RoundedBitmapDrawable roundDrawable = RoundedBitmapDrawableFactory.create(getResources(),foto);
        roundDrawable.setCornerRadius(foto.getHeight());
        return roundDrawable.getBitmap();
    }*/

    /*private void actualizarUbicacion(Location location) {
        if (location != null) {
            lat = location.getLatitude();
            lng = location.getLongitude();
            //agregarMarcador(lat, lng, "hello");
        }
    }*/

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

    //@TargetApi(23)
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

    /*public Boolean EliminarDatos(int codigo) {
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
    }*/

  /* public void CargarDatos() {
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
    }*/

    /*public void CargarResultados(SoapObject resultado) {
        //_user.Ubicaciones.clear();

        int cant= resultado.getPropertyCount();
        if(cant>0) {
            //_user.Ubicaciones.clear();

            SoapObject miUbicacion = (SoapObject) resultado.getProperty(4);

            for (int j = 0; j < miUbicacion.getPropertyCount(); j++) {
                SoapObject u = (SoapObject) miUbicacion.getProperty(j);
                Ubicacion ubi = new Ubicacion();
                ubi.id = Integer.parseInt(u.getProperty(0).toString());
                ubi.Latitud = Double.parseDouble(u.getProperty(1).toString());
                ubi.Longitud = Double.parseDouble(u.getProperty(2).toString());
               // _user.Ubicaciones.add(ubi);
            }
        }
    }*/

    /*public void AgregarMarcadores() {
        BorrarMarcadores();

        *//*for (Ubicacion ubi : _user.Ubicaciones)
            marcador.add(agregarMarcador(ubi.Latitud, ubi.Longitud, ubi.Nombre));*//*
    }*/

    /*public void BorrarMarcadores(){
        if (!marcador.isEmpty()) {
            for(Marker mar:marcador)
                mar.remove();
            marcador.clear();
        }
    }*/

    /*public Marker BuscarMarcador(String nombre)
    {
        for(Marker mar:marcador)
        {
            if(mar.getTitle().toString()==nombre)
                return mar;
        }
        return null;
    }
*/
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

class MarshalDouble implements Marshal {
    public Object readInstance(XmlPullParser parser, String namespace, String name,
                               PropertyInfo expected) throws IOException, XmlPullParserException {

        return Double.parseDouble(parser.nextText());
    }


    public void register(SoapSerializationEnvelope cm)  {
        cm.addMapping(cm.xsd, "double", Double.class, this);
    }


    public void writeInstance(XmlSerializer writer, Object obj) throws IOException {
        writer.text(obj.toString());
    }
}
