package bytech.got2eat;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.ArrayList;

public class SavedRecipes extends AppCompatActivity {

    private static final String TAG = "SavedRecipes";
    private RecyclerView recyclerView;
    private LinearLayoutManager llm;
    private SavedRecipesAdapter adapter;
    private FirebaseFirestore db;
    private Context context = this;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.saved_recipes);

        db = FirebaseFirestore.getInstance();

        final ArrayList<Recipe> recipes = new ArrayList<>();

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
                                                        adapter.notifyItemInserted(recipes.size()-1);
                                                    }
                                                    else{
                                                        Log.d(TAG, "No recipe found for id: " + id);
                                                    }
                                                }
                                            });
                                }
                            }
                        }
                        else{
                            Log.d(TAG, "No user found");
                        }
                    }
                });
    }
}
