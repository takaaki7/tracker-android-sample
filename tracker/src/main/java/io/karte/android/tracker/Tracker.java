package io.karte.android.tracker;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

public abstract class Tracker {

  public static final String LOG_TAG_NAME = "KarteTracker";

  private static final Map<String, Map<Application, Tracker>> keyToTrackerMap = new HashMap<>(1);

  public static void init(Context context, String key) {
    getInstance(context, key);
  }

  public abstract void identify(JSONObject values);
  public abstract void track(String event_name, JSONObject values);
  public abstract void track(String event_name, JSONObject values, boolean withAppInfo);
  public abstract void view();
  public abstract void view(JSONObject values);
  public abstract void flush();

  public static Tracker getInstance(Context context, String key) {
    if (context == null || key == null) {
      return new NullTracker();
    }
    Context appContext = context.getApplicationContext();
    if (!(appContext instanceof Application)) {
      Log.e(LOG_TAG_NAME, "application context is not an Application instance.");
      return  new NullTracker(); // TODO
    }
    Application application = (Application) appContext;
    synchronized (keyToTrackerMap) {
      Map<Application, Tracker> appToTracker = keyToTrackerMap.get(key);
      if (appToTracker == null) {
        appToTracker = new HashMap<>(1);
        keyToTrackerMap.put(key, appToTracker);
      }
      Tracker tracker = appToTracker.get(application);
      if (tracker == null) {
        tracker = new TrackerImpl(application, key);
        appToTracker.put(application, tracker);
      }
      return tracker;
    }
  }

  public static void trackUncaughtException(JSONObject values) {
    synchronized (keyToTrackerMap) {
      for (Map.Entry<String, Map<Application, Tracker>> e : keyToTrackerMap.entrySet()) {
        Map<Application, Tracker> appToTracker = e.getValue();
        for (Map.Entry<Application, Tracker> e2 : appToTracker.entrySet()) {
          Tracker tracker = e2.getValue();
          tracker.track("_app_crashed", values);
        }
      }
    }
  }

  private final static class NullTracker extends Tracker {
    @Override
    public void identify(JSONObject values) {}
    @Override
    public void track(String event_name, JSONObject values) {}
    @Override
    public void track(String event_name, JSONObject values, boolean withAppInfo) {}
    @Override
    public void view() {}
    @Override
    public void view(JSONObject values) {}
    @Override
    public void flush() {}
  }

  private final static class TrackerImpl extends Tracker {

    private static final String PREF_NAME_PREFIX = "io.karte.android.tracker.Data_";
    private static final int MAX_EVENT_BUFFER_SIZE = 10;
    private static final String TIMESTAMP_FIELD_NAME = "_local_event_date";
    private static final String APP_INFO_FIELD_NAME = "app_info";
    private static final String APP_KEY_FIELD = "X-KARTE-App-Key";

    private final Application application;
    private final String appKey;
    private final UserProfile userProfile;
    private final AppProfile appProfile;
    private final TrackerConfig trackerConfig;
    private final Handler handler;
    // Guarded by this
    private final List<JSONObject> bufferedEvents = new ArrayList<>();

    private TrackerImpl(Application application, String appKey, TrackerConfig trackerConfig) {

      this.application = application;
      this.appKey = appKey;

      SharedPreferences sharedPrefs = getSharedPreferences(application, appKey);
      this.userProfile = new UserProfile(sharedPrefs);
      this.appProfile = new AppProfile(application, sharedPrefs);

      HandlerThread thread = new HandlerThread("io.karte.android.Tracker", Thread.MIN_PRIORITY);
      thread.start();
      Looper looper = thread.getLooper();
      this.handler = new Handler(looper) {
        @Override
        public void handleMessage(Message msg) {
          sendQueuedEvents();
        }
      };

      registerActivityLifecycleCallback(application);

      this.trackerConfig = trackerConfig;
      if (this.trackerConfig.enabledTrackingAppLifeCycle()) {
        trackAppLifecycle();
      }
      if (this.trackerConfig.enabledTrackingCrashError()) {
        ExceptionHandler.init();
      }
    }

    private TrackerImpl(Application application, String key) {
      this(application, key, TrackerConfig.getInstance(application));
    }

