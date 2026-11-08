package org.pocketworkstation.pckeyboard;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.view.View;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class VoiceParticleRippleView extends View {

    // Strictly 2 colors as requested:
    // Color 1: Background color
    private static final int COLOR_BACKGROUND = Color.parseColor("#121316");
    // Color 2: Dot/Particle color
    private static final int COLOR_DOT = Color.WHITE;

    private final Paint mDotPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    // Audio level tracking
    private float mTargetEnergy = 0f;
    private float mCurrentEnergy = 0f;
    private long mLastTime = 0;

    // Ripple wave descriptor
    private static class Wave {
        float radius;
        float speed; // px per second
        float intensity; // 0..1
        float width; // wave thickness

        Wave(float radius, float speed, float intensity, float width) {
            this.radius = radius;
            this.speed = speed;
            this.intensity = intensity;
            this.width = width;
        }
    }

    private final List<Wave> mWaves = new ArrayList<Wave>();
    private long mLastWaveEmission = 0;

    public VoiceParticleRippleView(Context context) {
        super(context);
        init();
    }

    public VoiceParticleRippleView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        mDotPaint.setColor(COLOR_DOT);
        mDotPaint.setStyle(Paint.Style.FILL);
    }

    /**
     * Update audio RMS level from SpeechRecognizer
     * @param rmsdB speech audio level, typically -2dB to +12dB
     */
    public void setRmsdB(float rmsdB) {
        // Normalize rmsdB into 0.0 .. 1.0 range
        float normalized = Math.max(0f, Math.min(1f, (rmsdB + 2f) / 12f));
        mTargetEnergy = normalized;
        postInvalidateOnAnimation();
    }

    public void resetEnergy() {
        mTargetEnergy = 0f;
        mCurrentEnergy = 0f;
        mWaves.clear();
        postInvalidateOnAnimation();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        final int w = getWidth();
        final int h = getHeight();
        if (w <= 0 || h <= 0) return;

        // 1. Draw solid background (Color 1)
        canvas.drawColor(COLOR_BACKGROUND);

        long now = SystemClock.uptimeMillis();
        if (mLastTime == 0) mLastTime = now;
        float dt = Math.min(0.05f, (now - mLastTime) / 1000f);
        mLastTime = now;

        // Smooth energy transition
        mCurrentEnergy += (mTargetEnergy - mCurrentEnergy) * Math.min(1f, dt * 10f);

        final float cx = w / 2f;
        final float cy = h / 2f;
        final float maxRadius = (float) Math.hypot(w, h) / 2f + 40f;

        // Emit new wave based on speech energy
        long waveInterval = (long) (450 - (mCurrentEnergy * 250)); // between 200ms and 450ms
        if (now - mLastWaveEmission > waveInterval) {
            float waveIntensity = Math.max(0.2f, mCurrentEnergy);
            float speed = 260f + (mCurrentEnergy * 180f); // expanding speed px/s
            float waveWidth = dpToPx(38 + (mCurrentEnergy * 32));
            mWaves.add(new Wave(0f, speed, waveIntensity, waveWidth));
            mLastWaveEmission = now;
        }

        // Update active waves
        Iterator<Wave> it = mWaves.iterator();
        while (it.hasNext()) {
            Wave wave = it.next();
            wave.radius += wave.speed * dt;
            if (wave.radius > maxRadius) {
                it.remove();
            }
        }

        // 2. Render particle dot grid
        final float dotSpacing = dpToPx(18);
        final float baseRadius = dpToPx(1.6f);
        // Expansion: moderate, "نه زیاد و نه کم مناسب" -> up to +2.8dp (max ~4.4dp)
        final float maxExpansion = dpToPx(2.8f);

        int cols = (int) (w / dotSpacing) + 2;
        int rows = (int) (h / dotSpacing) + 2;

        float startX = (w - (cols - 1) * dotSpacing) / 2f;
        float startY = (h - (rows - 1) * dotSpacing) / 2f;

        for (int r = 0; r < rows; r++) {
            float y = startY + r * dotSpacing;
            for (int c = 0; c < cols; c++) {
                float x = startX + c * dotSpacing;

                float distToCenter = (float) Math.hypot(x - cx, y - cy);

                // Find wave influence
                float waveFactor = 0f;
                for (int i = 0; i < mWaves.size(); i++) {
                    Wave wave = mWaves.get(i);
                    float diff = Math.abs(distToCenter - wave.radius);
                    if (diff < wave.width) {
                        float influence = 1f - (diff / wave.width);
                        // Bell curve smooth falloff
                        influence = influence * influence * (3f - 2f * influence);
                        // Wave fades as it expands to edges
                        float distanceFade = Math.max(0f, 1f - (wave.radius / maxRadius));
                        float factor = influence * wave.intensity * distanceFade;
                        if (factor > waveFactor) {
                            waveFactor = factor;
                        }
                    }
                }

                // Dot size: base size + expansion
                float rDot = baseRadius + (maxExpansion * waveFactor);

                // Dot alpha: base opacity (Color 2 @ alpha 55) up to full brightness (255)
                int alpha = (int) (55 + (200 * waveFactor));
                mDotPaint.setAlpha(Math.min(255, alpha));

                canvas.drawCircle(x, y, rDot, mDotPaint);
            }
        }

        // Request next animation frame for continuous smooth rippling
        postInvalidateOnAnimation();
    }

    private float dpToPx(float dp) {
        return dp * getContext().getResources().getDisplayMetrics().density;
    }
}
