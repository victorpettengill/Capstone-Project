package br.com.victorpettengill.hawk_eyedcitizen.ui;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;

import br.com.victorpettengill.hawk_eyedcitizen.R;
import br.com.victorpettengill.hawk_eyedcitizen.beans.User;
import br.com.victorpettengill.hawk_eyedcitizen.dao.UserDao;
import br.com.victorpettengill.hawk_eyedcitizen.listeners.DaoListener;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class RegisterActivity extends AppCompatActivity {

    @BindView(R.id.content) View content;
    @BindView(R.id.progress_bar) ProgressBar progressBar;
    @BindView(R.id.coordinator) CoordinatorLayout coordinator;
    @BindView(R.id.toolbar) Toolbar toolbar;
    @BindView(R.id.image) ImageView image;
    @BindView(R.id.name) TextInputLayout nameLayout;
    @BindView(R.id.name_input) TextInputEditText name;
    @BindView(R.id.email) TextInputLayout emailLayout;
    @BindView(R.id.email_input) TextInputEditText email;
    @BindView(R.id.password) TextInputLayout passwordLayout;
    @BindView(R.id.password_input) TextInputEditText password;
    @BindView(R.id.create_account) Button createAccount;

    private final int REQUEST_IMAGE_CAPTURE = 1;
    private Bitmap imageBitmap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_register);

        ButterKnife.bind(this);

        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

    }

    @OnClick(R.id.image) void onImageClicked() {

        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }

    }

    @OnClick(R.id.create_account) void onCreateAccountClicked() {

        if(validate()) {

            content.setVisibility(View.INVISIBLE);
            progressBar.setVisibility(View.VISIBLE);

            UserDao.getInstance().signUp(
                    imageBitmap,
                    name.getText().toString(),
                    email.getText().toString(),
                    password.getText().toString(),
                    new DaoListener() {
                        @Override
                        public void onSuccess(Object object) {

                            content.setVisibility(View.VISIBLE);
                            progressBar.setVisibility(View.INVISIBLE);

                            User user = (User) object;
                            user.saveInstance();

                            setResult(RESULT_OK);
                            finish();

                        }

                        @Override
                        public void onError(String message) {

                            content.setVisibility(View.VISIBLE);
                            progressBar.setVisibility(View.INVISIBLE);

                            Snackbar.make(coordinator, message, Snackbar.LENGTH_LONG).show();

                        }
                    });

        }

    }

    private boolean validate() {

        if(name.getText().toString().length() < 3) {
            nameLayout.setError(getString(R.string.name_error));
            name.requestFocus();
            return false;
        }

        if(!Patterns.EMAIL_ADDRESS.matcher(email.getText().toString()).matches()) {
            emailLayout.setError(getString(R.string.email_error));
            email.requestFocus();
            return false;
        }

        if(password.getText().toString().length() < 3) {
            passwordLayout.setError(getString(R.string.password_error));
            password.requestFocus();
            return false;
        }

        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();

            if (extras != null) {
                imageBitmap = (Bitmap) extras.get("data");
            }

            image.setImageBitmap(imageBitmap);
        }
    }
}
