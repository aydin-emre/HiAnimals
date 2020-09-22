package com.team3.animalsintroduction;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.HorizontalScrollView;
import android.widget.Toast;

import com.team3.animalsintroduction.R;
import com.team3.animalsintroduction.common.PermissionManager;

public class AnimalListActivity extends AppCompatActivity {

    HorizontalScrollView horizontalScrollView;
    CardView cv1, cv2, cv3,cv4;
    Button btnShowDeer;
    Button btnShowDog;
    Button btnShowTiger,btnShowDuck;
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
        btnShowDeer = findViewById(R.id.btnShowDeer);
        btnShowDog = findViewById(R.id.btnShowDog);
        btnShowTiger = findViewById(R.id.btnShowTiger);
        btnShowDuck = findViewById(R.id.btnShowDuck);

        btnShowDeer.setOnClickListener(view -> {
            Intent intent = new Intent(this, AnimalActivity.class);
            intent.putExtra("Type", 1);
            startActivity(intent);
        });

        btnShowDog.setOnClickListener(view -> {
            Intent intent = new Intent(this, AnimalActivity.class);
            intent.putExtra("Type", 2);
            startActivity(intent);
        });

        btnShowTiger.setOnClickListener(view -> {
            Intent intent = new Intent(this, AnimalActivity.class);
            intent.putExtra("Type", 3);
            startActivity(intent);
        });
        btnShowDuck.setOnClickListener(view -> {
            Intent intent = new Intent(this, AnimalActivity.class);
            intent.putExtra("Type", 4);
            startActivity(intent);
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
