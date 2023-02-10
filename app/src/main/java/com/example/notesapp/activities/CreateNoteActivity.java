package com.example.notesapp.activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.notesapp.R;
import com.example.notesapp.database.NotesDatabase;
import com.example.notesapp.entities.Note;
import com.google.android.material.bottomsheet.BottomSheetBehavior;

import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class CreateNoteActivity extends AppCompatActivity {

    private EditText inputNoteTitle, inputNoteSubtitle, inputText;
    private TextView textDateTime, textUrl;
    private ImageView imageSave;
    private View viewSubtitleIndicator;
    private String selectedNoteColor;
    private ImageView imageNote;
    private LinearLayout layoutUrl;
    private String selectedImagePath;
    private Uri selectedImageUri;
    private AlertDialog dialogAddUrl;
    private static final int REQUEST_CODE_STORAGE_PERMISSION = 1;
    private static final int REQUEST_CODE_SELECT_IMAGE = 2;
    private Note alreadyAvailableNote;

    private void setViewOrUpdateNote() {
        inputNoteTitle.setText(alreadyAvailableNote.getTitle());
        inputNoteSubtitle.setText(alreadyAvailableNote.getSubtitle());
        inputText.setText(alreadyAvailableNote.getText());
        textDateTime.setText(alreadyAvailableNote.getDateTime());

        if(alreadyAvailableNote.getImagePath() != null && !alreadyAvailableNote.getImagePath().trim().isEmpty()) {
            imageNote.setImageBitmap(BitmapFactory.decodeFile(alreadyAvailableNote.getImagePath()));
            imageNote.setVisibility(View.VISIBLE);
        }

        if(alreadyAvailableNote.getWebLink() != null && !alreadyAvailableNote.getWebLink().trim().isEmpty()) {
            textUrl.setText(alreadyAvailableNote.getWebLink());
            layoutUrl.setVisibility(View.VISIBLE);
        }
    }

    private void showAddUrlDialog() {
        if (dialogAddUrl == null) {
            AlertDialog.Builder builder = new AlertDialog.Builder(CreateNoteActivity.this);
            View view = LayoutInflater.from(this).inflate(
                    R.layout.layout_add_url,
                    (ViewGroup) findViewById(R.id.layoutAddUrlContainer)
            );
            builder.setView(view);

            dialogAddUrl = builder.create();
            if (dialogAddUrl.getWindow() != null) {
                dialogAddUrl.getWindow().setBackgroundDrawable(new ColorDrawable(0));
            }

            final EditText inputUrl = view.findViewById(R.id.inputURL);
            inputUrl.requestFocus();

            view.findViewById(R.id.urlAdd).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (inputUrl.getText().toString().trim().isEmpty()) {
                        Toast.makeText(CreateNoteActivity.this, "Enter URL", Toast.LENGTH_SHORT).show();
                    } else if (!Patterns.WEB_URL.matcher(inputUrl.getText().toString()).matches()) {
                        Toast.makeText(CreateNoteActivity.this, "Enter valid URL", Toast.LENGTH_SHORT).show();
                    } else {
                        textUrl.setText(inputUrl.getText().toString());
                        layoutUrl.setVisibility(View.VISIBLE);
                        dialogAddUrl.dismiss();
                    }
                }
            });

            view.findViewById(R.id.urlCancel).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialogAddUrl.dismiss();
                }
            });
        }

        dialogAddUrl.show();
    }

    private String getPathFromUri(Uri contentUri) {
        String filePath;
        Cursor cursor = getContentResolver().query(
                contentUri,
                null,
                null,
                null,
                null
        );

        if (cursor == null) {
            filePath = contentUri.getPath();
        } else {
            cursor.moveToFirst();
            int index = cursor.getColumnIndex("_data");
            filePath = cursor.getString(index);
            cursor.close();
        }

        return filePath;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_SELECT_IMAGE && resultCode == RESULT_OK) {
            if (data != null) {
                selectedImageUri = data.getData();
                Toast.makeText(this, "OKAY#", Toast.LENGTH_SHORT).show();
                try {
                    InputStream inputStream = getContentResolver().openInputStream(selectedImageUri);
                    Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                    imageNote.setImageBitmap(bitmap);
                    imageNote.setVisibility(View.VISIBLE);

                    Toast.makeText(this, "PATH", Toast.LENGTH_SHORT).show();
                    selectedImagePath = getPathFromUri(selectedImageUri);
//                    selectedImagePath = selectedImageUri.toString();
                    Toast.makeText(this, selectedImagePath, Toast.LENGTH_SHORT).show();
                } catch (Exception e) {
                    Toast.makeText(CreateNoteActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        Toast.makeText(CreateNoteActivity.this, "LEts go", Toast.LENGTH_SHORT).show();

        if (requestCode == REQUEST_CODE_STORAGE_PERMISSION && grantResults.length > 0) {
            Toast.makeText(CreateNoteActivity.this, "GREAT", Toast.LENGTH_SHORT).show();
            Toast.makeText(CreateNoteActivity.this, String.valueOf(grantResults[0]), Toast.LENGTH_SHORT).show();
            Toast.makeText(CreateNoteActivity.this, String.valueOf(PackageManager.PERMISSION_GRANTED), Toast.LENGTH_SHORT).show();

            if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "All OKAY", Toast.LENGTH_SHORT).show();
                selectImage();
            } else {
                Toast.makeText(CreateNoteActivity.this, "Permission Denied", Toast.LENGTH_SHORT).show();
            }
        }

//        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
//        if (requestCode == REQUEST_CODE_STORAGE_PERMISSION) {
//            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                // Permission granted.
//                Toast.makeText(this, "All OKAY", Toast.LENGTH_SHORT).show();
//                selectImage();
//            } else {
//                // User refused to grant permission.
//                Toast.makeText(CreateNoteActivity.this, "Permission Denied", Toast.LENGTH_SHORT).show();
//            }
//        }

    }

    private void selectImage() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(intent, REQUEST_CODE_SELECT_IMAGE);
        }
    }

    private void setSubtitleIndicator() {
        GradientDrawable gradientDrawable = (GradientDrawable) viewSubtitleIndicator.getBackground();
        gradientDrawable.setColor(Color.parseColor(selectedNoteColor));
    }

    private void initMiscellaneous() {
        final LinearLayout layoutMiscellaneous = findViewById(R.id.layoutMiscellaneous);
        final BottomSheetBehavior bottomSheetBehavior = BottomSheetBehavior.from(layoutMiscellaneous);
        layoutMiscellaneous.findViewById(R.id.textMiscellaneous).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (bottomSheetBehavior.getState() != BottomSheetBehavior.STATE_EXPANDED) {
                    bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                } else {
                    bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                }
            }
        });

        final ImageView imageColor1st = layoutMiscellaneous.findViewById(R.id.imageColor1st);
        final ImageView imageColor2nd = layoutMiscellaneous.findViewById(R.id.imageColor2nd);
        final ImageView imageColor3rd = layoutMiscellaneous.findViewById(R.id.imageColor3rd);
        final ImageView imageColor4th = layoutMiscellaneous.findViewById(R.id.imageColor4th);
        final ImageView imageColor5th = layoutMiscellaneous.findViewById(R.id.imageColor5th);

        layoutMiscellaneous.findViewById(R.id.viewColor1st).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectedNoteColor = "#333333";
                imageColor1st.setImageResource(R.drawable.ic_done);
                imageColor2nd.setImageResource(0);
                imageColor3rd.setImageResource(0);
                imageColor4th.setImageResource(0);
                imageColor5th.setImageResource(0);
                setSubtitleIndicator();
            }
        });

        layoutMiscellaneous.findViewById(R.id.viewColor2nd).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectedNoteColor = "#FDBE3B";
                imageColor1st.setImageResource(0);
                imageColor2nd.setImageResource(R.drawable.ic_done);
                imageColor3rd.setImageResource(0);
                imageColor4th.setImageResource(0);
                imageColor5th.setImageResource(0);
                setSubtitleIndicator();
            }
        });

        layoutMiscellaneous.findViewById(R.id.viewColor3rd).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectedNoteColor = "#FF4842";
                imageColor1st.setImageResource(0);
                imageColor2nd.setImageResource(0);
                imageColor3rd.setImageResource(R.drawable.ic_done);
                imageColor4th.setImageResource(0);
                imageColor5th.setImageResource(0);
                setSubtitleIndicator();
            }
        });

        layoutMiscellaneous.findViewById(R.id.viewColor4th).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectedNoteColor = "#3A52Fc";
                imageColor1st.setImageResource(0);
                imageColor2nd.setImageResource(0);
                imageColor3rd.setImageResource(0);
                imageColor4th.setImageResource(R.drawable.ic_done);
                imageColor5th.setImageResource(0);
                setSubtitleIndicator();
            }
        });

        layoutMiscellaneous.findViewById(R.id.viewColor5th).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectedNoteColor = "#000000";
                imageColor1st.setImageResource(0);
                imageColor2nd.setImageResource(0);
                imageColor3rd.setImageResource(0);
                imageColor4th.setImageResource(0);
                imageColor5th.setImageResource(R.drawable.ic_done);
                setSubtitleIndicator();
            }
        });

        if (alreadyAvailableNote != null && alreadyAvailableNote.getColor() != null && !alreadyAvailableNote.getColor().trim().isEmpty()) {
            switch (alreadyAvailableNote.getColor()) {
                case "#FDBE3B":
                    layoutMiscellaneous.findViewById(R.id.viewColor2nd).performClick();
                    break;
                case "#FF4842":
                    layoutMiscellaneous.findViewById(R.id.viewColor3rd).performClick();
                    break;
                case "#3A52Fc":
                    layoutMiscellaneous.findViewById(R.id.viewColor4th).performClick();
                    break;
                case "#000000":
                    layoutMiscellaneous.findViewById(R.id.viewColor5th).performClick();
                    break;
            }
        }

        layoutMiscellaneous.findViewById(R.id.layoutAddImage).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                if(checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(
                            CreateNoteActivity.this,
                            new String[] {
                                    Manifest.permission.READ_EXTERNAL_STORAGE,
                                    Manifest.permission.READ_MEDIA_IMAGES
                            },
                            REQUEST_CODE_STORAGE_PERMISSION
                    );
                    Toast.makeText(CreateNoteActivity.this, "SHOW", Toast.LENGTH_SHORT).show();
                } else {
                    selectImage();
                }

