package br.com.victorpettengill.hawk_eyedcitizen.listeners;

/**
 * Created by appimagetech on 15/01/18.
 */

public abstract class DaoListener {

    public void onSuccess(Object object){}
    public void onError(String message){}
    public void onObjectAdded(Object object){}

}