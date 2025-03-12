package com.netrocreative.chatgptapp.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.netrocreative.chatgptapp.R;
import com.netrocreative.chatgptapp.activity.AboutUsActivity;
import com.netrocreative.chatgptapp.activity.SubscriptionActivity;
import com.netrocreative.chatgptapp.activity.WebsiteActivity;

public class SettingsActivity extends AppCompatActivity {

    private ImageView back;
    private CardView subscription;
    private CardView website;
    private CardView aboutUs;
    private CardView logout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        back = findViewById(R.id.back);
        subscription = findViewById(R.id.subscription);
        website = findViewById(R.id.website);
        aboutUs = findViewById(R.id.about_us);
        logout = findViewById(R.id.logout);

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        subscription.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(SettingsActivity.this, SubscriptionActivity.class));
            }
        });

        website.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(SettingsActivity.this, WebsiteActivity.class));
            }
        });

        aboutUs.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(SettingsActivity.this, AboutUsActivity.class));
            }
        });

        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Perform logout action here
            }
        });
    }
}
