package com.example.printxpress;

import android.app.Activity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;

public class ButtonLoadingManager {

    private ButtonLoadingManager() {
    }

    public static void attach(Activity activity) {
        if (activity == null || activity instanceof LoginActivity) {
            return;
        }

        View root = activity.getWindow().getDecorView().getRootView();
        attachToClickableViews(activity, root);
    }

    private static void attachToClickableViews(Activity activity, View view) {
        if (view == null) {
            return;
        }

        boolean shouldAttach = false;

        // Normal buttons in every screen
        if (view instanceof Button && !(view instanceof RadioButton)) {
            shouldAttach = true;
        }

        // Dashboard cards and other clickable panels such as admin function cards
        if (!(view instanceof EditText) && view.isClickable() && !(view instanceof RadioButton)) {
            shouldAttach = true;
        }

        if (shouldAttach) {
            Object alreadyAttached = view.getTag(R.id.tag_loading_listener_attached);
            if (alreadyAttached == null) {
                view.setTag(R.id.tag_loading_listener_attached, true);
                view.setOnTouchListener((buttonView, event) -> {
                    if (event.getAction() == MotionEvent.ACTION_UP && buttonView.isEnabled()) {
                        LoadingDialogUtil.showQuick(activity);
                    }
                    return false;
                });
            }
        }

        if (view instanceof ViewGroup) {
            ViewGroup group = (ViewGroup) view;
            for (int i = 0; i < group.getChildCount(); i++) {
                attachToClickableViews(activity, group.getChildAt(i));
            }
        }
    }
}
