package com.example.mechat;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    FirebaseAuth auth;
    RecyclerView mainUserRecyclerView;
    UserAdapter adapter;
    FirebaseDatabase database;
    List<Users> usersArrayList;
    ImageView imglogout, cumbut, setbut;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() == null) {
            Toast.makeText(this, "User not authenticated!", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(MainActivity.this, LoginActivity.class));
            finish();
            return;
        }

        database = FirebaseDatabase.getInstance();
        DatabaseReference reference = database.getReference().child("users");

        usersArrayList = new ArrayList<>();
        adapter = new UserAdapter(this, usersArrayList);

        imglogout = findViewById(R.id.logoutimg);
        mainUserRecyclerView = findViewById(R.id.mainUserRecyclerView);
        mainUserRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mainUserRecyclerView.setAdapter(adapter);

        setbut = findViewById(R.id.settingBut);
        cumbut = findViewById(R.id.camBut);

        // 🔹 Handle Logout Click
        imglogout.setOnClickListener(v -> {
            Dialog dialog = new Dialog(MainActivity.this, R.style.dialoge);
            dialog.setContentView(R.layout.dialog_layout);
            dialog.setCancelable(false);

            Button yes = dialog.findViewById(R.id.yesbnt);
            Button no = dialog.findViewById(R.id.nobnt);

            yes.setOnClickListener(v1 -> {
                dialog.dismiss();
                FirebaseAuth.getInstance().signOut();
                Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
            });

            no.setOnClickListener(v1 -> dialog.dismiss());

            dialog.show();
        });

        // 🔹 Open Settings Activity
        setbut.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, Setting.class);
            startActivity(intent);
        });

        // 🔹 Open Camera Safely
        cumbut.setOnClickListener(v -> {
            Intent intent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
            if (intent.resolveActivity(getPackageManager()) != null) {
                startActivityForResult(intent, 10);
            } else {
                Toast.makeText(MainActivity.this, "No camera app available", Toast.LENGTH_SHORT).show();
            }
        });

        // 🔹 Load Users from Firebase
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                usersArrayList.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    if (dataSnapshot.exists()) {
                        Users user = dataSnapshot.getValue(Users.class);
                        usersArrayList.add(user);
                    }
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(MainActivity.this, "Database error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    // ✅ Handle Camera Result
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 10 && resultCode == RESULT_OK) {
            Toast.makeText(this, "Photo captured successfully!", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Camera action canceled", Toast.LENGTH_SHORT).show();
        }
    }
}
