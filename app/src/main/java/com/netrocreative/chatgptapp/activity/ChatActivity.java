package com.netrocreative.chatgptapp.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

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
import com.google.android.gms.ads.OnUserEarnedRewardListener;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;
import com.google.android.gms.ads.rewarded.RewardItem;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.netrocreative.chatgptapp.R;
import com.netrocreative.chatgptapp.adapter.MessageAdapter;
import com.netrocreative.chatgptapp.model.Message;
import com.netrocreative.chatgptapp.util.PurchaseChecker;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
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

public class ChatActivity extends AppCompatActivity implements MaxAdListener {

    CoordinatorLayout main;
    ImageView back;
    TextView title;
    RecyclerView recyclerView;
    EditText messageEditText;
    CardView sendButton;
    List<Message> messageList = new ArrayList<>();
    MessageAdapter messageAdapter;
    OkHttpClient client;
    RelativeLayout bottomLayout;

    private BillingClient billingClient;

    private Dialog popupPricing;
    private ImageView close;
    private CardView pricing;

    private boolean firstStart = true;
    private boolean isRemoveAds = false;
    private boolean isSuccess = false;

    private InterstitialAd mInterstitialAd;

    //AppLovin
    private MaxInterstitialAd interstitialAd;
    private int retryAttempt;

    String Title;

    private static final String TAG = "Stats";

    private static final MediaType JSON = okhttp3.MediaType.parse("application/json; charset=utf-8");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        main = findViewById(R.id.main);
        back = findViewById(R.id.back);
        title = findViewById(R.id.title);
        bottomLayout = findViewById(R.id.bottom_layout);
        recyclerView = findViewById(R.id.recycler_view);
        messageEditText = findViewById(R.id.message_edit_text);
        sendButton = findViewById(R.id.send_btn);

        // Setup recycler view
        messageAdapter = new MessageAdapter(messageList, getApplicationContext());
        recyclerView.setAdapter(messageAdapter);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getApplicationContext());
        linearLayoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(linearLayoutManager);

        //Ads initialization
        initAdMob();
        initAppLovin();

        SharedPreferences prefs = getSharedPreferences("prefs", MODE_PRIVATE);
        firstStart = prefs.getBoolean("starting", true);

        if (firstStart) {
            getPopupData();
        }


        //Subscription

        PurchaseChecker purchaseChecker = new PurchaseChecker();

        billingClient = BillingClient.newBuilder(this)
                .setListener(purchaseChecker.purchasesUpdatedListener)
                .enablePendingPurchases()
                .build();

        purchaseChecker.purchaseQuery(billingClient);

        purchaseChecker.setPurchaseStatusCallback(new PurchaseChecker.PurchaseStatusCallback() {
            @Override
            public void onPurchaseStatusUpdated(boolean removeAds) {
                isRemoveAds = removeAds;
                Log.d(TAG, "onPurchaseStatusUpdated: "+isRemoveAds);
            }
        });
