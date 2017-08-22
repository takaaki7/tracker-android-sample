# tracker-android
Android client for [KARTE](https://karte.io)

## Usage
### 1. projectへ.aarを追加する
Android Studio上でprojectを右クリックし、"New" > "Module"から"Import .JAR/.AAR Package"を選択する。  
"tracker-release.aar"を選んで適当なモジュール名(ex."tracker-release")をつけて追加する。

### 2. build.gradleへ追加する
app/build.gradleのdependenciesに上でimportしたモジュールを追加する。

```groovy
dependencies {
  ...
  compile project(':tracker-release')
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

    Tracker tracker = Tracker.setupTracker(this, "YOUR_API_KEY");
  }
}
```

### 4. イベントの送信処理を追加する
```java
Tracker tracker = Tracker.getTracker();
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
Tracker tracker = Tracker.getTracker();
try {
  JSONObject values = new JSONObject();
  values.put("user_id", user_id);
  values.put("name", user_name);
  tracker.identify(values);
} catch (JSONException e) {
  Log.e("App", "Failed to construct JSONObject", e);
}
```
