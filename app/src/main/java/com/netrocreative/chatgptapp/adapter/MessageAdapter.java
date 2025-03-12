package com.netrocreative.chatgptapp.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;

import com.netrocreative.chatgptapp.R;
import com.netrocreative.chatgptapp.activity.ChatActivity;
import com.netrocreative.chatgptapp.model.Message;

import java.util.List;


public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MyViewHolder> {
    private List<Message> messageList;
    private Context context;
    private int lastInsertedPosition = -1;

    public MessageAdapter(List<Message> messageList, Context context) {
        this.messageList = messageList;
        this.context = context;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View chatView = LayoutInflater.from(parent.getContext()).inflate(R.layout.chat_item, null);
        return new MyViewHolder(chatView);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        Message message = messageList.get(position);
        if (message.getSentBy().equals(Message.SENT_BY_ME)) {
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
            holder.leftTextView.setText(message.getMessage());

            if (position > lastInsertedPosition) {
                loadAnimation(holder.leftChatView, R.anim.slide_in_left, "bot");
                lastInsertedPosition = position;
            }
        }
    }

    @Override
    public int getItemCount() {
        return messageList.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        private LinearLayout leftChatView;
        private LinearLayout rightChatView;
        private TextView leftTextView;
        private TextView rightTextView;

        public MyViewHolder(View itemView) {
            super(itemView);
            leftChatView = itemView.findViewById(R.id.left_chat_view);
            rightChatView = itemView.findViewById(R.id.right_chat_view);
            leftTextView = itemView.findViewById(R.id.left_chat_text_view);
            rightTextView = itemView.findViewById(R.id.right_chat_text_view);
        }
    }

    public void loadAnimation(View view, int animation, String category) {
        Animation anim = AnimationUtils.loadAnimation(context, animation);
        if (category.equals("bot")) {
            anim.setStartOffset(300);
        }
        view.startAnimation(anim);
    }
}