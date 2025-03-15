package com.example.mechat;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import com.cloudinary.android.MediaManager;
import com.cloudinary.android.callback.UploadCallback;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import java.util.HashMap;
import java.util.Map;
import de.hdodenhof.circleimageview.CircleImageView;
import com.cloudinary.android.callback.ErrorInfo;

public class RegistrationActivity extends AppCompatActivity {
    TextView loginBut;
    EditText rg_username, rg_password, rg_repassword, rg_email;
    Button rg_signup;
    CircleImageView rg_profileImg;
    FirebaseAuth auth;
    Uri imageUri;
    String imageUriString;
    FirebaseDatabase database;
    ProgressDialog progressDialog;

    private static final String DEFAULT_IMAGE_URL = "https://res.cloudinary.com/your_cloud/image/upload/v1738928649/Man.png";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Establishing your Account");
        progressDialog.setCancelable(false);

        // ✅ Removed initConfig() because Cloudinary is initialized in MyApplication.java

        loginBut = findViewById(R.id.loginbutton);
        rg_username = findViewById(R.id.rgUsername);
        rg_email = findViewById(R.id.rgEmailAddress);
        rg_password = findViewById(R.id.rgPassword);
        rg_repassword = findViewById(R.id.rgRePassword);
        rg_signup = findViewById(R.id.signupbutton);
        rg_profileImg = findViewById(R.id.profilerg);

        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();

        rg_profileImg.setOnClickListener(v -> {
            Intent intent = new Intent();
            intent.setType("image/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(Intent.createChooser(intent, "Select Picture"), 10);
        });

        rg_signup.setOnClickListener(v -> registerUser());

        loginBut.setOnClickListener(v -> {
            Intent intent = new Intent(RegistrationActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        });
    }

    private void registerUser() {
        String name = rg_username.getText().toString().trim();
        String email = rg_email.getText().toString().trim();
        String password = rg_password.getText().toString().trim();
        String confirmPassword = rg_repassword.getText().toString().trim();
        String status = "Online";

        if (TextUtils.isEmpty(name) || TextUtils.isEmpty(email) || TextUtils.isEmpty(password) || TextUtils.isEmpty(confirmPassword)) {
            progressDialog.dismiss();
            Toast.makeText(this, "Please enter valid information", Toast.LENGTH_SHORT).show();
            return;
        }

        auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                String id = task.getResult().getUser().getUid();
                DatabaseReference reference = database.getReference().child("users").child(id);

                if (imageUri != null) {
                    uploadToCloudinary(id, name, email, password, status, reference);
                } else {
                    saveUserToDatabase(id, name, email, password, DEFAULT_IMAGE_URL, status, reference);
                }
            } else {
                Toast.makeText(this, "Registration Failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void uploadToCloudinary(String id, String name, String email, String password, String status, DatabaseReference reference) {
        if (imageUri == null) {
            Toast.makeText(this, "No image selected!", Toast.LENGTH_SHORT).show();
            return;
        }

        progressDialog.setMessage("Uploading Image...");
        progressDialog.show();

        MediaManager.get().upload(imageUri)
                .option("resource_type", "image")
                .callback(new UploadCallback() {
                    @Override
                    public void onStart(String requestId) {
                        // ✅ Called when the upload starts
                        progressDialog.setMessage("Uploading Image...");
                    }

                    @Override
                    public void onProgress(String requestId, long bytes, long totalBytes) {
                        // ✅ Called while uploading
                        int progress = (int) ((bytes * 100) / totalBytes);
                        progressDialog.setMessage("Uploading... " + progress + "%");
                    }

                    @Override
                    public void onSuccess(String requestId, Map resultData) {
                        // ✅ Called when upload is successful
                        if (resultData != null && resultData.get("secure_url") != null) {
                            imageUriString = resultData.get("secure_url").toString();
                            saveUserToDatabase(id, name, email, password, imageUriString, status, reference);
                        } else {
                            Toast.makeText(RegistrationActivity.this, "Upload failed: No URL returned!", Toast.LENGTH_SHORT).show();
                        }
                        progressDialog.dismiss();
                    }

                    @Override
                    public void onError(String requestId, ErrorInfo error) {
                        // ✅ Handle Cloudinary upload failure
                        progressDialog.dismiss();
                        Toast.makeText(RegistrationActivity.this, "Image Upload Failed: " + error.getDescription(), Toast.LENGTH_SHORT).show();
                        saveUserToDatabase(id, name, email, password, DEFAULT_IMAGE_URL, status, reference);
                    }

                    @Override
                    public void onReschedule(String requestId, ErrorInfo error) {
                        // ✅ Called if upload is rescheduled (e.g., poor network)
                        Toast.makeText(RegistrationActivity.this, "Upload Rescheduled: " + error.getDescription(), Toast.LENGTH_SHORT).show();
                    }
                }).dispatch();
    }


    private void saveUserToDatabase(String id, String name, String email, String password, String imageUrl, String status, DatabaseReference reference) {
        Users user = new Users(id, name, email, password, imageUrl, status);
        reference.setValue(user).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(RegistrationActivity.this, "User Registered Successfully!", Toast.LENGTH_SHORT).show();
                navigateToMain();
            } else {
                Toast.makeText(RegistrationActivity.this, "Error in Creating the User", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void navigateToMain() {
        progressDialog.show();
        Intent intent = new Intent(RegistrationActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 10 && data != null && data.getData() != null) {
            imageUri = data.getData();
            rg_profileImg.setImageURI(imageUri);
        }
    }
}
