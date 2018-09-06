package com.example.usuario.geoubicacion;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.support.v7.widget.Toolbar;

public class inicialActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inicial);

        Toolbar toolbar = (Toolbar) findViewById(R.id.tool_bar);
        setSupportActionBar(toolbar);
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
        return true;
    }

}
