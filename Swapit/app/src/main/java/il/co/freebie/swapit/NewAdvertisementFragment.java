package il.co.freebie.swapit;

import android.*;
import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputEditText;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import static android.app.Activity.RESULT_OK;

/**
 * Created by one 1 on 26-Jan-19.
 */

public class NewAdvertisementFragment extends Fragment {

    private int CAMERA_REQUEST_CODE = 1;
    private int GALLERY_REQUEST_CODE = 2;
    private int READ_EXTERNAL_STORAGE_REQUEST = 1;
    private int WRITE_PERMISSION_REQUEST = 2;

    private static String usersCityLocation;
    private static String usersName;

    private String selectedCategory, name, description, city;
    private List<String> categoriesToSwapList = new ArrayList<>();

    private String[] photoUrlArr = new String[4];
    private int indexOfurlArr = 0;

    private ImageView selectedAdPhotoIv;//this for know on which from 4 iv's clicked, findViewById inside MyAdPhotoBtnListener
    private String currImageUrl;

    interface OnCreatedAdListener {
        void onNewAdCreated(Advertisement ad, List<String> urlsList);
    }

    private OnCreatedAdListener callBack;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            callBack = (OnCreatedAdListener)context;
        }catch (ClassCastException ex) {
            throw new ClassCastException("The activity must implement OnCreatedAdListener interface");
        }
    }

    public static NewAdvertisementFragment newInstance(String usersCity, String username) {
        NewAdvertisementFragment newAdvertisementFragment = new NewAdvertisementFragment();
        usersCityLocation = usersCity;
        usersName = username;

        return newAdvertisementFragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.new_ad_fragment,container,false);
        final Button selectCategoryBtn = view.findViewById(R.id.select_category_btn);
        final EditText adNameEt = view.findViewById(R.id.new_ad_name_text_input);
        final EditText adDescriptionEt = view.findViewById(R.id.new_ad_description_text_input);
        final EditText adCategoriesForSwappingEt = view.findViewById(R.id.new_ad_to_next_categories_text_input);
        final ImageView imgBtn1 = view.findViewById(R.id.new_ad_img1);
        final ImageView imgBtn2 = view.findViewById(R.id.new_ad_img2);
        final ImageView imgBtn3 = view.findViewById(R.id.new_ad_img3);
        final ImageView imgBtn4 = view.findViewById(R.id.new_ad_img4);
        final EditText adCityEt = view.findViewById(R.id.new_ad_city_text_input);
        ImageButton locationImgBtn = view.findViewById(R.id.location_new_ad_btn);
        Button publishBtn = view.findViewById(R.id.publish_ad_btn);

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

        adCategoriesForSwappingEt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                categoriesToSwapList.clear();
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setTitle(getResources().getString(R.string.select_catg_for_swapping))
                        .setMultiChoiceItems(R.array.categories,
                                null, new DialogInterface.OnMultiChoiceClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i, boolean b) {
                                        String[] categoriesStr = getResources().getStringArray(R.array.categories);
                                        if (b)
                                            categoriesToSwapList.add(categoriesStr[i]);
                                        else
                                            categoriesToSwapList.remove(categoriesStr[i]);
                                    }
                                }).setPositiveButton(R.string.save, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if (!categoriesToSwapList.isEmpty()) {
                            String str = "";
                            for (String category : categoriesToSwapList) {
                                str += category + ",";//USE STRINGBUILDER INSTEAD
                            }

                            str = str.substring(0, str.length() - 1);
                            adCategoriesForSwappingEt.setText(str);
                        }
                    }
                }).show();
            }
        });

        locationImgBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!usersCityLocation.isEmpty()){
                    adCityEt.setText(usersCityLocation);
                }
                else {
                    Toast.makeText(getActivity(), getResources().getString(R.string.no_hometown), Toast.LENGTH_SHORT).show();
                }
            }
        });


        publishBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                name = adNameEt.getText().toString();
                description = adDescriptionEt.getText().toString();
                city = adCityEt.getText().toString();

                boolean validInput = checkIfAllFieldAreFilled();
                boolean atLeastOnePhoto = checkPhotoNumber();

                if(!validInput)
                {
                    Toast.makeText(getActivity(), getResources().getString(R.string.fields_are_not_filled), Toast.LENGTH_SHORT).show();
                }
                else if(!atLeastOnePhoto)
                {
                    Toast.makeText(getActivity(), getResources().getString(R.string.at_least_one_photo), Toast.LENGTH_SHORT).show();
                }
                else
                {
                    Advertisement advertisement = new Advertisement();
                    advertisement.setAdName(name);
                    advertisement.setAdPublisherName(usersName);
                    advertisement.setAdCategory(selectedCategory);
                    advertisement.setAdDescription(description);
                    advertisement.setAdLocation(city);
                    advertisement.setAdTimePublished(new SimpleDateFormat("dd-MM-yyyy").format(Calendar.getInstance().getTime()));
                    for(String categoryStr : categoriesToSwapList){
                        advertisement.getCategoriesForSwap().add(categoryStr);
                    }

                    List<String> urlsList = new ArrayList<>();
                    for(int i = 0; i < photoUrlArr.length; i++){
                        if(photoUrlArr[i] != null && !photoUrlArr[i].isEmpty()){
                            urlsList.add(photoUrlArr[i]);
                        }
                    }

                    selectedCategory = "";
                    name = "";
                    description = "";
                    city = "";
                    categoriesToSwapList.clear();
                    photoUrlArr = new String[4];
                    indexOfurlArr = 0;
                    adNameEt.setText("");
                    selectCategoryBtn.setText(getResources().getString(R.string.select_category));
                    selectCategoryBtn.setTextColor(ContextCompat.getColor(getActivity(), R.color.grayicon));
                    adDescriptionEt.setText("");
                    adCategoriesForSwappingEt.setText("");
                    adCityEt.setText("");
                    callBack.onNewAdCreated(advertisement, urlsList);

                }
            }
        });

        imgBtn1.setOnClickListener(new MyAdPhotoBtnListener(view));
        imgBtn2.setOnClickListener(new MyAdPhotoBtnListener(view));
        imgBtn3.setOnClickListener(new MyAdPhotoBtnListener(view));
        imgBtn4.setOnClickListener(new MyAdPhotoBtnListener(view));

        return view;
    }

    //checks how many photos made (max num is 4)
    private boolean checkPhotoNumber(){
        boolean validInput = false;

        for(int i = 0; i < photoUrlArr.length; i++){
            if(photoUrlArr[i] != null && !photoUrlArr[i].isEmpty()){
                validInput = true;
                break;
            }
        }

        return validInput;
    }

    private boolean checkIfAllFieldAreFilled() {
        boolean validInput = true;

        if(name == null || name.isEmpty() || selectedCategory == null || selectedCategory.isEmpty() || description == null ||
                description.isEmpty() || city == null || city.isEmpty() || categoriesToSwapList.isEmpty())
        {
            validInput = false;
        }

        return validInput;
    }

    private class MyAdPhotoBtnListener implements View.OnClickListener{
        private View fragmentView;

        public MyAdPhotoBtnListener(View view) {
            fragmentView = view;
        }

        @Override
        public void onClick(View view) {
            //determination of selected iv
            selectedAdPhotoIv = fragmentView.findViewById(view.getId());
            switch (view.getId()){
                case R.id.new_ad_img1:
                    indexOfurlArr = 0;
                    break;
                case R.id.new_ad_img2:
                    indexOfurlArr = 1;
                    break;
                case R.id.new_ad_img3:
                    indexOfurlArr = 2;
                    break;
                case R.id.new_ad_img4:
                    indexOfurlArr = 3;
                    break;
            }

            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            LayoutInflater inflater = getLayoutInflater();
            View dialogView  = inflater.inflate(R.layout.set_profile_pic_dlg,null);
            builder.setView(dialogView);

            Button takeNewPicBtn = dialogView.findViewById(R.id.take_new_profile_photo_btn);
            Button takePicFromGalleryBtn = dialogView.findViewById(R.id.take_profile_photo_from_galery_btn);

            final AlertDialog dialog = builder.create();
            takeNewPicBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    dialog.dismiss();
                    if(Build.VERSION.SDK_INT>=23) {
                        int hasWritePermission = getActivity().checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE);
                        if(hasWritePermission!= PackageManager.PERMISSION_GRANTED) {
                            requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},WRITE_PERMISSION_REQUEST);
                        }
                        else takePhotoUsingCamera();
                    }
                    else takePhotoUsingCamera();                    
                }
            });

            takePicFromGalleryBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    dialog.dismiss();
                    if(Build.VERSION.SDK_INT>=23) {
                        int hasReadGalleryPermission = getActivity().checkSelfPermission(android.Manifest.permission.READ_EXTERNAL_STORAGE);
                        if(hasReadGalleryPermission != PackageManager.PERMISSION_GRANTED) {
                            requestPermissions(new String[] {android.Manifest.permission.READ_EXTERNAL_STORAGE},READ_EXTERNAL_STORAGE_REQUEST);
                        }
                        else pickPictureFromGallery();
                    }
                    else pickPictureFromGallery();
                }
            });

            dialog.show();
        }
    }

    private void takePhotoUsingCamera(){
        File photoFile = null;
        try {
            photoFile = ImageFileWorkingHelper.createImageFile();
            currImageUrl = "file:" + photoFile.getAbsolutePath();
            ImageFileWorkingHelper.takePictureForFragment(this, photoFile, CAMERA_REQUEST_CODE);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void pickPictureFromGallery(){
        ImageFileWorkingHelper.picPictureForFragment(this, GALLERY_REQUEST_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == READ_EXTERNAL_STORAGE_REQUEST) {
            if(grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(getActivity(), getActivity().getResources().getString(R.string.permission_denied), Toast.LENGTH_LONG).show();
            }
            else pickPictureFromGallery();
        }
        else if(requestCode == WRITE_PERMISSION_REQUEST) {
            if(grantResults[0]!=PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(getActivity(), getActivity().getResources().getString(R.string.permission_denied), Toast.LENGTH_LONG).show();
            }
            else
                takePhotoUsingCamera();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == RESULT_OK)
        {
            boolean validSize = true;
            Uri imageUri = null;
            if (requestCode == CAMERA_REQUEST_CODE) {
                imageUri = Uri.parse(currImageUrl);
            }
            else if (requestCode == GALLERY_REQUEST_CODE) {
                imageUri = data.getData();
                validSize = ImageFileWorkingHelper.validateFileSize(getActivity(), imageUri, 1500000);
            }

            if (validSize && imageUri != null) {
                ImageFileWorkingHelper.fillImageView(getActivity(), imageUri, selectedAdPhotoIv);
                selectedAdPhotoIv.setScaleType(ImageView.ScaleType.FIT_XY);
                photoUrlArr[indexOfurlArr] = imageUri.toString();
            } else {
                Toast.makeText(getActivity(), getActivity().getResources().getString(R.string.photo_size), Toast.LENGTH_LONG).show();
            }
        }
    }
/////////////////////////////////////////////////////////////////////////////////////////////////////////
}
