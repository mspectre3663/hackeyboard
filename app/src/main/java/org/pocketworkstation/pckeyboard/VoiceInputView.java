package org.pocketworkstation.pckeyboard;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;

public class VoiceInputView extends FrameLayout implements RecognitionListener {

    public interface VoiceInputListener {
        void onVoiceTextRecognized(String text);
        void onVoiceInputCancelled();
    }

    private VoiceInputListener mListener;
    private VoiceParticleRippleView mRippleView;
    private TextView mStatusText;
    private TextView mLanguageToggle;
    private View mCloseBtn;
    private View mDoneBtn;

    private SpeechRecognizer mSpeechRecognizer;
    private boolean mIsListening = false;
    private String mCurrentLanguage = "fa-IR"; // Default to Persian or English
    private String mLastRecognizedText = "";
    private final Handler mHandler = new Handler(Looper.getMainLooper());

    public VoiceInputView(Context context) {
        super(context);
        init(context);
    }

    public VoiceInputView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public void setListener(VoiceInputListener listener) {
        mListener = listener;
    }

    public void setInitialLanguage(boolean isPersian) {
        mCurrentLanguage = isPersian ? "fa-IR" : "en-US";
        updateLanguageButton();
    }

    private void init(Context context) {
        // 1. Layer: 2-color Particle Dot Ripple Animation View
        mRippleView = new VoiceParticleRippleView(context);
        addView(mRippleView, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));

        // 2. Layer: Foreground Overlay with Minimal Monochrome Controls
        LinearLayout overlay = new LinearLayout(context);
        overlay.setOrientation(LinearLayout.VERTICAL);
        overlay.setGravity(Gravity.CENTER);
        overlay.setPadding(dpToPx(16), dpToPx(12), dpToPx(16), dpToPx(12));

        // Top bar: Close (✕), Language Toggle, Done (✓)
        LinearLayout topBar = new LinearLayout(context);
        topBar.setOrientation(LinearLayout.HORIZONTAL);
        topBar.setGravity(Gravity.CENTER_VERTICAL);

