package com.example.chillchat;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class SignUp extends AppCompatActivity {

    EditText edSignName,edSignEmail,edSignPass,edSignRePass;
    Button btnSignUp,googleSignin;
    TextView tvLogin;
    ProgressBar progbar;
    FirebaseAuth fAuth;
    DatabaseReference dRef;
    HashMap<String,String>map;
    ProgressDialog progDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        // progress dialog initialize
        progDialog=new ProgressDialog(SignUp.this);
        progDialog.setMessage("Setting up your account...");
        progDialog.setCancelable(false);

        // find id's
        edSignEmail=findViewById(R.id.edSignEmail);
        edSignName=findViewById(R.id.edSignName);
        edSignPass=findViewById(R.id.edSignPass);
        edSignRePass=findViewById(R.id.edSignRePass);
        btnSignUp=findViewById(R.id.btnSignUp);
        tvLogin=findViewById(R.id.tvLogin);
        progbar=findViewById(R.id.progbar);

        // get instance of authentication
        fAuth=FirebaseAuth.getInstance();
        // get reference
        dRef=FirebaseDatabase.getInstance().getReference();


        // go to login page  click event
        tvLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent myIntent=new Intent(SignUp.this,Login.class);
                myIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK |Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(myIntent);
                finish();
            }
        });


        // click event on signup
        btnSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //take all the input
                String name=edSignName.getText().toString().trim();
                String email=edSignEmail.getText().toString().trim();
                String password=edSignPass.getText().toString().trim();
                String confirmPassword=edSignRePass.getText().toString().trim();

                  // check all condition
                if(name.isEmpty()){
                    edSignName.setError("You must provide a name");
                    edSignName.requestFocus();
                    return;
                }

                if(email.isEmpty()){
                    edSignEmail.setError("Email is required");
                    edSignEmail.requestFocus();
                    return;
                }

                if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
                    edSignEmail.setError("Please enter a valid email address.");
                    edSignEmail.requestFocus();
                    return;
                }

                if(password.isEmpty()){
                    edSignPass.requestFocus();
                    edSignPass.setError("Password cannot be empty");
                    return;
                }
                if(password.length() <6){
                    edSignPass.setError("Password must be at least 6 characters long.");
                    edSignPass.requestFocus();
                    return;
                }

                if(confirmPassword.isEmpty()){
                    edSignRePass.setError("Please confirm your password");
                    edSignRePass.requestFocus();
                    return;
                }

                if(!password.equals(confirmPassword)){
                    edSignRePass.setError("Please re-enter the same password");
                    edSignRePass.requestFocus();
                    return;

                }
                // if all condition statisfied then
               progbar.setVisibility(View.VISIBLE);
                DoSignup(name,email,password);


            }
        });







    }  // oncreate end

        // signup method for new User
     public  void  DoSignup(String name,String email,String password){

       fAuth.createUserWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
           @Override
           public void onComplete(@NonNull Task<AuthResult> task) {

               if(task.isSuccessful()){ // 1st- signup done -- signup task

                   progbar.setVisibility(View.GONE);

                   //2nd - send a verification code to the email
                      FirebaseUser user=fAuth.getCurrentUser();
                      if(user != null){
                          fAuth.getCurrentUser().sendEmailVerification().addOnCompleteListener(new OnCompleteListener<Void>() {
                              @Override
                              public void onComplete(@NonNull Task<Void> task) {
                                  if(task.isSuccessful()){  // email verification task

                                      Toast.makeText(SignUp.this, "send a verification code to "+fAuth.getCurrentUser().getEmail(), Toast.LENGTH_SHORT).show();

                                      //store user input  in a map
                                      map=new  HashMap<>();
                                      map.put("name",name);
                                      map.put("email",email);
                                      map.put("password",password);
                                      String uid=fAuth.getCurrentUser().getUid(); // get user uid
                                      map.put("uid",uid);


                                      //3rd - after send verification code check again and again if user verified the email
                                      checkEmailVerificationAndStoreData(user,map);

                                  }else{ // email verification task failled
                                      Toast.makeText(SignUp.this, "Failed to send a verification code  ", Toast.LENGTH_SHORT).show();

                                  }

                              }
                          });
                      }


               }
               else { // signup task failed

                   progbar.setVisibility(View.GONE);
                 if(task.getException() instanceof FirebaseAuthUserCollisionException){ // if emaill already used before

                     if(!fAuth.getCurrentUser().isEmailVerified()){ //eamil used before but not verified
                         Toast.makeText(SignUp.this, "Email is already in use ", Toast.LENGTH_SHORT).show();
                     }

                 }
                 else {
                     Toast.makeText(SignUp.this, "Registration failed  "+task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                 }

               }

           }
       });

     }


     // check again and again if email verified done or not
     private void checkEmailVerificationAndStoreData(FirebaseUser user,HashMap<String,String>userData) {
         new Thread(() -> {
             while (true) {
                 user.reload(); // Refresh user data
                 if (user.isEmailVerified()) {

                     // if email is verified then store data to database

                     String uid=user.getUid(); // Get the unique user ID
                     dRef.child("AllUsers").child(uid).child("userInfo").setValue(map).addOnCompleteListener(new OnCompleteListener<Void>() {
                         @Override
                         public void onComplete(@NonNull Task<Void> task) {
                             if(task.isSuccessful()){
                              // Show toast and navigate on the main thread
                              runOnUiThread(()->{  // main thread

                                 progDialog.show();  // show a progressdialog

                                 new Handler().postDelayed(new Runnable() { //add a delay
                                     @Override
                                     public void run() {
                                         Intent myIntent=new Intent(SignUp.this,MainActivity.class);
                                         startActivity(myIntent);
                                         finish();
                                     }
                                 },1200);

                              });

                             }else {
                                 runOnUiThread(()->{
                                     // Handle failure to store data
                                     Toast.makeText(SignUp.this, "Failed to store data: "+task.getException().getMessage() , Toast.LENGTH_SHORT).show();
                                 });

                             }

                         }
                     });
                     break;// Exit the loop after attempting to store data

                 }

                 try {
                     Thread.sleep(3000); // Wait for 3 seconds before checking again
                 } catch (InterruptedException e) {
                     e.printStackTrace();
                 }
             }
         }).start();
     }



}//  end signup class