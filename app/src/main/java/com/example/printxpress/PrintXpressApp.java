package com.example.printxpress;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

public class PrintXpressApp extends Application {

    private final Handler handler = new Handler(Looper.getMainLooper());

    @Override
    public void onCreate() {
        super.onCreate();

        registerActivityLifecycleCallbacks(new ActivityLifecycleCallbacks() {
            @Override
            public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
            }

            @Override
            public void onActivityStarted(Activity activity) {
            }

            @Override
            public void onActivityResumed(Activity activity) {
                handler.postDelayed(() -> ButtonLoadingManager.attach(activity), 180);
            }

            @Override
            public void onActivityPaused(Activity activity) {
                LoadingDialogUtil.hide(activity);
            }

            @Override
            public void onActivityStopped(Activity activity) {
            }

            @Override
            public void onActivitySaveInstanceState(Activity activity, Bundle outState) {
            }

            @Override
            public void onActivityDestroyed(Activity activity) {
                LoadingDialogUtil.hide(activity);
            }
        });
    }
}
