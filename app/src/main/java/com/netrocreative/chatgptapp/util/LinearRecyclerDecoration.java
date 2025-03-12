package com.netrocreative.chatgptapp.util;

import android.graphics.Rect;
import android.view.View;

import androidx.recyclerview.widget.RecyclerView;

public class LinearRecyclerDecoration extends RecyclerView.ItemDecoration {
    private int topPadding;
    private int bottomPadding;
    private int sidePadding;

    public LinearRecyclerDecoration(int topPadding, int bottomPadding, int sidePadding) {
        this.topPadding = topPadding;
        this.bottomPadding = bottomPadding;
        this.sidePadding = sidePadding;
    }

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
        super.getItemOffsets(outRect, view, parent, state);
        int itemCount = state.getItemCount();
        if (itemCount > 0 && parent.getChildAdapterPosition(view) == itemCount - 1) {
            outRect.bottom = bottomPadding;
        }
        outRect.top = topPadding;
        outRect.right = sidePadding;
        outRect.left = sidePadding;
    }
}
