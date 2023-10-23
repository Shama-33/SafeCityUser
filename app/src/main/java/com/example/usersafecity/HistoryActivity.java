package com.example.usersafecity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;

public class HistoryActivity extends AppCompatActivity {

    private RelativeLayout rltvlayoutHistory;

    private EditText edttxtsearch_bar;
    private Button btnFilter;
    private TextView txtviewfilter;
    private RecyclerView RecyclerViewrv;
    FirebaseAuth firebaseAuth=FirebaseAuth.getInstance();

    private ArrayList<UserPhoto> photoArray;
    private AdapterHistory adapterHistory;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

       rltvlayoutHistory=findViewById(R.id.rltvlayoutHistory);
       edttxtsearch_bar=findViewById(R.id.edttxtsearch_bar);
        btnFilter=findViewById(R.id.btnFilter);
        txtviewfilter=findViewById(R.id.txtviewfilter);
        RecyclerViewrv=findViewById(R.id.RecyclerViewrv);

         loadinfo();









        edttxtsearch_bar.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                try{
                    adapterHistory.getFilter().filter(s);


                }catch(Exception e)
                {
                    e.printStackTrace();

                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        btnFilter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder= new AlertDialog.Builder(HistoryActivity.this);
                builder.setTitle("Choose Sorting Criteria")
                        .setItems(SortTypes.types1, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                String selected= SortTypes.types1[which];
                                txtviewfilter.setText(selected);
                                //spec=C;
                                if(selected.equals("No Sorting"))
                                {
                                    loadinfo();

                                }
                                else
                                {
                                    loadFildinfo(selected);
                                }


                            }
                        }).show();
            }
        });




    }

    private void loadFildinfo(String selected) {
        photoArray=new ArrayList<>();

        DatabaseReference reff=FirebaseDatabase.getInstance().getReference("UserPhotos");
        reff.child(firebaseAuth.getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                photoArray.clear();
                /*for(DataSnapshot ds: snapshot.getChildren())
                {
                    String pc=""+ds.child("docSpec").getValue().toString();
                    if (selected.equals(pc)) {
                        UserPhoto dox = ds.getValue(UserPhoto.class);
                        photoArray.add(dox);
                    }
                }*/
                for (DataSnapshot ds : snapshot.getChildren()) {
                    UserPhoto dox = ds.getValue(UserPhoto.class);
                    photoArray.add(dox);
                }

                if ("Date Ascending".equals(selected)) {
                    Collections.sort(photoArray, new Comparator<UserPhoto>() {
                        @Override
                        public int compare(UserPhoto photo1, UserPhoto photo2) {
                            // Parse the dates and compare
                            SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
                            try {
                                Date date1 = sdf.parse(photo1.getDate());
                                Date date2 = sdf.parse(photo2.getDate());
                                return date1.compareTo(date2);
                            } catch (ParseException e) {
                                e.printStackTrace();
                                return 0; // Handle date parsing error
                            }
                        }
                    });
                }

                else if ("Date Descending".equals(selected)) {
                    Collections.sort(photoArray, new Comparator<UserPhoto>() {
                        @Override
                        public int compare(UserPhoto photo1, UserPhoto photo2) {
                            // Parse the dates and compare in descending order
                            SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
                            try {
                                Date date1 = sdf.parse(photo1.getDate());
                                Date date2 = sdf.parse(photo2.getDate());
                                return date2.compareTo(date1);
                            } catch (ParseException e) {
                                e.printStackTrace();
                                return 0; // Handle date parsing error
                            }
                        }
                    });
                }

                else if ("Address Ascending".equals(selected)) {
                    Collections.sort(photoArray, new Comparator<UserPhoto>() {
                        @Override
                        public int compare(UserPhoto photo1, UserPhoto photo2) {
                            int cityComparison = photo1.getCity().compareTo(photo2.getCity());
                            if (cityComparison != 0) {
                                return cityComparison;
                            }
                            return photo1.getLocality().compareTo(photo2.getLocality());
                        }
                    });
                } else if ("Address Descending".equals(selected)) {
                    Collections.sort(photoArray, new Comparator<UserPhoto>() {
                        @Override
                        public int compare(UserPhoto photo1, UserPhoto photo2) {
                            int cityComparison = photo2.getCity().compareTo(photo1.getCity());
                            if (cityComparison != 0) {
                                return cityComparison;
                            }
                            return photo2.getLocality().compareTo(photo1.getLocality());
                        }
                    });
                }
                adapterHistory= new AdapterHistory(getApplicationContext(),photoArray);
                RecyclerViewrv.setAdapter(adapterHistory);
            }


            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void loadinfo() {
        photoArray=new ArrayList<>();

        DatabaseReference reff = FirebaseDatabase.getInstance().getReference("UserPhotos");
        //reff.orderByChild("accountType").equalTo("Provider")
                reff.child(firebaseAuth.getUid())
                .addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                photoArray.clear();
                for (DataSnapshot dataSnapshot:snapshot.getChildren())
                {
                    //String T=dataSnapshot.child("accountType").getValue().toString();
                    UserPhoto userPhoto = dataSnapshot.getValue(UserPhoto.class);
                    //if(!T.equalsIgnoreCase("GeneralUser"));
                    // {

                    photoArray.add(userPhoto);
                    //}

                }

                adapterHistory= new AdapterHistory(getApplicationContext(),photoArray);
                RecyclerViewrv.setAdapter(adapterHistory);



            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

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



}