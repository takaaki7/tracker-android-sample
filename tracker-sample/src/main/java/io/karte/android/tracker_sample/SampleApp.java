package io.karte.android.tracker_sample;

import android.app.Application;

import java.util.Properties;

import io.karte.android.tracker.Tracker;

public class SampleApp extends Application {

  public final static String APP_KEY = "T14xyl7xHbqcXswWYQ8BAdcV9589AibN";

  @Override
  public void onCreate() {
    super.onCreate();
    Tracker.init(this, APP_KEY);
  }
}
