package br.com.victorpettengill.hawk_eyedcitizen.dao;

import android.graphics.Bitmap;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.util.Log;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.util.HashMap;

import br.com.victorpettengill.hawk_eyedcitizen.beans.Problem;
import br.com.victorpettengill.hawk_eyedcitizen.beans.User;
import br.com.victorpettengill.hawk_eyedcitizen.listeners.DaoListener;
import br.com.victorpettengill.hawk_eyedcitizen.utils.Utils;

/**
 * Created by victorfernandes on 06/02/18.
 */

public class ProblemDao {

    private static ProblemDao dao;
    private DatabaseReference reference;
    private DatabaseReference areaReference;
    private StorageReference storageReference;

    private final String PROBLEMS_REFERENCE = "Problems";
    private final String AREA_REFERENCE = "Area";

    public static ProblemDao getInstance() {

        if(dao == null) {
            dao = new ProblemDao();
        }

        return dao;
    }

    public ProblemDao() {

        reference = FirebaseDatabase.getInstance().getReference().child(PROBLEMS_REFERENCE);
        storageReference = FirebaseStorage.getInstance().getReference(PROBLEMS_REFERENCE);
        areaReference = FirebaseDatabase.getInstance().getReference().child(AREA_REFERENCE);

    }

    public void registerProblem(final User user,
                                final File image,
                                final String category,
                                final String description,
                                double latitude,
                                double longitude,
                                final DaoListener listener) {

        HashMap<String, Object> map = new HashMap<>();
        map.put("category", category);
        map.put("description", description);
        map.put("user", user.getUid());
        map.put("latitude", latitude);
        map.put("longitude", longitude);

        final GeoLocation geoLocation = new GeoLocation(latitude, longitude);

        final DatabaseReference problemReference = reference.push();
        problemReference.setValue(map, new OnCompleteListener() {
            @Override
            public void onComplete(@NonNull Task task) {

                if(task.isSuccessful()) {

                    if(image != null) {

                        storageReference.child(problemReference.getKey())
                                .putFile(Uri.fromFile(image))
                                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {

                                    @Override
                                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                                        if(taskSnapshot != null) {

                                            problemReference.child("image").setValue(taskSnapshot.getDownloadUrl().toString());

                                            Problem problem = new Problem();
                                            problem.setUid(problemReference.getKey());
                                            problem.setUser(user);
                                            problem.setImage(taskSnapshot.getDownloadUrl().toString());
                                            problem.setCategory(category);
                                            problem.setDescription(description);
                                            problem.setGeoLocation(geoLocation);

                                            listener.onSuccess(problem);

                                        } else {

                                            problemReference.removeValue();

                                            listener.onError("Error saving problem, try again later.");

                                        }

                                    }

                                });

                    } else {

                        Problem problem = new Problem();
                        problem.setUid(problemReference.getKey());
                        problem.setUser(user);
                        problem.setCategory(category);
                        problem.setDescription(description);
                        problem.setGeoLocation(geoLocation);

                        listener.onSuccess(problem);

                    }

                }

            }
        });


        GeoFire geoFire = new GeoFire(areaReference.child(problemReference.getKey()));
        geoFire.setLocation("geo", geoLocation, new GeoFire.CompletionListener() {

            @Override
            public void onComplete(String key, DatabaseError error) {

                if(error == null) {

                } else {


                }

            }

        });

    }

    public void getProblemsAtBounds(double latitude, double longitude) {

        final GeoLocation geoLocation = new GeoLocation(latitude, longitude);

        GeoFire geoFire = new GeoFire(reference);

        GeoQuery geoQuery = geoFire.queryAtLocation(geoLocation, 1000);
        geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {

            @Override
            public void onKeyEntered(String key, GeoLocation location) {

                Log.i("teste", key);

            }

            @Override
            public void onKeyExited(String key) {

            }

            @Override
            public void onKeyMoved(String key, GeoLocation location) {

            }

            @Override
            public void onGeoQueryReady() {

            }

            @Override
            public void onGeoQueryError(DatabaseError error) {

                Log.i("error", error.getMessage());

            }
        });

    }


}