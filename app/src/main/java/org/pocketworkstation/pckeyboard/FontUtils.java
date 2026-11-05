package org.pocketworkstation.pckeyboard;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.preference.PreferenceManager;
import android.util.Log;
import androidx.core.content.res.ResourcesCompat;
import java.io.File;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class FontUtils {
    private static final String TAG = "FontUtils";
    private static final Map<String, Typeface> sTypefaceCache = new HashMap<String, Typeface>();

    public static synchronized void clearCache() {
        sTypefaceCache.clear();
    }

    public static synchronized Typeface getPersianTypeface(Context context) {
        if (context == null) return Typeface.DEFAULT;

        String fontName = null;
        if (LatinIME.sKeyboardSettings != null && LatinIME.sKeyboardSettings.fontFa != null && !LatinIME.sKeyboardSettings.fontFa.isEmpty()) {
            fontName = LatinIME.sKeyboardSettings.fontFa;
        } else {
            try {
                SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
                fontName = sp.getString("pref_font_fa", "vazir.ttf");
            } catch (Throwable t) {
                fontName = "vazir.ttf";
            }
        }
        if (fontName == null || fontName.trim().isEmpty()) {
            fontName = "vazir.ttf";
        }

        String cacheKey = "fa:" + fontName;
        if (sTypefaceCache.containsKey(cacheKey)) {
            Typeface cached = sTypefaceCache.get(cacheKey);
            if (cached != null) return cached;
        }

        Typeface tf = loadTypeface(context, "fontFA/" + fontName);
        if (tf == null && !fontName.equals("vazir.ttf")) {
            tf = loadTypeface(context, "fontFA/vazir.ttf");
        }
        if (tf == null) {
            try {
                tf = ResourcesCompat.getFont(context, R.font.vazir);
            } catch (Throwable ignored) {}
        }
        if (tf == null) {
            try {
                tf = Typeface.createFromAsset(context.getAssets(), "vazir.ttf");
            } catch (Throwable ignored) {}
        }
        if (tf == null) {
            tf = Typeface.DEFAULT;
        }

        sTypefaceCache.put(cacheKey, tf);
        return tf;
    }

    public static synchronized Typeface getPersianBoldTypeface(Context context) {
        if (context == null) return Typeface.DEFAULT_BOLD;
        String fontName = (LatinIME.sKeyboardSettings != null && LatinIME.sKeyboardSettings.fontFa != null)
                ? LatinIME.sKeyboardSettings.fontFa : "vazir.ttf";
        String cacheKey = "fa_bold:" + fontName;
        if (sTypefaceCache.containsKey(cacheKey)) {
            Typeface cached = sTypefaceCache.get(cacheKey);
            if (cached != null) return cached;
        }

        Typeface base = getPersianTypeface(context);
        Typeface bold = (base != null && base != Typeface.DEFAULT)
                ? Typeface.create(base, Typeface.BOLD) : Typeface.DEFAULT_BOLD;
        sTypefaceCache.put(cacheKey, bold);
        return bold;
    }

    public static synchronized Typeface getEnglishTypeface(Context context) {
        if (context == null) return null;

        String fontName = null;
        if (LatinIME.sKeyboardSettings != null && LatinIME.sKeyboardSettings.fontEn != null) {
            fontName = LatinIME.sKeyboardSettings.fontEn;
        } else {
            try {
                SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
                fontName = sp.getString("pref_font_en", "");
            } catch (Throwable t) {
                fontName = "";
            }
        }
        if (fontName == null || fontName.trim().isEmpty()) {
            return null; // System default
        }

        String cacheKey = "en:" + fontName;
        if (sTypefaceCache.containsKey(cacheKey)) {
            return sTypefaceCache.get(cacheKey);
        }

        Typeface tf = loadTypeface(context, "fontEN/" + fontName);
        sTypefaceCache.put(cacheKey, tf);
        return tf;
    }

    public static synchronized Typeface getEnglishBoldTypeface(Context context) {
        if (context == null) return null;
        Typeface base = getEnglishTypeface(context);
        if (base == null) return null;

        String fontName = (LatinIME.sKeyboardSettings != null && LatinIME.sKeyboardSettings.fontEn != null)
                ? LatinIME.sKeyboardSettings.fontEn : "";
        String cacheKey = "en_bold:" + fontName;
        if (sTypefaceCache.containsKey(cacheKey)) {
            return sTypefaceCache.get(cacheKey);
        }

        Typeface bold = Typeface.create(base, Typeface.BOLD);
        sTypefaceCache.put(cacheKey, bold);
        return bold;
    }

    private static Typeface loadTypeface(Context context, String assetPath) {
        if (context == null || assetPath == null) return null;
        try {
            return Typeface.createFromAsset(context.getAssets(), assetPath);
        } catch (Throwable t) {
            Log.d(TAG, "createFromAsset failed for " + assetPath + ": " + t.getMessage());
        }
        try {
            File f = new File(context.getFilesDir(), assetPath);
            if (f.exists()) {
                return Typeface.createFromFile(f);
            }
        } catch (Throwable ignored) {}
        return null;
    }

    // Backwards compatibility methods
    public static Typeface getVazirTypeface(Context context) {
        return getPersianTypeface(context);
    }

    public static Typeface getVazirBoldTypeface(Context context) {
        return getPersianBoldTypeface(context);
    }

    public static boolean isPersianLocale(Locale locale) {
        if (locale == null) return false;
        String lang = locale.getLanguage();
        if (lang == null) return false;
        return "fa".equalsIgnoreCase(lang) || "pes".equalsIgnoreCase(lang) || "per".equalsIgnoreCase(lang);
    }

    public static boolean containsPersian(CharSequence text) {
        if (text == null) return false;
        final int len = text.length();
        for (int i = 0; i < len; i++) {
            char c = text.charAt(i);
            if ((c >= 0x0600 && c <= 0x06FF) ||
                (c >= 0x0750 && c <= 0x077F) ||
                (c >= 0x08A0 && c <= 0x08FF) ||
                (c >= 0xFB50 && c <= 0xFDFF) ||
                (c >= 0xFE70 && c <= 0xFEFF)) {
                return true;
            }
        }
        return false;
    }
}
