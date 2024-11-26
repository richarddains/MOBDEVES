package com.example.jotty;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.TextView;
import android.view.View;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.Collections;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private RecyclerView recyclerView, recyclerViewPinned;
    private NoteAdapter noteAdapter, noteAdapterPinned;
    private List<Note> noteList, pinnedNoteList;
    private DatabaseReference notesReference;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        FloatingActionButton fabAddNotes = findViewById(R.id.fabAddNotes);
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigationView);

        sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        String userId = sharedPreferences.getString("userId", "defaultUser");
        notesReference = FirebaseDatabase.getInstance().getReference("Notes").child(userId);

        recyclerView = findViewById(R.id.recyclerView);
        recyclerViewPinned = findViewById(R.id.recyclerViewPinned);

        noteList = new ArrayList<>();
        pinnedNoteList = new ArrayList<>();

        noteAdapter = new NoteAdapter(this, noteList);
        noteAdapterPinned = new NoteAdapter(this, pinnedNoteList);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewPinned.setLayoutManager(new LinearLayoutManager(this));

        recyclerView.setAdapter(noteAdapter);
        recyclerViewPinned.setAdapter(noteAdapterPinned);

        ActivityResultLauncher<Intent> addNoteLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK) {
                        loadUserNotes();
                    }
                }
        );

        fabAddNotes.setOnClickListener(view -> {
            Intent intent = new Intent(MainActivity.this, CreateNote.class);
            addNoteLauncher.launch(intent);
        });

        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            Intent intent = null;
            int itemId = item.getItemId();
            if (itemId == R.id.homeButton) {
                return true;
            } else if (itemId == R.id.calendarButton) {
                intent = new Intent(MainActivity.this, Calendar.class);
            } else if (itemId == R.id.profileButton) {
                intent = new Intent(MainActivity.this, Profile.class);
            }

            if (intent != null) {
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();
            }
            return true;
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadUserNotes();
    }

    private void loadUserNotes() {
        noteList.clear();
        pinnedNoteList.clear();

        String userId = sharedPreferences.getString("userId", "defaultUser");
        notesReference = FirebaseDatabase.getInstance().getReference("Notes").child(userId);

        notesReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                noteList.clear();
                pinnedNoteList.clear();

                if (snapshot.exists()) {
                    for (DataSnapshot noteSnapshot : snapshot.getChildren()) {
                        Note note = noteSnapshot.getValue(Note.class);
                        if (note != null) {
                            if (note.isPinned()) {
                                pinnedNoteList.add(note);
                            } else {
                                noteList.add(note);
                            }
                        }
                    }

                    sortNotesByDateTime(noteList);
                    sortNotesByDateTime(pinnedNoteList);
                    noteAdapter.notifyDataSetChanged();
                    noteAdapterPinned.notifyDataSetChanged();

                    TextView pinnedNotesLabel = findViewById(R.id.pinnedNotesLabel);
                    RecyclerView recyclerViewPinned = findViewById(R.id.recyclerViewPinned);
                    TextView notesLabel = findViewById(R.id.notesLabel);

                    if (!pinnedNoteList.isEmpty()) {
                        pinnedNotesLabel.setVisibility(View.VISIBLE);
                        recyclerViewPinned.setVisibility(View.VISIBLE);
                        notesLabel.setVisibility(View.VISIBLE);
                    } else {
                        pinnedNotesLabel.setVisibility(View.GONE);
                        recyclerViewPinned.setVisibility(View.GONE);
                        notesLabel.setVisibility(View.GONE);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(MainActivity.this, "Failed to load notes", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void sortNotesByDateTime(List<Note> notes) {
        Collections.sort(notes, (note1, note2) -> {
            try {
                SimpleDateFormat dateFormat = new SimpleDateFormat("EEE, MMM d, yyyy hh:mm a", Locale.getDefault());
                Date date1 = dateFormat.parse(note1.getDateTime());
                Date date2 = dateFormat.parse(note2.getDateTime());

                if (date1 != null && date2 != null) {
                    return date2.compareTo(date1);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return 0;
        });
    }



}
