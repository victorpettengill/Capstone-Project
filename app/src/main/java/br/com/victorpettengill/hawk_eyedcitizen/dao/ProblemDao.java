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
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;
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
    private final String PROBLEMS_REFERENCE = "Problems";
    private final String AREA_REFERENCE = "Area";
    private DatabaseReference reference;
    private DatabaseReference areaReference;
    private StorageReference storageReference;

    public ProblemDao() {

        reference = FirebaseDatabase.getInstance().getReference().child(PROBLEMS_REFERENCE);
        storageReference = FirebaseStorage.getInstance().getReference(PROBLEMS_REFERENCE);
        areaReference = FirebaseDatabase.getInstance().getReference().child(AREA_REFERENCE);

    }

    public static ProblemDao getInstance() {

        if(dao == null) {
            dao = new ProblemDao();
        }

        return dao;
    }

    public void getDataForProblem(final Problem problem, final DaoListener listener) {

        reference.child(problem.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                HashMap<String, Object> valueMap = (HashMap<String, Object>) dataSnapshot.getValue();

                problem.setCategory((String) valueMap.get("category"));
                problem.setDescription((String) valueMap.get("description"));
                problem.setUser(new User((String) valueMap.get("user")));
                problem.setImage(valueMap.containsKey("image") ? (String) valueMap.get("image") : null);

                listener.onSuccess(problem);

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

                listener.onError(databaseError.getMessage());

            }

        });

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
        problemReference.updateChildren(map, new DatabaseReference.CompletionListener() {

            @Override
            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {

                if(databaseError == null) {

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
                                            problem.setLatitude(geoLocation.latitude);
                                            problem.setLongitude(geoLocation.longitude);

                                            listener.onSuccess(problem);

                                        } else {

                                            problemReference.removeValue();

                                            listener.onError("Error saving problem, try again later.");

                                        }

                                    }

                                })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {

                                problemReference.removeValue();

                                listener.onError("Error saving problem, try again later.");

                            }
                        });

                    } else {

                        Problem problem = new Problem();
                        problem.setUid(problemReference.getKey());
                        problem.setUser(user);
                        problem.setCategory(category);
                        problem.setDescription(description);
                        problem.setLatitude(geoLocation.latitude);
                        problem.setLongitude(geoLocation.longitude);

                        listener.onSuccess(problem);

                    }

                } else {
                    listener.onError("Error saving problem, try again later.");
                }

            }

        });


        GeoFire geoFire = new GeoFire(areaReference);
        geoFire.setLocation(problemReference.getKey(), geoLocation, new GeoFire.CompletionListener() {

            @Override
            public void onComplete(String key, DatabaseError error) {

                if(error == null) {

                } else {


                }

            }

        });

    }

    public void getProblemsAtBounds(double latitude, double longitude, final DaoListener listener) {

        final GeoLocation geoLocation = new GeoLocation(latitude, longitude);

        GeoFire geoFire = new GeoFire(areaReference);

        GeoQuery geoQuery = geoFire.queryAtLocation(geoLocation, 1000);
        geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {

            @Override
            public void onKeyEntered(String key, GeoLocation location) {

                Problem problem = new Problem(key);
                problem.setLatitude(location.latitude);
                problem.setLongitude(location.longitude);

                listener.onObjectAdded(problem);


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

                listener.onError(error.getMessage());

            }
        });

    }

    public void clapProblem(Problem problem, final User user, final DaoListener listener) {

        reference.child(problem.getUid()).runTransaction(new Transaction.Handler() {

            @Override
            public Transaction.Result doTransaction(MutableData mutableData) {

                if(mutableData == null){
                    return Transaction.success(mutableData);
                }

                HashMap<String, Object> map = (HashMap<String, Object>) mutableData.getValue();

                if(map.containsKey("claps")) {

                    HashMap<String, Boolean> claps = (HashMap<String, Boolean>) map.get("claps");

                    if(claps.containsKey(user.getUid())) {

                        claps.remove(user.getUid());

                        long clapCount = (long) map.get("clapsCount") - 1;
                        map.put("clapsCount", clapCount);

                    } else {

                        claps.put(user.getUid(), true);
                        long clapCount = (long) map.get("clapsCount") + 1;
                        map.put("clapsCount", clapCount);

                    }

                } else {

                    HashMap<String, Boolean> claps = new HashMap<String, Boolean>();
                    claps.put(user.getUid(), true);

                    map.put("claps", claps);
                    map.put("clapsCount", 1);

                }

                mutableData.setValue(map);

                return Transaction.success(mutableData);
            }

            @Override
            public void onComplete(DatabaseError databaseError, boolean b, DataSnapshot dataSnapshot) {

                if(databaseError == null) {
                    listener.onSuccess(null);
                } else {
                    listener.onError(databaseError.getMessage());
                }

            }

        });

    }

    public void solveProblem(Problem problem, final User user, final DaoListener listener) {

        if(user.getUid().equals(problem.getUser().getUid())) {

            reference.child(problem.getUid()).child("solved").setValue(true);

            areaReference.child(problem.getUid()).removeValue(new DatabaseReference.CompletionListener() {

                @Override
                public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {

                    if(databaseError == null) {

                        listener.onSuccess("Problem solved successfully");

                    } else {

                        listener.onError(databaseError.getMessage());

                    }

                }

            });

        } else {

            reference.child(problem.getUid()).runTransaction(new Transaction.Handler() {

                @Override
                public Transaction.Result doTransaction(MutableData mutableData) {

                    if(mutableData == null){
                        return Transaction.success(mutableData);
                    }

                    HashMap<String, Object> map = (HashMap<String, Object>) mutableData.getValue();

                    if(map.containsKey("solvedMap")) {

                        HashMap<String, Boolean> solvedMap = (HashMap<String, Boolean>) map.get("solvedMap");

                        if(solvedMap.containsKey(user.getUid())) {

                            solvedMap.remove(user.getUid());

                            long clapCount = (long) map.get("solvedCount") - 1;
                            map.put("solvedCount", clapCount);

                        } else {

                            solvedMap.put(user.getUid(), true);
                            long clapCount = (long) map.get("solvedCount") + 1;
                            map.put("solvedCount", clapCount);

                        }

                    } else {

                        HashMap<String, Boolean> solvedMap = new HashMap<String, Boolean>();
                        solvedMap.put(user.getUid(), true);

                        map.put("solvedMap", solvedMap);
                        map.put("solvedCount", 1);

                    }

                    mutableData.setValue(map);

                    return Transaction.success(mutableData);
                }

                @Override
                public void onComplete(DatabaseError databaseError, boolean b, DataSnapshot dataSnapshot) {

                    if(databaseError == null) {
                        listener.onSuccess(null);
                    } else {
                        listener.onError(databaseError.getMessage());
                    }

                }

            });

        }

    }

}