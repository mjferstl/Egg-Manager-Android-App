package mfdevelopement.eggmanager.fragments;

import android.Manifest;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import org.json.JSONArray;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import kotlinx.coroutines.Dispatchers;
import mfdevelopement.eggmanager.R;
import mfdevelopement.eggmanager.activities.MainNavigationActivity;
import mfdevelopement.eggmanager.coroutines.MyCoroutines;
import mfdevelopement.eggmanager.data_models.DatabaseBackup;
import mfdevelopement.eggmanager.data_models.daily_balance.DailyBalance;
import mfdevelopement.eggmanager.dialog_fragments.ImportBackupDialog;
import mfdevelopement.eggmanager.list_adapters.DatabaseBackupListAdapter;
import mfdevelopement.eggmanager.utils.BackupCreateCoroutine;
import mfdevelopement.eggmanager.utils.FileUtil;
import mfdevelopement.eggmanager.utils.JSONUtil;
import mfdevelopement.eggmanager.utils.notifications.DatabaseBackupImportNotificationManager;
import mfdevelopement.eggmanager.viewmodels.SharedViewModel;

public class DatabaseBackupFragment extends Fragment {

    // get path to the directory, where the EggManager backup files are stored
    private static String publicDataDir;
    // LOG_TAG containing the name of the current class for debugging purpose
    private final String LOG_TAG = "DatabaseImportExportAct";
    // Locale is ENGLISH for importing and exporting data
    private final Locale stringFormatLocale = Locale.ENGLISH;
    // request codes
    private final int REQUEST_CODE_WRITE_EXTERNAL_STORAGE_PERMISSION = 20;
    // View Model
    private SharedViewModel viewModel;
    // id of container for showing Snackbars
    private int idSnackbarContainer;
    private View mainRoot;
    private DatabaseBackupListAdapter adapter;

