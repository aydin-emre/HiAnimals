package com.huawei.animalsintroduction;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.huawei.animalsintroduction.login.LoginActivity;

public class MainActivity extends AppCompatActivity {

    Button btnArPage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Button StartButton = findViewById(R.id.xxxx);
        btnArPage = findViewById(R.id.btnArPage);

        StartButton.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, LoginActivity.class)));
        btnArPage.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, AnimalListActivity.class)));
    }
}
