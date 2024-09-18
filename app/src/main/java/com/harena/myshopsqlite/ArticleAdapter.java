package com.harena.myshopsqlite;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.harena.myshopsqlite.database.DatabaseHelper;

import java.util.ArrayList;

public class ArticleAdapter extends ArrayAdapter<String[]> {

    private final Context context;
    private final ArrayList<String[]> articles;
    private final DatabaseHelper dbHelper;

    public ArticleAdapter(Context context, ArrayList<String[]> articles, DatabaseHelper dbHelper) {
        super(context, R.layout.article_item, articles);
        this.context = context;
        this.articles = articles;
        this.dbHelper = dbHelper;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            LayoutInflater inflater = LayoutInflater.from(context);
            convertView = inflater.inflate(R.layout.article_item, parent, false);
        }

        String[] article = articles.get(position);

        ImageView ivPhoto = convertView.findViewById(R.id.ivPhoto);
        TextView tvName = convertView.findViewById(R.id.tvName);
        TextView tvPrice = convertView.findViewById(R.id.tvPrice);
        TextView tvStock = convertView.findViewById(R.id.tvStock);
        Button btnAddToCart = convertView.findViewById(R.id.btnAddToCart);

        tvName.setText(article[0]);
        tvPrice.setText("$" + article[1]);
        tvStock.setText("Stock: " + article[2]);

        int imageResId = context.getResources().getIdentifier(article[3], "drawable", context.getPackageName());
        if (imageResId != 0) {
            ivPhoto.setImageResource(imageResId);
        } else {
            ivPhoto.setImageResource(R.drawable.default_image);
        }

        // Gérer l'ajout au panier avec l'ID de l'utilisateur
        btnAddToCart.setOnClickListener(v -> {
            int articleId = getArticleId(article[0]);
            int quantity = 1; // Quantité par défaut
            long userId = getUserId(); // Récupérer l'ID de l'utilisateur connecté
            if (userId != -1) {
                addToCart(article[0], articleId, quantity, userId);
            } else {
                Toast.makeText(context, "Veuillez vous connecter", Toast.LENGTH_SHORT).show();
            }
        });

        return convertView;
    }

    // Récupérer l'ID de l'article à partir de son nom
    private int getArticleId(String articleName) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(DatabaseHelper.TABLE_ARTICLE,
                new String[]{DatabaseHelper.COLUMN_ARTICLE_ID},
                DatabaseHelper.COLUMN_ARTICLE_NAME + " = ?",
                new String[]{articleName},
                null, null, null);

        int articleId = -1;
        if (cursor.moveToFirst()) {
            articleId = cursor.getInt(cursor.getColumnIndex(DatabaseHelper.COLUMN_ARTICLE_ID));
        }
        cursor.close();
        return articleId;
    }

    // Méthode pour ajouter au panier avec l'ID de l'utilisateur
    private void addToCart(String articleName, int articleId, int quantity, long userId) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        // Récupérer les détails de l'article
        Cursor cursor = db.query(DatabaseHelper.TABLE_ARTICLE,
                new String[]{DatabaseHelper.COLUMN_ARTICLE_PRICE, DatabaseHelper.COLUMN_ARTICLE_STOCK},
                DatabaseHelper.COLUMN_ARTICLE_ID + " = ?",
                new String[]{String.valueOf(articleId)},
                null, null, null);

        if (cursor.moveToFirst()) {
            double price = cursor.getDouble(cursor.getColumnIndex(DatabaseHelper.COLUMN_ARTICLE_PRICE));
            int stock = cursor.getInt(cursor.getColumnIndex(DatabaseHelper.COLUMN_ARTICLE_STOCK));

            if (stock >= quantity) {
                double total = price * quantity;

                // Insérer ou mettre à jour l'article dans le panier
                ContentValues values = new ContentValues();
                values.put(DatabaseHelper.COLUMN_CART_ARTICLE_ID, articleId);
                values.put(DatabaseHelper.COLUMN_CART_USER_ID, userId); // Associer l'utilisateur
                values.put(DatabaseHelper.COLUMN_CART_QUANTITY, quantity);
                values.put(DatabaseHelper.COLUMN_CART_TOTAL, total);

                db.insertWithOnConflict(DatabaseHelper.TABLE_CART, null, values, SQLiteDatabase.CONFLICT_REPLACE);

                // Mettre à jour le stock de l'article
                values.clear();
                values.put(DatabaseHelper.COLUMN_ARTICLE_STOCK, stock - quantity);
                db.update(DatabaseHelper.TABLE_ARTICLE, values,
                        DatabaseHelper.COLUMN_ARTICLE_ID + " = ?", new String[]{String.valueOf(articleId)});

                // Mise à jour locale des articles pour refléter le stock réduit
                for (int i = 0; i < articles.size(); i++) {
                    if (articles.get(i)[0].equals(articleName)) {
                        articles.get(i)[2] = String.valueOf(stock - quantity);
                        break;
                    }
                }
                notifyDataSetChanged();

                Toast.makeText(context, "Article ajouté au panier", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(context, "Stock insuffisant", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(context, "Article non trouvé", Toast.LENGTH_SHORT).show();
        }
        cursor.close();
    }

    // Simuler la récupération de l'ID de l'utilisateur connecté
    private long getUserId() {
        // Logique pour récupérer l'ID de l'utilisateur depuis les préférences ou la base de données
        // Exemple temporaire, à remplacer par une vraie implémentation
        return 1; // ID utilisateur factice
    }
}
