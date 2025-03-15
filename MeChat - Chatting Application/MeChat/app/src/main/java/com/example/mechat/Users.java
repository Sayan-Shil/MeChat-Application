package com.example.mechat;

public class Users {
    private String profilePic, mail, username, password, userId, lastMessage, status;

    // ✅ Default Constructor (Required for Firebase)
    public Users() {
    }

    // ✅ Properly Assign Data in Constructor
    public Users(String id, String name, String email, String password, String imageUri, String status) {
        this.userId = id;
        this.username = name;
        this.mail = email;
        this.password = password;
        this.profilePic = imageUri;
        this.status = status;
        this.lastMessage = ""; // Default empty message
    }

    // ✅ Getters & Setters
    public String getProfilePic() { return profilePic; }
    public void setProfilePic(String profilePic) { this.profilePic = profilePic; }

    public String getMail() { return mail; }
    public void setMail(String mail) { this.mail = mail; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getLastMessage() { return lastMessage; }
    public void setLastMessage(String lastMessage) { this.lastMessage = lastMessage; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
