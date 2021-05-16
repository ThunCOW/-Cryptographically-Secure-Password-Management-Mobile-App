package com.example.blackbox;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class CardAdapter extends RecyclerView.Adapter<CardHolder>{

    Context c;
    ArrayList<CardModel> cards; // this array list create a list of array which parameters define in the cardmodel class

    //constructor
    public CardAdapter(Context c, ArrayList<CardModel> cards) {
        this.c = c;
        this.cards = cards;
    }

    @NonNull
    @Override
    public CardHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {

        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.row, null); //inflate the row

        return new CardHolder(view); // return the view to cardholder
    }

    @Override
    public void onBindViewHolder(@NonNull CardHolder holder, int position) {

        holder.setId(cards.get(position).getId());
        holder.acc.setText(cards.get(position).getAcc());
        holder.pass.setText(cards.get(position).getPass());
        holder.title.setText(cards.get(position).getTitle());
        holder.img.setImageResource(cards.get(position).getImg());
        holder.txt_Copy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ClipboardManager clipboard = (ClipboardManager) view.getContext().getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("txtCopy", cards.get(position).getPass());
                clipboard.setPrimaryClip(clip);

                Toast.makeText(view.getContext(), "copied", Toast.LENGTH_SHORT).show();
            }
        });
        holder.txt_View.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DataBaseHelper db = new DataBaseHelper(view.getContext());
                //decryption
                try {
                    Toast.makeText(view.getContext(), "id = "+cards.get(position).getId(), Toast.LENGTH_SHORT).show();
                    AES aes = new AES();
                    holder.pass.setText(aes.decrypt(db.getPassword(cards.get(position).getId())));
                } catch (Exception e) {
                    Toast.makeText(view.getContext(), "DECRYPT FAILURE", Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return cards.size();
    }
}
