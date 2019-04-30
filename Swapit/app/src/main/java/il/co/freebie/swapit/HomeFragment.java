package il.co.freebie.swapit;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

/**
 * Created by one 1 on 26-Jan-19.
 */

public class HomeFragment extends Fragment {
    private static List<Advertisement> advertisementList;
    private static AdvertisementAdapter adapter;

    interface OnHomeFragmentListener{
        void onClicked(Advertisement ad);
        void onOwnerClicked(String id, String name);
    }

    private OnHomeFragmentListener callBack;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        try {
            callBack = (OnHomeFragmentListener) context;
        }catch (ClassCastException ex) {
            throw new ClassCastException("The activity must implement OnHomeFragmentListener interface");
        }
    }

    public static HomeFragment newInstance(AdvertisementAdapter adAdapter, List<Advertisement> adsList) {
        HomeFragment homeFragment = new HomeFragment();
        advertisementList = adsList;
        adapter = adAdapter;

        return homeFragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.home_fragment,container,false);

        RecyclerView recyclerView = rootView.findViewById(R.id.recycler);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        layoutManager.setReverseLayout(true);
        layoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(layoutManager);

        adapter.setListener(new AdvertisementAdapter.AdvertisementListener() {
            @Override
            public void OnAdClicked(int position, View view) {
                callBack.onClicked(advertisementList.get(position));
            }

            @Override
            public void OnAdLongClicked(int position, View view) {

            }

            @Override
            public void OnOwnerClicked(String id, String name) {
                callBack.onOwnerClicked(id, name);
            }
        });

        recyclerView.setAdapter(adapter);

        return rootView;
    }
}
