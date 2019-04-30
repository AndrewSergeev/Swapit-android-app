package il.co.freebie.swapit;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity
        implements HomeFragment.OnHomeFragmentListener, NewAdvertisementFragment.OnCreatedAdListener,
        CurrentUserFragment.OnCurrentUserFragmentListener, SelectedAdFragment.OnSelectedAdFragmentListener,
        AnotherUserFragment.OnAnotherUserFragmentListener, SearcherFragment.OnSearchFragmentListener,
        ConversationFragment.OnConversationFragmentListener, MyConversationsFragment.OnMyConversationsFragmentListener{

    private final static int LOCATION_PERMISSION_REQUEST = 1;

    private final static int MAX_SIZE_OF_ADS_LIST = 100;

    public static final String CONVERSATIONS = "conversations";
    public static final String USERS = "users";
    public static final String EDIT_AD_FRAGMENT_TAG = "new_ad_fragment";
    public static final String SELECTED_AD_FRAGMENT_TAG = "selected_ad_fragment";
    public static final String SELECTED_USER_FRAGMENT_TAG = "selected_user_fragment";
    public static final String CONVERSATION_FRAGMENT_TAG = "conversation_fragment";

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private BottomNavigationView bottomNavigationView;
    private ViewPagerAdapter pagerAdapter;
    private ViewPager pager;
    private ImageButton sorterBtn;

    private FusedLocationProviderClient client;
    private AlarmManager alarmManager;

    private List<Advertisement> adsList = new ArrayList<>();
    private User currUser = new User();
    private List<Advertisement> currUserAdsList = new ArrayList<>();
    private List<Conversation> conversationsList = new ArrayList<>();

    private AdvertisementAdapter adAdapter;
    private AdvertisementAdapter userPageAdAdapter;
    private ConversationAdapter conversationAdapter;

    private String currUsername;
    private FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
    private FirebaseAuth.AuthStateListener authStateListener;
    private FirebaseDatabase database = FirebaseDatabase.getInstance();
    private DatabaseReference users = database.getReference(USERS);
    private StorageReference storageRef = FirebaseStorage.getInstance().getReference();
    private DatabaseReference conversations = database.getReference(CONVERSATIONS);
    private FirebaseMessaging messaging = FirebaseMessaging.getInstance();


    TextView userTv; //inside navigationView
    ImageView userIv; //inside navigationView

    private boolean authFlag = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeAsUpIndicator(R.drawable.menu);
        actionBar.setTitle("");

        alarmManager = (AlarmManager)getSystemService(ALARM_SERVICE);

        adAdapter = new AdvertisementAdapter(adsList, getResources());
        userPageAdAdapter = new AdvertisementAdapter(currUserAdsList, getResources());
        conversationAdapter = new ConversationAdapter(conversationsList);

        bottomNavigationView = (BottomNavigationView) findViewById(R.id.bottom_navigation);
        BottomNavigationViewHelper.disableShiftMode(bottomNavigationView);
        final View bottomNavBarBorder = findViewById(R.id.bottom_navbar_border_view);

        CoordinatorLayout.LayoutParams layoutParams = (CoordinatorLayout.LayoutParams) bottomNavigationView.getLayoutParams();
        layoutParams.setBehavior(new BottomNavigationViewBehavior(findViewById(R.id.bottom_navbar_border_view)));

        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.navigation_view);
        sorterBtn = findViewById(R.id.toolbar_action_item);

        navigationView.setNavigationItemSelectedListener(new MyNavigationViewItemListener());
        bottomNavigationView.setOnNavigationItemSelectedListener(new MyBottomNavigationViewListener());

        pagerAdapter = new ViewPagerAdapter(getSupportFragmentManager());
        pager = findViewById(R.id.pager);
        pager.setAdapter(pagerAdapter);
        pager.setCurrentItem(1000);
        pager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                //no need implementation
            }

            @Override
            public void onPageSelected(int position) {
                bottomNavigationView.getMenu().getItem(position%5).setChecked(true);
                int visibility = (position % 5 == 0 && firebaseAuth.getCurrentUser() != null) ? View.VISIBLE : View.GONE;
                sorterBtn.setVisibility(visibility);
            }

            @Override
            public void onPageScrollStateChanged(int state) {
                //no need implementation
            }
        });

        authStateListener = new FirebaseAuth.AuthStateListener() {
            @Override   //will be called on sign in, sign out or sign up
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                View headerView  = navigationView.getHeaderView(0);
                userTv = headerView.findViewById(R.id.navigation_header_text_view);
                userIv = headerView.findViewById(R.id.nav_header_iv);
                final FirebaseUser user = firebaseAuth.getCurrentUser();

                if(user != null){//for sign up and for sign in
                    if(currUsername != null){//sign up
                        user.updateProfile(new UserProfileChangeRequest.Builder().setDisplayName(currUsername).build()).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                currUsername = null;
                                if(task.isSuccessful()){
                                    Toast.makeText(MainActivity.this, user.getDisplayName(), Toast.LENGTH_SHORT).show();

                                    pager.setAdapter(pagerAdapter);
                                    pager.setCurrentItem(1000);

                                }
                            }
                        });
                    }

                    userTv.setText(user.getDisplayName());
                    navigationView.getMenu().findItem(R.id.item_sign_in).setVisible(false);
                    navigationView.getMenu().findItem(R.id.item_sign_up).setVisible(false);
                    navigationView.getMenu().findItem(R.id.item_sign_out).setVisible(true);
                    navigationView.getMenu().findItem(R.id.item_notif_on).setVisible(true);
                    navigationView.getMenu().findItem(R.id.item_notif_off).setVisible(true);

                    bottomNavigationView.setVisibility(View.VISIBLE);
                    bottomNavBarBorder.setBackgroundColor(ContextCompat.getColor(MainActivity.this, R.color.materialBlue));
                    sorterBtn.setVisibility(View.VISIBLE);

                    getUsersData();
                    showNewestAds();
                }
                else
                {
                    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
                    authFlag = true;
                    userTv.setText("");
                    bottomNavigationView.setVisibility(View.INVISIBLE);
                    bottomNavBarBorder.setBackgroundColor(Color.TRANSPARENT);
                    sorterBtn.setVisibility(View.INVISIBLE);
                    if(currUser.getConversationsKeysList() != null){
                        for(String key : currUser.getConversationsKeysList()){
                            messaging.unsubscribeFromTopic(key);
                        }
                    }

                    showNewestAds();
                    navigationView.getMenu().findItem(R.id.item_sign_in).setVisible(true);
                    navigationView.getMenu().findItem(R.id.item_sign_up).setVisible(true);
                    navigationView.getMenu().findItem(R.id.item_sign_out).setVisible(false);
                    navigationView.getMenu().findItem(R.id.item_notif_on).setVisible(false);
                    navigationView.getMenu().findItem(R.id.item_notif_off).setVisible(false);
                }
            }
        };

        //determinates the order of the shown ads
        sorterBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PopupMenu popupMenu = new PopupMenu(MainActivity.this,view);
                popupMenu.getMenuInflater().inflate(R.menu.sort_popup_menu,popupMenu.getMenu());
                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem menuItem) {
                        switch (menuItem.getItemId()){
                            case R.id.nearby_item:
                                if(Build.VERSION.SDK_INT>=23) {
                                    int hasLocationPermission = checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION);
                                    if(hasLocationPermission!= PackageManager.PERMISSION_GRANTED) {
                                        requestPermissions(new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},LOCATION_PERMISSION_REQUEST);
                                    }
                                    else startLocation();
                                }
                                else startLocation();
                                break;
                            case R.id.newest_item:
                                showNewestAds();
                                break;
                        }
                        return false;
                    }
                });
                popupMenu.show();
            }
        });
    }

    private void startLocation() {
        client = LocationServices.getFusedLocationProviderClient(this);
        LocationCallback callback = new LocationCallback() {

            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);

                Location lastLocation = locationResult.getLastLocation();
                Geocoder geocoder = new Geocoder(MainActivity.this);
                try {
                    Address address = geocoder.getFromLocation(lastLocation.getLatitude(), lastLocation.getLongitude(), 1).get(0);
                    String city = address.getLocality();
                    showNearbyAds(city);
                    Toast.makeText(MainActivity.this, city, Toast.LENGTH_SHORT).show();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        };

        LocationRequest request = LocationRequest.create();
        request.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        request.setNumUpdates(1);

        if(Build.VERSION.SDK_INT>=23 && checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)
            client.requestLocationUpdates(request,callback,null);
        else if(Build.VERSION.SDK_INT<=22)
            client.requestLocationUpdates(request,callback,null);
    }

    private void getUsersData(){
        final ProgressDialog progressDialog = new ProgressDialog(MainActivity.this);
        progressDialog.setMessage(getResources().getString(R.string.loading));
        progressDialog.show();

        //singleValEventListener means that will refresh data only once
        //THIS FOR GET ONLY CURRENT USER DATA
        users.child(firebaseAuth.getCurrentUser().getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                currUserAdsList.clear();
                currUser.getInterestedInCategoriesList().clear();
                if(dataSnapshot.exists()) {
                    currUser.setImageUrl(dataSnapshot.child("imageUrl").getValue(String.class));
                    if(currUser.getImageUrl() != null){
                        ImageFileWorkingHelper.fillCircledIvWithPicasso(Uri.parse(currUser.getImageUrl()), userIv);
                    }

                    for(DataSnapshot snapshot : dataSnapshot.child("adslist").getChildren()) {
                        Advertisement advertisement = snapshot.getValue(Advertisement.class);
                        currUserAdsList.add(advertisement);
                    }

                    Integer swapps = dataSnapshot.child("completed_swapps").getValue(Integer.class);
                    if(swapps != null){
                        currUser.setNumOfCompletedSwapps(swapps);
                    }
                    currUser.setHometown(dataSnapshot.child("hometown").getValue(String.class));
                    for(DataSnapshot snapshot : dataSnapshot.child("interests").getChildren()){
                        String category = snapshot.getValue(String.class);
                        currUser.getInterestedInCategoriesList().add(category);
                    }

                    currUser.getConversationsKeysList().clear();
                    for(DataSnapshot snapshot : dataSnapshot.child("conversationsKeysList").getChildren()){
                        String conversationKey = snapshot.getValue(String.class);
                        currUser.getConversationsKeysList().add(conversationKey);
                    }
                    getUsersConversations();

                    if(authFlag){
                        authFlag = false;
                        pager.setAdapter(pagerAdapter);
                        pager.setCurrentItem(1000);
                        subscribeToTopicsForMessaging(currUser.getConversationsKeysList());
                    }

                    currUser.setAdsList(currUserAdsList);
                    userPageAdAdapter.notifyDataSetChanged();
                }
                progressDialog.dismiss();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                progressDialog.dismiss();
                Log.w("TAGGGG", "Failed to read value.", databaseError.toException());
            }
        });
    }

    private void subscribeToTopicsForMessaging(List<String> conversationsKeysList) {
        if(conversationsKeysList != null){
            for (String conversationKey : conversationsKeysList){
                messaging.subscribeToTopic(conversationKey);
            }
        }
    }

    int expectedConversationsNumber;
    private void getUsersConversations(){
        expectedConversationsNumber = currUser.getConversationsKeysList().size();
        conversationsList.clear();
        for(final String conversationKey : currUser.getConversationsKeysList()){
            Query lastQuery = conversations.child(conversationKey).orderByKey().limitToLast(1);
            lastQuery.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if(dataSnapshot.exists() && dataSnapshot.getChildren().iterator().hasNext()){
                        Message message = dataSnapshot.getChildren().iterator().next().getValue(Message.class);
                        Conversation conversation = null;

                        if(message.getReceiverId().equals(firebaseAuth.getUid()))//then sender is my interlocutor
                        {
                            conversation = new Conversation(conversationKey, message.getSenderPhotoUrl(), message.getSenderName(),
                                    message.getSenderId(), message.getMessageText(), message.getTime());
                        }
                        else //the receiver is  my interlocutor
                        {
                            conversation = new Conversation(conversationKey, message.getReceiverPhotoUrl(), message.getReceiverName(),
                                    message.getReceiverId(), message.getMessageText(), message.getTime());
                        }

                        conversationsList.add(conversation);

                    } else {
                        expectedConversationsNumber--;
                    }

                    if(conversationsList.size() == expectedConversationsNumber){
                        conversationAdapter.notifyDataSetChanged();

                        if(getIntent().getBooleanExtra("cameFromNotification", false)){
                            pager.setCurrentItem(1003);
                        }
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                }
            });
        }
    }


    private void showNewestAds() {
        final ProgressDialog progressDialog = new ProgressDialog(MainActivity.this);
        progressDialog.setMessage(getResources().getString(R.string.loading));
        progressDialog.show();
        Query lastQuery = users.child("all_users_ads").orderByKey().limitToLast(MAX_SIZE_OF_ADS_LIST);
        lastQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                adsList.clear();
                for(DataSnapshot adDataSnapshot : dataSnapshot.getChildren()){
                    Advertisement ad = adDataSnapshot.getValue(Advertisement.class);
                    adsList.add(ad);
                }
                adAdapter.notifyDataSetChanged();
                progressDialog.dismiss();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                progressDialog.dismiss();
            }
        });
    }

    //shows things that located in the city you are staying in
    private void showNearbyAds(final String city) {
        if(city != null){
            final ProgressDialog progressDialog = new ProgressDialog(MainActivity.this);
            progressDialog.setMessage(getResources().getString(R.string.loading));
            progressDialog.show();
            Query lastQuery = users.child("all_users_ads").orderByKey().limitToLast(MAX_SIZE_OF_ADS_LIST);
            lastQuery.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    adsList.clear();
                    for (DataSnapshot adDataSnapshot : dataSnapshot.getChildren()) {
                        Advertisement advertisement = adDataSnapshot.getValue(Advertisement.class);
                        if (advertisement.getAdLocation().toLowerCase().equals(city.toLowerCase())
                                || advertisement.getAdLocation().toLowerCase().contains(city.toLowerCase())
                                || city.toLowerCase().contains(advertisement.getAdLocation().toLowerCase())) {
                            adsList.add(advertisement);
                        }
                    }

                    adAdapter.notifyDataSetChanged();
                    progressDialog.dismiss();
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    progressDialog.dismiss();
                }
            });
        }
        else
        {
            Toast.makeText(MainActivity.this, getResources().getString(R.string.location_not_found), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == LOCATION_PERMISSION_REQUEST) {
            if(grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, getResources().getString(R.string.permission_denied), Toast.LENGTH_SHORT).show();
            }
            else startLocation();
        }
    }

    private void setupUser(int itemId){
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        View dialogView  = getLayoutInflater().inflate(R.layout.sign_dlg,null);

        final EditText usernameEt = dialogView.findViewById(R.id.username_input);
        final EditText passwordEt = dialogView.findViewById(R.id.password_input);
        final EditText passwordConfirmationEt = dialogView.findViewById(R.id.password_confirmation_input);
        final EditText emailEt = dialogView.findViewById(R.id.email_input);

        switch (itemId)
        {
            case R.id.item_sign_up:
                final AlertDialog dialog = builder.setView(dialogView).setPositiveButton(R.string.register, null).create();
                dialog.show();
                dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        currUsername = usernameEt.getText().toString();
                        String password = passwordEt.getText().toString();
                        String passwordConfirmation = passwordConfirmationEt.getText().toString();
                        String email = emailEt.getText().toString();
                        if(currUsername.isEmpty() || password.isEmpty() || passwordConfirmation.isEmpty() || email.isEmpty()){
                            Toast.makeText(MainActivity.this, getResources().getString(R.string.fields_are_not_filled), Toast.LENGTH_SHORT).show();
                        } else if (!password.equals(passwordConfirmation)) {
                            passwordEt.setError(getResources().getString(R.string.passwords_not_equal));
                            passwordEt.requestFocus();
                            passwordConfirmationEt.setError("");
                        } else {
                            firebaseAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if (!task.isSuccessful()) {
                                        try {
                                            throw task.getException();
                                        } catch (FirebaseAuthWeakPasswordException e) {
                                            passwordEt.setError(getResources().getString(R.string.password_not_strong));
                                            passwordEt.requestFocus();
                                        } catch (FirebaseAuthInvalidCredentialsException e) {
                                            emailEt.setError(getResources().getString(R.string.invalid_email));
                                            emailEt.requestFocus();
                                        } catch (FirebaseAuthUserCollisionException e) {
                                            emailEt.setError(getResources().getString(R.string.account_already_exists));
                                            emailEt.requestFocus();
                                        } catch (Exception e) {
                                            Log.e("createUserWithEmailAndPassword exception", e.getMessage());
                                        }
                                    } else {
                                        dialog.dismiss();
                                    }
                                }
                            });
                        }
                    }
                });
                break;
            case  R.id.item_sign_in:
                passwordConfirmationEt.setVisibility(View.GONE);
                usernameEt.setVisibility(View.GONE);
                final AlertDialog dialogSignIn = builder.setView(dialogView).setPositiveButton(R.string.login, null).create();
                dialogSignIn.show();
                dialogSignIn.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        String email = emailEt.getText().toString();
                        String password = passwordEt.getText().toString();
                        if(password.isEmpty() || email.isEmpty()){
                            Toast.makeText(MainActivity.this, getResources().getString(R.string.fields_are_not_filled), Toast.LENGTH_SHORT).show();
                        } else {
                            firebaseAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if(task.isSuccessful()){
                                        dialogSignIn.dismiss();
                                    } else {
                                        emailEt.setError(getResources().getString(R.string.invalid_email_or_password));
                                        emailEt.requestFocus();
                                        passwordEt.setError("");
                                    }
                                }
                            });
                        }
                    }
                });
                break;
            case R.id.item_sign_out:
                firebaseAuth.signOut();
                Intent intent = getIntent();
                finish();
                startActivity(intent);
                break;
        }
    }

    private class MyNavigationViewItemListener implements NavigationView.OnNavigationItemSelectedListener{
        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            drawerLayout.closeDrawers();
            if(item.getItemId() == R.id.item_sign_up || item.getItemId() == R.id.item_sign_in || item.getItemId() == R.id.item_sign_out)
            {
                setupUser(item.getItemId());
            }
            else if(item.getItemId() == R.id.item_notif_on)
            {
                Intent intent = new Intent(MainActivity.this, FrequentNotifierService.class);
                PendingIntent pendingIntent = PendingIntent.getService(MainActivity.this, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
                //every 30 sec - change it in service and here!!!!!!!!!!!!!!!
                alarmManager.setExact(AlarmManager.RTC_WAKEUP,System.currentTimeMillis() + 30000,pendingIntent);
                Toast.makeText(MainActivity.this, getResources().getString(R.string.subscribed), Toast.LENGTH_SHORT).show();
            }
            else if(item.getItemId() == R.id.item_notif_off)
            {
                Intent intent = new Intent(MainActivity.this, FrequentNotifierService.class);
                PendingIntent pendingIntent = PendingIntent.getService(MainActivity.this,0,intent,PendingIntent.FLAG_CANCEL_CURRENT);
                alarmManager.cancel(pendingIntent);
                stopService(intent);
                getResources().getString(R.string.unsubscribed);
            }

            return false;
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        firebaseAuth.addAuthStateListener(authStateListener);
    }

    @Override
    protected void onStop() {
        super.onStop();
        firebaseAuth.removeAuthStateListener(authStateListener);
    }

    private class ViewPagerAdapter extends FragmentStatePagerAdapter {

        public ViewPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            Fragment fragment = null;
            String username = firebaseAuth.getCurrentUser() == null ? "" : firebaseAuth.getCurrentUser().getDisplayName();
///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
            if(firebaseAuth.getCurrentUser() == null){
                fragment = HomeFragment.newInstance(adAdapter, adsList);
            }
            else
            {
                switch (position % 5)
                {
                    case 0:
                        fragment = HomeFragment.newInstance(adAdapter, adsList);
                        break;
                    case 1:
                        fragment = CurrentUserFragment.newInstance(userPageAdAdapter, currUserAdsList, currUser, username);
                        break;
                    case 2:
                        String currUserCity = currUser.getHometown() != null ? currUser.getHometown() : "";
                        fragment = NewAdvertisementFragment.newInstance(currUserCity, username);
                        break;
                    case 3:
                        fragment = MyConversationsFragment.newInstance(conversationsList, conversationAdapter);
                        break;
                    case 4:
                        fragment = SearcherFragment.newInstance();
                        break;
                }
            }


            return fragment;
        }

        @Override
        public int getCount() {
            return 3000;
        }
    }

    private class MyBottomNavigationViewListener implements BottomNavigationView.OnNavigationItemSelectedListener{
        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            Menu menuNav = bottomNavigationView.getMenu();
            switch (item.getItemId()) {
                case R.id.item_home:
                    menuNav.findItem(R.id.item_home).setChecked(true);
                    pager.setCurrentItem(1000);
                    return true;
                case R.id.item_user:
                    menuNav.findItem(R.id.item_user).setChecked(true);
                    pager.setCurrentItem(1001);
                    return true;
                case R.id.item_new_ad:
                    menuNav.findItem(R.id.item_new_ad).setChecked(true);
                    pager.setCurrentItem(1002);
                    return true;
                case R.id.item_conversations:
                    menuNav.findItem(R.id.item_conversations).setChecked(true);
                    pager.setCurrentItem(1003);
                    return true;
                case R.id.item_search:
                    menuNav.findItem(R.id.item_search).setChecked(true);
                    pager.setCurrentItem(1004);
                    return true;
            }
            return false;
        }
    }

    private void showSelectedAd(Advertisement ad, boolean currUsersAd){
        SelectedAdFragment adFragment = SelectedAdFragment.newInstance(ad, currUsersAd, currUserAdsList);

        android.app.FragmentManager fragmentManager = getFragmentManager();
        android.app.FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.add(R.id.drawer_layout, adFragment, SELECTED_AD_FRAGMENT_TAG);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    private void showUserPage(String userId, final String userName) {
        if(firebaseAuth.getCurrentUser() == null || !firebaseAuth.getUid().equals(userId))
        {
            final ProgressDialog progressDialog = new ProgressDialog(MainActivity.this);
            progressDialog.setMessage(getResources().getString(R.string.loading));
            progressDialog.show();

            users.child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if(dataSnapshot.exists()) {
                        User selectedUser = makeUser(dataSnapshot);
                        progressDialog.dismiss();

                        AnotherUserFragment userFragment = AnotherUserFragment.newInstance(new AdvertisementAdapter(selectedUser.getAdsList(), getResources()),
                                selectedUser.getAdsList(), selectedUser, userName);
                        android.app.FragmentManager fragmentManager = getFragmentManager();
                        android.app.FragmentTransaction transaction = fragmentManager.beginTransaction();
                        transaction.add(R.id.drawer_layout, userFragment, SELECTED_USER_FRAGMENT_TAG);
                        transaction.addToBackStack(null);
                        transaction.commit();
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    progressDialog.dismiss();
                }
            });
        }
        else
        {
            closeAllFragments();
            pager.setCurrentItem(1001);
        }
    }

    private User makeUser(DataSnapshot dataSnapshot) {
        User user = new User();
        for(DataSnapshot snapshotAd : dataSnapshot.child("adslist").getChildren()){
            Advertisement ad = snapshotAd.getValue(Advertisement.class);
            user.getAdsList().add(ad);
        }

        for(DataSnapshot snapshotAd : dataSnapshot.child("interests").getChildren()){
            String category = snapshotAd.getValue(String.class);
            user.getInterestedInCategoriesList().add(category);
        }

        String hometown = dataSnapshot.child("hometown").getValue(String.class);
        Integer completedSwapps = dataSnapshot.child("completed_swapps").getValue(Integer.class);
        String imageUrl = dataSnapshot.child("imageUrl").getValue(String.class);

        user.setHometown(hometown);
        user.setImageUrl(imageUrl);
        if(completedSwapps != null){
            user.setNumOfCompletedSwapps(completedSwapps);
        }

        for(DataSnapshot snapshot : dataSnapshot.child("conversationsKeysList").getChildren()){
            String conversationKey = snapshot.getValue(String.class);
            user.getConversationsKeysList().add(conversationKey);
        }

        return user;
    }

    private void deleteConversation(String conversationKey, final int adapterPosition) {
        currUser.getConversationsKeysList().remove(conversationKey);
        users.child(firebaseAuth.getUid()).child("conversationsKeysList").setValue(currUser.getConversationsKeysList()).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                conversationsList.remove(adapterPosition);
                conversationAdapter.notifyItemRemoved(adapterPosition);
            }
        });
    }

    private void startConversation(final String publisherId, final String publisherName, final Advertisement attachedAd) {
        final ProgressDialog progressDialog = new ProgressDialog(MainActivity.this);
        progressDialog.setMessage(getResources().getString(R.string.loading));
        progressDialog.show();

        users.child(publisherId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()) {
                    User publisherUser = makeUser(dataSnapshot);
                    progressDialog.dismiss();

                    ConversationFragment conversationFragment = ConversationFragment.newInstance(currUser, firebaseAuth.getUid(), firebaseAuth.getCurrentUser().getDisplayName(), publisherUser, publisherId, publisherName, attachedAd);
                    android.app.FragmentManager fragmentManager = getFragmentManager();
                    android.app.FragmentTransaction transaction = fragmentManager.beginTransaction();
                    transaction.add(R.id.drawer_layout, conversationFragment, CONVERSATION_FRAGMENT_TAG);
                    transaction.addToBackStack(null);
                    transaction.commit();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                progressDialog.dismiss();
            }
        });
    }

    /////////////////////MyConversationsFragment.OnMyConversationsFragmentListener implementation///////////////////////////////

    @Override
    public void OnConversationClicked(String interlocutorId, String interlocutorName) {
        startConversation(interlocutorId, interlocutorName, null);
    }

    @Override
    public void OnConversationLongClicked(String conversationKey, int adapterPosition) {
        deleteConversation(conversationKey, adapterPosition);
    }

    /////////////////////////////////////////////////////////////////////////////////////////////////////////

    ////////////////ConversationFragment.OnConversationFragmentListener implementation////////////////////////
    @Override
    public void OnMessageWithAdSelected(String linkToAd) {
        users.child("all_users_ads").child(linkToAd).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    Advertisement ad = dataSnapshot.getValue(Advertisement.class);
                    showSelectedAd(ad, true); //sending true because this way buttons 'start chat' and 'offer deal' will not be displayed
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    @Override
    public void OnInterlocutorClicked(String userId, String userName) {
        showUserPage(userId, userName);
    }

    @Override
    public void OnConversationAdded(String conversationKey) {
        messaging.subscribeToTopic(conversationKey);
        getUsersConversations();
    }
    /////////////////////////////////////////////////////////////////////////////////////////////////////

    //////////////////////////SelectedAdFragment.OnSelectedAdFragmentListener implementation////////////
    @Override
    public void onOfferDealClicked(Advertisement offeredAd, String publisherId, String publisherName) {
        startConversation(publisherId, publisherName, offeredAd);
    }

    @Override
    public void onStartConversationClicked(final String publisherId, final String publisherName) {
        startConversation(publisherId, publisherName, null);
    }

    @Override
    public void onOwnerPhotoClicked(String piblisherId, String publisherName) {
        showUserPage(piblisherId, publisherName);
    }


      /////////////////////////////////////////////////////////////////////////////////////////////////////////////

    ///////////////////////// AnotherUser.OnAnotherUserFragmentListener implementation//////////////////////////////////
    @Override
    public void onAnotherUserAdClicked(Advertisement ad) {
        boolean currUsersAd = firebaseAuth.getCurrentUser() != null ? ad.getAdPublisherId().equals(firebaseAuth.getCurrentUser().getUid()) : true;
        showSelectedAd(ad, currUsersAd);
    }
    ////////////////////////////////////////////////////////////////////////////////////////////////////////

    /////////////////////////// HomeFragment.OnHomeFragmentListener implementation/////////////////////
    @Override
    public void onClicked(Advertisement ad) {
        boolean currUsersAd = firebaseAuth.getCurrentUser() != null ? ad.getAdPublisherId().equals(firebaseAuth.getCurrentUser().getUid()) : true;
        showSelectedAd(ad, currUsersAd);
    }

    @Override
    public void onOwnerClicked(String id, String name) {
        showUserPage(id, name);
    }
    ////////////////////////////////////////////////////////////////////////////////////////////////////

    ////////////////////////////NewAdvertisementFragment.OnCreatedAdListener implementation/////////////////////////////////////////
    @Override
    public void onNewAdCreated(final Advertisement ad, final List<String> urlsList) {
        pager.setAdapter(pagerAdapter);
        pager.setCurrentItem(1001);
        String imgUrl = currUser.getImageUrl();
        if(imgUrl != null){
            ad.setPublisherPhotoUrl(imgUrl);
        }
        ad.setAdPublisherId(firebaseAuth.getCurrentUser().getUid());

        final ProgressDialog progressDialog = new ProgressDialog(MainActivity.this);
        progressDialog.setMessage(getResources().getString(R.string.loading));
        progressDialog.show();
        for(String url : urlsList)
        {
            Uri uri = Uri.parse(url);
            StorageReference filepath=storageRef.child("Images").child(firebaseAuth.getCurrentUser().getUid()).child(ad.getAdName()).child(uri.getLastPathSegment());
            filepath.putFile(uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    Uri downloadUrl = taskSnapshot.getDownloadUrl();
                    ad.getAdPhotosList().add(downloadUrl.toString());
                    if(ad.getAdPhotosList().size() == urlsList.size())
                    {
                        DatabaseReference newRef = users.child("all_users_ads").push();
                        ad.setKeyInsideCommonDb(newRef.getKey());
                        newRef.setValue(ad); //adding into common db

                        currUserAdsList.add(ad);
                        userPageAdAdapter.notifyItemInserted(adsList.size()-1);
                        users.child(firebaseAuth.getCurrentUser().getUid()).child("adslist").setValue(currUserAdsList);
                        progressDialog.dismiss();
                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    progressDialog.dismiss();
                }
            });
        }
    }
    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    ///////////////////////CurrentUserFragment.OnCurrentUserFragmentListener implementation///////////////////////////
    @Override
    public void onUserAdClicked(Advertisement ad) {
        showSelectedAd(ad, true);
    }

    @Override
    public void onUserItemSelectedForRemove(final Advertisement ad, int position) {
        for(String url : ad.getAdPhotosList()){
            deleteFromFBStorage(url);
        }
        currUserAdsList.remove(ad);
        users.child(firebaseAuth.getCurrentUser().getUid()).child("adslist").setValue(currUserAdsList);
        userPageAdAdapter.notifyItemRemoved(position);

        if(ad.getKeyInsideCommonDb() != null && !ad.getKeyInsideCommonDb().isEmpty()){
            users.child("all_users_ads").child(ad.getKeyInsideCommonDb()).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if(task.isSuccessful()){
                        for(Advertisement ad2 : adsList){
                            if(ad2.getKeyInsideCommonDb() != null && ad2.getKeyInsideCommonDb().equals(ad.getKeyInsideCommonDb())){
                                adsList.remove(ad2);
                                adAdapter.notifyDataSetChanged();
                                break;
                            }
                        }
                    }
                }
            });
        }
    }

    @Override
    public void onUserInfoEditCompleted(String placeOfLiving, List<String> categoriesInterestedIn) {
        users.child(firebaseAuth.getCurrentUser().getUid()).child("hometown").setValue(placeOfLiving);
        users.child(firebaseAuth.getCurrentUser().getUid()).child("interests").setValue(categoriesInterestedIn);
        currUser.setHometown(placeOfLiving);
        currUser.setInterestedInCategoriesList(categoriesInterestedIn);
    }

    @Override
    public void onUserPhotoChanged(Uri uri) {

        //deleting old image reference
        deleteFromFBStorage(currUser.getImageUrl());

        StorageReference filepath=storageRef.child("Images").child(firebaseAuth.getCurrentUser().getUid()).child(uri.getLastPathSegment());
        filepath.putFile(uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                Uri downloadUrl = taskSnapshot.getDownloadUrl();
                users.child(firebaseAuth.getCurrentUser().getUid()).child("imageUrl").setValue(downloadUrl.toString());
                ImageFileWorkingHelper.fillCircledIvWithPicasso(downloadUrl, userIv);

                //updating profile pics on each user's ad on user page
                for(Advertisement ad : currUserAdsList){
                    ad.setPublisherPhotoUrl(downloadUrl.toString());
                }

                //updating profile pics on each user's ad on home page and in common db
                for(Advertisement ad : adsList){
                    if(ad.getAdPublisherId() != null && ad.getAdPublisherId().equals(firebaseAuth.getUid())){
                        ad.setPublisherPhotoUrl(downloadUrl.toString());
                        if(ad.getKeyInsideCommonDb() != null){
                            users.child("all_users_ads").child(ad.getKeyInsideCommonDb()).child("publisherPhotoUrl").setValue(downloadUrl.toString());
                        }
                    }
                }

                adAdapter.notifyDataSetChanged();
                userPageAdAdapter.notifyDataSetChanged();
                users.child(firebaseAuth.getCurrentUser().getUid()).child("adslist").setValue(currUserAdsList);
                currUser.setImageUrl(downloadUrl.toString());
            }
        });
    }
    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    //////////////////////////SearcherFragment.OnSearcherFragmentListener implementation//////////////////////////////
    @Override
    public void OnFindAdsClicked(final String category, final String city, final boolean needToMatchCategories, final String freeText) {
        final ProgressDialog progressDialog = new ProgressDialog(MainActivity.this);
        progressDialog.setMessage(getResources().getString(R.string.loading));
        progressDialog.show();
        Query lastQuery = users.child("all_users_ads").orderByKey().limitToLast(1000);//.limitToLast(1000) - think how to make searching on huge db
        lastQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                adsList.clear();
                for(DataSnapshot adDataSnapshot : dataSnapshot.getChildren()){
                    Advertisement ad = adDataSnapshot.getValue(Advertisement.class);

                    if(!category.isEmpty() && !ad.getAdCategory().equals(category)){
                        continue;
                    }
                    if(!city.isEmpty() && !(ad.getAdLocation().toLowerCase().equals(city)
                                    || ad.getAdLocation().toLowerCase().contains(city)
                                    || city.contains(ad.getAdLocation().toLowerCase())))
                    {
                        continue;
                    }
                    if(needToMatchCategories){
                        boolean found = false;
                        for(Advertisement myAd : currUserAdsList){
                            for(String category : ad.getCategoriesForSwap()){
                                if(myAd.getAdCategory().equals(category)){
                                    found = true;
                                    break;
                                }
                            }

                            if(found){
                                break;
                            }
                        }
                        if(!found){
                            continue;
                        }
                    }
                    if(!freeText.isEmpty() && !(ad.getAdCategory().toLowerCase().contains(freeText) ||
                            ad.getAdLocation().toLowerCase().contains(freeText) ||  ad.getAdPublisherName().toLowerCase().contains(freeText) ||
                            ad.getAdName().toLowerCase().contains(freeText) || ad.getAdDescription().toLowerCase().contains(freeText)))
                    {
                       continue;
                    }

                    adsList.add(ad);
                }
                adAdapter.notifyDataSetChanged();
                progressDialog.dismiss();
                pager.setCurrentItem(1000);
                Toast.makeText(MainActivity.this, adsList.size() + " " + getResources().getString(R.string.results) + " " + getResources().getString(R.string.found),
                        Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                progressDialog.dismiss();
            }
        });
    }
    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    private void deleteFromFBStorage(String url){
        if(url != null && !url.isEmpty()){
            StorageReference oldImagePath = FirebaseStorage.getInstance().getReferenceFromUrl(url);
            oldImagePath.delete();
        }
    }

    @Override
    public void onBackPressed() {

        if(getFragmentManager().getBackStackEntryCount() > 0) {
            getFragmentManager().popBackStack();
        }
        else super.onBackPressed();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == android.R.id.home) {
            drawerLayout.openDrawer(Gravity.START);
        }

        return super.onOptionsItemSelected(item);
    }

    private void closeAllFragments() {
        while (getFragmentManager().getBackStackEntryCount() > 0) {
            getFragmentManager().popBackStackImmediate();
        }
    }

}
