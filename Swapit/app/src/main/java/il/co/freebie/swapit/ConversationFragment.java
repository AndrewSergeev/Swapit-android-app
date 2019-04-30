package il.co.freebie.swapit;

import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.firebase.ui.database.FirebaseListAdapter;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;


/**
 * Created by one 1 on 17-Feb-19.
 */

public class ConversationFragment extends Fragment {
    public static final String CONVERSATIONS = "conversations";
    public static final String USERS = "users";
    public static final String API_TOKEN_KEY = "AAAAAhB4HUA:APA91bEIHBLHIh0NLBZQk7V6K2mGRnPBaq5XfkabQJM28XzNW6MNgQ6TlaCoVUgxoyNWUAIgt5mpYm8dNlXEVsAy36R_rRCYyLfuadvfQYus9bOT7iLYYeaxlRIyxlPpet3crU-f552W";

    private FirebaseListAdapter<Message> adapter;
    private DatabaseReference users = FirebaseDatabase.getInstance().getReference(USERS);
    private DatabaseReference conversations = FirebaseDatabase.getInstance().getReference(CONVERSATIONS);

    private static User currUser;
    private static String currUserId;
    private static String currUserName;
    private static User receiverUser;
    private static String receiverUserId;
    private static String receiverUserName;
    private static String conversationKey;
    private static Advertisement offeredAdAsFirstMsg;

    interface OnConversationFragmentListener {
        void OnMessageWithAdSelected(String attachedAdInCommonDb);
        void OnInterlocutorClicked(String userId, final String userName);
        void OnConversationAdded(String conversationKey);
    }

