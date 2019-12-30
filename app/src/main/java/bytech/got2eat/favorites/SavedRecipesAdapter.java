package bytech.got2eat.favorites;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import bytech.got2eat.R;
import bytech.got2eat.recipe.Recipe;
import bytech.got2eat.recipe.RecipeShow;

public class SavedRecipesAdapter extends RecyclerView.Adapter<SavedRecipesAdapter.ViewHolder>{

    private ArrayList<Recipe> recipes;
    private Context context;

    public SavedRecipesAdapter(ArrayList<Recipe> recipes, Context context) {
        this.recipes = recipes;
        this.context = context;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // create a new view
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.single_saved_recipe, parent, false);
        ViewHolder vh = new ViewHolder(v);
        return vh;
    }



    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        final String recipeId = recipes.get(position).getId();
        holder.name.setText(recipes.get(position).getName());
        holder.view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, RecipeShow.class);
                intent.putExtra("firestoreId", recipeId);
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return recipes.size();
    }


    public class ViewHolder extends RecyclerView.ViewHolder{

        View view;
        TextView name;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.recipe_name);
            view = itemView;
        }
    }
}
