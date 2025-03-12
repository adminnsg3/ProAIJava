package com.netrocreative.chatgptapp.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.MediaScannerConnection;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.netrocreative.chatgptapp.R;
import com.netrocreative.chatgptapp.activity.ImageGenerationActivity;
import com.netrocreative.chatgptapp.model.Message;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

public class ImageResponseAdapter extends RecyclerView.Adapter<ImageResponseAdapter.MyViewHolder> {

    private List<Message> messageList;
    private ImageGenerationActivity activity;
    private int lastInsertedPosition = -1;
    private OnItemClickListener listener;

    public ImageResponseAdapter(List<Message> messageList, ImageGenerationActivity activity) {
        this.messageList = messageList;
        this.activity = activity;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View chatView = LayoutInflater.from(parent.getContext()).inflate(R.layout.image_response_item, null);
        return new MyViewHolder(chatView);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        Message message = messageList.get(position);
        if (message.getSentBy().equals(Message.SENT_BY_ME)) {
            holder.leftChatViewImage.setVisibility(View.GONE);
            holder.leftChatView.setVisibility(View.GONE);
            holder.rightChatView.setVisibility(View.VISIBLE);
            holder.rightTextView.setText(message.getMessage());

            if (position > lastInsertedPosition) {
                loadAnimation(holder.rightChatView, R.anim.slide_in_right, "user");
                lastInsertedPosition = position;
            }

        } else {
            holder.rightChatView.setVisibility(View.GONE);
            holder.leftChatView.setVisibility(View.VISIBLE);
            holder.leftChatViewImage.setVisibility(View.VISIBLE);

            if (message.getMessage().startsWith("https://")) {
                holder.leftChatView.setVisibility(View.GONE);
                Glide.with(activity)
                        .load(message.getMessage())
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .into(holder.leftImageView);
            } else {
                holder.leftChatViewImage.setVisibility(View.GONE);
                holder.leftTextView.setText(message.getMessage());
            }

            if (position > lastInsertedPosition) {
                loadAnimation(holder.leftChatView, R.anim.slide_in_left, "bot");
                lastInsertedPosition = position;
            }
        }

        holder.download.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onItemClick(holder.leftImageView);
            }
        });
    }

    @Override
    public int getItemCount() {
        return messageList.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        private CardView leftChatViewImage;
        private LinearLayout leftChatView;
        private LinearLayout rightChatView;
        private ImageView leftImageView;
        private ImageView download;
        private TextView rightTextView;
        private TextView leftTextView;

        public MyViewHolder(View itemView) {
            super(itemView);
            leftChatViewImage = itemView.findViewById(R.id.left_chat_view_image);
            leftChatView = itemView.findViewById(R.id.left_chat_view);
            rightChatView = itemView.findViewById(R.id.right_chat_view);
            download = itemView.findViewById(R.id.download);
            leftImageView = itemView.findViewById(R.id.left_chat_image_view);
            rightTextView = itemView.findViewById(R.id.right_chat_text_view);
            leftTextView = itemView.findViewById(R.id.left_chat_text_view);
        }
    }

    private void loadAnimation(View view, int animation, String category) {
        Animation anim = AnimationUtils.loadAnimation(activity, animation);
        if (category.equals("bot")) {
            anim.setStartOffset(300);
        }
        view.startAnimation(anim);
    }



    public interface OnItemClickListener {
        void onItemClick(ImageView imageView);
    }


    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }
}