package br.com.victorpettengill.hawk_eyedcitizen.beans;

import android.content.Context;
import android.content.SharedPreferences;

import br.com.victorpettengill.hawk_eyedcitizen.application.HawkEyedCitizen;

/**
 * Created by appimagetech on 12/01/18.
 */

public class User {

    private String uid;
    private String name;
    private String email;
    private String image;

    public User() {

    }

    public User(String uid, String name, String email, String image) {
        this.uid = uid;
        this.name = name;
        this.email = email;
        this.image = image;
    }

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

        return new User(
                sharedPreferences.getString("uid", null),
                sharedPreferences.getString("name", null),
                sharedPreferences.getString("email", null),
                sharedPreferences.getString("image", null)
        );
    }

}