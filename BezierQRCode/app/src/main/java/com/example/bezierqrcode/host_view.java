package com.example.bezierqrcode;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.zxing.BarcodeFormat;
import com.journeyapps.barcodescanner.BarcodeEncoder;

import java.util.UUID;

public class host_view extends AppCompatActivity {

    private TextView idTV;
    private ImageView qrCodeIV;
    private TextView participantsTV;
    private DatabaseReference mDatabase;
    private String lobbyId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_host_view);

        idTV = findViewById(R.id.idTV);
        qrCodeIV = findViewById(R.id.qrCodeIV);
        participantsTV = findViewById(R.id.participantsTV);

        mDatabase = FirebaseDatabase.getInstance().getReference();

        // Generate a unique lobby ID
        lobbyId = UUID.randomUUID().toString().substring(0, 8);
        idTV.setText("ID: " + lobbyId);

        // Save lobby to Firebase
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            mDatabase.child("lobbies").child(lobbyId).child("host").setValue(user.getEmail());
        }

        // Generate and display the QR code
        try {
            BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
            Bitmap bitmap = barcodeEncoder.encodeBitmap(lobbyId, BarcodeFormat.QR_CODE, 400, 400);
            qrCodeIV.setImageBitmap(bitmap);
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Listen for participants
        mDatabase.child("lobbies").child(lobbyId).child("participants")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        StringBuilder participants = new StringBuilder("Participants:\n");
                        for (DataSnapshot participant : snapshot.getChildren()) {
                            participants.append(participant.getValue(String.class)).append("\n");
                        }
                        participantsTV.setText(participants.toString());
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        // Error handle
                    }
                });
    }
}
