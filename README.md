# tracker-android-sample
Android client sample for [KARTE](https://karte.io)

## Setting up

### 1. Add to build.gradle
Add the module to the dependencies of app/build.gradle

```groovy
repositories {
  maven { url 'https://github.com/plaidev/tracker-android-sample/raw/master/maven-repo' }
}

dependencies {
  compile 'io.karte.android:tracker:0.0.3'
}
```

### 2. Implement set up code in Application or MainActivity
```java
import io.karte.android.tracker.Tracker;

public class MainActivity extends AppCompatActivity {

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    ...

    Tracker tracker = Tracker.getInstance(this, "YOUR_APP_KEY");
  }
}
```

### 3. Add to event tracking code
## View event to track opening a view
```java
Tracker tracker = Tracker.getInstance(this, "YOUR_APP_KEY");
try {
  JSONObject values = new JSONObject();
  values.put("sample_key", "sample_value");
  tracker.view("first_view", values);
} catch (JSONException e) {
  Log.e("App", "Failed to construct JSONObject", e);
}
```

## Identify event to track user infomation
```java
Tracker tracker = Tracker.getInstance(this, "YOUR_APP_KEY");
try {
  JSONObject values = new JSONObject();
  values.put("user_id", user_id);
  values.put("name", user_name);
  tracker.identify(values);
} catch (JSONException e) {
  Log.e("App", "Failed to construct JSONObject", e);
}
```

## Custom event
```java
Tracker tracker = Tracker.getInstance(this, "YOUR_APP_KEY");
try {
  JSONObject values = new JSONObject();
  values.put("sample_key", "sample_value");
  tracker.track("sample_event_name", values);
} catch (JSONException e) {
  Log.e("App", "Failed to construct JSONObject", e);
}
```

