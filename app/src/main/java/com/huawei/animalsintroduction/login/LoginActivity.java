package com.huawei.animalsintroduction.login;

import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.widget.NestedScrollView;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.huawei.agconnect.auth.AGConnectAuth;
import com.huawei.agconnect.auth.AGConnectAuthCredential;
import com.huawei.agconnect.auth.HwIdAuthProvider;
import com.huawei.agconnect.auth.SignInResult;
import com.huawei.animalsintroduction.Constant;
import com.huawei.animalsintroduction.MainActivity;
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

import java.util.Objects;


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
//        findViewById(R.id.btnLogin).setOnClickListener(v -> signInAccount());

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
            startActivity(new Intent(LoginActivity.this, MainActivity.class));
            finish();
        }
    }

//    private void signInAccount() {
//        editName = (EditText) findViewById(R.id.txtEmailAddress);
//        editPassword = (EditText) findViewById(R.id.txtPassword);
//        lblEmailAnswer = (TextView) findViewById(R.id.lblEmailAnswer);
//        lblPasswordAnswer = (TextView) findViewById(R.id.txtEmailAddress);
//
//        // get text from EditText name view
//        String name = editName.getText().toString();
//        // get text from EditText password view
//        String password = editPassword.getText().toString();
//
//        LoginUser loginUser = new LoginUser(name, password);
//        if (TextUtils.isEmpty(Objects.requireNonNull(loginUser).getStrEmailAddress())) {
//            editName.setError("Enter an E-Mail Address");
//            editName.requestFocus();
//        } else if (!loginUser.isEmailValid()) {
//            editName.setError("Enter a Valid E-mail Address");
//            editName.requestFocus();
//        } else if (TextUtils.isEmpty(Objects.requireNonNull(loginUser).getStrPassword())) {
//            editPassword.setError("Enter a Password");
//            editPassword.requestFocus();
//        } else if (!loginUser.isPasswordLengthGreaterThan5()) {
//            editPassword.setError("Enter at least 6 Digit password");
//            editPassword.requestFocus();
//        } else {
//            lblEmailAnswer.setText(loginUser.getStrEmailAddress());
//            lblPasswordAnswer.setText(loginUser.getStrPassword());
//        }
//    }

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
                startActivity(new Intent(LoginActivity.this, MainActivity.class));
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

//                Thread thread = new Thread() {
//                    @Override
//                    public void run() {
//                        try {
//                            Thread.sleep(500); // As I am using LENGTH_LONG in Toast
//                            startActivity(new Intent(LoginActivity.this, MainActivity.class));
//                        } catch (Exception e) {
//                            e.printStackTrace();
//                        }
//                    }
//                };
//                thread.start();

            } else {
                Log.i(TAG, "signIn failed: " + ((ApiException) authHuaweiIdTask.getException()).getStatusCode());
            }
        }
    }

    private void transmitTokenIntoAppGalleryConnect(String accessToken) {
        AGConnectAuthCredential credential = HwIdAuthProvider.credentialWithToken(accessToken);
        AGConnectAuth.getInstance().signIn(credential).addOnSuccessListener(new OnSuccessListener<SignInResult>() {
            @Override
            public void onSuccess(SignInResult signInResult) {
                startActivity(new Intent(LoginActivity.this, MainActivity.class));
                finish();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(Exception e) {
                Log.d(TAG, "Error " + e);
            }
        });
    }

}