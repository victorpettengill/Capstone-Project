package br.com.victorpettengill.hawk_eyedcitizen.ui;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputEditText;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import br.com.victorpettengill.hawk_eyedcitizen.R;
import br.com.victorpettengill.hawk_eyedcitizen.beans.User;
import br.com.victorpettengill.hawk_eyedcitizen.dao.ProblemDao;
import br.com.victorpettengill.hawk_eyedcitizen.listeners.DaoListener;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class RegisterProblemActivity extends AppCompatActivity {

    @BindView(R.id.image) ImageView image;
    @BindView(R.id.category) Spinner categories;
    @BindView(R.id.description) TextInputEditText description;
    @BindView(R.id.location) Button location;
    @BindView(R.id.other_address) TextView otherAddress;
    @BindView(R.id.loading) LinearLayout loading;

    private final int REQUEST_IMAGE_CAPTURE = 1;
    private final int REQUEST_IMAGE_GALLERY = 3;
    private final int READ_PERMISSION = 5;
    private final int REQUEST_OTHER_LOCATION = 8;

    private double latitude;
    private double longitude;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_problem);

        ButterKnife.bind(this);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        if(getIntent().getExtras() != null) {

            latitude = getIntent().getDoubleExtra("latitude", 0);
            longitude = getIntent().getDoubleExtra("longitude", 0);

        }

    }

    @OnClick(R.id.location) void onLocationClicked() {

        AlertDialog.Builder builder = new AlertDialog.Builder(RegisterProblemActivity.this);
        builder.setTitle(R.string.location_title);
        builder.setItems(R.array.location_options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

                if(i == 1) {

                    Intent intent = new Intent(RegisterProblemActivity.this, FindLocationActivity.class);
                    startActivityForResult(intent, REQUEST_OTHER_LOCATION);

                } else {

                }

            }
        });
        builder.show();

    }

    private void validate() {

        if(categories.getSelectedItemPosition() == 0) {
            return;
        }

        if(description.getText().toString().equals("")) {

        }
    }

    private void saveProblem() {

        image.setDrawingCacheEnabled(true);
        image.buildDrawingCache();
        Bitmap imageBitmap = image.getDrawingCache();

        loading.setVisibility(View.VISIBLE);

        ProblemDao.getInstance().registerProblem(User.getInstance(),
                imageBitmap,
                categories.getSelectedItem().toString(),
                description.getText().toString(),
                latitude,
                longitude,
                new DaoListener() {
                    @Override
                    public void onSuccess(Object object) {

                        loading.setVisibility(View.GONE);

                    }

                    @Override
                    public void onError(String message) {

                        loading.setVisibility(View.GONE);

                    }
                });

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if(item.getItemId() == R.id.save) {

            saveProblem();

            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.menu_done, menu);

        return true;
    }

    @OnClick(R.id.image) void onImageClicked() {

        AlertDialog.Builder selectImageDialog = new AlertDialog.Builder(this);
        selectImageDialog.setTitle(R.string.select_image);
        selectImageDialog.setItems(
                R.array.image_options,

                new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                        switch (i) {

                            case 0:
                                choosePhotoFromGallery();
                                break;

                            case 1:
                                takePhotoFromCamera();
                                break;

                        }

                    }

                });
        selectImageDialog.show();

    }

    private void choosePhotoFromGallery() {

        if (ContextCompat.checkSelfPermission(RegisterProblemActivity.this,
                android.Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(RegisterProblemActivity.this,
                    android.Manifest.permission.READ_EXTERNAL_STORAGE)) {

            } else {

                ActivityCompat.requestPermissions(RegisterProblemActivity.this,
                        new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE},
                        READ_PERMISSION);

            }

        } else {

            Intent galleryIntent = new Intent(Intent.ACTION_PICK,
                    android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);

            if (galleryIntent.resolveActivity(getPackageManager()) != null) {
                startActivityForResult(galleryIntent, REQUEST_IMAGE_GALLERY);
            }

        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {

            Uri selectedImageUri = data.getData();

            if (selectedImageUri == null) {
                selectedImageUri = Uri.parse(data.getAction());
            }

            Picasso.with(this).load(selectedImageUri).fit().centerCrop().into(image);

        } else if(requestCode == REQUEST_IMAGE_GALLERY && resultCode == RESULT_OK) {

            Uri selectedImageUri = data.getData();

            if (selectedImageUri == null) {
                selectedImageUri = Uri.parse(data.getAction());
            }

            Picasso.with(this).load(selectedImageUri).fit().centerCrop().into(image);

        } else if(requestCode == REQUEST_OTHER_LOCATION && resultCode == RESULT_OK) {

            location.setText(getString(R.string.title_activity_find_location));
            otherAddress.setText(data.getStringExtra("location"));
            latitude = data.getDoubleExtra("latitude", 0);
            longitude = data.getDoubleExtra("longitude", 0);

        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if(requestCode == READ_PERMISSION && grantResults.length > 0) {

            Intent galleryIntent = new Intent(Intent.ACTION_PICK,
                    android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);

            if (galleryIntent.resolveActivity(getPackageManager()) != null) {
                startActivityForResult(galleryIntent, REQUEST_IMAGE_GALLERY);
            }

        }

    }

    private void takePhotoFromCamera() {

        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }

    }

}
