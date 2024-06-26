package fi.oulu.danielszabo.pepper.log;

import android.util.Log;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LOG {

    private static final String TAG = "LOG";

    private static boolean printInsteadOfAndroidLog = false;


    public static List<LogEntry> logEntryList = new ArrayList<>();
    private static Map<String, LogListener> logListenerHashMap = new HashMap<>();

    public static void setPrintInsteadOfAndroidLog(boolean printInsteadOfAndroidLog) {
        LOG.printInsteadOfAndroidLog = printInsteadOfAndroidLog;
    }

    public static void addListener(String key, LogListener listener) {
        logListenerHashMap.put(key, listener);
    }

    private static void notifyListeners(LogEntry logEntry) {
        for (LogListener listener : logListenerHashMap.values()) {
            listener.accept(logEntry);
        }
    }


    public static void info(Object context, String message) {
        if (context instanceof Class) {
            info(((Class<?>) context).getSimpleName(), message);
        } else {
            info(context.getClass().getSimpleName(), message);
        }
    }

    public static void debug(Object context, String message) {
        if (context instanceof Class) {
            debug(((Class<?>) context).getSimpleName(), message);
        } else {
            debug(context.getClass().getSimpleName(), message);
        }
    }

    public static void warning(Object context, String message) {
        if (context instanceof Class) {
            warning(((Class<?>) context).getSimpleName(), message);
        } else {
            warning(context.getClass().getSimpleName(), message);
        }
    }

    public static void error(Object context, String message) {
        if (context instanceof Class) {
            error(((Class<?>) context).getSimpleName(), message);
        } else {
            error(context.getClass().getSimpleName(), message);
        }
    }

    public static void info(String tag, String message) {
        if(printInsteadOfAndroidLog) System.out.println("INFO [" + tag + "] " + message );
        else android.util.Log.i(tag, message);

        LogEntry logEntry = new LogEntry(
                LogEntry.LogLevel.INFO,
                tag,
                message,
                new Date().getTime()
        );

        logEntryList.add(logEntry);
        notifyListeners(logEntry);

        // Debug statement to verify the execution of the info() method
        Log.d(TAG, "Info message logged: " + message);
    }

    public static void debug(String tag, String message) {
        if(printInsteadOfAndroidLog) System.out.println("DEBUG [" + tag + "] " + message );
        else android.util.Log.d(tag, message);

        LogEntry logEntry = new LogEntry(
                LogEntry.LogLevel.DEBUG,
                tag,
                message,
                new Date().getTime()
        );

        logEntryList.add(logEntry);
        notifyListeners(logEntry);

        // Debug statement to verify the execution of the debug() method
        if(!printInsteadOfAndroidLog) Log.d(TAG, "Debug message logged: " + message);

    }

    public static void warning(String tag, String message) {
        if(printInsteadOfAndroidLog) System.out.println("WARNING [" + tag + "] " + message );
        else android.util.Log.w(tag, message);

        LogEntry logEntry = new LogEntry(
                LogEntry.LogLevel.WARNING,
                tag,
                message,
                new Date().getTime()
        );

        logEntryList.add(logEntry);
        notifyListeners(logEntry);

        // Debug statement to verify the execution of the warning() method
        Log.d(TAG, "Warning message logged: " + message);

    }

    public static void error(String tag, String message) {
        if(printInsteadOfAndroidLog) System.out.println("ERROR [" + tag + "] " + message );
        else android.util.Log.e(tag, message);

        LogEntry logEntry = new LogEntry(
                LogEntry.LogLevel.ERROR,
                tag,
                message,
                new Date().getTime()
        );

        logEntryList.add(logEntry);
        notifyListeners(logEntry);

        // Debug statement to verify the execution of the error() method
        Log.d(TAG, "Error message logged: " + message);
    }

    public static void removeListener(String key) {
        logListenerHashMap.remove(key);
    }

    public static Map<String, LogListener> getLogListenerHashMap() {
        return logListenerHashMap;
    }


}
