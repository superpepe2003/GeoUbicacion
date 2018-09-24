package com.utopia.usuario.geoubicacion;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;

import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;

import java.util.ArrayList;
import java.util.List;

public class inicialActivity extends AppCompatActivity {

    private static final String PREF_NAME = "reg";
    public static final String KEY_USER = "User";
    public static final String KEY_PASS = "Id";
    private String _usuario_Guardado="";
    private int _id_Guardado=-1;

    private boolean _existe_usuario=false;
    private boolean _permisos = false;

    private Usuario _usuario = new Usuario();

    Button btnIngresar;
    Button btnRegistrar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inicial);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        btnIngresar = (Button) findViewById(R.id.btnLogin);
        btnRegistrar = (Button) findViewById(R.id.btnRegistrar);

        LocationManager location = (LocationManager)getSystemService(LOCATION_SERVICE);
        if(!location.isProviderEnabled(location.GPS_PROVIDER))
            alertNoGps();
        if(checkPermissions()) {
            //  permissions  granted.
            CargarEventosBotones();
        }

    }

    private void CargarEventosBotones()
    {
        btnIngresar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!CargarUsuarioGuardado()) {
                    Intent in = new Intent(inicialActivity.this, MainActivity.class);
                    startActivity(in);
                }
            }
        });

        btnRegistrar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent in = new Intent(inicialActivity.this, registroActivity.class);
                startActivity(in);
            }
        });
    }

    private boolean CargarUsuarioGuardado()
    {
        SharedPreferences pref = getSharedPreferences(PREF_NAME, MODE_PRIVATE);
        _usuario_Guardado= pref.getString(KEY_USER,"");
        _id_Guardado=pref.getInt(KEY_PASS,-1);


        if(_usuario_Guardado.length()>0 && _id_Guardado!=-1) {
            AsyncCallWCF async = new AsyncCallWCF(this);
            async.execute(_usuario_Guardado,  Integer.toString(_id_Guardado));
            return true;
        }

        return false;
    }


    private class AsyncCallWCF extends AsyncTask<String, Void, String> {

        private Context ctx;

        public AsyncCallWCF() {
        }

        public AsyncCallWCF(Context ctx) {
            this.ctx = ctx;
        }

        @Override
        protected String doInBackground(String... params) {
            String usuario = params[0];
            String pass = params[1];
            String result = CargarUser(usuario, Integer.parseInt(pass));
            return result;
        }

        @Override
        protected void onPostExecute(String result) {
            if (result.length() > 0) {
                Toast.makeText(ctx, result.toString(), Toast.LENGTH_LONG).show();
                BorrarUsuario();
            } else {
                Intent intent = new Intent(ctx, MapsActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                intent.putExtra("User", _usuario);
                ctx.startActivity(intent);
                //this.cancel(true);
            }
        }
    }

    private void BorrarUsuario()
    {
        SharedPreferences pref = getSharedPreferences(PREF_NAME,MODE_PRIVATE);
        SharedPreferences.Editor editor= pref.edit();

        editor.putString(KEY_USER,"");
        editor.putInt(KEY_PASS,-1);
        editor.apply();
    }

    public String CargarUser(String user, int pass)
    {
        // Modelo el request
        SoapObject request = new SoapObject(cConeccion.NAMESPACE, cConeccion.METHOD_NAME_EXISTE);
        request.addProperty("usuario", user);
        request.addProperty("pass", pass);
        //request.addProperty("Param", "valor"); // Paso parametros al WS

        // Modelo el Sobre
        SoapSerializationEnvelope sobre = new SoapSerializationEnvelope(SoapEnvelope.VER10);
        sobre.dotNet = true;
        sobre.setOutputSoapObject(request);

        // Modelo el transporte
        HttpTransportSE transporte = new HttpTransportSE(cConeccion.URL, 60000);

        // Llamada
        try {
            transporte.call(cConeccion.SOAP_ACTION_EXISTE, sobre);
            SoapObject resultado = (SoapObject) sobre.getResponse();


            int cant= resultado.getPropertyCount();
            if(cant>0) {
                //_usuario.Ubicaciones.clear();

                //SoapObject datos=(SoapObject) resultado.getProperty(0);

                int id= Integer.parseInt(resultado.getProperty(1).toString());

                String stringFoto= resultado.getProperty(0).toString();
                _usuario.foto= cConeccion.ConvertiABitmap(stringFoto);
                _usuario.id = Integer.parseInt(resultado.getProperty(1).toString());
                _usuario.nombre = resultado.getProperty(2).toString();
                _usuario.pass = Integer.parseInt(resultado.getProperty(3).toString());
                _usuario.usuario = resultado.getProperty(7).toString();

                SoapObject _ubi = (SoapObject) resultado.getProperty(6);
                if(_ubi.getPropertyCount()>0) {
                    Ubicacion _ub = new Ubicacion();

                    _ub.Latitud = Double.parseDouble(_ubi.getProperty(1).toString());
                    _ub.Longitud = Double.parseDouble(_ubi.getProperty(2).toString());
                    _usuario.Ubicaciones = _ub;
                }
                return "";
            }
            else
            {
                return "Usuario no existe";
            }
        }
        catch (Exception e) {
            //Log.e("ERROR", e.getMessage());
            return "Problemas para acceder a la BD";
        }
        // Resultado
    }


    public static final int MULTIPLE_PERMISSIONS = 10; // code you want.

    String[] permissions= new String[]{
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.ACCESS_NETWORK_STATE,
            Manifest.permission.INTERNET,
            Manifest.permission.CAMERA,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION};




    private  boolean checkPermissions() {
        int result;
        List<String> listPermissionsNeeded = new ArrayList<>();
        for (String p:permissions) {
            result = ContextCompat.checkSelfPermission(inicialActivity.this,p);
            if (result != PackageManager.PERMISSION_GRANTED) {
                listPermissionsNeeded.add(p);
            }
        }
        if (!listPermissionsNeeded.isEmpty()) {
            ActivityCompat.requestPermissions(this, listPermissionsNeeded.toArray(new String[listPermissionsNeeded.size()]),MULTIPLE_PERMISSIONS );
            return false;
        }
        return true;
    }

    private void alertNoGps() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("El sistema GPS esta desactivado, ¿Desea Activarlo?")
                .setCancelable(false)
                .setPositiveButton("Si", new DialogInterface.OnClickListener(){
                    public void onClick(@SuppressWarnings("unused") final DialogInterface dialog,
                                        @SuppressWarnings("unused") final int i)
                    {
                        startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(@SuppressWarnings("unused") final DialogInterface dialog,
                                        @SuppressWarnings("unused") final int i) {
                        dialog.cancel();
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MULTIPLE_PERMISSIONS:{
                if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    CargarEventosBotones();

                    // permissions granted.
                } else {
                   Toast.makeText(this, "Ir a configuracion y habilitar permisos, para funcionamiento", Toast.LENGTH_LONG)
                            .show();
                   _permisos=false;
                }
                // permissions list of don't granted permission
            }
            return;
        }
    }


}
