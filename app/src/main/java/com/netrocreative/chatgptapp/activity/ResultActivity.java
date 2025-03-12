package com.netrocreative.chatgptapp.activity;

import android.app.Dialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.util.JsonToken;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.*;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.android.billingclient.api.*;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.applovin.mediation.MaxAd;
import com.applovin.mediation.MaxError;
import com.applovin.mediation.MaxReward;
import com.applovin.mediation.MaxRewardedAdListener;
import com.applovin.mediation.ads.MaxRewardedAd;
import com.applovin.sdk.AppLovinSdk;
import com.applovin.sdk.AppLovinSdkConfiguration;
import com.google.android.gms.ads.*;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.google.android.gms.ads.rewarded.RewardItem;
import com.google.android.gms.ads.rewardedinterstitial.RewardedInterstitialAd;
import com.google.android.gms.ads.rewardedinterstitial.RewardedInterstitialAdLoadCallback;
import com.netrocreative.chatgptapp.R;
import com.netrocreative.chatgptapp.util.PurchaseChecker;
import com.netrocreative.chatgptapp.util.Security;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class ResultActivity extends AppCompatActivity  implements MaxRewardedAdListener {

    private EditText question;
    private EditText result;
    private TextView resultTitle;
    private CardView start;
    private CardView resultCard;
    private LinearLayout copy;
    private TextView title;
    private ImageView back;

    private Dialog popupPricing;
    private ImageView close;
    private CardView pricing;

    private boolean firstStart = true;
    private boolean isRemoveAds = false;
    private boolean isSuccess = false;

    private String prompt;
    private String titleText;
    private int token = 0;
    private int temp = 0;

    private BillingClient billingClient;

    //AdMob
    private RewardedInterstitialAd rewardedInterstitialAd;

    //AppLovin
    private MaxRewardedAd appLovinRewardedAd;
    private int retryAttempt;

    private String completionEndpoint;

    private static final String TAG = "Stats";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);


        question = findViewById(R.id.question);
        result = findViewById(R.id.result);
        start = findViewById(R.id.start);
        resultCard = findViewById(R.id.resultCard);
        copy = findViewById(R.id.copy);
        title = findViewById(R.id.title);
        back = findViewById(R.id.back);
        resultTitle = findViewById(R.id.result_title);

        SharedPreferences prefs = getSharedPreferences("prefs", MODE_PRIVATE);
        firstStart = prefs.getBoolean("starting", true);

        if (firstStart) {
            getPopupData();
        }

        prompt = getIntent().getStringExtra("prompt");
        titleText = getIntent().getStringExtra("title");
        token = getIntent().getIntExtra("token", 0);
        temp = getIntent().getIntExtra("temp", 0);

        title.setText(titleText);

        //Ads initialization
        initAdMob();
        initAppLovin();


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


        copy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("label", result.getText());
                clipboard.setPrimaryClip(clip);
                Toast.makeText(ResultActivity.this, "Text copied to clipboard", Toast.LENGTH_SHORT).show();
            }
        });

        start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!question.getText().toString().isEmpty()) {
                    View currentFocus = ResultActivity.this.getCurrentFocus();
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(currentFocus.getWindowToken(), 0);
                    resultCard.setVisibility(View.VISIBLE);
                    resultTitle.setText("Query: " + question.getText());
                    result.setText("Please wait..");

                    if (question.getText().toString().length() > 0) {
                        getResponse(question.getText().toString());
                    }
                } else {
                    Toast.makeText(ResultActivity.this, "Please enter your requirement", Toast.LENGTH_SHORT).show();
                }
            }
        });

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String adMob = getString(R.string.adMob);
                String appLovin = getString(R.string.appLovin);
                String adPlatform = getString(R.string.adPlatform);

                if (adPlatform.equals(adMob)  && !isRemoveAds){
                    if (rewardedInterstitialAd != null) {
                        rewardedInterstitialAd.show(ResultActivity.this, new OnUserEarnedRewardListener() {
                            @Override
                            public void onUserEarnedReward(RewardItem rewardItem) {
                                finish();
                            }
                        });
                    } else {
                        finish();
                    }
                } else if (adPlatform.equals(appLovin)  && !isRemoveAds) {
                    appLovinRewardedAd.showAd();
                }else {
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
                startActivity(new Intent(ResultActivity.this, SubscriptionActivity.class));
            }
        });

        popupPricing.show();
    }


    private void getResponse(String query) {
        completionEndpoint = getResources().getString(R.string.completion_endpoint);
        final String API_KEY = getString(R.string.api_key);
        question.setText(query);
        RequestQueue queue = Volley.newRequestQueue(getApplicationContext());
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("model", "text-davinci-003");
            jsonObject.put("prompt", prompt + " " + query);
            jsonObject.put("temperature", temp);
            jsonObject.put("max_tokens", token);
            jsonObject.put("top_p", 1);
            jsonObject.put("frequency_penalty", 0.0);
            jsonObject.put("presence_penalty", 0.0);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        JsonObjectRequest postRequest = new JsonObjectRequest(
                Request.Method.POST, completionEndpoint, jsonObject,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            String responseMsg = response.getJSONArray("choices").getJSONObject(0).getString("text").trim();
                            result.setText(responseMsg.substring(responseMsg.indexOf("\n\n") + 2).trim());
                            question.setText("");
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e("TAGAPI", "Error is : " + error.getMessage() + "\n" + error);
                    }
                }
        ) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> params = new HashMap<>();
                params.put("Content-Type", "application/json");
                params.put("Authorization", "Bearer " + API_KEY);
                return params;
            }
        };

        postRequest.setRetryPolicy(new RetryPolicy() {
            @Override
            public int getCurrentTimeout() {
                return 50000;
            }

            @Override
            public int getCurrentRetryCount() {
                return 50000;
            }

            @Override
            public void retry(VolleyError error) throws VolleyError {
                // No need to implement retry logic here
            }
        });

        queue.add(postRequest);
    }




    private void initAdMob() {
        MobileAds.initialize(this, new OnInitializationCompleteListener() {
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

        appLovinRewardedAd = MaxRewardedAd.getInstance(getString(R.string.applovin_rewarded_ad_unit), ResultActivity.this);
        appLovinRewardedAd.setListener(ResultActivity.this);

        // Load the first ad
        appLovinRewardedAd.loadAd();
    }

    //AdMob Ads
    private void loadAdMobAds() {
        RewardedInterstitialAd.load(this, getString(R.string.admob_rewarded_ad_unit),
                new AdRequest.Builder().build(), new RewardedInterstitialAdLoadCallback() {
                    @Override
                    public void onAdLoaded(RewardedInterstitialAd ad) {
                        rewardedInterstitialAd = ad;
                        rewardedInterstitialAd.setFullScreenContentCallback(new FullScreenContentCallback() {
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
                                rewardedInterstitialAd = null;
                                finish();
                            }

                            @Override
                            public void onAdFailedToShowFullScreenContent(AdError adError) {
                                // Called when ad fails to show.
                                Log.e(TAG, "Ad failed to show fullscreen content.");
                                rewardedInterstitialAd = null;
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
                    public void onAdFailedToLoad(LoadAdError adError) {
                        Log.d(TAG, adError != null ? adError.toString() : "Unknown load ad error");
                        rewardedInterstitialAd = null;
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
    }

    @Override
    public void onAdHidden(MaxAd maxAd) {
        appLovinRewardedAd.loadAd();
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
                appLovinRewardedAd.loadAd();
            }
        }, delayMillis);
    }

    @Override
    public void onAdDisplayFailed(MaxAd maxAd, MaxError maxError) {
        appLovinRewardedAd.loadAd();
        Log.d(TAG, "Ad display failed");
    }

    @Override
    public void onUserRewarded(MaxAd maxAd, MaxReward maxReward) {

    }

        @Override
    public void onRewardedVideoStarted(MaxAd maxAd) {

    }

    @Override
    public void onRewardedVideoCompleted(MaxAd maxAd) {
        finish();
    }
    //....................................

    @Override
    public void onBackPressed() {

        String adMob = getString(R.string.adMob);
        String appLovin = getString(R.string.appLovin);
        String adPlatform = getString(R.string.adPlatform);

        if (adPlatform.equals(adMob)  && !isRemoveAds){
            if (rewardedInterstitialAd != null) {
                rewardedInterstitialAd.show(ResultActivity.this, new OnUserEarnedRewardListener() {
                    @Override
                    public void onUserEarnedReward(RewardItem rewardItem) {
                        finish();
                    }
                });
            } else {
                finish();
            }
        } else if (adPlatform.equals(appLovin)  && !isRemoveAds) {
            appLovinRewardedAd.showAd();
        }else {
            finish();
        }

    }
}

