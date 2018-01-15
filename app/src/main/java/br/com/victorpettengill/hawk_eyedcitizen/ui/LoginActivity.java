package br.com.victorpettengill.hawk_eyedcitizen.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.SignInButton;

import br.com.victorpettengill.hawk_eyedcitizen.R;
import br.com.victorpettengill.hawk_eyedcitizen.dao.UserDao;
import br.com.victorpettengill.hawk_eyedcitizen.listeners.DaoListener;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class LoginActivity extends AppCompatActivity {

    private final int CREATE_ACCOUNT = 19;
    private final int RC_SIGN_IN = 7;

    @BindView(R.id.content) View content;
    @BindView(R.id.progress_bar) ProgressBar progressBar;
    @BindView(R.id.email) TextInputLayout emailLayout;
    @BindView(R.id.email_input)TextInputEditText email;
    @BindView(R.id.password) TextInputLayout passwordLayout;
    @BindView(R.id.password_input) TextInputEditText password;
    @BindView(R.id.login) Button login;
    @BindView(R.id.facebook_login) LoginButton facebookLogin;

    private GoogleSignInOptions gso;
    private GoogleSignInClient googleSignInClient;
    private CallbackManager callbackManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        ButterKnife.bind(this);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        initGoogleSignIn();
        initFacebookLogin();

    }

    private void initFacebookLogin() {

        callbackManager = CallbackManager.Factory.create();

        facebookLogin.setReadPermissions("email", "public_profile");
        facebookLogin.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {

            @Override
            public void onSuccess(LoginResult loginResult) {

            }

            @Override
            public void onCancel() {

            }

            @Override
            public void onError(FacebookException error) {

            }
        });

    }

    private void initGoogleSignIn() {

        gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.server_id_token))
                .requestEmail()
                .requestProfile()
                .build();

        googleSignInClient = GoogleSignIn.getClient(this, gso);

    }

    @OnClick(R.id.login) void onLoginClicked() {

    }

    @OnClick(R.id.create_account) void onRegisterClicked() {

        startActivityForResult(
                new Intent(this, RegisterActivity.class),
                CREATE_ACCOUNT);

    }

    @OnClick(R.id.google_signin) void onGoogleLoginClicked() {

        showLoading();

        Intent signInIntent = googleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        callbackManager.onActivityResult(requestCode, resultCode, data);

        if(requestCode == CREATE_ACCOUNT && resultCode == RESULT_OK) {

            Intent i = new Intent(this, MainActivity.class);
            startActivity(i);

        } else if(requestCode == RC_SIGN_IN) {

            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            if (result.isSuccess()) {

                GoogleSignInAccount account = result.getSignInAccount();

                handleGoogleLogin(account);

            } else {

                hideLoading();

            }

        }

    }

    private void hideLoading() {

        progressBar.setVisibility(View.INVISIBLE);
        content.setVisibility(View.VISIBLE);

    }

    private void showLoading() {

        progressBar.setVisibility(View.VISIBLE);
        content.setVisibility(View.INVISIBLE);

    }

    private void handleGoogleLogin(GoogleSignInAccount account) {

        UserDao.getInstance().loginWithGoogle(account, new DaoListener() {
            @Override
            public void onSuccess(Object object) {

                finish();

                Intent i = new Intent(LoginActivity.this, MainActivity.class);
                startActivity(i);

            }

            @Override
            public void onError(String message) {

                progressBar.setVisibility(View.INVISIBLE);
                content.setVisibility(View.VISIBLE);

                Snackbar.make(content, message, Snackbar.LENGTH_LONG).show();

            }
        });

    }

}
