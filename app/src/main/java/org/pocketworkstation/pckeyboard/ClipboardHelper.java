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

    public static class ClipItem {
        public String text;
        public boolean isPinned;
        public long timestamp;

        public ClipItem(String text, boolean isPinned, long timestamp) {
            this.text = text;
            this.isPinned = isPinned;
            this.timestamp = timestamp;
        }
    }

    public static synchronized List<ClipItem> getClipItems(Context context) {
        List<ClipItem> list = new ArrayList<ClipItem>();
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        String json = sp.getString(PREF_CLIPBOARD_ITEMS, "[]");
        try {
            JSONArray arr = new JSONArray(json);
            for (int i = 0; i < arr.length(); i++) {
                Object elem = arr.get(i);
                if (elem instanceof JSONObject) {
                    JSONObject obj = (JSONObject) elem;
                    list.add(new ClipItem(
                            obj.optString("text", ""),
                            obj.optBoolean("pinned", false),
                            obj.optLong("timestamp", System.currentTimeMillis())
                    ));
                } else if (elem instanceof String) {
                    list.add(new ClipItem((String) elem, false, System.currentTimeMillis()));
                }
            }
        } catch (JSONException ignored) {}
        return list;
    }

    public static synchronized List<String> getClips(Context context) {
        List<ClipItem> items = getClipItems(context);
        List<String> list = new ArrayList<String>();
        for (ClipItem item : items) {
            list.add(item.text);
        }
        return list;
    }

    public static synchronized void togglePinClip(Context context, int index) {
        List<ClipItem> list = getClipItems(context);
        if (index >= 0 && index < list.size()) {
            ClipItem item = list.remove(index);
            item.isPinned = !item.isPinned;
            if (item.isPinned) {
                // Move to the very first position (top) as requested
                list.add(0, item);
            } else {
                // Move after any remaining pinned items
                int insertIdx = 0;
                while (insertIdx < list.size() && list.get(insertIdx).isPinned) {
                    insertIdx++;
                }
                list.add(insertIdx, item);
            }
            saveClipItems(context, list);
        }
    }

    public static synchronized void addClip(Context context, String text) {
        if (text == null || text.trim().isEmpty()) return;
        text = text.trim();

        List<ClipItem> list = getClipItems(context);
        boolean wasPinned = false;
        // Deduplicate: check if already present
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).text.equals(text)) {
                wasPinned = list.get(i).isPinned;
                list.remove(i);
                break;
            }
        }

        ClipItem newItem = new ClipItem(text, wasPinned, System.currentTimeMillis());
        if (wasPinned) {
            list.add(0, newItem);
        } else {
            // Insert right after pinned items
            int insertIdx = 0;
            while (insertIdx < list.size() && list.get(insertIdx).isPinned) {
                insertIdx++;
            }
            list.add(insertIdx, newItem);
        }

        // Pinned items are NEVER auto-deleted! Only unpinned items get trimmed from the end
        int maxSize = getMaxClipboardSize(context);
        while (list.size() > maxSize) {
            int lastUnpinned = -1;
            for (int i = list.size() - 1; i >= 0; i--) {
                if (!list.get(i).isPinned) {
                    lastUnpinned = i;
                    break;
                }
            }
            if (lastUnpinned >= 0) {
                list.remove(lastUnpinned);
            } else {
                // All remaining items are pinned, do not delete
                break;
            }
        }

        saveClipItems(context, list);
    }

    public static synchronized void deleteClip(Context context, int index) {
        List<ClipItem> list = getClipItems(context);
        if (index >= 0 && index < list.size()) {
            list.remove(index);
            saveClipItems(context, list);
        }
    }

    public static synchronized void clearAllClips(Context context) {
        // Clear unpinned clips, keep pinned clips protected
        List<ClipItem> list = getClipItems(context);
        List<ClipItem> pinnedOnly = new ArrayList<ClipItem>();
        for (ClipItem item : list) {
            if (item.isPinned) {
                pinnedOnly.add(item);
            }
        }
        saveClipItems(context, pinnedOnly);
    }

    private static void saveClipItems(Context context, List<ClipItem> list) {
        JSONArray arr = new JSONArray();
        for (ClipItem item : list) {
            try {
                JSONObject obj = new JSONObject();
                obj.put("text", item.text);
                obj.put("pinned", item.isPinned);
                obj.put("timestamp", item.timestamp);
                arr.put(obj);
            } catch (JSONException ignored) {}
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
