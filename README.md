# tracker-android-sample
Android client sample for [KARTE](https://karte.io)

## Usage

### 1. build.gradleへ追加する
app/build.gradleのdependenciesに上でimportしたモジュールを追加する。

```groovy
repositories {
  maven { url 'https://github.com/plaidev/tracker-android-sample/raw/master/maven-repo' }
}

dependencies {
  compile 'io.karte.android:tracker:0.0.0'
}
```

### 3. Application or MainActivityのonCreateにセットアップ用のコードを追加する
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

### 4. イベントの送信処理を追加する
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

### 5. ユーザ情報の送信処理を追加する
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
