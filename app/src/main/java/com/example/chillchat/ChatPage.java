package com.example.chillchat;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

public class ChatPage extends AppCompatActivity {

    ImageView chatProfileImage;
    TextView chatProfileName;
    EditText edMessage;
    CardView btnSend;
    DatabaseReference msgReference;
    FirebaseAuth  fAuth;
    ArrayList<HashMap<String,String>> messageList=new ArrayList<>();
    RecyclerView chatRecyclerView;
    MyAdapter adapter;


    String receiverName,receiverEmail,receiverStatus;
    String receiverUid,senderUid,chatId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_page);
        //find id's of chat Activity
        chatProfileImage=findViewById(R.id.chatProfileImage); // receiver profile image
        chatProfileName=findViewById(R.id.chatProfileName); // receiver  name
        chatRecyclerView=findViewById(R.id.chatRecyclerView) ;  // sender and reciever chat recyclerview
        edMessage=findViewById(R.id.edMessage) ;
        btnSend=findViewById(R.id.btnSend);

        edMessage.requestFocus(); // focus on textbox

        // Initialize Firebase Database reference
        msgReference=FirebaseDatabase.getInstance().getReference().child("Messages");

        // initialize firebase authentication
        fAuth=FirebaseAuth.getInstance();

        // get the intent and retrieve receiver data==================
         Intent myInetnt=getIntent();
         HashMap<String,String>receiverMap= (HashMap<String, String>) myInetnt.getSerializableExtra("userInfo");

          receiverName=receiverMap.get("name");
         // receiverStatus=receiverMap.get("status");

         chatProfileName.setText(receiverName);  // set receiver name

        //===============================================


        // Get sender and receiver UIDs
        senderUid=fAuth.getCurrentUser().getUid(); // Current user (sender)
        receiverUid=receiverMap.get("uid");  // Receiver UID get from MainActivity


        // initialize recyclerview and adapter
          adapter=new MyAdapter();
          chatRecyclerView.setAdapter(adapter);
          chatRecyclerView.setLayoutManager(new LinearLayoutManager(ChatPage.this));





        // Generate chatId based on senderUid and receiverUid
        if (senderUid.compareTo(receiverUid) < 0) {
            chatId = senderUid + "_" + receiverUid;
        } else {
            chatId = receiverUid + "_" + senderUid;
        }



        loadMessage(); //   // Load all existing messages



         // Send message on button click
        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMessage();
            }
        });






    } //oncreate end

    // method for sending message
    private void sendMessage() {

        String messageText=edMessage.getText().toString().trim(); // get the typed message

        if(messageText.isEmpty()){ // if
            Toast.makeText(this, "Type a  message first", Toast.LENGTH_SHORT).show();
            edMessage.requestFocus();
            return;
        }

       // create messsage as hashmap (Every message is a hashmap)
        HashMap<String,String> message=new HashMap<>();
        message.put("senderUid",senderUid);
        message.put("receiverUid",receiverUid);
        message.put("message",messageText);
        message.put("time",String.valueOf(System.currentTimeMillis()));
        message.put("isRead",Boolean.toString(false));

        // Push message to Firebase  =>(Messages/chatid/messsageId/message)
        msgReference.child(chatId).push().setValue(message).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){

                    Toast.makeText(ChatPage.this, "Message sent", Toast.LENGTH_SHORT).show();
                    edMessage.getText().clear();

                }
                else{

                    Toast.makeText(ChatPage.this, "Failed to send message!", Toast.LENGTH_SHORT).show();
                }
            }
        });


    }


    // method for load message

    private void loadMessage(){

       msgReference.child(chatId).addValueEventListener(new ValueEventListener() {
           @Override
           public void onDataChange(@NonNull DataSnapshot snapshot) {

                  messageList.clear();
               for(DataSnapshot msgSnap : snapshot.getChildren()){ // select one by one messageId

                   HashMap<String,String>message= (HashMap<String, String>) msgSnap.getValue();  // get messsage as hashmap from messageId
                   // every hashmap is a messsage
                   // collect all the hashmap and store in list
                   // a complete list is  chat between 2 user
                   messageList.add(message);

               }
               adapter.notifyDataSetChanged(); // notify the adapter about changed in data

               chatRecyclerView.scrollToPosition(messageList.size() -1); //ensure latst message shows on  bottom



           }

           @Override
           public void onCancelled(@NonNull DatabaseError error) {

             //  Toast.makeText(ChatPage.this, "Can't load messages "+error.getMessage(), Toast.LENGTH_SHORT).show();

           }
       });

    }


    // recyclerview adapter class
    public class MyAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{

        private static final int sender_view_type=0;
        private static final int receiver_view_type=1;

        public class SenderViewHolder extends RecyclerView.ViewHolder{ // sender viewholder

            ImageView senderProfileImage;
            TextView senderText,sendeTime;

            public SenderViewHolder(@NonNull View itemView) {
                super(itemView);
                senderText=itemView.findViewById(R.id.senderText);
                sendeTime=itemView.findViewById(R.id.sendTime);
            }
        }

        public class receiverViewHolder extends RecyclerView.ViewHolder{ // receiver viewholder
            ImageView receiverProfileImage;
            TextView receiverText,receiveTime;

            public receiverViewHolder(@NonNull View itemView) {
                super(itemView);
                receiverProfileImage=itemView.findViewById(R.id.receiverProfileImage);
                receiverText=itemView.findViewById(R.id.recieverText);
                receiveTime=itemView.findViewById(R.id.receiveTime);
            }
        }


        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

            if(viewType == sender_view_type){
                // Inflate sender layout
                LayoutInflater layInflater= (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                View myView=layInflater.inflate(R.layout.send_item,parent,false);

                SenderViewHolder holder=new SenderViewHolder(myView);
                return holder;

            }
            else{
                // Inflate receiver layout
                LayoutInflater layInflater= (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                View myView=layInflater.inflate(R.layout.receiver_item,parent,false);
                receiverViewHolder holder=new receiverViewHolder(myView);
                return holder;
            }




        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {

            if(getItemViewType(position) == sender_view_type){
                SenderViewHolder sHolder= (SenderViewHolder) holder;

                HashMap<String,String>message=messageList.get(position);
                String senderMessage=message.get("message"); // get sender text from hasMap
                String time=message.get("time"); // get time from hashmap

                String senderTime=timeFormate(time); // convert the time to understandable form


                sHolder.senderText.setText(senderMessage); // set text message
                sHolder.sendeTime.setText(senderTime);

            }
            else{
                receiverViewHolder rHolder= (receiverViewHolder) holder;

                HashMap<String,String>message=messageList.get(position);
                String recevierMessage=message.get("message"); // get reciver text from hashmap
                String time=message.get("time");

                String receiveTime=timeFormate(time); // convert the time to understandable format



                rHolder.receiveTime.setText(receiveTime); // set time
                rHolder.receiverText.setText(recevierMessage); // set text message



            }

        }

        @Override
        public int getItemCount() {
            return messageList.size();
        }


        @Override
        public int getItemViewType(int position) {

            HashMap<String,String>message=messageList.get(position);
            String senderUid=message.get("senderUid");

            if(senderUid.equals(fAuth.getCurrentUser().getUid())  ){
                return sender_view_type;

            }else{
                return  receiver_view_type;
            }

        }


    }

    // time format  method
    public  String timeFormate(String timeString){

      try {
          // Convert the time string to a long
          long timestamp = Long.valueOf(timeString);

          // Create a Date object with the timestamp
          Date date=new Date(timestamp);
          // Format the date into a readable format (e.g., "dd/MM/yyyy hh:mm a")
          SimpleDateFormat sdf = new SimpleDateFormat("hh:mm a", Locale.getDefault());

          return   sdf.format(date); //return time as string

      }catch (NumberFormatException  e){
          e.printStackTrace();
          return "Invalid time"; // return error text

      }

    } // end






}