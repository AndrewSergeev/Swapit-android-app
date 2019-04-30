package il.co.freebie.swapit;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import java.util.Comparator;
import java.util.List;

/**
 * Created by one 1 on 26-Jan-19.
 */

public class MyConversationsFragment extends Fragment {
    private static List<Conversation> conversationList;
    private static ConversationAdapter adapter;
    private ActionMode mActionMode;

    interface OnMyConversationsFragmentListener{
        void OnConversationClicked(String interlocutorId, String interlocutorName);
        void OnConversationLongClicked(String conversationKey, int adapterPosition);
    }

    private OnMyConversationsFragmentListener callback;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try{
            callback = (OnMyConversationsFragmentListener)context;
        }catch (ClassCastException ex) {
            throw new ClassCastException("The activity must implement OnMyConversationsFragmentListener interface");
        }
    }

    public static MyConversationsFragment newInstance(List<Conversation> list, ConversationAdapter conversationAdapter) {
        MyConversationsFragment conversationsFragment = new MyConversationsFragment();

        conversationList = list;
        adapter = conversationAdapter;

        if(conversationList.size() >= 2){
            conversationList.sort(new Comparator<Conversation>() {
                @Override
                public int compare(Conversation conversation, Conversation t1) {
                    return conversation.getLastMessageTime().compareTo(t1.getLastMessageTime());
                }
            });
        }

        return conversationsFragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.my_conversations_fragment,container,false);

        RecyclerView recyclerView = view.findViewById(R.id.conversations_recycler);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        layoutManager.setReverseLayout(true);
        layoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(layoutManager);

        adapter.setListener(new ConversationAdapter.ConversationListener() {
            @Override
            public void OnConversationClicked(String interlocutorId, String interlocutorName) {
                if(mActionMode != null)
                    mActionMode.finish();
                else if(callback != null)
                    callback.OnConversationClicked(interlocutorId, interlocutorName);
            }

            @Override
            public void OnConversationLongClicked(final String conversationKey, final View view, final String interlocutorName, final int position) {
                view.startActionMode(new ActionMode.Callback() {
                    @Override
                    public boolean onCreateActionMode(ActionMode actionMode, Menu menu) {
                        getActivity().getMenuInflater().inflate(R.menu.context_menu_action_mode,menu);
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
                            builder.setMessage(getResources().getString(R.string.delete_chat) + " " + interlocutorName + " ?")
                                    .setPositiveButton(getResources().getString(R.string.delete), new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            if(callback != null){
                                                callback.OnConversationLongClicked(conversationKey, position);
                                                actionMode.finish();
                                            }
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
        });

        recyclerView.setAdapter(adapter);

        return view;
    }
}
