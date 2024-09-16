package com.harena.myshopsqlite;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

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
        // You might want to add code to save the order details in the database here
        // For now, just showing a Toast
        Toast.makeText(this, "Commande validée", Toast.LENGTH_SHORT).show();
    }

    private void cancelOrder() {
        // Optionally, you can add code to clear the cart or navigate to another screen
        // For now, just showing a Toast
        Toast.makeText(this, "Commande annulée", Toast.LENGTH_SHORT).show();
    }
}
