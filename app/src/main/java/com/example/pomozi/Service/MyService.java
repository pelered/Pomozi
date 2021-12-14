package com.example.pomozi.Service;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

import com.example.pomozi.Adapter.ProfileMyAdapter;
import com.example.pomozi.MainActivity;
import com.example.pomozi.Model.Fav;
import com.example.pomozi.Model.ZivUpload;
import com.example.pomozi.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MyService extends Service {

    static boolean serviceAlive = true;
    NotificationManager mNotificationManager;
    NotificationCompat.Builder mNotificationBuilder;
    String NOTIFICATION_CHANNEL_ID = "My Channel ID";
    private final int CALL_DELAY = 10000;
    String personIdentity = "";
    //
    private HashMap<DatabaseReference, ValueEventListener> mListenerMap;
    //FirebaseDatabase database = FirebaseDatabase.getInstance();
    private DatabaseReference mRef;
    private Fav fav1;
    private String uid;
    private int count,privremeni_count;
    private ZivUpload ziv;

    public MyService() {
        super();
        System.out.println("****** [MyService] in (empty) constructor ...");
    }


    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        System.out.println("****** [MyService] in onStartCommand ...");
        super.onStartCommand(intent, flags, startId);
        //
        mRef=FirebaseDatabase.getInstance().getReference("Ziv");
        //uid=intent.getStringExtra("fid");
        SharedPreferences prefs = getSharedPreferences("shared_pref_name", Context.MODE_PRIVATE);
        uid=prefs.getString("uid",null);
        //

        System.out.println("****** [MyService] preparing notification resources...");
        mNotificationManager = getSystemService(NotificationManager.class);
        createNotificationChannel();
        count=0;
        mNotificationBuilder = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID);
        mNotificationBuilder.setSmallIcon(R.drawable.ic_launcher_foreground)
                .setLargeIcon(BitmapFactory.decodeResource(this.getResources(), R.drawable.ic_launcher_foreground))
                .setColor(Color.RED)
                .setContentTitle("Received data")
                // void pending intent:
                .setContentIntent(PendingIntent.getActivity(this, 0, new Intent(), 0))
                .setAutoCancel(true);

        serviceAlive = true;

        // Konkretni posao servisa:
        heavyWork();
        return START_STICKY;

    }


    private void createNotificationChannel() {
        // O+
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            CharSequence name = "my_channel";
            String Description = "This is my channel";
            int importance = NotificationManager.IMPORTANCE_LOW;
            NotificationChannel mChannel = new NotificationChannel(NOTIFICATION_CHANNEL_ID, name, importance);
            mChannel.setDescription(Description);
            mChannel.enableLights(true);
            mChannel.setShowBadge(false);
            mNotificationManager.createNotificationChannel(mChannel);
        }
    }


    // Konkretni posao servisa:
    public void heavyWork(){
        System.out.println("****** [MyService] in heavyWork: starting infinite loop...");
        DatabaseReference ref= FirebaseDatabase.getInstance().getReference("Fav").child(uid);
        //Log.d("FavF:",ref.toString());
        mListenerMap=new HashMap<>();
        privremeni_count=0;
//dohvati popis svi ziv sto pratis
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                fav1=dataSnapshot.getValue(Fav.class);
                if(fav1!=null) {
                    //Log.d("FavF:vr",fav1.toString());
                    //postavi_listener();
                    for(Map.Entry<String, String> entry :fav1.getFav().entrySet()){
                        ValueEventListener listener = new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                privremeni_count++;
                                Log.d("FavFcounter", String.valueOf(count));
                                Log.d("FavFprcounter", String.valueOf(privremeni_count));
                                if(count!=0 ) {
                                    Log.d("FavFpromjena", dataSnapshot.toString());
                                    ziv=dataSnapshot.getValue(ZivUpload.class);
                                    ziv.setKey(dataSnapshot.getKey());
                                    personIdentity=ziv.getGrad();
                                    String url=ziv.getUrl().get("0_key").toString();
                                    //doInBackground(url);
                                    DownloadImageTask dImageTask = new DownloadImageTask();
                                    dImageTask.execute(url);


                                   /* DownloadTaskZiv dTask = new DownloadTaskZiv();
                                    dTask.execute("url");*/
                                }
                                if (privremeni_count>=mListenerMap.size()){
                                    count++;
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }

                        };
                        //Log.d("FavFListene1",entry.getKey());
                        DatabaseReference childRef = mRef.child(entry.getValue());
                        //Log.d("FavFListene1",childRef.toString());
                        childRef.addValueEventListener(listener);
                        mListenerMap.put(childRef, listener);
                        //Log.d("FavFListene2",mListenerMap.toString());

                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.d("onCancelled:fav: ",databaseError.getMessage());
            }
        });

    }

    @Override
    public void onDestroy() {
        serviceAlive = false;
        System.out.println("****** [MyService] Deleting listeners...");
        for (Map.Entry<DatabaseReference, ValueEventListener> entry : mListenerMap.entrySet()) {
            DatabaseReference ref = entry.getKey();
            ValueEventListener listener = entry.getValue();
            ref.removeEventListener(listener);
        }
    }


    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }



    public void updateNotification(Bitmap b){
        Log.d("Service",ziv.toString());
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.putExtra("oznakan", ziv.getKey());
        intent.putExtra("notifikacija","not");

        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        System.out.println("****** [MyService] Image received, updating notification with name/surname and image...");
        if (mNotificationBuilder != null) {
            Notification newN = mNotificationBuilder
                    .setContentIntent(contentIntent)
                    .setContentTitle("Dogodila se promjena")
                    .setContentText(personIdentity)
                    .setLargeIcon(b)
                    .build();
            if (mNotificationManager != null) {
                mNotificationManager.notify(0, newN);
            }
        }
    }




    //////////////////////////////////////////////////////////////////////////////////////////////
    // Asinkroni zadatak za dohvat slike s doticnog url-a:
    @SuppressLint("StaticFieldLeak")
    private class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {

        DownloadImageTask() {
            System.out.println("****** [MyService] DownloadImageTask: in constructor...");
        }

        @Override
        protected Bitmap doInBackground(String... imageurl) {
            System.out.println("****** [MyService] DownloadImageTask in doInBackground: getting image...");
            Bitmap bitmap = null;
            OkHttpClient client = new OkHttpClient();
            Request request;

            try {
                request = new Request.Builder()
                        .url(new URL(imageurl[0]))
                        .get()
                        .build();

                Response response = client.newCall(request).execute();
                if (response.body() != null) {
                    InputStream inputStream = response.body().byteStream();
                    bitmap = BitmapFactory.decodeStream(inputStream);
                }
            } catch (Exception e) {
                System.out.println("****** [MyService] DownloadImageTask in doInBackground: error getting image...");
            }

            return bitmap;
        }

        @Override
        protected void onPostExecute(Bitmap resultImg) {
            System.out.println("****** [MyService] DownloadImageTask: in onPostExecute...");
            updateNotification(resultImg);
        }
    }

}