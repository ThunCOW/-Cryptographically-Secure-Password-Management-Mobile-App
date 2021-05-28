package com.example.blackbox;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.widget.Toast;

import androidx.annotation.Nullable;

import java.sql.Blob;
import java.util.ArrayList;
import java.util.List;

public class DataBaseHelper extends SQLiteOpenHelper {

    private Context context;
    private static final String DATABASE_NAME = "crypto.db";

    private static final String MAIL_TABLE = "CATEGORY_TABLE";
    private static final String COLUMN_MAIL_ID = "ID";
    private static final String COLUMN_MAIL_ACCOUNT = "CATEGORY_ACCOUNT";
    private static final String COLUMN_MAIL_PASSWORD = "CATEGORY_PASSWORD";
    private static final String COLUMN_IMG_INDEX = "CATEGORY_INDEX";
    private static final String COLUMN_TITLE = "CATEGORY_TITLE";
    private static final String COLUMN_ORDER = "CATEGORY_ORDER";
    private static final String COLUMN_SALT = "SALT";
    private static final String COLUMN_IV = "IV";

    public DataBaseHelper(@Nullable Context context) {
        super(context, DATABASE_NAME, null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        /*String query= "CREATE TABLE " + MAIL_TABLE + " (" +
                COLUMN_MAIL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_MAIL_ACCOUNT + " TEXT, " +
                COLUMN_MAIL_PASSWORD + " TEXT, " +
                COLUMN_IMG_INDEX + " INTEGER, " +
                COLUMN_TITLE + " TEXT)";*/

        String query= "CREATE TABLE " + MAIL_TABLE + " (" +
                COLUMN_MAIL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_MAIL_ACCOUNT + " TEXT, " +
                COLUMN_MAIL_PASSWORD + " TEXT, " +
                COLUMN_IMG_INDEX + " INTEGER, " +
                COLUMN_TITLE + " TEXT, " +
                COLUMN_ORDER + " INTEGER, " +
                COLUMN_SALT + " TEXT, " +
                COLUMN_IV + " TEXT)";

        db.execSQL(query);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int i, int i1) {
        db.execSQL("DROP TABLE IF EXISTS " + MAIL_TABLE);
        onCreate(db);
    }

    void addAcc(CardModel cardModel) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();

        cv.put(COLUMN_MAIL_ACCOUNT, cardModel.getAcc());
        cv.put(COLUMN_MAIL_PASSWORD, cardModel.getPass());
        cv.put(COLUMN_IMG_INDEX, cardModel.getImg());
        cv.put(COLUMN_TITLE, cardModel.getTitle());
        cv.put(COLUMN_ORDER, 1);
        cv.put(COLUMN_SALT, cardModel.getSalt());
        cv.put(COLUMN_IV, cardModel.getIv());

        // if 0 passed it will increase order of every data in db
        fixOrder(0);
        long result = db.insert(MAIL_TABLE, null, cv);
        if(result == -1)
            Toast.makeText(context, "DB ERROR", Toast.LENGTH_SHORT).show();
    }

    // sort by DESC, decrease order by 1 until cursor comes to the cardModel that needs to be deleted, then stop
    void deleteAcc(CardModel cardModel){

        String queryString = "SELECT * FROM " + MAIL_TABLE + " ORDER BY " + COLUMN_ORDER + " DESC";
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(queryString, null);
        if(cursor != null) {
            if (cursor.moveToFirst()) {
                do {
                    if(cursor.getInt(5) == cardModel.getOrder()){
                        long result = db.delete(MAIL_TABLE, COLUMN_MAIL_ID+"="+cardModel.getId(), null);
                        if(result == -1)
                            Toast.makeText(context, "(DB)FAILED TO DELETE", Toast.LENGTH_SHORT).show();
                        cursor.close();
                        return;
                    }else{
                        //ToDO: might try to update id too, don't know if it's possible tho
                        String queryString2 = "UPDATE " + MAIL_TABLE + " SET " + COLUMN_ORDER + "='" + (cursor.getInt(5)-1) + "' WHERE " + COLUMN_MAIL_ID + "='" + cursor.getInt(0) + "'";
                        db.execSQL(queryString2);
                    }
                } while (cursor.moveToNext());
            } else {
                // db empty dont do anything
            }
        }
    }

    void updateAcc(CardModel cardModel) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();

        //String queryString = "SELECT * FROM " + MAIL_TABLE + " WHERE " + COLUMN_MAIL_ID + "='" + cardModel.getId() +"'";

