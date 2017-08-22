package io.karte.android.tracker_sample;

import io.karte.android.tracker.firebase.KarteFirebaseInstanceIdService;
import io.karte.android.tracker.Tracker;

public class MyKarteFirebaseInstanceIdService extends KarteFirebaseInstanceIdService {

  protected Tracker getTracker() {
    return Tracker.getInstance(this, SampleApp.APP_KEY);
  }
}
