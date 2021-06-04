package com.example.blackbox;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputFilter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import javax.crypto.SecretKey;

public class CardAdapter extends RecyclerView.Adapter<CardHolder>{

    Context c;
    ArrayList<CardModel> cards; // this array list create a list of array which parameters define in the cardmodel class

    MainActivity mainActivity;

    //constructor
    public CardAdapter(Context c, ArrayList<CardModel> cards) {
        this.c = c;
        this.cards = cards;
        this.mainActivity = (MainActivity)c;
    }

    @NonNull
    @Override
    public CardHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {

        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.row, null); //inflate the row

        return new CardHolder(view); // return the view to cardholder
    }

    @Override
    public void onBindViewHolder(@NonNull CardHolder holder, int position) {

        if(cards.get(position).getAcc().length() > 13) {
            holder.acc.setText(cards.get(position).getAcc().substring(0, 13) + "...");
        }else
            holder.acc.setText(cards.get(position).getAcc());

        holder.pass.setText(cards.get(position).getPass());
        holder.title.setText(cards.get(position).getTitle());
        holder.img.setImageResource(cards.get(position).getImg());

        // copy the password
        holder.txt_Copy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ClipboardManager clipboard = (ClipboardManager) view.getContext().getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("txtCopy", cards.get(position).getPass());
                clipboard.setPrimaryClip(clip);

                DataBaseHelper db = new DataBaseHelper(view.getContext());
                if(cards.get(position).getOrder() == 1)
                    return; // do nothing
                else {
                    db.fixOrder(cards.get(position).getOrder());
                    if(mainActivity.toolbar.getTitle().equals("Recent")){
                        mainActivity.getCards();
                        mainActivity.cardAdapter.notifyDataSetChanged();
                    }
                }
            }
        });

        // view the password
        holder.txt_View.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DataBaseHelper db = new DataBaseHelper(view.getContext());
                //decryption
                try {
                    AES aes = new AES();
                    String salt = cards.get(position).getSalt();
                    String masterKey = mainActivity.edt_txt_master_key.getText().toString();
                    SecretKey key = aes.getKeyFromPassword(masterKey, salt);  //salt and masterkey will generate the secret key
                    String iv = cards.get(position).getIv();
                    String decrypted = aes.decrypt(db.getPassword(cards.get(position).getId()), key, iv);
                    holder.pass.setText(decrypted);
                } catch (Exception e) {
                    //Toast.makeText(view.getContext(), "DECRYPT FAILURE", Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                }
            }
        });

        holder.acc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                InputFilter[] fArray = new InputFilter[1];
                fArray[0] = new InputFilter.LengthFilter(30);
                holder.acc.setFilters(fArray);
                holder.acc.setText(cards.get(position).getAcc());
            }
        });

        // hold to update cardView
        holder.cardView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                if(cards.get(position).getPass() == "") {
                    // do nothn
                }
                else {
                    Intent intent = new Intent(view.getContext(), UpdateAcc.class);

                    Bundle bundle = new Bundle();
                    bundle.putString("masterKey", mainActivity.edt_txt_master_key.getText().toString());
                    bundle.putSerializable("card", cards.get(position));
                    intent.putExtras(bundle);

                    c.startActivity(intent);
                }
                return true;
            }
        });
    }

    public void deleteItem(int position){
        cards.remove(position);
    }

    @Override
    public int getItemCount() {
        return cards.size();
    }


}
