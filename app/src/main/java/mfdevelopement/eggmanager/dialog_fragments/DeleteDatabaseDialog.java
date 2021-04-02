package mfdevelopement.eggmanager.dialog_fragments;


import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;

import mfdevelopement.eggmanager.R;

public class DeleteDatabaseDialog extends DialogFragment {

    /**
     * String to be used for identification in log messages
     */
    private final static String LOG_TAG = "DeleteDatabaseDialog";

    /**
     * listener to provide the interaction between activity and this dialog
     */
    private DialogButtonClickedListener listener;

    /**
     * Interface containing functions to be called when the user clicks on buttons at the dialog UI
     */
    public interface DialogButtonClickedListener {
        // method to be called when the user clicks on the "OK" button
        void onDeleteOkClicked();

        // method to be called when the user clicks on the "Cancel" button
        void onDeleteCancelClicked();
    }

    /**
     * Create a new instance of SortingDialogFragment
     */
    public static DeleteDatabaseDialog newInstance() {
        Log.d(LOG_TAG, "creating new instance of DeleteDatabaseDialog");
        return new DeleteDatabaseDialog();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);

        // initialize the listener
        // the calling instance needs to implement the interface DeleteDatabaseDialog.DialogButtonClickedListener
        // Otherwise an Exception will be thrown
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
                .setPositiveButton(R.string.btn_text_ok, (dialog, id) -> listener.onDeleteOkClicked())
                .setNegativeButton(R.string.btn_text_cancel, (dialog, id) -> {
                    // User cancelled the dialog
                    listener.onDeleteCancelClicked();
                });
        // Create the AlertDialog object and return it
        return builder.create();
    }

    /**
     * Method which will be called when the dialog gets dismissed
     */
    @Override
    public void dismiss() {
        super.dismiss();
        Log.d(LOG_TAG, "dialog gets dismissed");
    }
}

