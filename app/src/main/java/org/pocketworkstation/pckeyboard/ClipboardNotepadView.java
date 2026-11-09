package org.pocketworkstation.pckeyboard;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.Typeface;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

public class ClipboardNotepadView extends LinearLayout {

    public interface ClipboardNotepadListener {
        void onPasteText(String text);
        void onCloseRequested();
    }

    private ClipboardNotepadListener mListener;
    private int mCurrentTab = 0; // 0 = Clipboard, 1 = Notepad

    private TextView mTabClipboard;
    private TextView mTabNotepad;
    private FrameLayout mContentContainer;

    private LinearLayout mClipboardLayout;
    private LinearLayout mNotepadLayout;

    // In-place note editor
    private View mNoteEditorOverlay;
    private EditText mEditNoteTitle;
    private EditText mEditNoteContent;
    private String mEditingNoteId = null;

    public ClipboardNotepadView(Context context) {
        super(context);
        init(context);
    }

    public ClipboardNotepadView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public void setListener(ClipboardNotepadListener listener) {
        mListener = listener;
    }

    private void init(Context context) {
        setOrientation(VERTICAL);
        setBackgroundColor(Color.parseColor("#15161A"));

        // 1. Top Bar: Tabs & Close Button
        LinearLayout topBar = new LinearLayout(context);
        topBar.setOrientation(HORIZONTAL);
        topBar.setGravity(Gravity.CENTER_VERTICAL);
        topBar.setBackgroundColor(Color.parseColor("#1B1C22"));
        topBar.setPadding(dpToPx(8), dpToPx(4), dpToPx(8), dpToPx(4));

        mTabClipboard = createTabButton(context, "📋 " + context.getString(R.string.tab_clipboard), 0);
        mTabNotepad = createTabButton(context, "📝 " + context.getString(R.string.tab_notepad), 1);

        topBar.addView(mTabClipboard);
        topBar.addView(mTabNotepad);

        // Spacer
        View spacer = new View(context);
        LinearLayout.LayoutParams spacerLp = new LinearLayout.LayoutParams(0, 1, 1.0f);
        topBar.addView(spacer, spacerLp);

        // Close button
        TextView closeBtn = new TextView(context);
        closeBtn.setText("✕");
        closeBtn.setTextColor(Color.WHITE);
        closeBtn.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
        closeBtn.setGravity(Gravity.CENTER);
        closeBtn.setBackgroundColor(Color.parseColor("#272933"));
        int pad = dpToPx(8);
        closeBtn.setPadding(pad, pad, pad, pad);
        closeBtn.setClickable(true);
        closeBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mListener != null) {
                    mListener.onCloseRequested();
                }
            }
        });
        topBar.addView(closeBtn, new LinearLayout.LayoutParams(dpToPx(38), dpToPx(38)));

        addView(topBar, new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));

        // Divider
        View div = new View(context);
        div.setBackgroundColor(Color.parseColor("#2C2E38"));
        addView(div, new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, dpToPx(1)));

        // 2. Content Container (Clipboard / Notepad / Editor)
        mContentContainer = new FrameLayout(context);
        addView(mContentContainer, new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, 0, 1.0f));

        buildClipboardView(context);
        buildNotepadView(context);
        buildEditorView(context);

        selectTab(0);
    }

    private TextView createTabButton(final Context context, String title, final int tabIndex) {
        TextView tv = new TextView(context);
        tv.setText(title);
        tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
        tv.setTypeface(Typeface.DEFAULT_BOLD);
        tv.setGravity(Gravity.CENTER);
        int padH = dpToPx(14);
        int padV = dpToPx(8);
        tv.setPadding(padH, padV, padH, padV);
        tv.setClickable(true);
        tv.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                selectTab(tabIndex);
            }
        });
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        lp.setMargins(0, 0, dpToPx(6), 0);
        tv.setLayoutParams(lp);
        return tv;
    }

    public void selectTab(int tabIndex) {
        mCurrentTab = tabIndex;
        if (mNoteEditorOverlay != null) {
            mNoteEditorOverlay.setVisibility(GONE);
        }

        if (tabIndex == 0) {
            // Clipboard
            mTabClipboard.setTextColor(Color.WHITE);
            mTabClipboard.setBackgroundColor(Color.parseColor("#343746"));
            mTabNotepad.setTextColor(Color.parseColor("#8E92A2"));
            mTabNotepad.setBackgroundColor(Color.TRANSPARENT);

            mClipboardLayout.setVisibility(VISIBLE);
            mNotepadLayout.setVisibility(GONE);
            refreshClipboardList();
        } else {
            // Notepad
            mTabNotepad.setTextColor(Color.WHITE);
            mTabNotepad.setBackgroundColor(Color.parseColor("#343746"));
            mTabClipboard.setTextColor(Color.parseColor("#8E92A2"));
            mTabClipboard.setBackgroundColor(Color.TRANSPARENT);

            mNotepadLayout.setVisibility(VISIBLE);
            mClipboardLayout.setVisibility(GONE);
            refreshNotepadList();
        }
    }

    // --- TAB 1: CLIPBOARD ---

    private LinearLayout mClipsContainer;

    private void buildClipboardView(final Context context) {
        mClipboardLayout = new LinearLayout(context);
        mClipboardLayout.setOrientation(VERTICAL);

        // Subheader
        LinearLayout subHeader = new LinearLayout(context);
        subHeader.setOrientation(HORIZONTAL);
        subHeader.setGravity(Gravity.CENTER_VERTICAL);
        subHeader.setPadding(dpToPx(12), dpToPx(6), dpToPx(12), dpToPx(6));

        TextView infoText = new TextView(context);
        infoText.setText("کپی‌های ذخیره‌شده (برای درج لمس کنید):");
        infoText.setTextColor(Color.parseColor("#A0A4B4"));
        infoText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
        subHeader.addView(infoText, new LinearLayout.LayoutParams(0, LayoutParams.WRAP_CONTENT, 1.0f));

        TextView clearAll = new TextView(context);
        clearAll.setText("🗑 پاک‌کردن همه");
        clearAll.setTextColor(Color.parseColor("#E06A6A"));
        clearAll.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
        clearAll.setPadding(dpToPx(8), dpToPx(4), dpToPx(8), dpToPx(4));
        clearAll.setBackgroundColor(Color.parseColor("#282024"));
        clearAll.setClickable(true);
        clearAll.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                ClipboardHelper.clearAllClips(context);
                refreshClipboardList();
            }
        });
        subHeader.addView(clearAll);
        mClipboardLayout.addView(subHeader);

        // Scrollable list
        ScrollView scroll = new ScrollView(context);
        mClipsContainer = new LinearLayout(context);
        mClipsContainer.setOrientation(VERTICAL);
        mClipsContainer.setPadding(dpToPx(10), dpToPx(4), dpToPx(10), dpToPx(12));
        scroll.addView(mClipsContainer);

        mClipboardLayout.addView(scroll, new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, 0, 1.0f));
        mContentContainer.addView(mClipboardLayout);
    }

    public void refreshClipboardList() {
        if (mClipsContainer == null) return;
        mClipsContainer.removeAllViews();
        final Context context = getContext();

        List<String> clips = ClipboardHelper.getClips(context);
        if (clips.isEmpty()) {
            TextView empty = new TextView(context);
            empty.setText(R.string.empty_clipboard);
            empty.setTextColor(Color.parseColor("#787B8A"));
            empty.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
            empty.setGravity(Gravity.CENTER);
            empty.setPadding(0, dpToPx(32), 0, dpToPx(32));
            mClipsContainer.addView(empty);
            return;
        }

        for (int i = 0; i < clips.size(); i++) {
            final int clipIndex = i;
            final String clipText = clips.get(i);

            LinearLayout card = new LinearLayout(context);
            card.setOrientation(HORIZONTAL);
            card.setGravity(Gravity.CENTER_VERTICAL);
            card.setBackgroundColor(Color.parseColor("#1F2027"));
            card.setPadding(dpToPx(12), dpToPx(10), dpToPx(8), dpToPx(10));
            card.setClickable(true);
            card.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mListener != null) {
                        mListener.onPasteText(clipText);
                    }
                }
            });

            TextView text = new TextView(context);
            text.setText(clipText);
            text.setTextColor(Color.WHITE);
            text.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
            text.setMaxLines(3);
            text.setEllipsize(TextUtils.TruncateAt.END);
            card.addView(text, new LinearLayout.LayoutParams(0, LayoutParams.WRAP_CONTENT, 1.0f));

            // Delete single clip button
            TextView delBtn = new TextView(context);
            delBtn.setText("🗑");
            delBtn.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
            delBtn.setGravity(Gravity.CENTER);
            delBtn.setPadding(dpToPx(8), dpToPx(8), dpToPx(8), dpToPx(8));
            delBtn.setClickable(true);
            delBtn.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    ClipboardHelper.deleteClip(context, clipIndex);
                    refreshClipboardList();
                }
            });
            card.addView(delBtn);

            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
            lp.setMargins(0, 0, 0, dpToPx(6));
            mClipsContainer.addView(card, lp);
        }
    }

    // --- TAB 2: NOTEPAD ---

    private LinearLayout mNotesContainer;

    private void buildNotepadView(final Context context) {
        mNotepadLayout = new LinearLayout(context);
        mNotepadLayout.setOrientation(VERTICAL);

        // Subheader with "+ New Note" button
        LinearLayout subHeader = new LinearLayout(context);
        subHeader.setOrientation(HORIZONTAL);
        subHeader.setGravity(Gravity.CENTER_VERTICAL);
        subHeader.setPadding(dpToPx(12), dpToPx(6), dpToPx(12), dpToPx(6));

        TextView infoText = new TextView(context);
        infoText.setText("یادداشت‌ها (لمس = درج فقط متن اصلی):");
        infoText.setTextColor(Color.parseColor("#A0A4B4"));
        infoText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
        subHeader.addView(infoText, new LinearLayout.LayoutParams(0, LayoutParams.WRAP_CONTENT, 1.0f));

        TextView addBtn = new TextView(context);
        addBtn.setText("➕ " + context.getString(R.string.btn_new_note));
        addBtn.setTextColor(Color.WHITE);
        addBtn.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
        addBtn.setTypeface(Typeface.DEFAULT_BOLD);
        addBtn.setPadding(dpToPx(10), dpToPx(5), dpToPx(10), dpToPx(5));
        addBtn.setBackgroundColor(Color.parseColor("#2E5BFF"));
        addBtn.setClickable(true);
        addBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                openNoteEditor(null, "", "");
            }
        });
        subHeader.addView(addBtn);
        mNotepadLayout.addView(subHeader);

        // Scrollable list
        ScrollView scroll = new ScrollView(context);
        mNotesContainer = new LinearLayout(context);
        mNotesContainer.setOrientation(VERTICAL);
        mNotesContainer.setPadding(dpToPx(10), dpToPx(4), dpToPx(10), dpToPx(12));
        scroll.addView(mNotesContainer);

        mNotepadLayout.addView(scroll, new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, 0, 1.0f));
        mContentContainer.addView(mNotepadLayout);
    }

    public void refreshNotepadList() {
        if (mNotesContainer == null) return;
        mNotesContainer.removeAllViews();
        final Context context = getContext();

        List<ClipboardHelper.NoteItem> notes = ClipboardHelper.getNotes(context);
        if (notes.isEmpty()) {
            TextView empty = new TextView(context);
            empty.setText(R.string.empty_notes);
            empty.setTextColor(Color.parseColor("#787B8A"));
            empty.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
            empty.setGravity(Gravity.CENTER);
            empty.setPadding(0, dpToPx(32), 0, dpToPx(32));
            mNotesContainer.addView(empty);
            return;
        }

        for (final ClipboardHelper.NoteItem item : notes) {
            LinearLayout card = new LinearLayout(context);
            card.setOrientation(VERTICAL);
            card.setBackgroundColor(Color.parseColor("#1F2027"));
            card.setPadding(dpToPx(12), dpToPx(10), dpToPx(12), dpToPx(10));
            card.setClickable(true);

            // CRITICAL: Tapping note pastes ONLY item.content! Title is never pasted.
            card.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mListener != null) {
                        mListener.onPasteText(item.content);
                    }
                }
            });

            // Card Header row: Title + Actions (Edit, Delete)
            LinearLayout headerRow = new LinearLayout(context);
            headerRow.setOrientation(HORIZONTAL);
            headerRow.setGravity(Gravity.CENTER_VERTICAL);

            TextView titleTv = new TextView(context);
            titleTv.setText(item.title != null && !item.title.isEmpty() ? item.title : "(بدون عنوان)");
            titleTv.setTextColor(Color.WHITE);
            titleTv.setTypeface(Typeface.DEFAULT_BOLD);
            titleTv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
            headerRow.addView(titleTv, new LinearLayout.LayoutParams(0, LayoutParams.WRAP_CONTENT, 1.0f));

            // Edit button
            TextView editBtn = new TextView(context);
            editBtn.setText("✏️");
            editBtn.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
            editBtn.setPadding(dpToPx(6), dpToPx(4), dpToPx(6), dpToPx(4));
            editBtn.setClickable(true);
            editBtn.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    openNoteEditor(item.id, item.title, item.content);
                }
            });
            headerRow.addView(editBtn);

            // Delete button
            TextView delBtn = new TextView(context);
            delBtn.setText("🗑");
            delBtn.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
            delBtn.setPadding(dpToPx(6), dpToPx(4), dpToPx(6), dpToPx(4));
            delBtn.setClickable(true);
            delBtn.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    ClipboardHelper.deleteNote(context, item.id);
                    refreshNotepadList();
                }
            });
            headerRow.addView(delBtn);
            card.addView(headerRow);

            // Content preview chip
            TextView contentTv = new TextView(context);
            contentTv.setText(item.content);
            contentTv.setTextColor(Color.parseColor("#9EA2B2"));
            contentTv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
            contentTv.setTypeface(Typeface.MONOSPACE);
            contentTv.setBackgroundColor(Color.parseColor("#17181E"));
            contentTv.setPadding(dpToPx(8), dpToPx(6), dpToPx(8), dpToPx(6));
            contentTv.setMaxLines(3);
            contentTv.setEllipsize(TextUtils.TruncateAt.END);

            LinearLayout.LayoutParams contentLp = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
            contentLp.setMargins(0, dpToPx(6), 0, 0);
            card.addView(contentTv, contentLp);

            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
            lp.setMargins(0, 0, 0, dpToPx(6));
            mNotesContainer.addView(card, lp);
        }
    }

    // --- IN-PLACE NOTE EDITOR ---

    private void buildEditorView(final Context context) {
        ScrollView scroll = new ScrollView(context);
        scroll.setBackgroundColor(Color.parseColor("#181920"));

        LinearLayout editor = new LinearLayout(context);
        editor.setOrientation(VERTICAL);
        editor.setPadding(dpToPx(16), dpToPx(12), dpToPx(16), dpToPx(16));

        TextView titleLabel = new TextView(context);
        titleLabel.setText("عنوان یادداشت (فقط برای شناسایی، درج نمی‌شود):");
        titleLabel.setTextColor(Color.parseColor("#A0A4B4"));
        titleLabel.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
        editor.addView(titleLabel);

        mEditNoteTitle = new EditText(context);
        mEditNoteTitle.setHint("مثلا: این کارت برای منه و رمزش 2222");
        mEditNoteTitle.setHintTextColor(Color.parseColor("#5A5D6E"));
        mEditNoteTitle.setTextColor(Color.WHITE);
        mEditNoteTitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
        mEditNoteTitle.setBackgroundColor(Color.parseColor("#22242D"));
        mEditNoteTitle.setPadding(dpToPx(10), dpToPx(8), dpToPx(10), dpToPx(8));
        LinearLayout.LayoutParams titleLp = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        titleLp.setMargins(0, dpToPx(4), 0, dpToPx(12));
        editor.addView(mEditNoteTitle, titleLp);

        TextView contentLabel = new TextView(context);
        contentLabel.setText("متن اصلی (این متن هنگام کلیک پیست می‌شود):");
        contentLabel.setTextColor(Color.parseColor("#A0A4B4"));
        contentLabel.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
        editor.addView(contentLabel);

        mEditNoteContent = new EditText(context);
        mEditNoteContent.setHint("مثلا: 5555555555555555 یا متن پیام آماده");
        mEditNoteContent.setHintTextColor(Color.parseColor("#5A5D6E"));
        mEditNoteContent.setTextColor(Color.WHITE);
        mEditNoteContent.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
        mEditNoteContent.setBackgroundColor(Color.parseColor("#22242D"));
        mEditNoteContent.setPadding(dpToPx(10), dpToPx(8), dpToPx(10), dpToPx(8));
        mEditNoteContent.setMinLines(3);
        mEditNoteContent.setGravity(Gravity.TOP);
        LinearLayout.LayoutParams contentLp = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        contentLp.setMargins(0, dpToPx(4), 0, dpToPx(16));
        editor.addView(mEditNoteContent, contentLp);

        // Buttons row (Save, Cancel)
        LinearLayout btnRow = new LinearLayout(context);
        btnRow.setOrientation(HORIZONTAL);
        btnRow.setGravity(Gravity.END);

        TextView cancelBtn = new TextView(context);
        cancelBtn.setText("انصراف");
        cancelBtn.setTextColor(Color.parseColor("#9FA3B2"));
        cancelBtn.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
        cancelBtn.setBackgroundColor(Color.parseColor("#252731"));
        cancelBtn.setPadding(dpToPx(18), dpToPx(8), dpToPx(18), dpToPx(8));
        cancelBtn.setClickable(true);
        cancelBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mNoteEditorOverlay.setVisibility(GONE);
            }
        });
        btnRow.addView(cancelBtn);

        TextView saveBtn = new TextView(context);
        saveBtn.setText("ذخیره یادداشت");
        saveBtn.setTextColor(Color.WHITE);
        saveBtn.setTypeface(Typeface.DEFAULT_BOLD);
        saveBtn.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
        saveBtn.setBackgroundColor(Color.parseColor("#2E5BFF"));
        saveBtn.setPadding(dpToPx(20), dpToPx(8), dpToPx(20), dpToPx(8));
        saveBtn.setClickable(true);
        saveBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                saveCurrentNote();
            }
        });
        LinearLayout.LayoutParams saveLp = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        saveLp.setMargins(dpToPx(10), 0, 0, 0);
        btnRow.addView(saveBtn, saveLp);

        editor.addView(btnRow);
        scroll.addView(editor);

        mNoteEditorOverlay = scroll;
        mNoteEditorOverlay.setVisibility(GONE);
        mContentContainer.addView(mNoteEditorOverlay, new FrameLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
    }

    private void openNoteEditor(String id, String title, String content) {
        mEditingNoteId = id;
        mEditNoteTitle.setText(title != null ? title : "");
        mEditNoteContent.setText(content != null ? content : "");
        mNoteEditorOverlay.setVisibility(VISIBLE);
        mEditNoteContent.requestFocus();
    }

    private void saveCurrentNote() {
        String title = mEditNoteTitle.getText().toString();
        String content = mEditNoteContent.getText().toString();
        if (content.trim().isEmpty()) {
            Toast.makeText(getContext(), "لطفاً متن اصلی را وارد کنید", Toast.LENGTH_SHORT).show();
            return;
        }

        if (mEditingNoteId == null) {
            ClipboardHelper.addNote(getContext(), title, content);
        } else {
            ClipboardHelper.updateNote(getContext(), mEditingNoteId, title, content);
        }

        mNoteEditorOverlay.setVisibility(GONE);
        refreshNotepadList();
    }

    private int dpToPx(float dp) {
        return (int) (dp * getContext().getResources().getDisplayMetrics().density + 0.5f);
    }
}
