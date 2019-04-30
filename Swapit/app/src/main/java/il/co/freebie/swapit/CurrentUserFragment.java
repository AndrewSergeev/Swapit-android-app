package il.co.freebie.swapit;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Shader;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import static android.app.Activity.RESULT_OK;

/**
 * Created by one 1 on 26-Jan-19.
 */

public class CurrentUserFragment extends Fragment {
    private int CAMERA_REQUEST_CODE = 1;
    private int GALLERY_REQUEST_CODE = 2;
    private int READ_EXTERNAL_STORAGE_REQUEST = 1;
    private int WRITE_PERMISSION_REQUEST = 2;

    private ActionMode mActionMode;
    private static List<Advertisement> advertisementList;
    private static AdvertisementAdapter adapter;
    private static String userName;
    private static User currUser;

    private String currImageUrl;//for captured photo

    ImageView userPhotoIv;

    interface OnCurrentUserFragmentListener{
        void onUserAdClicked(Advertisement ad);
        void onUserInfoEditCompleted(String placeOfLiving, List<String> categoriesInterestedIn);
        void onUserPhotoChanged(Uri uri);
        void onUserItemSelectedForRemove(Advertisement ad, int position);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        try {
            callback = (OnCurrentUserFragmentListener) context;
        }catch (ClassCastException ex) {
            throw new ClassCastException("The activity must implement OnCurrentUserFragmentListener interface");
        }
    }

    private OnCurrentUserFragmentListener callback;

    public static CurrentUserFragment newInstance(AdvertisementAdapter adAdapter, List<Advertisement> adsList,
                                                 User user, String username) {
        CurrentUserFragment currentUserFragment = new CurrentUserFragment();

        advertisementList = adsList;
        adapter = adAdapter;
        userName = username;
        currUser = user;

        return currentUserFragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.curr_user_fragment,container,false);

        RecyclerView recyclerView = view.findViewById(R.id.recycler_in_user_fragment);
        userPhotoIv = view.findViewById(R.id.curr_user_photo_iv);
        ImageButton editUserInfoBtn = view.findViewById(R.id.edit_user_info_ib);
        final TextView livesInTv = view.findViewById(R.id.lives_in_tv);
        final TextView interestedInTv = view.findViewById(R.id.user_interested_in_tv);
        TextView usernameTv = view.findViewById(R.id.user_name_tv);


        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        layoutManager.setReverseLayout(true);
        layoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(layoutManager);
        adapter.setListener(new AdvertisementAdapter.AdvertisementListener() {
            @Override
            public void OnAdClicked(int position, View view) {
                if(mActionMode != null)
                    mActionMode.finish();
                else
                    callback.onUserAdClicked(advertisementList.get(position));
            }

            @Override
            public void OnAdLongClicked(final int position, final View view) {
                view.startActionMode(new ActionMode.Callback() {
                    @Override
                    public boolean onCreateActionMode(ActionMode actionMode, Menu menu) {
                        getActivity().getMenuInflater().inflate(R.menu.context_menu_action_mode,menu);
                        actionMode.setTitle(advertisementList.get(position).getAdName());
                        mActionMode = actionMode;

                        view.animate().alpha(0.5f).setDuration(500).start();

                        return true;
                    }

                    @Override
                    public boolean onPrepareActionMode(ActionMode actionMode, Menu menu) {
                        return false;
                    }

                    @Override
                    public boolean onActionItemClicked(final ActionMode actionMode, MenuItem menuItem) {
                        if (menuItem.getItemId() == R.id.item_remove_ad) {
                            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                            builder.setMessage(getResources().getString(R.string.delete_ad_question) + " '" + advertisementList.get(position).getAdName() + "'?")
                                    .setPositiveButton(getResources().getString(R.string.delete), new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            callback.onUserItemSelectedForRemove(advertisementList.get(position), position);
                                            actionMode.finish();
                                        }
                                    }).setNegativeButton(getResources().getString(R.string.cancel), null).show();
                        }

                        return true;
                    }

                    @Override
                    public void onDestroyActionMode(ActionMode actionMode) {
                        view.animate().alpha(1f).setDuration(500).start();
                        mActionMode = null;
                    }
                });
            }

