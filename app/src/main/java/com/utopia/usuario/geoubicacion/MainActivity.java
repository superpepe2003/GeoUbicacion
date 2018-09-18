package com.utopia.usuario.geoubicacion;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Build;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Base64;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapPrimitive;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final String PREF_NAME = "reg";
    public static final String KEY_USER = "User";
    public static final String KEY_PASS = "Id";

    EditText txtUser;
    EditText txtPass;
    Button btnLogin;
    TextView lblInicio;
    ImageView miImagen;
    CheckBox chkSesion;

    private final String TAG="Utopia Soft";
    private Usuario _usuario= new Usuario();

    private String _usuario_Guardado="";
    private int _id_Guardado=-1;



    @TargetApi(23)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        txtUser = findViewById(R.id.txtUser);
        txtPass = findViewById(R.id.txtPass);
        btnLogin = findViewById(R.id.btnLog);
        lblInicio = findViewById(R.id.lblIngreso);
        miImagen= findViewById(R.id.miImagen);
        chkSesion=findViewById(R.id.chkSession);

        CargarEventos();

       // if(_usuario_Guardado.length()>0 && _id_Guardado!=-1) {
       //     AsyncCallWCF async = new AsyncCallWCF(MainActivity.this, true);
       //     async.execute(_usuario_Guardado,  Integer.toString(_id_Guardado));
       // }

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

                btnLogin.setEnabled(false);
                AsyncCallWCF async = new AsyncCallWCF(MainActivity.this, false);
                async.execute(txtUser.getText() + "", txtPass.getText() + "");
            }
        });


    }

/**
*
*/

    private class AsyncCallWCF extends AsyncTask<String, Void, String> {

        private Context ctx;
        private boolean session;

        public AsyncCallWCF(){}

        public AsyncCallWCF(Context ctx, boolean session) {
            this.ctx = ctx;
            this.session=session;
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
            btnLogin.setEnabled(true);
            if (result.length()>0) {
                    Toast.makeText(MainActivity.this, result.toString(), Toast.LENGTH_LONG).show();
            }
            else {
               if(chkSesion.isChecked())
                    GuardarSession();
                Intent intent = new Intent(ctx,MapsActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                intent.putExtra("User", _usuario);
                ctx.startActivity(intent);
                finish();
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
    private void BorrarSession()
    {
        SharedPreferences pref = getSharedPreferences(PREF_NAME,MODE_PRIVATE);
        SharedPreferences.Editor editor= pref.edit();

        editor.putString(KEY_USER,"");
        editor.putInt(KEY_PASS,-1);
        editor.apply();
    }

    public void GuardarSession()
    {
        SharedPreferences pref = getSharedPreferences(PREF_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.putString(KEY_USER, _usuario.usuario);
        editor.putInt(KEY_PASS,_usuario.pass);
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

//                    _usuario.usuario = resultado.getProperty(5).toString();
//
//                    SoapObject miUbicacion = (SoapObject) resultado.getProperty(4);
//
//                    for (int j = 0; j < miUbicacion.getPropertyCount(); j++) {
//                        SoapObject u = (SoapObject) miUbicacion.getProperty(j);
//                        Ubicacion ubi = new Ubicacion();
//                        ubi.id = Integer.parseInt(u.getProperty(0).toString());
//                        ubi.Latitud = Double.parseDouble(u.getProperty(1).toString());
//                        ubi.Longitud = Double.parseDouble(u.getProperty(2).toString());
//                        ubi.Nombre = u.getProperty(3).toString();
//                        ubi.UsuarioId = _usuario.id;
//                        _usuario.Ubicaciones.add(ubi);
 //                   }

                   // Log.e("Volvieron", "Usuario Existe!");

                    return "";
                }
                else
                {
                    //Log.e("Volvieron", "Usuario no Existe!");
                    return "Usuario no existe";
                }
            }
            catch (Exception e) {
                //Log.e("ERROR", e.getMessage());
                return "Problemas para acceder a la BD";
            }
            // Resultado
        }


}
