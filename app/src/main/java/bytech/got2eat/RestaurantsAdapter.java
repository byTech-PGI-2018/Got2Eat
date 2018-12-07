package bytech.got2eat;

import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;


import com.google.android.gms.tasks.OnSuccessListener;

import java.util.ArrayList;

class MyAdapter extends RecyclerView.Adapter<MyAdapter.ViewHolder> {
    private ArrayList<Restaurant> mDataset;
    private Context context;


    public MyAdapter(ArrayList<Restaurant> myDataset, Context context) {
        this.mDataset = myDataset;
        this.context = context;
    }



    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // create a new view
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.activity_restaurantes, parent, false);
        ViewHolder vh = new ViewHolder(v);
        return vh;
    }



    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        final Restaurant items = mDataset.get(position);
        holder.distance.setText("A cerca de " + items.distancia + " metros");
        holder.name.setText(items.nome);
        holder.address.setText(items.morada);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try{
                    Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(items.link));
                    context.startActivity(browserIntent);
                }catch(Exception e){
                    Toast.makeText(context,"Erro ao obter direções",Toast.LENGTH_LONG).show();
                }
            }
        });

    }

    @Override
    public int getItemCount() {
        return mDataset.size();
    }


    public class ViewHolder extends RecyclerView.ViewHolder{

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.restaurant_name);
            address = itemView.findViewById(R.id.restaurant_address);
            distance = itemView.findViewById(R.id.distance);
        }

        TextView name;
        TextView address;
        TextView distance;

    }

}