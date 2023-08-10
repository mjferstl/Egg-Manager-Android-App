package mfdevelopement.eggmanager.dialog_fragments;

import android.app.Dialog;
import android.util.Log;
import android.view.ViewGroup;
import android.view.WindowManager;

import androidx.fragment.app.DialogFragment;

public abstract class BasicDialogFragment extends DialogFragment {

    @Override
    public void onResume() {
        super.onResume();

        Dialog dialog = getDialog();
        if (dialog != null && dialog.getWindow() != null) {
            WindowManager.LayoutParams params = dialog.getWindow().getAttributes();
            params.width = ViewGroup.LayoutParams.MATCH_PARENT;
            dialog.getWindow().setAttributes(params);
        }
    }

    @Override
    public void dismiss() {
        super.dismiss();
        Log.d(getLogTag(), "dialog gets dismissed");
    }

    protected abstract String getLogTag();
}
