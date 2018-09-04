package com.example.usuario.geoubicacion;

import android.Manifest;
import android.annotation.TargetApi;
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

    private final String TAG="Utopia Soft";
    private Usuario _usuario= new Usuario();


    @TargetApi(23)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        txtUser = findViewById(R.id.txtUser);
        txtPass = findViewById(R.id.txtPass);
        btnLogin = findViewById(R.id.btnLog);
        lblInicio = findViewById(R.id.lblIngreso);

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
                /*Intent intent = new Intent(MainActivity.this, MapsActivity.class);
                startActivity(intent);*/
                String[] luser= {txtUser.getText()+"" , txtPass.getText()+""};

                AsyncCallWCF async = new AsyncCallWCF(MainActivity.this);
                async.execute(txtUser.getText() + "", txtPass.getText() + "");
            }
        });

    }

/**
*
*/

    private class AsyncCallWCF extends AsyncTask<String, Void, String> {

        Context ctx;

        public AsyncCallWCF(Context ctx) {
            this.ctx = ctx;
        }

        @Override
        protected String doInBackground(String... params) {
            String usuario = params[0];
            String pass= params[1];
            String result = CargarUser(usuario,Integer.parseInt(pass));
            return result;
        }

        @Override
        protected void onPostExecute(String result) {
            if (result.length()>0)
                Toast.makeText(MainActivity.this, result.toString(),Toast.LENGTH_LONG).show();
            else {
                Intent intent = new Intent(ctx,MapsActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                intent.putExtra("User", _usuario);
                ctx.startActivity(intent);
            }
        }

        @Override
        protected void onPreExecute() {

        }

        @Override
        protected void onProgressUpdate(Void... values) {
            Log.i(TAG, "onProgressUpdate");
        }


}

        public String CargarUser(String user, int pass)
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


                int cant= resultado.getPropertyCount();
                if(cant>0) {
                    _usuario.Ubicaciones.clear();

                    _usuario.id = Integer.parseInt(resultado.getProperty(1).toString());
                    _usuario.codigo = Integer.parseInt(resultado.getProperty(0).toString());
                    _usuario.nombre = resultado.getProperty(2).toString();
                    _usuario.pass = resultado.getProperty(3).toString();
                    _usuario.usuario = resultado.getProperty(5).toString();

                    SoapObject miUbicacion = (SoapObject) resultado.getProperty(4);

                    for (int j = 0; j < miUbicacion.getPropertyCount(); j++) {
                        SoapObject u = (SoapObject) miUbicacion.getProperty(j);
                        Ubicacion ubi = new Ubicacion();
                        ubi.id = Integer.parseInt(u.getProperty(0).toString());
                        ubi.Latitud = Double.parseDouble(u.getProperty(1).toString());
                        ubi.Longitud = Double.parseDouble(u.getProperty(2).toString());
                        ubi.Nombre = u.getProperty(3).toString();
                        ubi.UsuarioId = _usuario.id;
                        _usuario.Ubicaciones.add(ubi);
                    }

                    Log.e("Volvieron", "Usuario Existe!");

                    return "";
                }
                else
                {
                    Log.e("Volvieron", "Usuario no Existe!");
                    return "Usuario no existe";
                }
            }
            catch (Exception e) {
                Log.e("ERROR", e.getMessage());
                return "Problemas para acceder a la BD";
            }
            // Resultado
        }
}
