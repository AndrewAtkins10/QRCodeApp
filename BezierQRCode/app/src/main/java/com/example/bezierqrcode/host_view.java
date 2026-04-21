package com.example.bezierqrcode;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.zxing.BarcodeFormat;
import com.journeyapps.barcodescanner.BarcodeEncoder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class host_view extends AppCompatActivity {

    private TextView idTV, lobbyTV;
    private ImageView qrCodeIV;
    private RecyclerView participantsRV;
    private ParticipantAdapter adapter;
    private List<Participant> participantList;
    private Button endSessionBTN;
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
        participantsRV = findViewById(R.id.participantsRV);
        endSessionBTN = findViewById(R.id.endSessionBTN);

        db = FirebaseFirestore.getInstance();

        // Initialize RecyclerView
        participantList = new ArrayList<>();
        adapter = new ParticipantAdapter(participantList, this::removeParticipant);
        participantsRV.setLayoutManager(new LinearLayoutManager(this));
        participantsRV.setAdapter(adapter);

        sessionName = getIntent().getStringExtra("LOBBY_NAME");
        if (sessionName == null || sessionName.isEmpty()) {
            sessionName = "Untitled Lobby";
        }
        lobbyTV.setText(sessionName);

        createAttendanceSession();

        endSessionBTN.setOnClickListener(v -> {
            if (sessionId != null) {
                db.collection("attendance_sessions").document(sessionId)
                        .update("active", false)
                        .addOnSuccessListener(aVoid -> {
                            Toast.makeText(host_view.this, "Session Ended", Toast.LENGTH_SHORT).show();
                            finish();
                        });
            }
        });
    }

    private void removeParticipant(Participant participant) {
        if (sessionId == null) return;

        db.collection("attendance_sessions").document(sessionId)
                .collection("participants").document(participant.getUid())
                .delete()
                .addOnSuccessListener(aVoid -> {
                    db.collection("attendance_sessions").document(sessionId)
                            .update("attendeeCount", FieldValue.increment(-1));
                    Toast.makeText(this, "User removed", Toast.LENGTH_SHORT).show();
                });
    }

    private void createAttendanceSession() {
        sessionId = generateShortId(6);
        long timestamp = System.currentTimeMillis();
        String hostId = "SESSION-" + timestamp + "-" + new Random().nextInt(1000);

        Map<String, Object> sessionData = new HashMap<>();
        sessionData.put("active", true);
        sessionData.put("attendeeCount", 0);
        sessionData.put("hostId", hostId);
        sessionData.put("sessionId", sessionId);
        sessionData.put("sessionName", sessionName);
        sessionData.put("timestamp", timestamp);

        db.collection("attendance_sessions").document(sessionId)
                .set(sessionData)
                .addOnSuccessListener(aVoid -> {
                    idTV.setText("ID: " + sessionId);
                    generateQRCode(sessionId);
                    listenForParticipants(sessionId);
                });
    }

    private void listenForParticipants(String sessionId) {
        db.collection("attendance_sessions").document(sessionId)
                .collection("participants")
                .orderBy("timestamp", Query.Direction.ASCENDING)
                .addSnapshotListener((snapshots, e) -> {
                    if (e != null || snapshots == null) return;

                    participantList.clear();
                    for (QueryDocumentSnapshot doc : snapshots) {
                        String name = doc.getString("name");
                        String uid = doc.getString("uid");
                        if (name != null && uid != null) {
                            participantList.add(new Participant(uid, name));
                        }
                    }
                    adapter.notifyDataSetChanged();
                });
    }

    private String generateShortId(int length) {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        StringBuilder sb = new StringBuilder();
        Random random = new Random();
        for (int i = 0; i < length; i++) sb.append(chars.charAt(random.nextInt(chars.length())));
        return sb.toString();
    }

    private void generateQRCode(String data) {
        try {
            BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
            Bitmap bitmap = barcodeEncoder.encodeBitmap(data, BarcodeFormat.QR_CODE, 400, 400);
            qrCodeIV.setImageBitmap(bitmap);
        } catch (Exception e) { e.printStackTrace(); }
    }
}
