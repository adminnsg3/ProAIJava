package com.netrocreative.chatgptapp.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;

import com.netrocreative.chatgptapp.R;
import com.netrocreative.chatgptapp.adapter.ModuleAdapter;
import com.netrocreative.chatgptapp.adapter.SliderAdapter;
import com.netrocreative.chatgptapp.model.Modules;
import com.netrocreative.chatgptapp.model.SliderData;
import com.netrocreative.chatgptapp.util.LinearRecyclerDecoration;
import com.onesignal.OneSignal;
import com.smarteist.autoimageslider.IndicatorView.animation.type.IndicatorAnimationType;
import com.smarteist.autoimageslider.SliderAnimations;
import com.smarteist.autoimageslider.SliderView;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private SliderView sliderView;
    private RecyclerView recyclerView;
    private ImageView website;
    private ImageView aboutUS;
    private ImageView settings;

    private List<Modules> itemsList = new ArrayList<>();
    private ModuleAdapter moduleAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        website = findViewById(R.id.website);
        aboutUS = findViewById(R.id.about_us);
        settings = findViewById(R.id.settings);
        sliderView = findViewById(R.id.imageSlider);
        recyclerView = findViewById(R.id.recyclerview);
        int topPadding = getResources().getDimensionPixelSize(R.dimen.topPadding);
        int bottomPadding = getResources().getDimensionPixelSize(R.dimen.bottomPadding);
        int sidePadding = getResources().getDimensionPixelSize(R.dimen.sidePadding);
        recyclerView.addItemDecoration(
                new LinearRecyclerDecoration(topPadding, bottomPadding, sidePadding)
        );

        // OneSignal Initialization
        OneSignal.initWithContext(this);
        OneSignal.setAppId(getString(R.string.oen_signal_app_id));

        List<SliderData> sliderDataArrayList = new ArrayList<>();

        sliderView.setIndicatorAnimation(IndicatorAnimationType.WORM);
        sliderView.setSliderTransformAnimation(SliderAnimations.SIMPLETRANSFORMATION);

        sliderDataArrayList.add(new SliderData(R.drawable.banner1));
        sliderDataArrayList.add(new SliderData(R.drawable.banner2));
        sliderDataArrayList.add(new SliderData(R.drawable.banner3));

        SliderAdapter adapter = new SliderAdapter(this, sliderDataArrayList);
        sliderView.setAutoCycleDirection(SliderView.LAYOUT_DIRECTION_LTR);
        sliderView.setSliderAdapter(adapter);
        sliderView.setScrollTimeInSec(3);
        sliderView.setAutoCycle(true);
        sliderView.startAutoCycle();

        moduleAdapter = new ModuleAdapter(this, itemsList);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(moduleAdapter);

        moduleAdapter.setOnItemClickListener(new ModuleAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(String title) {
                if (title.equals("Personal Assistant")) {
                    Intent intent = new Intent(MainActivity.this, ChatActivity.class);
                    intent.putExtra("title", title);
                    startActivity(intent);
                } else if (title.equals("Image Generation")) {
                    Intent intent = new Intent(MainActivity.this, ImageGenerationActivity.class);
                    intent.putExtra("title", title);
                    startActivity(intent);
                } else if (title.equals("Marketing Plan")) {
                    sendData("Act like a professional marketing expert and give well organized marketing plan on",
                            "Marketing Plan",
                            450,
                            1);
                }else if (title.equals("Professionally Writing")) {
                    sendData("Act like a professional writer. Use professional words and write professionally on",
                            "Professionally Writing",
                            400,
                            1);
                } else if (title.equals("Blog Writing")) {
                    sendData("Act like a blog writer. Maintain blog format and write professional blog on",
                            "Blog Writing",
                            550,
                            1);
                } else if (title.equals("Grammar Correction")) {
                    sendData("Act like a grammar expert and fix all the misspellings and grammatical issues and provide solutions only on",
                            "Grammar Correction",
                            100,
                            0);
                } else if (title.equals("Summarize Data")) {
                    sendData("Act like a summary expert and summarize the text on",
                            "Summarize Data",
                            250,
                            1);
                } else if (title.equals("Product Description")) {
                    sendData("Act like a product description writer. Write a product description on",
                            "Product Description",
                            250,
                            1);
                } else if (title.equals("Financial Advisor")) {
                    sendData("Act like a financial advisor. Give professional financial advice on",
                            "Financial Advisor",
                            200,
                            1);
                } else if (title.equals("Math Problems")) {
                    sendData("Act like a mathematical expert. Give mathematical line-by-line solutions on",
                            "Math Problems",
                            100,
                            1);
                } else if (title.equals("Essay Writing")) {
                    sendData("Act like a professional essay writer, write in a professional format on",
                            "Essay Writing",
                            600,
                            1);
                } else if (title.equals("Letter Writing")) {
                    sendData("Act like a professional letter writer, write in full format with placeholders on",
                            "Letter Writing",
                            450,
                            1);
                }
            }
        });

        loadModules();

        website.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, WebsiteActivity.class));
            }
        });

        aboutUS.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, AboutUsActivity.class));
            }
        });

        settings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, SettingsActivity.class));
            }
        });

    }

    private void sendData(String prompt, String title, int maxToken, int temp) {
        Intent intent = new Intent(MainActivity.this, ResultActivity.class);
        intent.putExtra("prompt", prompt);
        intent.putExtra("title", title);
        intent.putExtra("token", maxToken);
        intent.putExtra("temp", temp);
        startActivity(intent);
    }

    private void loadModules() {
        itemsList.add(new Modules(R.drawable.module_personal,
                "Personal Assistant",
                "Chat with your very own personal assistant at your service"));
        itemsList.add(new Modules(R.drawable.module_image,
                "Image Generation",
                "Generate images with the power of Dall-E"));

        itemsList.add(new Modules(R.drawable.module_marketing,
                "Marketing Plan",
                "Get structured marketing plan with the help om most powerful AI"));

        itemsList.add(new Modules(R.drawable.module_professional_writing,
                "Professionally Writing",
                "Make your existing texts sound more professional"));

        itemsList.add(new Modules(R.drawable.module_blog,
                "Blog Writing",
                "Write blogs with the power of AI"));

        itemsList.add(new Modules(R.drawable.module_grammar,
                "Grammar Correction",
                "Fix grammatical issues in your writing"));

        itemsList.add(new Modules(R.drawable.module_summarize,
                "Summarize Data",
                "Get the key information from your text"));

        itemsList.add(new Modules(R.drawable.module_product,
                "Product Description",
                "Write your very own product description"));

        itemsList.add(new Modules(R.drawable.module_financial,
                "Financial Advisor",
                "Your very own financial adviser"));

        itemsList.add(new Modules(R.drawable.module_math,
                "Math Problems",
                "Give math problems, get solutions"));

        itemsList.add(new Modules(R.drawable.module_essay,
                "Essay Writing",
                "Write essay with the power of AI"));

        itemsList.add(new Modules(R.drawable.module_letter,
                "Letter Writing",
                "Write your very own personalized letter"));

    }

}
