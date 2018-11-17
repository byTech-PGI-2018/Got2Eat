package bytech.got2eat;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import io.grpc.internal.JsonParser;

public class Login extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener {

    private static final String TAG = "LoginActivity";
    FirebaseAuth mAuth;
    Button loginButton;
    Button registerButton;
    Button googleButton;
    Button uploadButton;
    ProgressBar progressBar;
    TextInputLayout loginPassword;
    TextInputLayout loginEmail;
    TextView progressReport;
    TextView loadingJson;
    private GoogleApiClient mGoogleApiClient;
    private Context context = this;
    private Login thisInstance = this;
    private FirebaseFirestore db;

    private int progressBarValue = 0;

    private int RC_SIGN_IN = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        db = FirebaseFirestore.getInstance();

        mAuth = FirebaseAuth.getInstance();
        /*if (mAuth.getCurrentUser()!=null){
            Toast.makeText(this, R.string.already_login, Toast.LENGTH_SHORT).show();
            Log.i(TAG, "User already logged in");

            //Create new intent to main activity
            Toast.makeText(context, "Logged in", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(thisInstance, Home.class);
            startActivity(intent);
            finish();
        }*/

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
                    String password = loginPassword.getEditText().getText().toString().trim();
                    String email = loginEmail.getEditText().getText().toString().trim();

                    if (validateInput(password, email)==0){
                        Log.d(TAG, "Password: " + loginPassword.getEditText().getText().toString());
                        Log.d(TAG, "Email: " + loginEmail.getEditText().getText().toString());

                        //Log in
                        signInEmailPassword(email, password);
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
                Intent intent = new Intent(Login.this, Register.class);
                startActivity(intent);
            }
        });

        googleButton = findViewById(R.id.google_button);
        googleButton.setOnClickListener(new View.OnClickListener() {
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
        });

        progressBar = findViewById(R.id.progressBar);
        progressReport = findViewById(R.id.progressReport);
        progressReport.setText("0/10");
        loadingJson = findViewById(R.id.loadingJson);
        loadingJson.setVisibility(View.GONE);
        uploadButton = findViewById(R.id.uploadButton);
        uploadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "Clicked uploadButton");
                new Upload();
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
                            db.collection("users").document(authResult.getUser().getUid())
                                    .update("lastlogin", Timestamp.now());

                            //Create new intent to main activity
                            Toast.makeText(context, "Logged in", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(thisInstance, Home.class);
                            startActivity(intent);
                            finish();
                        }
                        else Toast.makeText(context, "Wrong user/password", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(context, "Login failed", Toast.LENGTH_SHORT).show();
                    }
                });
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
            return 1;
        }
        else if (email.isEmpty()){
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

    private class Upload extends AsyncTask<Void, Void, Void>{

        Upload(){
            Log.d(TAG, "Upload constructor");
            this.execute();
        }

        @Override
        protected Void doInBackground(Void... voids) {
            //int size = receitas.length();
            Log.d(TAG, "Starting Upload");
            int size1 = 10;
            String receitas = null;
            JSONArray receitasJson = null;
            try {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        loadingJson.setVisibility(View.VISIBLE);
                        loadingJson.setText("Loading JSON file");
                    }
                });
                InputStream is = context.getAssets().open("receitas.json");
                int size = is.available();
                byte[] buffer = new byte[size];
                is.read(buffer);
                is.close();
                receitas = new String(buffer, "UTF-8");
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        loadingJson.setVisibility(View.GONE);
                    }
                });
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            try {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        loadingJson.setText("Creating JSONArray");
                        loadingJson.setVisibility(View.VISIBLE);
                    }
                });
                receitasJson = new JSONArray(receitas);
            } catch (Exception e){
                e.printStackTrace();
            }

            for (int i=0; i<size1; i++){
                    try{
                    JSONObject receita = receitasJson.getJSONObject(i);
                    JSONObject idObject = receita.getJSONObject("_id");
                    final String id = idObject.getString("$oid");
                    String nome = receita.getString("nome");
                    JSONArray secao = receita.getJSONArray("secao");
                    JSONArray conteudo = secao.getJSONObject(0).getJSONArray("conteudo");
                    String[] ingredientes = new String[conteudo.length()];
                    for (int j=0; j<conteudo.length(); j++){
                        ingredientes[j] = conteudo.getString(j).toLowerCase();
                    }
                    System.out.println("-------New receita:");
                    System.out.println("----id: " + id);
                    System.out.println("----nome: " + nome);
                    System.out.println("----ingredientes: ");
                    for (int k=0; k<ingredientes.length; k++){
                        System.out.println("--" + ingredientes[k]);
                    }
                    long unixTime = System.currentTimeMillis() / 1000L;

                    //Place in a map
                    Map<String, Object> data = new HashMap<>();
                    data.put("name", nome);
                    data.put("ingredients", Arrays.asList(ingredientes));
                    data.put("timestamp", Timestamp.now());

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            loadingJson.setText("Uploading to database");
                        }
                    });

                    //Upload to database
                    db.collection("receitas").document(id)
                            .set(data)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    progressBarValue+=1;
                                    progressBar.setProgress(progressBarValue);
                                    if (progressBarValue == progressBar.getMax()){
                                        runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                loadingJson.setText("Upload completed");
                                            }
                                        });
                                    }
                                    progressReport.setText(progressBarValue+"/"+progressBar.getMax());
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Log.e(TAG, "Failed uploading recipe with id: " + id);
                                }
                            });
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            return null;
        }
    }
}
