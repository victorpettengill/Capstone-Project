package br.com.victorpettengill.hawk_eyedcitizen.beans;

import com.firebase.geofire.GeoLocation;

/**
 * Created by appimagetech on 11/01/18.
 */

public class Problem {

    private String image;
    private String category;
    private String description;
    private GeoLocation geoLocation;

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public GeoLocation getGeoLocation() {
        return geoLocation;
    }

    public void setGeoLocation(GeoLocation geoLocation) {
        this.geoLocation = geoLocation;
    }
}
