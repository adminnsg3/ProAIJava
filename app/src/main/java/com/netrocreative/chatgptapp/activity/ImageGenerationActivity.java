package com.netrocreative.chatgptapp.activity;

import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
import static android.os.Build.VERSION.SDK_INT;

import android.Manifest;
import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.media.MediaScannerConnection;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.billingclient.api.BillingClient;
import com.applovin.mediation.MaxAd;
import com.applovin.mediation.MaxAdListener;
import com.applovin.mediation.MaxError;
import com.applovin.mediation.ads.MaxInterstitialAd;
import com.applovin.sdk.AppLovinSdk;
import com.applovin.sdk.AppLovinSdkConfiguration;
import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;
import com.netrocreative.chatgptapp.R;
import com.netrocreative.chatgptapp.adapter.ImageResponseAdapter;
import com.netrocreative.chatgptapp.model.Message;
import com.netrocreative.chatgptapp.util.PurchaseChecker;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ImageGenerationActivity extends AppCompatActivity implements MaxAdListener {
    private CoordinatorLayout main;
    private RecyclerView recyclerView;
    private TextView title;
    private ImageView back;
    private EditText messageEditText;
    private CardView sendButton;
    private final OkHttpClient client = new OkHttpClient();
    private String result = "";
    Boolean isAccess = false;

//    private BillingClient billingClient;

    private Dialog popupPricing;
    private ImageView close;
    private CardView pricing;

    private boolean firstStart = true;
    private boolean isRemoveAds = false;
    private boolean isSuccess = false;

    private InterstitialAd mInterstitialAd;
    private MaxInterstitialAd interstitialAd;
    private int retryAttempt;

    private String Title;


    private static final int PERMISSION_REQUEST_CODE = 123;

    private static final String TAG = "Stats";

    private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

    private final List<Message> messageList = new ArrayList<>();
    private ImageResponseAdapter messageAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_generation);

        main = findViewById(R.id.main);
        back = findViewById(R.id.back);
        recyclerView = findViewById(R.id.recycler_view);
        messageEditText = findViewById(R.id.message_edit_text);
        title = findViewById(R.id.title);
        sendButton = findViewById(R.id.send_btn);

        // Setup recycler view
        messageAdapter = new ImageResponseAdapter(messageList, this);
        recyclerView.setAdapter(messageAdapter);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(linearLayoutManager);


        //Ads initialization
//        initAdMob();
        initAppLovin();

//        SharedPreferences prefs = getSharedPreferences("prefs", MODE_PRIVATE);
//        firstStart = prefs.getBoolean("starting", true);
//
//        if (firstStart) {
//            getPopupData();
//        }

        Title = getIntent().getStringExtra("title");
        title.setText(Title);

        if (!isNetworkAvailable()) {
            Intent noInternet = new Intent(ImageGenerationActivity.this, NoInternetActivity.class);
            startActivity(noInternet);
        }

        //Subscription

