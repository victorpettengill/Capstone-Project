package br.com.victorpettengill.hawk_eyedcitizen.dao;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;

import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginResult;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

import br.com.victorpettengill.hawk_eyedcitizen.beans.User;
import br.com.victorpettengill.hawk_eyedcitizen.listeners.DaoListener;
import br.com.victorpettengill.hawk_eyedcitizen.utils.Utils;

/**
 * Created by appimagetech on 12/01/18.
 */

public class UserDao {

    private final String USERS_REFERENCE = "Users";
    private static UserDao instance;
    private DatabaseReference reference;
    private StorageReference storageReference;

    public static UserDao getInstance() {

        if(instance == null) {
            instance = new UserDao();
        }

        return instance;
    }

    private UserDao() {

        reference = FirebaseDatabase.getInstance().getReference();
        storageReference = FirebaseStorage.getInstance().getReference(USERS_REFERENCE);

    }

    public void login(String email, String password, final DaoListener listener) {

        FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password)
                .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                    @Override
                    public void onSuccess(AuthResult authResult) {

                        getUserData(authResult.getUser().getUid(), listener);

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                        listener.onError(e.getLocalizedMessage());

                    }
                });

    }

    private void getUserData(final String uid, final DaoListener listener) {

        reference.child(USERS_REFERENCE)
                .child(uid)
                .addListenerForSingleValueEvent(new ValueEventListener() {

                    @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                if(dataSnapshot.getValue() != null) {

                    HashMap<String, Object> map = (HashMap<String, Object>) dataSnapshot.getValue();

                    User user = new User(
                            uid,
                            (String) map.get("name"),
                            (String) map.get("email"),
                            (String) map.get("image"));
                    listener.onSuccess(user);

                } else {

                    listener.onError("User not found!");

                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

                listener.onError("User not found!");

            }
        });

    }

    public void signUp(final Bitmap bitmap,
                       final String name,
                       final String email,
                       String password,
                       final DaoListener listener) {

        FirebaseAuth.getInstance()
                .createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener(new OnSuccessListener<AuthResult>() {

                    @Override
                    public void onSuccess(final AuthResult authResult) {

                        if(authResult.getUser() != null) {

                            if(bitmap != null) {

                                storageReference.child(authResult.getUser()
                                        .getUid())
                                        .putBytes(Utils.bitmapToBytes(bitmap))
                                        .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {

                                            @Override
                                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                                                if(taskSnapshot != null) {

                                                    registerUserReference(authResult.getUser().getUid(),
                                                            name,
                                                            email,
                                                            taskSnapshot.getDownloadUrl().toString(),
                                                            listener);

                                                }

                                            }

                                        });


                            } else {

                                registerUserReference(authResult.getUser().getUid(),
                                        name,
                                        email,
                                        null,
                                        listener);

                            }

                        }

                    }

                }).addOnFailureListener(new OnFailureListener() {

            @Override
            public void onFailure(@NonNull Exception e) {

                listener.onError(e.getLocalizedMessage());

            }

        });

    }

    private void registerUserReference(final String uid,
                                       final String name,
                                       final String email,
                                       final String imageUrl,
                                       final DaoListener listener) {

        HashMap<String, Object> map = new HashMap<>();
        map.put("name", name);
        map.put("email", email);
        map.put("image", imageUrl);

        reference.child(USERS_REFERENCE).child(uid).setValue(map)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {

                        User user = new User(uid, name, email, imageUrl);
                        listener.onSuccess(user);

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                        listener.onError(e.getLocalizedMessage());

                    }
                });

    }

    public void loginWithGoogle(final GoogleSignInAccount account, final DaoListener listener) {

        AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);

        FirebaseAuth.getInstance().signInWithCredential(credential)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {

                        if (task.isSuccessful()) {

                            registerUserReference(FirebaseAuth.getInstance().getCurrentUser().getUid(),
                                    account.getDisplayName(),
                                    account.getEmail(),
                                    account.getPhotoUrl() != null ? account.getPhotoUrl().toString() : null,
                                    listener);

                        } else {

                            listener.onError("Authentication failed.");

                        }

                    }
                });

    }

    public void loginWithFacebook(final LoginResult result, final DaoListener listener) {

        GraphRequest request = GraphRequest.newMeRequest(
                result.getAccessToken(),
                new GraphRequest.GraphJSONObjectCallback() {
                    @Override
                    public void onCompleted(
                            final JSONObject object,
                            GraphResponse response) {

                        if(object != null) {

                            Log.i("facebook response", object.toString());

                            AuthCredential credential = FacebookAuthProvider.getCredential(
                                    result.getAccessToken().getToken());
                            FirebaseAuth.getInstance().signInWithCredential(credential)
                                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {

                                        @Override
                                        public void onComplete(@NonNull Task<AuthResult> task) {
                                            if (task.isSuccessful()) {
                                                // Sign in success, update UI with the signed-in user's information

                                                try {
                                                    registerUserReference(
                                                            FirebaseAuth.getInstance().getCurrentUser().getUid(),
                                                            object.getString("name"),
                                                            object.getString("email"),
                                                            object.getJSONObject("picture").
                                                                    getJSONObject("data").
                                                                    getString("url"),
                                                            listener);

                                                } catch (JSONException e) {
                                                    e.printStackTrace();

                                                    listener.onError("Authentication failed!");

                                                }


                                            } else {

                                                listener.onError("Authentication failed!");

                                            }

                                        }

                                    });

                        } else {

                            listener.onError("Authentication failed!");

                        }

                    }
                });

        Bundle parameters = new Bundle();
        parameters.putString("fields", "id,name,email,picture");
        request.setParameters(parameters);
        request.executeAsync();

    }

}