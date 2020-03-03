package com.app.example;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

public class DeepLinkActivity extends AppCompatActivity {


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        String action = intent.getAction();
        Uri data = intent.getData();
        Log.d(data.toString(),"?>?>> URL NAME !!!!!!!!!");
        switchActivity(data);

    }

    private void switchActivity(Uri url){
        finish();
        Intent mainActivity = new Intent(this, MainActivity.class);
        mainActivity.putExtra("EXTRA_SESSION_URL", url.toString());
        mainActivity.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivityIfNeeded(mainActivity, 0);
    }
}
