package org.pocketworkstation.pckeyboard;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CodeSymbolsView extends LinearLayout {

    public interface CodeSymbolsListener {
        void onSymbolSelected(String symbol);
        void onBackToKeyboard();
        void onBackspace();
        void onSpace();
    }

    private static final String[] CATEGORY_TITLES = {
        "پرکاربرد",
        "براکت و تگ",
        "عملگرها",
        "منطقی",
        "سینتکس",
        "کامنت و بلوک"
    };

    private static final String[][] CATEGORY_SYMBOLS = {
        // 0: Common
        {
            "{", "}", "[", "]", "(", ")", "</>", "/>", "=>", "->",
            "==", "!=", "===", "!==", "&&", "||", "!", ";", ":", "\\",
            "/", "$", "_", "\"", "'", "`", "?", "??", "?.", "+",
            "-", "*", "=", "%", "&", "|", "^", "<", ">", "#"
        },
        // 1: Brackets & Tags
        {
            "{", "}", "[", "]", "(", ")", "<", ">", "{}", "[]",
            "()", "<>", "</>", "/>", "<!--", "-->", "<?", "?>", "<script>", "</script>",
            "<div>", "</div>", "<span>", "</span>", "<a>", "</a>", "<p>", "</p>"
        },
        // 2: Operators
        {
            "=", "==", "===", "!=", "!==", "+", "-", "*", "/", "%",
            "++", "--", "+=", "-=", "*=", "/=", "%=", "**", "<=", ">=",
            "<<", ">>", ">>>", "&=", "|=", "^="
        },
        // 3: Logic & Bitwise
        {
            "&&", "||", "!", "&", "|", "^", "~", "?", ":", "??",
            "?.", "null", "undefined", "true", "false", "and", "or", "not"
        },
        // 4: Syntax & Punctuation
        {
            ";", ":", ",", ".", "->", "=>", "::", "\\", "/", "@",
            "#", "$", "%", "_", "\"", "'", "`", "~", "|", "^"
        },
        // 5: Comments & Code Snippets
        {
            "//", "/*", "*/", "/**", "*/", "#", "<!--", "-->", "#!",
            "return", "function", "const", "let", "var", "import", "export"
        }
    };

    private CodeSymbolsListener mListener;
    private int mSelectedCategory = 0;
    private LinearLayout mCategoryContainer;
    private GridView mSymbolsGrid;
    private SymbolsAdapter mAdapter;
    private final List<String> mCurrentSymbols = new ArrayList<String>();

    public CodeSymbolsView(Context context) {
        super(context);
        init(context);
    }

    public CodeSymbolsView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public void setListener(CodeSymbolsListener listener) {
        mListener = listener;
    }

    private void init(Context context) {
        setOrientation(VERTICAL);
        setBackgroundColor(Color.parseColor("#15161B"));

        // 1. Top Bar: Category tabs + Close button
        LinearLayout topBar = new LinearLayout(context);
        topBar.setOrientation(HORIZONTAL);
        topBar.setGravity(Gravity.CENTER_VERTICAL);
        topBar.setBackgroundColor(Color.parseColor("#1C1E26"));
        topBar.setPadding(dpToPx(6), dpToPx(3), dpToPx(6), dpToPx(3));

        // Prefix badge
        TextView badge = new TextView(context);
        badge.setText("</>");
        badge.setTextColor(Color.parseColor("#82AAFF"));
        badge.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
        badge.setTypeface(Typeface.MONOSPACE, Typeface.BOLD);
        badge.setPadding(dpToPx(8), dpToPx(4), dpToPx(8), dpToPx(4));
        topBar.addView(badge);

        // Scrollable category tabs
        HorizontalScrollView scroll = new HorizontalScrollView(context);
        scroll.setHorizontalScrollBarEnabled(false);
        LinearLayout.LayoutParams scrollLp = new LinearLayout.LayoutParams(0, LayoutParams.WRAP_CONTENT, 1.0f);
        mCategoryContainer = new LinearLayout(context);
        mCategoryContainer.setOrientation(HORIZONTAL);
        mCategoryContainer.setGravity(Gravity.CENTER_VERTICAL);

        for (int i = 0; i < CATEGORY_TITLES.length; i++) {
            final int catIdx = i;
            TextView tab = new TextView(context);
            tab.setText(CATEGORY_TITLES[i]);
            tab.setTextColor(Color.parseColor("#B0B4C8"));
            tab.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
            tab.setGravity(Gravity.CENTER);
            int padH = dpToPx(10);
            int padV = dpToPx(6);
            tab.setPadding(padH, padV, padH, padV);
            tab.setClickable(true);
            tab.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    selectCategory(catIdx);
                }
            });
            mCategoryContainer.addView(tab);
        }
        scroll.addView(mCategoryContainer);
        topBar.addView(scroll, scrollLp);

        // Close button (✕)
        TextView closeBtn = new TextView(context);
        closeBtn.setText("✕");
        closeBtn.setTextColor(Color.WHITE);
        closeBtn.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
        closeBtn.setGravity(Gravity.CENTER);
        closeBtn.setBackgroundColor(Color.parseColor("#262835"));
        int pad = dpToPx(6);
        closeBtn.setPadding(pad, pad, pad, pad);
        closeBtn.setClickable(true);
        closeBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mListener != null) {
                    mListener.onBackToKeyboard();
                }
            }
        });
        topBar.addView(closeBtn, new LinearLayout.LayoutParams(dpToPx(34), dpToPx(34)));

        addView(topBar, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));

        // Divider
        View divider = new View(context);
        divider.setBackgroundColor(Color.parseColor("#282A36"));
        addView(divider, new LayoutParams(LayoutParams.MATCH_PARENT, dpToPx(1)));

        // 2. Center Grid of Symbols
        mSymbolsGrid = new GridView(context);
        mSymbolsGrid.setNumColumns(5);
        mSymbolsGrid.setStretchMode(GridView.STRETCH_COLUMN_WIDTH);
        mSymbolsGrid.setGravity(Gravity.CENTER);
        mSymbolsGrid.setVerticalScrollBarEnabled(true);
        mSymbolsGrid.setPadding(dpToPx(6), dpToPx(6), dpToPx(6), dpToPx(6));
        mSymbolsGrid.setHorizontalSpacing(dpToPx(6));
        mSymbolsGrid.setVerticalSpacing(dpToPx(6));

        mAdapter = new SymbolsAdapter();
        mSymbolsGrid.setAdapter(mAdapter);

        LayoutParams gridLp = new LayoutParams(LayoutParams.MATCH_PARENT, 0, 1.0f);
        addView(mSymbolsGrid, gridLp);

        // 3. Bottom Control Bar (Keyboard return, Space, Backspace)
        LinearLayout bottomBar = new LinearLayout(context);
        bottomBar.setOrientation(HORIZONTAL);
        bottomBar.setGravity(Gravity.CENTER_VERTICAL);
        bottomBar.setBackgroundColor(Color.parseColor("#121317"));
        bottomBar.setPadding(dpToPx(8), dpToPx(4), dpToPx(8), dpToPx(6));

        // Return to keyboard button
        TextView kbBtn = new TextView(context);
        kbBtn.setText("⌨️ کیبورد");
        kbBtn.setTextColor(Color.WHITE);
        kbBtn.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
        kbBtn.setGravity(Gravity.CENTER);
        kbBtn.setBackgroundColor(Color.parseColor("#262835"));
        kbBtn.setPadding(dpToPx(14), dpToPx(8), dpToPx(14), dpToPx(8));
        kbBtn.setClickable(true);
        kbBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mListener != null) {
                    mListener.onBackToKeyboard();
                }
            }
        });
        bottomBar.addView(kbBtn);

        // Space button
        TextView spaceBtn = new TextView(context);
        spaceBtn.setText("فاصله");
        spaceBtn.setTextColor(Color.parseColor("#B0B4C8"));
        spaceBtn.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
        spaceBtn.setGravity(Gravity.CENTER);
        spaceBtn.setBackgroundColor(Color.parseColor("#1C1E26"));
        spaceBtn.setPadding(dpToPx(12), dpToPx(8), dpToPx(12), dpToPx(8));
        spaceBtn.setClickable(true);
        spaceBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mListener != null) {
                    mListener.onSpace();
                }
            }
        });
        LinearLayout.LayoutParams spaceLp = new LinearLayout.LayoutParams(0, LayoutParams.WRAP_CONTENT, 1.0f);
        spaceLp.setMargins(dpToPx(8), 0, dpToPx(8), 0);
        bottomBar.addView(spaceBtn, spaceLp);

        // Backspace button
        TextView delBtn = new TextView(context);
        delBtn.setText("⌫");
        delBtn.setTextColor(Color.WHITE);
        delBtn.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
        delBtn.setGravity(Gravity.CENTER);
        delBtn.setBackgroundColor(Color.parseColor("#262835"));
        delBtn.setPadding(dpToPx(16), dpToPx(6), dpToPx(16), dpToPx(6));
        delBtn.setClickable(true);
        delBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mListener != null) {
                    mListener.onBackspace();
                }
            }
        });
        bottomBar.addView(delBtn);

        addView(bottomBar, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));

        selectCategory(0);
    }

    public void selectCategory(int catIndex) {
        mSelectedCategory = catIndex;
        int count = mCategoryContainer.getChildCount();
        for (int i = 0; i < count; i++) {
            TextView tab = (TextView) mCategoryContainer.getChildAt(i);
            if (i == catIndex) {
                tab.setTextColor(Color.WHITE);
                tab.setBackgroundColor(Color.parseColor("#2E5BFF"));
                tab.setTypeface(Typeface.DEFAULT_BOLD);
            } else {
                tab.setTextColor(Color.parseColor("#B0B4C8"));
                tab.setBackgroundColor(Color.TRANSPARENT);
                tab.setTypeface(Typeface.DEFAULT);
            }
        }

        mCurrentSymbols.clear();
        if (catIndex >= 0 && catIndex < CATEGORY_SYMBOLS.length) {
            mCurrentSymbols.addAll(Arrays.asList(CATEGORY_SYMBOLS[catIndex]));
        }
        mAdapter.notifyDataSetChanged();
        mSymbolsGrid.setSelection(0);
    }

    private class SymbolsAdapter extends BaseAdapter {
        @Override
        public int getCount() {
            return mCurrentSymbols.size();
        }

        @Override
        public Object getItem(int position) {
            return mCurrentSymbols.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            TextView tv;
            if (convertView instanceof TextView) {
                tv = (TextView) convertView;
            } else {
                tv = new TextView(getContext());
                tv.setLayoutParams(new android.widget.AbsListView.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT, dpToPx(44)));
                tv.setGravity(Gravity.CENTER);
                tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
                tv.setTypeface(Typeface.MONOSPACE, Typeface.BOLD);
                tv.setBackgroundColor(Color.parseColor("#22242D"));
            }

            if (position >= 0 && position < mCurrentSymbols.size()) {
                final String sym = mCurrentSymbols.get(position);
                tv.setText(sym);

                // Syntax-specific subtle coloring
                if (sym.startsWith("<") || sym.endsWith(">")) {
                    tv.setTextColor(Color.parseColor("#82AAFF"));
                } else if ("{}[]()".contains(sym)) {
                    tv.setTextColor(Color.parseColor("#FFCB6B"));
                } else if ("=+-*/%!&|^~?:".contains(sym) || sym.equals("=>") || sym.equals("->")) {
                    tv.setTextColor(Color.parseColor("#89DDFF"));
                } else {
                    tv.setTextColor(Color.WHITE);
                }

                tv.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (mListener != null) {
                            mListener.onSymbolSelected(sym);
                        }
                    }
                });
            }
            return tv;
        }
    }

    private int dpToPx(float dp) {
        return (int) (dp * getResources().getDisplayMetrics().density + 0.5f);
    }
}