    private static SharedPreferences getSharedPreferences(Application application, String appKey) {
      return application.getSharedPreferences(PREF_NAME_PREFIX + appKey, Context.MODE_PRIVATE);
    }

    public void identify(JSONObject values) {
      track("identify", values, false);
    }

    public void track(String eventName, JSONObject values) {
      track(eventName, values, true);
    }

    public void track(String eventName, JSONObject values, boolean withAppInfo) {

      final JSONObject event = new JSONObject();
      try {

        final JSONObject copiedValues = new JSONObject();
        if (values != null) {
          for (Iterator<String> iter = values.keys(); iter.hasNext(); ) {
            String key = iter.next();
            copiedValues.put(key, values.get(key));
          }
        }

        long unixTimestamp = System.currentTimeMillis() / 1000L;
        copiedValues.put(TIMESTAMP_FIELD_NAME, unixTimestamp);
        if (withAppInfo) {
          copiedValues.put(APP_INFO_FIELD_NAME, this.appProfile.getAppProfileValues());
        }

        event
                .put("event_name", eventName)
                .put("values", copiedValues);

      } catch (JSONException e) {
        Log.e(LOG_TAG_NAME, "failed to construct json", e);
        return;
      }

      synchronized(this.bufferedEvents) {
        this.bufferedEvents.add(event);
      }

      flush();
    }

    public void view() {
      view(null);
    }

    public void view(JSONObject values) {
      String viewEventName = this.trackerConfig.getViewEventName();
      track(viewEventName, values);
    }

    public void flush () {
      this.handler.sendEmptyMessage(0);
    }

    private JSONObject createTrackData(JSONArray events) throws JSONException {
      final JSONObject keys = new JSONObject();
      keys
              .put("visitor_id", this.userProfile.getVisitorId());

      final JSONObject data = new JSONObject();
      data.put("keys", keys);
      data.put("events", events);

      return data;
    }

    private void sendQueuedEvents() {

      final JSONArray events = new JSONArray();
      synchronized (this.bufferedEvents) {

        if(this.bufferedEvents.size() == 0) {
          Log.w(LOG_TAG_NAME, "no event to send");
          return;
        }

        while(this.bufferedEvents.size() > MAX_EVENT_BUFFER_SIZE) {
          JSONObject removed = this.bufferedEvents.remove(0);// delete oldest event
          Log.e(LOG_TAG_NAME, "Overflowed buffer " + removed);
        }

        for (JSONObject event: this.bufferedEvents) {
          events.put(event);
        }
        this.bufferedEvents.clear();
      }

      if(!isOnline()) {
        Log.i(LOG_TAG_NAME, "network is currently unavailable");
        return;
      }

      final URL url;
      try {
        url = new URL(trackerConfig.getEndpoint());
      } catch (MalformedURLException e) {
        Log.e(LOG_TAG_NAME, "can't construct track url", e);
        return;
      }

      final JSONObject data;
      try {
        data = createTrackData(events);
      } catch (JSONException e) {
        Log.e(LOG_TAG_NAME, "failed to construct json");
        return;
      }

      final boolean success = sendRequestToTrack(url, data);
      if (!success) {
        // queue again
        synchronized (this.bufferedEvents) {
          for (int i = 0; i < events.length(); i++) {
            try {
              JSONObject obj = events.getJSONObject(i);
              this.bufferedEvents.add(obj);
            } catch (JSONException e) {
              Log.e(LOG_TAG_NAME, "JSON Exception:", e);
            }
          }
        }
      }
    }

    private boolean isOnline () {
      ConnectivityManager manager = (ConnectivityManager) this.application.getSystemService(Context.CONNECTIVITY_SERVICE);
      NetworkInfo info = manager.getActiveNetworkInfo();
      return (info != null && info.isConnectedOrConnecting());
    }

