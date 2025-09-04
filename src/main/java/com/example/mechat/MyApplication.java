package com.example.mechat;

import android.app.Application;
import com.cloudinary.android.MediaManager;
import java.util.HashMap;
import java.util.Map;

public class MyApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        // ✅ Cloudinary Initialization (Runs Once for the Whole App)
        Map<String, String> config = new HashMap<>();
        config.put("cloud_name", "decujmqwp");
        config.put("api_key", "322447726646421");
        config.put("api_secret", "bJ9GVHsCMmarUYTbuTTmWxJDtMY");
        MediaManager.init(this, config);

    }
}

