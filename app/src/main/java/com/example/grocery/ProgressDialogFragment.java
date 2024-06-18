package com.example.grocery;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;

import androidx.fragment.app.DialogFragment;

public class ProgressDialogFragment extends DialogFragment {
    private ProgressDialog progressDialog;

    public static ProgressDialogFragment newInstance(String message) {
        ProgressDialogFragment frag = new ProgressDialogFragment();
        Bundle args = new Bundle();
        args.putString("message", message); // Pass data as arguments
        frag.setArguments(args);
        return frag;
    }

    @androidx.annotation.NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        assert getArguments() != null;
        String message = getArguments().getString("message", "Please wait"); // Get message, or default
        ProgressDialog progressDialog = new ProgressDialog(getActivity());
        progressDialog.setTitle(message); // Set title from the message
        progressDialog.setIndeterminate(true);
        progressDialog.setCanceledOnTouchOutside(false);
        return progressDialog;
    }

    @Override
    public void onDismiss(@NonNull DialogInterface dialog) {
        super.onDismiss(dialog);
        if (progressDialog != null) {
            progressDialog.dismiss(); // Ensure the dialog is dismissed
        }
    }
}