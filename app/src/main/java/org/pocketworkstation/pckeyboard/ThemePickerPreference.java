package org.pocketworkstation.pckeyboard;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.PreferenceManager;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class ThemePickerPreference extends ListPreference {

    private int mSelectedThemeId = 0;
    private float mKeyCornerRadius = 0f;

    public ThemePickerPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ThemePickerPreference(Context context) {
        super(context);
        init();
    }

    private void init() {
        try {
            String val = getValue();
            if (val != null) {
                mSelectedThemeId = Integer.parseInt(val);
            }
        } catch (Exception ignored) {
            mSelectedThemeId = 0;
        }
        updateSummary();
    }

    private void updateSummary() {
        ThemeManager.ThemeInfo theme = ThemeManager.getTheme(mSelectedThemeId);
        if (theme != null) {
            setSummary(theme.getDisplayName() + " [" + theme.category.title + "]");
        }
    }

    @Override
    protected void onSetInitialValue(boolean restoreValue, Object defaultValue) {
        super.onSetInitialValue(restoreValue, defaultValue);
        try {
            String val = getValue();
            if (val != null) {
                mSelectedThemeId = Integer.parseInt(val);
            }
        } catch (Exception ignored) {
        }
        updateSummary();
    }

    @Override
    protected void showDialog(Bundle state) {
        Context context = getContext();
        float density = context.getResources().getDisplayMetrics().density;

        // Read current selected theme
        try {
            mSelectedThemeId = Integer.parseInt(getValue());
        } catch (Exception e) {
            mSelectedThemeId = 0;
        }

        // Read current corner radius
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        try {
            mKeyCornerRadius = Float.parseFloat(prefs.getString("pref_key_corner_radius", "0"));
        } catch (Exception e) {
            mKeyCornerRadius = 0f;
        }

        final int[] previewThemeId = new int[]{mSelectedThemeId};

        // Root Dialog Layout
        LinearLayout root = new LinearLayout(context);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding((int) (14 * density), (int) (12 * density), (int) (14 * density), (int) (8 * density));
        root.setBackgroundColor(0xFF1E212B);

        // Header Title
        TextView tvHeader = new TextView(context);
        tvHeader.setText("انتخاب تم کیبورد (پیش‌نمایش زنده)");
        tvHeader.setTextColor(0xFFFFFFFF);
        tvHeader.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
        tvHeader.setTypeface(Typeface.DEFAULT_BOLD);
        tvHeader.setGravity(Gravity.CENTER);
        tvHeader.setPadding(0, 0, 0, (int) (8 * density));
        root.addView(tvHeader);

        // --- Live Preview Card ---
        final LinearLayout previewCard = new LinearLayout(context);
        previewCard.setOrientation(LinearLayout.VERTICAL);
        previewCard.setPadding((int) (10 * density), (int) (8 * density), (int) (10 * density), (int) (8 * density));
        GradientDrawable previewCardBg = new GradientDrawable();
        previewCardBg.setColor(0xFF14161F);
        previewCardBg.setCornerRadius(12 * density);
        previewCardBg.setStroke((int) (1 * density), 0xFF353949);
        previewCard.setBackgroundDrawable(previewCardBg);

        final TextView tvThemeTitle = new TextView(context);
        tvThemeTitle.setTextColor(0xFFFFFFFF);
        tvThemeTitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
        tvThemeTitle.setTypeface(Typeface.DEFAULT_BOLD);
        tvThemeTitle.setGravity(Gravity.CENTER);
        previewCard.addView(tvThemeTitle);

        // Candidate strip preview
        final LinearLayout candidateStrip = new LinearLayout(context);
        candidateStrip.setOrientation(LinearLayout.HORIZONTAL);
        candidateStrip.setGravity(Gravity.CENTER_VERTICAL);
        candidateStrip.setPadding((int) (6 * density), (int) (4 * density), (int) (6 * density), (int) (4 * density));
        candidateStrip.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, (int) (28 * density)));
        previewCard.addView(candidateStrip);

        // Keyboard mock container
        final LinearLayout keyboardMock = new LinearLayout(context);
        keyboardMock.setOrientation(LinearLayout.VERTICAL);
        keyboardMock.setPadding((int) (4 * density), (int) (4 * density), (int) (4 * density), (int) (4 * density));
        LinearLayout.LayoutParams kbdParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        kbdParams.topMargin = (int) (4 * density);
        keyboardMock.setLayoutParams(kbdParams);
        previewCard.addView(keyboardMock);

        root.addView(previewCard);

        // --- Category Filter Tabs ---
        HorizontalScrollView hsv = new HorizontalScrollView(context);
        hsv.setHorizontalScrollBarEnabled(false);
        hsv.setPadding(0, (int) (8 * density), 0, (int) (6 * density));

        final LinearLayout categoryTabs = new LinearLayout(context);
        categoryTabs.setOrientation(LinearLayout.HORIZONTAL);
        hsv.addView(categoryTabs);
        root.addView(hsv);

        // --- Theme List ---
        final ListView listView = new ListView(context);
        listView.setDivider(new ColorDrawable(0xFF2C303E));
        listView.setDividerHeight((int) (1 * density));
        listView.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1.0f));
        root.addView(listView);

        // List Filter logic
        final List<ThemeManager.ThemeInfo> filteredList = new ArrayList<>();
        final ThemeManager.ThemeCategory[] selectedCategory = new ThemeManager.ThemeCategory[]{null}; // null = ALL

        final Runnable refreshFilter = new Runnable() {
            @Override
            public void run() {
                filteredList.clear();
                List<ThemeManager.ThemeInfo> all = ThemeManager.getAllThemes();
                for (ThemeManager.ThemeInfo t : all) {
                    if (selectedCategory[0] == null || t.category == selectedCategory[0]) {
                        filteredList.add(t);
                    }
                }
            }
        };
        refreshFilter.run();

        // Helper to update the Live Preview Box
        final Runnable updatePreviewBox = new Runnable() {
            @Override
            public void run() {
                ThemeManager.ThemeInfo theme = ThemeManager.getTheme(previewThemeId[0]);
                if (theme == null) return;

                tvThemeTitle.setText(theme.getDisplayName() + "  •  " + theme.category.title);

                // Update candidate strip
                candidateStrip.setBackgroundColor(theme.keyboardBgColor);
                candidateStrip.removeAllViews();

                TextView codeTag = new TextView(context);
                codeTag.setText("</>");
                codeTag.setTextSize(TypedValue.COMPLEX_UNIT_SP, 10);
                codeTag.setTypeface(Typeface.DEFAULT_BOLD);
                codeTag.setTextColor(theme.isLight ? 0xFF222222 : 0xFFFFFFFF);
                codeTag.setPadding((int) (6 * density), 0, (int) (8 * density), 0);
                candidateStrip.addView(codeTag);

                String[] sampleWords = new String[]{"سلام", "کد", "نوت", "هوشمند"};
                for (int i = 0; i < sampleWords.length; i++) {
                    TextView wordTv = new TextView(context);
                    wordTv.setText(sampleWords[i]);
                    wordTv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 10);
                    wordTv.setTextColor(i == 0 ? theme.accentColor : (theme.isLight ? 0xFF444444 : 0xFFCCCCCC));
                    wordTv.setPadding((int) (6 * density), 0, (int) (6 * density), 0);
                    candidateStrip.addView(wordTv);
                }

                // Update keyboard mock
                GradientDrawable kbdBg = new GradientDrawable();
                kbdBg.setColor(theme.keyboardBgColor);
                kbdBg.setCornerRadius(6 * density);
                keyboardMock.setBackgroundDrawable(kbdBg);
                keyboardMock.removeAllViews();

                // Compute key radius for preview
                float radiusDp;
                if (mKeyCornerRadius >= 99f) {
                    radiusDp = 12f; // full capsule in mini preview
                } else if (mKeyCornerRadius > 0) {
                    radiusDp = mKeyCornerRadius * 0.7f;
                } else {
                    radiusDp = 3f;
                }

                // Row 1: Q W E R T Y U I O P
                String[] row1 = new String[]{"Q", "W", "E", "R", "T", "Y", "U", "I", "O", "P"};
                keyboardMock.addView(createMockKeyRow(context, row1, theme, radiusDp, density, false));

                // Row 2: A S D F G H J K L
                String[] row2 = new String[]{"A", "S", "D", "F", "G", "H", "J", "K", "L"};
                keyboardMock.addView(createMockKeyRow(context, row2, theme, radiusDp, density, false));

                // Row 3: ⇧ Z X C V B N M ⌫
                String[] row3 = new String[]{"⇧", "Z", "X", "C", "V", "B", "N", "M", "⌫"};
                keyboardMock.addView(createMockKeyRow(context, row3, theme, radiusDp, density, true));

                // Row 4: 😊 ?123 [  فاصله  ] ⏎
                String[] row4 = new String[]{"😊", "?123", "فا/EN", "فاصله", "⏎"};
                keyboardMock.addView(createMockKeyRow(context, row4, theme, radiusDp, density, true));
            }
        };

        updatePreviewBox.run();

        // Theme Adapter
        final BaseAdapter adapter = new BaseAdapter() {
            @Override
            public int getCount() {
                return filteredList.size();
            }

            @Override
            public Object getItem(int position) {
                return filteredList.get(position);
            }

            @Override
            public long getItemId(int position) {
                return filteredList.get(position).id;
            }

            @Override
            public View getView(final int position, View convertView, ViewGroup parent) {
                final ThemeManager.ThemeInfo item = filteredList.get(position);
                LinearLayout row;
                if (convertView instanceof LinearLayout) {
                    row = (LinearLayout) convertView;
                } else {
                    row = new LinearLayout(context);
                    row.setOrientation(LinearLayout.HORIZONTAL);
                    row.setGravity(Gravity.CENTER_VERTICAL);
                    row.setPadding((int) (8 * density), (int) (8 * density), (int) (8 * density), (int) (8 * density));
                }

                boolean isSelected = (item.id == previewThemeId[0]);
                row.setBackgroundColor(isSelected ? 0xFF2A3144 : Color.TRANSPARENT);

                row.removeAllViews();

                // Radio Button indicator
                RadioButton rb = new RadioButton(context);
                rb.setChecked(isSelected);
                rb.setClickable(false);
                rb.setFocusable(false);
                row.addView(rb);

                // Text info
                LinearLayout infoCol = new LinearLayout(context);
                infoCol.setOrientation(LinearLayout.VERTICAL);
                infoCol.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1.0f));

                TextView titleTv = new TextView(context);
                titleTv.setText(item.getDisplayName());
                titleTv.setTextColor(isSelected ? 0xFF7AA2F7 : 0xFFFFFFFF);
                titleTv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
                titleTv.setTypeface(isSelected ? Typeface.DEFAULT_BOLD : Typeface.DEFAULT);
                infoCol.addView(titleTv);

                TextView catTv = new TextView(context);
                catTv.setText(item.category.title);
                catTv.setTextColor(0xFF888FA0);
                catTv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 10);
                infoCol.addView(catTv);

                row.addView(infoCol);

                // Color swatches (4 circles)
                LinearLayout swatches = new LinearLayout(context);
                swatches.setOrientation(LinearLayout.HORIZONTAL);
                swatches.setGravity(Gravity.CENTER_VERTICAL);

                swatches.addView(createColorDot(context, item.keyboardBgColor, density));
                swatches.addView(createColorDot(context, item.keyBgColor, density));
                swatches.addView(createColorDot(context, item.keyTextColor, density));
                swatches.addView(createColorDot(context, item.accentColor, density));

                row.addView(swatches);

                row.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        previewThemeId[0] = item.id;
                        notifyDataSetChanged();
                        updatePreviewBox.run();
                    }
                });

                return row;
            }
        };

        listView.setAdapter(adapter);

        // Populate Category Filter Chips
        String[] catTitles = new String[]{"همه (۵۲)", "تاریک و معتدل", "روشن و ملایم", "نئونی و خاص", "کلاسیک"};
        final ThemeManager.ThemeCategory[] catEnums = new ThemeManager.ThemeCategory[]{
                null,
                ThemeManager.ThemeCategory.DARK_BALANCED,
                ThemeManager.ThemeCategory.LIGHT_BALANCED,
                ThemeManager.ThemeCategory.NEON,
                ThemeManager.ThemeCategory.CLASSIC
        };

        for (int i = 0; i < catTitles.length; i++) {
            final int index = i;
            final TextView tab = new TextView(context);
            tab.setText(catTitles[i]);
            tab.setTextSize(TypedValue.COMPLEX_UNIT_SP, 11);
            tab.setPadding((int) (10 * density), (int) (4 * density), (int) (10 * density), (int) (4 * density));
            LinearLayout.LayoutParams tabParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            tabParams.rightMargin = (int) (6 * density);
            tab.setLayoutParams(tabParams);

            boolean active = (selectedCategory[0] == catEnums[i]);
            updateChipStyle(tab, active, density);

            tab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    selectedCategory[0] = catEnums[index];
                    refreshFilter.run();
                    adapter.notifyDataSetChanged();
                    // update chip styles
                    for (int j = 0; j < categoryTabs.getChildCount(); j++) {
                        View child = categoryTabs.getChildAt(j);
                        if (child instanceof TextView) {
                            updateChipStyle((TextView) child, j == index, density);
                        }
                    }
                }
            });
            categoryTabs.addView(tab);
        }

        // Scroll to current selected theme in list
        for (int i = 0; i < filteredList.size(); i++) {
            if (filteredList.get(i).id == previewThemeId[0]) {
                listView.setSelection(i);
                break;
            }
        }

        // Build Dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setView(root);
        builder.setPositiveButton("انتخاب و اعمال تم", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                mSelectedThemeId = previewThemeId[0];
                String valStr = String.valueOf(mSelectedThemeId);
                if (callChangeListener(valStr)) {
                    setValue(valStr);
                    updateSummary();
                }
            }
        });
        builder.setNegativeButton("انصراف", null);

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private static void updateChipStyle(TextView tab, boolean active, float density) {
        GradientDrawable gd = new GradientDrawable();
        gd.setCornerRadius(14 * density);
        if (active) {
            gd.setColor(0xFF2E5BFF);
            tab.setTextColor(0xFFFFFFFF);
            tab.setTypeface(Typeface.DEFAULT_BOLD);
        } else {
            gd.setColor(0xFF252936);
            gd.setStroke((int) (1 * density), 0xFF383E52);
            tab.setTextColor(0xFFA0A6B8);
            tab.setTypeface(Typeface.DEFAULT);
        }
        tab.setBackgroundDrawable(gd);
    }

    private static View createColorDot(Context context, int color, float density) {
        View dot = new View(context);
        int size = (int) (11 * density);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(size, size);
        lp.leftMargin = (int) (3 * density);
        dot.setLayoutParams(lp);

        GradientDrawable gd = new GradientDrawable();
        gd.setShape(GradientDrawable.OVAL);
        gd.setColor(color);
        gd.setStroke((int) (1 * density), 0xFF4A4E5D);
        dot.setBackgroundDrawable(gd);
        return dot;
    }

    private static LinearLayout createMockKeyRow(Context context, String[] labels, ThemeManager.ThemeInfo theme,
                                                 float radiusDp, float density, boolean hasSpecialKeys) {
        LinearLayout row = new LinearLayout(context);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, (int) (22 * density)));
        row.setPadding(0, (int) (1 * density), 0, (int) (1 * density));

        for (String label : labels) {
            TextView keyTv = new TextView(context);
            keyTv.setText(label);
            keyTv.setGravity(Gravity.CENTER);
            keyTv.setTextSize(TypedValue.COMPLEX_UNIT_SP, label.length() > 2 ? 8 : 9);
            keyTv.setTextColor(theme.keyTextColor);

            LinearLayout.LayoutParams lp;
            if (label.equals("فاصله")) {
                lp = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 3.5f);
            } else if (label.equals("⇧") || label.equals("⌫") || label.equals("⏎") || label.equals("?123") || label.equals("فا/EN")) {
                lp = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 1.4f);
            } else {
                lp = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 1.0f);
            }
            lp.leftMargin = (int) (1 * density);
            lp.rightMargin = (int) (1 * density);
            keyTv.setLayoutParams(lp);

            GradientDrawable keyBg = new GradientDrawable();
            keyBg.setShape(GradientDrawable.RECTANGLE);
            keyBg.setCornerRadius(radiusDp * density);

            if (label.equals("⏎")) {
                keyBg.setColor(theme.accentColor);
                keyTv.setTextColor(0xFFFFFFFF);
            } else if (label.equals("⇧") || label.equals("⌫")) {
                keyBg.setColor(theme.keyPressedColor);
            } else {
                keyBg.setColor(theme.keyBgColor);
            }

            if (theme.strokeWidthDp > 0 && theme.keyStrokeColor != 0) {
                keyBg.setStroke((int) (Math.max(1, theme.strokeWidthDp * density * 0.7f)), theme.keyStrokeColor);
            }

            keyTv.setBackgroundDrawable(keyBg);
            row.addView(keyTv);
        }
        return row;
    }
}