        // Close button
        TextView closeBtn = new TextView(context);
        closeBtn.setText("✕");
        closeBtn.setTextColor(Color.WHITE);
        closeBtn.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
        closeBtn.setGravity(Gravity.CENTER);
        closeBtn.setBackgroundColor(Color.parseColor("#25262B"));
        int pad = dpToPx(8);
        closeBtn.setPadding(pad, pad, pad, pad);
        closeBtn.setClickable(true);
        closeBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                cancelListening();
                if (mListener != null) {
                    mListener.onVoiceInputCancelled();
                }
            }
        });
        mCloseBtn = closeBtn;
        topBar.addView(closeBtn, new LinearLayout.LayoutParams(dpToPx(38), dpToPx(38)));

        // Status text / live transcription
        mStatusText = new TextView(context);
        mStatusText.setTextColor(Color.WHITE);
        mStatusText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
        mStatusText.setGravity(Gravity.CENTER);
        mStatusText.setText("در حال شنیدن...");
        mStatusText.setMaxLines(2);
        LinearLayout.LayoutParams statusLp = new LinearLayout.LayoutParams(0, LayoutParams.WRAP_CONTENT, 1.0f);
        statusLp.setMargins(dpToPx(10), 0, dpToPx(10), 0);
        topBar.addView(mStatusText, statusLp);

        // Language toggle button (FA / EN)
        mLanguageToggle = new TextView(context);
        mLanguageToggle.setTextColor(Color.WHITE);
        mLanguageToggle.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
        mLanguageToggle.setGravity(Gravity.CENTER);
        mLanguageToggle.setBackgroundColor(Color.parseColor("#25262B"));
        mLanguageToggle.setPadding(dpToPx(10), dpToPx(6), dpToPx(10), dpToPx(6));
        mLanguageToggle.setClickable(true);
        mLanguageToggle.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if ("fa-IR".equals(mCurrentLanguage)) {
                    mCurrentLanguage = "en-US";
                } else {
                    mCurrentLanguage = "fa-IR";
                }
                updateLanguageButton();
                restartListening();
            }
        });
        topBar.addView(mLanguageToggle, new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, dpToPx(38)));

        overlay.addView(topBar, new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));

        // Center Area: Clean, transparent area showing purely the 2-color particle wave
        FrameLayout centerArea = new FrameLayout(context);
        LinearLayout.LayoutParams centerLp = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, 0, 1.0f);
        centerArea.setLayoutParams(centerLp);
        centerArea.setClickable(true);
        centerArea.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mIsListening) {
                    stopListeningAndSubmit();
                } else {
                    startListening();
                }
            }
        });
        overlay.addView(centerArea);

        // Bottom Done / Submit button
        TextView doneBtn = new TextView(context);
        doneBtn.setText("✓ تایید و ورود متن");
        doneBtn.setTextColor(Color.WHITE);
        doneBtn.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
        doneBtn.setGravity(Gravity.CENTER);
        doneBtn.setBackgroundColor(Color.parseColor("#282930"));
        int padH = dpToPx(24);
        int padV = dpToPx(10);
        doneBtn.setPadding(padH, padV, padH, padV);
        doneBtn.setClickable(true);
        doneBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                stopListeningAndSubmit();
            }
        });
        mDoneBtn = doneBtn;
        LinearLayout.LayoutParams doneLp = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        doneLp.gravity = Gravity.CENTER_HORIZONTAL;
        overlay.addView(doneBtn, doneLp);

        addView(overlay, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
        updateLanguageButton();
    }

    private void updateLanguageButton() {
        if ("fa-IR".equals(mCurrentLanguage)) {
            mLanguageToggle.setText("فارسی");
        } else {
            mLanguageToggle.setText("English");
        }
    }

    public void startListening() {
        Context context = getContext();
        if (!SpeechRecognizer.isRecognitionAvailable(context)) {
            mStatusText.setText("تشخیص صوتی در دستگاه در دسترس نیست");
            return;
        }

        mLastRecognizedText = "";
        mRippleView.resetEnergy();
        mStatusText.setText("fa-IR".equals(mCurrentLanguage) ? "در حال شنیدن..." : "Listening...");

        try {
            if (mSpeechRecognizer != null) {
                mSpeechRecognizer.destroy();
            }
            mSpeechRecognizer = SpeechRecognizer.createSpeechRecognizer(context);
            mSpeechRecognizer.setRecognitionListener(this);

            Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, mCurrentLanguage);
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, mCurrentLanguage);
            intent.putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true);
            intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3);

            mSpeechRecognizer.startListening(intent);
            mIsListening = true;
        } catch (Throwable t) {
            mStatusText.setText("خطا در شروع ضبط صدا");
            mIsListening = false;
        }
    }

    public void restartListening() {
        cancelListening();
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                startListening();
            }
        }, 150);
    }

    public void stopListeningAndSubmit() {
        if (mSpeechRecognizer != null && mIsListening) {
            try {
                mSpeechRecognizer.stopListening();
            } catch (Throwable ignored) {}
        }
        mIsListening = false;
        mRippleView.resetEnergy();

        if (mLastRecognizedText != null && !mLastRecognizedText.trim().isEmpty()) {
            if (mListener != null) {
                mListener.onVoiceTextRecognized(mLastRecognizedText.trim());
            }
        }
    }

    public void cancelListening() {
        mIsListening = false;
        if (mSpeechRecognizer != null) {
            try {
                mSpeechRecognizer.cancel();
                mSpeechRecognizer.destroy();
            } catch (Throwable ignored) {}
            mSpeechRecognizer = null;
        }
        mRippleView.resetEnergy();
    }

    // RecognitionListener Callbacks
    @Override
    public void onReadyForSpeech(Bundle params) {
        mIsListening = true;
        mStatusText.setText("fa-IR".equals(mCurrentLanguage) ? "در حال شنیدن..." : "Listening...");
    }

    @Override
    public void onBeginningOfSpeech() {
        mRippleView.setRmsdB(5f);
    }

    @Override
    public void onRmsChanged(float rmsdB) {
        mRippleView.setRmsdB(rmsdB);
    }

    @Override
    public void onBufferReceived(byte[] buffer) {}

    @Override
    public void onEndOfSpeech() {
        mRippleView.resetEnergy();
        mStatusText.setText("fa-IR".equals(mCurrentLanguage) ? "در حال پردازش..." : "Processing...");
    }

    @Override
    public void onError(int error) {
        mIsListening = false;
        mRippleView.resetEnergy();

        String msg;
        switch (error) {
            case SpeechRecognizer.ERROR_NO_MATCH:
            case SpeechRecognizer.ERROR_SPEECH_TIMEOUT:
                msg = "صدا واضح شنیده نشد، دوباره امتحان کنید";
                break;
            case SpeechRecognizer.ERROR_AUDIO:
                msg = "خطای صوتی میکروفون";
                break;
            case SpeechRecognizer.ERROR_NETWORK:
            case SpeechRecognizer.ERROR_NETWORK_TIMEOUT:
                msg = "خطای ارتباط اینترنتی";
                break;
            default:
                msg = "لمس کنید تا مجدد صحبت کنید";
                break;
        }
        mStatusText.setText(msg);
    }

    @Override
    public void onResults(Bundle results) {
        mIsListening = false;
        mRippleView.resetEnergy();

        ArrayList<String> matches = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
        if (matches != null && !matches.isEmpty()) {
            String text = matches.get(0);
            mLastRecognizedText = text;
            mStatusText.setText(text);
            if (mListener != null) {
                mListener.onVoiceTextRecognized(text);
            }
        }
    }

    @Override
    public void onPartialResults(Bundle partialResults) {
        ArrayList<String> matches = partialResults.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
        if (matches != null && !matches.isEmpty()) {
            String text = matches.get(0);
            mLastRecognizedText = text;
            mStatusText.setText(text);
        }
    }

    @Override
    public void onEvent(int eventType, Bundle params) {}

    private int dpToPx(float dp) {
        return (int) (dp * getContext().getResources().getDisplayMetrics().density + 0.5f);
    }
}
