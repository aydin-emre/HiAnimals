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

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.huawei.agconnect.auth.AGConnectAuth;
import com.team3.animalsintroduction.login.LoginActivity;
import com.huawei.hms.ads.AdParam;
import com.huawei.hms.ads.BannerAdSize;
import com.huawei.hms.ads.HwAds;
import com.huawei.hms.ads.banner.BannerView;

public class MainActivity extends AppCompatActivity {

    ImageView imageViewLogin;
    Button btnArPage, StartButton;
    TextView mainTitle;
    AGConnectAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getAdsKit();
        auth = AGConnectAuth.getInstance();

        StartButton = findViewById(R.id.xxxx);
        //btnArPage = findViewById(R.id.btnArPage);
        imageViewLogin = findViewById(R.id.ivLogin);



        StartButton.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, LoginActivity.class)));
       // btnArPage.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, AnimalListActivity.class)));
        imageViewLogin.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, AnimalListActivity.class)));

        mainTitle = findViewById(R.id.main_title);
        mainTitle.setText((auth.getCurrentUser() != null) ? "Welcome, " + auth.getCurrentUser().getDisplayName() : "Welcome");

        findViewById(R.id.logout).setOnClickListener(v -> {
            AGConnectAuth.getInstance().signOut();
            SharedPreferences sharedPreferences = getApplicationContext().getSharedPreferences("MyPref", MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.clear();
            editor.commit();
            startActivity(new Intent(MainActivity.this, LoginActivity.class));
            finish();
        });
    }


    protected void onStart() {
        super.onStart();
        Bundle bundle = getIntent().getExtras();
        int value = -1; // or other values
        if(bundle != null)
            value = bundle.getInt("key");
        if (AGConnectAuth.getInstance().getCurrentUser() == null && value == -1) {
            startActivity(new Intent(MainActivity.this, LoginActivity.class));
            finish();
        }
    }

    private void getAdsKit() {
        // Initialize the HUAWEI Ads SDK.
        HwAds.init(this);

        // Obtain BannerView based on the configuration in layout/ad_fragment.xml.
        BannerView bottomBannerView = findViewById(R.id.hw_banner_view);
        AdParam adParam = new AdParam.Builder().build();
        bottomBannerView.loadAd(adParam);

        // Call new BannerView(Context context) to create a BannerView class.
        BannerView topBannerView = new BannerView(this);
        topBannerView.setAdId(Constant.AD_ID);
        topBannerView.setBannerAdSize(BannerAdSize.BANNER_SIZE_360_57);
        topBannerView.loadAd(adParam);

        ConstraintLayout rootView = findViewById(R.id.mainActivity);
        rootView.addView(topBannerView);
    }
}
