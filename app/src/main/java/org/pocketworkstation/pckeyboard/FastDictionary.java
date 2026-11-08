package org.pocketworkstation.pckeyboard;

import android.content.Context;
import android.util.Log;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * High-performance, pure Java dictionary engine for Persian and English.
 * Loads 100,000+ words per language from merged frequency files.
 * Uses 2-character prefix bucket indexing for sub-millisecond suggestions.
 */
public class FastDictionary extends Dictionary {
    private static final String TAG = "FastDictionary";

    private static final String FILE_EN = "update_en_merged.txt";
    private static final String FILE_FA = "update_fa_merged.txt";

    private static final int MAX_RESULTS_TO_SEND = 25;

    private static class LangDict {
        String[] words;
        int[] freqs;
        // 2-character prefix index -> array of indices in words[]
        Map<String, int[]> prefixBuckets = new HashMap<String, int[]>();
        // 1-character prefix index -> array of top word indices in words[]
        Map<String, int[]> singleCharBuckets = new HashMap<String, int[]>();
        // Fast exact word existence set
        Set<String> wordSet = new HashSet<String>();
        volatile boolean isLoaded = false;
        int totalWords = 0;
    }

    private final LangDict mDictEn = new LangDict();
    private final LangDict mDictFa = new LangDict();

    private final Context mContext;
    private final ExecutorService mExecutor = Executors.newFixedThreadPool(2);

    public FastDictionary(Context context) {
        mContext = context.getApplicationContext();
        loadDictionariesAsync();
    }

