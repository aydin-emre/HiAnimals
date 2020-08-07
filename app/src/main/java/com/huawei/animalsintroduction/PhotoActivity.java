package com.huawei.animalsintroduction;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;

public class PhotoActivity extends AppCompatActivity {

    ImageView imageView;
    TextView textView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo);

        imageView = findViewById(R.id.imageView);

        //gets the file path
        String filePath=getIntent().getStringExtra("path");

            //loads the file
            File file = new File(filePath);
            Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath());
            imageView.setImageBitmap(bitmap);


    }
}
