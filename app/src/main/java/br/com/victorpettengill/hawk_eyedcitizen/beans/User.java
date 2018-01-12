package br.com.victorpettengill.hawk_eyedcitizen.beans;

/**
 * Created by appimagetech on 12/01/18.
 */

public class User {

    public String uid;
    public String name;
    public String email;
    public String image;

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
}