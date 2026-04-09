package com.example.bezierqrcode;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private EditText emailET, passwordET, firstNameET, lastNameET;
    private Button signBTN, signUpBTN;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        emailET = findViewById(R.id.emailET);
        passwordET = findViewById(R.id.passwordET);
        firstNameET = findViewById(R.id.firstNameET);
        lastNameET = findViewById(R.id.lastNameET);
        signBTN = findViewById(R.id.signBTN);
        signUpBTN = findViewById(R.id.signUpBTN);

        signBTN.setOnClickListener(v -> handleSignIn());
        signUpBTN.setOnClickListener(v -> handleSignUp());
    }

    private void handleSignIn() {
        String email = emailET.getText().toString().trim();
        String password = passwordET.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please enter email and password", Toast.LENGTH_SHORT).show();
            return;
        }

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            // Update lastLoginAt on successful sign-in
                            db.collection("users").document(user.getUid())
                                    .update("lastLoginAt", new Timestamp(new Date()));
                            checkUserInFirestore(email);
                        }
                    } else {
                        Toast.makeText(this, "Sign-in failed: " + (task.getException() != null ? task.getException().getMessage() : "Check credentials"), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void handleSignUp() {
        String email = emailET.getText().toString().trim();
        String password = passwordET.getText().toString().trim();
        String firstName = firstNameET.getText().toString().trim();
        String lastName = lastNameET.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty() || firstName.isEmpty() || lastName.isEmpty()) {
            Toast.makeText(this, "Please fill all fields for Sign Up", Toast.LENGTH_SHORT).show();
            return;
        }

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        saveUserToFirestore(email, firstName, lastName);
                    } else {
                        Toast.makeText(this, "Sign-up failed: " + (task.getException() != null ? task.getException().getMessage() : "Unknown error"), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void saveUserToFirestore(String email, String firstName, String lastName) {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) return;

        Timestamp now = new Timestamp(new Date());

        Map<String, Object> userMap = new HashMap<>();
        userMap.put("uid", currentUser.getUid());
        userMap.put("email", email);
        userMap.put("displayName", firstName + " " + lastName);
        userMap.put("photoURL", ""); // Default empty string
        userMap.put("createdAt", now);
        userMap.put("lastLoginAt", now);

        db.collection("users").document(currentUser.getUid())
                .set(userMap)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(MainActivity.this, "Account Created!", Toast.LENGTH_SHORT).show();
                    navigateToNext(email);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(MainActivity.this, "Firestore Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    private void checkUserInFirestore(String email) {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) return;

        db.collection("users").document(currentUser.getUid()).get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult().exists()) {
                        navigateToNext(email);
                    } else {
                        Toast.makeText(this, "User record not found in Firestore.", Toast.LENGTH_SHORT).show();
                        mAuth.signOut();
                    }
                });
    }

    private void navigateToNext(String email) {
        Intent intent = new Intent(MainActivity.this, host_join.class);
        intent.putExtra("USER_EMAIL", email);
        startActivity(intent);
        finish();
    }
}
