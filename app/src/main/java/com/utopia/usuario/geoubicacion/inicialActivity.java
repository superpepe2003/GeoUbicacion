package com.utopia.usuario.geoubicacion;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.ContextCompat;
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

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.List;

public class inicialActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inicial);
        if (checkPermissions()){
            //  permissions  granted.

        }

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Button btnIngresar= (Button) findViewById(R.id.btnLogin);
        Button btnRegistrar= (Button) findViewById(R.id.btnRegistrar);

        btnIngresar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent in = new Intent(inicialActivity.this, MainActivity.class);
                startActivity(in);
            }
        });

        btnRegistrar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent in= new Intent(inicialActivity.this, registroActivity.class);
                startActivity(in);
            }
        });

        //Ejemplo
//        String[] l= {"Pablo", "Silvia", "Maria", "Pablito", "Agustin"};
//        ArrayAdapter<String> adapter= new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item,l);
//
//        Spinner sp= (Spinner) findViewById(R.id.cboPrimero);
//
//        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
//        sp.setAdapter(adapter);

    }

//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        getMenuInflater().inflate(R.menu.menuprincipal, menu);
//        return true;
//    }
//
//    @Override
//    public boolean onOptionsItemSelected(MenuItem opcMenu) {
//        int id = opcMenu.getItemId();
//        if (id == R.id.menu_admin) {
//            Intent in = new Intent(this, MainActivity.class);
//            startActivity(in);
//        }
//        if (id == R.id.menu_user) {
//            Intent in = new Intent(this, userActivity.class);
//            startActivity(in);
//        }
//
//        if(id==R.id.menu_setting){
//            try {
//                DialogFragment d = new dialogFragmentMap(new LatLng(-34,-55), "Pablo");
//                d.show(getSupportFragmentManager(), null);
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//        }
//        return true;
//    }

    public static final int MULTIPLE_PERMISSIONS = 10; // code you want.

    String[] permissions= new String[]{
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
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


    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MULTIPLE_PERMISSIONS:{
                if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    // permissions granted.
                } else {
//                    Toast.makeText(this, "Go to settings and enable permissions", Toast.LENGTH_LONG)
//                            .show();
                }
                // permissions list of don't granted permission
            }
            return;
        }
    }


}
