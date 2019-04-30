package il.co.freebie.swapit;
import android.text.format.DateFormat;

import java.util.Date;

/**
 * Created by one 1 on 17-Feb-19.
 */

public class Message {
    private String receiverId;
    private String receiverName;
    private String receiverPhotoUrl;
    private String senderId;
    private String senderName;
    private String senderPhotoUrl;
    private String messageText;
    private String time;
    private String attachedAdInCommonDb;
    private String photoUrl;

    public Message() {
    }

    public Message(String receiverId, String senderId, String messageText, String attachedAdInCommonDb, String photoUrl,
                   String receiverName, String receiverPhotoUrl, String senderName, String senderPhotoUrl) {
        this.receiverId = receiverId;
        this.senderId = senderId;
        this.messageText = messageText;
        this.attachedAdInCommonDb = attachedAdInCommonDb;
        this.photoUrl = photoUrl;
        this.receiverName = receiverName;
        this.receiverPhotoUrl = receiverPhotoUrl;
        this.senderName = senderName;
        this.senderPhotoUrl = senderPhotoUrl;
        time = DateFormat.format("dd/MM  HH:mm", new Date().getTime()).toString();
    }

    public String getReceiverName() {
        return receiverName;
    }

    public void setReceiverName(String receiverName) {
        this.receiverName = receiverName;
    }

    public String getReceiverPhotoUrl() {
        return receiverPhotoUrl;
    }

    public void setReceiverPhotoUrl(String receiverPhotoUrl) {
        this.receiverPhotoUrl = receiverPhotoUrl;
    }

    public String getSenderName() {
        return senderName;
    }

    public void setSenderName(String senderName) {
        this.senderName = senderName;
    }

    public String getSenderPhotoUrl() {
        return senderPhotoUrl;
    }

    public void setSenderPhotoUrl(String senderPhotoUrl) {
        this.senderPhotoUrl = senderPhotoUrl;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }

    public String getReceiverId() {
        return receiverId;
    }

    public void setReceiverId(String receiverId) {
        this.receiverId = receiverId;
    }

    public String getSenderId() {
        return senderId;
    }

    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }

    public String getMessageText() {
        return messageText;
    }

    public void setMessageText(String messageText) {
        this.messageText = messageText;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getAttachedAdInCommonDb() {
        return attachedAdInCommonDb;
    }

    public void setAttachedAdInCommonDb(String attachedAdInCommonDb) {
        this.attachedAdInCommonDb = attachedAdInCommonDb;
    }
}
