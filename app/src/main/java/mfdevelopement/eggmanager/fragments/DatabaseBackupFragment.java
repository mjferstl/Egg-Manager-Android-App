package mfdevelopement.eggmanager.fragments;

import android.Manifest;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
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
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.File;
import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import mfdevelopement.eggmanager.R;
import mfdevelopement.eggmanager.activities.MainNavigationActivity;
import mfdevelopement.eggmanager.data_models.DailyBalance;
import mfdevelopement.eggmanager.data_models.DatabaseBackup;
import mfdevelopement.eggmanager.list_adapters.DatabaseBackupListAdapter;
import mfdevelopement.eggmanager.utils.AppNotificationManager;
import mfdevelopement.eggmanager.utils.FileUtil;
import mfdevelopement.eggmanager.utils.JSONUtil;
import mfdevelopement.eggmanager.viewmodels.SharedViewModel;

public class DatabaseBackupFragment extends Fragment {

    // LOG_TAG containing the name of the current class for debugging purpose
    private final String LOG_TAG = "DatabaseImportExportAct";

    // Locale is ENGLISH for importing and exporting data
    private final Locale stringFormatLocale = Locale.ENGLISH;

    // get path to the directory, where the EggManager backup files are stored
    private static final String publicDataDir = Environment.getExternalStorageDirectory().getPath();

    // View Model
    private SharedViewModel viewModel;

    // id of container for showing Snackbars
    private int idSnackbarContainer;

    // request codes
    private final int REQUEST_CODE_WRITE_EXTERNAL_STORAGE_PERMISSION = 20;

    private View mainRoot;
    private DatabaseBackupListAdapter adapter;

