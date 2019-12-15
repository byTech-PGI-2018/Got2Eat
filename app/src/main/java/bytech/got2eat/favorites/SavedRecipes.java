package bytech.got2eat.favorites;

import android.animation.ValueAnimator;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;

import android.view.View;
import android.widget.TextView;
import com.airbnb.lottie.LottieAnimationView;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.ArrayList;

import bytech.got2eat.R;
import bytech.got2eat.recipe.Recipe;

public class SavedRecipes extends AppCompatActivity {

    private static final String TAG = "SavedRecipes";
    private RecyclerView recyclerView;
    private LinearLayoutManager llm;
    private SavedRecipesAdapter adapter;
    private FirebaseFirestore db;
    private Context context = this;
    private LottieAnimationView animationView;
    private TextView noRecipesText;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.saved_recipes);

        db = FirebaseFirestore.getInstance();

        final ArrayList<Recipe> recipes = new ArrayList<>();

        animationView = findViewById(R.id.animation_view);
        noRecipesText = findViewById(R.id.no_recipes_here);
        noRecipesText.setVisibility(View.GONE);

        animationView.setRepeatCount(ValueAnimator.INFINITE);
        animationView.playAnimation();

        recyclerView = findViewById(R.id.saved_recipes);
        llm = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(llm);

        //Update recyclerview
        adapter = new SavedRecipesAdapter(recipes, context);
        recyclerView.setAdapter(adapter);

        //Load recipes from database
        db.collection("users").document(FirebaseAuth.getInstance().getUid())
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()){
                            DocumentSnapshot snap = task.getResult();
                            ArrayList<String> savedRecipes = (ArrayList<String>)snap.get("saved");
                            if (savedRecipes!=null && !savedRecipes.isEmpty()){
                                for (final String id:savedRecipes){
                                    Log.d(TAG, "On id: " + id);
                                    //Get recipe name
                                    db.collection("receitas").document(id).get()
                                            .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                                @Override
                                                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                                    if (task.isSuccessful()){
                                                        Log.d(TAG, "Found recipe for id: " + id + " with name: " + (String)task.getResult().get("name"));
                                                        Recipe recipe = new Recipe((String)task.getResult().get("name"), id);
                                                        recipes.add(recipe);
                                                        animationView.pauseAnimation();
                                                        animationView.setVisibility(View.GONE);
                                                        adapter.notifyItemInserted(recipes.size()-1);
                                                    }
                                                    else{
                                                        Log.d(TAG, "No recipe found for id: " + id);
                                                    }
                                                }
                                            });
                                }
                            }
                            else{
                                animationView.pauseAnimation();
                                animationView.setVisibility(View.GONE);
                                noRecipesText.setText(R.string.no_saved_recipes);
                                noRecipesText.setVisibility(View.VISIBLE);
                            }
                        }
                        else{
                            Log.d(TAG, "No user found");
                        }
                    }
                });
    }
}
