package bytech.got2eat;

import android.content.Context;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.airbnb.lottie.LottieAnimationView;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.lang.reflect.Array;
import java.util.ArrayList;

public class RecipeShow extends AppCompatActivity {

    private static final String TAG = "RecipeShow";
    private FirebaseFirestore db;
    private TextView recipeName;
    private TextView recipeDuration;
    private TextView recipePortion;
    private TextView recipeIngredients;
    private TextView recipePrep;
    private Button hideIngredients;
    private Button hidePrep;
    private boolean ingredientsUpArrow = false;
    private boolean prepUpArrow = false;
    private boolean isSaved = false;
    private boolean savingEnabled = false;
    private LottieAnimationView animationView;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_recipe);

        if (isNetworkConnected()){
            db = FirebaseFirestore.getInstance();

            recipeName = findViewById(R.id.recipe_name);
            recipeDuration = findViewById(R.id.recipe_duration_value);
            recipePortion = findViewById(R.id.recipe_portion_value);
            recipeIngredients = findViewById(R.id.recipe_ingredients);
            recipePrep = findViewById(R.id.recipe_preparation);
            hideIngredients = findViewById(R.id.recipe_ingredients_hide_btn);
            hidePrep = findViewById(R.id.recipe_preparation_hide_btn);
            animationView = findViewById(R.id.animation_view);

            final String recipeId = getIntent().getStringExtra("firestoreId");

            db.collection("receitas").document(recipeId)
                    .get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            recipeName.setText((String)document.get("name"));
                            recipeDuration.setText((String)document.get("tempo"));
                            recipePortion.setText((String)document.get("porção"));
                            String temp = "";
                            ArrayList<String> stringArray = (ArrayList<String>) document.get("quantidade");
                            if (stringArray != null){
                                for (String s:stringArray){
                                    s = s.substring(0,1).toUpperCase() + s.substring(1);
                                    temp += (s+"\n");
                                }
                            }
                            recipeIngredients.setText(temp);
                            temp = "";
                            stringArray = (ArrayList<String>) document.get("preparação");
                            if (stringArray != null){
                                for (String s:stringArray){
                                    temp += (s+"\n");
                                }
                            }
                            recipePrep.setText(temp);
                        } else {
                            Log.d(TAG, "No such document");
                        }
                    } else {
                        Log.d(TAG, "get failed with ", task.getException());
                    }
                }
            });
            
            final Animation animationIngredients = AnimationUtils.loadAnimation(this, R.anim.rotate_180);
            final Animation animationPrep = AnimationUtils.loadAnimation(this, R.anim.rotate_180);

            hideIngredients.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    hideIngredients.startAnimation(animationIngredients);
                    if (ingredientsUpArrow){
                        hideIngredients.setBackground(getDrawable(R.drawable.down_arrow));
                    }
                    else hideIngredients.setBackground(getDrawable(R.drawable.up_arrow));
                    ingredientsUpArrow = !ingredientsUpArrow;
                    ScrollView scrollView = findViewById(R.id.scroll_ingredients);
                    if (scrollView.getVisibility() == View.VISIBLE){
                        scrollView.setVisibility(View.GONE);
                    }
                    else scrollView.setVisibility(View.VISIBLE);
                }
            });

            hidePrep.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    hidePrep.startAnimation(animationPrep);
                    if (prepUpArrow){
                        hidePrep.setBackground(getDrawable(R.drawable.down_arrow));
                    }
                    else hidePrep.setBackground(getDrawable(R.drawable.up_arrow));
                    prepUpArrow = !prepUpArrow;
                    ScrollView scrollView = findViewById(R.id.scroll_preparation);
                    if (scrollView.getVisibility() == View.VISIBLE){
                        scrollView.setVisibility(View.GONE);
                    }
                    else scrollView.setVisibility(View.VISIBLE);
                }
            });

            animationView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Log.d(TAG, "isSaved: " + isSaved);
                    if (savingEnabled){
                        if (!isSaved){
                            db.collection("users").document(FirebaseAuth.getInstance().getUid())
                                    .update("saved", FieldValue.arrayUnion(recipeId))
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()){
                                                animationView.setMinProgress(0.2f);
                                                animationView.setMaxProgress(0.7f);
                                                animationView.playAnimation();
                                                isSaved = true;
                                            }
                                            else{
                                                Log.d(TAG, "Failed to save recipe on database");
                                                Toast.makeText(RecipeShow.this, R.string.recipe_not_saved, Toast.LENGTH_LONG).show();
                                            }
                                        }
                                    });
                        }
                        else{
                            db.collection("users").document(FirebaseAuth.getInstance().getUid())
                                    .update("saved", FieldValue.arrayRemove(recipeId))
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()){
                                                animationView.setMinProgress(0.7f);
                                                animationView.setMaxProgress(1f);
                                                animationView.playAnimation();
                                                isSaved = false;
                                            }
                                            else{
                                                Log.d(TAG, "Failed to unsave recipe on database");
                                                Toast.makeText(RecipeShow.this, R.string.recipe_not_unsaved, Toast.LENGTH_LONG).show();
                                            }
                                        }
                                    });
                        }
                    }
                }
            });

            //Check if this recipe is already saved in user's account
            db.collection("users").document(FirebaseAuth.getInstance().getUid())
                    .get()
                    .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                        @Override
                        public void onSuccess(DocumentSnapshot documentSnapshot) {
                            ArrayList<String> savedRecipes = (ArrayList<String>) documentSnapshot.get("saved");
                            isSaved = (savedRecipes!=null && !savedRecipes.isEmpty() && savedRecipes.contains(recipeId));
                            if (isSaved){
                                animationView.setMinProgress(0.7f);
                                animationView.setMaxProgress(1f);
                            }
                            else{
                                animationView.setMinProgress(0.2f);
                                animationView.setMaxProgress(0.7f);
                            }
                            savingEnabled = true;
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.d(TAG, "Failed to check if recipe is already saved");
                        }
                    });
        }
        else{
            Log.e(TAG, "Tried to show recipe without internet");
            Toast.makeText(this, R.string.no_internet, Toast.LENGTH_LONG).show();
        }
    }

    private boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        return cm.getActiveNetworkInfo() != null;
    }
}
