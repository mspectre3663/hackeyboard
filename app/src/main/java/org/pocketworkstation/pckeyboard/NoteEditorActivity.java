package org.pocketworkstation.pckeyboard;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

public class NoteEditorActivity extends Activity {

    public static final String EXTRA_NOTE_ID = "extra_note_id";
    public static final String EXTRA_NOTE_TITLE = "extra_note_title";
    public static final String EXTRA_NOTE_CONTENT = "extra_note_content";

    public interface NoteSaveCallback {
        void onNoteSaved();
    }

    private static NoteSaveCallback sCallback;

    public static void setCallback(NoteSaveCallback callback) {
        sCallback = callback;
    }

    private String mNoteId = null;
    private EditText mTitleEdit;
    private EditText mContentEdit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle(R.string.tab_notepad);

        mNoteId = getIntent().getStringExtra(EXTRA_NOTE_ID);
        String initialTitle = getIntent().getStringExtra(EXTRA_NOTE_TITLE);
        String initialContent = getIntent().getStringExtra(EXTRA_NOTE_CONTENT);

        final boolean isEdit = mNoteId != null && !mNoteId.isEmpty();

        // Main Dialog layout
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(Color.parseColor("#18191E"));
        int pad = dpToPx(16);
        root.setPadding(pad, pad, pad, pad);

        // Header Title
        TextView header = new TextView(this);
        header.setText(isEdit ? "✏️ " + getString(R.string.edit_note_title) : "➕ " + getString(R.string.btn_new_note));
        header.setTextColor(Color.WHITE);
        header.setTextSize(TypedValue.COMPLEX_UNIT_SP, 17);
        header.setTypeface(Typeface.DEFAULT_BOLD);
        root.addView(header);

        // Help notice
        TextView notice = new TextView(this);
        notice.setText(R.string.note_editor_help);
        notice.setTextColor(Color.parseColor("#8E92A4"));
        notice.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
        notice.setPadding(0, dpToPx(4), 0, dpToPx(12));
        root.addView(notice);

        ScrollView scroll = new ScrollView(this);
        LinearLayout fieldsLayout = new LinearLayout(this);
        fieldsLayout.setOrientation(LinearLayout.VERTICAL);

        // Title label
        TextView titleLabel = new TextView(this);
        titleLabel.setText(R.string.note_title_label);
        titleLabel.setTextColor(Color.parseColor("#CCD0E0"));
        titleLabel.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
        fieldsLayout.addView(titleLabel);

        mTitleEdit = new EditText(this);
        mTitleEdit.setHint(R.string.note_title_hint);
        mTitleEdit.setHintTextColor(Color.parseColor("#5A5D6E"));
        mTitleEdit.setTextColor(Color.WHITE);
        mTitleEdit.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
        mTitleEdit.setBackgroundColor(Color.parseColor("#22242C"));
        int editPad = dpToPx(10);
        mTitleEdit.setPadding(editPad, editPad, editPad, editPad);
        mTitleEdit.setMaxLines(2);
        if (initialTitle != null) mTitleEdit.setText(initialTitle);
        LinearLayout.LayoutParams titleLp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        titleLp.setMargins(0, dpToPx(4), 0, dpToPx(12));
        fieldsLayout.addView(mTitleEdit, titleLp);

        // Content label
        TextView contentLabel = new TextView(this);
        contentLabel.setText(R.string.note_content_label);
        contentLabel.setTextColor(Color.parseColor("#CCD0E0"));
        contentLabel.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
        fieldsLayout.addView(contentLabel);

        mContentEdit = new EditText(this);
        mContentEdit.setHint(R.string.note_content_hint);
        mContentEdit.setHintTextColor(Color.parseColor("#5A5D6E"));
        mContentEdit.setTextColor(Color.WHITE);
        mContentEdit.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
        mContentEdit.setBackgroundColor(Color.parseColor("#22242C"));
        mContentEdit.setPadding(editPad, editPad, editPad, editPad);
        mContentEdit.setMinLines(4);
        mContentEdit.setMaxLines(8);
        mContentEdit.setGravity(Gravity.TOP | Gravity.START);
        if (initialContent != null) mContentEdit.setText(initialContent);
        LinearLayout.LayoutParams contentLp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        contentLp.setMargins(0, dpToPx(4), 0, dpToPx(16));
        fieldsLayout.addView(mContentEdit, contentLp);

        scroll.addView(fieldsLayout);
        root.addView(scroll, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1.0f));

        // Buttons row
        LinearLayout btnRow = new LinearLayout(this);
        btnRow.setOrientation(LinearLayout.HORIZONTAL);
        btnRow.setGravity(Gravity.END | Gravity.CENTER_VERTICAL);
        btnRow.setPadding(0, dpToPx(12), 0, 0);

        Button cancelBtn = new Button(this);
        cancelBtn.setText(R.string.btn_cancel);
        cancelBtn.setTextColor(Color.parseColor("#CCD0E0"));
        cancelBtn.setBackgroundColor(Color.parseColor("#2B2D38"));
        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        LinearLayout.LayoutParams cancelLp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        cancelLp.setMargins(0, 0, dpToPx(10), 0);
        btnRow.addView(cancelBtn, cancelLp);

        Button saveBtn = new Button(this);
        saveBtn.setText(R.string.btn_save);
        saveBtn.setTextColor(Color.WHITE);
        saveBtn.setBackgroundColor(Color.parseColor("#2E5BFF"));
        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String title = mTitleEdit.getText().toString();
                String content = mContentEdit.getText().toString();
                if (content.trim().isEmpty()) {
                    Toast.makeText(NoteEditorActivity.this, R.string.error_empty_note_content, Toast.LENGTH_SHORT).show();
                    return;
                }
                if (isEdit) {
                    ClipboardHelper.updateNote(NoteEditorActivity.this, mNoteId, title, content);
                } else {
                    ClipboardHelper.addNote(NoteEditorActivity.this, title, content);
                }
                if (sCallback != null) {
                    sCallback.onNoteSaved();
                    sCallback = null;
                }
                finish();
            }
        });
        btnRow.addView(saveBtn);
        root.addView(btnRow);

        setContentView(root);

        // Auto-show keyboard
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        final EditText targetFocus = (initialTitle == null || initialTitle.isEmpty()) ? mTitleEdit : mContentEdit;
        targetFocus.requestFocus();
        if (targetFocus == mContentEdit) {
            mContentEdit.setSelection(mContentEdit.getText().length());
        }
        targetFocus.postDelayed(new Runnable() {
            @Override
            public void run() {
                android.view.inputmethod.InputMethodManager imm = (android.view.inputmethod.InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                if (imm != null) {
                    imm.showSoftInput(targetFocus, android.view.inputmethod.InputMethodManager.SHOW_IMPLICIT);
                }
            }
        }, 150);
    }

    private int dpToPx(float dp) {
        return (int) (dp * getResources().getDisplayMetrics().density + 0.5f);
    }
}
