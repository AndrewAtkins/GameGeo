package com.android.gamegeo;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.mongodb.stitch.android.core.Stitch;
import com.mongodb.stitch.android.core.StitchAppClient;
import com.mongodb.stitch.android.core.auth.StitchUser;
import com.mongodb.stitch.android.services.mongodb.remote.RemoteMongoClient;
import com.mongodb.stitch.android.services.mongodb.remote.RemoteMongoCollection;
import com.mongodb.stitch.core.auth.providers.anonymous.AnonymousCredential;

import org.bson.Document;

public class App extends Application {
//    public RemoteMongoCollection<Document> pictionaryCollection;
    public RemoteMongoClient mongoClient;
    @Override
    public void onCreate() {
        super.onCreate();

    Stitch.initializeDefaultAppClient(getResources().getString(R.string.my_app_id));
    final StitchAppClient stitchAppClient = Stitch.getDefaultAppClient();
//        final StitchAppClient stitchAppClient =
//                Stitch.initializeDefaultAppClient(getResources().getString(R.string.my_app_id));
    stitchAppClient.getAuth().loginWithCredential(new AnonymousCredential())
            .addOnCompleteListener(new OnCompleteListener<StitchUser>() {
                @Override
                public void onComplete(@NonNull Task<StitchUser> task) {
                    if(task.isSuccessful())
                    {
                        Log.d("stitch", "logged in anonymously");
                    } else {
                        Log.e("stitch", "failed to log in anonymously", task.getException());
                    }
                }
            });

        mongoClient =
                stitchAppClient.getServiceClient(RemoteMongoClient.factory, "mongodb-atlas");
    }

    public RemoteMongoClient getMongoClient() {
        return mongoClient;
    }
}
