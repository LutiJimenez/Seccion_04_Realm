package com.luti.seccion_04_realm.activities;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intentBoard =  new Intent(this, BoardActivity.class);
        startActivity(intentBoard);
        finish();
    }
}
