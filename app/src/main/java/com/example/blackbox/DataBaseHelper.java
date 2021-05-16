package com.example.blackbox;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.widget.Toast;

import androidx.annotation.Nullable;

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

    public DataBaseHelper(@Nullable Context context) {
        super(context, DATABASE_NAME, null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String query= "CREATE TABLE " + MAIL_TABLE + " (" +
                COLUMN_MAIL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_MAIL_ACCOUNT + " TEXT, " +
                COLUMN_MAIL_PASSWORD + " TEXT, " +
                COLUMN_IMG_INDEX + " INTEGER, " +
                COLUMN_TITLE + " TEXT)";

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

        long result = db.insert(MAIL_TABLE, null, cv);
        if(result == -1)
            Toast.makeText(context, "DB ERROR", Toast.LENGTH_SHORT).show();
    }

    public String getPassword(int id){
        // get one data
        //("select * from " + BanksTable.NAME + " where " + BanksTable.COL_NAME + "='" + bankName + "'" , null);
        String queryString = "SELECT * FROM " + MAIL_TABLE + " WHERE " + COLUMN_MAIL_ID + "='" + id +"'";
        //String queryString = "SELECT * FROM " + MAIL_TABLE;
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

    public ArrayList<CardModel> getAllCards(){
        // get all data from database

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

                CardModel cardModel = new CardModel();
                cardModel.setId(cardID);
                cardModel.setAcc(cardAccount);
                cardModel.setPass(cardPassword);
                cardModel.setImg(cardImage);
                cardModel.setTitle(cardTitle);
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
