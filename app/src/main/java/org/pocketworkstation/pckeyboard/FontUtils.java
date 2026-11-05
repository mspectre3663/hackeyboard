package org.pocketworkstation.pckeyboard;

import android.content.Context;
import android.graphics.Typeface;
import android.util.Log;
import androidx.core.content.res.ResourcesCompat;
import java.io.File;
import java.util.Locale;

public class FontUtils {
    private static final String TAG = "FontUtils";
    private static Typeface sVazirTypeface = null;
    private static Typeface sVazirBoldTypeface = null;

    public static synchronized Typeface getVazirTypeface(Context context) {
        if (sVazirTypeface != null) {
            return sVazirTypeface;
        }
        if (context == null) {
            return Typeface.DEFAULT;
        }
        try {
            sVazirTypeface = ResourcesCompat.getFont(context, R.font.vazir);
        } catch (Throwable t) {
            Log.w(TAG, "Could not load R.font.vazir", t);
        }
        if (sVazirTypeface == null) {
            try {
                sVazirTypeface = Typeface.createFromAsset(context.getAssets(), "vazir.ttf");
            } catch (Throwable t) {
                Log.w(TAG, "Could not load asset vazir.ttf", t);
            }
        }
        if (sVazirTypeface == null) {
            try {
                File f = new File(context.getFilesDir(), "vazir.ttf");
                if (f.exists()) {
                    sVazirTypeface = Typeface.createFromFile(f);
                }
            } catch (Throwable t) {
                Log.w(TAG, "Could not load file vazir.ttf", t);
            }
        }
        if (sVazirTypeface == null) {
            sVazirTypeface = Typeface.DEFAULT;
        }
        return sVazirTypeface;
    }

    public static synchronized Typeface getVazirBoldTypeface(Context context) {
        if (sVazirBoldTypeface != null) {
            return sVazirBoldTypeface;
        }
        Typeface base = getVazirTypeface(context);
        if (base != null && base != Typeface.DEFAULT) {
            sVazirBoldTypeface = Typeface.create(base, Typeface.BOLD);
        } else {
            sVazirBoldTypeface = Typeface.DEFAULT_BOLD;
        }
        return sVazirBoldTypeface;
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
