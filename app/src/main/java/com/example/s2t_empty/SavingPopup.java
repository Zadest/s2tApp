package com.example.s2t_empty;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;


public class SavingPopup extends DialogFragment {

    //enable handling saving from MainActivity

    public interface SavingPopupListener{
        void onDialogPositiveClick(DialogFragment dialog);
    }

    SavingPopupListener listener;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        try {
            listener = (SavingPopupListener) context;
        } catch (ClassCastException e){
            throw new ClassCastException(getActivity().toString() + " must implement SavingPopupListener");
        }
    }

    //build dialog, add layout
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        builder.setView(inflater.inflate(R.layout.activity_saving_popup, null))
                .setTitle(R.string.savingpopup)
                .setPositiveButton(R.string.save, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        listener.onDialogPositiveClick(SavingPopup.this);
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                    }
                });
        return builder.create();
    }

}