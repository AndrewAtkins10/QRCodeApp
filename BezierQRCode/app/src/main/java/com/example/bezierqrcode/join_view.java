package com.example.bezierqrcode;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanOptions;

public class join_view extends AppCompatActivity {

    private ImageButton cameraButton;
    private DatabaseReference mDatabase;

    private final ActivityResultLauncher<ScanOptions> qrCodeLauncher = registerForActivityResult(new ScanContract(),
            result -> {
                if(result.getContents() == null) {
                    Toast.makeText(this, "Cancelled", Toast.LENGTH_LONG).show();
                } else {
                    // The QR code was scanned successfully
                    String lobbyId = result.getContents();
                    joinLobby(lobbyId);
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_join_view);

        mDatabase = FirebaseDatabase.getInstance().getReference();

        cameraButton = findViewById(R.id.camera_open);
        cameraButton.setOnClickListener(v -> {
            ScanOptions options = new ScanOptions();
            options.setPrompt("Scan a QR Code");
            options.setBeepEnabled(true);
            options.setOrientationLocked(false);
            qrCodeLauncher.launch(options);
        });
    }

    private void joinLobby(String lobbyId) {
        mDatabase.child("lobbies").child(lobbyId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                    if (user != null) {
                        String email = user.getEmail();
                        // Add user to participants list in Firebase
                        mDatabase.child("lobbies").child(lobbyId).child("participants").push().setValue(email);

                        // Start the join_lobby activity and pass the lobby ID
                        Intent intent = new Intent(join_view.this, join_lobby.class);
                        intent.putExtra("LOBBY_ID", lobbyId);
                        startActivity(intent);
                    }
                } else {
                    Toast.makeText(join_view.this, "Lobby not found!", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(join_view.this, "Error connecting to database", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
