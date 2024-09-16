package com.harena.myshopsqlite;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ListView;

import com.harena.myshopsqlite.database.DatabaseHelper;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private DatabaseHelper dbHelper;
    private static final String TAG = "MainActivity";
    private ListView lvArticles;
    private Button btnViewCart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialiser les vues
        lvArticles = findViewById(R.id.lvArticles);
        btnViewCart = findViewById(R.id.btnViewCart);

        try {
            // Initialiser le DatabaseHelper
            dbHelper = new DatabaseHelper(this);
            Log.d(TAG, "DatabaseHelper initialized");

            // Ajouter des articles pour test
            addTestArticles();

            // Afficher les articles dans la ListView
            displayArticles();

            // Configurer le bouton pour ouvrir l'activité Cart
            btnViewCart.setOnClickListener(v -> {
                Intent intent = new Intent(MainActivity.this, CartActivity.class);
                startActivity(intent);
            });

        } catch (Exception e) {
            Log.e(TAG, "Error initializing database or displaying articles", e);
        }
    }

    private void addTestArticles() {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.execSQL("DELETE FROM " + DatabaseHelper.TABLE_ARTICLE); // Clean up previous test data
        db.execSQL("INSERT INTO " + DatabaseHelper.TABLE_ARTICLE + " (name, price, stock, photo) VALUES ('Appareil photo', 700.0, 50, 'photo_camera')");
        db.execSQL("INSERT INTO " + DatabaseHelper.TABLE_ARTICLE + " (name, price, stock, photo) VALUES ('Smartphone', 400.0, 100, 'smartphone')");
        db.execSQL("INSERT INTO " + DatabaseHelper.TABLE_ARTICLE + " (name, price, stock, photo) VALUES ('Ordinateur portable', 500.0, 70, 'laptop')");
        db.close();
        Log.d(TAG, "Test articles added");
    }

    private void displayArticles() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(DatabaseHelper.TABLE_ARTICLE, null, null, null, null, null, null);

        ArrayList<String[]> articles = new ArrayList<>();
        while (cursor.moveToNext()) {
            String name = cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_ARTICLE_NAME));
            double price = cursor.getDouble(cursor.getColumnIndex(DatabaseHelper.COLUMN_ARTICLE_PRICE));
            int stock = cursor.getInt(cursor.getColumnIndex(DatabaseHelper.COLUMN_ARTICLE_STOCK));
            String photo = cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_ARTICLE_PHOTO));
            articles.add(new String[]{name, String.valueOf(price), String.valueOf(stock), photo});
        }
        cursor.close();

        // Utiliser l'ArticleAdapter pour afficher les articles
        ArticleAdapter adapter = new ArticleAdapter(this, articles, dbHelper);
        lvArticles.setAdapter(adapter);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (dbHelper != null) {
            dbHelper.close();
        }
        Log.d(TAG, "DatabaseHelper closed");
    }
}