    private OnConversationFragmentListener callback;
    private MyOnInterlocutorClickListener interlocutorClickListener = new MyOnInterlocutorClickListener();

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            callback = (ConversationFragment.OnConversationFragmentListener) context;
        } catch (ClassCastException ex) {
            throw new ClassCastException("The activity must implement OnConversationFragmentListener interface");
        }
    }

    public static ConversationFragment newInstance(User sender, String senderId, String senderName,
                                                   User receiver, String receiverId, String receiverName, Advertisement attachedAd){
        ConversationFragment fragment = new ConversationFragment();
        currUser = sender;
        receiverUser = receiver;
        currUserName = senderName;
        receiverUserName = receiverName;
        currUserId = senderId;
        receiverUserId = receiverId;
        offeredAdAsFirstMsg = attachedAd;
        conversationKey = getConversationKey(currUserId, receiverUserId);

        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        if(offeredAdAsFirstMsg != null){
            sendMessage(receiverUserId, currUserId, offeredAdAsFirstMsg.getAdName(), offeredAdAsFirstMsg.getKeyInsideCommonDb(), offeredAdAsFirstMsg.getAdPhotosList().get(0),
                    receiverUserName, receiverUser.getImageUrl(), currUserName, currUser.getImageUrl());
        }

        View view = inflater.inflate(R.layout.conversation_fragment,container,false);
        final ListView listOfMessages = view.findViewById(R.id.list_of_messages);
        ImageButton attachAdIb = view.findViewById(R.id.attach_ib);
        ImageButton sendIb = view.findViewById(R.id.send_msg_ib);
        final EditText messageEt = view.findViewById(R.id.msg_input_et);

        Toolbar toolbar = view.findViewById(R.id.toolbar);
        if(TextUtils.getLayoutDirectionFromLocale(Locale.getDefault()) != ViewCompat.LAYOUT_DIRECTION_RTL){
            toolbar.setNavigationIcon(R.drawable.leftarrow);
        } else {
            toolbar.setNavigationIcon(R.drawable.rightarrow);
        }
        TextView toolbarTv = view.findViewById(R.id.toolbar_tv);
        toolbarTv.setText(receiverUserName);
        toolbarTv.setOnClickListener(interlocutorClickListener);
        ImageView toolbarIv = view.findViewById(R.id.toolbar_iv);
        if(receiverUser.getImageUrl() != null){
            ImageFileWorkingHelper.fillCircledIvWithPicasso(Uri.parse(receiverUser.getImageUrl()), toolbarIv);
        }
        toolbarIv.setOnClickListener(interlocutorClickListener);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().onBackPressed();
            }
        });

        //sending simple message
        sendIb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String text = messageEt.getText().toString();
                if(!text.isEmpty()){
                    sendMessage(receiverUserId, currUserId, messageEt.getText().toString(), "", "",
                            receiverUserName, receiverUser.getImageUrl(), currUserName, currUser.getImageUrl());
                    messageEt.setText("");
                }
            }
        });

        //offering a deal
        attachAdIb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                LayoutInflater inflater = getActivity().getLayoutInflater();
                View dialogView  = inflater.inflate(R.layout.my_ads_dlg,null);
                builder.setView(dialogView);
                builder.setTitle(getResources().getString(R.string.choose_ad));

                RecyclerView recyclerView = dialogView.findViewById(R.id.recycler_in_items_dlg);
                recyclerView.setLayoutManager(new GridLayoutManager(getActivity(), 2));
                AdSquareAdapter adSquareAdapter = new AdSquareAdapter(currUser.getAdsList());
                final AlertDialog dialog = builder.create();
                adSquareAdapter.setListener(new AdSquareAdapter.AdSquareListener() {
                    @Override
                    public void OnSquaredAdClicked(final int position, View view) {
                        dialog.dismiss();
                        sendMessage(receiverUserId, currUserId, currUser.getAdsList().get(position).getAdName(),
                                currUser.getAdsList().get(position).getKeyInsideCommonDb(), currUser.getAdsList().get(position).getAdPhotosList().get(0),
                                receiverUserName, receiverUser.getImageUrl(), currUserName, currUser.getImageUrl());
                    }
                });
                recyclerView.setAdapter(adSquareAdapter);
                dialog.show();
            }
        });

        adapter = new FirebaseListAdapter<Message>(getActivity(), Message.class,
                R.layout.message_cell, conversations.child(conversationKey)) {
            @Override
            protected void populateView(View v, final Message model, int position) {

                LinearLayout layout = v.findViewById(R.id.root_msg_layout);
                RelativeLayout relativeLayout = v.findViewById(R.id.msg_relative);
                LinearLayout.LayoutParams params = (LinearLayout.LayoutParams)relativeLayout.getLayoutParams();
                float factorRel = relativeLayout.getContext().getResources().getDisplayMetrics().density;
                if(model.getSenderId().equals(currUserId)){
                    layout.setGravity(Gravity.END);
                    params.setMarginStart((int)(20 * factorRel));
                    params.setMarginEnd(0);
                    relativeLayout.setBackground(ContextCompat.getDrawable(getActivity(), R.drawable.message_of_sender_background_shape));
                } else {
                    layout.setGravity(Gravity.START);
                    params.setMarginStart(0);
                    params.setMarginEnd((int)(20 * factorRel));
                    relativeLayout.setBackground(ContextCompat.getDrawable(getActivity(), R.drawable.message_background_shape));
                }
                relativeLayout.setLayoutParams(params);


                ImageView attachedAdPhotoIv = v.findViewById(R.id.ad_photo_in_message_iv);
                TextView messageTv = v.findViewById(R.id.msg_txt_tv);
                TextView timeTv = v.findViewById(R.id.time_msg_sent_tv);

                RelativeLayout.LayoutParams paramsMessageTv = (RelativeLayout.LayoutParams)messageTv.getLayoutParams();
                RelativeLayout.LayoutParams paramstimeTv = (RelativeLayout.LayoutParams)timeTv.getLayoutParams();
                if(TextUtils.getLayoutDirectionFromLocale(Locale.getDefault()) != ViewCompat.LAYOUT_DIRECTION_RTL){
                    paramsMessageTv.setMarginEnd((int)(5 * factorRel));
                    paramstimeTv.setMarginEnd((int)(5 * factorRel));
                } else {
                    paramsMessageTv.setMarginEnd((int)(-5 * factorRel));
                    paramstimeTv.setMarginEnd((int)(-5 * factorRel));
                }
                messageTv.setLayoutParams(paramsMessageTv);
                timeTv.setLayoutParams(paramstimeTv);

                messageTv.setText(model.getMessageText());
                timeTv.setText(model.getTime());
                if (model.getPhotoUrl() != null && !model.getPhotoUrl().isEmpty()) {
                    float factor = attachedAdPhotoIv.getContext().getResources().getDisplayMetrics().density;
                    attachedAdPhotoIv.getLayoutParams().height = (int) (120 * factor);
                    attachedAdPhotoIv.getLayoutParams().width = (int) (120 * factor);
                    Picasso.get().load(model.getPhotoUrl()).into(attachedAdPhotoIv);
                    attachedAdPhotoIv.setScaleType(ImageView.ScaleType.FIT_XY);

                    v.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            callback.OnMessageWithAdSelected(model.getAttachedAdInCommonDb());
                        }
                    });
                } else {
                    attachedAdPhotoIv.getLayoutParams().height = 0;
                    attachedAdPhotoIv.getLayoutParams().width = 0;
                    v.setOnClickListener(null);
                }
            }
        };

        listOfMessages.setAdapter(adapter);

        return view;
    }

    private void sendMessage(String receiverId, String senderId, String messageText, String attachedAdInCommonDb, String attachedAdPhotoUrl,
                             String receiverName, String receiverPhotoUrl, String senderName, String senderPhotoUrl) {
        if(attachedAdPhotoUrl == null){
            attachedAdPhotoUrl = "";
        }

        if(attachedAdInCommonDb == null){
            attachedAdInCommonDb = "";
        }

        Message message = new Message(receiverId, senderId, messageText, attachedAdInCommonDb, attachedAdPhotoUrl,
                receiverName, receiverPhotoUrl, senderName, senderPhotoUrl);
        conversations.child(conversationKey).push().setValue(message);
        saveConversationKeyIfNotExists();



        final JSONObject rootObject  = new JSONObject();
        try {
            rootObject.put("to", "/topics/" + conversationKey);
            rootObject.put("data",new JSONObject().put("message", senderName + ": " + messageText).put("sender", senderId));

            String url = "https://fcm.googleapis.com/fcm/send";

            RequestQueue queue = Volley.newRequestQueue(getActivity());
            StringRequest request = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {}
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {  }
            }) {
                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    Map<String,String> headers = new HashMap<>();
                    headers.put("Content-Type","application/json");
                    headers.put("Authorization","key="+API_TOKEN_KEY);
                    return headers;
                }

                @Override
                public byte[] getBody() throws AuthFailureError {
                    return rootObject.toString().getBytes();
                }
            };
            queue.add(request);
            //queue.start();

        }catch (JSONException ex) {
            ex.printStackTrace();
        }
    }

    private void saveConversationKeyIfNotExists() {
        if(!currUser.getConversationsKeysList().contains(conversationKey)){
            currUser.getConversationsKeysList().add(conversationKey);
            users.child(currUserId).child("conversationsKeysList").setValue(currUser.getConversationsKeysList()).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if(callback != null){
                        callback.OnConversationAdded(conversationKey);
                    }
                }
            });
        }
        if(!receiverUser.getConversationsKeysList().contains(conversationKey)){
            receiverUser.getConversationsKeysList().add(conversationKey);
            users.child(receiverUserId).child("conversationsKeysList").setValue(receiverUser.getConversationsKeysList());
        }
    }

    private static String getConversationKey(String currUserId, String receiverUserId) {
        return currUserId.compareTo(receiverUserId) > 0 ? currUserId+receiverUserId : receiverUserId+currUserId;
    }

    private class MyOnInterlocutorClickListener implements View.OnClickListener{
        @Override
        public void onClick(View view) {
            callback.OnInterlocutorClicked(receiverUserId, receiverUserName);
        }
    }
}
