package com.example.mechat;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.cardview.widget.CardView;
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
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Date;

import de.hdodenhof.circleimageview.CircleImageView;

public class chatWin extends AppCompatActivity {
    String reciverimg, reciverUid, reciverName, SenderUID;
    CircleImageView profile;
    TextView reciverNName;
    FirebaseDatabase database;
    FirebaseAuth firebaseAuth;
    public static String senderImg;
    public static String reciverImg;
    AppCompatImageView sendbtn;
    AppCompatEditText textmsg;

    String senderRoom, reciverRoom;
    RecyclerView messageAdpter;
    ArrayList<msgModelclass> messagesArrayList;
    messagesAdpter mmessagesAdpter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_win);

        database = FirebaseDatabase.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();

        // Initialize Views
        sendbtn = findViewById(R.id.sendbtnn);
        textmsg = findViewById(R.id.textmsg);
        reciverNName = findViewById(R.id.recivername);
        profile = findViewById(R.id.profileimgg);
        messageAdpter = findViewById(R.id.msgadpter);

        // Get Intent Data
        reciverName = getIntent().getStringExtra("nameee");
        reciverimg = getIntent().getStringExtra("recieverImg");
        reciverUid = getIntent().getStringExtra("uid");

        Log.d("Chat", "Receiver Name: " + reciverName);
        Log.d("Chat", "Receiver Image: " + reciverimg);

        // Set Receiver Name
        if (reciverName != null) {
            reciverNName.setText(reciverName);
        } else {
            reciverNName.setText("Unknown User");
        }

        // Set Profile Image with Picasso
        if (reciverimg != null && !reciverimg.isEmpty()) {
            Picasso.get().load(reciverimg).placeholder(R.drawable.man).into(profile);
        } else {
            profile.setImageResource(R.drawable.man);
        }

        // RecyclerView Setup
        messagesArrayList = new ArrayList<>();
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        messageAdpter.setLayoutManager(linearLayoutManager);

        mmessagesAdpter = new messagesAdpter(chatWin.this, messagesArrayList);
        messageAdpter.setAdapter(mmessagesAdpter);

        SenderUID = firebaseAuth.getUid();
        senderRoom = SenderUID + reciverUid;
        reciverRoom = reciverUid + SenderUID;

        DatabaseReference chatreference = database.getReference().child("chats").child(senderRoom).child("messages");

        chatreference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                messagesArrayList.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    msgModelclass messages = dataSnapshot.getValue(msgModelclass.class);
                    messagesArrayList.add(messages);
                }
                mmessagesAdpter.notifyDataSetChanged();

                // Scroll to the last message after data update
                messageAdpter.scrollToPosition(mmessagesAdpter.getItemCount() - 1);
                Log.d("Chat", "Messages updated: " + messagesArrayList.size());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("Chat", "Database Error: " + error.getMessage());
            }
        });

        // ✅ Send Message on Button Click
        sendbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String message = textmsg.getText().toString().trim();

                if (message.isEmpty()) {
                    Toast.makeText(chatWin.this, "Enter a message first!", Toast.LENGTH_SHORT).show();
                    return;
                }

                textmsg.setText(""); // Clear input box
                Date date = new Date();
                msgModelclass messagess = new msgModelclass(message, SenderUID, date.getTime());

                Log.d("Chat", "Sending message: " + message);

                // Send message to Firebase
                database.getReference().child("chats").child(senderRoom).child("messages")
                        .push().setValue(messagess)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    Log.d("Chat", "Message sent successfully");

                                    // Also save to receiver's chat room
                                    database.getReference().child("chats").child(reciverRoom).child("messages")
                                            .push().setValue(messagess)
                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    Log.d("Chat", "Message added to receiver's chat room");
                                                }
                                            });

                                } else {
                                    Log.e("Chat", "Failed to send message", task.getException());
                                }
                            }
                        });
            }
        });
    }
}
