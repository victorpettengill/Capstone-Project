package br.com.victorpettengill.hawk_eyedcitizen.dao;

import android.graphics.Bitmap;
import android.support.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.HashMap;

import br.com.victorpettengill.hawk_eyedcitizen.utils.Utils;

/**
 * Created by appimagetech on 12/01/18.
 */

public class UserDao {

    private static UserDao instance;
    private DatabaseReference reference;
    private StorageReference storageReference;

    public static UserDao getInstance() {

        if(instance == null) {
            instance = new UserDao();
        }

        return instance;
    }

    public UserDao() {

        reference = FirebaseDatabase.getInstance().getReference();
        storageReference = FirebaseStorage.getInstance().getReference("Users");

    }

    public void createUser(final Bitmap bitmap, String name, String email, String password) {

        FirebaseAuth.getInstance()
                .createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener(new OnSuccessListener<AuthResult>() {

            @Override
            public void onSuccess(AuthResult authResult) {

                if(authResult.getUser() != null) {

                    if(bitmap != null) {

                        storageReference.child(authResult.getUser()
                                .getUid())
                                .putBytes(Utils.bitmapToBytes(bitmap))
                                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {

                                    @Override
                                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                                        taskSnapshot.getDownloadUrl();

                                    }

                                });


                    }

                }

            }

        }).addOnFailureListener(new OnFailureListener() {

            @Override
            public void onFailure(@NonNull Exception e) {

            }

        });

    }

    private void registerUserReference(String uid, String name, String email, String imageUrl) {

        HashMap<String, Object> map = new HashMap<>();
        map.put("name", name);
        map.put("email", email);
        map.put("image", imageUrl);

        reference.child("Users").child(uid).setValue(map);


    }

}