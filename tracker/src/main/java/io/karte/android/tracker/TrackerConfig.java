package io.karte.android.tracker;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;

class TrackerConfig {

  private static final String CONFIG_PREFIX = "io.karte.android.Tracker";

  private static final String DEFAULT_ENDPOINT_URL = "https://api.karte.io/v0/track";
  private static final String DEFAULT_VIEW_EVENT_NAME = "view";

  private final boolean enableTrackingAppLifecycle;
  private final String endpoint;
  private final String viewEventName;
  private final boolean enableTrackingCrashError;

  public TrackerConfig(Bundle bundle, Context context) {

    this.enableTrackingAppLifecycle = bundle.getBoolean(CONFIG_PREFIX + "." + "EnableTrackingAppLifecycle", true);
    this.endpoint = bundle.getString(CONFIG_PREFIX + "." + "Endpoint", DEFAULT_ENDPOINT_URL);
    this.viewEventName = bundle.getString(CONFIG_PREFIX + "." + "ViewEventName", DEFAULT_VIEW_EVENT_NAME);
    this.enableTrackingCrashError = bundle.getBoolean(CONFIG_PREFIX + "." + "TrackingCrashError", false);

    Log.v(Tracker.LOG_TAG_NAME, "configured with\n" +
      "TrackingAppLifecycle " + enabledTrackingAppLifeCycle() + "\n" +
      "Endpoint " + getEndpoint() + "\n" +
      "ViewEventName " + getViewEventName() + "\n" +
      "TrackingCrachError " + enabledTrackingCrashError()
    );
  }

  // single-ton object
  private static TrackerConfig instance;

  public static synchronized TrackerConfig getInstance(Context context) {
    if (instance != null) {
      return instance;
    }
    Context appContext = context.getApplicationContext();
    TrackerConfig config = createConfig(appContext);
    if (config != null) {
      instance = config;
    } else {
      config = new TrackerConfig(new Bundle(), context);
    }
    return config;
  }

  static TrackerConfig createConfig(Context appContext) {
    final String packageName = appContext.getPackageName();
    try {
      final ApplicationInfo appInfo = appContext.getPackageManager().getApplicationInfo(packageName, PackageManager.GET_META_DATA);
      Bundle configBundle = appInfo.metaData;
      if (null == configBundle) {
        configBundle = new Bundle();
      }
      return new TrackerConfig(configBundle, appContext);
    } catch (PackageManager.NameNotFoundException e) {
      Log.e(Tracker.LOG_TAG_NAME, "Error occurred when ", e);
      return null;
    }
  }

  public String getViewEventName() {
    return this.viewEventName;
  }

  public String getEndpoint() {
    return this.endpoint;
  }

  public boolean enabledTrackingAppLifeCycle() {
    return this.enableTrackingAppLifecycle;
  }

  public boolean enabledTrackingCrashError() {
    return this.enableTrackingCrashError;
  }
}
