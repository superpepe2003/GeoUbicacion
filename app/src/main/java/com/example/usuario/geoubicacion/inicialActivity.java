package com.example.usuario.geoubicacion;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;

public class inicialActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inicial);
    }

    @Override
    public boolean onCreateOpptionsMenu(Menu menu)   {
        getMenuInflater().inflate(R.menu.menuprincipal, menu);
        return true;
    }

}
