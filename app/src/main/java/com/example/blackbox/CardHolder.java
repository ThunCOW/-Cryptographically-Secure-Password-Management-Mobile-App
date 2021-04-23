package com.example.blackbox;

import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class CardHolder extends RecyclerView.ViewHolder{

    ImageView img;
    TextView acc, pass, txt_Copy, title, txt_View;

    public CardHolder(@NonNull View itemView) {
        super(itemView);

        this.img = itemView.findViewById(R.id.img_Card);
        this.acc = itemView.findViewById(R.id.txt_Hid_Acc);
        this.pass = itemView.findViewById(R.id.txt_Hid_Pass);
        this.txt_Copy = itemView.findViewById(R.id.txt_Copy);
        this.txt_View = itemView.findViewById(R.id.txt_view);
        this.title = itemView.findViewById(R.id.category_title);
        //this.rowLayout = itemView.findViewById(R.id.rowLayout);
    }
}
