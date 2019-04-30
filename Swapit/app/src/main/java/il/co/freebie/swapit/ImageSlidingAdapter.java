package il.co.freebie.swapit;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.List;

/**
 * Created by one 1 on 11-Feb-19.
 */

public class ImageSlidingAdapter extends PagerAdapter {
    private Context context;
    private List<String> imgUrlList;

    LayoutInflater layoutInflater;

    ImageSlidingAdapter(Context context, List<String> urlsList){
        this.context=context;
        imgUrlList = urlsList;
        layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return imgUrlList.size();
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == ((LinearLayout) object);
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        View itemView = layoutInflater.inflate(R.layout.pager_image_item, container, false);
        ImageView imageView = (ImageView) itemView.findViewById(R.id.imageView);
        Picasso.get().load(imgUrlList.get(position)).into(imageView);
        container.addView(itemView);

        return itemView;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((LinearLayout)object);
    }
}
