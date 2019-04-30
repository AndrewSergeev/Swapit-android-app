package il.co.freebie.swapit;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by one 1 on 26-Jan-19.
 */

public class Advertisement {
    private boolean liked = false;
    private String adPublisherName;
    private String adPublisherId;
    private String adTimePublished;
    private String adLocation;
    private String adName;
    private String adDescription;
    private String adCategory;
    private List<String> categoriesForSwap = new ArrayList<>();
    private String publisherPhotoUrl;
    private List<String> adPhotosList = new ArrayList<>();
    private String keyInsideCommonDb;

    public Advertisement() { }

    public boolean isLiked() {
        return liked;
    }

    public String getKeyInsideCommonDb() {
        return keyInsideCommonDb;
    }

    public void setKeyInsideCommonDb(String keyInsideCommonDb) {
        this.keyInsideCommonDb = keyInsideCommonDb;
    }

    public void setLiked(boolean liked) {
        this.liked = liked;
    }

    public String getPublisherPhotoUrl() {
        return publisherPhotoUrl;
    }

    public void setPublisherPhotoUrl(String publisherPhotoUrl) {
        this.publisherPhotoUrl = publisherPhotoUrl;
    }

    public List<String> getAdPhotosList() {
        return adPhotosList;
    }

    public void setAdPhotosList(List<String> adPhotosList) {
        this.adPhotosList = adPhotosList;
    }

    public String getAdPublisherName() {
        return adPublisherName;
    }

    public void setAdPublisherName(String adPublisherName) {
        this.adPublisherName = adPublisherName;
    }

    public String getAdPublisherId() {
        return adPublisherId;
    }

    public void setAdPublisherId(String adPublisherId) {
        this.adPublisherId = adPublisherId;
    }

    public String getAdTimePublished() {
        return adTimePublished;
    }

    public void setAdTimePublished(String adTimePublished) {
        this.adTimePublished = adTimePublished;
    }

    public String getAdLocation() {
        return adLocation;
    }

    public void setAdLocation(String adLocation) {
        this.adLocation = adLocation;
    }

    public String getAdName() {
        return adName;
    }

    public void setAdName(String adName) {
        this.adName = adName;
    }

    public String getAdDescription() {
        return adDescription;
    }

    public void setAdDescription(String adDescription) {
        this.adDescription = adDescription;
    }

    public String getAdCategory() {
        return adCategory;
    }

    public void setAdCategory(String adCategory) {
        this.adCategory = adCategory;
    }

    public List<String> getCategoriesForSwap() {
        return categoriesForSwap;
    }

    public void setCategoriesForSwap(List<String> categoriesForSwap) {
        this.categoriesForSwap = categoriesForSwap;
    }
}
