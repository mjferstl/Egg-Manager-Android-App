package mfdevelopement.eggmanager.dialog_fragments;

import android.app.Dialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;

import org.json.JSONArray;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import mfdevelopement.eggmanager.R;
import mfdevelopement.eggmanager.data_models.DatabaseBackup;
import mfdevelopement.eggmanager.data_models.daily_balance.DailyBalance;
import mfdevelopement.eggmanager.utils.FileUtil;
import mfdevelopement.eggmanager.utils.JSONUtil;

public class ImportBackupDialog extends DialogFragment {

    private static final String LOG_TAG = "ImportBackupDialog";

    private String backupFileName = null;
    private List<DailyBalance> existingDailyBalances = new ArrayList<>();

    public interface OnButtonClickListener {
        void onOkClicked(boolean overwriteExisting);

        void onCancelClicked();
    }

    private OnButtonClickListener listener = null;

    /**
     * Create a new instance of SortingDialogFragment
     */
    public static ImportBackupDialog newInstance(@NonNull String backupFileName) {
        Log.d(LOG_TAG, "creating new instance of ImportBackupDialog with backupFileName");
        ImportBackupDialog ibd = new ImportBackupDialog();
        ibd.backupFileName = backupFileName;
        return ibd;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_import_backup_dialog, container, false);
        TextView txtvBackupContent = v.findViewById(R.id.txtv_import_backup_dialog_content);
        TextView txtvBackupImportQuestion = v.findViewById(R.id.txtv_import_backup_dialog_question);

        // Find the buttons
        Button btnOk = v.findViewById(R.id.btn_import_backup_dialog_ok);
        Button btnCancel = v.findViewById(R.id.btn_import_backup_dialog_cancel);
        CheckBox checkBoxOverwriteExisting = v.findViewById(R.id.checkBox_import_backup_dialog_overwrite_existing_items);
        // Hide the checkbox by default
        checkBoxOverwriteExisting.setVisibility(View.GONE);

        // Add actions to the buttons
        btnOk.setOnClickListener(view -> {
            boolean overwriteExisting = checkBoxOverwriteExisting.isChecked() && (checkBoxOverwriteExisting.getVisibility() == View.VISIBLE);
            if (listener != null) listener.onOkClicked(overwriteExisting);
        });
        btnCancel.setOnClickListener(view -> {
            if (listener != null) listener.onCancelClicked();
        });

        String publicDataDir = FileUtil.getExternalDirPath(this.getContext());
        DatabaseBackup databaseBackup = new DatabaseBackup(new File(backupFileName));

        // read the content of the file
        File file = new File(publicDataDir, databaseBackup.getFilename());
        String content = FileUtil.readFile(file);

        // convert String to JSONObject
        JSONArray jsonArray = JSONUtil.getJSONObject(content);

        // check if data was loaded
        if (jsonArray == null) {
            String msg = String.format(Locale.getDefault(), getString(R.string.dialog_import_backup_text_cannot_load), backupFileName);
            txtvBackupContent.setText(msg);
            return v;
        }
        if (jsonArray.length() == 0) {
            txtvBackupContent.setText(getString(R.string.dialog_import_backup_text_no_items));
            txtvBackupImportQuestion.setVisibility(View.GONE);
            return v;
        }

        // import data to the database. If data exists, it gets overwritten
        List<DailyBalance> dailyBalances = DailyBalance.getDailyBalanceFromJSON(jsonArray);

        // Check if there are any items, which already exist in the database
        long numExistingItems = 0;
        if (!this.existingDailyBalances.isEmpty()) {
            List<String> dailyBalanceDateKeys = new ArrayList<>();
            for (DailyBalance db : existingDailyBalances) {
                dailyBalanceDateKeys.add(db.getDateKey());
            }

            for (DailyBalance importedDailyBalance : dailyBalances) {
                if (dailyBalanceDateKeys.contains(importedDailyBalance.getDateKey())) {
                    numExistingItems++;
                }
            }
        }


        if (numExistingItems == 0) {
            // Hide the checkbox
            checkBoxOverwriteExisting.setVisibility(View.GONE);

            String msg = String.format(Locale.getDefault(), getString(R.string.dialog_import_backup_text_wo_duplicates), dailyBalances.size());
            txtvBackupContent.setText(msg);
        } else {
            // Show the checkbox
            checkBoxOverwriteExisting.setVisibility(View.VISIBLE);

            String msg = String.format(Locale.getDefault(), getString(R.string.dialog_import_backup_text_w_duplicates), dailyBalances.size(), numExistingItems);
            txtvBackupContent.setText(msg);
        }

        return v;
    }

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
        Log.d(LOG_TAG, "dialog gets dismissed");
    }

    public void setOnButtonClickListener(OnButtonClickListener listener) {
        this.listener = listener;
    }

    public void setExistingDailyBalances(List<DailyBalance> dailyBalanceList) {
        this.existingDailyBalances = dailyBalanceList;
    }
}
