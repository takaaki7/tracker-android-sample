package io.karte.android.tracker;

import android.content.SharedPreferences;

import java.util.UUID;

class UserProfile {

  private static final String PREF_VISITOR_ID_KEY = "visitor_id";

  private final SharedPreferences sharedPrefs;

  UserProfile(SharedPreferences sharedPrefs){
    this.sharedPrefs = sharedPrefs;
  }

  public String getVisitorId() {
    String visitorId = this.sharedPrefs.getString(PREF_VISITOR_ID_KEY, null);
    if (visitorId == null) {
      visitorId = generateVisitorId();
      saveString(PREF_VISITOR_ID_KEY, visitorId);
    }
    return visitorId;
  }

  private static String generateVisitorId() {
    return UUID.randomUUID().toString();
  }

  private void saveString(String key, String value) {
    SharedPreferences.Editor editor = this.sharedPrefs.edit();
    editor.putString(key, value);
    editor.apply();
  }
}
