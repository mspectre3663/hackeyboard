package org.pocketworkstation.pckeyboard;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.InsetDrawable;
import android.graphics.drawable.StateListDrawable;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ThemeManager {

    public enum ThemeCategory {
        DARK_BALANCED("تاریک و معتدل"),
        LIGHT_BALANCED("روشن و ملایم"),
        NEON("نئونی و خاص"),
        CLASSIC("کلاسیک");

        public final String title;
        ThemeCategory(String title) {
            this.title = title;
        }
    }

    public static class ThemeInfo {
        public final int id;
        public final String englishName;
        public final String persianName;
        public final ThemeCategory category;
        public final int keyboardBgColor;
        public final int keyBgColor;
        public final int keyPressedColor;
        public final int keyActiveColor;
        public final int keyStrokeColor;
        public final float strokeWidthDp;
        public final int keyTextColor;
        public final int keyHintColor;
        public final boolean isLight;
        public final int accentColor;

        public ThemeInfo(int id, String englishName, String persianName, ThemeCategory category,
                         int keyboardBgColor, int keyBgColor, int keyPressedColor,
                         int keyActiveColor, int keyStrokeColor, float strokeWidthDp,
                         int keyTextColor, int keyHintColor, boolean isLight, int accentColor) {
            this.id = id;
            this.englishName = englishName;
            this.persianName = persianName;
            this.category = category;
            this.keyboardBgColor = keyboardBgColor;
            this.keyBgColor = keyBgColor;
            this.keyPressedColor = keyPressedColor;
            this.keyActiveColor = keyActiveColor;
            this.keyStrokeColor = keyStrokeColor;
            this.strokeWidthDp = strokeWidthDp;
            this.keyTextColor = keyTextColor;
            this.keyHintColor = keyHintColor;
            this.isLight = isLight;
            this.accentColor = accentColor;
        }

        public String getDisplayName() {
            return englishName + " (" + persianName + ")";
        }
    }

    private static final Map<Integer, ThemeInfo> THEMES_MAP = new HashMap<>();
    private static final List<ThemeInfo> ALL_THEMES = new ArrayList<>();

    static {
        // --- Classic Themes (0 to 21) ---
        addTheme(new ThemeInfo(0, "ICS Holo", "آی‌سی‌اس کلاسیک", ThemeCategory.CLASSIC,
                0xFF000000, 0xFF222222, 0xFF333333, 0xFF33B5E5, 0x00000000, 0f, 0xFFFFFFFF, 0xFF888888, false, 0xFF33B5E5));
        addTheme(new ThemeInfo(1, "Gingerbread", "نان زنجبیلی", ThemeCategory.CLASSIC,
                0xFF1A1A1A, 0xFF333333, 0xFF444444, 0xFFFF8800, 0x00000000, 0f, 0xFFFFFFFF, 0xFF888888, false, 0xFFFF8800));
        addTheme(new ThemeInfo(2, "Stone Bold", "سنگ مشکی", ThemeCategory.CLASSIC,
                0xFF2A2B2E, 0xFF3E4045, 0xFF52555C, 0xFF00B0FF, 0x00000000, 0f, 0xFFFFFFFF, 0xFFAAAAAA, false, 0xFF00B0FF));
        addTheme(new ThemeInfo(3, "Trans Neon", "نئون ترنس", ThemeCategory.NEON,
                0xFF050811, 0xFF0F1829, 0xFF192A47, 0xFF00F0FF, 0xFF00F0FF, 1.2f, 0xFF00F0FF, 0xFF5080A0, false, 0xFF00F0FF));
        addTheme(new ThemeInfo(4, "Material Dark", "متریال دارک", ThemeCategory.DARK_BALANCED,
                0xFF263238, 0xFF37474F, 0xFF455A64, 0xFF009688, 0x00000000, 0f, 0xFFFFFFFF, 0xFF90A4AE, false, 0xFF009688));
        addTheme(new ThemeInfo(5, "Material Light", "متریال لایت", ThemeCategory.LIGHT_BALANCED,
                0xFFECEFF1, 0xFFFFFFFF, 0xFFCFD8DC, 0xFF009688, 0xFFCFD8DC, 1.0f, 0xFF263238, 0xFF78909C, true, 0xFF009688));
        addTheme(new ThemeInfo(6, "ICS Darker", "آی‌سی‌اس تیره‌تر", ThemeCategory.CLASSIC,
                0xFF0A0A0A, 0xFF1A1A1A, 0xFF2E2E2E, 0xFF33B5E5, 0x00000000, 0f, 0xFFFFFFFF, 0xFF777777, false, 0xFF33B5E5));
        addTheme(new ThemeInfo(7, "Material Black", "متریال مشکی خالص", ThemeCategory.CLASSIC,
                0xFF000000, 0xFF1E1E1E, 0xFF2D2D2D, 0xFF4CAF50, 0x00000000, 0f, 0xFFFFFFFF, 0xFF888888, false, 0xFF4CAF50));
        addTheme(new ThemeInfo(8, "Pure White", "سفید خالص", ThemeCategory.LIGHT_BALANCED,
                0xFFF8F9FA, 0xFFFFFFFF, 0xFFE9ECEF, 0xFF007AFF, 0xFFE2E8F0, 1.0f, 0xFF1A1A1A, 0xFF8E8E93, true, 0xFF007AFF));
        addTheme(new ThemeInfo(9, "Cream White", "کرم روشن", ThemeCategory.LIGHT_BALANCED,
                0xFFFAF8F5, 0xFFFFFFFF, 0xFFEFE9DF, 0xFF8D6E63, 0xFFE8DFD5, 1.0f, 0xFF2D2A26, 0xFF9E9488, true, 0xFF8D6E63));
        addTheme(new ThemeInfo(10, "Nordic Ice", "یخ قطبی", ThemeCategory.LIGHT_BALANCED,
                0xFFF0F4F8, 0xFFFFFFFF, 0xFFDCE6EF, 0xFF2B6CB0, 0xFFD2DFEC, 1.0f, 0xFF1A2A3A, 0xFF7B93A8, true, 0xFF2B6CB0));
        addTheme(new ThemeInfo(11, "Lavender White", "اسطوخودوس روشن", ThemeCategory.LIGHT_BALANCED,
                0xFFF6F2FB, 0xFFFFFFFF, 0xFFE9DFF7, 0xFF6B46C1, 0xFFE2D6F5, 1.0f, 0xFF241442, 0xFF8C73B0, true, 0xFF6B46C1));
        addTheme(new ThemeInfo(12, "Silver Light", "نقره‌ای مات", ThemeCategory.LIGHT_BALANCED,
                0xFFF0F2F5, 0xFFFFFFFF, 0xFFD8DCE3, 0xFF4A5568, 0xFFD2D7E0, 1.0f, 0xFF1A202C, 0xFF718096, true, 0xFF4A5568));
        addTheme(new ThemeInfo(13, "OLED Black", "اولد بلک خالص", ThemeCategory.CLASSIC,
                0xFF000000, 0xFF111111, 0xFF222222, 0xFF3B82F6, 0xFF222222, 1.0f, 0xFFFFFFFF, 0xFF666666, false, 0xFF3B82F6));
        addTheme(new ThemeInfo(14, "Cyber Neon", "سایبر نئون آبی", ThemeCategory.NEON,
                0xFF060B17, 0xFF0C152B, 0xFF162447, 0xFF00F0FF, 0xFF00F0FF, 1.3f, 0xFF00F0FF, 0xFF38BDF8, false, 0xFF00F0FF));
        addTheme(new ThemeInfo(15, "Midnight Navy", "سرمه‌ای نیمه‌شب", ThemeCategory.DARK_BALANCED,
                0xFF0B1329, 0xFF142142, 0xFF1D2E5C, 0xFF38BDF8, 0xFF20325C, 1.0f, 0xFFE2E8F0, 0xFF64748B, false, 0xFF38BDF8));
        addTheme(new ThemeInfo(16, "Carbon Dark", "فیبر کربن دارک", ThemeCategory.DARK_BALANCED,
                0xFF141619, 0xFF202328, 0xFF2D3138, 0xFF60A5FA, 0xFF2E333A, 1.0f, 0xFFF1F5F9, 0xFF64748B, false, 0xFF60A5FA));
        addTheme(new ThemeInfo(17, "Emerald Dark", "زمرد تاریک", ThemeCategory.DARK_BALANCED,
                0xFF091A14, 0xFF112920, 0xFF1A3D30, 0xFF10B981, 0xFF1A3D30, 1.0f, 0xFFD1FAE5, 0xFF34D399, false, 0xFF10B981));
        addTheme(new ThemeInfo(18, "Huawei Y330 White", "هواوی لایت", ThemeCategory.CLASSIC,
                0xFFF0F0F0, 0xFFFFFFFF, 0xFFE0E0E0, 0xFF00796B, 0xFFDCDCDC, 1.0f, 0xFF333333, 0xFF888888, true, 0xFF00796B));
        addTheme(new ThemeInfo(19, "Huawei Y330 Dark", "هواوی دارک", ThemeCategory.CLASSIC,
                0xFF222222, 0xFF333333, 0xFF444444, 0xFF009688, 0x00000000, 0f, 0xFFFFFFFF, 0xFFAAAAAA, false, 0xFF009688));
        addTheme(new ThemeInfo(20, "Galaxy S5 Light", "سامسونگ لایت", ThemeCategory.CLASSIC,
                0xFFF4F4F4, 0xFFFFFFFF, 0xFFE2E2E2, 0xFF00838F, 0xFFDDDDDD, 1.0f, 0xFF222222, 0xFF777777, true, 0xFF00838F));
        addTheme(new ThemeInfo(21, "Galaxy S5 Dark", "سامسونگ دارک", ThemeCategory.CLASSIC,
                0xFF1F1F1F, 0xFF333333, 0xFF484848, 0xFF00ACC1, 0x00000000, 0f, 0xFFFFFFFF, 0xFFAAAAAA, false, 0xFF00ACC1));

        // --- 30 New Distinct Themes (22 to 51) ---
        // 22. Tokyo Night
        addTheme(new ThemeInfo(22, "Tokyo Night", "توکیو نایت", ThemeCategory.DARK_BALANCED,
                0xFF1A1B26, 0xFF24283B, 0xFF2F354F, 0xFF7AA2F7, 0xFF3B4261, 1.0f, 0xFFC0CAF5, 0xFF565F89, false, 0xFF7AA2F7));
        // 23. Dracula Classic
        addTheme(new ThemeInfo(23, "Dracula Classic", "دراکولا کلاسیک", ThemeCategory.DARK_BALANCED,
                0xFF282A36, 0xFF383A59, 0xFF44475A, 0xFFFF79C6, 0xFF6272A4, 1.0f, 0xFFF8F8F2, 0xFF6272A4, false, 0xFFBD93F9));
        // 24. Catppuccin Mocha
        addTheme(new ThemeInfo(24, "Catppuccin Mocha", "کاتپوچین موکا", ThemeCategory.DARK_BALANCED,
                0xFF1E1E2E, 0xFF313244, 0xFF45475A, 0xFFCBA6F7, 0xFF585B70, 0.8f, 0xFFCDD6F4, 0xFF6C7086, false, 0xFFCBA6F7));
        // 25. Catppuccin Latte
        addTheme(new ThemeInfo(25, "Catppuccin Latte", "کاتپوچین لاته", ThemeCategory.LIGHT_BALANCED,
                0xFFEFF1F5, 0xFFE6E9EF, 0xFFCCD0DA, 0xFF1E66F5, 0xFFBCC0CC, 1.0f, 0xFF4C4F69, 0xFF8C8FA1, true, 0xFF1E66F5));
        // 26. Nord Aurora
        addTheme(new ThemeInfo(26, "Nord Aurora", "نورد آرورا", ThemeCategory.DARK_BALANCED,
                0xFF2E3440, 0xFF3B4252, 0xFF434C5E, 0xFF88C0D0, 0xFF4C566A, 1.0f, 0xFFECEFF4, 0xFF81A1C1, false, 0xFF88C0D0));
        // 27. Nord Frost Light
        addTheme(new ThemeInfo(27, "Nord Frost Light", "نورد فراست روشن", ThemeCategory.LIGHT_BALANCED,
                0xFFECEFF4, 0xFFE5E9F0, 0xFFD8DEE9, 0xFF5E81AC, 0xFFC6D1DE, 1.0f, 0xFF2E3440, 0xFF4C566A, true, 0xFF5E81AC));
        // 28. One Dark Pro
        addTheme(new ThemeInfo(28, "One Dark Pro", "وان دارک پرو", ThemeCategory.DARK_BALANCED,
                0xFF21252B, 0xFF282C34, 0xFF353B45, 0xFF61AFEF, 0xFF3E4451, 1.0f, 0xFFABB2BF, 0xFF5C6370, false, 0xFF61AFEF));
        // 29. Monokai Pro
        addTheme(new ThemeInfo(29, "Monokai Pro", "مونوکای پرو", ThemeCategory.DARK_BALANCED,
                0xFF2D2A2E, 0xFF3A363B, 0xFF49454A, 0xFFFF6188, 0xFF5B565C, 1.0f, 0xFFFCFCFA, 0xFF727072, false, 0xFFFFD866));
        // 30. Cyberpunk Neon Yellow
        addTheme(new ThemeInfo(30, "Cyberpunk Neon", "سایبرپانک نئون زرد", ThemeCategory.NEON,
                0xFF151821, 0xFF1F2432, 0xFF2E3547, 0xFFFCEE0A, 0xFFFCEE0A, 1.5f, 0xFFFCEE0A, 0xFF8B94A8, false, 0xFFFCEE0A));
        // 31. Synthwave Neon Pink
        addTheme(new ThemeInfo(31, "Synthwave Neon", "سینث‌ویو صورتی نئونی", ThemeCategory.NEON,
                0xFF1A102A, 0xFF291840, 0xFF3C245C, 0xFFFF2A85, 0xFFFF2A85, 1.5f, 0xFF00F5D4, 0xFFB55FE6, false, 0xFFFF2A85));
        // 32. Matrix Neon Green
        addTheme(new ThemeInfo(32, "Matrix Green", "ماتریکس نئون سبز", ThemeCategory.NEON,
                0xFF0C1510, 0xFF13261A, 0xFF1A3824, 0xFF00FF66, 0xFF00FF66, 1.5f, 0xFF00FF66, 0xFF2F7E4C, false, 0xFF00FF66));
        // 33. Coral Sunset
        addTheme(new ThemeInfo(33, "Coral Sunset", "غروب مرجانی گرم", ThemeCategory.DARK_BALANCED,
                0xFF291B2C, 0xFF3D2642, 0xFF54325A, 0xFFFF7E67, 0xFF683D70, 1.0f, 0xFFFFD166, 0xFF8E6398, false, 0xFFFF7E67));
        // 34. Rose Pine Moon
        addTheme(new ThemeInfo(34, "Rose Pine Moon", "رز پاین مون", ThemeCategory.DARK_BALANCED,
                0xFF232136, 0xFF2A273F, 0xFF393552, 0xFFC4A7E7, 0xFF44415A, 1.0f, 0xFFEA9A97, 0xFF6E6A86, false, 0xFFEBBCBA));
        // 35. Rose Pine Dawn
        addTheme(new ThemeInfo(35, "Rose Pine Dawn", "رز پاین داون", ThemeCategory.LIGHT_BALANCED,
                0xFFFAF4ED, 0xFFF2E9DE, 0xFFE4D7C7, 0xFFB4637A, 0xFFD7C8B6, 1.0f, 0xFF575279, 0xFF9893A5, true, 0xFFB4637A));
        // 36. Matcha Latte
        addTheme(new ThemeInfo(36, "Matcha Latte", "ماچا لاته ملایم", ThemeCategory.LIGHT_BALANCED,
                0xFFE8EDE4, 0xFFD8E2D3, 0xFFC5D4BF, 0xFF3B5E3D, 0xFFB6C7AF, 1.0f, 0xFF2D4A2C, 0xFF6F886C, true, 0xFF3B5E3D));
        // 37. Forest Emerald
        addTheme(new ThemeInfo(37, "Forest Emerald", "زمرد جنگلی دارک", ThemeCategory.DARK_BALANCED,
                0xFF13241B, 0xFF1C3629, 0xFF274B39, 0xFF5AF5B1, 0xFF32614A, 1.0f, 0xFFE1FCF0, 0xFF4E876A, false, 0xFF5AF5B1));
        // 38. Ocean Abyss
        addTheme(new ThemeInfo(38, "Ocean Abyss", "اقیانوس عمیق", ThemeCategory.DARK_BALANCED,
                0xFF0E1F2F, 0xFF18334B, 0xFF23496B, 0xFF38EF7D, 0xFF2D567C, 1.0f, 0xFFD4F1F9, 0xFF4D7C9F, false, 0xFF11998E));
        // 39. Sky Pastel Light
        addTheme(new ThemeInfo(39, "Sky Pastel Light", "آبی پاستلی آسمانی", ThemeCategory.LIGHT_BALANCED,
                0xFFEEF4F9, 0xFFDBE7F3, 0xFFC8DCF0, 0xFF2563EB, 0xFFB7D1EA, 1.0f, 0xFF1E3A5F, 0xFF64748B, true, 0xFF2563EB));
        // 40. Lavender Dream
        addTheme(new ThemeInfo(40, "Lavender Dream", "رویای اسطوخودوس", ThemeCategory.LIGHT_BALANCED,
                0xFFF4EFFB, 0xFFE5D9F5, 0xFFD4C2EE, 0xFF7C3AED, 0xFFC5AFEE, 1.0f, 0xFF4C1D95, 0xFF8B5CF6, true, 0xFF7C3AED));
        // 41. Amethyst Neon
        addTheme(new ThemeInfo(41, "Amethyst Neon", "آمیتیست نئون بنفش", ThemeCategory.NEON,
                0xFF170E24, 0xFF26163B, 0xFF391F59, 0xFFBD00FF, 0xFFBD00FF, 1.5f, 0xFFEBD4FF, 0xFF8B32CC, false, 0xFFBD00FF));
        // 42. Amber Terminal
        addTheme(new ThemeInfo(42, "Amber Terminal", "کهربایی ترمینال", ThemeCategory.DARK_BALANCED,
                0xFF1C1917, 0xFF292524, 0xFF3C3533, 0xFFF59E0B, 0xFF44403C, 1.0f, 0xFFFBBF24, 0xFF78716C, false, 0xFFF59E0B));
        // 43. Coffee Mocha
        addTheme(new ThemeInfo(43, "Coffee Mocha", "قهوه موکا گرم", ThemeCategory.DARK_BALANCED,
                0xFF211915, 0xFF332720, 0xFF46352C, 0xFFD4A373, 0xFF534036, 1.0f, 0xFFFAEDCD, 0xFF856A5C, false, 0xFFD4A373));
        // 44. Vintage Sepia
        addTheme(new ThemeInfo(44, "Vintage Sepia", "کاغذ قدیمی سپیا", ThemeCategory.LIGHT_BALANCED,
                0xFFF7F3E8, 0xFFECE4D2, 0xFFDDD1BA, 0xFF6B4F3B, 0xFFCFC0A3, 1.0f, 0xFF3D2B1F, 0xFF8A7662, true, 0xFF6B4F3B));
        // 45. Electric Cyan Glow
        addTheme(new ThemeInfo(45, "Electric Cyan", "سایان درخشان نئونی", ThemeCategory.NEON,
                0xFF11161D, 0xFF1A232F, 0xFF273546, 0xFF00E5FF, 0xFF00E5FF, 1.5f, 0xFFE0F7FA, 0xFF0097A7, false, 0xFF00E5FF));
        // 46. Crimson Shadow
        addTheme(new ThemeInfo(46, "Crimson Shadow", "زرشکی تاریک سایه", ThemeCategory.DARK_BALANCED,
                0xFF1B1617, 0xFF301F22, 0xFF452A2F, 0xFFFF3366, 0xFF572E36, 1.0f, 0xFFFFE4E8, 0xFF794A53, false, 0xFFFF3366));
        // 47. Spring Mint Light
        addTheme(new ThemeInfo(47, "Spring Mint Light", "نعنایی بهاری روشن", ThemeCategory.LIGHT_BALANCED,
                0xFFEDF7F3, 0xFFD6EEE3, 0xFFBFDED1, 0xFF0D9488, 0xFFA7D1C1, 1.0f, 0xFF134E4A, 0xFF2DD4BF, true, 0xFF0D9488));
        // 48. Twilight Violet
        addTheme(new ThemeInfo(48, "Twilight Violet", "شفق بنفش ملایم", ThemeCategory.DARK_BALANCED,
                0xFF1E1933, 0xFF2D254B, 0xFF3F3366, 0xFFA78BFA, 0xFF4D3F78, 1.0f, 0xFFEDE9FE, 0xFF7C6AAB, false, 0xFFA78BFA));
        // 49. Solarized Dark
        addTheme(new ThemeInfo(49, "Solarized Dark", "سولارایزد دارک", ThemeCategory.DARK_BALANCED,
                0xFF002B36, 0xFF073642, 0xFF0E4A5A, 0xFF2AA198, 0xFF165464, 1.0f, 0xFF93A1A1, 0xFF586E75, false, 0xFF2AA198));
        // 50. Solarized Light
        addTheme(new ThemeInfo(50, "Solarized Light", "سولارایزد لایت", ThemeCategory.LIGHT_BALANCED,
                0xFFFDF6E3, 0xFFEEE8D5, 0xFFDFD6BF, 0xFF268BD2, 0xFFCDC2A7, 1.0f, 0xFF586E75, 0xFF839496, true, 0xFF268BD2));
        // 51. Gruvbox Retro Dark
        addTheme(new ThemeInfo(51, "Gruvbox Retro Dark", "گرووباکس رترو", ThemeCategory.DARK_BALANCED,
                0xFF282828, 0xFF3C3836, 0xFF504945, 0xFFFABD2F, 0xFF665C54, 1.0f, 0xFFEBDBB2, 0xFF928374, false, 0xFFFABD2F));
    }

    private static void addTheme(ThemeInfo theme) {
        THEMES_MAP.put(theme.id, theme);
        ALL_THEMES.add(theme);
    }

    public static List<ThemeInfo> getAllThemes() {
        return ALL_THEMES;
    }

    public static ThemeInfo getTheme(int id) {
        ThemeInfo info = THEMES_MAP.get(id);
        if (info == null) {
            info = THEMES_MAP.get(0);
        }
        return info;
    }

    public static Drawable createKeyBackground(ThemeInfo theme, float density) {
        StateListDrawable sld = new StateListDrawable();

        int radiusPx = (int) (4 * density);
        int strokePx = (int) (theme.strokeWidthDp * density);

        // Pressed State
        GradientDrawable pressed = new GradientDrawable();
        pressed.setShape(GradientDrawable.RECTANGLE);
        pressed.setColor(theme.keyPressedColor);
        pressed.setCornerRadius(radiusPx);
        if (strokePx > 0 && theme.keyStrokeColor != 0) {
            pressed.setStroke(strokePx, theme.keyStrokeColor);
        }

        // Active State (Shift / Checkable checked)
        GradientDrawable active = new GradientDrawable();
        active.setShape(GradientDrawable.RECTANGLE);
        active.setColor(theme.keyActiveColor);
        active.setCornerRadius(radiusPx);
        if (strokePx > 0 && theme.keyStrokeColor != 0) {
            active.setStroke(strokePx, theme.keyStrokeColor);
        }

        // Normal State
        GradientDrawable normal = new GradientDrawable();
        normal.setShape(GradientDrawable.RECTANGLE);
        normal.setColor(theme.keyBgColor);
        normal.setCornerRadius(radiusPx);
        if (strokePx > 0 && theme.keyStrokeColor != 0) {
            normal.setStroke(strokePx, theme.keyStrokeColor);
        }

        int insetPx = (int) (1.5f * density);
        Drawable pressedInset = new InsetDrawable(pressed, insetPx, insetPx, insetPx, insetPx);
        Drawable activeInset = new InsetDrawable(active, insetPx, insetPx, insetPx, insetPx);
        Drawable normalInset = new InsetDrawable(normal, insetPx, insetPx, insetPx, insetPx);

        sld.addState(new int[]{android.R.attr.state_active, android.R.attr.state_pressed}, pressedInset);
        sld.addState(new int[]{android.R.attr.state_active}, activeInset);
        sld.addState(new int[]{android.R.attr.state_checkable, android.R.attr.state_checked, android.R.attr.state_pressed}, pressedInset);
        sld.addState(new int[]{android.R.attr.state_checkable, android.R.attr.state_checked}, activeInset);
        sld.addState(new int[]{android.R.attr.state_checkable, android.R.attr.state_pressed}, pressedInset);
        sld.addState(new int[]{android.R.attr.state_empty, android.R.attr.state_pressed}, pressedInset);
        sld.addState(new int[]{android.R.attr.state_empty}, new ColorDrawable(Color.TRANSPARENT));
        sld.addState(new int[]{android.R.attr.state_pressed}, pressedInset);
        sld.addState(new int[]{}, normalInset);

        return sld;
    }

    public static void applyTheme(LatinKeyboardView view, int themeId) {
        if (view == null) return;
        ThemeInfo theme = getTheme(themeId);
        if (theme == null) return;

        view.setBackgroundColor(theme.keyboardBgColor);
        float density = view.getResources().getDisplayMetrics().density;
        view.setKeyBackground(createKeyBackground(theme, density));
        view.setKeyTextColor(theme.keyTextColor);
        view.setKeyHintColor(theme.keyHintColor);
        view.setInvertSymbols(theme.isLight);
        view.setRecolorSymbols(true);
        view.invalidateAllKeys();
    }

    public static void applyCornerRadius(LatinKeyboardView view) {
        if (view == null) return;
        view.updateKeyCornerRadius();
        view.invalidateAllKeys();
    }

    public static void applyCandidateStripTheme(LatinIME ime, int themeId) {
        if (ime == null) return;
        View container = ime.mCandidateViewContainer;
        if (container == null) return;

        ThemeInfo theme = getTheme(themeId);
        if (theme == null) return;

        // Subtle candidate strip background matching theme
        container.setBackgroundColor(theme.keyboardBgColor);

        // Code symbols button
        TextView codeBtn = (TextView) container.findViewById(R.id.btn_code_symbols);
        if (codeBtn != null) {
            codeBtn.setTextColor(theme.isLight ? 0xFF222222 : 0xFFFFFFFF);
        }

        // Clipboard icon tint
        ImageView clipBtn = (ImageView) container.findViewById(R.id.btn_clipboard);
        if (clipBtn != null) {
            clipBtn.setColorFilter(theme.isLight ? 0xFF333333 : 0xFFFFFFFF);
        }
    }
}
