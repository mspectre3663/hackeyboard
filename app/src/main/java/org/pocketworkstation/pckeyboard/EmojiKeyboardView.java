package org.pocketworkstation.pckeyboard;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.preference.PreferenceManager;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class EmojiKeyboardView extends LinearLayout {

    public interface EmojiEventListener {
        void onEmojiSelected(String emoji);
        void onBackToKeyboard();
        void onBackspace();
        void onSpace();
    }

    private static final String PREF_RECENT_EMOJIS = "pref_recent_emojis";
    private static final int MAX_RECENTS = 45;

    // Categories
    private static final String[] CATEGORY_ICONS = {
        "🕒", "😀", "👋", "🐶", "🍔", "🚗", "⚽", "💡", "🔣", "🏁"
    };

    private static final String[][] CATEGORY_EMOJIS = {
        // 0: Recent (dynamically populated)
        {},
        // 1: Smileys & Emotion
        {
            "😀", "😃", "😄", "😁", "😆", "😅", "😂", "🤣", "🥲", "🥹",
            "😊", "😇", "🙂", "🙃", "😉", "😌", "😍", "🥰", "😘", "😗",
            "😙", "😚", "😋", "😛", "😝", "😜", "🤪", "🤨", "🧐", "🤓",
            "😎", "🥸", "🤩", "🥳", "😏", "😒", "😞", "😔", "😟", "😕",
            "🙁", "☹️", "😣", "😖", "😫", "😩", "🥺", "😢", "😭", "😮‍💨",
            "😤", "😠", "😡", "🤬", "🤯", "😳", "🥵", "🥶", "😱", "😨",
            "😰", "😥", "😓", "🫣", "🤗", "🫡", "🤔", "🫢", "🤫", "🤥",
            "😶", "😐", "😑", "😬", "🫨", "🫠", "🙄", "😯", "😦", "😧",
            "😮", "😲", "🥱", "😴", "🤤", "😪", "😵", "😵‍💫", "🤐", "🥴",
            "🤢", "🤮", "🤧", "😷", "🤒", "🤕", "🤑", "🤠", "😈", "👿",
            "👹", "👺", "🤡", "💩", "👻", "💀", "☠️", "👽", "👾", "🤖",
            "🎃", "😺", "😸", "😹", "😻", "😼", "😽", "🙀", "😿", "😾"
        },
        // 2: People & Hands
        {
            "👋", "🤚", "🖐️", "✋", "🖖", "🫱", "🫲", "🫳", "🫴", "👌",
            "🤌", "🤏", "✌️", "🤞", "🫰", "🤟", "🤘", "🤙", "👈", "👉",
            "👆", "🖕", "👇", "☝️", "🫵", "👍", "👎", "✊", "👊", "🤛",
            "🤜", "👏", "🙌", "🫶", "👐", "🤲", "🤝", "🙏", "✍️", "💅",
            "🤳", "💪", "🦾", "🦿", "🦵", "🦶", "👂", "🦻", "👃", "🫀",
            "🫁", "🧠", "🦷", "🦴", "👀", "👁️", "👅", "👄", "🫦", "👶",
            "👧", "🧒", "👦", "👩", "🧑", "👨", "👩‍🦱", "🧑‍🦱", "👨‍🦱", "👩‍🦰",
            "🧑‍🦰", "👨‍🦰", "👱‍♀️", "👱", "👱‍♂️", "👩‍🦳", "🧑‍🦳", "👨‍🦳", "👩‍🦲", "🧑‍🦲",
            "👨‍🦲", "🧔‍♀️", "🧔", "🧔‍♂️", "👵", "🧓", "👴", "👲", "👳‍♀️", "👳"
        },
        // 3: Animals & Nature
        {
            "🐶", "🐱", "🐭", "🐹", "🐰", "🦊", "🐻", "🐼", "🐻‍❄️", "🐨",
            "🐯", "🦁", "🐮", "🐷", "🐽", "🐸", "🐵", "🙈", "🙉", "🙊",
            "🐒", "🐔", "🐧", "🐦", "🐤", "🐣", "🐥", "🦆", "🦅", "🦉",
            "🦇", "🐺", "🐗", "🐴", "🦄", "🐝", "🪱", "🐛", "🦋", "🐌",
            "🐞", "🐜", "🪰", "🪲", "🪳", "🦟", "🦗", "🕷️", "🕸️", "🦂",
            "🐢", "🐍", "🦎", "🦖", "🦕", "🐙", "🦑", "🦐", "🦞", "🦀",
            "🐡", "🐠", "🐟", "🐬", "🐳", "🐋", "🦈", "🦭", "🐊", "🐅",
            "🐆", "🦓", "🦍", "🦧", "🦣", "🐘", "🦛", "🦏", "🐪", "🐫",
            "🦒", "🦘", "🦬", "🐃", "🐂", "🐄", "🐎", "🐖", "🐏", "🐑",
            "🦙", "🐐", "🦌", "🐕", "🐩", "🦮", "🐕‍🦺", "🐈", "🐈‍⬛", "🪶",
            "🌲", "🌳", "🌴", "🌵", "🌾", "🌿", "☘️", "🍀", "🍁", "🍂",
            "🍃", "🪴", "🎍", "🎋", "🌱", "🌺", "🌸", "🌷", "🌹", "🌻"
        },
        // 4: Food & Drink
        {
            "🍏", "🍎", "🍐", "🍊", "🍋", "🍌", "🍉", "🍇", "🍓", "🫐",
            "🍈", "🍒", "🍑", "🥭", "🍍", "🥥", "🥝", "🍅", "🍆", "🥑",
            "🥦", "🥬", "🥒", "🌶️", "🫑", "🌽", "🥕", "🫒", "🧄", "🧅",
            "🥔", "🍠", "🥐", "🥯", "🍞", "🥖", "🥨", "🧀", "🥚", "🍳",
            "🧈", "🥞", "🧇", "🥓", "🥩", "🍗", "🍖", "🦴", "🌭", "🍔",
            "🍟", "🍕", "🫓", "🥪", "🥙", "🧆", "🌮", "🌯", "🫔", "🥗",
            "🥘", "🫕", "🥫", "🍝", "🍜", "🍲", "🍛", "🍣", "🍱", "🥟",
            "🦪", "🍤", "🍙", "🍚", "🍘", "🍢", "🥠", "🥮", "🍡", "🍧",
            "🍨", "🍦", "🥧", "🧁", "🍰", "🎂", "🍮", "🍭", "🍬", "🍫",
            "🍿", "🍩", "🍪", "🌰", "🥜", "🍯", "🥛", "🍼", "🫖", "☕",
            "🧃", "🥤", "🧋", "🍶", "🍺", "🍻", "🥂", "🍷", "🥃", "🍸"
        },
        // 5: Travel & Places
        {
            "🚗", "🚕", "🚙", "🚌", "🚎", "🏎️", "🚓", "🚑", "🚒", "🚐",
            "🛻", "🚚", "🚛", "🚜", "🦯", "🦽", "🦼", "🛴", "🚲", "🛵",
            "🏍️", "🛺", "🚨", "🚔", "🚍", "🚘", "🚖", "🚡", "🚠", "🚟",
            "🚃", "🚋", "🚞", "🚝", "🚄", "🚅", "🚈", "🚂", "🚆", "🚇",
            "🚊", "🚉", "✈️", "🛫", "🛬", "🛩️", "💺", "🛰️", "🚀", "🛸",
            "🚁", "🛶", "⛵", "🚤", "🛥️", "🛳️", "⛴️", "🚢", "⚓", "🛟",
            "⛽", "🚧", "🚦", "🚥", "🗺️", "🗿", "🗽", "🗼", "🏰", "🏯",
            "🏟️", "🎡", "🎢", "🎠", "⛲", "⛱️", "🏖️", "🏝️", "🏜️", "🌋",
            "⛰️", "🏔️", "🗻", "🏕️", "⛺", "🛖", "🏠", "🏡", "🏘️", "🏚️"
        },
        // 6: Activities & Sports
        {
            "⚽", "🏀", "🏈", "⚾", "🥎", "🎾", "🏐", "🏉", "🥏", "🎱",
            "🪀", "🏓", "🏸", "🏒", "🏑", "🥍", "🏏", "🪃", "🥅", "⛳",
            "🪁", "🏹", "🎣", "🤿", "🥊", "🥋", "🎽", "🛹", "🛼", "🛷",
            "⛸️", "🥌", "🎿", "⛷️", "🏂", "🪂", "🏋️‍♀️", "🏋️", "🏋️‍♂️", "🤼‍♀️",
            "🤼", "🤼‍♂️", "🤸‍♀️", "🤸", "🤸‍♂️", "⛹️‍♀️", "⛹️", "⛹️‍♂️", "🤺", "🤾‍♀️",
            "🏊‍♀️", "🏊", "🏊‍♂️", "🚴‍♀️", "🚴", "🚴‍♂️", "🏆", "🥇", "🥈", "🥉",
            "🏅", "🎖️", "🏵️", "🎗️", "🎫", "🎟️", "🎪", "🤹", "🎭", "🎨",
            "🎬", "🎤", "🎧", "🎼", "🎹", "🥁", "🪘", "🎷", "🎺", "🪗"
        },
        // 7: Objects
        {
            "💡", "🔦", "🕯️", "🪔", "🧯", "🛢️", "💸", "💵", "💴", "💶",
            "💷", "🪙", "💰", "💳", "💎", "⚖️", "🪜", "🧰", "🪛", "🔧",
            "🔨", "⚒️", "🛠️", "⛏️", "🪚", "🔩", "⚙️", "🪤", "🧱", "⛓️",
            "🧲", "🔫", "💣", "🧨", "🪓", "🔪", "🗡️", "⚔️", "🛡️", "🚬",
            "⚰️", "🪦", "⚱️", "🏺", "🔮", "📿", "🧿", "🪬", "💈", "🔭",
            "🔬", "🩻", "🩹", "🩺", "💊", "💉", "🩸", "🧬", "🦠", "🧫",
            "🧪", "🌡️", "🧹", "🪠", "🧺", "🧻", "🚽", "🚰", "🚿", "🛁",
            "🛀", "🧼", "🪥", "🪒", "🧽", "🪣", "🧴", "🛎️", "🔑", "🗝️",
            "🚪", "🪑", "🛋️", "🛏️", "🛌", "🧸", "🪆", "🖼️", "🪞", "🪟"
        },
        // 8: Symbols
        {
            "❤️", "🧡", "💛", "💚", "💙", "💜", "🖤", "🤍", "🤎", "💔",
            "❤️‍🔥", "❤️‍🩹", "❣️", "💕", "💞", "💓", "💗", "💖", "💘", "💝",
            "💟", "☮️", "✝️", "☪️", "🕉️", "☸️", "✡️", "🔯", "🕎", "☯️",
            "☦️", "🛐", "⛎", "♈", "♉", "♊", "♋", "♌", "♍", "♎",
            "♏", "♐", "♑", "♒", "♓", "🆔", "⚛️", "🉑", "☢️", "☣️",
            "📴", "📳", "🈶", "🈚", "🈸", "🈺", "🈷️", "✴️", "♨️", "🛑",
            "🕛", "🕧", "🕐", "🕜", "🕑", "🕝", "🕒", "🕞", "🕓", "🕟",
            "🕔", "🕠", "🕕", "🕡", "🕖", "🕢", "🕗", "🕣", "🕘", "🕤",
            "🕙", "🕥", "🕚", "🕦", "💯", "💢", "💬", "👁️‍🗨️", "🗯️", "💭",
            "💤", "⚠️", "🚸", "⛔", "🚫", "🚳", "🚭", "🚯", "🚱", "🚷",
            "📵", "🔞", "🔙", "🔚", "🔛", "🔜", "🔝", "🔄", "▶️", "⏸️"
        },
        // 9: Flags
        {
            "🏁", "🚩", "🎌", "🏴", "🏳️", "🏳️‍🌈", "🏳️‍⚧️", "🏴‍☠️", "🇮🇷", "🇺🇸",
            "🇬🇧", "🇨🇦", "🇩🇪", "🇫🇷", "🇮🇹", "🇪🇸", "🇷🇺", "🇨🇳", "🇯🇵", "🇰🇷",
            "🇮🇳", "🇧🇷", "🇦🇺", "🇹🇷", "🇸🇦", "🇦🇪", "🇮🇶", "🇸🇾", "🇱🇧", "🇯🇴",
            "🇪🇬", "🇶🇦", "🇰🇼", "🇧🇭", "🇴🇲", "🇵🇰", "🇦🇫", "🇹🇯", "🇦🇿", "🇦🇲",
            "🇬🇪", "🇺🇦", "🇸🇪", "🇳🇴", "🇩🇰", "🇫🇮", "🇳🇱", "🇧🇪", "🇨🇭", "🇦🇹"
        }
    };

    private EmojiEventListener mListener;
    private int mSelectedCategory = 1; // Default to Smileys
    private final List<String> mRecentEmojis = new ArrayList<String>();

    private LinearLayout mCategoryContainer;
    private GridView mEmojiGrid;
    private EmojiAdapter mAdapter;
    private List<String> mCurrentEmojiList = new ArrayList<String>();

    public EmojiKeyboardView(Context context) {
        super(context);
        init(context);
    }

    public EmojiKeyboardView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public void setListener(EmojiEventListener listener) {
        mListener = listener;
    }

    private void init(Context context) {
        setOrientation(VERTICAL);
        setBackgroundColor(Color.parseColor("#1B1C20")); // Modern dark charcoal
        loadRecentEmojis();

        // 1. Top Category Bar
        HorizontalScrollView scroll = new HorizontalScrollView(context);
        scroll.setHorizontalScrollBarEnabled(false);
        scroll.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));

        mCategoryContainer = new LinearLayout(context);
        mCategoryContainer.setOrientation(HORIZONTAL);
        mCategoryContainer.setGravity(Gravity.CENTER_VERTICAL);
        mCategoryContainer.setPadding(dpToPx(4), dpToPx(4), dpToPx(4), dpToPx(4));

        for (int i = 0; i < CATEGORY_ICONS.length; i++) {
            final int catIndex = i;
            TextView tab = new TextView(context);
            tab.setText(CATEGORY_ICONS[i]);
            tab.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
            tab.setGravity(Gravity.CENTER);
            int pad = dpToPx(8);
            tab.setPadding(pad, pad, pad, pad);
            tab.setClickable(true);
            tab.setFocusable(true);
            tab.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    selectCategory(catIndex);
                }
            });
            mCategoryContainer.addView(tab, new LayoutParams(dpToPx(42), dpToPx(42)));
        }
        scroll.addView(mCategoryContainer);
        addView(scroll);

        // Divider
        View divider = new View(context);
        divider.setBackgroundColor(Color.parseColor("#2C2D35"));
        addView(divider, new LayoutParams(LayoutParams.MATCH_PARENT, dpToPx(1)));

        // 2. Center Emoji Grid
        mEmojiGrid = new GridView(context);
        mEmojiGrid.setNumColumns(7);
        mEmojiGrid.setStretchMode(GridView.STRETCH_COLUMN_WIDTH);
        mEmojiGrid.setGravity(Gravity.CENTER);
        mEmojiGrid.setVerticalScrollBarEnabled(true);
        mEmojiGrid.setPadding(dpToPx(4), dpToPx(4), dpToPx(4), dpToPx(4));

        mAdapter = new EmojiAdapter();
        mEmojiGrid.setAdapter(mAdapter);

        LayoutParams gridLp = new LayoutParams(LayoutParams.MATCH_PARENT, 0, 1.0f);
        addView(mEmojiGrid, gridLp);

        // 3. Bottom Control Bar (ABC, Space, Backspace)
        LinearLayout bottomBar = new LinearLayout(context);
        bottomBar.setOrientation(HORIZONTAL);
        bottomBar.setGravity(Gravity.CENTER_VERTICAL);
        bottomBar.setBackgroundColor(Color.parseColor("#151619"));
        bottomBar.setPadding(dpToPx(8), dpToPx(4), dpToPx(8), dpToPx(6));

        // ABC Button
        TextView abcBtn = new TextView(context);
        abcBtn.setText("ABC");
        abcBtn.setTextColor(Color.WHITE);
        abcBtn.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
        abcBtn.setGravity(Gravity.CENTER);
        abcBtn.setBackgroundColor(Color.parseColor("#2A2B33"));
        int padH = dpToPx(16);
        int padV = dpToPx(8);
        abcBtn.setPadding(padH, padV, padH, padV);
        abcBtn.setClickable(true);
        abcBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mListener != null) {
                    mListener.onBackToKeyboard();
                }
            }
        });
        LayoutParams abcLp = new LayoutParams(LayoutParams.WRAP_CONTENT, dpToPx(40));
        bottomBar.addView(abcBtn, abcLp);

        // Space Button
        TextView spaceBtn = new TextView(context);
        spaceBtn.setText("Space");
        spaceBtn.setTextColor(Color.parseColor("#9E9E9E"));
        spaceBtn.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
        spaceBtn.setGravity(Gravity.CENTER);
        spaceBtn.setBackgroundColor(Color.parseColor("#222329"));
        spaceBtn.setClickable(true);
        spaceBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mListener != null) {
                    mListener.onSpace();
                }
            }
        });
        LayoutParams spaceLp = new LayoutParams(0, dpToPx(40), 1.0f);
        spaceLp.setMargins(dpToPx(10), 0, dpToPx(10), 0);
        bottomBar.addView(spaceBtn, spaceLp);

        // Backspace Button
        TextView backspaceBtn = new TextView(context);
        backspaceBtn.setText("⌫");
        backspaceBtn.setTextColor(Color.WHITE);
        backspaceBtn.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
        backspaceBtn.setGravity(Gravity.CENTER);
        backspaceBtn.setBackgroundColor(Color.parseColor("#2A2B33"));
        backspaceBtn.setPadding(padH, padV, padH, padV);
        backspaceBtn.setClickable(true);
        backspaceBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mListener != null) {
                    mListener.onBackspace();
                }
            }
        });
        LayoutParams delLp = new LayoutParams(LayoutParams.WRAP_CONTENT, dpToPx(40));
        bottomBar.addView(backspaceBtn, delLp);

        addView(bottomBar, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));

        selectCategory(mSelectedCategory);
    }

    public void selectCategory(int catIndex) {
        mSelectedCategory = catIndex;
        // Update category tab backgrounds
        int childCount = mCategoryContainer.getChildCount();
        for (int i = 0; i < childCount; i++) {
            View child = mCategoryContainer.getChildAt(i);
            if (i == catIndex) {
                child.setBackgroundColor(Color.parseColor("#3B3D4A"));
            } else {
                child.setBackgroundColor(Color.TRANSPARENT);
            }
        }

        mCurrentEmojiList.clear();
        if (catIndex == 0) {
            // Recents
            mCurrentEmojiList.addAll(mRecentEmojis);
        } else if (catIndex >= 0 && catIndex < CATEGORY_EMOJIS.length) {
            mCurrentEmojiList.addAll(Arrays.asList(CATEGORY_EMOJIS[catIndex]));
        }
        mAdapter.notifyDataSetChanged();
        mEmojiGrid.setSelection(0);
    }

    private void onEmojiClicked(String emoji) {
        addRecentEmoji(emoji);
        if (mListener != null) {
            mListener.onEmojiSelected(emoji);
        }
    }

    private void loadRecentEmojis() {
        mRecentEmojis.clear();
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getContext());
        String saved = sp.getString(PREF_RECENT_EMOJIS, "😀,😂,❤️,👍,🙏,🔥,😍,🎉,✨,😊,👏,🙌,🥺,😎,🤔");
        if (saved != null && !saved.isEmpty()) {
            String[] split = saved.split(",");
            for (String s : split) {
                String trimmed = s.trim();
                if (!trimmed.isEmpty() && !mRecentEmojis.contains(trimmed)) {
                    mRecentEmojis.add(trimmed);
                }
            }
        }
    }

    private void addRecentEmoji(String emoji) {
        mRecentEmojis.remove(emoji);
        mRecentEmojis.add(0, emoji);
        while (mRecentEmojis.size() > MAX_RECENTS) {
            mRecentEmojis.remove(mRecentEmojis.size() - 1);
        }
        // Save to SharedPreferences
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < mRecentEmojis.size(); i++) {
            if (i > 0) sb.append(",");
            sb.append(mRecentEmojis.get(i));
        }
        PreferenceManager.getDefaultSharedPreferences(getContext())
                .edit()
                .putString(PREF_RECENT_EMOJIS, sb.toString())
                .apply();
    }

    private class EmojiAdapter extends BaseAdapter {
        @Override
        public int getCount() {
            return mCurrentEmojiList.size();
        }

        @Override
        public Object getItem(int position) {
            return mCurrentEmojiList.get(position);
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
                tv.setLayoutParams(new GridView.LayoutParams(LayoutParams.MATCH_PARENT, dpToPx(48)));
                tv.setGravity(Gravity.CENTER);
                tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 26);
            }
            final String emoji = mCurrentEmojiList.get(position);
            tv.setText(emoji);
            tv.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    onEmojiClicked(emoji);
                }
            });
            return tv;
        }
    }

    private int dpToPx(float dp) {
        return (int) (dp * getContext().getResources().getDisplayMetrics().density + 0.5f);
    }
}
