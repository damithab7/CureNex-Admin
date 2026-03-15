package lk.damithab.curenexadmin.dialog;

import android.app.Dialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;

import lk.damithab.curenexadmin.R;

public class SpinnerDialog extends DialogFragment {

    private static final String TAG = "SpinnerDialog";

    public static SpinnerDialog show(FragmentManager fragmentManager) {
        SpinnerDialog dialog = new SpinnerDialog();
        dialog.show(fragmentManager, TAG);
        return dialog;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Dialog dialog = new Dialog(requireContext());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_spinner);

        setCancelable(false);

        // Apply transparent background and remove dim
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            dialog.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
            dialog.getWindow().getAttributes().windowAnimations = R.style.DialogFadeAnimation;
        }
//        // Auto-dismiss after 1.5 seconds
//        new Handler(Looper.getMainLooper()).postDelayed(() -> {
//            if (isAdded()) {
//                dismiss();
//            }
//        }, 1500);
        return dialog;

    }

}
