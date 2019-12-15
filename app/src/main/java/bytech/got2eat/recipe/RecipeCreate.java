package bytech.got2eat.recipe;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import bytech.got2eat.R;

public class RecipeCreate extends AppCompatActivity {

    private static final String TAG = "RecipeCreate";
    private FirebaseFirestore db;
    private TextView recipeName;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipe);

        db = FirebaseFirestore.getInstance();

        String firestoreId = getIntent().getStringExtra("firestoreId");

        recipeName = findViewById(R.id.recipeName);

        db.collection("receitas").document(firestoreId)
                .get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document.exists()) {
                                recipeName.setText((String)document.get("name"));
                            } else {
                                Log.d(TAG, "No such document");
                            }
                        } else {
                            Log.d(TAG, "get failed with ", task.getException());
                        }
                    }
                });
    }
}
