package mfdevelopement.eggmanager.dialog_fragments;


import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;

import mfdevelopement.eggmanager.R;

public class DeleteDatabaseDialog extends DialogFragment {

    private final static String LOG_TAG = "DeleteDatabaseDialog";

    private DialogButtonClickedListener listener;

    public interface DialogButtonClickedListener {
        void onDeleteOkClicked();
        void onDeleteCancelClicked();
    }

    /**
     * Create a new instance of SortingDialogFragment
     */
    public static DeleteDatabaseDialog newInstance() {
        Log.d(LOG_TAG,"creating new instance of DeleteDatabaseDialog");
        return new DeleteDatabaseDialog();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof DialogButtonClickedListener) {
            listener = (DialogButtonClickedListener) context;
        } else {
            throw new ClassCastException(context.toString()
                    + " must implement DeleteDatabaseDialog.DialogButtonClickedListener");
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(R.string.dialog_title_delete_database)
                .setPositiveButton(R.string.btn_text_ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        listener.onDeleteOkClicked();
                    }
                })
                .setNegativeButton(R.string.btn_text_cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // User cancelled the dialog
                        listener.onDeleteCancelClicked();
                    }
                });
        // Create the AlertDialog object and return it
        return builder.create();
    }

    @Override
    public void dismiss() {
        super.dismiss();
        Log.d(LOG_TAG,"dialog gets dismissed");
    }
}

