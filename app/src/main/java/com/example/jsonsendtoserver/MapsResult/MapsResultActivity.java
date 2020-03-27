package com.example.jsonsendtoserver.MapsResult;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;


import android.Manifest;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import android.app.ProgressDialog;
import android.graphics.Color;

import com.example.jsonsendtoserver.Navigation.NavigateActivity;
import com.example.jsonsendtoserver.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

public class MapsResultActivity extends AppCompatActivity {

    private static final String TAG = MapsResultActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps_result);
        BottomNavigationView navView = findViewById(R.id.nav_view);

         Toolbar myToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(myToolbar);

        Button beginButoon = findViewById(R.id.beginButton);
        beginButoon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ArrayList<HashMap<String, String>> latLngPlot = (ArrayList<HashMap<String, String>>) getIntent().getSerializableExtra("result");
                Intent intent = new Intent(MapsResultActivity.this, NavigateActivity.class);
                intent.putExtra("result2", (Serializable) latLngPlot);
                startActivity(intent);

            }
        });

        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_maps, R.id.navigation_trip)
                .build();
      NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
//       NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(navView, navController);
    }
}

