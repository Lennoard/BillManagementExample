package com.lennoardsilva.androidmobillschallenge.helpers;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;

import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.view.ViewCompat;

import com.google.android.material.snackbar.Snackbar;

import org.jetbrains.annotations.NotNull;

public class MoveUpwardBehavior extends CoordinatorLayout.Behavior<View> {

    public MoveUpwardBehavior() {
        super();
    }

    public MoveUpwardBehavior(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean layoutDependsOn(@NotNull CoordinatorLayout parent, @NotNull View child, @NotNull View dependency) {
        return dependency instanceof Snackbar.SnackbarLayout;
    }

    @Override
    public boolean onDependentViewChanged(@NotNull CoordinatorLayout parent, @NotNull View child, @NotNull View dependency) {
        float translationY = Math.min(0, ViewCompat.getTranslationY(dependency) - dependency.getHeight());
        ViewCompat.setTranslationY(child, translationY);
        return true;
    }

    //you need this when you swipe the snackbar(thanx to ubuntudroid's comment)
    @Override
    public void onDependentViewRemoved(@NotNull CoordinatorLayout parent, @NotNull View child, @NotNull View dependency) {
        ViewCompat.animate(child).translationY(0).start();
    }
}