            @Override
            public void OnOwnerClicked(String id, String name) {
                //no need implementing
            }
        });
        recyclerView.setAdapter(adapter);
        recyclerView.setFocusable(false);

        //STRBUILDER instead!!!!
        String categories = "";
        for(String categoryStr : currUser.getInterestedInCategoriesList()){
            categories += categoryStr + ", ";
        }
        final String categoriesStr = categories.isEmpty() ? getResources().getString(R.string.not_specified)
                : getResources().getString(R.string.user_interested_in) + " " + categories.substring(0, categories.length() - 2);
        final String livesInStr = currUser.getHometown() == null || currUser.getHometown().isEmpty() ? getResources().getString(R.string.not_specified)
                : getResources().getString(R.string.lives_in) + " " + currUser.getHometown();

        if(currUser.getImageUrl() != null && !currUser.getImageUrl().isEmpty()){
            ImageFileWorkingHelper.fillCircledIvWithPicasso(Uri.parse(currUser.getImageUrl()), userPhotoIv);
        }
        livesInTv.setText(livesInStr);
        interestedInTv.setText(categoriesStr);

        final String categoriesNoComma = categories;
        editUserInfoBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(mActionMode != null)
                    mActionMode.finish();
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                LayoutInflater inflater = getLayoutInflater();
                View dialogView  = inflater.inflate(R.layout.edit_user_info_dlg,null);

                final EditText livesInEt = dialogView.findViewById(R.id.lives_in_input_edit);
                final EditText interestedInEt =  dialogView.findViewById(R.id.interested_in_input_edit);

                livesInEt.setText(currUser.getHometown());
                String categories = currUser.getInterestedInCategoriesList().size() == 0 ? "" : categoriesNoComma;
                interestedInEt.setText(categories);

                final List<String> categoriesToSwapList = currUser.getInterestedInCategoriesList();
                interestedInEt.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        categoriesToSwapList.clear();
                        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                        builder.setMultiChoiceItems(R.array.categories,
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
                                        str += category + ", ";//USE STRINGBUILDER INSTEAD
                                    }

                                    str = str.substring(0, str.length() - 2);
                                    interestedInEt.setText(str);
                                }
                            }
                        }).show();
                    }
                });
                builder.setView(dialogView).setPositiveButton(getResources().getString(R.string.save), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        String livesIn =  livesInEt.getText().toString();
                        if (livesIn.isEmpty()) {
                            livesInTv.setText(getResources().getString(R.string.not_specified));
                        } else {
                            livesInTv.setText(getResources().getString(R.string.lives_in) + " " + livesIn);
                        }

                        String interestedIn = interestedInEt.getText().toString();
                        if(interestedIn.isEmpty()){
                            interestedInTv.setText(getResources().getString(R.string.not_specified));
                        } else {
                            interestedInTv.setText(getResources().getString(R.string.user_interested_in) + " " + interestedIn);
                        }

                        currUser.setHometown(livesIn);
                        currUser.setInterestedInCategoriesList(categoriesToSwapList);
                        callback.onUserInfoEditCompleted(livesIn, categoriesToSwapList);
                    }
                }).setNegativeButton(getResources().getString(R.string.cancel),null).show();
            }
        });

        userPhotoIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(mActionMode != null)
                    mActionMode.finish();
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
                            int hasWritePermission = getActivity().checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE);
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
                            int hasReadGalleryPermission = getActivity().checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE);
                            if(hasReadGalleryPermission != PackageManager.PERMISSION_GRANTED) {

                                requestPermissions(new String[] {Manifest.permission.READ_EXTERNAL_STORAGE},READ_EXTERNAL_STORAGE_REQUEST);
                            }
                            else pickPictureFromGallery();
                        }
                        else pickPictureFromGallery();
                    }
                });

                dialog.show();
            }
        });

        usernameTv.setText(userName);

        return view;
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
        if(resultCode == RESULT_OK){
            boolean validSize = true;
            Uri imageUri = null;

            if(requestCode == CAMERA_REQUEST_CODE){
                imageUri = Uri.parse(currImageUrl);
            }
            else if(requestCode == GALLERY_REQUEST_CODE){
                    imageUri = data.getData();
                    validSize = ImageFileWorkingHelper.validateFileSize(getActivity(), imageUri, 1500000);
            }

            if (validSize && imageUri != null) {
                ImageFileWorkingHelper.fillCircledIvWithPicasso(imageUri, userPhotoIv);
                callback.onUserPhotoChanged(imageUri);
            } else {
                Toast.makeText(getActivity(), getActivity().getResources().getString(R.string.photo_size), Toast.LENGTH_LONG).show();
            }
        }
    }
}