        cv.put(COLUMN_MAIL_ACCOUNT, cardModel.getAcc());
        cv.put(COLUMN_MAIL_PASSWORD, cardModel.getPass());  //needs to be encrypted
        cv.put(COLUMN_IMG_INDEX, cardModel.getImg());
        cv.put(COLUMN_TITLE, cardModel.getTitle());
        //updateOrder();
        cv.put(COLUMN_ORDER, 1);

        //Cursor cursor = db.rawQuery()
        long result = db.update(MAIL_TABLE, cv, COLUMN_MAIL_ID+"="+cardModel.getId(), null);
        if(result == -1)
            Toast.makeText(context, "DB ERROR", Toast.LENGTH_SHORT).show();
    }

    void updateOrder() {
        String queryString = "SELECT * FROM " + MAIL_TABLE;
        SQLiteDatabase db_write = this.getWritableDatabase();
        SQLiteDatabase db_read = this.getReadableDatabase();
        Cursor cursor = db_read.rawQuery(queryString, null);
        if(cursor != null) {
            if (cursor.moveToFirst()) {
                do {
                    String queryString2 = "UPDATE " + MAIL_TABLE + " SET " + COLUMN_ORDER + "='" + (cursor.getInt(5)+1) + "' WHERE " + COLUMN_MAIL_ID + "='" + cursor.getInt(0) + "'";
                    db_write.execSQL(queryString2);
                } while (cursor.moveToNext());
            } else {
                // db empty dont do anything
            }
        }

        cursor.close();
        // gives error when i close db
        //db_write.close();
        //db_read.close();

        //ContentValues cv = new ContentValues();

        //String queryString = "SELECT * FROM " + MAIL_TABLE + " WHERE " + COLUMN_MAIL_ID + "='" + cardModel.getId() +"'";

        /*cv.put(COLUMN_MAIL_ACCOUNT, cardModel.getAcc());
        cv.put(COLUMN_MAIL_PASSWORD, cardModel.getPass());  //needs to be encrypted
        cv.put(COLUMN_IMG_INDEX, cardModel.getImg());
        cv.put(COLUMN_TITLE, cardModel.getTitle());*/
        //fixOrder();
        //cv.put(COLUMN_ORDER, 1);

        //Cursor cursor = db.rawQuery()
        /*long result = db.update(MAIL_TABLE, cv, COLUMN_MAIL_ID+"="+cardModel.getId(), null);
        if(result == -1)
            Toast.makeText(context, "DB ERROR", Toast.LENGTH_SHORT).show();*/
    }

    // increase order in DB, db sorted by Order
    void fixOrder(int order) {

        // sorted by ASC
        String queryString = "SELECT * FROM " + MAIL_TABLE + " ORDER BY " + COLUMN_ORDER + " ASC";
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(queryString, null);
        if(cursor != null) {
            if (cursor.moveToFirst()) {
                do {
                    String queryString2 = "UPDATE " + MAIL_TABLE + " SET " + COLUMN_ORDER + "='" + (cursor.getInt(5)+1) + "' WHERE " + COLUMN_MAIL_ID + "='" + cursor.getInt(0) + "'";
                    db.execSQL(queryString2);
                    if(cursor.getInt(5) == order){
                        db.execSQL("UPDATE " + MAIL_TABLE + " SET " + COLUMN_ORDER + "='" + 1 + "' WHERE " + COLUMN_MAIL_ID + "='" + cursor.getInt(0) + "'");
                        cursor.close();
                        return;
                    }
                } while (cursor.moveToNext());
            } else {
                // db empty dont do anything
            }
        }

        cursor.close();
    }

    // get one column, return encrypted password
    public String getPassword(int id){
        String queryString = "SELECT * FROM " + MAIL_TABLE + " WHERE " + COLUMN_MAIL_ID + "='" + id +"'";
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.rawQuery(queryString, null);
        String encrypted = "CURSOR FAILED";
        if(cursor != null){
            if(cursor.moveToFirst()){
                do {
                    encrypted = cursor.getString(2);
                }while (cursor.moveToNext());
            }
        }else{
            // db empty dont do anything
        }
        cursor.close();
        db.close();
        return encrypted;
    }

    // get all data from database
    public ArrayList<CardModel> getAllCards(){

        String queryString = "SELECT * FROM " + MAIL_TABLE;
        SQLiteDatabase db = this.getReadableDatabase();

        ArrayList<CardModel> cards = new ArrayList<>();

        Cursor cursor = db.rawQuery(queryString, null);
        if(cursor.moveToFirst())
        {
            do {
                int cardID = cursor.getInt(0);
                String cardAccount = cursor.getString(1);
                String cardPassword = cursor.getString(2);
                Integer cardImage = Integer.parseInt(cursor.getString(3));
                String cardTitle = cursor.getString(4);
                Integer cardOrder = cursor.getInt(5);
                String cardSalt = cursor.getString(6);
                String cardIv = cursor.getString(7);

                CardModel cardModel = new CardModel();
                cardModel.setId(cardID);
                cardModel.setAcc(cardAccount);
                cardModel.setPass(cardPassword);
                cardModel.setImg(cardImage);
                cardModel.setTitle(cardTitle);
                cardModel.setOrder(cardOrder);
                cardModel.setSalt(cardSalt);
                cardModel.setIv(cardIv);
                cards.add(cardModel);

            }while (cursor.moveToNext());
        }
        else{
            // db empty dont do anything
        }

        cursor.close();
        db.close();
        return cards;
    }

    // get all data from database by Category
    public ArrayList<CardModel> getByCategory(int catIndex){

        String queryString = "SELECT * FROM " + MAIL_TABLE + " WHERE " + COLUMN_IMG_INDEX + "='" + catIndex + "'";
        SQLiteDatabase db = this.getReadableDatabase();

        ArrayList<CardModel> cards = new ArrayList<>();

        Cursor cursor = db.rawQuery(queryString, null);
        if(cursor.moveToFirst())
        {
            do {
                int cardID = cursor.getInt(0);
                String cardAccount = cursor.getString(1);
                String cardPassword = cursor.getString(2);
                Integer cardImage = Integer.parseInt(cursor.getString(3));
                String cardTitle = cursor.getString(4);
                Integer cardOrder = cursor.getInt(5);
                String cardSalt = cursor.getString(6);
                String cardIv = cursor.getString(7);

                CardModel cardModel = new CardModel();
                cardModel.setId(cardID);
                cardModel.setAcc(cardAccount);
                cardModel.setPass(cardPassword);
                cardModel.setImg(cardImage);
                cardModel.setTitle(cardTitle);
                cardModel.setOrder(cardOrder);
                cardModel.setSalt(cardSalt);
                cardModel.setIv(cardIv);
                cards.add(cardModel);

            }while (cursor.moveToNext());
        }
        else{
            // db empty dont do anything
        }

        cursor.close();
        db.close();
        return cards;
    }

    /*public static final String CUSTOMER_TABLE = "CUSTOMER_TABLE";
    public static final String COLUMN_ID = "ID";
    public static final String COLUMN_CUSTOMER_NAME = "CUSTOMER_NAME";
    public static final String COLUMN_CUSTOMER_AGE = "CUSTOMER_AGE";
    public static final String COLUMN_ACTIVE_CUSTOMER = "ACTIVE_CUSTOMER";

    public DataBaseHelper(@Nullable Context context) {

        super(context, "customer.db", null, 1);
    }

    //first time accessed
    @Override
    public void onCreate(SQLiteDatabase db) {
        String createTableStatement= "CREATE TABLE " + CUSTOMER_TABLE + " (" +
                COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_CUSTOMER_NAME + " TEXT, " +
                COLUMN_CUSTOMER_AGE + " INT, " +
                COLUMN_ACTIVE_CUSTOMER + " BOOL )";

        db.execSQL(createTableStatement);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    public  boolean addOne(CustomerModel customerModel){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();

        cv.put(COLUMN_CUSTOMER_NAME, customerModel.getName());
        cv.put(COLUMN_CUSTOMER_AGE, customerModel.getAge());
        cv.put(COLUMN_ACTIVE_CUSTOMER, customerModel.getIsActive());

        db.insert(CUSTOMER_TABLE,null, cv);
        return true;
    }

    public List<CustomerModel> getAllCustomers(){
        List<CustomerModel> customers = new ArrayList<>();

        // get data from database

        String queryString = "SELECT * FROM " + CUSTOMER_TABLE;

        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.rawQuery(queryString, null);
        if(cursor.moveToFirst())
        {
            do {
                int customerID = cursor.getInt(0);
                String customerName = cursor.getString(1);
                int customerAge = cursor.getInt(2);
                boolean customerActive = cursor.getInt(3) == 1 ? true: false;

                CustomerModel newCustomer = new CustomerModel(customerID, customerName, customerAge, customerActive);
                customers.add(newCustomer);
            }while (cursor.moveToNext());
        }
        else{
            // do not add anything
        }

        cursor.close();
        db.close();
        return customers;
    }*/
}
