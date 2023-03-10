package com.example.notesapp.activities;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.notesapp.R;
import com.example.notesapp.adapters.NotesAdapter;
import com.example.notesapp.database.NotesDatabase;
import com.example.notesapp.entities.Note;
import com.example.notesapp.listeners.NotesListener;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements NotesListener {

    public static final int REQUEST_CODE_ADD_NOTE = 1;
    public static final int REQUEST_CODE_UPDATE_NOTE = 2;
    public static final int REQUEST_CODE_SHOW_NOTES = 3;

    private int noteClickedPosition = -1;

    private RecyclerView notesRecyclerView;
    private List<Note> noteList;
    private NotesAdapter notesAdapter;

    private void getNotes(final int requestCode, final boolean isNoteDeleted) {

        @SuppressLint("StaticFieldLeak")
        class GetNotesTask extends AsyncTask<Void, Void, List<Note>> {
            @Override
            protected List<Note> doInBackground(Void... voids) {
                return NotesDatabase
                        .getDatabase(getApplicationContext())
                        .noteDao().getAllNotes();
            }

            @Override
            protected void onPostExecute(List<Note> notes) {
                super.onPostExecute(noteList);
//                Toast.makeText(MainActivity.this, "NOTE WAS CREATED", Toast.LENGTH_SHORT).show();
//                Log.d("MY_NOTES", noteList.toString());
//                if(noteList.size() == 0) {
//                    noteList.addAll(notes);
//                    notesAdapter.notifyDataSetChanged();
//                } else {
//                    noteList.add(0, notes.get(0));
//                    notesAdapter.notifyItemInserted(0);
//                }
//                notesRecyclerView.smoothScrollToPosition(0);
                Toast.makeText(MainActivity.this, String.valueOf(requestCode), Toast.LENGTH_SHORT).show();
                if (requestCode == REQUEST_CODE_SHOW_NOTES) {
//                    SHOW ALL
                    noteList.addAll(notes);
                    notesAdapter.notifyDataSetChanged();
                } else if (requestCode == REQUEST_CODE_ADD_NOTE) {
//                  ADD
                    Toast.makeText(MainActivity.this, "ADD", Toast.LENGTH_SHORT).show();

                    noteList.add(0, notes.get(0));
                    notesAdapter.notifyItemInserted(0);
                    notesRecyclerView.smoothScrollToPosition(0);
                } else if (requestCode == REQUEST_CODE_UPDATE_NOTE) {
//                    Update
                    Toast.makeText(MainActivity.this, "UPDATE", Toast.LENGTH_SHORT).show();

                    noteList.remove(noteClickedPosition);

                    if (isNoteDeleted) {
//                        if note has deleted
                        notesAdapter.notifyItemRemoved(noteClickedPosition);
                    } else {
                        noteList.add(noteClickedPosition, notes.get(noteClickedPosition));
                        notesAdapter.notifyItemChanged(noteClickedPosition);
                    }

                }
            }
        }
        new GetNotesTask().execute();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ImageView imageAddNoteMain = findViewById(R.id.addNoteMain);
        imageAddNoteMain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivityForResult(
                        new Intent(getApplicationContext(), CreateNoteActivity.class), REQUEST_CODE_ADD_NOTE
                );
            }
        });

        notesRecyclerView = findViewById(R.id.notesRecyclerView);
        notesRecyclerView.setLayoutManager(
                new StaggeredGridLayoutManager(
                        2,
                        StaggeredGridLayoutManager.VERTICAL
                )
        );

        noteList = new ArrayList<>();
        notesAdapter = new NotesAdapter(noteList, this);
        notesRecyclerView.setAdapter(notesAdapter);

        getNotes(REQUEST_CODE_SHOW_NOTES, false);

        EditText inputSearch = findViewById(R.id.inputSearch);
        inputSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                notesAdapter.cancelTimer();
            }

            @Override
            public void afterTextChanged(Editable s) {
//                  notesAdapter.getItemCount() -- count words in RecyclerView.
//                  But we need number of all notes -- noteList.size()
//                Toast.makeText(MainActivity.this, String.valueOf(notesAdapter.getItemCount()), Toast.LENGTH_SHORT).show();
                if(noteList.size() != 0) {
                    notesAdapter.searchNotes(s.toString());
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Toast.makeText(this, "REQUEST - " + String.valueOf(requestCode), Toast.LENGTH_SHORT).show();
        Toast.makeText(this, "RESULT - " + String.valueOf(resultCode), Toast.LENGTH_SHORT).show();

        if (requestCode == REQUEST_CODE_ADD_NOTE && resultCode == RESULT_OK) {
            getNotes(REQUEST_CODE_ADD_NOTE, false);
        } else if (requestCode == REQUEST_CODE_UPDATE_NOTE && resultCode == RESULT_OK){
            if (data != null) {
                getNotes(REQUEST_CODE_UPDATE_NOTE, data.getBooleanExtra("isNoteDeleted", false));
            }
        }
    }

    @Override
    public void onNoteClicked(Note note, int position) {
        noteClickedPosition = position;
        Intent intent = new Intent(getApplicationContext(), CreateNoteActivity.class);
        intent.putExtra("isViewOrUpdate", true);
        intent.putExtra("note", note);

        startActivityForResult(intent, REQUEST_CODE_UPDATE_NOTE);

//        Toast.makeText(this, String.valueOf(intent.getData()), Toast.LENGTH_SHORT).show();
//
//        startActivity(intent, new Bundle(REQUEST_CODE_UPDATE_NOTE));
    }
}