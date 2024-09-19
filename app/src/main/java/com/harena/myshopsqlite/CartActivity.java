package com.harena.myshopsqlite;

import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class CartActivity extends AppCompatActivity {

    private DatabaseHelper dbHelper;
    private ListView lvCartItems;
    private Button btnValidateOrder;
    private Button btnCancelOrder;
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
    String currentDate = sdf.format(new Date());


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
        // Retrieve cart details
        StringBuilder orderDetails = new StringBuilder();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(DatabaseHelper.TABLE_CART, null, null, null, null, null, null);

        final double[] totalPriceHolder = {0.0}; // Holder for totalPrice
        long userId = getCurrentUserId(); // Get the current logged-in user ID

        // Calculate the total price before creating the order
        while (cursor.moveToNext()) {
            int articleId = cursor.getInt(cursor.getColumnIndex(DatabaseHelper.COLUMN_CART_ARTICLE_ID));
            int quantity = cursor.getInt(cursor.getColumnIndex(DatabaseHelper.COLUMN_CART_QUANTITY));
            double total = cursor.getDouble(cursor.getColumnIndex(DatabaseHelper.COLUMN_CART_TOTAL));
            totalPriceHolder[0] += total;
        }
        cursor.moveToFirst(); // Move cursor back to start to retrieve details again

        long commandeId = -1;
        if (cursor.getCount() > 0) {
            // Create the order (commande) with the total price
            commandeId = dbHelper.addCommande(userId, currentDate, totalPriceHolder[0]);
        }

        // Fetch details for the order lines
        cursor = db.query(DatabaseHelper.TABLE_CART, null, null, null, null, null, null);
        while (cursor.moveToNext()) {
            int articleId = cursor.getInt(cursor.getColumnIndex(DatabaseHelper.COLUMN_CART_ARTICLE_ID));
            int quantity = cursor.getInt(cursor.getColumnIndex(DatabaseHelper.COLUMN_CART_QUANTITY));
            double total = cursor.getDouble(cursor.getColumnIndex(DatabaseHelper.COLUMN_CART_TOTAL));

            // Retrieve article info
            Cursor articleCursor = db.query(DatabaseHelper.TABLE_ARTICLE,
                    new String[]{DatabaseHelper.COLUMN_ARTICLE_NAME, DatabaseHelper.COLUMN_ARTICLE_PRICE},
                    DatabaseHelper.COLUMN_ARTICLE_ID + " = ?",
                    new String[]{String.valueOf(articleId)},
                    null, null, null);

            if (articleCursor.moveToFirst()) {
                String name = articleCursor.getString(articleCursor.getColumnIndex(DatabaseHelper.COLUMN_ARTICLE_NAME));
                double price = articleCursor.getDouble(articleCursor.getColumnIndex(DatabaseHelper.COLUMN_ARTICLE_PRICE));

                // Add order details to a String
                orderDetails.append("Article: ").append(name)
                        .append(", Quantité: ").append(quantity)
                        .append(", Prix unitaire: ").append(price)
                        .append("€, Total: ").append(total).append("€\n");

                // Add the order line to the database
                dbHelper.addCommandeLine(commandeId, articleId, quantity, total);
            }
            articleCursor.close();
        }
        cursor.close();

        // Add the total price to the end of the order details
        final double totalPrice = totalPriceHolder[0];
        orderDetails.append("\nTotal de la commande: ").append(totalPrice).append("€");

        // Show confirmation dialog
        new AlertDialog.Builder(this)
                .setTitle("Commande validée")
                .setMessage("Votre commande a été validée. Détails:\n\n" + orderDetails.toString())
                .setPositiveButton("OK", (dialog, which) -> {
                    // Clear the cart after order is validated
                    SQLiteDatabase writableDb = dbHelper.getWritableDatabase();
                    writableDb.delete(DatabaseHelper.TABLE_CART, null, null);
                    writableDb.close();

                    Toast.makeText(CartActivity.this, "Commande validée", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .setNegativeButton("Annuler", null)
                .show();
    }



    // Dummy method to simulate getting the current user's ID
    private long getCurrentUserId() {
        // Récupère l'ID utilisateur à partir de SharedPreferences
        SharedPreferences prefs = getSharedPreferences("MyPrefs", MODE_PRIVATE);

        // Retourne l'ID utilisateur stocké, ou -1 si aucun utilisateur n'est connecté
        return prefs.getLong("user_id", -1);
    }



    /*private void sendOrderConfirmationEmail(String orderDetails) {
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
    }*/


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
