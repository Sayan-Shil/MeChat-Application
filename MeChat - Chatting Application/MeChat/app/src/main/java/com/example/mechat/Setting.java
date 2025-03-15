package com.example.mechat;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.cloudinary.android.MediaManager;
import com.cloudinary.android.callback.ErrorInfo;
import com.cloudinary.android.callback.UploadCallback;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.Map;

public class Setting extends AppCompatActivity {
    ImageView setprofile;
    EditText setname, setstatus;
    Button donebut;
    FirebaseAuth auth;
    FirebaseDatabase database;
    Uri setImageUri;
    ProgressDialog progressDialog;
    String email, password;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();

        setprofile = findViewById(R.id.settingprofile);
        setname = findViewById(R.id.settingname);
        setstatus = findViewById(R.id.settingstatus);
        donebut = findViewById(R.id.donebutt);

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Saving...");
        progressDialog.setCancelable(false);

        DatabaseReference reference = database.getReference().child("users").child(auth.getUid());

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                email = snapshot.child("mail").getValue(String.class);
                password = snapshot.child("password").getValue(String.class);
                String name = snapshot.child("username").getValue(String.class);
                String profile = snapshot.child("profilePic").getValue(String.class);
                String status = snapshot.child("status").getValue(String.class);

                setname.setText(name);
                setstatus.setText(status);

                if (profile != null && !profile.isEmpty()) {
                    Picasso.get().load(profile).into(setprofile);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("Firebase", "Error fetching data: " + error.getMessage());
            }
        });

        // Select Image from Gallery
        setprofile.setOnClickListener(view -> {
            Intent intent = new Intent();
            intent.setType("image/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(Intent.createChooser(intent, "Select Picture"), 10);
        });

        // Save Data on Button Click
        donebut.setOnClickListener(view -> {
            progressDialog.show();
            String name = setname.getText().toString();
            String status = setstatus.getText().toString();

            if (setImageUri != null) {
                uploadImageToCloudinary(name, status, reference);
            } else {
                updateUserProfile(name, status, null, reference);
            }
        });
    }

    // ✅ Handle Image Selection from Gallery
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 10 && data != null) {
            setImageUri = data.getData();
            setprofile.setImageURI(setImageUri);
        }
    }

    // ✅ Upload Image to Cloudinary
    private void uploadImageToCloudinary(String name, String status, DatabaseReference reference) {
        progressDialog.setMessage("Uploading Image...");
        progressDialog.show();

        MediaManager.get().upload(setImageUri)
                .callback(new UploadCallback() {
                    @Override
                    public void onStart(String requestId) {
                        Log.d("Cloudinary", "Upload started...");
                    }

                    @Override
                    public void onProgress(String requestId, long bytes, long totalBytes) {
                        Log.d("Cloudinary", "Uploading: " + (bytes * 100 / totalBytes) + "%");
                    }

                    @Override
                    public void onSuccess(String requestId, Map resultData) {
                        String imageUrl = resultData.get("secure_url").toString();
                        Log.d("Cloudinary", "Image uploaded: " + imageUrl);

                        updateUserProfile(name, status, imageUrl, reference);
                    }

                    @Override
                    public void onError(String requestId, ErrorInfo error) {
                        progressDialog.dismiss();
                        Toast.makeText(Setting.this, "Image Upload Failed!", Toast.LENGTH_SHORT).show();
                        Log.e("Cloudinary", "Upload error: " + error.getDescription());
                    }

                    @Override
                    public void onReschedule(String requestId, ErrorInfo error) {
                        Log.e("Cloudinary", "Upload rescheduled: " + error.getDescription());
                    }
                })
                .dispatch();
    }

    // ✅ Update User Profile in Firebase
    private void updateUserProfile(String name, String status, @Nullable String imageUrl, DatabaseReference reference) {
        progressDialog.setMessage("Saving Data...");
        progressDialog.show();

        Map<String, Object> userData = new HashMap<>();
        userData.put("username", name);
        userData.put("status", status);
        if (imageUrl != null) {
            userData.put("profilePic", imageUrl);
        }

        reference.updateChildren(userData).addOnCompleteListener(task -> {
            progressDialog.dismiss();
            if (task.isSuccessful()) {
                Toast.makeText(Setting.this, "Profile Updated!", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(Setting.this, MainActivity.class));
                finish();
            } else {
                Toast.makeText(Setting.this, "Error Saving Data!", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