//                if (
//                        Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
//                        && ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
//                ) {
//                    Toast.makeText(CreateNoteActivity.this, "SHOW", Toast.LENGTH_SHORT).show();
//
//                    ActivityCompat.requestPermissions(
//                            CreateNoteActivity.this,
//                            new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
//                            REQUEST_CODE_STORAGE_PERMISSION);
//                } else {
//                    selectImage();
//                }

//                if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
//                        != PackageManager.PERMISSION_GRANTED) {
//
//                    // Should we show an explanation?
//                    if (shouldShowRequestPermissionRationale(
//                            Manifest.permission.READ_EXTERNAL_STORAGE)) {
//                        // Explain to the user why we need to read the contacts
//                        Toast.makeText(CreateNoteActivity.this, "SHOW", Toast.LENGTH_SHORT).show();
//                    }
//
//                    ActivityCompat.requestPermissions(
//                            CreateNoteActivity.this,
//                            new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
//                            REQUEST_CODE_STORAGE_PERMISSION);
//
//                    // MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE is an
//                    // app-defined int constant that should be quite unique
//
//                    return;
//                }
            }
        });

        layoutMiscellaneous.findViewById(R.id.layoutAddUrl).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAddUrlDialog();
            }
        });
    }

    private void saveNote() {
        if (inputNoteTitle.getText().toString().trim().isEmpty()) {
            Toast.makeText(CreateNoteActivity.this, "Note title cannot be empty", Toast.LENGTH_SHORT).show();
            return;
        } else if(inputNoteSubtitle.getText().toString().isEmpty() && inputText.getText().toString().isEmpty()) {
            Toast.makeText(CreateNoteActivity.this, "Note cannot be empty", Toast.LENGTH_SHORT).show();
            return;
        }

        final Note note = new Note();

        note.setTitle(inputNoteTitle.getText().toString());
        note.setSubtitle(inputNoteSubtitle.getText().toString());
        note.setText(inputText.getText().toString());
        note.setDateTime(textDateTime.getText().toString());
        note.setColor(selectedNoteColor);
        note.setImagePath(selectedImagePath);

        if (layoutUrl.getVisibility() == View.VISIBLE) {
            note.setWebLink(textUrl.getText().toString());
        }

        if (alreadyAvailableNote != null) {
            note.setId(alreadyAvailableNote.getId());
        }

        @SuppressLint("StaticFieldLeak")
        class SaveNoteTask extends AsyncTask<Void, Void, Void> {
            @Override
            protected Void doInBackground(Void... voids) {
                NotesDatabase.getDatabase(getApplicationContext()).noteDao().insertNote(note);
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                Intent intent = new Intent();
                setResult(RESULT_OK, intent);
                finish();
            }
        }
        new SaveNoteTask().execute();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_note);

        ImageView imageBack = findViewById(R.id.imageBack);
        imageBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        inputNoteTitle = findViewById(R.id.inputNoteTitle);
        inputNoteSubtitle = findViewById(R.id.inputNoteSubtitle);
        inputText = findViewById(R.id.inputNote);
        textDateTime = findViewById(R.id.textDateTime);
        viewSubtitleIndicator = findViewById(R.id.subtitleIndicator);
        imageNote = findViewById(R.id.imageNote);
        textUrl = findViewById(R.id.textUrl);
        layoutUrl = findViewById(R.id.layoutUrl);

        textDateTime.setText(
                new SimpleDateFormat("EEEE, dd MMMM yyyy HH:mm a", Locale.getDefault())
                        .format(new Date())
        );

        imageSave = findViewById(R.id.imageSave);
        imageSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveNote();
            }
        });

        selectedNoteColor = "#333333";
        selectedImagePath = "";

        if(getIntent().getBooleanExtra("isViewOrUpdate", false)) {
            alreadyAvailableNote = (Note) getIntent().getSerializableExtra("note");
            setViewOrUpdateNote();
        }

        initMiscellaneous();

        setSubtitleIndicator();
    }
}