package com.monzo.releases;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final TextView packageName = findViewById(R.id.packageName);
        packageName.setText(BuildConfig.APPLICATION_ID);

        final TextView versionName = findViewById(R.id.versionName);
        versionName.setText(BuildConfig.VERSION_NAME);

        final TextView versionCode = findViewById(R.id.versionCode);
        versionCode.setText(String.valueOf(BuildConfig.VERSION_CODE));
    }
}
