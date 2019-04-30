package il.co.freebie.swapit;

import android.app.Application;
import android.content.res.Resources;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import java.util.List;

/**
 * Created by one 1 on 26-Jan-19.
 */

public class AdvertisementAdapter extends RecyclerView.Adapter<AdvertisementAdapter.AdvertisementViewHolder>  {
    private List<Advertisement> adsList;
    private AdvertisementListener listener;
    private Resources resources;

    interface AdvertisementListener {
        void OnAdClicked(int position, View view);
        void OnAdLongClicked(int position, View view);
        void OnOwnerClicked(String id, String name);
    }

    public void setListener(AdvertisementListener listener) {
        this.listener = listener;
    }

    public AdvertisementAdapter(List<Advertisement> adsList,  Resources res) {
        this.adsList = adsList;
        resources = res;
    }

    @Override
    public AdvertisementViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.ad_cell,parent,false);
        AdvertisementViewHolder advertisementViewHolder = new AdvertisementViewHolder(view);
        return advertisementViewHolder;
    }

    @Override
    public void onBindViewHolder(AdvertisementAdapter.AdvertisementViewHolder holder, int position) {
        Advertisement ad = adsList.get(position);
        holder.adOwnerNameTv.setText(ad.getAdPublisherName());
        holder.adNameTv.setText(ad.getAdName());
        holder.adTimePublishedTv.setText(ad.getAdTimePublished());
        holder.adPlaceTv.setText(ad.getAdLocation());

        //use StrBuilder instead!!!!!!!!!!!!
        String categories = resources.getString(R.string.swappable_on) + " " + ad.getCategoriesForSwap().get(0);
        if(ad.getCategoriesForSwap().size() > 1){
            categories += " ," + ad.getCategoriesForSwap().get(1);
        }
        if(ad.getCategoriesForSwap().size() > 2){
            categories += " " + resources.getString(R.string.and) + " " + (ad.getCategoriesForSwap().size() - 2) + " " + resources.getString(R.string.others);
        }
        holder.categoriesTv.setText(categories);

        if(ad.getAdPhotosList() != null && ad.getAdPhotosList().size() >= 1)
        {
            Picasso.get().load(ad.getAdPhotosList().get(0)).resize(500, 500).into(holder.adPhotoIv);
            holder.adPhotoIv.setScaleType(ImageView.ScaleType.CENTER_CROP);
        }

        if(ad.getPublisherPhotoUrl() != null && !ad.getPublisherPhotoUrl().isEmpty()){
            Picasso.get().load(ad.getPublisherPhotoUrl()).transform(new CircleTransform()).into(holder.adOwnerPhotoIv);
            holder.adOwnerPhotoIv.setScaleType(ImageView.ScaleType.CENTER_CROP);
        } else {
            holder.adOwnerPhotoIv.setImageDrawable(ContextCompat.getDrawable(holder.adOwnerPhotoIv.getContext(), R.drawable.userpic));
            holder.adOwnerPhotoIv.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        }
    }

    @Override
    public int getItemCount() {
        return adsList.size();
    }

    public class AdvertisementViewHolder extends RecyclerView.ViewHolder {
        ImageView adOwnerPhotoIv;
        TextView adOwnerNameTv;
        TextView adTimePublishedTv;
        TextView adPlaceTv;
        TextView adNameTv;
        ImageView adPhotoIv;
        TextView categoriesTv;

        public AdvertisementViewHolder(View itemView) {
            super(itemView);

            adOwnerPhotoIv = itemView.findViewById(R.id.ad_owner_iv);
            adOwnerNameTv = itemView.findViewById(R.id.ad_owner_tv);
            adPhotoIv = itemView.findViewById(R.id.ad_photo_iv);
            adNameTv = itemView.findViewById(R.id.name_ad_tv);
            adPlaceTv = itemView.findViewById(R.id.location_ad_tv);
            adTimePublishedTv = itemView.findViewById(R.id.ad_time_published_tv);
            categoriesTv = itemView.findViewById(R.id.categories_to_swap_tv_cell);

            adOwnerPhotoIv.setOnClickListener(new MyItemClickedListener());
            adOwnerNameTv.setOnClickListener(new MyItemClickedListener());

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(listener != null)
                    {
                        listener.OnAdClicked(getAdapterPosition(), view);
                    }
                }
            });

            itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    if(listener != null)
                    {
                        listener.OnAdLongClicked(getAdapterPosition(), view);;
                    }
                    return true;
                }
            });
        }

        private class MyItemClickedListener implements View.OnClickListener{
            @Override
            public void onClick(View view) {
                if(listener != null)
                {
                    listener.OnOwnerClicked(adsList.get(getAdapterPosition()).getAdPublisherId(), adsList.get(getAdapterPosition()).getAdPublisherName());
                }
            }
        }
    }
}
