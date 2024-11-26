package com.example.jotty;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

public class Profile extends AppCompatActivity {

    private TextView helloUsernameText;
    private EditText usernameText;
    private EditText bioInput;
    private Button saveButton;
    private Button logoutButton;
    private FloatingActionButton fabAddNote;
    private BottomNavigationView bottomNavigationView;
    private SharedPreferences sharedPreferences;
    private DatabaseReference usersReference;
    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        helloUsernameText = findViewById(R.id.greetingText);
        usernameText = findViewById(R.id.usernameInput);
        bioInput = findViewById(R.id.bioInput);
        saveButton = findViewById(R.id.saveButton);
        logoutButton = findViewById(R.id.logoutButton);
        fabAddNote = findViewById(R.id.fabAddNotes);
        bottomNavigationView = findViewById(R.id.bottomNavigationView);

        sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        userId = sharedPreferences.getString("userId", "");
        usersReference = FirebaseDatabase.getInstance().getReference("Users").child(userId);

        bottomNavigationView.setSelectedItemId(R.id.profileButton);

        loadUserProfileData();
        ActivityResultLauncher<Intent> addNoteLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK) {
                        // Update your profile if needed after adding a note
                    }
                }
        );

        fabAddNote.setOnClickListener(view -> {
            Intent intent = new Intent(Profile.this, CreateNote.class);
            addNoteLauncher.launch(intent);
        });

        saveButton.setOnClickListener(v -> {
            String bioText = bioInput.getText().toString();
            String updatedUsername = usernameText.getText().toString();

            saveUserProfile(updatedUsername, bioText);

            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString("username", updatedUsername);
            editor.putString("bio", bioText);
            editor.apply();
            helloUsernameText.setText("Hello, " + updatedUsername + "!");

            Toast.makeText(Profile.this, "Profile updated!", Toast.LENGTH_SHORT).show();
        });


        logoutButton.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();

            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.clear();
            editor.apply();

            Intent intent = new Intent(Profile.this, Login.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        });

        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            Intent intent = null;

            if (item.getItemId() == R.id.homeButton) {
                intent = new Intent(Profile.this, MainActivity.class);
            } else if (item.getItemId() == R.id.calendarButton) {
                intent = new Intent(Profile.this, Calendar.class);
            } else if (item.getItemId() == R.id.profileButton) {
                return true; // Stay in the profile
            }

            if (intent != null) {
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();
            }

            return true;
        });
    }

    private void loadUserProfileData() {
        usersReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String username = dataSnapshot.child("username").getValue(String.class);
                    String bio = dataSnapshot.child("bio").getValue(String.class);

                    if (username != null) {
                        usernameText.setText(username);
                        helloUsernameText.setText("Hello, " + username + "!");
                    }
                    if (bio != null) {
                        bioInput.setText(bio);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(Profile.this, "Failed to load profile data.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void saveUserProfile(String username, String bio) {
        String userId = sharedPreferences.getString("userId", null);

        if (userId != null) {
            DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("Users").child(userId);
            userRef.child("username").setValue(username);
            userRef.child("bio").setValue(bio);

            Toast.makeText(Profile.this, "Profile updated!", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(Profile.this, "User ID not found!", Toast.LENGTH_SHORT).show();
        }
    }
}
