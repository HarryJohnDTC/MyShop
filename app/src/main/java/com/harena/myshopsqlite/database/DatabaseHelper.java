package com.harena.myshopsqlite.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "myshop.db";
    private static final int DATABASE_VERSION = 3; // Increment the version

    // Table names
    public static final String TABLE_ARTICLE = "article";
    public static final String TABLE_CART = "cart";

    // Article Table Columns
    public static final String COLUMN_ARTICLE_ID = "_id";
    public static final String COLUMN_ARTICLE_NAME = "name";
    public static final String COLUMN_ARTICLE_PRICE = "price";
    public static final String COLUMN_ARTICLE_STOCK = "stock";
    public static final String COLUMN_ARTICLE_PHOTO = "photo";

    // Cart Table Columns
    public static final String COLUMN_CART_ID = "_id";
    public static final String COLUMN_CART_ARTICLE_ID = "article_id";
    public static final String COLUMN_CART_QUANTITY = "quantity";
    public static final String COLUMN_CART_TOTAL = "total";

    // Create table SQL statements
    private static final String TABLE_CREATE_ARTICLE =
            "CREATE TABLE " + TABLE_ARTICLE + " (" +
                    COLUMN_ARTICLE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_ARTICLE_NAME + " TEXT, " +
                    COLUMN_ARTICLE_PRICE + " REAL, " +
                    COLUMN_ARTICLE_STOCK + " INTEGER, " +
                    COLUMN_ARTICLE_PHOTO + " TEXT" +
                    ");";

    private static final String TABLE_CREATE_CART =
            "CREATE TABLE " + TABLE_CART + " (" +
                    COLUMN_CART_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_CART_ARTICLE_ID + " INTEGER, " +
                    COLUMN_CART_QUANTITY + " INTEGER, " +
                    COLUMN_CART_TOTAL + " REAL, " +
                    "FOREIGN KEY(" + COLUMN_CART_ARTICLE_ID + ") REFERENCES " + TABLE_ARTICLE + "(" + COLUMN_ARTICLE_ID + ")" +
                    ");";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(TABLE_CREATE_ARTICLE);
        db.execSQL(TABLE_CREATE_CART);
        db.execSQL(TABLE_CREATE_USER);
    }



    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.w(DatabaseHelper.class.getName(),
                "Upgrading database from version " + oldVersion + " to "
                        + newVersion + ", which will destroy all old data");
        // Drop older tables if existed
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_ARTICLE);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_CART);
        // Create new tables
        onCreate(db);
    }

    // Table names
    public static final String TABLE_USER = "user";

    // User Table Columns
    public static final String COLUMN_USER_ID = "_id";
    public static final String COLUMN_USER_EMAIL = "email";
    public static final String COLUMN_USER_PASSWORD = "password";

    // Create table SQL statements
    private static final String TABLE_CREATE_USER =
            "CREATE TABLE " + TABLE_USER + " (" +
                    COLUMN_USER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_USER_EMAIL + " TEXT, " +
                    COLUMN_USER_PASSWORD + " TEXT" +
                    ");";
}
