package org.pocketworkstation.pckeyboard;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;

public class VoicePermissionActivity extends Activity {
    private static final int PERMISSION_REQUEST_CODE = 42;

    public interface PermissionCallback {
        void onPermissionResult(boolean granted);
    }

    private static PermissionCallback sCallback;

    public static void setCallback(PermissionCallback callback) {
        sCallback = callback;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.RECORD_AUDIO}, PERMISSION_REQUEST_CODE);
                return;
            }
        }
        notifyResult(true);
        finish();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            boolean granted = grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED;
            notifyResult(granted);
        }
        finish();
    }

    private void notifyResult(boolean granted) {
        if (sCallback != null) {
            sCallback.onPermissionResult(granted);
            sCallback = null;
        }
    }
}
