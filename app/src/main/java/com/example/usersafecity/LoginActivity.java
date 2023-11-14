package com.example.usersafecity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class LoginActivity extends AppCompatActivity  {


    private FirebaseAuth mAuth;

    private EditText emailin,passwordin;
    private TextView textViewin,forgotpass;
    private Button buttonin;

  String redirect="false";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);




        mAuth = FirebaseAuth.getInstance();

        this.setTitle("Login");

        redirect=getIntent().getStringExtra("finish");

       // getSupportActionBar().setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.acbar)));
        emailin=(EditText) findViewById(R.id.edttxtEmail);
        passwordin=(EditText) findViewById(R.id.edttxtPassword);
        buttonin= (Button) findViewById(R.id.btnSignin);
        forgotpass=findViewById(R.id.txtForgotPassword);
        textViewin=findViewById(R.id.txtSignUp);


        //buttonin.setOnClickListener(this);
        //forgotpass.setOnClickListener(this);

        textViewin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent= new Intent(getApplicationContext(),SignUpActivity.class);
                startActivity(intent);
                finish();
            }
        });

        buttonin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                InternetConnectionChecker connectionChecker = new InternetConnectionChecker(LoginActivity.this); // Replace 'this' with your activity or context
                if (connectionChecker.isInternetConnected()) {
                    userlogin();

                } else {
                    Toast.makeText(getApplicationContext(), "No Internet Connection", Toast.LENGTH_SHORT).show();
                }



            }
        });
        forgotpass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(),ForgotPasswordActivity.class);
                startActivity(intent);

            }
        });






    }



    private void userlogin() {


        String email=emailin.getText().toString().trim();
        String pass=passwordin.getText().toString().trim();

        //validity of email
        if(email.isEmpty())
        {
            emailin.setError("Enter email address");
            Toast.makeText(LoginActivity.this,"Invalid Email",Toast.LENGTH_SHORT).show();
            return;
        }
        if(!Patterns.EMAIL_ADDRESS.matcher(email).matches())
        {
            emailin.setError("Enter valid email address");
            Toast.makeText(LoginActivity.this,"Invalid Email",Toast.LENGTH_SHORT).show();
            emailin.requestFocus();
            return;
        }


        if(pass.isEmpty())
        {
            passwordin.setError("Enter a password");
            Toast.makeText(LoginActivity.this,"Enter Password",Toast.LENGTH_SHORT).show();
            return;
        }








        mAuth.signInWithEmailAndPassword(email,pass).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {

                //progressbarin.setVisibility(View.GONE);



                if(task.isSuccessful())
                {

                    FirebaseUser u=FirebaseAuth.getInstance().getCurrentUser();
                    String S=u.getUid();

                    DatabaseReference reff= FirebaseDatabase.getInstance().getReference().child("UserInfo");
                    //reff.orderByChild("userid").equalTo(S);
                    reff.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            UserData userData=new UserData();

                            if (snapshot.exists()) {
                                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                                    userData = dataSnapshot.getValue(UserData.class);
                                    if (userData != null) {
                                        String c = userData.getAccountType();
                                        String uid=userData.getUserid();
                                        //Toast.makeText(MainActivity.this, accountType, Toast.LENGTH_SHORT).show();

                                        if(c.equalsIgnoreCase("GeneralUser") && uid.equalsIgnoreCase(S))

                                        {
                                            if(FirebaseAuth.getInstance().getCurrentUser().isEmailVerified())
                                            {
                                                finish();//page wont be seen while returning
                                                Intent intent= new Intent(getApplicationContext(),UploadActivity.class);
                                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                                startActivity(intent);


                                            }
                                            else
                                            {
                                                Toast.makeText(LoginActivity.this, "Verify your email address before Login", Toast.LENGTH_SHORT).show();
                                                FirebaseAuth.getInstance().signOut();
                                                finish();
                                            }






                                        }

                                        else
                                        {
                                            Toast.makeText(LoginActivity.this, "You are not a registered user", Toast.LENGTH_SHORT).show();
                                            finish();//page wont be seen while returning
                                            FirebaseAuth.getInstance().signOut();
                                            finish();
                                            Intent intent= new Intent(LoginActivity.this,SignUpActivity.class);
                                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                            startActivity(intent);

                                        }


                                    }
                                }
                            }




                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                            FirebaseAuth.getInstance().signOut();
                            finish();
                            Toast.makeText(getApplicationContext(),"Login Unsuccessful,Data Not Found",Toast.LENGTH_SHORT).show();
                        }
                    });




                }
                else
                {
                    Toast.makeText(getApplicationContext(),"Login Unsuccessful",Toast.LENGTH_SHORT).show();
                }

            }
        });


    }




    @Override
    public void onStart() {

        super.onStart();
        FirebaseUser firebaseUser = mAuth.getCurrentUser();
        if(firebaseUser!=null)
        {
            /*
            String S=firebaseUser.getUid();
            DatabaseReference reff=FirebaseDatabase.getInstance().getReference().child("UserInfo").child(S);
            reff.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    UserData userData=new UserData();
                    //userData.AccountType=snapshot.child("accountType").getValue().toString();
                    userData.setAccountType(snapshot.child("accountType").getValue().toString());
                    String c=userData.getAccountType();
                    if(c.equalsIgnoreCase("GeneralUser"))
                    {
                        Intent intent = new Intent(getApplicationContext(),IndexActivity.class);
                        startActivity(intent);
                        finish();

                    }


                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });

             */


                    Intent intent = new Intent(getApplicationContext(),UploadActivity.class);
                    startActivity(intent);
                    finish();








        }
    }
}