    private List<DailyBalance> allDailyBalances;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_database_backup, container, false);
        mainRoot = root;

        // get the view model
        viewModel = new ViewModelProvider(this).get(SharedViewModel.class);
        idSnackbarContainer = R.id.database_import_export_container;

        publicDataDir = FileUtil.getExternalDirPath(this.getContext());

        // Initialize UI Elements
        initRecyclerView();
        initFab();
        initListeners();
        initObservers();

        requestWritePermission();

        return root;
    }

    @Override
    public void onResume() {
        super.onResume();

        // update items in recyclerview, because files may have been deleted or added
        updateRecyclerView();
    }

    /**
     * Initialize listeners for items of the recyclerview, which can be clicked by the user
     * These listeners handle the execution of the corresponding tasks like importing a backup or deleting a backup
     */
    private void initListeners() {
        // add BackupListener
        // when the user changes the sorting order, then the recycler view needs to be updated manually
        if (getActivity() != null)
            ((MainNavigationActivity) getActivity()).setBackupListener(new MainNavigationActivity.BackupListener() {

                @Override
                public void onBackupImportClicked(int position) {
                    DatabaseBackup backup = adapter.getItem(position);
                    Log.d(LOG_TAG, "user wants to import the backup with the name \"" + backup.getBackupName() + "\"");
                    importBackup(backup);
                }

                @Override
                public void onBackupDeleteClicked(int position) {
                    DatabaseBackup backup = adapter.getItem(position);
                    Log.d(LOG_TAG, "user wants to delete the backup with the name \"" + backup.getBackupName() + "\"");
                    deleteFile(backup.getFilename());
                }

                @Override
                public void onBackupCreated(String filename) {
                    updateRecyclerView();
                }
            });
    }

    /**
     * Initialize the {@link RecyclerView}, which shows the available backups
     */
    private void initRecyclerView() {
        List<DatabaseBackup> backupList = getBackupFiles();

        RecyclerView recyclerView = mainRoot.findViewById(R.id.recv_database_backup);
        adapter = new DatabaseBackupListAdapter(getContext(), backupList);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
        recyclerView.setAdapter(adapter);
    }

    /**
     * Update the {@link RecyclerView}, which shows the available backup files
     */
    private void updateRecyclerView() {
        List<DatabaseBackup> backupList = getBackupFiles();
        adapter.setDatabaseBackupList(backupList);

        LinearLayout linearLayoutRecyclerView = mainRoot.findViewById(R.id.linLay_database_backup_recv);
        LinearLayout linearLayoutRecyclerViewEmpty = mainRoot.findViewById(R.id.linLay_database_backup_recv_empty);
        TextView txtvRecvEmpty = mainRoot.findViewById(R.id.txtv_database_backup_recv_empty);

        if (adapter.getItemCount() == 0) {
            linearLayoutRecyclerView.setVisibility(View.GONE);
            linearLayoutRecyclerViewEmpty.setVisibility(View.VISIBLE);
            txtvRecvEmpty.setOnClickListener(v -> showDialogCreateBackup());
        } else {
            linearLayoutRecyclerView.setVisibility(View.VISIBLE);
            linearLayoutRecyclerViewEmpty.setVisibility(View.GONE);
        }
    }

    /**
     * Delete a file from the device.
     * A {@link Snackbar} will be shown when the file can not be deleted
     *
     * @param filename Name of the file to be deleted
     */
    private void deleteFile(String filename) {

        // Delete the file
        File file = new File(publicDataDir, filename);
        boolean fileDeleted = file.delete();

        // show a Snackbar to inform the user if deleting the file was not finished successfully
        if (!fileDeleted)
            Snackbar.make(mainRoot.findViewById(idSnackbarContainer), getString(R.string.snackbar_error_delete_backup), Snackbar.LENGTH_SHORT).show();

        // update the entries in the recycler view
        updateRecyclerView();
    }

    /**
     * Import data from a backup file
     * This overwrites existing entries in the database
     *
     * @param backup DatabaseBackup object
     */
    private void importBackup(DatabaseBackup backup) {
        Log.d(LOG_TAG, "importBackup()");

        // TODO: Show a dialog to inform the user, how many items are in the backup and if there are any duplicates
        ImportBackupDialog dialog = ImportBackupDialog.newInstance(backup.getFilename());
        List<DailyBalance> existingDailyBalances = viewModel.getAllDailyBalances().getValue();
        dialog.setExistingDailyBalances(existingDailyBalances);

        // Handle button clicks on the dialog
        dialog.setOnButtonClickListener(new ImportBackupDialog.OnButtonClickListener() {
            @Override
            public void onOkClicked(boolean overwriteExisting) {
                MyCoroutines.Companion.doAsync(() -> {
                    importDataTask(backup, overwriteExisting);
                    return null;
                }, Dispatchers.getIO());
                dialog.dismiss();
            }

            @Override
            public void onCancelClicked() {
                String snackbarMessage = getString(R.string.snackbar_import_backup_cancelled);
                Snackbar.make(mainRoot, snackbarMessage, Snackbar.LENGTH_SHORT).show();
                dialog.dismiss();
            }
        });

        // Show the dialog
        if (getActivity() != null) {
            String dialogFragmentTag = "ImportBackupDialog";
            FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
            Fragment prev = getActivity().getSupportFragmentManager().findFragmentByTag(dialogFragmentTag);
            if (prev != null) {
                ft.remove(prev);
            }
            ft.addToBackStack(null);
            dialog.show(ft, dialogFragmentTag);
        }
    }

    /**
     * Task to import data from a backup file
     *
     * @param databaseBackup    DatabaseBackup object which contains information about the backup to be imported
     * @param overwriteExisting Flag to activate overwriting existing entries in the database
     */
    private void importDataTask(DatabaseBackup databaseBackup, boolean overwriteExisting) {
        final String logIdentifier = "importBackup():Coroutine: ";

        // Variables for the progress
        final int maxProgress = 5;
        int executionProgress = 0;

        // Create a notification
        DatabaseBackupImportNotificationManager notificationManager = new DatabaseBackupImportNotificationManager(this.getContext());
        notificationManager.showImportNotification(databaseBackup.getBackupName());

        // read the content of the file
        File file = new File(publicDataDir, databaseBackup.getFilename());
        String content = FileUtil.readFile(file);
        // Update notification progress
        notificationManager.updateImportNotification(++executionProgress, maxProgress);

        // convert String to JSONObject
        JSONArray jsonArray = JSONUtil.getJSONObject(content);
        // Update notification progress
        notificationManager.updateImportNotification(++executionProgress, maxProgress);

        // check if data was loaded
        if (jsonArray == null) {
            Log.e(LOG_TAG, logIdentifier + "jsonArray == null");
            return;
        }
        if (jsonArray.length() == 0) {
            Log.e(LOG_TAG, logIdentifier + "jsonArray.length() == 0");
            return;
        }

        // import data to the database. If data exists, it gets overwritten
        List<DailyBalance> loadedDailyBalances = DailyBalance.getDailyBalanceFromJSON(jsonArray);
        // Update notification progress
        notificationManager.updateImportNotification(++executionProgress, maxProgress);

        // Load the entries into the database
        Log.d(LOG_TAG, logIdentifier + "backup contains " + loadedDailyBalances.size() + " entries.");
        Log.d(LOG_TAG, logIdentifier + "starting the import...");

        // Store the dateKeys for the items, which should not be overwritten
        List<String> dailyBalanceDateKeysSkip = new ArrayList<>();
        if (!overwriteExisting) {
            List<DailyBalance> existingDailyBalances = viewModel.getAllDailyBalances().getValue();
            if (existingDailyBalances != null) {
                for (DailyBalance dailyBalance : existingDailyBalances) {
                    dailyBalanceDateKeysSkip.add(dailyBalance.getDateKey());
                }
            }
        }

        long importCounter = 0;
        for (int i = 0; i < loadedDailyBalances.size(); i++) {
            DailyBalance currentDailyBalance = loadedDailyBalances.get(i);
            if (!dailyBalanceDateKeysSkip.contains(currentDailyBalance.getDateKey())) {
                viewModel.insert(currentDailyBalance);
                importCounter++;
            }
            int remainingProgress = Math.max(1, maxProgress - executionProgress);
            float progress = executionProgress + ((i + 1) / (float) loadedDailyBalances.size() * (float) remainingProgress);
            notificationManager.updateImportNotification(progress, maxProgress);
        }

        Log.d(LOG_TAG, String.format(logIdentifier + "import of %d items finished", importCounter));
        String notificationMessage = String.format(Locale.getDefault(), getString(R.string.notification_backup_import_finished), importCounter);
        notificationManager.setImportNotificationFinished(notificationMessage);
    }


    /**
     * check if the permission to write to the external storage is given
     *
     * @return flag, if the permission is granted
     */
    private boolean isWritePermissionGranted() {
        // Check whether this app has write external storage permission or not.
        int writeExternalStoragePermission = PackageManager.PERMISSION_DENIED;
        if (getActivity() != null) {
            writeExternalStoragePermission = ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE);
        } else {
            Log.d(LOG_TAG, "isWritePermissionGranted(): failed, because getActivity = null");
        }

        return (writeExternalStoragePermission == PackageManager.PERMISSION_GRANTED);
    }

    /**
     * if the permission to write to external storage is not given yet, a dialog requesting the permission is shown
     */
    private void requestWritePermission() {

        if (getActivity() != null) {
            // If write external storage permission is not granted
            if (!isWritePermissionGranted()) {
                // Request user to grant write external storage permission.
                ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_CODE_WRITE_EXTERNAL_STORAGE_PERMISSION);
                Log.d(LOG_TAG, "requestWritePermission(): requesting user to grant write permission");
            } else {
                Log.d(LOG_TAG, "requestWritePermission(): write permission is granted");
            }
        } else {
            Log.e(LOG_TAG, "requestWritePermission(): getActivity = null");
        }
    }

    /**
     * Checks if external storage is available for read and write
     */
    private boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(state);
    }

    /**
     * Checks if external storage is available to at least read
     */
    private boolean isExternalStorageReadable() {
        String state = Environment.getExternalStorageState();
        return (Environment.MEDIA_MOUNTED.equals(state) ||
                Environment.MEDIA_MOUNTED_READ_ONLY.equals(state));
    }

    /**
     * show an Alert Dialog containing a EditText field for entering the name of the backup
     */
    private void showDialogCreateBackup() {

        if (getActivity() == null)
            return;

        // create an EditText field for entering a backup name
        float factor = getActivity().getResources().getDisplayMetrics().density;
        final EditText input = new EditText(getActivity());
        input.setHint("Names des Backups");
        int small_margin = (int) (getActivity().getResources().getDimension(R.dimen.small_padding) * factor);
        int large_margin = (int) (getActivity().getResources().getDimension(R.dimen.large_padding) * factor);
        int medium_margin = (small_margin + large_margin) / 2;

        // create a new AlertDialog, which contains the EditText field
        final AlertDialog alertDialog = new AlertDialog.Builder(getActivity()).create();
        alertDialog.setTitle("Neue Datensicherung");
        alertDialog.setMessage("Geben Sie einen Namen für das Backup ein");
        alertDialog.setView(input, medium_margin, 0, medium_margin, 0);
        alertDialog.setButton(DialogInterface.BUTTON_POSITIVE, "Erstellen", (dialog, which) -> {
            String newBackupName = input.getText().toString().trim();
            Log.d(LOG_TAG, "create new Backup with title: \"" + newBackupName + "\"");
            createNewBackup(newBackupName);
        });
        alertDialog.setButton(DialogInterface.BUTTON_NEUTRAL, "Abbruch", (dialog, which) -> alertDialog.dismiss());

        alertDialog.show();
    }


    /**
     * Create a new backup file in JSON-Format containing all Entries of the database
     *
     * @param backupName Name of the backup used for the filename and for displaying in the GUI
     */
    private void createNewBackup(String backupName) {

        requestWritePermission();

        if (isExternalStorageWritable()) {

            if (isWritePermissionGranted()) {

                // if data was not loaded, the list == null
                if (allDailyBalances == null)
                    Snackbar.make(mainRoot.findViewById(idSnackbarContainer), getString(R.string.snackbar_error_loading_data_from_database), Snackbar.LENGTH_LONG).show();
                else {
                    BackupCreateCoroutine cor = new BackupCreateCoroutine();
                    cor.addBackupCreateListener(new BackupCreateCoroutine.BackupCreateListener() {
                        @Override
                        public void onSuccess() {
                            Log.d(LOG_TAG, "Successfully created backup file " + backupName);
                            updateRecyclerView();
                        }

                        @Override
                        public void onFailure() {
                            Log.e(LOG_TAG, "Could not create backup file " + backupName);
                        }
                    });
                    cor.create(backupName, allDailyBalances, getContext());
                    // Write file in a coroutine
                    /*MyCoroutines.Companion.doAsync(() -> {

                        // Create a new backup
                        DatabaseBackup backup = new DatabaseBackup();
                        backup.setBackupName(backupName);

                        // Create a notification
                        DatabaseBackupCreateNotificationManager notificationManager = new DatabaseBackupCreateNotificationManager(this.getContext());
                        notificationManager.showFileCreateNotification(backup.getFilename());

                        // Create a JSONArray and write it to the file
                        JSONArray jsonArray = createJsonArrayFromDailyBalance(allDailyBalances);
                        int status = FileUtil.writeContentToFile(getContext(), backup.getFilename(), jsonArray.toString());

                        // Update the notification
                        String msg = String.format(Locale.getDefault(), "Datensicherung \"%s\" mit %d Einträgen erstellt.", backup.getBackupName(), allDailyBalances.size());
                        notificationManager.setFileCreateNotificationFinished(msg);

                        if (status == 0) {
                            if (getActivity() != null) {
                                ((MainNavigationActivity) getActivity()).onBackupCreated(backup.getFilename());
                                updateRecyclerView();
                            } else {
                                Log.d(LOG_TAG, "createNewBackup(): getActivity returned null");
                            }
                        } else {
                            Log.e(LOG_TAG, "Could not create backup file " + backup.getFilename());
                        }
                        return null;
                    }, Dispatchers.getIO());*/
                }
            } else {
                // inform the user, that no write permission is granted
                Snackbar.make(mainRoot.findViewById(R.id.database_import_export_container), getString(R.string.snackbar_permission_ext_storage_denied), Snackbar.LENGTH_SHORT).show();
            }
        } else {
            Snackbar.make(mainRoot.findViewById(idSnackbarContainer), getString(R.string.snackbar_no_external_storage), Snackbar.LENGTH_SHORT).show();
        }
    }

    /**
     * Get all EggManager backup files on the device
     *
     * @return List<DatabaseBackup> containing all backups found
     */
    private List<DatabaseBackup> getBackupFiles() {

        String fileName;
        // List for storing the dates of the backup files
        List<DatabaseBackup> backupFiles = new ArrayList<>();

        if (isExternalStorageReadable()) {

            // get path, where the files are stored
            String path = FileUtil.getExternalDirPath(this.getContext());
            Log.d(LOG_TAG, "getBackupFiles(): searching for backup files in the directory " + path);

            // get content of this directory
            File directory = new File(path);
            File[] files = directory.listFiles();

            if (files == null) {
                Log.e(LOG_TAG, "Files[]  is null");
            } else {
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm", stringFormatLocale);
                for (File file : files) {

                    // file name of the current file
                    fileName = file.getName();

                    // check if the file is a EggManager backup file
                    if (DatabaseBackup.isEggManagerBackupFile(fileName)) {
                        Log.d(LOG_TAG, "backup file: " + fileName + ", last modified: " + simpleDateFormat.format(file.lastModified()) + ", size: " + file.length() + " bytes");

                        // add the date to the list
                        backupFiles.add(new DatabaseBackup(file));
                    }
                }

                // sort in descending order
                Collections.sort(backupFiles);
                Log.d(LOG_TAG, "Found " + backupFiles.size() + " backup files");
            }
        } else {
            Log.e(LOG_TAG, "external storage is not readable");
            Snackbar.make(mainRoot.findViewById(idSnackbarContainer), "Berechtigung zum Lesen des Speichers fehlt", Snackbar.LENGTH_LONG).show();
        }

        return backupFiles;
    }

    // TODO: replace deprecated method
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_CODE_WRITE_EXTERNAL_STORAGE_PERMISSION) {
            int grantResultsLength = grantResults.length;
            if (grantResultsLength > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Snackbar.make(mainRoot.findViewById(idSnackbarContainer), getString(R.string.snackbar_permission_ext_storage_granted), Snackbar.LENGTH_SHORT).show();
            } else {
                Snackbar.make(mainRoot.findViewById(idSnackbarContainer), getString(R.string.snackbar_permission_ext_storage_denied), Snackbar.LENGTH_SHORT).show();
            }
        }
    }

    /**
     * Initialize the Floating Action Button
     */
    private void initFab() {
        FloatingActionButton fab = mainRoot.findViewById(R.id.fab_database_content);
        fab.setOnClickListener(v -> {
            Log.d(LOG_TAG, "user wants to create a new backup");
            showDialogCreateBackup();
        });
    }

    /**
     * Initialize observers for receiving LiveData
     */
    private void initObservers() {
        viewModel.getAllDailyBalances().observe(getViewLifecycleOwner(), dailyBalanceList -> allDailyBalances = dailyBalanceList);
    }
}
