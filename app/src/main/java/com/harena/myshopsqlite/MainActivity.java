package com.harena.myshopsqlite;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.harena.myshopsqlite.database.DatabaseHelper;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private DatabaseHelper dbHelper;
    private static final String TAG = "MainActivity";
    private ListView lvArticles;
    private Button btnViewCart, btnLogout; // Bouton de déconnexion

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialiser les vues
        lvArticles = findViewById(R.id.lvArticles);
        btnViewCart = findViewById(R.id.btnViewCart);
        btnLogout = findViewById(R.id.btnLogout); // Bouton "Déconnexion"

        try {
            // Initialiser le DatabaseHelper
            dbHelper = new DatabaseHelper(this);
            Log.d(TAG, "DatabaseHelper initialized");

            // Initialiser les articles
            initializeArticles();

            // Configurer le bouton pour ouvrir l'activité Cart
            btnViewCart.setOnClickListener(v -> {
                Intent intent = new Intent(MainActivity.this, CartActivity.class);
                startActivity(intent);
            });

            // Configurer le bouton "Déconnexion"
            btnLogout.setOnClickListener(v -> logoutUser());

        } catch (Exception e) {
            Log.e(TAG, "Error initializing database or displaying articles", e);
        }
    }


    @Override
    protected void onResume() {
        super.onResume();
        // Recharger les articles quand l'activité reprend
        displayArticles();
    }

    private void displayArticles() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(DatabaseHelper.TABLE_ARTICLE, null, null, null, null, null, null);

        ArrayList<String[]> articles = new ArrayList<>();
        while (cursor.moveToNext()) {
            int id = cursor.getInt(cursor.getColumnIndex(DatabaseHelper.COLUMN_ARTICLE_ID));
            String name = cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_ARTICLE_NAME));
            double price = cursor.getDouble(cursor.getColumnIndex(DatabaseHelper.COLUMN_ARTICLE_PRICE));
            int stock = cursor.getInt(cursor.getColumnIndex(DatabaseHelper.COLUMN_ARTICLE_STOCK));
            String photo = cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_ARTICLE_PHOTO));
            articles.add(new String[]{name, String.valueOf(price), String.valueOf(stock), photo});
        }
        cursor.close();

        // Utiliser l'ArticleAdapter pour afficher les articles dans la ListView
        ArticleAdapter adapter = new ArticleAdapter(this, articles, dbHelper);
        lvArticles.setAdapter(adapter);
    }

    // Méthode pour gérer la déconnexion
    private void logoutUser() {
        SharedPreferences prefs = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.remove("user_id"); // Efface l'ID utilisateur pour forcer une reconnexion
        editor.apply();

        // Redirige vers l'activité de connexion
        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        startActivity(intent);
        finish(); // Ferme l'activité actuelle
        Toast.makeText(this, "Déconnecté avec succès", Toast.LENGTH_SHORT).show();
    }
    private void initializeArticles() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(DatabaseHelper.TABLE_ARTICLE, null, null, null, null, null, null);

        if (cursor.getCount() == 0) { // Vérifiez si la table est vide
            dbHelper.addArticle("Appareil photo", 700.0, 50, "photo_camera");
            dbHelper.addArticle("Smartphone", 400.0, 100, "smartphone");
            dbHelper.addArticle("Ordinateur portable", 500.0, 70, "laptop");
            Log.d(TAG, "Test articles added");
        }
        cursor.close();
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
