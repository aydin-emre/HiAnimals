/*
*       Copyright 2020. Huawei Technologies Co., Ltd. All rights reserved.
        Licensed under the Apache License, Version 2.0 (the "License");
        you may not use this file except in compliance with the License.
        You may obtain a copy of the License at
        http://www.apache.org/licenses/LICENSE-2.0
        Unless required by applicable law or agreed to in writing, software
        distributed under the License is distributed on an "AS IS" BASIS,
        WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
        See the License for the specific language governing permissions and
        limitations under the License.
*/
package com.team3.animalsintroduction;

import androidx.annotation.NonNull;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.team3.animalsintroduction.model.Photo;
import com.team3.animalsintroduction.model.User;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.Objects;

public class PhotoActivityLocal extends Activity implements CloudDBZoneWrapper.UiCallBack {

    ImageView ivShare, ivSave, imageView;
    private CloudDBZoneWrapper mCloudDBZoneWrapper;
    private MyHandler mHandler = new MyHandler();
    RelativeLayout relativeLayout;
    Bitmap bmp;


    private static final class MyHandler extends Handler {
        @Override
        public void handleMessage(@NonNull Message msg) {
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo_local);

        ivSave =findViewById(R.id.ivSave);
        ivShare = findViewById(R.id.ivShare);
        imageView = findViewById(R.id.imageView);
        relativeLayout = findViewById(R.id.loadingPanel);

        String path = getIntent().getStringExtra("filePath");

        //loads the file
        File file = new File(path);
        Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath());
        bmp = BitmapFactory.decodeFile(file.getAbsolutePath());
        imageView.setImageBitmap(bitmap);

        mCloudDBZoneWrapper = new CloudDBZoneWrapper();
        mHandler.post(() -> {
            mCloudDBZoneWrapper.addCallBacks(PhotoActivityLocal.this);
            mCloudDBZoneWrapper.createObjectType();
            mCloudDBZoneWrapper.openCloudDBZone();
        });


        ivSave.setOnClickListener(view -> {
            relativeLayout.setVisibility(View.VISIBLE);
            ivSave.setClickable(false);
            ivSave.setAlpha(0.5f);
            ivShare.setClickable(false);
            Bitmap bitmap1 = BitmapFactory.decodeFile(file.getAbsolutePath());

            String date = Long.toHexString(System.currentTimeMillis());
            // Create a file in the Pictures/HiAnimals album.
            final File out = new File(Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_PICTURES) + "/HiAnimals", "Img" +
                    date + ".jpeg");

            // Make sure the directory exists
            if (!Objects.requireNonNull(out.getParentFile()).exists()) {
                out.getParentFile().mkdirs();
            }

            // Write it to disk.
            FileOutputStream fos = null;
            try {
                fos = new FileOutputStream(out);
                bitmap1.compress(Bitmap.CompressFormat.PNG, 100, fos);
                fos.flush();
                fos.close();
            } catch (IOException e) {
                Toast.makeText(this, "Photo can not saved", Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }


            //Add photo to Huawei Cloud DB
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            bitmap1.compress(Bitmap.CompressFormat.PNG, 100, stream);
            byte[] byteArray = stream.toByteArray();
            bitmap1.recycle();
            SharedPreferences pref = this.getSharedPreferences("MyPref", MODE_PRIVATE);
            Photo p = new Photo(pref.getString("token", null), byteArray, date);
            mCloudDBZoneWrapper.insertPhoto(p);
            Toast.makeText(getApplicationContext(), "Photo saved successfully", Toast.LENGTH_SHORT).show();
            relativeLayout.setVisibility(View.GONE);
            ivShare.setClickable(true);
        });

        ivShare.setOnClickListener(view -> {
            Intent share = new Intent(Intent.ACTION_SEND);
            share.setType("image/jpeg");
            ContentValues values = new ContentValues();
            values.put(MediaStore.Images.Media.TITLE, "title");
            values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
            Uri uri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    values);
            OutputStream outstream;
            try {
                outstream = getContentResolver().openOutputStream(uri);
                bmp.compress(Bitmap.CompressFormat.JPEG, 100, outstream);
                outstream.close();
            } catch (Exception e) {
                System.err.println(e.toString());
            }

            share.putExtra(Intent.EXTRA_STREAM, uri);
            startActivity(Intent.createChooser(share, "Share Image"));
        });
    }

    @Override
    public void onAddOrQuery(List<User> userList) {

    }


    @Override
    public void isDataUpsert(Boolean state) {

    }

    @Override
    public void updateUiOnError(String errorMessage) {

    }

    @Override
    public void onAddOrQueryPhoto(List<Photo> photoList) {

    }
}
