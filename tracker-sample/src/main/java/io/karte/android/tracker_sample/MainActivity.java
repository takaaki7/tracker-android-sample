package io.karte.android.tracker_sample;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.google.firebase.iid.FirebaseInstanceId;

import org.json.JSONException;
import org.json.JSONObject;

import io.karte.android.tracker.Tracker;

public class MainActivity extends AppCompatActivity {
  public final static String EXTRA_MESSAGE = "io.karte.android.tracker_sample.MESSAGE";

  private final static String LOG_KEY = "MainActivity";

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    Log.i(LOG_KEY, "MainActivity created");

    setContentView(R.layout.activity_main);

    setEventListeners();

  }

  private void setEventListeners (){
    final Button buttonIdentifyEvent = (Button) findViewById(R.id.send_identify_event);
    buttonIdentifyEvent.setOnClickListener(new View.OnClickListener(){
      @Override
      public void onClick(View v){
        sendIdentifyEvent();
      }
    });

    final Button buttonViewEvent = (Button) findViewById(R.id.send_view_event);
    buttonViewEvent.setOnClickListener(new View.OnClickListener(){
      @Override
      public void onClick(View v){
        sendViewEvent();
      }
    });

    final Button buttonBuyEvent = (Button) findViewById(R.id.send_buy_event);
    buttonBuyEvent.setOnClickListener(new View.OnClickListener(){
      @Override
      public void onClick(View v){
        sendBuyEvent();
      }
    });

    final Button buttonLogToken = (Button) findViewById(R.id.log_token);
    buttonLogToken.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        logToken();
      }
    });
  }

  private void sendIdentifyEvent() {
    Log.i(LOG_KEY, "identify event button clicked");

    final EditText editText = (EditText) findViewById(R.id.edit_user_id);
    final String user_id = editText.getText().toString();
    if( user_id.length() > 0 ) {
      try {
        JSONObject values = new JSONObject();
        values.put("user_id", user_id);
        values.put("is_app_user", true);
        Tracker.getInstance(this, SampleApp.APP_KEY).identify(values);
      } catch (JSONException e) {
        e.printStackTrace();
      }
    }else{
      Log.w(LOG_KEY, "no user_id");
    }
  }

  private void sendViewEvent() {
    Log.i(LOG_KEY, "view event button clicked");
    try {
      JSONObject values = new JSONObject("{\"title\":\"app test view\"}");
      Tracker.getInstance(this, SampleApp.APP_KEY).view("app test view", values);
    } catch (JSONException e){
      e.printStackTrace();
    }
  }

  private void sendBuyEvent() {
    Log.i(LOG_KEY, "buy event button clicked");
    try {
      JSONObject values = new JSONObject(
        "{" +
          "\"affiliation\":\"shop name\"," +
          "\"revenue\":" + String.valueOf((int) (Math.random() * 10000)) + "," +
          "\"shipping\":100," +
          "\"tax\":10," +
          "\"items\":[{" +
          "  \"item_id\":\"test\"," +
          "  \"name\":\"掃除機A\"," +
          "  \"category\": [\"家電\", \"掃除機\"]," +
          "  \"price\":" + String.valueOf((int) (Math.random() * 1000)) + "," +
          "  \"quantity\":1" +
          "}]" +
        "}"
      );
      Tracker.getInstance(this, SampleApp.APP_KEY).track("buy", values);
    } catch (JSONException e){
      e.printStackTrace();
    }
  }

  private void logToken() {
    String token = FirebaseInstanceId.getInstance().getToken();
    Log.i(LOG_KEY, "token: " + token);
  }

  public void sendMessage(View view) {
    Intent intent = new Intent(this, DisplayMessageActivity.class);
    EditText editText = (EditText) findViewById(R.id.edit_message);
    String message = editText.getText().toString();
    intent.putExtra(EXTRA_MESSAGE, message);
    startActivity(intent);
  }
}
