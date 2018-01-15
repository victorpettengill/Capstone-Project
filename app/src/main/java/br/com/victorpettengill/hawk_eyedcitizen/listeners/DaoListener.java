package br.com.victorpettengill.hawk_eyedcitizen.listeners;

/**
 * Created by appimagetech on 15/01/18.
 */

public interface DaoListener {

    void onSuccess(Object object);
    void onError(String message);

}