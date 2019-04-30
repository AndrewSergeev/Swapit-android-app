package il.co.freebie.swapit;

import android.app.Fragment;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;
import java.util.Locale;

/**
 * Created by one 1 on 11-Feb-19.
 */

public class AnotherUserFragment extends Fragment {
    private static List<Advertisement> advertisementList;
    private static AdvertisementAdapter adapter;
    private static String userName;
    private static User user;

    interface OnAnotherUserFragmentListener{
        void onAnotherUserAdClicked(Advertisement ad);
    }

    private OnAnotherUserFragmentListener callback;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        try {
            callback = (AnotherUserFragment.OnAnotherUserFragmentListener) context;
        } catch (ClassCastException ex) {
            throw new ClassCastException("The activity must implement OnAnotherUserFragmentListener interface");
        }
    }

    public static AnotherUserFragment newInstance(AdvertisementAdapter adAdapter, List<Advertisement> adsList,
                                                  User selectedUser, String username) {
        AnotherUserFragment anotherUserFragment = new AnotherUserFragment();

        advertisementList = adsList;
        adapter = adAdapter;
        userName = username;
        user = selectedUser;

        return anotherUserFragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.curr_user_fragment,container,false);

        Toolbar toolbar = view.findViewById(R.id.toolbar);
        toolbar.setVisibility(View.VISIBLE);
        if(TextUtils.getLayoutDirectionFromLocale(Locale.getDefault()) != ViewCompat.LAYOUT_DIRECTION_RTL){
            toolbar.setNavigationIcon(R.drawable.leftarrow);
        } else {
            toolbar.setNavigationIcon(R.drawable.rightarrow);
        }
        TextView toolbarTv = view.findViewById(R.id.toolbar_tv);
        toolbarTv.setText("");
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().onBackPressed();
            }
        });

        RecyclerView recyclerView = view.findViewById(R.id.recycler_in_user_fragment);
        ImageView userPhotoIv = view.findViewById(R.id.curr_user_photo_iv);
        TextView livesInTv = view.findViewById(R.id.lives_in_tv);
        TextView interestedInTv = view.findViewById(R.id.user_interested_in_tv);
        TextView usernameTv = view.findViewById(R.id.user_name_tv);

        view.findViewById(R.id.edit_user_info_ib).setVisibility(View.GONE);

        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        layoutManager.setReverseLayout(true);
        layoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(layoutManager);
        adapter.setListener(new AdvertisementAdapter.AdvertisementListener() {
            @Override
            public void OnAdClicked(int position, View view) {
                callback.onAnotherUserAdClicked(advertisementList.get(position));
            }

            @Override
            public void OnAdLongClicked(int position, View view) {
                //nothing happens, no need this action here
            }

            @Override
            public void OnOwnerClicked(String id, String name) {
                //nothing happens, no need this action here
            }
        });

        recyclerView.setAdapter(adapter);
        recyclerView.setFocusable(false);//for nestedScrollView

        //STRBUILDER instead!!!!
        String categories = "";
        for(String categoryStr : user.getInterestedInCategoriesList()){
            categories += categoryStr + ", ";
        }
        final String categoriesStr = categories.isEmpty() ? getResources().getString(R.string.not_specified)
                : getResources().getString(R.string.user_interested_in) + " " + categories.substring(0, categories.length() - 2);
        final String livesInStr = user.getHometown() == null || user.getHometown().isEmpty() ? getResources().getString(R.string.not_specified)
                : getResources().getString(R.string.lives_in) + " " + user.getHometown();

        if(user.getImageUrl() != null && !user.getImageUrl().isEmpty()){
            ImageFileWorkingHelper.fillCircledIvWithPicasso(Uri.parse(user.getImageUrl()), userPhotoIv);
        }

        livesInTv.setText(livesInStr);
        interestedInTv.setText(categoriesStr);
        usernameTv.setText(userName);

        return view;
    }
}
