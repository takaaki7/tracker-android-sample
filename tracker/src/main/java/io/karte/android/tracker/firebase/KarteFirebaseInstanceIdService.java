package io.karte.android.tracker.firebase;

import android.util.Log;

import com.google.firebase.iid.FirebaseInstanceId;

import io.karte.android.tracker.Tracker;

public abstract class KarteFirebaseInstanceIdService extends com.google.firebase.iid.FirebaseInstanceIdService {

  @Override
  public void onTokenRefresh() {
    String refreshedToken = FirebaseInstanceId.getInstance().getToken();
    sendToken(refreshedToken);
  }

  private void sendToken(String token) {
    Tracker tracker = getTracker();
    if (tracker == null){
      Log.i(Tracker.LOG_TAG_NAME, "tracker is uninitialized");
      return;
    }

    tracker.trackFcmToken(token);
  }

  protected abstract Tracker getTracker();

}
