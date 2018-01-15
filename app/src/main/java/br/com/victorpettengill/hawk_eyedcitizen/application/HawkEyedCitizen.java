package br.com.victorpettengill.hawk_eyedcitizen.application;

import android.app.Application;
import android.content.Context;

import com.google.firebase.FirebaseApp;

/**
 * Created by appimagetech on 15/01/18.
 */

public class HawkEyedCitizen extends Application {

    private static Context context;

    @Override
    public void onCreate() {
        super.onCreate();

        FirebaseApp.initializeApp(this);

        context = getApplicationContext();
    }


    public static Context getAppContext() {
        return context;
    }


}
