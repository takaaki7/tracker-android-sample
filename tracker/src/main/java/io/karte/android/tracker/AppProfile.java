package io.karte.android.tracker;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Iterator;

final class AppProfile {

  private static final String VERSION_NAME_KEY = "app_version_name";
  private static final String VERSION_CODE_KEY = "app_version_code";

  private final String packageName;
  // previous application version
  public final String prevVersionName;
  public final int prevVersionCode;
  // current application version
  public final String versionName;
  public final int versionCode;

  private volatile JSONObject appProfileValues;
  private volatile JSONObject appProfileValuesForUpdate;

  AppProfile(Context context, SharedPreferences sharedPrefs) {

    this.prevVersionName = sharedPrefs.getString(VERSION_NAME_KEY, null);
    this.prevVersionCode = sharedPrefs.getInt(VERSION_CODE_KEY, -1);

    // get current version
    final PackageManager packageManager = context.getPackageManager();
    PackageInfo packageInfo = null;
    String packageName = context.getPackageName();
    this.packageName = packageName;
    try {
      packageInfo = packageManager.getPackageInfo(packageName, 0);
    } catch (PackageManager.NameNotFoundException e) {
      Log.e(Tracker.LOG_TAG_NAME, "failed to get current package info", e);
    }

    this.versionName = (packageInfo != null) ? packageInfo.versionName : null;
    this.versionCode = (packageInfo != null) ? packageInfo.versionCode : -1;

    // write to shared preferences
    if(this.versionCode != -1 && this.versionCode != this.prevVersionCode){
      SharedPreferences.Editor editor = sharedPrefs.edit();
      editor.putString(VERSION_NAME_KEY, this.versionName);
      editor.putInt(VERSION_CODE_KEY, this.versionCode);
      editor.apply();
    }
  }

  JSONObject getAppProfileValues() {
    if (this.appProfileValues != null) {
      return this.appProfileValues;
    }

    JSONObject values = new JSONObject();
    try {
      values
              .put("version_name", this.versionName)
              .put("version_code", this.versionCode)
              .put("package_name", this.packageName)
              .put("system_info", getSystemInfoValues());

      this.appProfileValues = values;
    } catch (JSONException e){
      Log.e(Tracker.LOG_TAG_NAME, "failed to construct json", e);
    }
    return values;
  }

  JSONObject getAppProfileValuesForUpdate() {
    if (this.appProfileValuesForUpdate != null) {
      return this.appProfileValuesForUpdate;
    }

    final JSONObject copiedValues = new JSONObject();
    try {
      JSONObject values = getAppProfileValues();
      if (values != null) {
        for (Iterator<String> iter = values.keys(); iter.hasNext(); ) {
          String key = iter.next();
          copiedValues.put(key, values.get(key));
        }
      }
      copiedValues
              .put("prev_version_name", this.prevVersionName)
              .put("prev_version_code", this.prevVersionCode);
      this.appProfileValuesForUpdate = copiedValues;
    } catch (JSONException e) {
      Log.e(Tracker.LOG_TAG_NAME, "failed to construct json", e);
    }
    return copiedValues;
  }

  private JSONObject getSystemInfoValues() {
    JSONObject values = new JSONObject();
    try {
      values.put("os", "Android");
      if (Build.VERSION.RELEASE != null) {
        values.put("os_version", Build.VERSION.RELEASE);
      }
      if (Build.DEVICE != null) {
        values.put("device", Build.DEVICE);
      }
      if (Build.BRAND != null) {
        values.put("brand", Build.BRAND);
      }
      if (Build.MODEL != null) {
        values.put("model", Build.MODEL);
      }
      if (Build.PRODUCT != null) {
        values.put("product", Build.MODEL);
      }
    } catch (JSONException e) {
      Log.e(Tracker.LOG_TAG_NAME, "failed to construct json", e);
    }
    return values;
  }
}
