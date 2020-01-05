package mfdevelopement.eggmanager.utils;

import android.os.Environment;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class FileUtil {

    private static final String LOG_TAG = "FileUtil";

    /**
     * read the content of a File on the device
     * @param file File to be read
     * @return String containing the content of the file
     */
    public static String readFile(File file) {

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


    public static int writeContentToFile(String fileName, String content) {

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
            return 0;
        }
        catch (IOException e) {
            e.printStackTrace();
            Log.e(LOG_TAG, "exportDataToFile::creating the file " + fileName + " failed");
            return 1;
        }
    }
}