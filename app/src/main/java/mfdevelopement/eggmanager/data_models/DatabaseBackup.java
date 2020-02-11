package mfdevelopement.eggmanager.data_models;

import android.util.Log;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DatabaseBackup implements Comparable<DatabaseBackup> {

    private static final String LOG_TAG = "DatabaseBackup";

    private static final Locale defaultLocale = Locale.ENGLISH;
    private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss", defaultLocale);

    private static final String exportFilePrefix = "EggManager_backup_";
    private static final String exportFileDataType = ".emb"; // EggManagerBackup

    private String backupName, filename;
    private Calendar saveDate;

    private long fileSize;

    public DatabaseBackup() {
        this("unnamed");
    }

    public DatabaseBackup(String backupName) {
        this(backupName, Calendar.getInstance());
    }

    public DatabaseBackup(String backupName, Calendar saveDate) {
        setBackupName(backupName);
        this.saveDate = saveDate;
        this.filename = createBackupFilename();
    }

    public DatabaseBackup(File file) {
        this.filename = file.getName();
        this.backupName = getBackupNameFromFilename(this.filename);
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(file.lastModified());
        this.saveDate = calendar;
        this.fileSize = file.length();
    }

    public String getBackupName() {
        if (this.backupName == null)
            return "unnamed";
        else
            return this.backupName;
    }

    public void setBackupName(String backupName) {
        if (backupName.equals(""))
            this.backupName = "unnamed";
        else
            this.backupName = backupName;

        this.filename = createBackupFilename();
    }

    public Calendar getSaveDate() {
        if (this.saveDate != null)
            return this.saveDate;
        else
            return Calendar.getInstance();
    }

    public String getFilename() {
        return filename;
    }

    public String getFileSizeFormatted() {
        if (this.fileSize < 1_000)
            return this.fileSize + " Bytes";
        else if (this.fileSize > 1_000 && this.fileSize <= 1_000_000)
            return String.format(Locale.getDefault(), "%.1f KB", this.fileSize/1e3);
        else if (this.fileSize > 1_000_000 && this.fileSize <= 1_000_000_000)
            return String.format(Locale.getDefault(), "%.1f MB", this.fileSize/1e6);
        else if (this.fileSize > 1_000_000_000)
            return String.format(Locale.getDefault(), "%.1f GB", this.fileSize/1e9);
        else
            return "";
    }

    public String getFormattedSaveDate() {
        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault());
        return sdf.format(this.saveDate.getTimeInMillis());
    }

    /**
     * create the filename for the backup file
     * @return
     */
    private String createBackupFilename() {
        return exportFilePrefix + this.getBackupName() + "_" + sdf.format(this.getSaveDate().getTimeInMillis()) + exportFileDataType;
    }

    private String getBackupNameFromFilename(String filename) {
        String nameAndDate = filename.replace(exportFilePrefix,"").replace(exportFileDataType,"");

        // Create a Pattern object
        final Pattern r = Pattern.compile("\\d{8}_\\d{6}$");

        // Now create matcher object.
        Matcher m = r.matcher(nameAndDate);
        if (m.find( )) {
            String date = m.group(0);
            return nameAndDate.replace("_" + date,"");
        }else {
            Log.d(LOG_TAG,"getBackupNameFromFilename(): could not find the date included in the name \"" + nameAndDate + "\"");
        }

        return nameAndDate;
    }

    /**
     * checks if a file is a backup file of EggManager depending on the filename
     * @param filename Name of the file to check
     * @return status wheather the file is an EggManager backup file or not
     */
    public static boolean isEggManagerBackupFile(String filename) {
        return (filename.length() > (exportFilePrefix.length() + exportFileDataType.length()) &&
                filename.substring(0, exportFilePrefix.length()).equals(exportFilePrefix) &&
                filename.substring(filename.length()-exportFileDataType.length()).equals(exportFileDataType));
    }


    @Override
    public int compareTo(DatabaseBackup otherBackup) {
        long diff = otherBackup.getSaveDate().getTimeInMillis() - this.getSaveDate().getTimeInMillis();
        if (diff < 0)
            return -1;
        else if (diff > 0)
            return 1;
        else
            return 0;
    }
}
