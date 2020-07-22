package com.huawei.animalsintroduction;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.huawei.agconnect.auth.AGConnectAuth;
import com.huawei.animalsintroduction.login.LoginActivity;
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
        btnArPage = findViewById(R.id.btnArPage);
        imageViewLogin = findViewById(R.id.ivLogin);

        StartButton.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, LoginActivity.class)));
        btnArPage.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, AnimalListActivity.class)));
        imageViewLogin.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, AnimalListActivity.class)));

        mainTitle = findViewById(R.id.main_title);
        mainTitle.setText((auth.getCurrentUser() != null) ? "Welcome, " + auth.getCurrentUser().getDisplayName() : "Welcome");

        findViewById(R.id.logout).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AGConnectAuth.getInstance().signOut();
                startActivity(new Intent(MainActivity.this, LoginActivity.class));
                finish();
            }
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
