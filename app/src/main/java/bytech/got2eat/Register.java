package bytech.got2eat;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
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

import com.airbnb.lottie.LottieAnimationView;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class Register extends AppCompatActivity {

    private static final String TAG = "Register";
    FirebaseAuth mAuth;

    EditText registerName;
    EditText registerUsername;
    EditText registerEmail;
    EditText registerPassword;
    Button registerButton;
    Button passwordVisibilityButton;
    private boolean passwordVisible = false;
    private Context context = this;
    private FirebaseFirestore db;
    private LottieAnimationView animationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        db = FirebaseFirestore.getInstance();

        animationView = findViewById(R.id.animation_view);
        animationView.setVisibility(View.GONE);
        animationView.setRepeatCount(ValueAnimator.INFINITE);

        mAuth = FirebaseAuth.getInstance();

        registerName = findViewById(R.id.register_name_inputLayout);
        registerUsername = findViewById(R.id.register_username_inputLayout);
        registerEmail = findViewById(R.id.register_email_inputLayout);
        registerPassword = findViewById(R.id.register_password_inputLayout);

        registerButton = findViewById(R.id.confirm_button);
        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isNetworkConnected()){
                    String password = registerPassword.getText().toString();
                    String name = registerName.getText().toString();
                    String username = registerUsername.getText().toString();
                    String email = registerEmail.getText().toString();

                    //Check input first
                    if (validateInput(password, email, name, username)==0){
                        registerButton.setVisibility(View.GONE);
                        animationView.setVisibility(View.VISIBLE);
                        animationView.playAnimation();

                        Map<String, Object> userData = new HashMap<>();
                        userData.put("username", username);
                        userData.put("firstname", name);
                        userData.put("email", email);
                        //Create the account
                        register(email, password, userData);
                    }
                }
                else{
                    Log.e(TAG, "Tried to register without internet");
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
                    registerPassword.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                }
                else{
                    passwordVisibilityButton.setBackground(ContextCompat.getDrawable(context, R.drawable.password_not_visible));
                    registerPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                }
                registerPassword.setSelection(registerPassword.length());
            }
        });
    }

    private void register(String email, String password, final Map<String, Object> userData){
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                    @Override
                    public void onSuccess(AuthResult authResult) {
                        if (authResult.getUser()!=null){
                            //Create new user in database
                            db.collection("users").document(authResult.getUser().getUid())
                                    .set(userData)
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            //Data added successfully
                                            Toast.makeText(context, "Registo completo", Toast.LENGTH_SHORT).show();
                                            finish();
                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Toast.makeText(context, "Erro no registo", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                        }
                        else{
                            animationView.pauseAnimation();
                            animationView.setVisibility(View.GONE);
                            registerButton.setVisibility(View.VISIBLE);
                            Toast.makeText(context, "Erro no registo", Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        animationView.pauseAnimation();
                        animationView.setVisibility(View.GONE);
                        registerButton.setVisibility(View.VISIBLE);
                        Toast.makeText(context, "Erro no registo", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private int validateInput(String password, String email, String name, String username){
        if (password.isEmpty()){
            registerPassword.setError(getString(R.string.empty_username));
            return 1;
        }
        else if (email.isEmpty()){
            registerEmail.setError(getString(R.string.empty_username));
            return 1;
        }
        else if (name.isEmpty()){
            registerName.setError(getString(R.string.empty_username));
            return 1;
        }
        else if (username.isEmpty()){
            registerUsername.setError(getString(R.string.empty_username));
            return 1;
        }
        else{
            registerPassword.setError(null);
            registerName.setError(null);
            registerUsername.setError(null);
            registerEmail.setError(null);
            return 0;
        }
    }

    private boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        return cm.getActiveNetworkInfo() != null;
    }

    public void gotoLogin(View view){
        finish();
    }
}
