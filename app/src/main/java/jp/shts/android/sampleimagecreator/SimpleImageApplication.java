package jp.shts.android.sampleimagecreator;

import android.app.Application;

import com.jakewharton.threetenabp.AndroidThreeTen;


public class SimpleImageApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        AndroidThreeTen.init(this);
    }
}
