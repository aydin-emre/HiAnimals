package com.huawei.animalsintroduction.login;

import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.huawei.agconnect.auth.AGConnectAuth;
import com.huawei.agconnect.auth.AGConnectAuthCredential;
import com.huawei.agconnect.auth.HwIdAuthProvider;
import com.huawei.agconnect.auth.SignInResult;
import com.huawei.animalsintroduction.AnimalListActivity;
import com.huawei.animalsintroduction.Constant;
import com.huawei.animalsintroduction.R;
import com.huawei.hmf.tasks.OnFailureListener;
import com.huawei.hmf.tasks.OnSuccessListener;
import com.huawei.hmf.tasks.Task;
import com.huawei.hms.common.ApiException;
import com.huawei.hms.support.hwid.HuaweiIdAuthManager;
import com.huawei.hms.support.hwid.request.HuaweiIdAuthParams;
import com.huawei.hms.support.hwid.request.HuaweiIdAuthParamsHelper;
import com.huawei.hms.support.hwid.result.AuthHuaweiId;
import com.huawei.hms.support.hwid.service.HuaweiIdAuthService;

public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "Account Service";
//    EditText editName, editPassword;
//    TextView lblEmailAnswer, lblPasswordAnswer;
//    ViewModelProvider.Factory viewModelFactory;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_lay);

        findViewById(R.id.btn_huawei).setOnClickListener(v -> signInWithHuaweiAccount());
        findViewById(R.id.skipLabel).setOnClickListener(v -> signInWithAnonymousAccount());
        findViewById(R.id.skipLabel).setOnClickListener(v -> skipLoginPage());

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
        HuaweiIdAuthParams authParams = new HuaweiIdAuthParamsHelper(HuaweiIdAuthParams.DEFAULT_AUTH_REQUEST_PARAM)
                .setIdToken()
                .setAccessToken()
                .createParams();
        HuaweiIdAuthService mHuaweiIdAuthService = HuaweiIdAuthManager.getService(LoginActivity.this, authParams);
        startActivityForResult(mHuaweiIdAuthService.getSignInIntent(), Constant.REQUEST_SIGN_IN_LOGIN);
    }

    private void signInWithAnonymousAccount() {
        AGConnectAuth.getInstance().signInAnonymously().addOnSuccessListener(new OnSuccessListener<SignInResult>() {
            @Override
            public void onSuccess(SignInResult signInResult) {
                startActivity(new Intent(LoginActivity.this, AnimalListActivity.class));
                finish();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(Exception e) {
                Log.d(TAG, "Error " + e);
                Toast.makeText(LoginActivity.this, "Login with Anonymous Account Failed", Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void skipLoginPage() {
        Intent intent = new Intent(LoginActivity.this, AnimalListActivity.class);
        startActivity(intent);
        Bundle bundle = new Bundle();
        bundle.putInt("key", 1); //Your id
        intent.putExtras(bundle); //Put your id to your next Intent
        startActivity(intent);
        finish();
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
                startActivity(new Intent(LoginActivity.this, AnimalListActivity.class));
                finish();


            } else {
                Log.i(TAG, "signIn failed: " + ((ApiException) authHuaweiIdTask.getException()).getStatusCode());
            }
        }
    }

    private void transmitTokenIntoAppGalleryConnect(String accessToken) {
        AGConnectAuthCredential credential = HwIdAuthProvider.credentialWithToken(accessToken);
        Log.d(TAG, "accessToken: " + accessToken);
        Log.d(TAG, "credential: " + credential);
        AGConnectAuth.getInstance().signIn(credential).addOnSuccessListener(new OnSuccessListener<SignInResult>() {
            @Override
            public void onSuccess(SignInResult signInResult) {
                startActivity(new Intent(LoginActivity.this, AnimalListActivity.class));
                finish();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(Exception e) {
                Log.d(TAG, "Error: " + e);
            }
        });
    }

}