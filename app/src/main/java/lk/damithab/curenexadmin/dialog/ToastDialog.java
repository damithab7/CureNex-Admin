package lk.damithab.curenexadmin.dialog;

import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import lk.damithab.curenexadmin.R;

public class ToastDialog extends DialogFragment {

    private static final String ARG_MESSAGE = "message";

    public ToastDialog(androidx.fragment.app.FragmentManager fragmentManager, String message){
        Bundle args = new Bundle();
        args.putString(ARG_MESSAGE, message);
        this.setArguments(args);
        this.show(fragmentManager, "ToastDialog");
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Dialog dialog = new Dialog(requireContext());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_toast);

        if (getArguments() != null) {
            String message = getArguments().getString(ARG_MESSAGE);
            TextView textView = dialog.findViewById(R.id.dialog_text_view);
            textView.setText(message);
        }

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            dialog.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
        }

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            if (isAdded()) {
                dismiss();
            }
        }, 1500);

        return dialog;
    }
}