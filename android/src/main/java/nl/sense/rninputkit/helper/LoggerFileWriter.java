package nl.sense.rninputkit.helper;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import nl.sense.rninputkit.BuildConfig;
import nl.sense.rninputkit.R;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;


/**
 * Created by panji on 22/02/18.
 */

public class LoggerFileWriter {
    private final Context context;
    private static final String TAG = "LoggerFileWriter";
    private File logFile;

    public LoggerFileWriter(Context context) {
        this.context = context;
        if (BuildConfig.IS_DEBUG_MODE_ENABLED) {
            initializeLogFile();
        }
    }

    /**
     * Log an event into file logger
     *
     * @param timeStamp Define a timestamp of recent event
     * @param tag       Define a tag of recent event
     * @param message   Define a message of recent event
     * @throws IOException
     */
    public void logEvent(long timeStamp, String tag, String message) {
        if (BuildConfig.IS_DEBUG_MODE_ENABLED) {
            try {
                appendToLogFile(timeStamp + ": [" + tag + "]: " + message);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Initialising log file on external storage
     */
    private void initializeLogFile() {
        File logFileDirectory = new File(Environment.getExternalStorageDirectory(), "sense");
        logFile = new File(logFileDirectory, String.format("%s-input-kit.log.txt",
                context.getString(R.string.app_name)));
        if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            Log.d(TAG, "initializeLogFile: Storage unavailable (probably mounted elsewhere)");
            return;
        }
        if (!logFileDirectory.exists() && !logFileDirectory.mkdirs()) {
            Log.d(TAG, "initializeLogFile: Could not create the directory for log file");
            return;
        }
        if (!logFile.exists()) {
            try {
                createLogFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Append an information into log file
     *
     * @param line Define a message that we want to append to the log file
     * @throws IOException whenever something went wrong during writing to the log file
     */
    private void appendToLogFile(String line) throws IOException {
        Writer writer = null;
        try {
            writer = new OutputStreamWriter(new FileOutputStream(logFile, true), "UTF-8");
            writer.append(line).append("\n");
            writer.flush();
        } finally {
            if (writer != null) writer.close();
        }
    }

    /**
     * Create new log file if it doesn't exists on local storage
     *
     * @throws IOException whenever something went wrong during creating a new log file
     */
    private void createLogFile() throws IOException {
        if (!logFile.createNewFile()) {
            Log.d(TAG, "createLogFile: Could not create log file for writing");
        }
    }
}
