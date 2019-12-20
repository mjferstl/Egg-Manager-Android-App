package mfdevelopement.eggmanager.fragments;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import mfdevelopement.eggmanager.R;
import mfdevelopement.eggmanager.activities.MainNavigationActivity;
import mfdevelopement.eggmanager.data_models.DailyBalance;
import mfdevelopement.eggmanager.data_models.DatabaseBackup;
import mfdevelopement.eggmanager.dialog_fragments.BackupOptionsDialogFragment;
import mfdevelopement.eggmanager.list_adapters.DatabaseBackupListAdapter;
import mfdevelopement.eggmanager.viewmodels.SharedViewModel;

public class DatabseBackupFragment extends Fragment {

    // LOG_TAG containing the name of the current class for debugging purpose
    private final String LOG_TAG = "DatabaseImportExportAct";

    // Locale is ENGLISH for importing and exporting data
    private final Locale stringFormatLocale = Locale.ENGLISH;

    // get path to the directory, where the EggManager backup files are stored
    private final String publicDataDir = Environment.getExternalStorageDirectory().getPath();


    // View Model
    private SharedViewModel viewModel;

    // id of container for showing Snackbars
    private int idSnackbarContainer;

    // parts of the filename of the exported file
    private final SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss", stringFormatLocale);
    private final SimpleDateFormat sdf_date = new SimpleDateFormat("dd.MM.yyyy", stringFormatLocale);

    private final String exportFilePrefix = "EggManager_backup_";
    private final String exportFileDataType = ".emb"; // EggManagerBackup

    // request codes
    private final int REQUEST_CODE_WRITE_EXTERNAL_STORAGE_PERMISSION = 20;

    private View mainRoot;
    private DatabaseBackupListAdapter adapter;

    private BackupOptionsDialogFragment backupOptionsDialog;

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

    private void initListeners() {
        // add BackupSelectedListener
        // when the user changes the sorting order, then the recycler view needs to be updated manually
        if (getActivity() != null)
            ((MainNavigationActivity)getActivity()).setBackupSelectedListener(new MainNavigationActivity.BackupSelectedListener() {
                @Override
                public void onBackupSelected(DatabaseBackup backup) {
                    showBackupOptionsDialog(backup);
                }

                @Override
                public void onBackupDeleteClicked(DatabaseBackup backup) {
                    Log.d(LOG_TAG,"user wants to delete the backup with the name \"" + backup.getName() + "\"");
                    backupOptionsDialog.dismiss();
                    deleteFile(backup.getFilename());
                }

                @Override
                public void onBackupImportClicked(DatabaseBackup backup) {
                    Log.d(LOG_TAG,"user wants to import the backup with the name \"" + backup.getName() + "\"");
                    backupOptionsDialog.dismiss();
                    importBackup(backup);
                }
            });
    }

