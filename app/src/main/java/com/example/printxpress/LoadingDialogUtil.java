package com.example.printxpress;

import android.app.Activity;
import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Handler;
import android.os.Looper;
import android.view.Window;
import android.widget.TextView;

import java.util.WeakHashMap;

public class LoadingDialogUtil {

    private static final Handler handler = new Handler(Looper.getMainLooper());
    private static final WeakHashMap<Activity, Dialog> dialogMap = new WeakHashMap<>();

    private LoadingDialogUtil() {
    }

    public static void showQuick(Activity activity) {
        show(activity, "Loading...", 420);
    }

    public static void showLogin(Activity activity) {
        show(activity, "Signing in...", 0);
    }

    public static void show(Activity activity, String message, long autoDismissMillis) {
        if (activity == null || activity.isFinishing() || activity.isDestroyed()) {
            return;
        }

        hide(activity);

        Dialog dialog = new Dialog(activity);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_loading);
        dialog.setCancelable(false);

        TextView loadingText = dialog.findViewById(R.id.tvLoadingText);
        if (loadingText != null) {
            loadingText.setText(message);
        }

        Window window = dialog.getWindow();
        if (window != null) {
            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }

        dialogMap.put(activity, dialog);
        dialog.show();

        if (autoDismissMillis > 0) {
            handler.postDelayed(() -> hide(activity), autoDismissMillis);
        }
    }

    public static void hide(Activity activity) {
        if (activity == null) {
            return;
        }

        Dialog dialog = dialogMap.remove(activity);
        if (dialog != null && dialog.isShowing()) {
            try {
                dialog.dismiss();
            } catch (Exception ignored) {
            }
        }
    }
}
