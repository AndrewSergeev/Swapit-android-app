package il.co.freebie.swapit;

import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;

import java.io.IOException;

/**
 * Created by one 1 on 26-Jan-19.
 */

public class SearcherFragment extends Fragment {
    private final static int LOCATION_PERMISSION_REQUEST = 1;
    private FusedLocationProviderClient client;
    private EditText cityEt;
    private String selectedCategory = "";

    interface OnSearchFragmentListener{
        void OnFindAdsClicked(String category, String city, boolean categoryMatching, String freeText);
    }

    private OnSearchFragmentListener callBack;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            callBack = (OnSearchFragmentListener)context;
        }catch (ClassCastException ex) {
            throw new ClassCastException("The activity must implement OnSearchFragmentListener interface");
        }
    }


    public static SearcherFragment newInstance() {
        SearcherFragment searcherFragment = new SearcherFragment();

        return searcherFragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.search_fragment,container,false);
        final Button selectCategoryBtn = view.findViewById(R.id.select_category_btn);
        cityEt = view.findViewById(R.id.new_ad_city_text_input);
        ImageButton locationImgBtn = view.findViewById(R.id.location_new_ad_btn);
        LinearLayout cbHolderLayout = view.findViewById(R.id.cb_layout);
        final CheckBox checkBox = view.findViewById(R.id.request_matches_cb);
        final EditText freeTextEt = view.findViewById(R.id.new_ad_free_text_input);
        Button findBtn = view.findViewById(R.id.find_ads_btn);

        selectCategoryBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setTitle(getResources().getString(R.string.choose_category)).setSingleChoiceItems(R.array.categories, 2, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        selectedCategory =  getResources().getStringArray(R.array.categories)[i];
                        selectCategoryBtn.setText(selectedCategory);
                        selectCategoryBtn.setTextColor(Color.BLACK);
                        dialogInterface.dismiss();
                    }
                }).show();
            }
        });

        cbHolderLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                checkBox.setChecked(!checkBox.isChecked());
            }
        });

        locationImgBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(Build.VERSION.SDK_INT>=23) {
                    int hasLocationPermission = getActivity().checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION);
                    if(hasLocationPermission!= PackageManager.PERMISSION_GRANTED) {
                        requestPermissions(new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},LOCATION_PERMISSION_REQUEST);
                    }
                    else startLocation();
                }
                else startLocation();
            }
        });

        findBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String category = selectedCategory;
                String city = cityEt.getText().toString();
                boolean categoryMatching = checkBox.isChecked();
                String freeText = freeTextEt.getText().toString();
                if(category.isEmpty() && city.isEmpty() && freeText.isEmpty()){
                    Toast.makeText(getActivity(), getResources().getString(R.string.at_least_one_field), Toast.LENGTH_SHORT).show();
                }
                else{
                    callBack.OnFindAdsClicked(category, city.toLowerCase(), categoryMatching, freeText.toLowerCase());
                }
            }
        });

        return view;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == LOCATION_PERMISSION_REQUEST) {
            if(grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(getActivity(), getResources().getString(R.string.permission_denied), Toast.LENGTH_SHORT).show();
            }
            else startLocation();
        }
    }

    private void startLocation() {

        client = LocationServices.getFusedLocationProviderClient(getActivity());

        LocationCallback callback = new LocationCallback() {

            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);

                Location lastLocation = locationResult.getLastLocation();
                Geocoder geocoder = new Geocoder(getActivity());
                try {
                    Address address = geocoder.getFromLocation(lastLocation.getLatitude(), lastLocation.getLongitude(), 1).get(0);
                    String city = address.getLocality();
                    if(cityEt != null){
                        cityEt.setText(city);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        };

        LocationRequest request = LocationRequest.create();
        request.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        request.setNumUpdates(1);

        if(Build.VERSION.SDK_INT>=23 && getActivity().checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)
            client.requestLocationUpdates(request,callback,null);
        else if(Build.VERSION.SDK_INT<=22)
            client.requestLocationUpdates(request,callback,null);
    }
}
