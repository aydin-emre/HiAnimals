package com.huawei.animalsintroduction;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.HorizontalScrollView;
import android.widget.Toast;

import com.huawei.animalsintroduction.common.PermissionManager;

public class AnimalListActivity extends AppCompatActivity {

    HorizontalScrollView horizontalScrollView;
    CardView cv1;
    CardView cv2;
    CardView cv3;
    CardView cv4;
    CardView cv5;
    Button btnShowAnimal;
    private static final String TAG = AnimalListActivity.class.getSimpleName();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_animal_list);

        horizontalScrollView = findViewById(R.id.horizontalScrollView);
        cv1 = findViewById(R.id.cv1);
        cv2 = findViewById(R.id.cv2);
        cv3 = findViewById(R.id.cv3);
        cv4 = findViewById(R.id.cv4);
        cv5 = findViewById(R.id.cv5);

        btnShowAnimal = findViewById(R.id.btnShowAnimal);
        btnShowAnimal.setOnClickListener(view -> {
            startActivity(new Intent(AnimalListActivity.this, AnimalActivity.class));
        });
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] results) {
        if (!PermissionManager.hasPermission(this)) {
            Toast.makeText(this, "This application needs camera permission.", Toast.LENGTH_LONG).show();
            finish();
        }
    }


    @Override
    protected void onResume() {
        Log.d(TAG, "onResume");
        super.onResume();

        // AR Engine requires the camera permission.
        PermissionManager.onResume(this);
    }
}
