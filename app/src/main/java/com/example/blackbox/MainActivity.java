package com.example.blackbox;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SortedList;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewSwitcher;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;

import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.crypto.SecretKey;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    // toolbar
    Toolbar toolbar;
    // recycler view, card adapter, swipe helper
    RecyclerView recyclerView;
    CardAdapter cardAdapter;
    ItemTouchHelper itemTouchHelper;
    // floating button
    FloatingActionButton btn_add;
    FloatingActionButton btn_toMainActivity;
    FloatingActionButton btn_master_key;
    // database
    DataBaseHelper db;
    // navigation
    DrawerLayout drawer;
    NavigationView nav;
    // view switcher
    ViewSwitcher viewSwitcher;
    // master key check
    EditText edt_txt_master_key;

    ArrayList<CardModel> cards;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // view switcher, buttons and masterKey check
        viewSwitcher = (ViewSwitcher)findViewById(R.id.viewSwitcher);

        edt_txt_master_key = findViewById(R.id.edt_txt_master_key);

        btn_toMainActivity = findViewById(R.id.btn_toMainActivity);
        btn_toMainActivity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ArrayList<CardModel> newCards = db.getAllCards();

                // if db is not empty, check whether masterkey can decrypt or not
                if(newCards.isEmpty() == false)
                {
                    try {
                        AES aes = new AES();
                        String salt = newCards.get(0).getSalt();
                        String masterKey = edt_txt_master_key.getText().toString();
                        SecretKey key = aes.getKeyFromPassword(masterKey, salt);  //salt and masterkey will generate the secret key
                        String iv = newCards.get(0).getIv();
                        String decrypted = aes.decrypt(db.getPassword(newCards.get(0).getId()), key, iv);

                        // won't throw error and continue when masterkey is right
                        btn_master_key.setEnabled(false);
                        btn_master_key.hide();
                        btn_add.setEnabled(true);
                        btn_add.show();

                        // swipe possible if masterkey is true
                        itemTouchHelper = new ItemTouchHelper(simpleCallback);
                        itemTouchHelper.attachToRecyclerView(recyclerView);
                    } catch (Exception e) {
                        Toast.makeText(view.getContext(), "Wrong Master Password!", Toast.LENGTH_SHORT).show();
                        btn_master_key.setEnabled(true);
                        btn_master_key.show();
                        btn_add.setEnabled(false);
                        btn_add.hide();
                    }
                }
                // if db is empty, make sure masterkey is not empty
                else if(newCards.isEmpty() && edt_txt_master_key.getText().toString() == ""){
                    Toast.makeText(view.getContext(), "Need Master Password!", Toast.LENGTH_SHORT).show();
                    btn_master_key.setEnabled(true);
                    btn_master_key.show();
                    btn_add.setEnabled(false);
                    btn_add.hide();
                }
                // if db is empty, master key is not empty
                else{
                    itemTouchHelper = null;
                    btn_master_key.setEnabled(false);
                    btn_master_key.hide();
                    btn_add.setEnabled(true);
                    btn_add.show();
                }
                getCards();
                viewSwitcher.setInAnimation(view.getContext(), R.anim.slide_in_right);
                viewSwitcher.setOutAnimation(view.getContext(), R.anim.slide_out_left);
                viewSwitcher.showNext();
            }
        });

        btn_master_key = findViewById(R.id.btn_master_key);
        btn_master_key.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                viewSwitcher.setInAnimation(view.getContext(), R.anim.slide_in_left);
                viewSwitcher.setOutAnimation(view.getContext(), R.anim.slide_out_right);
                viewSwitcher.showPrevious();
            }
        });

        // toolbar
        toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("Recent");
        toolbar.setTitleTextColor(Color.WHITE);
        //toolbar.setNavigationIcon(R.drawable.ic_baseline_menu_24);
        toolbar.getNavigationIcon().setColorFilter(Color.WHITE, PorterDuff.Mode.MULTIPLY);
        setSupportActionBar(toolbar);

        // database
        db = new DataBaseHelper(this);

        // navigation drawer
        nav = findViewById(R.id.nav_view);
        nav.setNavigationItemSelectedListener(this);
        nav.setItemIconTintList(null);
        drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this,drawer,toolbar,R.string.navigation_drawer_open,R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        // card populate
        recyclerView = findViewById(R.id.recyclerView);

        // new acc
        btn_add = findViewById(R.id.btn_add_acc);
        btn_add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(MainActivity.this, AddAcc.class);
                intent.putExtra("masterKey", edt_txt_master_key.getText().toString());
                startActivity(intent);
            }
        });
    }

    public void getCards() {

        cards = new ArrayList<>();

        ArrayList<CardModel> newCards = db.getAllCards();
        for(CardModel item : newCards){
            CardModel cardModel = new CardModel();

            //decryption
            String decrypted = "";
            try {
                AES aes = new AES();
                String salt = item.getSalt();
                String masterKey = edt_txt_master_key.getText().toString();
                SecretKey key = aes.getKeyFromPassword(masterKey, salt);  //salt and masterkey will generate the secret key
                String iv = item.getIv();
                decrypted = aes.decrypt(db.getPassword(item.getId()), key, iv);
            } catch (Exception e) {
                //Toast.makeText(getApplicationContext(), "getCards() DECRYPT FAILURE", Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }

            cardModel.setId(item.getId());
            cardModel.setAcc(item.getAcc());
            String str = decrypted.replaceAll(".", "*");
            cardModel.setPass(str);
            cardModel.setTitle(item.getTitle());
            cardModel.setOrder(item.getOrder());
            cardModel.setSalt(item.getSalt());
            cardModel.setIv(item.getIv());
            //Toast.makeText(getApplicationContext(),"cardOrder= " + cardModel.getOrder(),Toast.LENGTH_SHORT).show();
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
                case 5:
                    cardModel.setImg(R.drawable.debit_card);
                    break;
                case 6:
                    cardModel.setImg(R.drawable.website);
                    break;
                case 7:
                    cardModel.setImg(R.drawable.wifi);
                    break;
            }
            cards.add(cardModel);
        }

        Collections.sort(cards, CardModel.byOrder);

        cardAdapter = new CardAdapter(this, cards);
        recyclerView.setAdapter(cardAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
    }

    private void getCardsByCategory (int catIndex) {

        cards = new ArrayList<>();

        ArrayList<CardModel> newCards = db.getByCategory(catIndex);
        for(CardModel item : newCards){
            CardModel cardModel = new CardModel();

            //decryption
            String decrypted = "";
            try {
                AES aes = new AES();
                String salt = item.getSalt();
                String masterKey = edt_txt_master_key.getText().toString();
                SecretKey key = aes.getKeyFromPassword(masterKey, salt);  //salt and masterkey will generate the secret key
                String iv = item.getIv();
                decrypted = aes.decrypt(db.getPassword(item.getId()), key, iv);
            } catch (Exception e) {
                //Toast.makeText(getApplicationContext(), "getCardsByCategory() DECRYPT FAILURE", Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }

            cardModel.setId(item.getId());
            cardModel.setAcc(item.getAcc());
            String str = decrypted.replaceAll(".", "*");
            cardModel.setPass(str);
            cardModel.setTitle(item.getTitle());
            cardModel.setOrder(item.getOrder());
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
                case 5:
                    cardModel.setImg(R.drawable.debit_card);
                    break;
                case 6:
                    cardModel.setImg(R.drawable.website);
                    break;
                case 7:
                    cardModel.setImg(R.drawable.wifi);
                    break;
            }
            cards.add(cardModel);
        }

        Collections.sort(cards, CardModel.byOrder);

        cardAdapter = new CardAdapter(this, cards);
        recyclerView.setAdapter(cardAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
    }

    ItemTouchHelper.SimpleCallback simpleCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
        @Override
        public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
            return false;
        }

        @Override
        public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
            int position = viewHolder.getAdapterPosition();
            Toast.makeText(getApplicationContext(), "pos = "+position, Toast.LENGTH_SHORT).show();
            db.deleteAcc(cardAdapter.cards.get(position));
            cardAdapter.deleteItem(position);
            cardAdapter.notifyItemRemoved(position);

            for(CardModel item : cards){
                if(position < item.getOrder()){
                    item.setOrder(item.getOrder()-1);
                }else{
                    // do nothn
                }
            }
        }
    };

    @Override
    public void onBackPressed() {
        if(drawer.isDrawerOpen(GravityCompat.START))
            drawer.closeDrawer(GravityCompat.START);
        else
            super.onBackPressed();
    }

    @Override
    public void onResume() {
        super.onResume();
        getCards();
        toolbar.setTitle("Recent");
        cardAdapter.notifyDataSetChanged();

        // first time adding accounts
        if(itemTouchHelper == null) {
            itemTouchHelper = new ItemTouchHelper(simpleCallback);
            itemTouchHelper.attachToRecyclerView(recyclerView);
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.nav_recent:
                getCards();
                toolbar.setTitle("Recent");
                cardAdapter.notifyDataSetChanged();
                break;
            case R.id.nav_mail:
                getCardsByCategory(0);
                toolbar.setTitle("E-Mail");
                cardAdapter.notifyDataSetChanged();
                break;
            case R.id.nav_social:
                getCardsByCategory(1);
                toolbar.setTitle("Social Media");
                cardAdapter.notifyDataSetChanged();
                break;
            case R.id.nav_computer:
                getCardsByCategory(2);
                toolbar.setTitle("Computer");
                cardAdapter.notifyDataSetChanged();
                break;
            case R.id.nav_phone:
                getCardsByCategory(3);
                toolbar.setTitle("Phone");
                cardAdapter.notifyDataSetChanged();
                break;
            case R.id.nav_credit_card:
                getCardsByCategory(5);
                toolbar.setTitle("Debit Card");
                cardAdapter.notifyDataSetChanged();
                break;
            case R.id.nav_website:
                getCardsByCategory(6);
                toolbar.setTitle("Website");
                cardAdapter.notifyDataSetChanged();
                break;
            case R.id.nav_wifi:
                getCardsByCategory(7);
                toolbar.setTitle("Wifi");
                cardAdapter.notifyDataSetChanged();
                break;
        }
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}