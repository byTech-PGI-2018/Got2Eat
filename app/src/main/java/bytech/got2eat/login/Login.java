package bytech.got2eat.login;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.airbnb.lottie.LottieAnimationView;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

import bytech.got2eat.home.Home;
import bytech.got2eat.R;

//FIXME: View does not resize when opening keyboard

public class Login extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener {

    private static final String TAG = "LoginActivity";
    FirebaseAuth mAuth;
    Button loginButton;
    Button registerButton;
    Button googleButton;
    Button passwordVisibilityButton;
    EditText loginPassword;
    EditText loginEmail;
    private boolean passwordVisible = false;
    private GoogleApiClient mGoogleApiClient;
    private Context context = this;
    private Login thisInstance = this;
    private FirebaseFirestore db;
    private boolean alreadyClicked = false;
    private LottieAnimationView animationView;

    private int failedAttempts;
    private int RC_SIGN_IN = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.AppTheme);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        failedAttempts = 0;

        animationView = findViewById(R.id.animation_view);
        animationView.setVisibility(View.GONE);
        animationView.setRepeatCount(ValueAnimator.INFINITE);

        //Comment for testing gpg key
        db = FirebaseFirestore.getInstance();

        mAuth = FirebaseAuth.getInstance();
        if (mAuth.getCurrentUser()!=null){
            //Toast.makeText(this, R.string.already_login, Toast.LENGTH_SHORT).show();
            Log.i(TAG, "User already logged in");

            //Create new intent to main activity
            //Toast.makeText(context, "Logged in", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(thisInstance, Home.class);
            startActivity(intent);
            finish();
        }

        //Firebase auth
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleApiClient = new GoogleApiClient.Builder(context)
                .enableAutoManage(Login.this,thisInstance)
                .addApi(Auth.GOOGLE_SIGN_IN_API,gso)
                .build();

        loginPassword = findViewById(R.id.login_password_inputLayout);
        loginEmail = findViewById(R.id.login_email_inputLayout);

        loginButton = findViewById(R.id.login_button);
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isNetworkConnected()){
                    if (!alreadyClicked){
                        alreadyClicked = true;
                        String password = loginPassword.getText().toString().trim();
                        String email = loginEmail.getText().toString().trim();

                        if (validateInput(password, email)==0){
                            loginButton.setVisibility(View.GONE);
                            registerButton.setVisibility(View.GONE);
                            animationView.setVisibility(View.VISIBLE);
                            animationView.setRepeatCount(ValueAnimator.INFINITE);
                            animationView.playAnimation();
                            Log.d(TAG, "Password: " + loginPassword.getText().toString());
                            Log.d(TAG, "Email: " + loginEmail.getText().toString());

                            //Log in
                            signInEmailPassword(email, password);
                        }
                    }
                }
                else{
                    Log.e(TAG, "Tried to login without network");
                    Toast.makeText(context, getString(R.string.no_internet), Toast.LENGTH_SHORT).show();
                }
            }
        });

        registerButton = findViewById(R.id.register_button);
        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!alreadyClicked){
                    alreadyClicked = true;
                    Intent intent = new Intent(Login.this, Register.class);
                    startActivity(intent);
                    alreadyClicked = false;
                }
            }
        });

        googleButton = findViewById(R.id.google_button);
        googleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isNetworkConnected()){
                    //Log in with google
                    Log.d(TAG, "Clicked and internet is up");
                    signInGoogle();
                }
                else{
                    Log.e(TAG, "Tried to login without internet");
                    Toast.makeText(context, R.string.no_internet, Toast.LENGTH_SHORT).show();
                }
            }
        });

        passwordVisibilityButton = findViewById(R.id.icon_password_visibility);
        passwordVisibilityButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                passwordVisible = !passwordVisible;
                if (passwordVisible){
                    passwordVisibilityButton.setBackground(ContextCompat.getDrawable(context, R.drawable.password_visible));
                    loginPassword.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                }
                else{
                    passwordVisibilityButton.setBackground(ContextCompat.getDrawable(context, R.drawable.password_not_visible));
                    loginPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                }
                loginPassword.setSelection(loginPassword.length());
            }
        });
    }

    private void signInEmailPassword(String email, String password){
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                    @Override
                    public void onSuccess(AuthResult authResult) {
                        if (authResult.getUser()!=null){
                            //Update timestamp in database
                            failedAttempts = 0;
                            Map<String, Object> data = new HashMap<>();
                            data.put("lastlogin", Timestamp.now());
                            db.collection("users").document(authResult.getUser().getUid())
                                    .update(data);

                            //Create new intent to main activity
                            Toast.makeText(context, "Logged in", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(thisInstance, Home.class);
                            startActivity(intent);
                            finish();
                            Handler h = new Handler();
                            h.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    alreadyClicked = false;
                                }
                            }, 2000);
                        }
                        else Toast.makeText(context, "Email/password incorreto", Toast.LENGTH_SHORT).show();
                        alreadyClicked = false;
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        failedAttempts+=1;
                        loginButton.setVisibility(View.VISIBLE);
                        registerButton.setVisibility(View.VISIBLE);
                        animationView.pauseAnimation();
                        animationView.setVisibility(View.GONE);
                        Toast.makeText(context, "Erro de login.", Toast.LENGTH_SHORT).show();
                        if (failedAttempts > 3){
                            Toast.makeText(context, "Associou este email a uma conta Google?", Toast.LENGTH_LONG).show();
                        }
                        alreadyClicked = false;
                    }
                });
    }

    private void signInGoogle(){
        Log.d(TAG, "Trying to sign in with google");
        Intent signIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(signIntent, RC_SIGN_IN);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==RC_SIGN_IN){
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            Log.d(TAG, "Result is :" + result);
            Log.d(TAG, "Result status: " + result.getStatus());
            if(result.isSuccess()){
                Log.d(TAG, "ActivityResult is successful, starting authWtihGoogle");
                GoogleSignInAccount account = result.getSignInAccount();
                authWithGoogle(account);
            }
        }
    }

    private void authWithGoogle(GoogleSignInAccount account) {
        Log.d(TAG, "Started authWithGoogle");
        AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(),null);
        mAuth.signInWithCredential(credential).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()){
                    //Update value in database
                    Map<String,Object> data = new HashMap<>();
                    data.put("lastlogin", Timestamp.now());
                    Log.d(TAG,"Task signInGoogle is success: " + FirebaseAuth.getInstance().getUid());
                    db.collection("users").document(FirebaseAuth.getInstance().getUid())
                            .update(data);
                    startActivity(new Intent(getApplicationContext(),Login.class));
                    finish();
                }
                else{
                    Toast.makeText(getApplicationContext(),"Auth Error",Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private int validateInput(String password, String email){
        if (password.isEmpty()){
            loginPassword.setError(getString(R.string.empty_username));
            alreadyClicked = false;
            return 1;
        }
        else if (email.isEmpty()){
            alreadyClicked = false;
            loginEmail.setError(getString(R.string.empty_username));
            return 1;
        }
        else{
            loginPassword.setError(null);
            loginEmail.setError(null);
            return 0;
        }
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
    }

    private boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        return cm.getActiveNetworkInfo() != null;
    }
}
