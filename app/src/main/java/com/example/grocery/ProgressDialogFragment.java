package com.example.grocery;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.os.Bundle;

import androidx.fragment.app.DialogFragment;

public class ProgressDialogFragment extends DialogFragment {

    public static ProgressDialogFragment newInstance(String message) {
        ProgressDialogFragment frag = new ProgressDialogFragment();
        Bundle args = new Bundle();
        args.putString("message", message);
        frag.setArguments(args);
        return frag;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        ProgressDialog progressDialog = new ProgressDialog(getActivity());
        progressDialog.setMessage(getArguments().getString("message"));
        progressDialog.setIndeterminate(true);
        progressDialog.setCanceledOnTouchOutside(false);
        return progressDialog;
    }
}