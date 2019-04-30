package il.co.freebie.swapit;

import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.List;

/**
 * Created by one 1 on 19-Feb-19.
 */

public class ConversationAdapter  extends RecyclerView.Adapter<ConversationAdapter.ConversationViewHolder>  {
    private List<Conversation> conversationsList;
    private ConversationListener listener;

    interface ConversationListener{
        void OnConversationClicked(String interlocutorId, String interlocutorName);
        void OnConversationLongClicked(String conversationKey, View view, String interlocutorName, int position);
    }

    public void setListener(ConversationListener listener) {
        this.listener = listener;
    }

    public ConversationAdapter(List<Conversation> conversationsList) {
        this.conversationsList = conversationsList;
    }

    @Override
    public ConversationViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.conversation_cell, parent,false);

        return new ConversationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ConversationViewHolder holder, int position) {
        Conversation conversation = conversationsList.get(position);
        if(conversation.getInterlocutorPhotoUrl() != null && !conversation.getInterlocutorPhotoUrl().isEmpty()){
            Picasso.get().load(conversation.getInterlocutorPhotoUrl()).transform(new CircleTransform()).into(holder.interlocutorPhotoIv);
            holder.interlocutorPhotoIv.setScaleType(ImageView.ScaleType.CENTER_CROP);
        } else {
            holder.interlocutorPhotoIv.setImageDrawable(ContextCompat.getDrawable(holder.interlocutorPhotoIv.getContext(), R.drawable.userpic));
            holder.interlocutorPhotoIv.setScaleType(ImageView.ScaleType.CENTER);
        }

        holder.interlocutorNameTv.setText(conversation.getInterlocutorName());
        holder.lastMessageText.setText(conversation.getLastMessageText());
        holder.lastMessageTime.setText(conversation.getLastMessageTime());
        if(position == 0){
            holder.delimiterView.setVisibility(View.INVISIBLE);
        } else {
            holder.delimiterView.setVisibility(View.VISIBLE);
        }

    }

    @Override
    public int getItemCount() {
        return conversationsList.size();
    }

    public class ConversationViewHolder extends RecyclerView.ViewHolder{
        ImageView interlocutorPhotoIv;
        TextView interlocutorNameTv;
        TextView lastMessageText;
        TextView lastMessageTime;
        View delimiterView;

        public ConversationViewHolder(View itemView) {
            super(itemView);

            interlocutorPhotoIv = itemView.findViewById(R.id.interlocutor_photo_iv);
            interlocutorNameTv = itemView.findViewById(R.id.interlocutor_name_tv);
            lastMessageText = itemView.findViewById(R.id.last_msg_tv);
            lastMessageTime = itemView.findViewById(R.id.time_of_last_msg_tv);
            delimiterView = itemView.findViewById(R.id.delimiterView);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(listener != null){
                        Conversation selectedConversation = conversationsList.get(getAdapterPosition());
                        listener.OnConversationClicked(selectedConversation.getInterlocutorId(), selectedConversation.getInterlocutorName());
                    }
                }
            });

            itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    if(listener != null){
                        Conversation selectedConversation = conversationsList.get(getAdapterPosition());
                        listener.OnConversationLongClicked(selectedConversation.getConversationKey(), view, selectedConversation.getInterlocutorName(), getAdapterPosition());
                    }
                    return true;
                }
            });
        }
    }
}
