package com.netrocreative.chatgptapp.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.viewpager.widget.PagerAdapter;

import com.netrocreative.chatgptapp.R;

public class OnboardingAdapter extends PagerAdapter {

    private Context context;
    private int[] images = {
            R.drawable.onboarding_one,
            R.drawable.onboarding_two,
            R.drawable.onboarding_three
    };
    private String[] title;
    private String[] desc;

    public OnboardingAdapter(Context context) {
        this.context = context;
        this.title = new String[]{
                context.getResources().getString(R.string.first_slide_title),
                context.getResources().getString(R.string.second_slide_title),
                context.getResources().getString(R.string.third_slide_title)
        };
        this.desc = new String[]{
                context.getResources().getString(R.string.first_slide_desc),
                context.getResources().getString(R.string.second_slide_desc),
                context.getResources().getString(R.string.third_slide_desc)
        };
    }

    @Override
    public int getCount() {
        return images.length;
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        LayoutInflater inflater = LayoutInflater.from(context);
        ViewGroup layout = (ViewGroup) inflater.inflate(R.layout.onboarding_layout, container, false);

        ImageView imageView = layout.findViewById(R.id.slider_image);
        TextView titleText = layout.findViewById(R.id.slider_heading);
        TextView descriptionText = layout.findViewById(R.id.slider_desc);

        imageView.setImageResource(images[position]);
        titleText.setText(title[position]);
        descriptionText.setText(desc[position]);

        container.addView(layout);
        return layout;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((View) object);
    }
}
