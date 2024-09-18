package com.harena.myshopsqlite;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.harena.myshopsqlite.database.DatabaseHelper;

public class LoginActivity extends AppCompatActivity {

    private EditText etEmail, etPassword;
    private Button btnLogin, btnRegister;
    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        btnRegister = findViewById(R.id.btnRegister);

        dbHelper = new DatabaseHelper(this);

        btnLogin.setOnClickListener(v -> loginUser());
        btnRegister.setOnClickListener(v -> registerUser());
    }

    @Override
    protected void onStart() {
        super.onStart();

        // Vérifiez si un utilisateur est déjà connecté
        long userId = getUserId();
        if (userId != -1) {
            // Si l'utilisateur est connecté, redirigez vers MainActivity
            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
            startActivity(intent);
            finish(); // Terminez LoginActivity pour éviter que l'utilisateur puisse y revenir
        }
    }

    private void loginUser() {
        String email = etEmail.getText().toString();
        String password = etPassword.getText().toString();

        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
            Toast.makeText(this, "Please enter email and password", Toast.LENGTH_SHORT).show();
            return;
        }

        long userId = dbHelper.getUserIdByEmailAndPassword(email, password);

        if (userId != -1) {
            saveUserId(userId); // Stocker l'ID utilisateur
            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        } else {
            Toast.makeText(this, "Invalid email or password", Toast.LENGTH_SHORT).show();
        }
    }

    private void saveUserId(long userId) {
        // Obtenez l'instance de SharedPreferences
        SharedPreferences prefs = getSharedPreferences("MyPrefs", MODE_PRIVATE);

        // Créez un éditeur pour les SharedPreferences
        SharedPreferences.Editor editor = prefs.edit();

        // Stockez l'ID utilisateur
        editor.putLong("user_id", userId);

        // Appliquez les changements
        editor.apply();
    }

    private long getUserId() {
        SharedPreferences prefs = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        return prefs.getLong("user_id", -1); // -1 est la valeur par défaut si l'ID utilisateur n'est pas trouvé
    }

    private void registerUser() {
        String email = etEmail.getText().toString();
        String password = etPassword.getText().toString();

        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
            Toast.makeText(this, "Please enter email and password", Toast.LENGTH_SHORT).show();
            return;
        }

        long result = dbHelper.addUser(email, password);

        if (result == -1) {
            Toast.makeText(this, "Registration failed", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Registration successful", Toast.LENGTH_SHORT).show();
        }
    }
}
