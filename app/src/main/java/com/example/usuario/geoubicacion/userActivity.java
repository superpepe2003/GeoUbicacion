package com.example.usuario.geoubicacion;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Build;
import android.provider.Settings;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;
import org.w3c.dom.Text;

public class userActivity extends AppCompatActivity {

    final String NAMESPACE = "http://tempuri.org/";
    final String URL="http://geolocaliza.ddns.net/WcfGeoLocation/WSGeoUbicacion.svc";
    final String METHOD_NAME = "ExisteCodigo";
    final String SOAP_ACTION = "http://tempuri.org/IWSGeolizacion/ExisteCodigo";


    public static final int LOCATION_REQUEST_CODE = 1001;
    private static final int MY_PERMISSION_ACCESS_COARSE_LOCATION = 11;
    Button btnLog;
    TextView txtUser;
    TextView txtCodigo;

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user);

        btnLog = (Button)findViewById(R.id.btnLog);
        txtUser=(TextView)findViewById(R.id.txtUser);
        txtCodigo=(TextView)findViewById(R.id.txtCodigo);

        LocationManager location = (LocationManager)getSystemService(LOCATION_SERVICE);
        if(!location.isProviderEnabled(location.GPS_PROVIDER))
            alertNoGps();

        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            //, Manifest.permission.ACCESS_COARSE_LOCATION

            ActivityCompat.requestPermissions(userActivity.this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.ACCESS_COARSE_LOCATION}, LOCATION_REQUEST_CODE);
        }
        else {
            CargarEventos();
        }
    }

    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults)
    {
        switch(requestCode)
        {
            case LOCATION_REQUEST_CODE:
                if(grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                {
                    CargarEventos();
                }
                else {
                    Toast.makeText(this,"Tienes que dar permiso para que la app funcione !", Toast.LENGTH_LONG).show();
                }
                return;
        }
    }

    public void CargarEventos()
    {
        btnLog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(TieneGps()) {

                    if(txtCodigo.getText().toString().length()>0 && txtUser.getText().toString().length()>0) {
                        AsycnCodigo asycnCodigo = new AsycnCodigo(userActivity.this);
                        asycnCodigo.execute(txtUser.getText().toString(), txtCodigo.getText().toString());
                    }
                    else
                        Toast.makeText(userActivity.this, "El Nombre y el codigo no pueden estar vacio",Toast.LENGTH_LONG).show();

                    /*if(Existe) {
                        Intent in = new Intent(userActivity.this, cargarTodosActivity.class);
                        in.putExtra("user", txtUser.getText() + "");
                        in.putExtra("codigo", Integer.parseInt(txtCodigo.getText().toString()));
                        startActivity(in);
                    }
                    else
                    {
                        Toast.makeText(userActivity.this, "El Codigo no pertenece a ningun user",Toast.LENGTH_LONG).show();
                    }*/
                }
                else
                {
                    alertNoGps();
                }
            }
        });
    }

    private class AsycnCodigo extends AsyncTask<String, Void, Boolean>
    {
        Context ctx;
        String _user;
        int _codigo;

        AsycnCodigo(Context ctx)
        {
            this.ctx = ctx;
        }

        @Override
        protected Boolean doInBackground(String... params) {
            _codigo = Integer.parseInt(params[1]);
            _user= params[0];

            boolean b = BuscarUser(_codigo);
            return b;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            if(result) {
                Intent intent = new Intent(ctx,cargarTodosActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                intent.putExtra("user", _user);
                intent.putExtra("codigo", _codigo);
                ctx.startActivity(intent);
            }
            else
            {
                Toast.makeText(userActivity.this, "El Codigo no pertenece a ningun user", Toast.LENGTH_LONG).show();
            }
        }

        @Override
        protected void onPreExecute() {

        }

        @Override
        protected void onProgressUpdate(Void... values) {
            //Log.i(TAG, "onProgressUpdate");
        }

    }

    private Boolean BuscarUser(int _codigo)
    {
        try {
            //cargarTodosActivity.this.txtCarga.setText("Conectando");
            // Modelo el request
            SoapObject request = new SoapObject(NAMESPACE, METHOD_NAME);

            request.addProperty("codigo",_codigo);

            // Modelo el Sobre
            SoapSerializationEnvelope sobre = new SoapSerializationEnvelope(SoapEnvelope.VER11);

            sobre.implicitTypes = true;
            sobre.dotNet = true;
            sobre.encodingStyle = SoapSerializationEnvelope.XSD;

            sobre.setOutputSoapObject(request);

            // Modelo el transporte
            HttpTransportSE transporte = new HttpTransportSE(URL);

            // Llamada
            transporte.call(SOAP_ACTION, sobre);

            // Resultado
            //String resultado = sobre.getResponse().toString();
            Boolean resultado = Boolean.parseBoolean(sobre.getResponse().toString());

            Log.i("Resultado", resultado.toString());

            return resultado;

        } catch (Exception e) {
            Log.e("ERROR", e.getMessage());
            return false;
        }
    }

    private Boolean TieneGps()
    {
        LocationManager location = (LocationManager)getSystemService(LOCATION_SERVICE);
        if(!location.isProviderEnabled(location.GPS_PROVIDER))
            return false;
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
}
