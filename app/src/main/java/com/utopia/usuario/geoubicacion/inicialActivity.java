package com.utopia.usuario.geoubicacion;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.support.v7.widget.Toolbar;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.List;

public class inicialActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inicial);

        Toolbar toolbar = (Toolbar) findViewById(R.id.tool_bar);
        setSupportActionBar(toolbar);

        //Ejemplo
//        String[] l= {"Pablo", "Silvia", "Maria", "Pablito", "Agustin"};
//        ArrayAdapter<String> adapter= new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item,l);
//
//        Spinner sp= (Spinner) findViewById(R.id.cboPrimero);
//
//        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
//        sp.setAdapter(adapter);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menuprincipal, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem opcMenu) {
        int id = opcMenu.getItemId();
        if (id == R.id.menu_admin) {
            Intent in = new Intent(this, MainActivity.class);
            startActivity(in);
        }
        if (id == R.id.menu_user) {
            Intent in = new Intent(this, userActivity.class);
            startActivity(in);
        }

        if(id==R.id.menu_setting){
            try {
                DialogFragment d = new dialogFragmentMap(new LatLng(-34,-55), "Pablo");
                d.show(getSupportFragmentManager(), null);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return true;
    }

}
