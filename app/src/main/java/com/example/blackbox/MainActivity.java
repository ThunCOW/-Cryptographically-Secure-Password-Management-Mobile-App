package com.example.blackbox;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    // cards
    RecyclerView recyclerView;
    CardAdapter cardAdapter;
    // floating button
    FloatingActionButton btn_add;
    // database
    DataBaseHelper db;
    // navigation
    DrawerLayout drawer;
    NavigationView nav;
    // copy view
    TextView txt_Copy, txt_show, txt_Hid_Acc;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("Recent");
        toolbar.setTitleTextColor(Color.WHITE);
        //toolbar.setNavigationIcon(R.drawable.ic_baseline_menu_24);
        toolbar.getNavigationIcon().setColorFilter(Color.WHITE, PorterDuff.Mode.MULTIPLY);
        setSupportActionBar(toolbar);

        // database
        db = new DataBaseHelper(this);

        // navigation drawer
        nav = findViewById(R.id.nav_view);
        nav.setItemIconTintList(null);
        drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this,drawer,toolbar,R.string.navigation_drawer_open,R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        // card populate
        recyclerView = findViewById(R.id.recyclerView);

        cardAdapter = new CardAdapter(this, getCards());
        recyclerView.setAdapter(cardAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // new acc
        btn_add = findViewById(R.id.btn_add_acc);
        btn_add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(MainActivity.this, AddAcc.class);
                startActivity(intent);
            }
        });
    }


    private ArrayList<CardModel> getCards() {

        ArrayList<CardModel> cards = new ArrayList<>();

        ArrayList<CardModel> newCards = db.getAllCards();
        for(CardModel item : newCards){
            CardModel cardModel = new CardModel();
            //char l = item.getPass().charAt(item.getAcc().length());

            //db
            DataBaseHelper db = new DataBaseHelper(getApplicationContext());
            //Toast.makeText(getApplicationContext(), "encPASS = "+ db.getPassword(item.getAcc()), Toast.LENGTH_SHORT).show();
            //decryption
            String decrypted = "";
            try {
                AES aes = new AES();
                decrypted = aes.decrypt(db.getPassword(item.getId()));
            } catch (Exception e) {
                Toast.makeText(getApplicationContext(), "getCards() DECRYPT FAILURE", Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }

            cardModel.setId(item.getId());
            cardModel.setAcc(item.getAcc());
            String str = decrypted.replaceAll(".", "*");
            cardModel.setPass(str);
            cardModel.setTitle(item.getTitle());
            switch (item.getImg()) {
                case 0:
                    cardModel.setImg(R.drawable.e_mail);
                    break;
                case 1:
                    cardModel.setImg(R.drawable.social_media);
                    break;
                case 2:
                    cardModel.setImg(R.drawable.computer);
                    break;
                case 3:
                    cardModel.setImg(R.drawable.phone);
                    break;
                case 4:
                    cardModel.setImg(R.drawable.title);
                    break;
            }
            cards.add(cardModel);
        }
        return cards;
    }

    @Override
    public void onBackPressed() {
        if(drawer.isDrawerOpen(GravityCompat.START))
            drawer.closeDrawer(GravityCompat.START);
        else
            super.onBackPressed();
    }
}