    private boolean sendRequestToTrack(URL url, JSONObject data) {
      HttpURLConnection conn;
      try {
        conn = (HttpURLConnection) url.openConnection();
        try {
          conn.setReadTimeout(10000);
          conn.setConnectTimeout(10000);
          conn.setRequestMethod("POST");
          conn.setDoInput(true);
          conn.setDoOutput(true);
          conn.setUseCaches(false);

          conn.setRequestProperty(APP_KEY_FIELD, this.appKey);
          conn.setRequestProperty("content-type", "text/plain; charset=utf-8");

          String body = data.toString();
          writeBody(conn, body);

          conn.connect();

          InputStream is = new BufferedInputStream(conn.getInputStream());
          try {
            return handleResponse(conn, is);
          } finally {
            is.close();
          }
        } catch (FileNotFoundException ignore) {
          InputStream es = conn.getErrorStream();
          try {
            return handleResponse(conn, es);
          } finally {
            es.close();
          }
        } catch (IOException e){
          Log.e(Tracker.LOG_TAG_NAME, "failed to send request", e);
          return false;
        } finally {
          conn.disconnect();
        }
      } catch (IOException e){
        Log.e(Tracker.LOG_TAG_NAME, "failed to connect track server", e);
        return false;
      }
    }

    private void writeBody(HttpURLConnection conn, String body) throws IOException {
      OutputStream os = conn.getOutputStream();
      try {
        BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
        bw.write(body);
        bw.close();
      } finally {
        os.close();
      }
    }

    private boolean handleResponse(HttpURLConnection conn, InputStream is) throws IOException {
      int responseCode = conn.getResponseCode();
      if (responseCode != HttpURLConnection.HTTP_OK) {
        String body = toString(is);
        Log.e(Tracker.LOG_TAG_NAME, "server respond error: " + String.valueOf(responseCode) + " body:" + body);
        return false;
      }
      return true;
    }

    private String toString(InputStream is) throws IOException {
      ByteArrayOutputStream os = new ByteArrayOutputStream();
      byte[] buf = new byte[4096];
      int len;
      while((len = is.read(buf)) > 0){
        os.write(buf, 0, len);
      }
      try {
        return os.toString("UTF-8");
      } catch (UnsupportedEncodingException e) {
        return os.toString();
      }
    }

    private void trackAppLifecycle() {

      if (this.appProfile.versionCode == -1) {
        return;
      }

      if (this.appProfile.prevVersionCode == -1) {
        // application installed
        JSONObject values = this.appProfile.getAppProfileValues();
        track("native_app_install", values);

      } else if(this.appProfile.prevVersionCode != this.appProfile.versionCode) {
        // application updated
        JSONObject values = this.appProfile.getAppProfileValuesForUpdate();
        track("native_app_update", values);
      }
    }

    private void trackMessageClick(String campaign_id, String shorten_id) {
      JSONObject values = new JSONObject();
      try {
        JSONObject message = new JSONObject();
        message.put("campaign_id", campaign_id);
        message.put("shorten_id", shorten_id);
        values.put("message", message);
      } catch (JSONException e) {
        Log.e(LOG_TAG_NAME, "failed to construct json", e);
        return;
      }
      track("message_click", values);
    }

    private void registerActivityLifecycleCallback (Application application) {

      application.registerActivityLifecycleCallbacks(new Application.ActivityLifecycleCallbacks() {

        private static final String EXTRA_PUSH_FLAG = "krt_push_notification";
        private static final String EXTRA_CAMPAIGN_ID = "krt_campaign_id";
        private static final String EXTRA_SHORTEN_ID = "krt_shorten_id";

        @Override
        public void onActivityCreated(Activity activity, Bundle savedInstanceState) {}

        @Override
        public void onActivityDestroyed(Activity activity) {}

        @Override
        public void onActivityPaused(Activity activity) {}

        @Override
        public void onActivityResumed(Activity activity) {}

        @Override
        public void onActivitySaveInstanceState(Activity activity, Bundle outState) {}

        @Override
        public void onActivityStarted(Activity activity) {
          final Intent intent = activity.getIntent();

          final String sentByKarte = intent.getStringExtra(EXTRA_PUSH_FLAG);
          if (sentByKarte == null) { return; }

          intent.removeExtra(EXTRA_PUSH_FLAG);

          // click tracking
          final String campaign_id = intent.getStringExtra(EXTRA_CAMPAIGN_ID);
          final String shorten_id = intent.getStringExtra(EXTRA_SHORTEN_ID);
          if (campaign_id != null && shorten_id != null) {
            trackMessageClick(campaign_id, shorten_id);
            intent.removeExtra(EXTRA_CAMPAIGN_ID);
            intent.removeExtra(EXTRA_SHORTEN_ID);
          }
        }

        @Override
        public void onActivityStopped(Activity activity) {}
      });

    }
  }



}
