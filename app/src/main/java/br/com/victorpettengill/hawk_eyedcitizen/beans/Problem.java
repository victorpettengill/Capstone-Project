package br.com.victorpettengill.hawk_eyedcitizen.beans;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.HashMap;

/**
 * Created by appimagetech on 11/01/18.
 */

public class Problem implements Parcelable{

    public static final Creator<Problem> CREATOR = new Creator<Problem>() {
        @Override
        public Problem createFromParcel(Parcel in) {
            return new Problem(in);
        }

        @Override
        public Problem[] newArray(int size) {
            return new Problem[size];
        }
    };
    private String uid;
    private User user;
    private String image;
    private String category;
    private String description;
    private double latitude;
    private double longitude;
    private boolean solved;
    private long clapsCount;
    private int solvedCount;
    private HashMap<String, Boolean> claps;
    private HashMap<String, Boolean> solvedMap;

    public Problem() {

    }

    public Problem(String uid) {
        this.uid = uid;
    }

    protected Problem(Parcel in) {
        uid = in.readString();
        image = in.readString();
        category = in.readString();
        description = in.readString();
        latitude = in.readDouble();
        longitude = in.readDouble();
        solved = in.readInt() == 1;
        clapsCount = in.readLong();
        solvedCount = in.readInt();

        user = new User(in.readString());

    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

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

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(uid);
        parcel.writeString(image);
        parcel.writeString(category);
        parcel.writeString(description);
        parcel.writeDouble(latitude);
        parcel.writeDouble(longitude);
        parcel.writeInt(solved ? 1 : 0);
        parcel.writeLong(clapsCount);
        parcel.writeInt(solvedCount);

        if(user != null) {
            parcel.writeString(user.getUid());
        }
    }

}
