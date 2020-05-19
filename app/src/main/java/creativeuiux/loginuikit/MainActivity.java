package creativeuiux.loginuikit;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;
import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.tbruyelle.rxpermissions2.RxPermissions;

import java.util.Arrays;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private GoogleSignInClient mGoogleSignInClient;
    private FirebaseAuth mAuth;
    private int RC_GOOGLE_SIGN_IN = 1;
    private CallbackManager mCallbackManager;
    RxPermissions rxPermissions;
    FirebaseRemoteConfig mFirebaseRemoteConfig;
    long cacheExpiration = 0;
    private int themeValue;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FacebookSdk.sdkInitialize(getApplicationContext());
        setContentView(R.layout.activity_login);

        final ProgressDialog progressDialog = new ProgressDialog(MainActivity.this);
        progressDialog.setMessage("Please Wait...");


        LoginButton mFaceLoginButton = findViewById(R.id.login_button);
        SignInButton mGoogleLoginButton = findViewById(R.id.google_button);




        mFaceLoginButton.setReadPermissions(Arrays.asList(
                "public_profile", "email"));
        mCallbackManager = CallbackManager.Factory.create();


        mFaceLoginButton.registerCallback(mCallbackManager, new FacebookCallback<LoginResult>() {

            @Override
            public void onSuccess(LoginResult loginResult) {


                handleFacebookToken(loginResult.getAccessToken());
            }

            @Override
            public void onCancel() {

            }

            @Override
            public void onError(FacebookException error) {

            }
        });

        rxPermissions = new RxPermissions(this);


        mAuth = FirebaseAuth.getInstance();
        if (mAuth.getCurrentUser() != null) {
            progressDialog.show();

        }


        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id)).requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        //remoteconfig
        mFirebaseRemoteConfig = FirebaseRemoteConfig.getInstance();

        mFirebaseRemoteConfig.fetch(cacheExpiration)
                .addOnCompleteListener(this, new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {


                            mFirebaseRemoteConfig.activateFetched();
                            progressDialog.dismiss();
                            setFetchedValues();
                        } else {
                            Toast.makeText(MainActivity.this, "Fetch Failed",
                                    Toast.LENGTH_SHORT).show();
                        }


                    }
                });


        String[] permissions = {
                Manifest.permission.READ_PHONE_STATE, Manifest.permission.ACCESS_NETWORK_STATE,
                Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.INTERNET, Manifest.permission.ACCESS_WIFI_STATE};

        rxPermissions.request(permissions).subscribe();


        mGoogleLoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                signInGoogle();

            }


        });


    }

    private void handleFacebookToken(AccessToken accessToken) {

        final ProgressDialog progressDialog = new ProgressDialog(MainActivity.this);
        progressDialog.setMessage("Please Wait...");
        progressDialog.show();

        AuthCredential credential = FacebookAuthProvider.getCredential(accessToken.getToken());
        mAuth.signInWithCredential(credential).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {

                if (task.isSuccessful()) {

                    FirebaseUser user = mAuth.getCurrentUser();
                    MapActivity.url = user.getProviderData().get(0).getPhotoUrl().toString();
                    MapActivity.email = user.getProviderData().get(0).getEmail().toString();
                    MapActivity.name = user.getProviderData().get(0).getDisplayName().toString();

                    progressDialog.dismiss();


                    final Intent intent = new Intent(MainActivity.this, MapActivity.class);
                    startActivity(intent);

                }


            }
        });

    }

    private void setFetchedValues() {



        String dis = mFirebaseRemoteConfig.getString("distance");
        String them = mFirebaseRemoteConfig.getString("theme");


        int distance = Integer.parseInt(dis);
        int theme = Integer.parseInt(them);

        MapActivity.distanceValue = distance;
        MapActivity.themeValue = theme;

        if (mAuth.getCurrentUser() != null) {


            autoLogin();

        }


    }

    private void autoLogin() {


        try {
            MapActivity.url = mAuth.getCurrentUser().getPhotoUrl().toString();
            MapActivity.email = mAuth.getCurrentUser().getEmail().toString();
            MapActivity.name = mAuth.getCurrentUser().getDisplayName();

        } catch (Exception e) {

            GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(getApplicationContext());
            MapActivity.url = account.getPhotoUrl().toString();
            MapActivity.email = account.getEmail().toString();
            MapActivity.name = account.getDisplayName().toString();
        }


        final Intent intent = new Intent(MainActivity.this, MapActivity.class);
        startActivity(intent);

    }


    private void signInGoogle() {
        final ProgressDialog progressDialog = new ProgressDialog(MainActivity.this);
        progressDialog.setMessage("Please Wait...");
        progressDialog.show();
        Intent SignGoogleIntent = mGoogleSignInClient.getSignInIntent();
        progressDialog.dismiss();
        startActivityForResult(SignGoogleIntent, RC_GOOGLE_SIGN_IN);


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {


        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_GOOGLE_SIGN_IN) {

            final ProgressDialog progressDialog = new ProgressDialog(MainActivity.this);
            progressDialog.setMessage("Please Wait...");
            progressDialog.show();
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            progressDialog.dismiss();

            handleGoogleSignIN(task);
        } else {

            mCallbackManager.onActivityResult(requestCode, resultCode, data);
            super.onActivityResult(requestCode, resultCode, data);


        }
    }

    private void handleGoogleSignIN(Task<GoogleSignInAccount> completedTask) {

        try {

            GoogleSignInAccount acc = completedTask.getResult(ApiException.class);


            FireBaseGoogleAuth(acc);

        } catch (ApiException e) {

            Toast.makeText(MainActivity.this, "failed login", Toast.LENGTH_LONG);
            FireBaseGoogleAuth(null);
        }


    }

    private void FireBaseGoogleAuth(GoogleSignInAccount ac) {

        final ProgressDialog progressDialog = new ProgressDialog(MainActivity.this);
        progressDialog.setMessage("Please Wait...");
        progressDialog.show();

        final AuthCredential authCredential = GoogleAuthProvider.getCredential(ac.getIdToken(), null);
        mAuth.signInWithCredential(authCredential).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {

                if (task.isSuccessful()) {

                    Log.d("1", "successs");
                    FirebaseUser user = mAuth.getCurrentUser();
                    progressDialog.dismiss();
                    updateUI(user);



                }


            }
        });


    }

    private void updateUI(FirebaseUser user) {


        final ProgressDialog progressDialog = new ProgressDialog(MainActivity.this);
        progressDialog.setMessage("Please Wait...");
        progressDialog.show();

        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(getApplicationContext());


        if (account != null) {

            MapActivity.url = account.getPhotoUrl().toString();
            MapActivity.email = account.getEmail().toString();
            MapActivity.name = account.getDisplayName().toString();
            progressDialog.dismiss();

            Intent intent = new Intent(MainActivity.this, MapActivity.class);
            startActivity(intent);

        }

    }
}
