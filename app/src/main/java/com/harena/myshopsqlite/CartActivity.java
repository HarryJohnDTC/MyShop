package com.harena.myshopsqlite;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.harena.myshopsqlite.database.DatabaseHelper;

import java.util.ArrayList;

public class CartActivity extends AppCompatActivity {

    private DatabaseHelper dbHelper;
    private ListView lvCartItems;
    private Button btnValidateOrder;
    private Button btnCancelOrder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);

        lvCartItems = findViewById(R.id.lvCartItems);
        btnValidateOrder = findViewById(R.id.btnValidateOrder);
        btnCancelOrder = findViewById(R.id.btnCancelOrder);

        dbHelper = new DatabaseHelper(this);
        displayCartItems();

        btnValidateOrder.setOnClickListener(v -> {
            // Logic to validate the order
            validateOrder();
        });

        btnCancelOrder.setOnClickListener(v -> {
            // Logic to cancel the order
            cancelOrder();
        });
    }

    private void displayCartItems() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(DatabaseHelper.TABLE_CART, null, null, null, null, null, null);

        ArrayList<String[]> cartItems = new ArrayList<>();
        while (cursor.moveToNext()) {
            int articleId = cursor.getInt(cursor.getColumnIndex(DatabaseHelper.COLUMN_CART_ARTICLE_ID));
            int quantity = cursor.getInt(cursor.getColumnIndex(DatabaseHelper.COLUMN_CART_QUANTITY));
            double total = cursor.getDouble(cursor.getColumnIndex(DatabaseHelper.COLUMN_CART_TOTAL));

            Cursor articleCursor = db.query(DatabaseHelper.TABLE_ARTICLE,
                    new String[]{DatabaseHelper.COLUMN_ARTICLE_NAME, DatabaseHelper.COLUMN_ARTICLE_PRICE},
                    DatabaseHelper.COLUMN_ARTICLE_ID + " = ?",
                    new String[]{String.valueOf(articleId)},
                    null, null, null);

            if (articleCursor.moveToFirst()) {
                String name = articleCursor.getString(articleCursor.getColumnIndex(DatabaseHelper.COLUMN_ARTICLE_NAME));
                double price = articleCursor.getDouble(articleCursor.getColumnIndex(DatabaseHelper.COLUMN_ARTICLE_PRICE));

                boolean itemFound = false;
                for (String[] item : cartItems) {
                    if (item[0].equals(name)) {
                        int existingQuantity = Integer.parseInt(item[1]);
                        double existingTotal = Double.parseDouble(item[3]);

                        item[1] = String.valueOf(existingQuantity + quantity);
                        item[3] = String.valueOf(existingTotal + total);
                        itemFound = true;
                        break;
                    }
                }

                if (!itemFound) {
                    cartItems.add(new String[]{name, String.valueOf(quantity), String.valueOf(price), String.valueOf(total)});
                }
            }
            articleCursor.close();
        }
        cursor.close();

        CartAdapter adapter = new CartAdapter(this, cartItems);
        lvCartItems.setAdapter(adapter);
    }

    private void validateOrder() {
        // Récupérer les détails de la commande à partir du panier
        StringBuilder orderDetails = new StringBuilder();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(DatabaseHelper.TABLE_CART, null, null, null, null, null, null);

        double totalPrice = 0.0;
        while (cursor.moveToNext()) {
            int articleId = cursor.getInt(cursor.getColumnIndex(DatabaseHelper.COLUMN_CART_ARTICLE_ID));
            int quantity = cursor.getInt(cursor.getColumnIndex(DatabaseHelper.COLUMN_CART_QUANTITY));
            double total = cursor.getDouble(cursor.getColumnIndex(DatabaseHelper.COLUMN_CART_TOTAL));
            totalPrice += total;

            // Récupérer les informations de l'article
            Cursor articleCursor = db.query(DatabaseHelper.TABLE_ARTICLE,
                    new String[]{DatabaseHelper.COLUMN_ARTICLE_NAME, DatabaseHelper.COLUMN_ARTICLE_PRICE},
                    DatabaseHelper.COLUMN_ARTICLE_ID + " = ?",
                    new String[]{String.valueOf(articleId)},
                    null, null, null);

            if (articleCursor.moveToFirst()) {
                String name = articleCursor.getString(articleCursor.getColumnIndex(DatabaseHelper.COLUMN_ARTICLE_NAME));
                double price = articleCursor.getDouble(articleCursor.getColumnIndex(DatabaseHelper.COLUMN_ARTICLE_PRICE));

                // Ajouter les détails de la commande dans une String
                orderDetails.append("Article: ").append(name)
                        .append(", Quantité: ").append(quantity)
                        .append(", Prix unitaire: ").append(price)
                        .append("€, Total: ").append(total).append("€\n");
            }
            articleCursor.close();
        }
        cursor.close();
        db.close();

        // Ajouter le prix total à la fin des détails de la commande
        orderDetails.append("\nPrix total de la commande: ").append(totalPrice).append("€");

        // Afficher le popup de confirmation
        new AlertDialog.Builder(this)
                .setTitle("Commande validée")
                .setMessage("Votre commande a été validée. Détails:\n\n" + orderDetails.toString())
                .setPositiveButton("OK", (dialog, which) -> {
                    // Envoyer un email
                    sendOrderConfirmationEmail(orderDetails.toString());
                })
                .setNegativeButton("Annuler", null)
                .show();
    }

    private void sendOrderConfirmationEmail(String orderDetails) {
        // Préparer l'intent pour envoyer un email
        Intent emailIntent = new Intent(Intent.ACTION_SENDTO);
        emailIntent.setData(Uri.parse("mailto:")); // Only email apps should handle this
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Confirmation de votre commande");
        emailIntent.putExtra(Intent.EXTRA_TEXT, "Merci d'avoir passé commande. Voici les détails:\n\n" + orderDetails);

        try {
            startActivity(Intent.createChooser(emailIntent, "Envoyer l'email de confirmation"));
        } catch (android.content.ActivityNotFoundException ex) {
            Toast.makeText(this, "Aucune application de messagerie installée.", Toast.LENGTH_SHORT).show();
        }
    }


    private void cancelOrder() {
        // Créer une boîte de dialogue de confirmation
        new AlertDialog.Builder(this)
                .setTitle("Annulation de la commande")
                .setMessage("Êtes-vous sûr de vouloir annuler la commande ?")
                .setPositiveButton(android.R.string.yes, (dialog, which) -> {
                    // Si l'utilisateur confirme l'annulation
                    SQLiteDatabase db = dbHelper.getWritableDatabase();

                    // Sélectionner tous les articles du panier
                    Cursor cursor = db.query(DatabaseHelper.TABLE_CART,
                            new String[]{DatabaseHelper.COLUMN_CART_ARTICLE_ID, DatabaseHelper.COLUMN_CART_QUANTITY},
                            null, null, null, null, null);

                    // Parcourir chaque article du panier et restaurer le stock
                    while (cursor.moveToNext()) {
                        int articleId = cursor.getInt(cursor.getColumnIndex(DatabaseHelper.COLUMN_CART_ARTICLE_ID));
                        int quantity = cursor.getInt(cursor.getColumnIndex(DatabaseHelper.COLUMN_CART_QUANTITY));

                        // Récupérer le stock actuel de l'article
                        Cursor articleCursor = db.query(DatabaseHelper.TABLE_ARTICLE,
                                new String[]{DatabaseHelper.COLUMN_ARTICLE_STOCK},
                                DatabaseHelper.COLUMN_ARTICLE_ID + " = ?",
                                new String[]{String.valueOf(articleId)}, null, null, null);

                        if (articleCursor.moveToFirst()) {
                            int currentStock = articleCursor.getInt(articleCursor.getColumnIndex(DatabaseHelper.COLUMN_ARTICLE_STOCK));

                            // Calculer le nouveau stock en réajoutant la quantité annulée
                            int newStock = currentStock + quantity;

                            // Mettre à jour le stock dans la base de données
                            ContentValues values = new ContentValues();
                            values.put(DatabaseHelper.COLUMN_ARTICLE_STOCK, newStock);
                            db.update(DatabaseHelper.TABLE_ARTICLE, values,
                                    DatabaseHelper.COLUMN_ARTICLE_ID + " = ?",
                                    new String[]{String.valueOf(articleId)});
                        }
                        articleCursor.close();
                    }
                    cursor.close();

                    // Vider le panier après l'annulation de la commande
                    db.delete(DatabaseHelper.TABLE_CART, null, null);

                    // Afficher un message de confirmation
                    Toast.makeText(CartActivity.this, "Commande annulée, stock restauré", Toast.LENGTH_SHORT).show();

                    // Mettre à jour l'affichage des articles du panier
                    displayCartItems();
                })
                .setNegativeButton(android.R.string.no, null)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

}
