package com.example.chillchat;

import android.app.Dialog;
import android.media.Image;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.w3c.dom.Text;

import java.util.HashMap;

public class SettingActivity extends AppCompatActivity {

    ImageView edit;
    TextView tvUsername,tvEmail;
    DatabaseReference infoRef;
  FirebaseAuth fAuth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        // find id's
        edit=findViewById(R.id.edit);
        tvUsername=findViewById(R.id.tvUserName);
        tvEmail=findViewById(R.id.tvEmail);

        // firebase database and authentication initialize
        infoRef= FirebaseDatabase.getInstance().getReference().child("AllUsers");
      fAuth=FirebaseAuth.getInstance();


        // click on edit

        loadUserInfo();

        edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Dialog dialog=new Dialog(SettingActivity.this);
                dialog.setContentView(R.layout.update_info);
                dialog.getWindow().setBackgroundDrawableResource(R.drawable.dialog_style);
                dialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                // find id's
                EditText edName=dialog.findViewById(R.id.edUpName);
                Button btnSave=dialog.findViewById(R.id.btnSave);

                // btnupdate click event
                btnSave.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String upName=edName.getText().toString().trim();
                        if(upName.isEmpty()){
                            edName.setError("Cant be empty");
                            return;
                        }
                         btnSave.setEnabled(false);
                        HashMap<String,Object>updateMap=new HashMap<>();
                        updateMap.put("name",upName);

                        infoRef.child(fAuth.getCurrentUser().getUid()).child("userInfo").updateChildren(updateMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if(task.isSuccessful()){
                                    btnSave.setEnabled(true);
                                    dialog.dismiss();
                                    Toast.makeText(SettingActivity.this, "Name updated successfully!", Toast.LENGTH_SHORT).show();
                                }else{
                                    btnSave.setEnabled(true);
                                    dialog.dismiss();
                                    Toast.makeText(SettingActivity.this, "Failed to update name", Toast.LENGTH_SHORT).show();
                                }

                            }
                        });

                    }
                });

                dialog.show();


            }
        });




    }


    // load user info

    public void loadUserInfo(){
        String uid=fAuth.getCurrentUser().getUid();
        infoRef.child(uid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                DataSnapshot infoSnap=snapshot.child("userInfo");

                String name=infoSnap.child("name").getValue().toString();
                String email=infoSnap.child("email").getValue().toString();

                tvEmail.setText(email);
                tvUsername.setText(name);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
              //  Toast.makeText(SettingActivity.this, "Failed to fetch data", Toast.LENGTH_SHORT).show();
            }
        });

    }






}