//        PurchaseChecker purchaseChecker = new PurchaseChecker();
//
//        billingClient = BillingClient.newBuilder(this)
//                .setListener(purchaseChecker.purchasesUpdatedListener)
//                .enablePendingPurchases()
//                .build();
//
//        purchaseChecker.purchaseQuery(billingClient);
//
//        purchaseChecker.setPurchaseStatusCallback(new PurchaseChecker.PurchaseStatusCallback() {
//            @Override
//            public void onPurchaseStatusUpdated(boolean removeAds) {
//                isRemoveAds = removeAds;
//                Log.d(TAG, "onPurchaseStatusUpdated: "+isRemoveAds);
//            }
//        });
//...............................


        // Inside your activity's onCreate or any relevant method
        if (ContextCompat.checkSelfPermission(ImageGenerationActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            // Permission is not granted, request it
            ActivityCompat.requestPermissions(ImageGenerationActivity.this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    PERMISSION_REQUEST_CODE);

        } else {
            isAccess=true;
            Log.d(TAG, "onCreate: "+isAccess);
        }

        messageAdapter.setOnItemClickListener(new ImageResponseAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(ImageView imageView) {


                if (isAccess) {

                    Toast.makeText(ImageGenerationActivity.this, "Saving..", Toast.LENGTH_SHORT).show();
                    // Get the Drawable from the ImageView
                    Drawable drawable = imageView.getDrawable();

                    if (drawable instanceof BitmapDrawable) {
                        Bitmap bitmap = ((BitmapDrawable) drawable).getBitmap();

                        // Save the bitmap to the app's private external storage directory
                        String fileName = String.valueOf(System.currentTimeMillis()) + ".jpg";

                        // Construct the custom directory path
                        File customDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM), getResources().getString(R.string.app_name));
                        if (!customDir.exists()) {
                            customDir.mkdirs();
                        }

                        Log.d("saveImageToGallery", "saveImageToGallery: " + customDir);

                        File file = new File(customDir, fileName);

                        try {
                            OutputStream fos = new FileOutputStream(file);
                            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
                            fos.flush();
                            fos.close();

                            // Notify the gallery that a new image was saved
                            MediaScannerConnection.scanFile(ImageGenerationActivity.this, new String[]{file.getAbsolutePath()}, null, null);

                            // Display a toast message indicating that the image was saved
                            Toast.makeText(ImageGenerationActivity.this, "Image saved to gallery", Toast.LENGTH_SHORT).show();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    } else {
                        // Handle the case where the ImageView does not contain a BitmapDrawable
                        Toast.makeText(ImageGenerationActivity.this, "Unable to save image", Toast.LENGTH_SHORT).show();
                    }

                }else {
                    Toast.makeText(ImageGenerationActivity.this, "Give storage permission to save the image", Toast.LENGTH_SHORT).show();
                }

            }
        });

        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isNetworkAvailable()) {
                    Intent noInternet = new Intent(ImageGenerationActivity.this, NoInternetActivity.class);
                    startActivity(noInternet);
                } else {
                    if (!messageEditText.getText().toString().isEmpty()) {
                        String question = messageEditText.getText().toString().trim();
                        addToChat(question, Message.SENT_BY_ME);
                        messageEditText.setText("");
                        callAPI(question);
                    }
                }
            }
        });

        messageList.add(new Message("Hello!\nHow may I assist you today?", Message.SENT_BY_BOT));

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String adMob = getString(R.string.adMob);
                String appLovin = getString(R.string.appLovin);
                String adPlatform = getString(R.string.adPlatform);
                if (adPlatform.equals(adMob)) {
                    if (mInterstitialAd != null) {
                        mInterstitialAd.show(ImageGenerationActivity.this);
                    } else {
                        finish();
                    }
                } else if (adPlatform.equals(appLovin)) {
                    interstitialAd.showAd();
                } else {
                    finish();
                }
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                isAccess = true;
                Log.d(TAG, "onRequestPermissionsResult: "+isAccess);
            } else {
                isAccess = false;

                Log.d(TAG, "onRequestPermissionsResult: "+isAccess);
            }
        }
    }

    private void getPopupData() {
        popupPricing = new Dialog(this);
        popupPricing.setContentView(R.layout.popup_remove_ads);
        popupPricing.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        close = popupPricing.findViewById(R.id.close);
        pricing = popupPricing.findViewById(R.id.pricing);
        popupPricing.setCancelable(false);

        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences prefs = getSharedPreferences("prefs", MODE_PRIVATE);
                SharedPreferences.Editor editor = prefs.edit();
                editor.putBoolean("starting", false);
                editor.apply();

                popupPricing.dismiss();
            }
        });

        pricing.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences prefs = getSharedPreferences("prefs", MODE_PRIVATE);
                SharedPreferences.Editor editor = prefs.edit();
                editor.putBoolean("starting", false);
                editor.apply();

                popupPricing.dismiss();
                startActivity(new Intent(ImageGenerationActivity.this, SubscriptionActivity.class));
            }
        });

        popupPricing.show();
    }

    private void addToChat(String message, String sentBy) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                messageList.add(new Message(message, sentBy));
                messageAdapter.notifyDataSetChanged();
                recyclerView.smoothScrollToPosition(messageAdapter.getItemCount());
            }
        });
    }

    private void addResponse(String response) {
        messageList.remove(messageList.size() - 1);
        addToChat(response, Message.SENT_BY_BOT);
    }

    private void callAPI(String question) {
        // OkHttp
        messageList.add(new Message("Generating image... ", Message.SENT_BY_BOT));

        JSONObject jsonBody = new JSONObject();
        try {
            jsonBody.put("prompt", question);
            jsonBody.put("n", 1);
            jsonBody.put("size", "256x256");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        RequestBody body = RequestBody.create(JSON, jsonBody.toString());
        Request request = new Request.Builder()
                .url("https://api.openai.com/v1/images/generations")
                .header("Authorization", "Bearer " + getString(R.string.api_key))
                .post(body)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                addResponse("I am sorry but there seems to be some sort of problem here. Can you please say that again?");
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    JSONObject jsonObject = null;
                    try {
                        jsonObject = new JSONObject(response.body().string());
                        JSONArray jsonArray = jsonObject.getJSONArray("data");
                        result = jsonArray.getJSONObject(0).getString("url");
                        addResponse(result);
                        Log.d("Main", "onResponse: " + jsonObject);
                        Log.d("Main", "onResponse: " + result);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } else {
                    addResponse("I am sorry but there seems to be some sort of problem here. Can you please say that again?");
                }
            }
        });
    }

    private void initAdMob() {
        MobileAds.initialize(getApplicationContext(), new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {
                loadAdMobAds();
            }
        });
    }

    private void initAppLovin() {
        AppLovinSdk.getInstance(ImageGenerationActivity.this).setMediationProvider("max");
        AppLovinSdk.initializeSdk(ImageGenerationActivity.this, new AppLovinSdk.SdkInitializationListener() {
            @Override
            public void onSdkInitialized(final AppLovinSdkConfiguration configuration) {

            }
        });

        interstitialAd = new MaxInterstitialAd(getString(R.string.applovin_interstitial_ad_id), ImageGenerationActivity.this);
        interstitialAd.setListener((MaxAdListener) ImageGenerationActivity.this);

        // Load the first ad
        interstitialAd.loadAd();
    }

    private void loadAdMobAds() {
        InterstitialAd.load(
                getApplicationContext(),
                getString(R.string.admob_interstitial_ad_id),
                new AdRequest.Builder().build(),
                new InterstitialAdLoadCallback() {
                    @Override
                    public void onAdLoaded(InterstitialAd interstitialAd) {
                        mInterstitialAd = interstitialAd;
                        mInterstitialAd.setFullScreenContentCallback(new FullScreenContentCallback() {
                            @Override
                            public void onAdDismissedFullScreenContent() {
                                mInterstitialAd = null;
                                finish();
                            }

                            @Override
                            public void onAdFailedToShowFullScreenContent(AdError adError) {
                                mInterstitialAd = null;
                                finish();
                            }
                        });
                    }

                    @Override
                    public void onAdFailedToLoad(LoadAdError loadAdError) {
                        mInterstitialAd = null;
                        finish();
                    }
                });
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    @Override
    public void onAdLoaded(MaxAd maxAd) {
        retryAttempt = 0;
    }

    @Override
    public void onAdDisplayed(MaxAd maxAd) {
        finish();
    }

    @Override
    public void onAdHidden(MaxAd maxAd) {
        interstitialAd.loadAd();
    }

    @Override
    public void onAdClicked(MaxAd maxAd) {
    }

    @Override
    public void onAdLoadFailed(String s, MaxError maxError) {
        retryAttempt++;
        long delayMillis = TimeUnit.SECONDS.toMillis((long) Math.pow(2, Math.min(6, retryAttempt)));
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                interstitialAd.loadAd();
            }
        }, delayMillis);
        finish();
    }

    @Override
    public void onAdDisplayFailed(MaxAd maxAd, MaxError maxError) {
        finish();
        interstitialAd.loadAd();
        finish();
    }

    @Override
    public void onBackPressed() {
        String adMob = getString(R.string.adMob);
        String appLovin = getString(R.string.appLovin);
        String adPlatform = getString(R.string.adPlatform);

        if (adPlatform.equals(adMob)) {
            if (mInterstitialAd != null) {
                mInterstitialAd.show(ImageGenerationActivity.this);
            } else {
                finish();
            }
        } else if (adPlatform.equals(appLovin)) {
            interstitialAd.showAd();
        } else {
            finish();
        }
    }
}