    private void loadDictionariesAsync() {
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                loadLanguage(FILE_FA, mDictFa, true);
            }
        });
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                loadLanguage(FILE_EN, mDictEn, false);
            }
        });
    }

    private void loadLanguage(String assetName, LangDict dict, boolean isPersian) {
        try {
            long startTime = System.currentTimeMillis();
            InputStream is = mContext.getAssets().open(assetName);
            BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8), 32768);

            ArrayList<String> wordList = new ArrayList<String>(100500);
            ArrayList<Integer> freqList = new ArrayList<Integer>(100500);
            Map<String, ArrayList<Integer>> tempBuckets = new HashMap<String, ArrayList<Integer>>(1500);
            Map<String, ArrayList<Integer>> tempSingleBuckets = new HashMap<String, ArrayList<Integer>>(100);

            String line;
            int index = 0;
            double maxFreq = 1.0;

            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;

                int spaceIdx = line.indexOf(' ');
                String word;
                int freq = 1;
                if (spaceIdx > 0) {
                    word = line.substring(0, spaceIdx).trim();
                    try {
                        freq = Integer.parseInt(line.substring(spaceIdx + 1).trim());
                    } catch (NumberFormatException e) {
                        freq = 1;
                    }
                } else {
                    word = line;
                }

                if (isPersian) {
                    word = normalizePersian(word);
                } else {
                    word = word.toLowerCase();
                }

                if (word.isEmpty()) continue;

                if (index == 0) {
                    maxFreq = Math.max(1.0, (double) freq);
                }

                // Logarithmic frequency normalization into 1..255 scale
                int normalizedFreq = Math.max(1, (int) Math.round(255.0 * Math.log(freq + 1.0) / Math.log(maxFreq + 1.0)));

                wordList.add(word);
                freqList.add(normalizedFreq);
                dict.wordSet.add(word);

                // Index 1-character prefix (keep top 40)
                String c1 = word.substring(0, 1);
                ArrayList<Integer> singleList = tempSingleBuckets.get(c1);
                if (singleList == null) {
                    singleList = new ArrayList<Integer>(45);
                    tempSingleBuckets.put(c1, singleList);
                }
                if (singleList.size() < 40) {
                    singleList.add(index);
                }

                // Index 2-character prefix
                String c2 = word.length() >= 2 ? word.substring(0, 2) : c1;
                ArrayList<Integer> bucket = tempBuckets.get(c2);
                if (bucket == null) {
                    bucket = new ArrayList<Integer>();
                    tempBuckets.put(c2, bucket);
                }
                bucket.add(index);

                index++;
            }
            reader.close();

            dict.words = wordList.toArray(new String[wordList.size()]);
            dict.freqs = new int[freqList.size()];
            for (int i = 0; i < freqList.size(); i++) {
                dict.freqs[i] = freqList.get(i);
            }

            // Compact prefix buckets into primitive int[] arrays for optimal cache locality
            for (Map.Entry<String, ArrayList<Integer>> entry : tempBuckets.entrySet()) {
                ArrayList<Integer> list = entry.getValue();
                int[] arr = new int[list.size()];
                for (int i = 0; i < list.size(); i++) {
                    arr[i] = list.get(i);
                }
                dict.prefixBuckets.put(entry.getKey(), arr);
            }

            for (Map.Entry<String, ArrayList<Integer>> entry : tempSingleBuckets.entrySet()) {
                ArrayList<Integer> list = entry.getValue();
                int[] arr = new int[list.size()];
                for (int i = 0; i < list.size(); i++) {
                    arr[i] = list.get(i);
                }
                dict.singleCharBuckets.put(entry.getKey(), arr);
            }

            dict.totalWords = index;
            dict.isLoaded = true;

            long elapsed = System.currentTimeMillis() - startTime;
            Log.i(TAG, "Loaded " + dict.totalWords + " words from " + assetName + " in " + elapsed + "ms");
        } catch (Throwable t) {
            Log.e(TAG, "Error loading " + assetName, t);
        }
    }

    @Override
    public void getWords(WordComposer composer, WordCallback callback, int[] nextLettersFrequencies) {
        if (composer == null || callback == null) return;

        CharSequence typedWord = composer.getTypedWord();
        if (typedWord == null || typedWord.length() == 0) return;

        String rawQuery = typedWord.toString();
        boolean isPersian = FontUtils.containsPersian(rawQuery);
        if (!isPersian && LatinIME.sKeyboardSettings != null && FontUtils.isPersianLocale(LatinIME.sKeyboardSettings.inputLocale)) {
            isPersian = true;
        }

        LangDict dict = isPersian ? mDictFa : mDictEn;
        if (!dict.isLoaded) {
            return;
        }

        String query = isPersian ? normalizePersian(rawQuery) : rawQuery.toLowerCase();
        int queryLen = query.length();
        if (queryLen == 0) return;

        int[] candidateIndices = null;
        if (queryLen == 1) {
            candidateIndices = dict.singleCharBuckets.get(query);
        } else {
            String prefix2 = query.substring(0, 2);
            candidateIndices = dict.prefixBuckets.get(prefix2);
        }

        if (candidateIndices == null) {
            return;
        }

        boolean isFirstCap = !isPersian && Character.isUpperCase(rawQuery.charAt(0));
        boolean isAllUpper = !isPersian && rawQuery.length() > 1 && isAllUpperCase(rawQuery);

        int count = 0;
        int nCandidates = candidateIndices.length;

        for (int i = 0; i < nCandidates; i++) {
            int wIdx = candidateIndices[i];
            String candidate = dict.words[wIdx];

            if (queryLen == 1 || candidate.startsWith(query)) {
                // Formatting for display (case adjustment)
                String displayWord = candidate;
                if (isAllUpper) {
                    displayWord = candidate.toUpperCase();
                } else if (isFirstCap && candidate.length() > 0) {
                    displayWord = Character.toUpperCase(candidate.charAt(0)) + (candidate.length() > 1 ? candidate.substring(1) : "");
                }

                char[] chars = displayWord.toCharArray();
                int score = dict.freqs[wIdx];

                // Exact match bonus
                if (candidate.equals(query)) {
                    score = Math.min(255, score + 40);
                }

                boolean keepGoing = callback.addWord(chars, 0, chars.length, score, Suggest.DIC_MAIN, DataType.UNIGRAM);

                // Update next letter frequency
                if (nextLettersFrequencies != null && candidate.length() > queryLen) {
                    char nextChar = candidate.charAt(queryLen);
                    if (nextChar < nextLettersFrequencies.length) {
                        nextLettersFrequencies[nextChar] = Math.min(255, nextLettersFrequencies[nextChar] + 5);
                    }
                }

                count++;
                if (!keepGoing || count >= MAX_RESULTS_TO_SEND) {
                    break;
                }
            }
        }
    }

    @Override
    public boolean isValidWord(CharSequence word) {
        if (word == null || word.length() == 0) return false;
        String w = word.toString();
        boolean isPersian = FontUtils.containsPersian(w);
        if (isPersian) {
            String norm = normalizePersian(w);
            return mDictFa.wordSet.contains(norm);
        } else {
            String lower = w.toLowerCase();
            return mDictEn.wordSet.contains(lower);
        }
    }

    public int getSize() {
        return mDictFa.totalWords + mDictEn.totalWords;
    }

    public static String normalizePersian(String input) {
        if (input == null) return "";
        final int len = input.length();
        StringBuilder sb = new StringBuilder(len);
        for (int i = 0; i < len; i++) {
            char c = input.charAt(i);
            switch (c) {
                case '\u064A': // Arabic Yeh -> Persian Yeh
                case '\u0649': // Alef Maksura -> Persian Yeh
                    sb.append('\u06CC');
                    break;
                case '\u0643': // Arabic Kaf -> Persian Kaf
                    sb.append('\u06A9');
                    break;
                case '\u06C0': // Heh with Yeh above -> Heh
                case '\u06C1':
                case '\u06C2':
                    sb.append('\u0647');
                    break;
                case '\u064B': // Tanween / Harakat (strip)
                case '\u064C':
                case '\u064D':
                case '\u064E':
                case '\u064F':
                case '\u0650':
                case '\u0651':
                case '\u0652':
                case '\u0654':
                    // skip diacritics
                    break;
                default:
                    sb.append(c);
                    break;
            }
        }
        return sb.toString();
    }

    private static boolean isAllUpperCase(String s) {
        for (int i = 0; i < s.length(); i++) {
            if (Character.isLetter(s.charAt(i)) && !Character.isUpperCase(s.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    @Override
    public void close() {
        mExecutor.shutdown();
    }
}
