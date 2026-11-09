package org.pocketworkstation.pckeyboard;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class ClipboardHelper {

    private static final String PREF_CLIPBOARD_ITEMS = "pref_clipboard_items_json";
    private static final String PREF_NOTEPAD_ITEMS = "pref_notepad_items_json";
    private static final String PREF_CLIPBOARD_ENABLED = "pref_clipboard_enabled";
    private static final String PREF_CLIPBOARD_SIZE = "pref_clipboard_history_size";

    public static class NoteItem {
        public String id;
        public String title;
        public String content;
        public long timestamp;

        public NoteItem(String id, String title, String content, long timestamp) {
            this.id = id;
            this.title = title;
            this.content = content;
            this.timestamp = timestamp;
        }
    }

    public static boolean isClipboardEnabled(Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return sp.getBoolean(PREF_CLIPBOARD_ENABLED, true);
    }

    public static int getMaxClipboardSize(Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        try {
            return Integer.parseInt(sp.getString(PREF_CLIPBOARD_SIZE, "50"));
        } catch (Exception e) {
            return 50;
        }
    }

    // --- Clipboard History ---

    public static synchronized List<String> getClips(Context context) {
        List<String> list = new ArrayList<String>();
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        String json = sp.getString(PREF_CLIPBOARD_ITEMS, "[]");
        try {
            JSONArray arr = new JSONArray(json);
            for (int i = 0; i < arr.length(); i++) {
                list.add(arr.getString(i));
            }
        } catch (JSONException ignored) {}
        return list;
    }

    public static synchronized void addClip(Context context, String text) {
        if (text == null || text.trim().isEmpty()) return;
        text = text.trim();

        List<String> list = getClips(context);
        // Deduplicate: remove if already present
        list.remove(text);
        // Add to top
        list.add(0, text);

        int maxSize = getMaxClipboardSize(context);
        while (list.size() > maxSize) {
            list.remove(list.size() - 1);
        }

        saveClips(context, list);
    }

    public static synchronized void deleteClip(Context context, int index) {
        List<String> list = getClips(context);
        if (index >= 0 && index < list.size()) {
            list.remove(index);
            saveClips(context, list);
        }
    }

    public static synchronized void clearAllClips(Context context) {
        saveClips(context, new ArrayList<String>());
    }

    private static void saveClips(Context context, List<String> list) {
        JSONArray arr = new JSONArray();
        for (String s : list) {
            arr.put(s);
        }
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putString(PREF_CLIPBOARD_ITEMS, arr.toString())
                .apply();
    }

    // --- Notepad / Quick Notes ---

    public static synchronized List<NoteItem> getNotes(Context context) {
        List<NoteItem> list = new ArrayList<NoteItem>();
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        String json = sp.getString(PREF_NOTEPAD_ITEMS, null);

        if (json == null) {
            // Seed with sample initial notes
            list.add(new NoteItem("1", "شماره کارت بانکی", "6037997512345678", System.currentTimeMillis()));
            list.add(new NoteItem("2", "این کارت برای منه و رمزش 2222", "5555555555555555", System.currentTimeMillis()));
            saveNotes(context, list);
            return list;
        }

        try {
            JSONArray arr = new JSONArray(json);
            for (int i = 0; i < arr.length(); i++) {
                JSONObject obj = arr.getJSONObject(i);
                list.add(new NoteItem(
                        obj.optString("id", String.valueOf(System.currentTimeMillis())),
                        obj.optString("title", ""),
                        obj.optString("content", ""),
                        obj.optLong("timestamp", System.currentTimeMillis())
                ));
            }
        } catch (JSONException ignored) {}
        return list;
    }

    public static synchronized void addNote(Context context, String title, String content) {
        if (content == null || content.trim().isEmpty()) return;
        List<NoteItem> list = getNotes(context);
        String id = String.valueOf(System.currentTimeMillis());
        list.add(0, new NoteItem(id, title != null ? title.trim() : "", content.trim(), System.currentTimeMillis()));
        saveNotes(context, list);
    }

    public static synchronized void updateNote(Context context, String id, String newTitle, String newContent) {
        if (newContent == null || newContent.trim().isEmpty()) return;
        List<NoteItem> list = getNotes(context);
        for (NoteItem item : list) {
            if (item.id.equals(id)) {
                item.title = newTitle != null ? newTitle.trim() : "";
                item.content = newContent.trim();
                item.timestamp = System.currentTimeMillis();
                break;
            }
        }
        saveNotes(context, list);
    }

    public static synchronized void deleteNote(Context context, String id) {
        List<NoteItem> list = getNotes(context);
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).id.equals(id)) {
                list.remove(i);
                break;
            }
        }
        saveNotes(context, list);
    }

    private static void saveNotes(Context context, List<NoteItem> list) {
        JSONArray arr = new JSONArray();
        for (NoteItem item : list) {
            try {
                JSONObject obj = new JSONObject();
                obj.put("id", item.id);
                obj.put("title", item.title);
                obj.put("content", item.content);
                obj.put("timestamp", item.timestamp);
                arr.put(obj);
            } catch (JSONException ignored) {}
        }
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putString(PREF_NOTEPAD_ITEMS, arr.toString())
                .apply();
    }
}
