package lk.damithab.curenexadmin.dialog;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;

import com.google.android.material.button.MaterialButton;

import lk.damithab.curenexadmin.R;

public class CustomAlertDialog {
    private final Context context;
    private final View dialogView;
    private final AlertDialog alertDialog;

    public CustomAlertDialog(Context context) {
        this.context = context;
        dialogView = LayoutInflater.from(context).inflate(R.layout.item_custom_alert_dialog, null);

        alertDialog = new AlertDialog.Builder(context)
                .setView(dialogView)
                .create();

        if (alertDialog.getWindow() != null) {
            Window window = alertDialog.getWindow();
            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            int width = (int)(context.getResources().getDisplayMetrics().widthPixels * 0.90);
            window.setLayout(width, ViewGroup.LayoutParams.WRAP_CONTENT);
        }
    }

    public CustomAlertDialog setTitle(String title) {
        ((TextView) dialogView.findViewById(R.id.alert_dialog_title)).setText(title);
        return this;
    }

    public CustomAlertDialog setMessage(String message) {
        ((TextView) dialogView.findViewById(R.id.alert_dialog_message)).setText(message);
        return this;
    }

    public CustomAlertDialog setPositiveButton(String text, View.OnClickListener listener) {
        MaterialButton btn = dialogView.findViewById(R.id.alert_dialog_positive_btn);
        btn.setText(text);
        btn.setOnClickListener(v -> {
            listener.onClick(v);
            alertDialog.dismiss();
        });
        return this;
    }

    public CustomAlertDialog setNegativeButton(){
        MaterialButton btn = dialogView.findViewById(R.id.alert_dialog_negative_btn);
        btn.setOnClickListener(v->{
            alertDialog.dismiss();
        });
        return this;
    }

    public void show() {
        alertDialog.show();
    }
}
