package com.example.bezierqrcode;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class join_lobby extends AppCompatActivity {

    private TextView lobbyIdTV;
    private Button doneBTN;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.join_lobby);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        lobbyIdTV = findViewById(R.id.lobbyIdTV);
        doneBTN = findViewById(R.id.doneBTN);

        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("LOBBY_ID")) {
            String lobbyId = intent.getStringExtra("LOBBY_ID");
            lobbyIdTV.setText("Lobby ID: " + lobbyId);
        }

        doneBTN.setOnClickListener(v -> {
            // Return to join_view as requested
            Intent backIntent = new Intent(join_lobby.this, join_view.class);
            backIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(backIntent);
            finish();
        });
    }
}