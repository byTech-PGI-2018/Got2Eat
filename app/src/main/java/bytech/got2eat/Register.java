package bytech.got2eat;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class Register extends AppCompatActivity {

    FirebaseAuth mAuth;

    TextInputLayout registerName;
    TextInputLayout registerUsername;
    TextInputLayout registerEmail;
    TextInputLayout registerPassword;
    Button registerButton;
    private Context context = this;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mAuth = FirebaseAuth.getInstance();

        registerName = findViewById(R.id.register_name_inputLayout);
        registerUsername = findViewById(R.id.register_username_inputLayout);
        registerEmail = findViewById(R.id.register_email_inputLayout);
        registerPassword = findViewById(R.id.register_password_inputLayout);

        registerButton = findViewById(R.id.confirm_button);
        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String password = registerPassword.getEditText().getText().toString();
                String name = registerName.getEditText().getText().toString();
                String username = registerUsername.getEditText().getText().toString();
                String email = registerEmail.getEditText().getText().toString();
                //Check input first
                if (validateInput(password, email, name, username)==0){
                    //Create the account
                    register(email, password);
                }
            }
        });
    }

    private void register(String email, String password){
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                    @Override
                    public void onSuccess(AuthResult authResult) {
                        if (authResult.getUser()!=null) Toast.makeText(context, "Registered", Toast.LENGTH_SHORT).show();
                        else Toast.makeText(context, "Register failed", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(context, "Register failed", Toast.LENGTH_SHORT).show();
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
}
