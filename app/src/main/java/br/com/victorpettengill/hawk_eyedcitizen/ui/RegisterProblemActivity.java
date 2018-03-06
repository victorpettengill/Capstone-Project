package br.com.victorpettengill.hawk_eyedcitizen.ui;

import android.annotation.SuppressLint;
import android.content.ContentUris;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputEditText;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import br.com.victorpettengill.hawk_eyedcitizen.BuildConfig;
import br.com.victorpettengill.hawk_eyedcitizen.R;
import br.com.victorpettengill.hawk_eyedcitizen.beans.Problem;
import br.com.victorpettengill.hawk_eyedcitizen.beans.User;
import br.com.victorpettengill.hawk_eyedcitizen.dao.ProblemDao;
import br.com.victorpettengill.hawk_eyedcitizen.listeners.DaoListener;
import br.com.victorpettengill.hawk_eyedcitizen.utils.Utils;
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
    @BindView(R.id.coordinator) CoordinatorLayout coordinator;

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

        loading.setVisibility(View.VISIBLE);

        ProblemDao.getInstance().registerProblem(User.getInstance(),
                imageFile,
                categories.getSelectedItem().toString(),
                description.getText().toString(),
                latitude,
                longitude,
                new DaoListener() {
                    @Override
                    public void onSuccess(final Object object) {

                        loading.setVisibility(View.GONE);

                        new AlertDialog.Builder(RegisterProblemActivity.this)
                        .setMessage(R.string.success)
                        .setNeutralButton("Ok", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {

                                dialogInterface.dismiss();

                                Intent intent = new Intent();
                                intent.putExtra("problem", (Problem) object);

                                setResult(RESULT_OK, intent);
                                finish();



                            }
                        }).show();


                    }

                    @Override
                    public void onError(String message) {

                        loading.setVisibility(View.GONE);
                        Snackbar.make(coordinator, message, Snackbar.LENGTH_LONG).show();

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

            Uri selectedImageUri = Uri.fromFile(imageFile);

            selectedImagePath = getPath(RegisterProblemActivity.this, selectedImageUri);
            imagemEnvio = loadImage(selectedImagePath);
            imageFile = Utils.saveResizedImage(imagemEnvio);

            Picasso.with(this).load(imageFile).fit().centerCrop().into(image);

        } else if(requestCode == REQUEST_IMAGE_GALLERY && resultCode == RESULT_OK) {

            Uri selectedImageUri = data.getData();

            selectedImagePath = getPath(RegisterProblemActivity.this, selectedImageUri);
            imagemEnvio = loadImage(selectedImagePath);
            imageFile = Utils.saveResizedImage(imagemEnvio);

            Picasso.with(this).load(imageFile).fit().centerCrop().into(image);

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

        Intent takePictureIntent = new Intent(
                MediaStore.ACTION_IMAGE_CAPTURE);

        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            imageFile = null;
            try {
                imageFile = createImageFile();
            } catch (IOException ex) {
                Log.e("exception", "error creating image", ex);
            }

            if (imageFile != null) {
                Uri photoURI = FileProvider.getUriForFile(RegisterProblemActivity.this,
                        BuildConfig.APPLICATION_ID+".fileprovider",
                        imageFile);

                takePictureIntent.putExtra(
                        MediaStore.EXTRA_SCREEN_ORIENTATION,
                        ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT,
                        photoURI);
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            }

        }

    }

    private String selectedImagePath;
    private Bitmap imagemEnvio;

    private int hei;
    private int wid;

    private File imageFile;

    private String mCurrentPhotoPath;
    private byte[] imageBytes;

    private File createImageFile() throws IOException {

        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        mCurrentPhotoPath = image.getAbsolutePath();
        return image;
    }


    /**
     * Get a file path from a Uri. This will get the the path for Storage Access
     * Framework Documents, as well as the _data field for the MediaStore and
     * other file-based ContentProviders.
     *
     * @param context The context.
     * @param uri     The Uri to query.
     * @author paulburke
     */
    @SuppressLint("NewApi")
    public static String getPath(final Context context, final Uri uri) {

        final boolean isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;

        // DocumentProvider
        if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {
            // ExternalStorageProvider
            if (isExternalStorageDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                if ("primary".equalsIgnoreCase(type)) {
                    return Environment.getExternalStorageDirectory() + "/" + split[1];
                }

                // TODO handle non-primary volumes
            }
            // DownloadsProvider
            else if (isDownloadsDocument(uri)) {

                final String id = DocumentsContract.getDocumentId(uri);
                final Uri contentUri = ContentUris.withAppendedId(
                        Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));

                return getDataColumn(context, contentUri, null, null);
            }
            // MediaProvider
            else if (isMediaDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                Uri contentUri = null;
                if ("image".equals(type)) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(type)) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(type)) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }

                final String selection = "_id=?";
                final String[] selectionArgs = new String[]{
                        split[1]
                };

                return getDataColumn(context, contentUri, selection, selectionArgs);
            }
        }
        // MediaStore (and general)
        else if ("content".equalsIgnoreCase(uri.getScheme())) {

            // Return the remote address
            if (isGooglePhotosUri(uri))
                return uri.getLastPathSegment();

            return getDataColumn(context, uri, null, null);
        }
        // File
        else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }

        return null;
    }

    /**
     * Get the value of the data column for this Uri. This is useful for
     * MediaStore Uris, and other file-based ContentProviders.
     *
     * @param context       The context.
     * @param uri           The Uri to query.
     * @param selection     (Optional) Filter used in the query.
     * @param selectionArgs (Optional) Selection arguments used in the query.
     * @return The value of the _data column, which is typically a file path.
     */
    public static String getDataColumn(Context context, Uri uri, String selection,
                                       String[] selectionArgs) {

        Cursor cursor = null;
        final String column = "_data";
        final String[] projection = {
                column
        };

        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs,
                    null);
            if (cursor != null && cursor.moveToFirst()) {
                final int index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(index);
            }
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return null;
    }


    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is ExternalStorageProvider.
     */
    public static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is DownloadsProvider.
     */
    public static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is MediaProvider.
     */
    public static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is Google Photos.
     */
    public static boolean isGooglePhotosUri(Uri uri) {
        return "com.google.android.apps.photos.content".equals(uri.getAuthority());
    }

    private Bitmap loadImage(String path) {

        File f = new File(path);

        Bitmap retorno = null;

        try {
            retorno = openImage(Uri.fromFile(f), true);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return retorno;
    }

    private Bitmap openImage(Uri uri, boolean resample) throws IOException {

        File f = new File(uri.getPath());
        ExifInterface exif = new ExifInterface(f.getPath());
        int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION,
                ExifInterface.ORIENTATION_NORMAL);

        int angle = 0;

        if (orientation == ExifInterface.ORIENTATION_ROTATE_90) {
            angle = 90;
        } else if (orientation == ExifInterface.ORIENTATION_ROTATE_180) {
            angle = 180;
        } else if (orientation == ExifInterface.ORIENTATION_ROTATE_270) {
            angle = 270;
        }

        Matrix mat = new Matrix();
        mat.postRotate(angle);

        BitmapFactory.Options o = new BitmapFactory.Options();
        o.inTempStorage = new byte[8 * 1024];

        Bitmap bmp = BitmapFactory.decodeFile(f.getPath(), o);
        Bitmap correctBmp = Bitmap.createBitmap(bmp, 0, 0, bmp.getWidth(),
                bmp.getHeight(), mat, true);

        return correctBmp;
    }

}