    private void createNewBackup() {
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
                    exportDataToFile(backup.getFilename(), jsonArray.toString());
                    updateRecyclerView();
                }
            } else {
                // inform the user, that no write persmission is granted
                Snackbar.make(mainRoot.findViewById(R.id.database_import_export_container), getString(R.string.snackbar_permission_ext_storage_denied), Snackbar.LENGTH_SHORT).show();
            }
        } else {
            Snackbar.make(mainRoot.findViewById(idSnackbarContainer), getString(R.string.snackbar_no_external_storage), Snackbar.LENGTH_SHORT).show();
        }
    }

    private void showBackupOptionsDialog(DatabaseBackup backup) {
        Log.d(LOG_TAG,"user clicked on item with name " + backup.getName());

        // DialogFragment.show() will take care of adding the fragment
        // in a transaction.  We also want to remove any currently showing
        // dialog, so make our own transaction and take care of that here.
        if (getActivity() != null) {
            // FragmentActivity.getSupportFragmentManager()
            FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
            Fragment prev = getActivity().getSupportFragmentManager().findFragmentByTag("dialog");
            if (prev != null) {
                ft.remove(prev);
            }
            ft.addToBackStack(null);

            // Create and show the dialog
            backupOptionsDialog = BackupOptionsDialogFragment.newInstance(backup);
            backupOptionsDialog.show(ft, "dialog");
        } else {
            Log.e(LOG_TAG,"getActivity() = null");
        }
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
    }

    private void deleteFile(String filename) {

        // Delete the file
        File file = new File(publicDataDir, filename);

        File directory = new File(publicDataDir);
        File[] files = directory.listFiles();

        for (File f : files)
            Log.d(LOG_TAG,"File in " + publicDataDir + ": " + f.getName());

        Log.d(LOG_TAG,"check if the file " + file.getAbsolutePath() + " exists: " + file.exists());
        boolean fileDeleted = file.delete();

        // create snackbar text depending on the result of the delete process
        String snackbarText = "";
        if (fileDeleted)
            snackbarText = "Datei gelöscht";
        else
            snackbarText = "Fehler beim Löschen der Datei";

        // show a snackbar to inform the user about the delete progress
        Snackbar.make(mainRoot.findViewById(idSnackbarContainer), snackbarText, Snackbar.LENGTH_SHORT).show();

        // update the entries in the recycler view
        updateRecyclerView();
    }

    private void importBackup(DatabaseBackup backup) {
        String filename = backup.getFilename();

        // read the content of the file
        File file = new File(publicDataDir, filename);
        String content = readFile(file);

        // convert String to JSONObject
        JSONArray jsonArray = getJSONObject(content);

        // check if data was loaded
        if (jsonArray == null || jsonArray.length() == 0) {
            Snackbar.make(mainRoot.findViewById(idSnackbarContainer), getString(R.string.snackbar_error_loading_data_from_database), Snackbar.LENGTH_SHORT).show();
            return;
        }

        // import data to the database. If data exists, it gets overwritten
        List<DailyBalance> dailyBalances = getDailyBalancFromJSON(jsonArray);
        for (int i = 0; i < dailyBalances.size(); i++) {
            Log.d(LOG_TAG, "Date of loaded data: " + sdf_date.format(dailyBalances.get(i).getDate()));
            viewModel.insert(dailyBalances.get(i));
        }

        // inform the user about the process
        Snackbar.make(mainRoot.findViewById(idSnackbarContainer), String.format(getString(R.string.snackbar_data_imported_successfully), dailyBalances.size()), Snackbar.LENGTH_SHORT).show();
    }

    /**
     * parse a JSONArray to a list of DailyBalance objects
     * @param jsonArray JSONArray containing data for a DailyBalance
     * @return List<DailyBalance> list of DailyBalance objects
     */
    private List<DailyBalance> getDailyBalancFromJSON(JSONArray jsonArray) {

        List<DailyBalance> dailyBalanceList = new ArrayList<>();

        JSONObject item;

        for (int i = 0; i < jsonArray.length(); i++) {
            try {
                item = jsonArray.getJSONObject(i);
                String dateKey = item.getString(DailyBalance.COL_DATE_PRIMARY_KEY);
                int eggsCollected = item.getInt(DailyBalance.COL_EGGS_COLLECTED_NAME);
                int eggsSold = item.getInt(DailyBalance.COL_EGGS_SOLD_NAME);
                double pricePerEgg = item.getDouble(DailyBalance.COL_PRICE_PER_EGG);
                int numHens = item.getInt(DailyBalance.COL_NUMBER_HENS);

                dailyBalanceList.add(new DailyBalance(dateKey, eggsCollected, eggsSold, pricePerEgg, numHens));
            } catch (JSONException e) {
                e.printStackTrace();
                return dailyBalanceList;
            }
        }

        return dailyBalanceList;
    }

    /**
     * parse a String to a JSONObject
     * @param content String in JSON notation
     * @return JSONArray
     */
    private JSONArray getJSONObject(String content) {

        // create a new JSONObject
        JSONArray jsonObject = new JSONArray();

        // convert Stirng to JSONObject
        try {
            jsonObject = new JSONArray(content);
        } catch (JSONException e) {
            Log.e(LOG_TAG, "getJSONObject::error when converting String to JSONObject");
        }
        return jsonObject;
    }

    /**
     * read the content of a File on the device
     * @param file File to be read
     * @return String containing the content of the file
     */
    private String readFile(File file) {

        StringBuilder builder = new StringBuilder();
        Log.e(LOG_TAG, "readFile::start to read file " + file.getAbsolutePath());

        try {
            BufferedReader br = new BufferedReader(new FileReader(file));
            String line;
            while ((line = br.readLine()) != null) {
                builder.append(line);
                builder.append("\n");
            }
            br.close();
        } catch (Exception e) {
            Log.e(LOG_TAG, "readFile::error while reading file..");
            e.printStackTrace();
        }

        return builder.toString();
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
                jsonArray.put(i, createJsonFromDailyBalance(dailyBalanceList.get(i)));
            } catch (JSONException e) {
                e.printStackTrace();
                Log.e(LOG_TAG, "createJsonArrayFromDailyBalance::Error occurred while creating JSONArray");
            }
        }
        return jsonArray;
    }

    /**
     * create a JSONObject from a DailyBalance obejct
     * @param dailyBalance DailyBalance object
     * @return JSONObject representing the DailyBalance object in JSON notation
     */
    private JSONObject createJsonFromDailyBalance(@NonNull DailyBalance dailyBalance) {

        // new JSONObject
        JSONObject jsonObject = new JSONObject();

        try {
            jsonObject.put(DailyBalance.COL_DATE_PRIMARY_KEY, dailyBalance.getDateKey());
            jsonObject.put(DailyBalance.COL_EGGS_COLLECTED_NAME, dailyBalance.getEggsCollected());
            jsonObject.put(DailyBalance.COL_EGGS_SOLD_NAME, dailyBalance.getEggsSold());
            jsonObject.put(DailyBalance.COL_PRICE_PER_EGG, dailyBalance.getPricePerEgg());
            jsonObject.put(DailyBalance.COL_MONEY_EARNED, dailyBalance.getMoneyEarned());
            jsonObject.put(DailyBalance.COL_NUMBER_HENS, dailyBalance.getNumHens());

        } catch (JSONException e) {
            e.printStackTrace();
            Log.d(LOG_TAG, "Error when adding fields to JSONObject");
        }

        return jsonObject;
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
     * create a file on the external storage of the device and add content to the file
     * @param fileName Name of the file to be stored. The file will be created on the external storage of the device
     * @param content String containing the content of the file
     */
    private void exportDataToFile(String fileName, String content) {

        // get path
        String publicDataDir = Environment.getExternalStorageDirectory().getPath();

        // create a new file
        File newFile = new File(publicDataDir, fileName);

        // save content to the file
        try {
            FileWriter fw = new FileWriter(newFile);
            fw.write(content);
            fw.flush();
            fw.close();
            Log.d(LOG_TAG, "Created file " + newFile.getAbsolutePath() + " successfully");
            Snackbar.make(mainRoot.findViewById(R.id.database_import_export_container), getString(R.string.snackbar_data_exported_successfully), Snackbar.LENGTH_SHORT).show();
        }
        // inform the user, if the creation of the file failed
        catch (IOException e) {
            e.printStackTrace();
            Log.e(LOG_TAG, "exportDataToFile::creating the file " + fileName + " failed");
            Snackbar.make(mainRoot.findViewById(idSnackbarContainer), "Fehler beim Erstellen der Datei. Bitte den Entwickler kontaktieren", Snackbar.LENGTH_LONG).show();
        }
    }

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
                    Log.d(LOG_TAG, "Found a backup file: " + fileName);

                    // remove prefix and extension of the filename. The rest of the filename is just the date
                    String backupName = fileName.replace(exportFilePrefix, "").replace(exportFileDataType, "");

                    // create a new DataBase object
                    DatabaseBackup newBackupFile = new DatabaseBackup(backupName);
                    newBackupFile.setFilename(fileName);

                    // try to parse the date
                    try {
                        // parse the date
                        Calendar saveDate = Calendar.getInstance();
                        saveDate.setTime(DatabaseBackup.getDateFromFilename(fileName));
                        newBackupFile.setSaveDate(saveDate);

                        // add the date to the list
                        backupFiles.add(newBackupFile);
                    }
                    // handle Exception
                    catch (ParseException e) {
                        e.printStackTrace();
                        Snackbar.make(mainRoot.findViewById(idSnackbarContainer), "Es können nicht alle Backups angezeigt werden", Snackbar.LENGTH_SHORT).show();
                        Log.e(LOG_TAG, "getBackupFiles(): Fehler beim Parsen des Datums aus dem Dateinamen \"" + fileName + "\"");
                    }
                }
            }

            // sort in descending order
            Collections.sort(backupFiles);

            Log.d(LOG_TAG, "found " + backupFiles.size() + " backup files");
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

    private void initFab() {
        FloatingActionButton fab = mainRoot.findViewById(R.id.fab_database_content);
        fab.setOnClickListener(v -> {
            Log.d(LOG_TAG,"user wants to create a new backup");
            createNewBackup();
        });
    }

    private void initObservers() {
        viewModel.getAllDailyBalances().observe(getViewLifecycleOwner(), dailyBalanceList -> allDailyBalances = dailyBalanceList);
    }
}
