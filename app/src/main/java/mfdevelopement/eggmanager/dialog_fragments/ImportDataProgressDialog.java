package mfdevelopement.eggmanager.dialog_fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.google.android.material.progressindicator.LinearProgressIndicator;

import mfdevelopement.eggmanager.R;

public class ImportDataProgressDialog extends BasicDialogFragment {

    private static final String LOG_TAG = "ImportDataProgressDialog";

    private LinearProgressIndicator progress = null;
    private Button btnOk = null;

    // Variables for storing the initial values, which will be loaded when creating the view
    private Integer initialProgress = null;
    private Integer initialMaxProgress = null;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_dialog_import_progess, container, false);
        // Initialize the progress bar
        this.progress = v.findViewById(R.id.progress_import_data);
        if (this.initialProgress != null)
            this.progress.setProgress(this.initialProgress);
        if (this.initialMaxProgress != null)
            this.progress.setMax(this.initialMaxProgress);

        // Initialize the button
        this.btnOk = v.findViewById(R.id.btn_import_data_dialog_ok);
        this.btnOk.setEnabled(this.progress.getProgress() >= this.progress.getMax());
        this.btnOk.setOnClickListener(view -> dismiss());

        // Make the dialog not cancelable
        setCancelable(false);

        // Return the view
        return v;
    }

    @Override
    protected String getLogTag() {
        return LOG_TAG;
    }

    public void setProgress(int progress) {
        if (this.progress != null) {
            this.progress.setProgress(progress);
            this.btnOk.setEnabled(progress >= this.progress.getMax());
        } else {
            this.initialProgress = progress;
        }
    }

    public void setMaxProgress(int maxProgress) {
        if (this.progress != null) {
            this.progress.setMax(maxProgress);
        } else {
            this.initialMaxProgress = maxProgress;
        }
    }
}
