package com.tct.app.mapboxdemo.logfile;

import java.text.SimpleDateFormat;
import java.util.Locale;

/**
 * Created by hupei on 2017/8/21.
 */

public final class LogFileConfig {
    private String logDirPath;
    private boolean isCrashLog;
    private SimpleDateFormat dateFormatFileName = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
    private SimpleDateFormat dateFormatLogName = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);

    /**
     * 日志保存目录
     *
     * @param logDirPath
     * @return
     */
    public LogFileConfig setLogDirPath(String logDirPath) {
        this.logDirPath = logDirPath;
        return this;
    }

    /**
     * 是否开启崩溃日志记录
     *
     * @param crashLog
     * @return
     */
    public LogFileConfig setCrashLog(boolean crashLog) {
        isCrashLog = crashLog;
        return this;
    }

    String getLogDirPath() {
        return logDirPath;
    }

    boolean isCrashLog() {
        return isCrashLog;
    }

    SimpleDateFormat getDateFormatFileName() {
        return dateFormatFileName;
    }

    SimpleDateFormat getDateFormatLogName() {
        return dateFormatLogName;
    }
}
