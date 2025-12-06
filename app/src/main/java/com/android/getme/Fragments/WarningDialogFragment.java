package com.android.getme.Fragments;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.Layout;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.android.getme.R;

import java.util.zip.Inflater;

public class WarningDialogFragment extends DialogFragment {

    // empty constructor
    public WarningDialogFragment() {
        super();
    }

    public static WarningDialogFragment newInstance(String title, String content) {
        Bundle b = new Bundle();
        b.putString("title", title);
        b.putString("content", content);
        WarningDialogFragment fragment = new WarningDialogFragment();
        fragment.setArguments(b);
        return fragment;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.fragment_warning_dialog,null);

        TextView title = view.findViewById(R.id.dialogTitle);
        TextView content = view.findViewById(R.id.dialogContent);

        if(getArguments() != null) {
            title.setText(getArguments().getString("title"));
            content.setText(getArguments().getString("content"));
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(view);
        builder.setNeutralButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dismiss();
            }
        });

        return builder.create();
    }
}