//...............................

        Title = getIntent().getStringExtra("title");

        title.setText(Title);

        client = new OkHttpClient();


        if (!isNetworkAvailable()) {
            Intent noInternet = new Intent(getApplicationContext(), NoInternetActivity.class);
            startActivity(noInternet);
        }


        sendButton.setOnClickListener(v -> {
            if (!isNetworkAvailable()) {
                Intent noInternet = new Intent(getApplicationContext(), NoInternetActivity.class);
                startActivity(noInternet);
            } else {
                if (!messageEditText.getText().toString().isEmpty()) {
                    String question = messageEditText.getText().toString().trim();
                    addToChat(question, Message.SENT_BY_ME);
                    messageEditText.setText("");
                    callAPI(question);
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

                if (adPlatform.equals(adMob) && !isRemoveAds) {
                    if (mInterstitialAd != null) {
                        mInterstitialAd.show(ChatActivity.this);
                    } else {
                        finish();
                    }
                } else if (adPlatform.equals(appLovin) && !isRemoveAds) {
                    interstitialAd.showAd();
                } else {
                    finish();
                }
            }
        });

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
            public void onClick(View view) {
                SharedPreferences prefs = getSharedPreferences("prefs", MODE_PRIVATE);
                SharedPreferences.Editor editor = prefs.edit();
                editor.putBoolean("starting", false);
                editor.apply();

                popupPricing.dismiss();
            }
        });

        pricing.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SharedPreferences prefs = getSharedPreferences("prefs", MODE_PRIVATE);
                SharedPreferences.Editor editor = prefs.edit();
                editor.putBoolean("starting", false);
                editor.apply();

                popupPricing.dismiss();
                startActivity(new Intent(ChatActivity.this, SubscriptionActivity.class));
            }
        });

        popupPricing.show();
    }


    private void addToChat(String message, String sentBy) {
        runOnUiThread(() -> {
            messageList.add(new Message(message, sentBy));
            messageAdapter.notifyDataSetChanged();
            recyclerView.smoothScrollToPosition(messageAdapter.getItemCount());
        });
    }

    private void addResponse(String response) {
        messageList.remove(messageList.size() - 1);
        addToChat(response, Message.SENT_BY_BOT);
    }

    private void callAPI(String message) {
        // OkHttp
        messageList.add(new Message("Typing... ", Message.SENT_BY_BOT));

        JSONObject jsonBody = new JSONObject();
        try {
            jsonBody.put("model", "gpt-3.5-turbo");
            jsonBody.put("messages", new JSONArray().put(new JSONObject()
                            .put("role", "system")
                            .put("content", "You are a personal AI assistant, the smartest out there."))
                    .put(new JSONObject()
                            .put("role", "system")
                            .put("content", "If someone asks question that is inappropriate just say that the question you are asking is not appropriate."))
                    .put(new JSONObject()
                            .put("role", "system")
                            .put("content", "If someone greets you, you greet them back from."))
                    .put(new JSONObject()
                            .put("role", "system")
                            .put("content", "If there is no internet connection, inform the user about it and apologize."))
                    .put(new JSONObject()
                            .put("role", "user")
                            .put("content", "Give structured response on " + message))

            );
        } catch (JSONException e) {
            e.printStackTrace();
        }
        String chatEndpoint = getResources().getString(R.string.chat_endpoint);
        RequestBody body = RequestBody.create(JSON, jsonBody.toString());
        Request request = new Request.Builder()
                .url(chatEndpoint)
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
                        Log.d(TAG, "onResponse: " + jsonObject);
                        JSONArray jsonArray = jsonObject.getJSONArray("choices");
                        String responseValue = jsonArray.getJSONObject(0).getJSONObject("message").getString("content");
                        addResponse(responseValue);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } else {
                    addResponse("I am sorry but there seems to be some sort of problem here. Can you please say that again?");
                }
            }
        });
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        if (connectivityManager != null) {
            android.net.NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
            return activeNetworkInfo != null && activeNetworkInfo.isConnected();
        }
        return false;
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
        AppLovinSdk.getInstance(getApplicationContext()).setMediationProvider("max");
        AppLovinSdk.initializeSdk(getApplicationContext(), new AppLovinSdk.SdkInitializationListener() {
            @Override
            public void onSdkInitialized(final AppLovinSdkConfiguration configuration) {

            }
        });

        interstitialAd = new MaxInterstitialAd(getString(R.string.applovin_interstitial_ad_id), ChatActivity.this);
        interstitialAd.setListener((MaxAdListener) ChatActivity.this);

        // Load the first ad
        interstitialAd.loadAd();
    }


    //AdMob Ads

    private void loadAdMobAds() {

        InterstitialAd.load(getApplicationContext(), getString(R.string.admob_interstitial_ad_id), new AdRequest.Builder().build(),
                new InterstitialAdLoadCallback() {
                    @Override
                    public void onAdLoaded(@NonNull InterstitialAd interstitialAd) {
                        // The mInterstitialAd reference will be null until
                        // an ad is loaded.
                        mInterstitialAd = interstitialAd;
                        Log.i(TAG, "onAdLoaded");

                        mInterstitialAd.setFullScreenContentCallback(new FullScreenContentCallback() {
                            @Override
                            public void onAdClicked() {
                                // Called when a click is recorded for an ad.
                                Log.d(TAG, "Ad was clicked.");
                            }

                            @Override
                            public void onAdDismissedFullScreenContent() {
                                // Called when ad is dismissed.
                                // Set the ad reference to null so you don't show the ad a second time.
                                Log.d(TAG, "Ad dismissed fullscreen content.");
                                mInterstitialAd = null;
                                finish();
                            }

                            @Override
                            public void onAdFailedToShowFullScreenContent(AdError adError) {
                                // Called when ad fails to show.
                                Log.e(TAG, "Ad failed to show fullscreen content.");
                                mInterstitialAd = null;
                                finish();
                            }

                            @Override
                            public void onAdImpression() {
                                // Called when an impression is recorded for an ad.
                                Log.d(TAG, "Ad recorded an impression.");
                            }

                            @Override
                            public void onAdShowedFullScreenContent() {
                                // Called when ad is shown.
                                Log.d(TAG, "Ad showed fullscreen content.");
                            }
                        });
                    }

                    @Override
                    public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                        // Handle the error
                        Log.d(TAG, loadAdError.toString());
                        mInterstitialAd = null;
                    }


                });
    }
    //...............................


    //AppLovin Ads

    @Override
    public void onAdLoaded(MaxAd maxAd) {
        retryAttempt = 0;
        Log.d(TAG, "Ad loaded");
    }

    @Override
    public void onAdDisplayed(MaxAd maxAd) {
        Log.d(TAG, "Ad displayed");
        finish();
    }

    @Override
    public void onAdHidden(MaxAd maxAd) {
        interstitialAd.loadAd();
        Log.d(TAG, "Ad hidden");
    }

    @Override
    public void onAdClicked(MaxAd maxAd) {
        Log.d(TAG, "Ad clicked");
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
    }

    @Override
    public void onAdDisplayFailed(MaxAd maxAd, MaxError maxError) {
        interstitialAd.loadAd();
        Log.d(TAG, "Ad display failed");
    }
    //....................................


    @Override
    public void onBackPressed() {
        String adMob = getString(R.string.adMob);
        String appLovin = getString(R.string.appLovin);
        String adPlatform = getString(R.string.adPlatform);

        if (adPlatform.equals(adMob) && !isRemoveAds) {
            if (mInterstitialAd != null) {
                mInterstitialAd.show(ChatActivity.this);
            } else {
                finish();
            }
        } else if (adPlatform.equals(appLovin) && !isRemoveAds) {
            interstitialAd.showAd();
        } else {
            finish();
        }
    }
}