package mfdevelopement.eggmanager.dialog_fragments;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import mfdevelopement.eggmanager.R;
import mfdevelopement.eggmanager.data_models.DatabaseBackup;

public class BackupOptionsDialogFragment extends DialogFragment {

    private static DatabaseBackup backup;
    private BackupOptionClickListener listener;

    private final String LOG_TAG = "BackupOptionsDialogFrag";

    public static final int OPTION_IMPORT = 1;
    public static final int OPTION_DELETE = 2;


    public interface BackupOptionClickListener {
        void onOptionClicked(int option, DatabaseBackup backup);
    }

    public static BackupOptionsDialogFragment newInstance(DatabaseBackup databaseBackup) {
        backup = databaseBackup;
        return new BackupOptionsDialogFragment();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);

        if (context instanceof BackupOptionClickListener) {
            listener = (BackupOptionClickListener) context;
        } else {
            throw new ClassCastException(context.toString()
                    + " must implement BackupOptionsDialogFragment.BackupOptionClickListener");
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_backup_options, container, false);

        Button btn_delete = v.findViewById(R.id.btn_backup_option_delete);
        btn_delete.setOnClickListener(view -> listener.onOptionClicked(OPTION_DELETE, backup));

        Button btn_import = v.findViewById(R.id.btn_backup_option_import);
        btn_import.setOnClickListener(view -> listener.onOptionClicked(OPTION_IMPORT, backup));

        return v;
    }

    @Override
    public void dismiss() {
        super.dismiss();
        Log.d(LOG_TAG,"dismissed dialog");
    }
}
