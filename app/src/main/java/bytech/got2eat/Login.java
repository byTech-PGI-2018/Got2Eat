package bytech.got2eat;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

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

import java.sql.Time;
import java.util.HashMap;
import java.util.Map;

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

    private int RC_SIGN_IN = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.AppTheme);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        //Comment for testing gpg key
        db = FirebaseFirestore.getInstance();

        final String email = "overskilers@gmail.com";
        final String pass = "qwerty";

        mAuth = FirebaseAuth.getInstance();
        mAuth.signOut();
        if (mAuth.getCurrentUser()!=null){
            Toast.makeText(this, R.string.already_login, Toast.LENGTH_SHORT).show();
            Log.i(TAG, "User already logged in");

            //Create new intent to main activity
            Toast.makeText(context, "Logged in", Toast.LENGTH_SHORT).show();
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
        /*googleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isNetworkConnected()){
                    //Log in with google
                    signInGoogle();
                }
                else{
                    Log.e(TAG, "Tried to login without internet");
                    Toast.makeText(context, R.string.no_internet, Toast.LENGTH_SHORT).show();
                }
            }
        });*/

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

        Handler h = new Handler();
        h.postDelayed(new Runnable() {
            @Override
            public void run() {
                signInEmailPassword(email, pass);
            }
        }, 1000);
    }

    private void signInEmailPassword(String email, String password){
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                    @Override
                    public void onSuccess(AuthResult authResult) {
                        Log.d(TAG, "Entered onSuccess");
                        if (authResult.getUser()!=null){
                            //Update timestamp in database
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
                        else Toast.makeText(context, "Wrong user/password", Toast.LENGTH_SHORT).show();
                        alreadyClicked = false;
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, "Exception when sign in: " + e);
                        Toast.makeText(context, "Login failed", Toast.LENGTH_SHORT).show();
                        alreadyClicked = false;
                    }
                });
        Log.d(TAG, "Finished login");
    }

    private void signInGoogle(){
        Intent signIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(signIntent,RC_SIGN_IN);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==RC_SIGN_IN){
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            if(result.isSuccess()){
                GoogleSignInAccount account = result.getSignInAccount();
                authWithGoogle(account);
            }
        }
    }

    private void authWithGoogle(GoogleSignInAccount account) {
        AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(),null);
        mAuth.signInWithCredential(credential).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()){
                    //Update value in database
                    Map<String,Object> data = new HashMap<>();
                    data.put("lastlogin", Timestamp.now());
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
