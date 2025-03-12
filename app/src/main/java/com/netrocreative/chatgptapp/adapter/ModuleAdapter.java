package com.netrocreative.chatgptapp.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.netrocreative.chatgptapp.R;
import com.netrocreative.chatgptapp.model.Modules;

import java.util.List;

public class ModuleAdapter extends RecyclerView.Adapter<ModuleAdapter.ViewHolder> {
    private Context context;
    private List<Modules> mList;
    private OnItemClickListener onItemClickListener;

    public ModuleAdapter(Context context, List<Modules> mList) {
        this.context = context;
        this.mList = mList;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.module_items, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Modules modules = mList.get(position);
        Glide.with(context).load(modules.getImage()).into(holder.image);
        holder.title.setText(modules.getTitle());
        holder.description.setText(modules.getDescription());

        if (position==0){
            holder.isNew.setVisibility(View.VISIBLE);
        }if (position==1){
            holder.isNew.setVisibility(View.VISIBLE);
        }if (position==2){
            holder.isNew.setVisibility(View.VISIBLE);
        }

    }

    @Override
    public int getItemCount() {
        return mList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        ImageView image, isNew;
        TextView title;
        TextView description;
        CardView moduleCard;

        public ViewHolder(View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.image);
            isNew = itemView.findViewById(R.id.is_new);
            title = itemView.findViewById(R.id.title);
            description = itemView.findViewById(R.id.description);
            moduleCard = itemView.findViewById(R.id.module_card);

            moduleCard.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        Modules modules = mList.get(position);
                        if (onItemClickListener != null) {
                            onItemClickListener.onItemClick(modules.getTitle());
                        }
                    }
                }
            });
        }
    }

    public interface OnItemClickListener {
        void onItemClick(String title);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        onItemClickListener = listener;
    }
}
