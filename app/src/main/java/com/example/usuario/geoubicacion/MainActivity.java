package com.example.usuario.geoubicacion;

import android.Manifest;
import android.annotation.TargetApi;
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
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    final String NAMESPACE = "http://tempuri.org/";
    final String URL="http://geolocaliza.ddns.net/WcfGeoLocation/WSGeoUbicacion.svc";
    final String METHOD_NAME = "Existe";
    final String SOAP_ACTION = "http://tempuri.org/IWSGeolizacion/Existe";


    EditText txtUser;
    EditText txtPass;
    Button btnLogin;
    TextView lblInicio;
    TextView btnUser;

    private final String TAG="Utopia Soft";


    @TargetApi(23)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        txtUser = findViewById(R.id.txtUser);
        txtPass = findViewById(R.id.txtPass);
        btnLogin = findViewById(R.id.btnLog);
        lblInicio = findViewById(R.id.lblIngreso);
        btnUser = findViewById(R.id.txtIngresoUser);

        CargarEventos();

    }


    public void CargarEventos() {
        txtUser.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                lblInicio.setText("");
            }
        });

        txtPass.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                lblInicio.setText("");
            }
        });

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                Intent intent = new Intent(MainActivity.this, MapsActivity.class);
//                startActivity(intent);
                String[] luser= {txtUser.getText()+"" , txtPass.getText()+""};

                AsyncCallWCF async = new AsyncCallWCF();
                async.execute(luser);
            }
        });

        btnUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, userActivity.class);
                startActivity(intent);
            }
        });

    }

/**
*
*/

    private class AsyncCallWCF extends AsyncTask<String, Void, Void> {
        @Override
        protected Void doInBackground(String... params) {
            String usuario = params[0];
            String pass=params[1];
            CargarUser(usuario,Integer.parseInt(pass));
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {

        }

        @Override
        protected void onPreExecute() {

        }

        @Override
        protected void onProgressUpdate(Void... values) {
            Log.i(TAG, "onProgressUpdate");
        }

    }

        public void CargarUser(String user, int pass)
        {
            // Modelo el request
            SoapObject request = new SoapObject(NAMESPACE, METHOD_NAME);
            request.addProperty("usuario", user);
            request.addProperty("pass", pass);
            //request.addProperty("Param", "valor"); // Paso parametros al WS

            // Modelo el Sobre
            SoapSerializationEnvelope sobre = new SoapSerializationEnvelope(SoapEnvelope.VER10);
            sobre.dotNet = true;
            sobre.setOutputSoapObject(request);

            // Modelo el transporte
            HttpTransportSE transporte = new HttpTransportSE(URL);

            // Llamada
            try {
                transporte.call(SOAP_ACTION, sobre);
                SoapObject resultado = (SoapObject) sobre.getResponse();
                Log.e("Volvieron", "ahora si");
            }
            catch (Exception e) {
                Log.e("ERROR", e.getMessage());
            }
            // Resultado
        }
}
