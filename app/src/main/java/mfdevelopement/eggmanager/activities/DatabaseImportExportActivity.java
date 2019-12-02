package mfdevelopement.eggmanager.activities;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

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
import java.util.Date;
import java.util.List;
import java.util.Locale;

import mfdevelopement.eggmanager.R;
import mfdevelopement.eggmanager.data_models.DailyBalance;
import mfdevelopement.eggmanager.viewmodels.DatabaseImportExportViewModel;

public class DatabaseImportExportActivity extends Fragment implements View.OnClickListener {

    // LOG_TAG containing the name of the current class for debugging purpose
    private final String LOG_TAG = "DatabaseImportExportAct";

    // Locale is ENGLISH for importing and exporting data
    private final Locale stringFormatLocale = Locale.ENGLISH;

    // View Model
    private DatabaseImportExportViewModel viewModel;

    // id of container for showing Snackbars
    private int idSnackbarContainer;

    // parts of the filename of the exported file
    private final SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault());
    private final SimpleDateFormat sdf_readable = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss", Locale.getDefault());
    private final SimpleDateFormat sdf_date = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault());
    private final String exportFilePrefix = "EggManager_backup_";
    private final String exportFileDataType = ".emb"; // EggManagerBackup

    //
    private Spinner importSpinner;

    // request codes
    private final int REQUEST_CODE_WRITE_EXTERNAL_STORAGE_PERMISSION = 20;

    private View mainRoot;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_backup, container,false);
        mainRoot = root;

        viewModel = new ViewModelProvider(this).get(DatabaseImportExportViewModel.class);
        idSnackbarContainer = R.id.database_import_export_container;

        initButtons(root);
        initSpinner();

        requestWritePermission();

        return root;
    }

