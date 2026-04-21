package com.example.bezierqrcode;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanOptions;

import java.util.HashMap;
import java.util.Map;

public class join_view extends AppCompatActivity {

    private ImageButton cameraButton, backButton;
    private EditText sessionIdET;
    private Button manualJoinBTN;
    private FirebaseFirestore db;

    private final ActivityResultLauncher<ScanOptions> qrCodeLauncher = registerForActivityResult(new ScanContract(),
            result -> {
                if(result.getContents() == null) {
                    Toast.makeText(this, "Cancelled", Toast.LENGTH_LONG).show();
                } else {
                    String scannedSessionId = result.getContents();
                    joinSession(scannedSessionId);
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_join_view);

        db = FirebaseFirestore.getInstance();

        cameraButton = findViewById(R.id.camera_open);
        backButton = findViewById(R.id.backBTN);
        sessionIdET = findViewById(R.id.sessionIdET);
        manualJoinBTN = findViewById(R.id.manualJoinBTN);

        cameraButton.setOnClickListener(v -> {
            ScanOptions options = new ScanOptions();
            options.setPrompt("Scan Session QR Code");
            options.setBeepEnabled(true);
            options.setOrientationLocked(false);
            qrCodeLauncher.launch(options);
        });

        backButton.setOnClickListener(v -> finish());

        manualJoinBTN.setOnClickListener(v -> {
            String sessionId = sessionIdET.getText().toString().trim().toUpperCase();
            if (!sessionId.isEmpty()) {
                joinSession(sessionId);
            } else {
                Toast.makeText(this, "Please enter a Session ID", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void joinSession(String sessionId) {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "Please sign in again", Toast.LENGTH_SHORT).show();
            return;
        }

        db.collection("users").document(currentUser.getUid()).get()
                .addOnSuccessListener(userDoc -> {
                    String name = userDoc.getString("displayName");
                    if (name == null) name = "Anonymous User";
                    final String userName = name;

                    db.collection("attendance_sessions").document(sessionId).get()
                            .addOnSuccessListener(documentSnapshot -> {
                                if (documentSnapshot.exists() && Boolean.TRUE.equals(documentSnapshot.getBoolean("active"))) {
                                    
                                    Map<String, Object> participantData = new HashMap<>();
                                    participantData.put("name", userName);
                                    participantData.put("timestamp", FieldValue.serverTimestamp());

                                    db.collection("attendance_sessions").document(sessionId)
                                            .collection("participants").add(participantData);

                                    db.collection("attendance_sessions").document(sessionId)
                                            .update("attendeeCount", FieldValue.increment(1))
                                            .addOnSuccessListener(aVoid -> {
                                                Intent intent = new Intent(join_view.this, join_lobby.class);
                                                intent.putExtra("LOBBY_ID", sessionId);
                                                startActivity(intent);
                                            });
                                } else {
                                    Toast.makeText(this, "Invalid or inactive session ID: " + sessionId, Toast.LENGTH_LONG).show();
                                }
                            })
                            .addOnFailureListener(e -> Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                });
    }
}
