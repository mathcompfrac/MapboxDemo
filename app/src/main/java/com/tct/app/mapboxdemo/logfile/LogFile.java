package com.tct.app.mapboxdemo.logfile;

import android.content.Context;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;

import java.util.Date;

/**
 * Created by hupei on 2017/8/21.
 */

public final class LogFile {
    private static LogFileConfig LOGGER_CONFIG;
    private final static String SUFFIX_LOG = ".log";

    public static void init(Context appContext, LogFileConfig config) {
        LOGGER_CONFIG = config;
        if (TextUtils.isEmpty(LOGGER_CONFIG.getLogDirPath())) {
            String logDirPath;
            if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())
                    || !Environment.isExternalStorageRemovable()) {
                logDirPath = appContext.getExternalFilesDir(null).getPath();
            } else {
                logDirPath = appContext.getFilesDir().getPath();
            }
            Log.w("renfei", "init: " + logDirPath);
            LOGGER_CONFIG.setLogDirPath(logDirPath);
        }
    }

    public static void putMsg(String format, Object... msg) {
        putMsg(String.format(format, msg));
    }

    public static void putMsg(String tag, String format, Object... msg) {
        putMsg(tag, String.format(format, msg));
    }

    public static void putMsg(String msg) {
        putMsg("", msg);
    }

    public static void putMsg(String tag, String msg) {
        writeToFile(tag + msg);
    }


    /**
     * 将log信息写入文件中
     *
     * @param msg
     */
    private static void writeToFile(final String msg) {
        if (LOGGER_CONFIG == null)
            throw new NullPointerException("must init()");
        final String logDirPath = LOGGER_CONFIG.getLogDirPath();
        if (TextUtils.isEmpty(logDirPath))
            throw new NullPointerException("logDirPath is null");

        new LogFileThread().post(new Runnable() {
            @Override
            public void run() {
                String logFileDirPath = logDirPath;
                Date date = new Date();
                if (!logDirPath.endsWith("/"))
                    logFileDirPath += "/";

                String logFileName = logFileDirPath + LOGGER_CONFIG.getDateFormatFileName().format(date) + SUFFIX_LOG;

                String log = "\r\n" + LOGGER_CONFIG.getDateFormatLogName().format(date) + "------->" + msg + "\r\n";

                Util.writeFile(logFileDirPath, logFileName, log);
            }
        });
    }
}
