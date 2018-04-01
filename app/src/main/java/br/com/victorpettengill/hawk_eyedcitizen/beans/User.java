package br.com.victorpettengill.hawk_eyedcitizen.beans;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Parcel;
import android.os.Parcelable;

import br.com.victorpettengill.hawk_eyedcitizen.application.HawkEyedCitizen;

/**
 * Created by appimagetech on 12/01/18.
 */

public class User implements Parcelable{

    private String uid;
    private String name;
    private String email;
    private String image;

    public User() {

    }

    public User(String uid) {
        this.uid = uid;
    }

    public User(String uid, String name, String email, String image) {
        this.uid = uid;
        this.name = name;
        this.email = email;
        this.image = image;
    }

    protected User(Parcel in) {
        uid = in.readString();
        name = in.readString();
        email = in.readString();
        image = in.readString();
    }

    public static final Creator<User> CREATOR = new Creator<User>() {
        @Override
        public User createFromParcel(Parcel in) {
            return new User(in);
        }

        @Override
        public User[] newArray(int size) {
            return new User[size];
        }
    };

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public void saveInstance() {

        SharedPreferences sharedPreferences = HawkEyedCitizen.getAppContext().getSharedPreferences(
                "userinfo",
                Context.MODE_PRIVATE);

        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("uid", uid);
        editor.putString("name", name);
        editor.putString("email", email);
        editor.putString("image", image);
        editor.apply();

    }

    public static User getInstance() {

        SharedPreferences sharedPreferences = HawkEyedCitizen.getAppContext().getSharedPreferences(
                "userinfo",
                Context.MODE_PRIVATE);

        User user = null;

        if(sharedPreferences.getString("uid", null) != null) {

            user = new User(
                    sharedPreferences.getString("uid", null),
                    sharedPreferences.getString("name", null),
                    sharedPreferences.getString("email", null),
                    sharedPreferences.getString("image", null)
            );

        }

        return user;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(uid);
        parcel.writeString(name);
        parcel.writeString(email);
        parcel.writeString(image);
    }
}