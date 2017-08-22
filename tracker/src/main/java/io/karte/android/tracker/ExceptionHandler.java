package io.karte.android.tracker;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

public class ExceptionHandler implements Thread.UncaughtExceptionHandler {

  private static ExceptionHandler instance;
  static synchronized void init() {
    if (instance == null) {
      instance = new ExceptionHandler();
    }
  }

  private Thread.UncaughtExceptionHandler handler;

  private ExceptionHandler() {
    this.handler = Thread.getDefaultUncaughtExceptionHandler();
    Thread.setDefaultUncaughtExceptionHandler(this);
  }

  @Override
  public void uncaughtException(Thread thread, Throwable th) {
    JSONObject values = new JSONObject();
    try {
      values.put("throwable", th.toString());
      Tracker.trackUncaughtException(values);
    } catch (JSONException e) {
      Log.e(Tracker.LOG_TAG_NAME, "failed to construct json", e);
    }

    if (this.handler != null) {
      this.handler.uncaughtException(thread, th);
    } else {
      try {
        Thread.sleep(500);
      } catch (InterruptedException e1) {
      }
      android.os.Process.killProcess(android.os.Process.myPid());
      System.exit(10);
    }
  }
}
