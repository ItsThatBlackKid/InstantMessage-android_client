package com.adeptlabs.android.instantmessage;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.InputType;
import android.widget.EditText;

/**
 * Created by sheku on 8/01/2016.
 */
public  class ServerChooseFragment extends DialogFragment {
    String serverIP = "139.216.250.16";

    @Override
    public Dialog onCreateDialog(Bundle instanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Enter Server IP");

        final EditText input = new EditText(getActivity());
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
        builder.setView(input)

                .setPositiveButton("DONE", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        serverIP = input.getText().toString();
                    }
                })
                .setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
        return builder.show();
    }

    public String getServerIP() {
        return serverIP;
    }
}