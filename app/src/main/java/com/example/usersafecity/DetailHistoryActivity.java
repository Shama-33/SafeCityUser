package com.example.usersafecity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

public class DetailHistoryActivity extends AppCompatActivity {
    String photoid;
    TextView historyupdate,historyuptime,historystatus,htextaddress,htextlocality,htextcode,htextstate,htextdistrict,htextcountry,txtviewdescription,txtInternetStatusDetailHistory;
    ImageView histimgML;
    private NetworkChangeReceiver networkChangeReceiver;
    private BroadcastReceiver networkChangeBroadcastReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_history);
        photoid= getIntent().getStringExtra("PHOTOID");
        //Toast.makeText(this, photoid, Toast.LENGTH_SHORT).show();

        historyupdate=findViewById(R.id.historyupdate);
        historyuptime=findViewById(R.id.historyuptime);
        historystatus=findViewById(R.id.historystatus);
        htextaddress=findViewById(R.id.htextaddress);
        htextlocality=findViewById(R.id.htextlocality);
        htextcode=findViewById(R.id.htextcode);
        htextstate=findViewById(R.id.htextstate);
        htextdistrict=findViewById(R.id.htextdistrict);
        htextcountry=findViewById(R.id.htextcountry);
        txtviewdescription=findViewById(R.id.txtviewdescription);
        txtInternetStatusDetailHistory=findViewById(R.id.txtInternetStatusDetailHistory);




        histimgML=findViewById(R.id.histimgML);




        if (!isNetworkConnected()) {
            showNoInternetUI();
        } else {
            hideNoInternetUI();
        }

        // Register the network change receiver
        networkChangeReceiver = new NetworkChangeReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(new NetworkChangeReceiver(), intentFilter);


        networkChangeBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                boolean isConnected = intent.getBooleanExtra("isConnected", false);
                if (isConnected) {
                    // Network connected

                    showNoInternetUI();
                } else {
                    // Network disconnected
                    hideNoInternetUI();

                }
            }
        };
        LocalBroadcastManager.getInstance(this)
                .registerReceiver(networkChangeBroadcastReceiver, new IntentFilter(NetworkChangeReceiver.NETWORK_CHANGE_ACTION));




        String UID= FirebaseAuth.getInstance().getUid();
       if(photoid != null && UID != null)
       {
           DatabaseReference dref= FirebaseDatabase.getInstance().getReference("UserPhotos").child(UID);
           dref.orderByChild("photoid").equalTo(photoid)
           //dref.child(photoid)
                   .addValueEventListener(new ValueEventListener() {
               @Override
               public void onDataChange(@NonNull DataSnapshot snapshot) {
                   if(snapshot.exists())
                   {
                       for(DataSnapshot ds : snapshot.getChildren())
                       {
                           String area,ct,div,dist,post,con,desc,status,date,time;
                           UserPhoto userPhoto=ds.getValue(UserPhoto.class);
                           if (userPhoto == null)
                           {
                               return;
                           }
                           area=userPhoto.getLocality();
                           ct=userPhoto.getCity();
                           div=userPhoto.getDivision();
                           date=userPhoto.getDate();
                           dist=userPhoto.getDistrict();
                           post=userPhoto.getPincode();
                           con=userPhoto.getCountry();
                           time=userPhoto.getTime();
                           desc=userPhoto.getDescription();
                           status=userPhoto.getStatus();



                           historyupdate.setText(date);
                           historyuptime.setText(time);
                           historystatus.setText(status);
                           htextaddress.setText(area);
                           htextlocality.setText(ct);
                           htextcode.setText(post);
                           htextstate.setText(div);
                           htextdistrict.setText(dist);
                           htextcountry.setText(con);
                           txtviewdescription.setText(desc);

                           String  imagepath=userPhoto.getImagepath();
                           if (imagepath != null) {
                               try {
                                   Picasso.with(getApplicationContext()).load(imagepath).into(histimgML);
                               }catch  (Exception e){

                                   e.printStackTrace(); // Log the exception for debugging
                                   Toast.makeText(DetailHistoryActivity.this, "Error Loading Image", Toast.LENGTH_SHORT).show();

                               }

                           }





                       }
                   }

               }

               @Override
               public void onCancelled(@NonNull DatabaseError error) {

               }
           });

       }

    }

    @Override
    public boolean onCreateOptionsMenu (Menu menu)
    {
        getMenuInflater().inflate(R.menu.content_menu,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId()==R.id.SignOutMenuId)
        {

            FirebaseAuth.getInstance().signOut();
            finish();
            Intent intent=new Intent(getApplicationContext(),LoginActivity.class);
            startActivity(intent);
        }
        else if (item.getItemId()==R.id.ProfileMenuId)
        {
            Intent i=new Intent(getApplicationContext(),ProfileActivity.class);
            startActivity(i);
        }
        else if (item.getItemId()==R.id.HistoryMenuId)
        {
            Intent i=new Intent(getApplicationContext(),HistoryActivity.class);
            startActivity(i);

        }
        else if (item.getItemId()==R.id.HomeMenuId)
        {
            finish();
            Intent i = new Intent(getApplicationContext(),UploadActivity.class);
            startActivity(i);


        }
        return super.onOptionsItemSelected(item);
    }


    private void showNoInternetUI() {
        txtInternetStatusDetailHistory.setVisibility(View.VISIBLE);
        //btnProfileUpdate.setEnabled(false); // Disable the upload button
    }

    private void hideNoInternetUI() {
        txtInternetStatusDetailHistory.setVisibility(View.GONE);
        // btnProfileUpdate.setEnabled(true); // Enable the upload button
    }

    private boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm != null) {
            NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
            return activeNetwork != null && activeNetwork.isConnected();
        }
        return false;
    }

    @Override
    protected void onStart() {
        super.onStart();

        // Check the internet connection status again when the activity is started
        if (!isNetworkConnected()) {
            showNoInternetUI();
        } else {
            hideNoInternetUI();
        }
    }
    @Override
    protected void onResume()
    {
        super.onResume();
        if (!isNetworkConnected()) {
            showNoInternetUI();
        } else {
            hideNoInternetUI();
        }
    }
    @Override
    protected void onPause() {
        super.onPause();
        // Unregister the network change receiver to avoid unnecessary updates
        unregisterReceiver(networkChangeReceiver);
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Unregister the network change receiver and local broadcast receiver
        unregisterReceiver(networkChangeReceiver);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(networkChangeBroadcastReceiver);
    }

    @Override
    public void onBackPressed() {
        // Instead of calling super.onBackPressed(), start HomeActivity
        Intent intent = new Intent(DetailHistoryActivity.this, UploadActivity.class);
        startActivity(intent);
        finish(); // Optional: finish the current activity if you don't want to keep it in the back stack
    }
}