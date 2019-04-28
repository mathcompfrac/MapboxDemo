package com.tct.app.mapboxdemo.logfile;

import android.os.Handler;
import android.os.HandlerThread;

/**
 * Created by Administrator on 2017/8/22.
 */

public final class LogFileThread {
    private HandlerThread fileLoggerThread;
    private Handler handler;

    public LogFileThread() {
        fileLoggerThread = new HandlerThread(LogFile.class.getSimpleName());
        fileLoggerThread.start();
        handler = new Handler(fileLoggerThread.getLooper());
    }

    public void post(Runnable r) {
        handler.post(r);
    }
}
