package com.example.jotty;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class CreateNote extends AppCompatActivity {

    private EditText inputNoteTitle, inputNoteSubtitle, inputNoteContent;
    private TextView textDateTime;
    private DatabaseReference notesReference;
    private SharedPreferences sharedPreferences;
    private String noteId = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_note);

        inputNoteTitle = findViewById(R.id.inputNoteTitle);
        inputNoteSubtitle = findViewById(R.id.inputNoteSubtitle);
        inputNoteContent = findViewById(R.id.inputNote);
        textDateTime = findViewById(R.id.textDateTime);
        Button saveButton = findViewById(R.id.saveButton);
        ImageView backButton = findViewById(R.id.imageBack);

        sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        String userId = sharedPreferences.getString("userId", "defaultUser");

        notesReference = FirebaseDatabase.getInstance().getReference("Notes").child(userId);
        backButton.setOnClickListener(view -> finish());

        Intent intent = getIntent();
        if (intent.hasExtra("noteId")) {
            noteId = intent.getStringExtra("noteId");
            inputNoteTitle.setText(intent.getStringExtra("noteTitle"));
            inputNoteSubtitle.setText(intent.getStringExtra("noteSubtitle"));
            inputNoteContent.setText(intent.getStringExtra("noteContent"));
            textDateTime.setText(intent.getStringExtra("noteDateTime"));
        } else {
            textDateTime.setText(
                    new SimpleDateFormat("EEE, MMM d, yyyy hh:mm a", Locale.getDefault()).format(new Date())
            );
        }
        saveButton.setOnClickListener(v -> saveNote());
    }

    private void saveNote() {
        String title = inputNoteTitle.getText().toString().trim();
        String subtitle = inputNoteSubtitle.getText().toString().trim();
        String content = inputNoteContent.getText().toString().trim();
        String dateTime = textDateTime.getText().toString();

        if (title.isEmpty() && content.isEmpty()) {
            Toast.makeText(this, "Please enter some content", Toast.LENGTH_SHORT).show();
            return;
        }

        Note note = new Note(title, subtitle, content, dateTime);

        if (noteId != null) {
            note.setId(noteId);
            String updatedDateTime = new SimpleDateFormat("EEE, MMM d, yyyy hh:mm a", Locale.getDefault()).format(new Date());
            note.setDateTime(updatedDateTime);

            notesReference.child(noteId).setValue(note)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(this, "Note updated successfully", Toast.LENGTH_SHORT).show();
                        sendUpdatedNoteBack();
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(this, "Failed to update note", Toast.LENGTH_SHORT).show()
                    );
        } else {
            String newNoteId = notesReference.push().getKey();
            note.setId(newNoteId);
            notesReference.child(newNoteId).setValue(note)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(this, "Note saved successfully", Toast.LENGTH_SHORT).show();
                        sendUpdatedNoteBack();
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(this, "Failed to save note", Toast.LENGTH_SHORT).show()
                    );
        }
    }

    private void sendUpdatedNoteBack() {
        Intent resultIntent = new Intent();
        resultIntent.putExtra("noteId", noteId);
        resultIntent.putExtra("noteTitle", inputNoteTitle.getText().toString());
        resultIntent.putExtra("noteSubtitle", inputNoteSubtitle.getText().toString());
        resultIntent.putExtra("noteContent", inputNoteContent.getText().toString());
        resultIntent.putExtra("noteDateTime", textDateTime.getText().toString());
        setResult(RESULT_OK, resultIntent);
        finish();
    }
}
