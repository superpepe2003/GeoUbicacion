package com.example.usuario.geoubicacion;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Build;
import android.provider.Settings;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

public class userActivity extends AppCompatActivity {


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

                    Intent in = new Intent(userActivity.this, cargarTodosActivity.class);
                    in.putExtra("user", txtUser.getText() + "");
                    in.putExtra("codigo", Integer.parseInt(txtCodigo.getText().toString()));
                    startActivity(in);
                }
                else
                {
                    alertNoGps();
                }
            }
        });
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
