package com.netrocreative.chatgptapp.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.cardview.widget.CardView;

import com.android.billingclient.api.AcknowledgePurchaseParams;
import com.android.billingclient.api.AcknowledgePurchaseResponseListener;
import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingClientStateListener;
import com.android.billingclient.api.BillingFlowParams;
import com.android.billingclient.api.BillingResult;
import com.android.billingclient.api.ConsumeParams;
import com.android.billingclient.api.ConsumeResponseListener;
import com.android.billingclient.api.ProductDetails;
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.PurchasesUpdatedListener;
import com.android.billingclient.api.QueryProductDetailsParams;
import com.android.billingclient.api.QueryPurchasesParams;
import com.netrocreative.chatgptapp.R;
import com.netrocreative.chatgptapp.util.Security;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SubscriptionActivity extends AppCompatActivity {

    private ImageView back;
    private BillingClient billingClient;
    private String responseMonthly;
    private String responseHalfYearly;
    private String responseYearly;
    private String bundle;
    private boolean isSuccess = false;
    private boolean isSubscribed = false;

    private CardView monthly;
    private CardView halfYearly;
    private CardView yearly;
    private TextView monthlyPrice;
    private TextView halfYearlyPrice;
    private TextView yearlyPrice;
    private TextView monthlyBtnText;
    private TextView halfYearlyBtnText;
    private TextView yearlyBtnText;
    private Dialog popupLoading;
    private Dialog popupMessage;

    private ImageView close;
    private TextView message;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_subscription);

        back = findViewById(R.id.back);
        monthly = findViewById(R.id.monthly);
        halfYearly = findViewById(R.id.half_yearly);
        yearly = findViewById(R.id.yearly);
        monthlyPrice = findViewById(R.id.monthly_price);
        halfYearlyPrice = findViewById(R.id.half_yearly_price);
        yearlyPrice = findViewById(R.id.yearly_price);
        monthlyBtnText = findViewById(R.id.monthly_btn_text);
        halfYearlyBtnText = findViewById(R.id.half_yearly_btn_text);
        yearlyBtnText = findViewById(R.id.yearly_btn_text);

        getPopupData();

        billingClient = BillingClient.newBuilder(this)
                .setListener(purchasesUpdatedListener)
                .enablePendingPurchases()
                .build();

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        monthly.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isSubscribed) {
                    message.setText("You are already subscribed to a package. Cancel it from Google Play Store to update to another package.");
                    popupMessage.show();
                } else {
                    bundle = getString(R.string.sub_m);
                    getSubscription(getString(R.string.sub_m));
                }
            }
        });

        halfYearly.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isSubscribed) {
                    message.setText("You are already subscribed to a package. Cancel it from Google Play Store to update to another package.");
                    popupMessage.show();
                } else {
                    bundle = getString(R.string.sub_h);
                    getSubscription(getString(R.string.sub_h));
                }
            }
        });

        yearly.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isSubscribed) {
                    message.setText("You are already subscribed to a package. Cancel it from Google Play Store to update to another package.");
                    popupMessage.show();
                } else {
                    bundle = getString(R.string.sub_y);
                    getSubscription(getString(R.string.sub_y));
                }
            }
        });

        getPrice();
    }

    private void getPopupData() {
        popupLoading = new Dialog(this);
        popupLoading.setContentView(R.layout.popup_loading);
        popupLoading.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        popupLoading.setCancelable(false);
        popupLoading.show();

        popupMessage = new Dialog(this);
        popupMessage.setContentView(R.layout.popup_message);
        popupMessage.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        close = popupMessage.findViewById(R.id.close);
        message = popupMessage.findViewById(R.id.message);

        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                popupMessage.dismiss();
            }
        });
    }

    private final PurchasesUpdatedListener purchasesUpdatedListener =
            new PurchasesUpdatedListener() {
                @Override
                public void onPurchasesUpdated(BillingResult billingResult, List<Purchase> purchases) {
                    if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK && purchases != null) {
                        for (Purchase purchase : purchases) {
                            handlePurchase(purchase);
                        }
                    } else if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED) {
                        isSubscribed = true;
                        Toast.makeText(SubscriptionActivity.this, "Purchase completed", Toast.LENGTH_SHORT).show();
                    } else if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.ITEM_UNAVAILABLE) {
                        Toast.makeText(SubscriptionActivity.this, "ITEM_UNAVAILABLE", Toast.LENGTH_SHORT).show();
                    } else {
                        // Handle other response codes
                    }
                }
            };

    private void handlePurchase(Purchase purchase) {
        ConsumeParams consumeParams = ConsumeParams.newBuilder()
                .setPurchaseToken(purchase.getPurchaseToken())
                .build();

        ConsumeResponseListener listener = new ConsumeResponseListener() {
            @Override
            public void onConsumeResponse(BillingResult billingResult, String s) {
                if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                    // Handle successful consumption
                }
            }
        };

        billingClient.consumeAsync(consumeParams, listener);

        if (purchase.getPurchaseState() == Purchase.PurchaseState.PURCHASED) {
            if (!verifyValidSignature(purchase.getOriginalJson(), purchase.getSignature())) {
                Toast.makeText(this, "Invalid Purchase", Toast.LENGTH_SHORT).show();
                return;
            }

            if (purchase.isAcknowledged()) {
                AcknowledgePurchaseParams acknowledgePurchaseParams = AcknowledgePurchaseParams.newBuilder()
                        .setPurchaseToken(purchase.getPurchaseToken())
                        .build();

                billingClient.acknowledgePurchase(acknowledgePurchaseParams, acknowledgePurchaseResponseListener);

                message.setText("Your purchase was successful!");
                popupMessage.show();
                isSuccess = true;
            } else {
                isSubscribed = true;
                message.setText("Your purchase was successful!");
                popupMessage.show();

                if (bundle.equals(getString(R.string.sub_m))) {
                    monthlyBtnText.setText("Subscribed");
                }
                if (bundle.equals(getString(R.string.sub_h))) {
                    halfYearlyBtnText.setText("Subscribed");
                }
                if (bundle.equals(getString(R.string.sub_y))) {
                    yearlyBtnText.setText("Subscribed");
                }
            }
        } else if (purchase.getPurchaseState() == Purchase.PurchaseState.PENDING) {
            Toast.makeText(this, "PENDING", Toast.LENGTH_SHORT).show();
        } else if (purchase.getPurchaseState() == Purchase.PurchaseState.UNSPECIFIED_STATE) {
            Toast.makeText(this, "UNSPECIFIED_STATE", Toast.LENGTH_SHORT).show();
        }
    }

    private AcknowledgePurchaseResponseListener acknowledgePurchaseResponseListener = new AcknowledgePurchaseResponseListener() {
        @Override
        public void onAcknowledgePurchaseResponse(BillingResult billingResult) {
            if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                isSubscribed = true;
                Toast.makeText(SubscriptionActivity.this, "Subscribed", Toast.LENGTH_SHORT).show();
            }
        }
    };

    private boolean verifyValidSignature(String signedData, String signature) {
        try {
            String base64key = getString(R.string.base64key);
            Security security = new Security();
            return security.verifyPurchase(base64key, signedData, signature);
        } catch (IOException e) {
            return false;
        }
    }

    private void getPrice() {

        billingClient.startConnection(new BillingClientStateListener() {
            @Override
            public void onBillingServiceDisconnected() {
                // Handle billing service disconnection
            }

            @Override
            public void onBillingSetupFinished(BillingResult billingResult) {
                List<String> subscriptionProducts = Arrays.asList(
                        getString(R.string.sub_m),
                        getString(R.string.sub_h),
                        getString(R.string.sub_y)
                );

                ExecutorService executorService = Executors.newSingleThreadExecutor();
                executorService.execute(() -> {
                    for (String productId : subscriptionProducts) {
                        List<QueryProductDetailsParams.Product> productList = Collections.singletonList(
                                QueryProductDetailsParams.Product.newBuilder()
                                        .setProductId(productId)
                                        .setProductType(BillingClient.ProductType.SUBS)
                                        .build()
                        );

                        QueryProductDetailsParams params = QueryProductDetailsParams.newBuilder()
                                .setProductList(productList)
                                .build();


                        billingClient.queryProductDetailsAsync(params, (billingResult2, productDetailsList) -> {
                            for (ProductDetails productDetails : productDetailsList) {
                                switch (productId) {
                                    case "sub_m":
                                        responseMonthly = productDetails.getSubscriptionOfferDetails()
                                                .get(0).getPricingPhases().getPricingPhaseList()
                                                .get(0).getFormattedPrice();
                                        break;
                                    case "sub_h":
                                        responseHalfYearly = productDetails.getSubscriptionOfferDetails()
                                                .get(0).getPricingPhases().getPricingPhaseList()
                                                .get(0).getFormattedPrice();
                                        break;
                                    case "sub_y":
                                        responseYearly = productDetails.getSubscriptionOfferDetails()
                                                .get(0).getPricingPhases().getPricingPhaseList()
                                                .get(0).getFormattedPrice();
                                        break;
                                }
                                popupLoading.dismiss();
                            }
                        });
                    }
                });

                runOnUiThread(() -> {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    // Update UI with product prices
                    monthlyPrice.setText(responseMonthly);
                    halfYearlyPrice.setText(responseHalfYearly);
                    yearlyPrice.setText(responseYearly);
                });

                billingClient.queryPurchasesAsync(QueryPurchasesParams.newBuilder()
                        .setProductType(BillingClient.ProductType.SUBS)
                        .build(), (queryBillingResult, purchaseList) -> {
                    if (queryBillingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                        for (Purchase purchase : purchaseList) {
                            if (purchase != null) {
                                isSubscribed = true;

                                String jsonString = purchase.getOriginalJson();
                                try {
                                    JSONObject jsonObject = new JSONObject(jsonString);
                                    String productId = jsonObject.getString("productId");

                                    if (productId.equals(getString(R.string.sub_m))) {
                                        monthlyBtnText.setText("Subscribed");
                                    }
                                    if (productId.equals(getString(R.string.sub_h))) {
                                        halfYearlyBtnText.setText("Subscribed");
                                    }
                                    if (productId.equals(getString(R.string.sub_y))) {
                                        yearlyBtnText.setText("Subscribed");
                                    }
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    } else {
                        Toast.makeText(getApplicationContext(), "Something went wrong!", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    private void getSubscription(String packageName) {
        if (!isSubscribed) {
            billingClient.startConnection(new BillingClientStateListener() {
                @Override
                public void onBillingSetupFinished(BillingResult billingResult) {
                    if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                        List<QueryProductDetailsParams.Product> productList = Arrays.asList(
                                QueryProductDetailsParams.Product.newBuilder()
                                        .setProductId(packageName)
                                        .setProductType(BillingClient.ProductType.SUBS)
                                        .build()
                        );

                        QueryProductDetailsParams params = QueryProductDetailsParams.newBuilder()
                                .setProductList(productList)
                                .build();

                        billingClient.queryProductDetailsAsync(params, (billingResult2, productDetailsList) -> {
                            for (ProductDetails productDetails : productDetailsList) {
                                String offerToken = productDetails.getSubscriptionOfferDetails().get(0).getOfferToken();
                                List<BillingFlowParams.ProductDetailsParams> productDetailsParamsList = Arrays.asList(
                                        offerToken != null ? BillingFlowParams.ProductDetailsParams.newBuilder()
                                                .setProductDetails(productDetails)
                                                .setOfferToken(offerToken)
                                                .build() : null
                                );

                                BillingFlowParams billingFlowParams = BillingFlowParams.newBuilder()
                                        .setProductDetailsParamsList(productDetailsParamsList)
                                        .build();

                                BillingResult result = billingClient.launchBillingFlow(
                                        SubscriptionActivity.this, billingFlowParams
                                );
                            }
                        });
                    }
                }

                @Override
                public void onBillingServiceDisconnected() {
                    // Handle billing service disconnection
                }
            });
        } else {
            message.setText("You are already subscribed to a package. Cancel it from Google Play Store to update to another package.");
            popupMessage.show();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (billingClient != null) {
            billingClient.endConnection();
        }
    }

}