    private List<DailyBalance> allDailyBalances;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_database_backup, container,false);
        mainRoot = root;

        // get the view model
        viewModel = new ViewModelProvider(this).get(SharedViewModel.class);
        idSnackbarContainer = R.id.database_import_export_container;

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

    private void initListeners() {
        // add BackupListener
        // when the user changes the sorting order, then the recycler view needs to be updated manually
        if (getActivity() != null)
            ((MainNavigationActivity)getActivity()).setBackupListener(new MainNavigationActivity.BackupListener() {

                @Override
                public void onBackupImportClicked(int position) {
                    DatabaseBackup backup = adapter.getItem(position);
                    Log.d(LOG_TAG,"user wants to import the backup with the name \"" + backup.getBackupName() + "\"");
                    importBackup(backup);
                }

                @Override
                public void onBackupDeleteClicked(int position) {
                    DatabaseBackup backup = adapter.getItem(position);
                    Log.d(LOG_TAG,"user wants to delete the backup with the name \"" + backup.getBackupName() + "\"");
                    deleteFile(backup.getFilename());
                }

                @Override
                public void onBackupCreated(String filename) {
                    updateRecyclerView();
                }
            });
    }

    private void initRecyclerView() {
        List<DatabaseBackup> backupList = getBackupFiles();

        RecyclerView recyclerView = mainRoot.findViewById(R.id.recv_database_backup);
        adapter = new DatabaseBackupListAdapter(getContext(), backupList);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);
    }

    private void updateRecyclerView() {
        List<DatabaseBackup> backupList = getBackupFiles();
        adapter.setDatabaseBackupList(backupList);

        LinearLayout linearLayoutRecyclerView = mainRoot.findViewById(R.id.linLay_database_backup_recv);
        LinearLayout linearLayoutRecyclerViewEmpty = mainRoot.findViewById(R.id.linLay_database_backup_recv_empty);
        TextView txtv_recv_empty = mainRoot.findViewById(R.id.txtv_database_backup_recv_empty);

        if (adapter.getItemCount() == 0) {
            linearLayoutRecyclerView.setVisibility(View.GONE);
            linearLayoutRecyclerViewEmpty.setVisibility(View.VISIBLE);
            txtv_recv_empty.setOnClickListener(v -> showDialogCreateBackup());
        } else {
            linearLayoutRecyclerView.setVisibility(View.VISIBLE);
            linearLayoutRecyclerViewEmpty.setVisibility(View.GONE);
        }
    }

    private void deleteFile(String filename) {

        // Delete the file
        File file = new File(publicDataDir, filename);
        boolean fileDeleted = file.delete();

        // show a snackbar to inform the user if deleting the file was not finished successfully
        if (!fileDeleted)
            Snackbar.make(mainRoot.findViewById(idSnackbarContainer), "Fehler beim Löschen der Datensicherung", Snackbar.LENGTH_SHORT).show();

        // update the entries in the recycler view
        updateRecyclerView();
    }

    /**
     * Import data from a backup file and overwrite existing entries in the database
     * @param backup DatabaseBackup
     */
    private void importBackup(DatabaseBackup backup) {
        Log.d(LOG_TAG,"importBackup()");

        // load the backup in an async task
        new importBackupAsyncTask((MainNavigationActivity) getActivity()).execute(backup.getFilename());
    }


    /**
     * check if the permission to write to the external storage is given
     * @return flag, if the permission is granted
     */
    private boolean isWritePermissionGranted() {
        // Check whether this app has write external storage permission or not.
        int writeExternalStoragePermission = PackageManager.PERMISSION_DENIED;
        if (getActivity() != null) {
            writeExternalStoragePermission = ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE);
        } else {
            Log.d(LOG_TAG,"isWritePermissionGranted(): failed, because getActivity = null");
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
                Log.d(LOG_TAG,"requestWritePermission(): requesting user to grant write permission");
            } else {
                Log.d(LOG_TAG,"requestWritePermission(): write permission is granted");
            }
        } else {
            Log.e(LOG_TAG,"requestWritePermission(): getActivity = null");
        }
    }

    /**
     * create a JSONArray from a list of DailyBalance objects
     * @param dailyBalanceList List of DailyBalance objects
     * @return JSONArray
     */
    private JSONArray createJsonArrayFromDailyBalance(@NonNull List<DailyBalance> dailyBalanceList) {

        JSONArray jsonArray = new JSONArray();

        for (int i = 0; i < dailyBalanceList.size(); i++) {
            try {
                jsonArray.put(i, dailyBalanceList.get(i).toJSON());
            } catch (JSONException e) {
                e.printStackTrace();
                Log.e(LOG_TAG, "createJsonArrayFromDailyBalance(): Error encountered while creating JSONArray");
            }
        }
        return jsonArray;
    }

    /**
     *  Checks if external storage is available for read and write
     */
    private boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(state);
    }

    /**
     *  Checks if external storage is available to at least read
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
            Log.d(LOG_TAG,"create new Backup with title: \"" + newBackupName + "\"");
            createNewBackup(newBackupName);
        });
        alertDialog.setButton(DialogInterface.BUTTON_NEUTRAL,"Abbruch", (dialog, which) -> alertDialog.dismiss());

        alertDialog.show();
    }


    /**
     * Create a new backup file in JSON-Format containing all Entries of the database
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
                    // create a JSONArray
                    JSONArray jsonArray = createJsonArrayFromDailyBalance(allDailyBalances);

                    // save the JSONArray to file as a String
                    DatabaseBackup backup = new DatabaseBackup();
                    backup.setBackupName(backupName);
                    new createBackupAsyncTask((MainNavigationActivity) getActivity()).execute(backup.getFilename(), jsonArray.toString());
                }
            } else {
                // inform the user, that no write persmission is granted
                Snackbar.make(mainRoot.findViewById(R.id.database_import_export_container), getString(R.string.snackbar_permission_ext_storage_denied), Snackbar.LENGTH_SHORT).show();
            }
        } else {
            Snackbar.make(mainRoot.findViewById(idSnackbarContainer), getString(R.string.snackbar_no_external_storage), Snackbar.LENGTH_SHORT).show();
        }
    }

    /**
     * Get all EggManager backup files on the device
     * @return List<DatabaseBackup> containing all backups found
     */
    private List<DatabaseBackup> getBackupFiles() {

        String fileName;
        // List for storing the dates of the backup files
        List<DatabaseBackup> backupFiles = new ArrayList<>();

        if (isExternalStorageReadable()) {

            // get path, where the files are stored
            String path = Environment.getExternalStorageDirectory().getPath();
            Log.d(LOG_TAG, "getBackupFiles(): searching for backup files in the directory " + path);

            // get content of this directory
            File directory = new File(path);
            File[] files = directory.listFiles();

            for (File file : files) {

                // file name of the current file
                fileName = file.getName();

                // check if the file is a EggManager backup file
                if (DatabaseBackup.isEggManagerBackupFile(fileName)) {

                    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm", stringFormatLocale);
                    Log.d(LOG_TAG,"backup file: " + fileName + ", last modified: " + simpleDateFormat.format(file.lastModified()) + ", size: " + file.length() + " bytes");

                    // add the date to the list
                    backupFiles.add(new DatabaseBackup(file));
                }
            }

            // sort in descending order
            Collections.sort(backupFiles);

            Log.d(LOG_TAG, "Found " + backupFiles.size() + " backup files");
        } else {
            Log.e(LOG_TAG,"external storage is not readable");
            Snackbar.make(mainRoot.findViewById(idSnackbarContainer),"Berechtigung zum Lesen des Speichers fehlt", Snackbar.LENGTH_LONG).show();
        }

        return backupFiles;
    }


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
            Log.d(LOG_TAG,"user wants to create a new backup");
            showDialogCreateBackup();
        });
    }


    /**
     * Initialize observers for receiving LiveData
     */
    private void initObservers() {
        viewModel.getAllDailyBalances().observe(getViewLifecycleOwner(), dailyBalanceList -> allDailyBalances = dailyBalanceList);
    }


    private static class createBackupAsyncTask extends AsyncTask<String, Void, String> {

        private WeakReference<MainNavigationActivity> weakReference;
        private final String LOG_TAG = "createBackupAsyncTask";

        createBackupAsyncTask(MainNavigationActivity context) {
            weakReference = new WeakReference<>(context);
        }

        @Override
        protected String doInBackground(String... strings) {
            String filename = strings[0];
            String content = strings[1];
            int status = FileUtil.writeContentToFile(filename, content);

            MainNavigationActivity activity = weakReference.get();
            if (activity == null || activity.isFinishing()) {
                Log.e(LOG_TAG, "activity == null or activity is finishing");
                return "";
            }

            if (status == 0)
                return filename;
            else
                return "";
        }

        @Override
        protected void onPostExecute(String filename) {
            super.onPostExecute(filename);

            MainNavigationActivity activity = weakReference.get();
            if (activity == null || activity.isFinishing())
                return;

            Log.d(LOG_TAG,"sending name of the created file \"" + filename + "\" to the main activity");
            activity.onBackupCreated(filename);
        }
    }

    private static class importBackupAsyncTask extends AsyncTask<String, Void, Integer> {

        private WeakReference<MainNavigationActivity> weakReference;
        private final String LOG_TAG = "importBackupAsyncTask";

        private AppNotificationManager appNotificationManager;

        private final SimpleDateFormat sdf_date = new SimpleDateFormat("dd.MM.yyyy", Locale.ENGLISH);

        importBackupAsyncTask(MainNavigationActivity context) {
            weakReference = new WeakReference<>(context);
            appNotificationManager = new AppNotificationManager(context);
            appNotificationManager.createNotificationChannel();
        }

        @Override
        protected Integer doInBackground(String... strings) {
            String filename = strings[0];

            MainNavigationActivity activity = weakReference.get();

            // read the content of the file
            File file = new File(publicDataDir, filename);
            String content = FileUtil.readFile(file);

            // convert String to JSONObject
            JSONArray jsonArray = JSONUtil.getJSONObject(content);

            // check if data was loaded
            if (jsonArray == null || jsonArray.length() == 0)
                return 0;

            // import data to the database. If data exists, it gets overwritten
            List<DailyBalance> dailyBalances = DailyBalance.getDailyBalanceFromJSON(jsonArray);

            Log.d(LOG_TAG,"backup contains " + dailyBalances.size() + " entries");

            appNotificationManager.showImportNotification(filename);

            for (int i = 0; i < dailyBalances.size(); i++) {
                Log.d(LOG_TAG, "Date of loaded data: " + sdf_date.format(dailyBalances.get(i).getDate()));
                activity.updateDailyBalance(dailyBalances.get(i));
                appNotificationManager.updateImportNotification(((i+1)*100)/dailyBalances.size());

                try {
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            return dailyBalances.size();
        }

        @Override
        protected void onPostExecute(Integer numItems) {
            super.onPostExecute(numItems);

            if (numItems > 0)
                appNotificationManager.setImportNotificationFinished("Import abgeschlossen.\n" + numItems + " Einträge eingelesen.");
            else
                appNotificationManager.setImportNotificationFinished("Fehler beim Einlesen der Daten. Keine neuen Daten importiert.");
        }
    }
}
