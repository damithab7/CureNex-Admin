package lk.damithab.curenexadmin.util;

import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;

public class AnimationUtil {

    public static void bottomSlideDown(final View view) {
        if (view == null || view.getVisibility() == View.GONE) return;

        view.animate()
                .translationY(view.getHeight())
                .alpha(0f)
                .setDuration(300)
                .setInterpolator(new AccelerateInterpolator())
                .withEndAction(() -> view.setVisibility(View.GONE))
                .start();
    }

    public static void topSlideDown(final View view) {
        if (view == null || view.getVisibility() == View.VISIBLE) return;

        view.setVisibility(View.VISIBLE);
        view.setAlpha(0f);

        view.post(() -> {
            float height = view.getHeight();
            view.setTranslationY(-height);
            view.animate()
                    .translationY(0)
                    .alpha(1f)
                    .setDuration(300)
                    .setInterpolator(new AccelerateInterpolator())
                    .start();
        });
    }

    public static void topSlideUp(final View view) {
        if (view == null || view.getVisibility() == View.GONE) return;
        float height = view.getHeight();
        view.animate()
                .translationY(-height)
                .alpha(0f)
                .setDuration(300)
                .setInterpolator(new DecelerateInterpolator())
                .withEndAction(() ->{
                    view.setVisibility(View.GONE);
                    view.setTranslationY(0);
                })
                .start();
    }

    public static void bottomSlideUp(final View view) {
        if (view == null || view.getVisibility() == View.VISIBLE) return;

        view.setVisibility(View.VISIBLE);
        view.setAlpha(0f);
        view.setTranslationY(view.getHeight());

        view.animate()
                .translationY(0)
                .alpha(1f)
                .setDuration(300)
                .setInterpolator(new DecelerateInterpolator())
                .setListener(null)
                .start();
    }
}
