package com.harena.myshopsqlite;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.harena.myshopsqlite.database.DatabaseHelper;

import java.util.ArrayList;

public class CartAdapter extends ArrayAdapter<String[]> {

    private final Context context;
    private final ArrayList<String[]> cartItems;

    public CartAdapter(Context context, ArrayList<String[]> cartItems) {
        super(context, R.layout.cart_item, cartItems);
        this.context = context;
        this.cartItems = cartItems;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            LayoutInflater inflater = LayoutInflater.from(context);
            convertView = inflater.inflate(R.layout.cart_item, parent, false);
        }

        String[] cartItem = cartItems.get(position);

        TextView tvName = convertView.findViewById(R.id.tvName);
        TextView tvQuantity = convertView.findViewById(R.id.tvQuantity);
        TextView tvPrice = convertView.findViewById(R.id.tvPrice);
        TextView tvTotal = convertView.findViewById(R.id.tvTotal);

        tvName.setText(cartItem[0]);
        tvQuantity.setText("Quantity: " + cartItem[1]);
        tvPrice.setText("Price: $" + cartItem[2]);
        tvTotal.setText("Total: $" + cartItem[3]);

        return convertView;
    }
}
