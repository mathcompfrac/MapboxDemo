package com.tct.app.mapboxdemo.logfile;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

/**
 * Created by Administrator on 2017/8/22.
 */

final class Util {
    private Util() {
    }

    public static void writeFile(String logDirPath, String logFileName, String log) {
        File file = new File(logDirPath);
        if (!file.exists()) {
            file.mkdirs();
        }

        FileOutputStream fos;
        BufferedWriter bw = null;
        try {
            fos = new FileOutputStream(logFileName, true);
            bw = new BufferedWriter(new OutputStreamWriter(fos));
            bw.write(log);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (bw != null) {
                    bw.close();//关闭缓冲流
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