/*    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_database_import_export);
        Toolbar toolbar = findViewById(R.id.import_export_toolbar);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        viewModel = new ViewModelProvider(this).get(DatabaseImportExportViewModel.class);
        idSnackbarContainer = R.id.database_import_export_container;

        initButtons();
        initSpinner();

        requestWritePermission();
    }*/

    /**
     * initialize the Spinner
     */
    private void initSpinner() {
        importSpinner = mainRoot.findViewById(R.id.spinner_import_data);
        updateSpinner();
    }

    /**
     * load data for the Spinner
     */
    private void updateSpinner() {
        LinearLayout linearLayoutImport = mainRoot.findViewById(R.id.linLay_import_data);

        List<Date> dateList = getDatesOfBackupFiles();

        // if the list is empty hide the import fields
        if (dateList == null || dateList.size() == 0) {
            linearLayoutImport.setVisibility(View.GONE);
        }
        // show the import fields and create the spinner adapter
        else {
            linearLayoutImport.setVisibility(View.VISIBLE);
            List<String> dateStringList = new ArrayList<>();
            for (Date date : dateList) {
                dateStringList.add(sdf_readable.format(date));
            }

            ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_1, dateStringList);
            importSpinner.setAdapter(adapter);
        }
    }

    private void handleExportClick() {
        requestWritePermission();

        if (isExternalStorageWritable()) {

            if (isWritePermissionGranted()) {

                // get all data from the database
                List<DailyBalance> dailyBalanceList = viewModel.getAllData();

                // if data was not loaded, the list == null
                if (dailyBalanceList == null)
                    Snackbar.make(mainRoot.findViewById(idSnackbarContainer), getString(R.string.snackbar_error_loading_data_from_database), Snackbar.LENGTH_LONG).show();
                else {
                    // create a JSONArray
                    JSONArray jsonArray = createJsonArrayFromDailyBalance(dailyBalanceList);

                    // save the JSONArray to file as a String
                    String filename = exportFilePrefix + sdf.format(new Date(System.currentTimeMillis())) + exportFileDataType;
                    exportDataToFile(filename, jsonArray.toString());
                    updateSpinner();
                }
            } else {
                // inform the user, that no write persmission is granted
                Snackbar.make(mainRoot.findViewById(R.id.database_import_export_container), getString(R.string.snackbar_permission_ext_storage_denied), Snackbar.LENGTH_SHORT).show();
            }
        } else {
            Snackbar.make(mainRoot.findViewById(idSnackbarContainer), getString(R.string.snackbar_no_external_storage), Snackbar.LENGTH_SHORT).show();
        }
    }

    private void handleImportClick() {
        String selectedDate = importSpinner.getSelectedItem().toString();
        try {

            // get the filename of the selected backup date
            Date date = sdf_readable.parse(selectedDate);
            String filename = exportFilePrefix + sdf.format(date) + exportFileDataType;

            // get path to the directory, where the EggManager backup files are stored
            String publicDataDir = Environment.getExternalStorageDirectory().getPath();

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

            // import data to the database. If data exists, it is overritten
            List<DailyBalance> dailyBalances = getDailyBalanceList(jsonArray);
            for (int i=0; i<dailyBalances.size(); i++) {
                Log.d(LOG_TAG, "Date of loaded data: " + sdf_date.format(dailyBalances.get(i).getDate()));
                viewModel.insert(dailyBalances.get(i));
            }

            // inform the user about the process
            Snackbar.make(mainRoot.findViewById(idSnackbarContainer),String.format(getString(R.string.snackbar_data_imported_successfully),dailyBalances.size()),Snackbar.LENGTH_SHORT).show();

        } catch (ParseException e) {
            e.printStackTrace();
            Log.e(LOG_TAG, "btnImport::onClick::error when parsing String to date. String = " + selectedDate);
            Snackbar.make(mainRoot.findViewById(idSnackbarContainer),getString(R.string.snackbar_error_loading_data_from_database),Snackbar.LENGTH_SHORT).show();
        }
    }


    /**
     * initialize all buttons of the UI and set OnClickListeners
     */
    private void initButtons(View v) {

        // initialize the export button
        Button btnExport = v.findViewById(R.id.btn_save_database);
        btnExport.setOnClickListener(this);
/*        btnExport.setOnClickListener(new View.OnFilterDialogClickListener() {
            @Override
            public void onClick(View v) {
                handleExportClick();
            }
        });*/

        // initialize the import button
        Button btnImport = mainRoot.findViewById(R.id.btn_import_data);
        btnImport.setOnClickListener(this);
/*        btnImport.setOnClickListener(new View.OnFilterDialogClickListener() {
            @Override
            public void onClick(View v) {
                handleImportClick();
            }
        });*/
    }

    /**
     * parse a JSONArray to a list of DailyBalance objects
     * @param jsonArray JSONArray containing data for a DailyBalance
     * @return List<DailyBalance> list of DailyBalance objects
     */
    private List<DailyBalance> getDailyBalanceList(JSONArray jsonArray) {

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
        int writeExternalStoragePermission = ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE);
        return (writeExternalStoragePermission == PackageManager.PERMISSION_GRANTED);
    }

    /**
     * if the permission to write to external storage is not given yet, a dialog requesting the permission is shown
     */
    private void requestWritePermission() {

        // If do not grant write external storage permission.
        if (!isWritePermissionGranted()) {
            // Request user to grant write external storage permission.
            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_CODE_WRITE_EXTERNAL_STORAGE_PERMISSION);
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

    /**
     * find all EggManager backup files and return the date when they have been saved
     *
     * @return List<Date> containing the dates, when the backup files have been stored on the phone
     */
    private List<Date> getDatesOfBackupFiles() {

        // get path, where the files are stored
        String path = Environment.getExternalStorageDirectory().getPath();

        // get content of this directory
        File directory = new File(path);
        File[] files = directory.listFiles();

        String fileName;
        Date date;

        // List for storing the dates of the backup files
        List<Date> backupFilesDates = new ArrayList<>();

        for (File file : files) {

            fileName = file.getName();

            // check if the file is a EggManager backup file
            if (fileName.length() > (exportFilePrefix.length() + exportFileDataType.length()) &&
                    fileName.substring(0, exportFilePrefix.length()).equals(exportFilePrefix) &&
                    fileName.substring(fileName.length()-4, fileName.length()).equals(exportFileDataType)) {

                // file name of the current file
                fileName = file.getName();
                Log.d(LOG_TAG, "Backup file name: " + fileName);

                // remove prefix and extension of the filename. The rest of the filename is just the date
                fileName = fileName.replace(exportFilePrefix, "");
                fileName = fileName.replace(exportFileDataType, "");

                // try to parse the date
                try {
                    // parse the date
                    date = sdf.parse(fileName);

                    // get a human readably format of the date for the log
                    Log.d(LOG_TAG, "Date: " + sdf_readable.format(date));

                    // add the date to the list
                    backupFilesDates.add(date);
                }
                // handle Exception
                catch (ParseException e) {
                    e.printStackTrace();
                    Log.e(LOG_TAG, "Fehler beim Parsen");
                }
            }
        }
        return backupFilesDates;
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

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_save_database:
                handleExportClick();
                break;
            case R.id.btn_import_data:
                handleImportClick();
                break;
        }
    }
}
