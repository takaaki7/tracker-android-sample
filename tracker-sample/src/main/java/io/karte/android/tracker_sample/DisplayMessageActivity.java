package io.karte.android.tracker_sample;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.ViewGroup;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import io.karte.android.tracker.Tracker;

public class DisplayMessageActivity extends AppCompatActivity {

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_display_message);

    Intent intent = getIntent();
    String message = intent.getStringExtra(MainActivity.EXTRA_MESSAGE);
    TextView textView = new TextView(this);
    textView.setTextSize(40);
    textView.setText(message);

    ViewGroup layout = (ViewGroup) findViewById(R.id.activity_display_message);
    layout.addView(textView);

    sendCustomEvent(message);
  }

  private void sendCustomEvent (String message){
    Tracker tracker = Tracker.getInstance(this, SampleApp.APP_KEY);
    try {
      JSONObject values = new JSONObject();
      values.put("message_text", message);
      tracker.track("display_message", values);
    } catch (JSONException e) {
      e.printStackTrace();
    }
  }
}
