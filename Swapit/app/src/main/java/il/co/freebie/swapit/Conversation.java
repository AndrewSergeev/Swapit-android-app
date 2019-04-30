package il.co.freebie.swapit;

/**
 * Created by one 1 on 19-Feb-19.
 */

public class Conversation {
    private String conversationKey;
    private String interlocutorPhotoUrl;
    private String interlocutorName;
    private String interlocutorId;
    private String lastMessageText;
    private String lastMessageTime;

    public Conversation() {
    }

    public Conversation(String conversationKey, String interlocutorPhotoUrl, String interlocutorName, String interlocutorId, String lastMessageText, String lastMessageTime) {
        this.conversationKey = conversationKey;
        this.interlocutorPhotoUrl = interlocutorPhotoUrl;
        this.interlocutorName = interlocutorName;
        this.interlocutorId = interlocutorId;
        this.lastMessageText = lastMessageText;
        this.lastMessageTime = lastMessageTime;
    }

    public String getConversationKey() {
        return conversationKey;
    }

    public void setConversationKey(String conversationKey) {
        this.conversationKey = conversationKey;
    }

    public String getInterlocutorPhotoUrl() {
        return interlocutorPhotoUrl;
    }

    public void setInterlocutorPhotoUrl(String interlocutorPhotoUrl) {
        this.interlocutorPhotoUrl = interlocutorPhotoUrl;
    }

    public String getInterlocutorName() {
        return interlocutorName;
    }

    public void setInterlocutorName(String interlocutorName) {
        this.interlocutorName = interlocutorName;
    }

    public String getInterlocutorId() {
        return interlocutorId;
    }

    public void setInterlocutorId(String interlocutorId) {
        this.interlocutorId = interlocutorId;
    }

    public String getLastMessageText() {
        return lastMessageText;
    }

    public void setLastMessageText(String lastMessageText) {
        this.lastMessageText = lastMessageText;
    }

    public String getLastMessageTime() {
        return lastMessageTime;
    }

    public void setLastMessageTime(String lastMessageTime) {
        this.lastMessageTime = lastMessageTime;
    }
}
