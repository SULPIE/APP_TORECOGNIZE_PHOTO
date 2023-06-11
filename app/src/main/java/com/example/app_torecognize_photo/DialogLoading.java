package com.example.app_torecognize_photo;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;

public class DialogLoading
{
    private final Context context;
    private Dialog dialog;

    public DialogLoading(Context context)
    {
        this.context = context;
    }

    public void showDialog()
    {
        dialog = new Dialog(context);
        dialog.setContentView(R.layout.custom_dialog);
        dialog.setCancelable(true);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.create();
        dialog.show();
    }

    public void closeDialog()
    {
        dialog.dismiss();
    }
}
