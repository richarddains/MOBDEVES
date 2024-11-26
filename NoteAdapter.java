package com.example.jotty;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.LinearLayout;
import android.widget.Toast;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import android.view.MotionEvent;
import java.util.Collections;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;
public class NoteAdapter extends RecyclerView.Adapter<NoteAdapter.NoteViewHolder> {

    private List<Note> noteList;
    private Context context;
    private SharedPreferences sharedPreferences;

    public NoteAdapter(Context context, List<Note> noteList) {
        this.context = context;
        this.noteList = noteList;
        sharedPreferences = context.getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
    }

    @NonNull
    @Override
    public NoteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_container_note, parent, false);
        return new NoteViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NoteViewHolder holder, int position) {
        Note note = noteList.get(position);
        holder.textTitle.setText(note.getTitle());
        holder.textSubtitle.setText(note.getSubtitle());
        holder.textDateTime.setText(note.getDateTime());

        holder.noteContainer.setOnClickListener(v -> {
            if (!holder.isSwiping) {
                Intent intent = new Intent(context, CreateNote.class);
                intent.putExtra("noteId", note.getId());
                intent.putExtra("noteTitle", note.getTitle());
                intent.putExtra("noteSubtitle", note.getSubtitle());
                intent.putExtra("noteContent", note.getContent());
                intent.putExtra("noteDateTime", note.getDateTime());
                context.startActivity(intent);
            }
        });

        holder.btnPin.setOnClickListener(v -> {
            note.setPinned(!note.isPinned());
            updateNotePinnedStatus(note);
            noteList.remove(position);
            if (note.isPinned()) {
                noteList.add(0, note);
            } else {
                noteList.add(note);
            }
            notifyItemMoved(position, 0);
        });

        holder.btnDelete.setOnClickListener(v -> {
            noteList.remove(position);
            notifyItemRemoved(position);
            deleteNoteFromDatabase(note);
        });

        holder.noteContainer.setOnTouchListener((v, event) -> {
            float swipeThreshold = -100;
            boolean isSwipe = false;
            boolean isSwipeBack = false;

            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    holder.initialTouchX = event.getX();
                    holder.isSwiping = false;
                    break;
                case MotionEvent.ACTION_MOVE:
                    float deltaX = event.getX() - holder.initialTouchX;

                    if (deltaX < 0) {
                        if (deltaX < swipeThreshold) {
                            deltaX = swipeThreshold;
                        }
                        holder.noteContainer.setTranslationX(deltaX);

                        if (deltaX <= swipeThreshold) {
                            holder.buttonContainer.setVisibility(View.VISIBLE);
                        }
                        isSwipe = true;
                        holder.isSwiping = true;
                    } else if (deltaX > 0) {
                        if (deltaX > 0) {
                            holder.noteContainer.setTranslationX(Math.min(deltaX, 0));
                        }
                        isSwipeBack = true;
                    }
                    break;

                case MotionEvent.ACTION_UP:
                    if (holder.noteContainer.getTranslationX() <= swipeThreshold) {
                        holder.noteContainer.setTranslationX(swipeThreshold);
                        holder.buttonContainer.setVisibility(View.VISIBLE);
                    } else if (holder.noteContainer.getTranslationX() > 0) {
                        holder.noteContainer.setTranslationX(0);
                        holder.buttonContainer.setVisibility(View.GONE);
                    } else {
                        holder.noteContainer.setTranslationX(0);
                        holder.buttonContainer.setVisibility(View.GONE);
                    }
                    break;
            }

            if (isSwipe || isSwipeBack) {
                return true;
            }

            return false;
        });
    }

    @Override
    public int getItemCount() {
        return noteList.size();
    }

    private void updateNotePinnedStatus(Note note) {
        DatabaseReference noteRef = FirebaseDatabase.getInstance().getReference("Notes")
                .child(sharedPreferences.getString("userId", "defaultUser"))
                .child(note.getId());
        noteRef.setValue(note)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(context, "Note pinned status updated", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(context, "Failed to update pinned status", Toast.LENGTH_SHORT).show();
                });
    }

    private void deleteNoteFromDatabase(Note note) {
        DatabaseReference noteRef = FirebaseDatabase.getInstance().getReference("Notes")
                .child(sharedPreferences.getString("userId", "defaultUser"))
                .child(note.getId());
        noteRef.removeValue()
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(context, "Note deleted successfully", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(context, "Failed to delete note", Toast.LENGTH_SHORT).show();
                });
    }

    static class NoteViewHolder extends RecyclerView.ViewHolder {
        TextView textTitle, textSubtitle, textDateTime;
        View noteContainer;
        LinearLayout buttonContainer;
        ImageButton btnPin, btnDelete;
        float initialTouchX;
        boolean isSwiping = false;

        NoteViewHolder(@NonNull View itemView) {
            super(itemView);
            textTitle = itemView.findViewById(R.id.textTitle);
            textSubtitle = itemView.findViewById(R.id.textSubtitle);
            textDateTime = itemView.findViewById(R.id.textDateTime);
            noteContainer = itemView.findViewById(R.id.noteContainer);
            buttonContainer = itemView.findViewById(R.id.buttonContainer);
            btnPin = itemView.findViewById(R.id.btnPin);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }
}
