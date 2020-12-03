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
package com.team3.animalsintroduction.login;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.StrictMode;
import android.util.Log;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.huawei.agconnect.auth.AGConnectAuth;
import com.huawei.agconnect.auth.AGConnectAuthCredential;
import com.huawei.agconnect.auth.HwIdAuthProvider;
import com.huawei.agconnect.auth.SignInResult;
import com.team3.animalsintroduction.AnimalListActivity;
import com.team3.animalsintroduction.CloudDBZoneWrapper;
import com.team3.animalsintroduction.Constant;
import com.team3.animalsintroduction.R;
import com.team3.animalsintroduction.model.Photo;
import com.team3.animalsintroduction.model.User;
import com.huawei.hmf.tasks.OnSuccessListener;
import com.huawei.hmf.tasks.Task;
import com.huawei.hms.common.ApiException;
import com.huawei.hms.support.hwid.HuaweiIdAuthManager;
import com.huawei.hms.support.hwid.request.HuaweiIdAuthParams;
import com.huawei.hms.support.hwid.request.HuaweiIdAuthParamsHelper;
import com.huawei.hms.support.hwid.result.AuthHuaweiId;
import com.huawei.hms.support.hwid.service.HuaweiIdAuthService;

import java.util.List;

public class LoginActivity extends AppCompatActivity implements CloudDBZoneWrapper.UiCallBack {

    private static final String TAG = "Account Service";
//    EditText editName, editPassword;
//    TextView lblEmailAnswer, lblPasswordAnswer;
//    ViewModelProvider.Factory viewModelFactory;

    private CloudDBZoneWrapper mCloudDBZoneWrapper;
    private MyHandler mHandler = new MyHandler();
    RelativeLayout loadingPanel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_lay);

        CloudDBZoneWrapper.initAGConnectCloudDB(getApplicationContext());

        mCloudDBZoneWrapper = new CloudDBZoneWrapper();

        mHandler.post(() -> {
            mCloudDBZoneWrapper.addCallBacks(LoginActivity.this);
            mCloudDBZoneWrapper.createObjectType();
            mCloudDBZoneWrapper.openCloudDBZone();
        });

        findViewById(R.id.btn_huawei).setOnClickListener(v -> signInWithHuaweiAccount());
        findViewById(R.id.btn_anonym).setOnClickListener(v -> signInWithAnonymousAccount());
        findViewById(R.id.skipLabel).setOnClickListener(v -> skipLoginPage());
        loadingPanel = findViewById(R.id.loadingPanel);

        //login page animation
        try {
            ConstraintLayout nestedScrollView = findViewById(R.id.layoutLogin);
            AnimationDrawable animationDrawable = (AnimationDrawable) nestedScrollView.getBackground();
            animationDrawable.setEnterFadeDuration(1000);
            animationDrawable.setExitFadeDuration(3000);
            animationDrawable.start();
        } catch (
                Exception allExceptions) {
            Log.d("Layout", "Cast Problem");
        }
    }

    protected void onStart() {
        super.onStart();
        if (AGConnectAuth.getInstance().getCurrentUser() != null) {
            startActivity(new Intent(LoginActivity.this, AnimalListActivity.class));
            finish();
        }
    }

    private void signInWithHuaweiAccount() {
        loadingPanel.setVisibility(View.VISIBLE);
        AGConnectAuth auth = AGConnectAuth.getInstance();
        auth.signOut();
        HuaweiIdAuthParams authParams = new HuaweiIdAuthParamsHelper(HuaweiIdAuthParams.DEFAULT_AUTH_REQUEST_PARAM)
                .setIdToken()
                .setAccessToken()
                .createParams();
        HuaweiIdAuthService mHuaweiIdAuthService = HuaweiIdAuthManager.getService(LoginActivity.this, authParams);
        startActivityForResult(mHuaweiIdAuthService.getSignInIntent(), Constant.REQUEST_SIGN_IN_LOGIN);
    }

    private void signInWithAnonymousAccount() {
        loadingPanel.setVisibility(View.VISIBLE);
        AGConnectAuth.getInstance().signInAnonymously().addOnSuccessListener(new OnSuccessListener<SignInResult>() {
            @Override
            public void onSuccess(SignInResult signInResult) {
                startActivity(new Intent(LoginActivity.this, AnimalListActivity.class));
                finish();
            }
        }).addOnFailureListener(e -> {
            Log.d(TAG, "Error " + e);
            Toast.makeText(LoginActivity.this, "Login with Anonymous Account Failed", Toast.LENGTH_SHORT).show();
        });
    }
    private void skipLoginPage() {
        loadingPanel.setVisibility(View.VISIBLE);
        Intent intent = new Intent(LoginActivity.this, AnimalListActivity.class);
        startActivity(intent);
        Bundle bundle = new Bundle();
        bundle.putInt("key", 1); //Your id
        intent.putExtras(bundle); //Put your id to your next Intent
        startActivity(intent);
        finish();
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

    private static final class MyHandler extends Handler {
        @Override
        public void handleMessage(@NonNull Message msg) {
            // dummy
        }
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Constant.REQUEST_SIGN_IN_LOGIN) {
            Task<AuthHuaweiId> authHuaweiIdTask = HuaweiIdAuthManager.parseAuthResultFromIntent(data);
            if (authHuaweiIdTask.isSuccessful()) {
                AuthHuaweiId huaweiAccount = authHuaweiIdTask.getResult();
                Log.i(TAG, "signIn success Access Token = " + huaweiAccount.getAccessToken());
                Log.i(TAG, "signIn success User Name = " + huaweiAccount.getDisplayName());

                Toast.makeText(this.getApplicationContext(), "Successfully User Name = " + huaweiAccount.getDisplayName(), Toast.LENGTH_LONG).show();
                transmitTokenIntoAppGalleryConnect(huaweiAccount.getAccessToken());

            } else {
                Log.i(TAG, "signIn failed: " + ((ApiException) authHuaweiIdTask.getException()).getStatusCode());
            }
        }
    }

    private void transmitTokenIntoAppGalleryConnect(String accessToken) {
        AGConnectAuthCredential credential = HwIdAuthProvider.credentialWithToken(accessToken);
        Log.d(TAG, "accessToken: " + accessToken);
        Log.d(TAG, "credential: " + credential);
        AGConnectAuth.getInstance().signIn(credential).addOnSuccessListener(signInResult -> {
            Log.d("user1", "asdqq");
            SharedPreferences pref = getApplicationContext().getSharedPreferences("MyPref", MODE_PRIVATE);
            SharedPreferences.Editor editor = pref.edit();
            editor.putString("token", accessToken);  // Saving token
            // Save the changes in SharedPreferences
            editor.apply(); // commit changes
            Log.d("user1", "asd");
            User u = new User(signInResult.getUser().getUid(), accessToken, signInResult.getUser().getDisplayName());
            Log.d("user1", "dsfsdfsdfds");
            mCloudDBZoneWrapper.insertUser(u);
            Log.d("user1", "dsssqqqfsdfsdfds");
            mCloudDBZoneWrapper.closeCloudDBZone();
            startActivity(new Intent(LoginActivity.this, AnimalListActivity.class));
            finish();
        }).addOnFailureListener(e -> Log.d(TAG, "ErrorTransmitTokenAppGallery: " + e));

    }

}