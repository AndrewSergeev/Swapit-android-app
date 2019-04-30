package il.co.freebie.swapit;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.List;

/**
 * Created by one 1 on 11-Feb-19.
 */

public class AdSquareAdapter extends RecyclerView.Adapter<AdSquareAdapter.AdSquaredViewHolder>  {
    private List<Advertisement> adsList;
    private AdSquareAdapter.AdSquareListener listener;

    @Override
    public AdSquaredViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.ad_squared_cell,parent,false);
        AdSquaredViewHolder advertisementViewHolder = new AdSquaredViewHolder(view);
        return advertisementViewHolder;
    }

    @Override
    public void onBindViewHolder(AdSquaredViewHolder holder, int position) {
        Advertisement ad = adsList.get(position);
        if(ad.getAdPhotosList() != null && ad.getAdPhotosList().size() >= 1){
            Picasso.get().load(ad.getAdPhotosList().get(0)).resize(200,200).into(holder.photoIv);
            holder.photoIv.setScaleType(ImageView.ScaleType.CENTER_CROP);
        }

        holder.nameTv.setText(ad.getAdName());
    }

    public class AdSquaredViewHolder extends RecyclerView.ViewHolder{

        ImageView photoIv;
        TextView nameTv;

        public AdSquaredViewHolder(View itemView) {
            super(itemView);

            photoIv = itemView.findViewById(R.id.ad_squared_iv);
            nameTv = itemView.findViewById(R.id.ad_squared_tv);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(listener != null)
                    {
                        listener.OnSquaredAdClicked(getAdapterPosition(), view);
                    }
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return adsList.size();
    }

    interface AdSquareListener {
        void OnSquaredAdClicked(int position, View view);
    }

    public void setListener(AdSquareListener listener) {
        this.listener = listener;
    }

    public AdSquareAdapter(List<Advertisement> adsList) {
        this.adsList = adsList;
    }
}
