package com.example.chillchat;

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
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class Login extends AppCompatActivity {

    EditText logEmail,logPassword;
    TextView tvSignup;
    Button btnLogin;
    ProgressBar progbar;
    FirebaseAuth fAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
         // find id's
        logEmail=findViewById(R.id.logEmail);
        logPassword=findViewById(R.id.logPassword);
        tvSignup=findViewById(R.id.tvSignup);
        btnLogin=findViewById(R.id.btnLogin);
        progbar=findViewById(R.id.progbar);


        fAuth=FirebaseAuth.getInstance();

        // go to signUp page
         tvSignup.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View v) {
                 Intent myIntent=new Intent(Login.this,SignUp.class);
                 startActivity(myIntent);

             }
         });

        // login btn click event
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //take all the input
                String email=logEmail.getText().toString().trim();
                String password=logPassword.getText().toString().trim();

                // check all condition
                if(email.isEmpty()){
                    logEmail.setError("Please enter the email");
                    logEmail.requestFocus();
                    return;
                }
                if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
                    logEmail.setError("Please enter a valid email address.");
                    logEmail.requestFocus();
                    return;
                }
                if(password.isEmpty()){
                    logPassword.setError("Please enter the password");
                     logPassword.requestFocus();
                     return;
                }

                // if all conditions are satisfied then
                progbar.setVisibility(View.VISIBLE);
                DoLogin(email,password);



            }
        });






    } // end oncreate


    // method for login==============
    public void DoLogin(String email,String password){

        fAuth.signInWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {

                if(task.isSuccessful()){ // login task success
                    
                    FirebaseUser user=fAuth.getCurrentUser();
                    if(user !=null && user.isEmailVerified()){ // if user email verified then enter
                        progbar.setVisibility(View.GONE);
                        Intent myIntent=new Intent(Login.this,MainActivity.class);
                        startActivity(myIntent);
                        finish();
                    }else { // if user email not verified
                        Toast.makeText(Login.this, "Email is not verified", Toast.LENGTH_SHORT).show();
                        progbar.setVisibility(View.GONE);
                        
                    }
                 
                }
                else { // login task fail
                    progbar.setVisibility(View.GONE);
                    Toast.makeText(Login.this, ""+task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                }

            }
        });

    }
//===============end=====================

}// end login class