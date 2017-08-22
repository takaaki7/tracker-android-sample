package io.karte.android.tracker.firebase;

import android.util.Log;

import com.google.firebase.iid.FirebaseInstanceId;

import org.json.JSONException;
import org.json.JSONObject;

import io.karte.android.tracker.Tracker;

public abstract class KarteFirebaseInstanceIdService extends com.google.firebase.iid.FirebaseInstanceIdService {

  @Override
  public void onTokenRefresh() {
    String refreshedToken = FirebaseInstanceId.getInstance().getToken();
    sendToken(refreshedToken);
  }

  private void sendToken(String token) {
    Tracker tracker = getTracker();
    if(tracker == null){
      Log.i(Tracker.LOG_TAG_NAME, "tracker is uninitialized");
      return;
    }

    JSONObject values = new JSONObject();
    try {
      values.put("fcm_token", token);
      values.put("subscribe", true);
      values.put("os", "Android");
    } catch (JSONException e) {
      Log.e(Tracker.LOG_TAG_NAME, "failed to construct json", e);
    }

    tracker.track("plugin_native_app_identify", values);
  }

  protected abstract Tracker getTracker();

}
