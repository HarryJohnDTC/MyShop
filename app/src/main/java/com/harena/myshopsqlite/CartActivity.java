package com.harena.myshopsqlite;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.widget.ListView;
import androidx.appcompat.app.AppCompatActivity;
import com.harena.myshopsqlite.database.DatabaseHelper;
import java.util.ArrayList;

public class CartActivity extends AppCompatActivity {

    private DatabaseHelper dbHelper;
    private ListView lvCartItems;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);

        lvCartItems = findViewById(R.id.lvCartItems);
        dbHelper = new DatabaseHelper(this);
        displayCartItems();
    }

    private void displayCartItems() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(DatabaseHelper.TABLE_CART, null, null, null, null, null, null);

        // Use a HashMap to keep track of article IDs and their aggregated data
        ArrayList<String[]> cartItems = new ArrayList<>();
        while (cursor.moveToNext()) {
            int articleId = cursor.getInt(cursor.getColumnIndex(DatabaseHelper.COLUMN_CART_ARTICLE_ID));
            int quantity = cursor.getInt(cursor.getColumnIndex(DatabaseHelper.COLUMN_CART_QUANTITY));
            double total = cursor.getDouble(cursor.getColumnIndex(DatabaseHelper.COLUMN_CART_TOTAL));

            // Get article details
            Cursor articleCursor = db.query(DatabaseHelper.TABLE_ARTICLE,
                    new String[]{DatabaseHelper.COLUMN_ARTICLE_NAME, DatabaseHelper.COLUMN_ARTICLE_PRICE},
                    DatabaseHelper.COLUMN_ARTICLE_ID + " = ?",
                    new String[]{String.valueOf(articleId)},
                    null, null, null);

            if (articleCursor.moveToFirst()) {
                String name = articleCursor.getString(articleCursor.getColumnIndex(DatabaseHelper.COLUMN_ARTICLE_NAME));
                double price = articleCursor.getDouble(articleCursor.getColumnIndex(DatabaseHelper.COLUMN_ARTICLE_PRICE));

                // Check if the item already exists in the list to aggregate quantities
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
}
