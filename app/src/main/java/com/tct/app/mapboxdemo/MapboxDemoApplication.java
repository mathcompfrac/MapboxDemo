package com.tct.app.mapboxdemo;

import android.app.Application;

import com.mapbox.android.search.MapboxSearch;
import com.mapbox.android.search.MapboxSearchOptions;
import com.mapbox.mapboxsdk.Mapbox;
import com.tct.app.mapboxdemo.logfile.LogFile;
import com.tct.app.mapboxdemo.logfile.LogFileConfig;

public class MapboxDemoApplication extends Application {
    public static final String ACCESS_TOKEN = "pk.eyJ1IjoibWF0aGNvbXBmcmFjIiwiYSI6ImNqdXRvaGxsODBhNTYzeW5yYjhnMWF1MGIifQ.MHuQ1aEgDY4fPiUF7Zierw";
    private static String TAG = "@MapboxDemo/";
    @Override
    public void onCreate() {
        super.onCreate();
        LogFileConfig loggerConfig = new LogFileConfig();
        LogFile.init(this, loggerConfig);
        LogFile.putMsg(TAG + "MapboxDemoApplication onCreate");
        MapboxSearchOptions mso = new MapboxSearchOptions();
        mso.setCachingEnabled(true);
        MapboxSearch.getInstance(getApplicationContext(), ACCESS_TOKEN, mso);
        Mapbox.getInstance(getApplicationContext(), ACCESS_TOKEN);
    }
}
