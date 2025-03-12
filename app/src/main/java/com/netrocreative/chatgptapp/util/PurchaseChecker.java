package com.netrocreative.chatgptapp.util;

import com.android.billingclient.api.BillingClient;

import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import com.android.billingclient.api.AcknowledgePurchaseParams;
import com.android.billingclient.api.AcknowledgePurchaseResponseListener;
import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingClientStateListener;
import com.android.billingclient.api.BillingResult;
import com.android.billingclient.api.ConsumeParams;
import com.android.billingclient.api.ConsumeResponseListener;
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.PurchasesUpdatedListener;
import com.android.billingclient.api.QueryPurchasesParams;
import com.netrocreative.chatgptapp.R;

import java.io.IOException;
import java.util.concurrent.Executors;

public class PurchaseChecker extends AppCompatActivity {

    private BillingClient billingClient;
    private boolean isRemoveAds = false;
    private boolean isSuccess = false;
    private PurchaseStatusCallback callback;

    private static final String TAG = "Stats";

    //Play Store Subscription Check
    public final PurchasesUpdatedListener purchasesUpdatedListener =
            (billingResult, purchases) -> {
                if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK && purchases != null) {
                    for (Purchase purchase : purchases) {
                        String packageName = purchase.getOrderId();
                        Log.d("billingClient", "packageName: " + packageName);
                        handlePurchase(purchase);
                    }
                } else if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED) {
                    Log.d(TAG, "ITEM_ALREADY_OWNED");
                    isSuccess = true;
                } else if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.ITEM_UNAVAILABLE) {
                    Log.d(TAG, "ITEM_UNAVAILABLE");
                } else {
                    Log.d(TAG, billingResult.getDebugMessage());
                }
            };

    private void handlePurchase(Purchase purchase) {
        ConsumeParams consumeParams = ConsumeParams.newBuilder()
                .setPurchaseToken(purchase.getPurchaseToken())
                .build();

        ConsumeResponseListener listener = (billingResult, s) -> {
            if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                // Handle successful consumption if needed
            }
        };

        billingClient.consumeAsync(consumeParams, listener);

        if (purchase.getPurchaseState() == Purchase.PurchaseState.PURCHASED) {
            if (!verifyValidSignature(purchase.getOriginalJson(), purchase.getSignature())) {
                Log.d(TAG, "Invalid Purchase");
                return;
            }

            if (purchase.isAcknowledged()) {
                AcknowledgePurchaseParams acknowledgePurchaseParams = AcknowledgePurchaseParams.newBuilder()
                        .setPurchaseToken(purchase.getPurchaseToken())
                        .build();

                billingClient.acknowledgePurchase(acknowledgePurchaseParams, acknowledgePurchaseResponseListener);
                Log.d(TAG, "Subscribed");
                isSuccess = true;
            } else {
                Log.d(TAG, "Already Subscribed");
            }
        } else if (purchase.getPurchaseState() == Purchase.PurchaseState.PENDING) {
            Log.d(TAG, "PENDING");
        } else if (purchase.getPurchaseState() == Purchase.PurchaseState.UNSPECIFIED_STATE) {
            Log.d(TAG, "UNSPECIFIED_STATE");
        }
    }

    private final AcknowledgePurchaseResponseListener acknowledgePurchaseResponseListener = billingResult -> {
        if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
            Log.d(TAG, "Subscribed");
            isSuccess = true;
        }
    };

    private boolean verifyValidSignature(String signedData, String signature) {
        try {
            String base64key = getApplicationContext().getResources().getString(R.string.base64key);
            Security security = new Security();
            return security.verifyPurchase(base64key, signedData, signature);
        } catch (IOException e) {
            return false;
        }
    }

    public void purchaseQuery(BillingClient billingClient) {
        this.billingClient = billingClient;

        billingClient.startConnection(new BillingClientStateListener() {
            @Override
            public void onBillingServiceDisconnected() {
                // Not yet implemented
            }

            @Override
            public void onBillingSetupFinished(BillingResult billingResult) {
                Executors.newSingleThreadExecutor().execute(() -> {
                    try {
                        billingClient.queryPurchasesAsync(
                                QueryPurchasesParams.newBuilder()
                                        .setProductType(BillingClient.SkuType.SUBS)
                                        .build(),
                                (billingResult1, purchaseList) -> {
                                    for (Purchase purchase : purchaseList) {
                                        if (purchase != null) {
                                            isRemoveAds = true;
                                            Log.d(TAG, "onBillingSetupFinished: "+isRemoveAds);
                                        }
                                    }
                                    if (callback != null) {
                                        Log.d(TAG, "onBillingSetupFinished: "+isRemoveAds);
                                        callback.onPurchaseStatusUpdated(isRemoveAds);
                                    }
                                }
                        );
                    } catch (Exception e) {
                        isRemoveAds = false;
                        if (callback != null) {
                            callback.onPurchaseStatusUpdated(isRemoveAds);
                        }
                    }
                });

                runOnUiThread(() -> {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        // Not yet implemented
                    }
                });
            }
        });
    }

    public interface PurchaseStatusCallback {
        void onPurchaseStatusUpdated(boolean removeAds);
    }

    public void setPurchaseStatusCallback(PurchaseStatusCallback callback) {
        this.callback = callback;
    }
}
