package com.example.chillchat;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import org.w3c.dom.Text;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

public class All_Messages extends AppCompatActivity {

    RecyclerView msgRecyclerView;


    FirebaseAuth fAuth;
    String currentUserId,chatId;

    ArrayList<HashMap<String,String>> messageList=new ArrayList<>();
    MyAdapter adapter;
    ArrayList<HashMap<String,String>> userInfo=new ArrayList<>();



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_messages);
        // find id's
        msgRecyclerView=findViewById(R.id.messageRecyclerView);
// initialize firebase authentication
        fAuth=FirebaseAuth.getInstance();
         currentUserId=fAuth.getCurrentUser().getUid(); // get the currentUserId



        fetchUserChats(currentUserId);

        adapter=new MyAdapter();
        msgRecyclerView.setAdapter(adapter);
        msgRecyclerView.setLayoutManager(new LinearLayoutManager(All_Messages.this));


    }

// fetch userchat method
    private void fetchUserChats(String currentUserId) {

        DatabaseReference msgRef= FirebaseDatabase.getInstance().getReference().child("Messages");


        msgRef.addValueEventListener(new ValueEventListener() {
            @SuppressLint({"NotifyDataSetChanged", "NewApi"})
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                messageList.clear();

                for(DataSnapshot chatIdSnap : snapshot.getChildren()){

                    String chatKey=chatIdSnap.getKey();
                    // Check if the current user ID is part of the chat key
                    if(chatKey.contains(currentUserId)){

                        String users []= chatKey.split("_");

                        String otherUserUid=(  users[0].equals(currentUserId) ? users[1] : users[0] );

                        String lastMessage =" ";
                        String timeStamp=" " ;
                        boolean isRead=true;
                        String messageId="";
                        String senderId="";



                        // iterate all the messageid get the last message
                        for(DataSnapshot msgIdSnap : chatIdSnap.getChildren() ){

                            lastMessage = msgIdSnap.child("message").getValue().toString();
                            timeStamp = msgIdSnap.child("time").getValue().toString();
                            senderId= msgIdSnap.child("senderUid").getValue().toString();
                            isRead = Boolean.parseBoolean(msgIdSnap.child("isRead").getValue(String.class));  // Convert String to boolean
                            messageId=msgIdSnap.getKey();


                        }

                            HashMap<String,String>msgMap=new HashMap<>();
                            msgMap.put("lastMessage",lastMessage);
                            msgMap.put("otherUserId",otherUserUid);
                            msgMap.put("timeStamp",timeStamp);
                            msgMap.put("isRead",String.valueOf(isRead));
                            msgMap.put("messageId",messageId);
                            msgMap.put("senderId",senderId);


                            messageList.add(msgMap);

                    }
                }

                if(messageList.isEmpty()){
                    Toast.makeText(All_Messages.this, "You have no messages", Toast.LENGTH_SHORT).show();
                }

                // Sort by most recent message timestamp
                messageList.sort((chat1, chat2) -> Long.compare(
                        Long.parseLong(chat2.get("timeStamp")),
                        Long.parseLong(chat1.get("timeStamp"))
                ));

                adapter.notifyDataSetChanged();

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
              //  Toast.makeText(All_Messages.this, "Failed to fetch messages", Toast.LENGTH_SHORT).show();

            }
        });

    }



    // adapter class
    public class  MyAdapter extends  RecyclerView.Adapter<MyAdapter.MyHolder>{

        public class MyHolder extends  RecyclerView.ViewHolder{
            TextView tvLastMessage,messageUserName,messageTime;
            ImageView iconNotification;
            ConstraintLayout messageBody;
            public MyHolder(@NonNull View itemView) {
                super(itemView);
                tvLastMessage=itemView.findViewById(R.id.tvLastMessage);
                messageUserName=itemView.findViewById(R.id.messageUserName);
                messageTime=itemView.findViewById(R.id.messageTime);
                messageBody=itemView.findViewById(R.id.messageBody);
                iconNotification=itemView.findViewById(R.id.iconNotification);
            }
        }

        @NonNull
        @Override
        public MyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

            LayoutInflater inflater= (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View myView= inflater.inflate(R.layout.message_item,parent,false);


            MyHolder holder=new MyHolder(myView);
            return holder;
        }

        @Override
        public void onBindViewHolder(@NonNull MyHolder holder, int position) {

               HashMap<String,String>map=messageList.get(position);

               String lstmsg=map.get("lastMessage");
               String messageTime=map.get("timeStamp");
               boolean isRead=Boolean.parseBoolean(map.get("isRead"));
               String msgId=map.get("messageId");
               String senderId=map.get("senderId");

               String otherUserId=map.get("otherUserId");   // this is other userId


            getInfoFromUserId(otherUserId,holder);  // pass the userId and holder

             // Format timestamp into a readable date/time
             String formattedTime = formatTimestamp(messageTime);

            // Set the last message and message time
               holder.tvLastMessage.setText(lstmsg);
               holder.messageTime.setText(formattedTime);

            // Check if the current message was sent by the current user
            if (isRead == false && !senderId.equals(fAuth.getCurrentUser().getUid())) {
                // Show notification icon if the message is unread and sent by someone else
                holder.iconNotification.setVisibility(View.VISIBLE);
            } else {
                // Hide notification icon for your own messages or read messages
                holder.iconNotification.setVisibility(View.GONE);
            }

// Click event to open the ChatPage
               holder.messageBody.setOnClickListener(new View.OnClickListener() {
                   @Override
                   public void onClick(View v) {
                       // Prepare data to pass to the chat activity
                       Intent myIntent=new Intent(All_Messages.this,ChatPage.class);
                       myIntent.putExtra("userInfo",userInfo.get(position));


                       DatabaseReference msgRef=FirebaseDatabase.getInstance().getReference().child("Messages");

                       // Generate chatId based on senderUid and receiverUid
                       if (fAuth.getCurrentUser().getUid().compareTo(otherUserId) < 0) {
                           chatId = fAuth.getCurrentUser().getUid() + "_" + otherUserId;
                       } else {
                           chatId = otherUserId + "_" + fAuth.getCurrentUser().getUid();
                       }
                       // Update the isRead status
                       msgRef.child(chatId).child(msgId).child("isRead").setValue("true");

                       startActivity(myIntent);

                   }
               });

        }

        @Override
        public int getItemCount() {
            return messageList.size();
        }






    }


    // method for convert time to understandable formate
    public String formatTimestamp(String messageTime){

        if(messageTime.isEmpty() || messageTime == null){
            return "Unknown time";
        }

        long longTine=Long.valueOf(messageTime);
        Date date=new Date(longTine);
        // Define the desired date and time format
        SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault());

        return sdf.format(date);

    }

    // getInformation from userId metthod

    public void getInfoFromUserId(String userId,MyAdapter.MyHolder holder){

        DatabaseReference infoRef=FirebaseDatabase.getInstance().getReference().child("AllUsers");

        infoRef.child(userId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                     DataSnapshot infoSnap=snapshot.child("userInfo");

                     HashMap<String,String>infoMap= (HashMap<String, String>) infoSnap.getValue();

                     if(infoMap != null){
                         userInfo.add(infoMap);
                     }

                     String name=infoSnap.child("name").getValue().toString();

                     holder.messageUserName.setText(name);

                     }



            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });



    }






}