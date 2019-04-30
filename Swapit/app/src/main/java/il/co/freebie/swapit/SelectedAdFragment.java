package il.co.freebie.swapit;

import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;
import java.util.Locale;

/**
 * Created by one 1 on 10-Feb-19.
 */

public class SelectedAdFragment extends Fragment {
    private static Advertisement advertisement;
    private static boolean showButtons; //show buttons only on not current user's selected ad
    private static List<Advertisement> adsList;//curr users list

    interface OnSelectedAdFragmentListener{
        void onOfferDealClicked(Advertisement offeredAd, String publisherId, String publisherName);//(offered ad from me, the id of the advertisement owner that interested me, his name)
        void onStartConversationClicked(String publisherId, String publisherName);
        void onOwnerPhotoClicked(String piblisherId, String publisherName);
    }

    private OnSelectedAdFragmentListener callback;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        try {
            callback = (SelectedAdFragment.OnSelectedAdFragmentListener) context;
        } catch (ClassCastException ex) {
            throw new ClassCastException("The activity must implement OnSelectedAdFragmentListener interface");
        }
    }

    public static SelectedAdFragment newInstance(Advertisement ad, boolean currUsersAd, List<Advertisement> list){
        SelectedAdFragment fragment = new SelectedAdFragment();

        advertisement = ad;
        showButtons = !currUsersAd;
        adsList = list;

        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.selected_ad_fragment,container,false);

        Toolbar toolbar = view.findViewById(R.id.toolbar);
        if(TextUtils.getLayoutDirectionFromLocale(Locale.getDefault()) != ViewCompat.LAYOUT_DIRECTION_RTL){
            toolbar.setNavigationIcon(R.drawable.leftarrow);
        } else {
            toolbar.setNavigationIcon(R.drawable.rightarrow);
        }
        TextView toolbarTv = view.findViewById(R.id.toolbar_tv);
        toolbarTv.setText(advertisement.getAdName());
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().onBackPressed();
            }
        });

        Button offerDealBtn = view.findViewById(R.id.offer_deal_btn);
        Button startConversationBtn = view.findViewById(R.id.start_conv_btn);
        ViewPager viewPager = (ViewPager) view.findViewById(R.id.iv_pager);
        final TextView imgCounterTv = view.findViewById(R.id.image_counter_tv);
        TextView adNameTv = view.findViewById(R.id.selected_item_name_tv);
        TextView adDescriptionTv = view.findViewById(R.id.selected_item_description_tv);
        TextView adCategoriesTv = view.findViewById(R.id.selected_item_categories_tv);
        TextView adTimeTv = view.findViewById(R.id.selected_item_time_tv);
        TextView adPlaceTv = view.findViewById(R.id.selected_item_place_tv);
        ImageView adOwnerIv = view.findViewById(R.id.selected_ad_owner_iv);

        if(!showButtons){
            offerDealBtn.setVisibility(View.GONE);
            startConversationBtn.setVisibility(View.GONE);
        }

        String str = " ";
        for (String category : advertisement.getCategoriesForSwap()) {
            str += category + ", ";//USE STRINGBUILDER INSTEAD
        }
        str = str.substring(0, str.length() - 2);

        imgCounterTv.setText(1 + "/" + advertisement.getAdPhotosList().size());
        adNameTv.setText(advertisement.getAdName());
        adDescriptionTv.setText(advertisement.getAdDescription());
        adCategoriesTv.setText(getResources().getString(R.string.swappable_on) + str);
        adTimeTv.setText(advertisement.getAdTimePublished());
        adPlaceTv.setText(advertisement.getAdLocation());
        if(advertisement.getPublisherPhotoUrl() != null){
            ImageFileWorkingHelper.fillCircledIvWithPicasso(Uri.parse(advertisement.getPublisherPhotoUrl()), adOwnerIv);
        }

        ImageSlidingAdapter imageAdapter = new ImageSlidingAdapter(getActivity(), advertisement.getAdPhotosList());
        viewPager.setAdapter(imageAdapter);
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                imgCounterTv.setText((position+1) + "/" + advertisement.getAdPhotosList().size());
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        if(showButtons){
            offerDealBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                    LayoutInflater inflater = getActivity().getLayoutInflater();
                    View dialogView  = inflater.inflate(R.layout.my_ads_dlg,null);
                    builder.setView(dialogView);
                    builder.setTitle(getResources().getString(R.string.choose_ad));

                    RecyclerView recyclerView = dialogView.findViewById(R.id.recycler_in_items_dlg);
                    recyclerView.setLayoutManager(new GridLayoutManager(getActivity(),2));
                    AdSquareAdapter adSquareAdapter = new AdSquareAdapter(adsList);
                    final AlertDialog dialog = builder.create();
                    adSquareAdapter.setListener(new AdSquareAdapter.AdSquareListener() {
                        @Override
                        public void OnSquaredAdClicked(final int position, View view) {
                            boolean verified = match(advertisement.getCategoriesForSwap(), adsList.get(position).getAdCategory());
                            if(!verified)
                            {
                                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                                builder.setTitle(getResources().getString(R.string.cayegory_not_matched))
                                        .setMessage(getResources().getString(R.string.still_offer))
                                        .setPositiveButton(getResources().getString(R.string.yes), new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialogInterface, int i) {
                                                dialog.dismiss();
                                                if(callback != null){
                                                    callback.onOfferDealClicked(adsList.get(position), advertisement.getAdPublisherId(), advertisement.getAdPublisherName());
                                                }
                                            }
                                        }).setNegativeButton(getResources().getString(R.string.no_choose_another), null).show();
                            }
                            else
                            {
                                dialog.dismiss();
                                if(callback != null){
                                    callback.onOfferDealClicked(adsList.get(position), advertisement.getAdPublisherId(), advertisement.getAdPublisherName());
                                }
                            }
                        }
                    });
                    recyclerView.setAdapter(adSquareAdapter);
                    dialog.show();
                }
            });
            startConversationBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(callback != null){
                        callback.onStartConversationClicked(advertisement.getAdPublisherId(), advertisement.getAdPublisherName());
                    }
                }
            });
        }

        adOwnerIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(callback != null){
                    callback.onOwnerPhotoClicked(advertisement.getAdPublisherId(), advertisement.getAdPublisherName());
                }
            }
        });

        return view;
    }

    private boolean match(List<String> categories, String category){
        boolean matched = false;
        for(String ctgry : categories){
            if(ctgry.equals(category)){//!!!check if will work with hebrew
                matched = true;
                break;
            }
        }

        return matched;
    }
}
