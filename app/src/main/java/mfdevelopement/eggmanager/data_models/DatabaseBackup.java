package mfdevelopement.eggmanager.data_models;

import android.util.Log;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DatabaseBackup implements Comparable<DatabaseBackup> {

    private static final String LOG_TAG = "DatabaseBackup";

    private static final Locale defaultLocale = Locale.ENGLISH;
    private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss", defaultLocale);

    private static final String exportFilePrefix = "EggManager_backup_";
    private static final String exportFileDataType = ".emb"; // EggManagerBackup

    private String name, filename;
    private Calendar saveDate;

    public DatabaseBackup() {
        this("unnamed");
    }

    public DatabaseBackup(String backupName) {
        this(backupName, Calendar.getInstance());
    }

    public DatabaseBackup(String backupName, Calendar saveDate) {
        this.name = backupName;
        this.saveDate = saveDate;
        this.filename = getBackupFilename();
    }

    public DatabaseBackup(String backupName, Date date) {
        this(backupName);
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        this.setSaveDate(cal);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Calendar getSaveDate() {
        return saveDate;
    }

    public void setSaveDate(Calendar saveDate) {
        this.saveDate = saveDate;
    }

    public void setSaveDate(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        setSaveDate(cal);
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public String getFormattedSaveDate() {
        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault());
        return sdf.format(this.saveDate.getTimeInMillis());
    }

    private String getBackupFilename() {
        return exportFilePrefix + this.getName() + "_" + sdf.format(new Date(System.currentTimeMillis())) + exportFileDataType;
    }

    public static Date getDateFromFilename(String filename) throws ParseException{

        String nameAndDate = filename.replace(exportFilePrefix, "").replace(exportFileDataType, "");

        // Create a Pattern object
        final Pattern r = Pattern.compile("\\d+_\\d+");

        // Now create matcher object.
        Matcher m = r.matcher(nameAndDate);
        if (m.find( )) {
            return sdf.parse(m.group(0));
        }else {
            Log.d(LOG_TAG,"getDateFromFilename(): could not find the date included in the name \"" + nameAndDate + "\"");
        }

        return new Date();
    }

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
