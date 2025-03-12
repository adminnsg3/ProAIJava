package com.netrocreative.chatgptapp.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import com.netrocreative.chatgptapp.R;

public class AboutUsActivity extends AppCompatActivity {

    private ImageView back;
    private ImageView facebook;
    private ImageView twitter;
    private ImageView linkedin;
    private ImageView whatsapp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about_us);

        back = findViewById(R.id.back);
        facebook = findViewById(R.id.facebook);
        twitter = findViewById(R.id.twitter);
        linkedin = findViewById(R.id.linkdin);
        whatsapp = findViewById(R.id.whatsapp);

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        facebook.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openUrl(getString(R.string.facebook));
            }
        });

        twitter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openUrl(getString(R.string.twitter));
            }
        });

        linkedin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openUrl(getString(R.string.linkedin));
            }
        });

        whatsapp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openUrl(getString(R.string.whatsapp));
            }
        });
    }

    private void openUrl(String url) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse(url));
        String title = "Complete action using";
        Intent chooser = Intent.createChooser(intent, title);
        startActivity(chooser);
    }
}
