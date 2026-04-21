package com.example.bezierqrcode;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;

public class host_join extends AppCompatActivity {

    private TextView emailTV2;
    private Button joinBTN;
    private Button hostBTN;
    private ImageButton logoutBTN;
    private EditText lobbyNameET;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_host_join);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        emailTV2 = findViewById(R.id.emailTV2);
        joinBTN = findViewById(R.id.joinBTN);
        hostBTN = findViewById(R.id.hostBTN);
        logoutBTN = findViewById(R.id.logoutBTN);
        lobbyNameET = findViewById(R.id.lobbyNameET);

        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("USER_EMAIL")) {
            String email = intent.getStringExtra("USER_EMAIL");
            emailTV2.setText(email);
        }

        joinBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(host_join.this, join_view.class);
                startActivity(intent);
            }
        });

        hostBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String lobbyName = lobbyNameET.getText().toString().trim();
                if (lobbyName.isEmpty()) {
                    Toast.makeText(host_join.this, "Please enter a lobby name", Toast.LENGTH_SHORT).show();
                    return;
                }
                Intent intent = new Intent(host_join.this, host_view.class);
                intent.putExtra("LOBBY_NAME", lobbyName);
                startActivity(intent);
            }
        });

        logoutBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseAuth.getInstance().signOut();
                Intent intent = new Intent(host_join.this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            }
        });
    }
}
