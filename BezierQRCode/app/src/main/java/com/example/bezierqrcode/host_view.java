package com.example.bezierqrcode;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.zxing.BarcodeFormat;
import com.journeyapps.barcodescanner.BarcodeEncoder;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class host_view extends AppCompatActivity {

    private TextView idTV, lobbyTV;
    private ImageView qrCodeIV;
    private TextView participantsTV;
    private FirebaseFirestore db;
    private String sessionId;
    private String sessionName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_host_view);

        idTV = findViewById(R.id.idTV);
        lobbyTV = findViewById(R.id.lobbyTV);
        qrCodeIV = findViewById(R.id.qrCodeIV);
        participantsTV = findViewById(R.id.participantsTV);

        db = FirebaseFirestore.getInstance();

        // Get the lobby name from the intent
        sessionName = getIntent().getStringExtra("LOBBY_NAME");
        if (sessionName == null || sessionName.isEmpty()) {
            sessionName = "Untitled Lobby";
        }
        lobbyTV.setText(sessionName);

        createAttendanceSession();
    }

    private void createAttendanceSession() {
        long timestamp = System.currentTimeMillis();
        sessionId = generateShortId(6);
        String hostId = "SESSION-" + timestamp + "-" + new Random().nextInt(1000);

        Map<String, Object> sessionData = new HashMap<>();
        sessionData.put("active", true);
        sessionData.put("attendeeCount", 0);
        sessionData.put("hostId", hostId);
        sessionData.put("sessionId", sessionId);
        sessionData.put("sessionName", sessionName); // Use the user-provided name
        sessionData.put("timestamp", timestamp);

        db.collection("attendance_sessions").document(sessionId)
                .set(sessionData)
                .addOnSuccessListener(aVoid -> {
                    idTV.setText("Session ID: " + sessionId);
                    generateQRCode(sessionId);
                    listenForParticipants(sessionId);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to create session: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private String generateShortId(int length) {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        StringBuilder sb = new StringBuilder();
        Random random = new Random();
        for (int i = 0; i < length; i++) {
            sb.append(chars.charAt(random.nextInt(chars.length())));
        }
        return sb.toString();
    }

    private void generateQRCode(String data) {
        try {
            BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
            Bitmap bitmap = barcodeEncoder.encodeBitmap(data, BarcodeFormat.QR_CODE, 400, 400);
            qrCodeIV.setImageBitmap(bitmap);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void listenForParticipants(String sessionId) {
        db.collection("attendance_sessions").document(sessionId)
                .collection("participants")
                .orderBy("timestamp", Query.Direction.ASCENDING)
                .addSnapshotListener((snapshots, e) -> {
                    if (e != null) return;

                    if (snapshots != null) {
                        StringBuilder nameList = new StringBuilder("Participants:\n");
                        for (QueryDocumentSnapshot doc : snapshots) {
                            String name = doc.getString("name");
                            if (name != null) {
                                nameList.append("• ").append(name).append("\n");
                            }
                        }
                        participantsTV.setText(nameList.toString());
                    }
                });
    }
}
