package il.co.freebie.swapit;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by one 1 on 02-Feb-19.
 */

public class User {
    //username saved together with email, password
    private String imageUrl = "";
    private String hometown = "";
    private int numOfCompletedSwapps = 0;
    private List<String> interestedInCategoriesList = new ArrayList<>();
    private List<String> conversationsKeysList = new ArrayList<>();
    private List<Advertisement> adsList = new ArrayList<>();

    public User() {
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public List<String> getConversationsKeysList() {
        return conversationsKeysList;
    }

    public void setConversationsKeysList(List<String> conversationsKeysList) {
        this.conversationsKeysList = conversationsKeysList;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getHometown() {
        return hometown;
    }

    public void setHometown(String hometown) {
        this.hometown = hometown;
    }

    public int getNumOfCompletedSwapps() {
        return numOfCompletedSwapps;
    }

    public void setNumOfCompletedSwapps(int numOfCompletedSwapps) {
        this.numOfCompletedSwapps = numOfCompletedSwapps;
    }

    public List<String> getInterestedInCategoriesList() {
        return interestedInCategoriesList;
    }

    public void setInterestedInCategoriesList(List<String> interestedInCategoriesList) {
        this.interestedInCategoriesList = interestedInCategoriesList;
    }

    public List<Advertisement> getAdsList() {
        return adsList;
    }

    public void setAdsList(List<Advertisement> adsList) {
        this.adsList = adsList;
    }
}
