package br.com.victorpettengill.hawk_eyedcitizen;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import br.com.victorpettengill.hawk_eyedcitizen.ui.LoginActivity;
import br.com.victorpettengill.hawk_eyedcitizen.ui.MainActivity;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        startActivity(new Intent(this, LoginActivity.class));

    }
}
