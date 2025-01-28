package com.example.chillchat;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;

public class MainActivity extends AppCompatActivity {
    ImageView allChat, mainSetting,logOut;
    Animation anim;
    RecyclerView userRecycleView;
    HashMap<String,String> map;
    ArrayList<HashMap<String,String>> userList=new ArrayList<>();
    DatabaseReference dRef;
    MyAdapter adapter;

    ProgressDialog progdialog;

    String chatId;
  FirebaseAuth fAuth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // find id's
        allChat=findViewById(R.id.allChat);
        mainSetting=findViewById(R.id.mainSetting);
        logOut=findViewById(R.id.logOut);
        userRecycleView=findViewById(R.id.userRecycleView);

      // showing a progessDialog
        progdialog=new ProgressDialog(MainActivity.this);
        progdialog.setCancelable(false);
        progdialog.setMessage("Loading User..");
        progdialog.show();

        // initialize database
        dRef= FirebaseDatabase.getInstance().getReference();
        // initialize authentication
        fAuth=FirebaseAuth.getInstance();

        getAllUser();  // gets all the user

        // initialize  a animation
        anim= AnimationUtils.loadAnimation(MainActivity.this,R.anim.click_anim);



        // all chat click event
        allChat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                allChat.startAnimation(anim); //set the animation
                Intent myIntent=new Intent(MainActivity.this,All_Messages.class);
                startActivity(myIntent);
            }
        });

        // setting click event
        mainSetting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mainSetting.startAnimation(anim); //set the animation
                Intent myIntent=new Intent(MainActivity.this,SettingActivity.class);
                startActivity(myIntent);
            }
        });

        // logout btn click event
        logOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                logOut.startAnimation(anim);  // set the animation

                if(fAuth.getCurrentUser() != null){

                    Dialog dialog=new Dialog(MainActivity.this); // showing custom dialog
                    dialog.setContentView(R.layout.logout_dialog);
                    dialog.getWindow().setBackgroundDrawableResource(R.drawable.dialog_style);
                    // find id's of dialog
                    TextView tvYes=dialog.findViewById(R.id.tvYes);
                    TextView tvNo=dialog.findViewById(R.id.tvNo);

                    tvYes.setOnClickListener(new View.OnClickListener() { // yes click event
                        @Override
                        public void onClick(View v) {

                    fAuth.signOut(); // loging out
                    Intent logoutIntent=new Intent(MainActivity.this,Login.class);
                    logoutIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK); // create new task and clear all pervious activity
                    startActivity(logoutIntent);

                    dialog.dismiss();

                        }
                    });

                    tvNo.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {

                            dialog.dismiss();
                        }
                    });

                    dialog.show();




                }
            }
        });


        // create object of adapter class
         adapter=new MyAdapter();
        userRecycleView.setAdapter(adapter);
        userRecycleView.setLayoutManager(new LinearLayoutManager(MainActivity.this));




    }  // end on creat

    // create adapter class

    public class MyAdapter extends RecyclerView.Adapter<MyAdapter.MyHolder>{

        public class MyHolder extends RecyclerView.ViewHolder{

            TextView mainUserName,tvStatus;
            ConstraintLayout userBody;
            public MyHolder(@NonNull View itemView) {
                super(itemView);
                // find id's of userItem
                mainUserName=itemView.findViewById(R.id.mainUserName);
                tvStatus=itemView.findViewById(R.id.tvStatus);
                userBody=itemView.findViewById(R.id.UserBody);

            }
        }

        @NonNull
        @Override
        public MyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

            // inflate the layout
            LayoutInflater layInflater= (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View myView=layInflater.inflate(R.layout.user_item,parent,false);

            MyHolder holder=new MyHolder(myView);
            return holder;
        }

        @Override
        public void onBindViewHolder(@NonNull MyHolder holder, int position) {

            // get hashmap from arraylist
             HashMap<String,String>userMap=userList.get(position);
             String name=userMap.get("name"); // get the name from hashmap
             String status= userMap.get("status");

             if(status !=null){  // check if status field missing
                 holder.tvStatus.setText(status);
             }else {
                 holder.tvStatus.setText("Hi There I'm Using ChillChat");
             }


            holder.mainUserName.setText(name); // set the Username




            // click on any user to chat
            holder.userBody.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent myInent=new Intent(MainActivity.this,ChatPage.class);
                    myInent.putExtra("userInfo",userMap);
                    startActivity(myInent);

                }
            });



        }

        @Override
        public int getItemCount() {
            return userList.size();
        }



    } // adapter ends

    // all user get method
    public void getAllUser(){

        dRef.child("AllUsers").addValueEventListener(new ValueEventListener() {  // get value from database
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                userList.clear(); // Clear list before adding new data

              for(DataSnapshot snap : snapshot.getChildren()){   // get all userId one by one

                  DataSnapshot infoSnapshot=snap.child("userInfo"); // target info node from the user

                  if(infoSnapshot !=null && infoSnapshot.getValue()!=null){

                      HashMap<String,String>infoMap= (HashMap<String, String>) infoSnapshot.getValue();


                      if( ! fAuth.getCurrentUser().getUid().equals(infoMap.get("uid")) ){ // Add only if it's not the current user

                           userList.add(infoMap);
                      }

                  }

              }

              progdialog.dismiss(); // dismiss  dialog
                adapter.notifyDataSetChanged();




            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

                progdialog.dismiss();// dismiss dialog
                Log.d("dataReterieve",error.getMessage());
               // Toast.makeText(MainActivity.this, "Failed to retrieve data 1 ", Toast.LENGTH_SHORT).show();

            }
        });

    }




}