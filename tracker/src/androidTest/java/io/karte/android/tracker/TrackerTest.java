package io.karte.android.tracker;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;
import android.test.suitebuilder.annotation.SmallTest;
import android.util.Base64;

import junit.framework.Assert;

import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Properties;
import java.util.zip.Inflater;

@RunWith(AndroidJUnit4.class)
@SmallTest
public class TrackerTest {
  @Before
  public void setUp() throws Exception {
    Context context = InstrumentationRegistry.getContext();
    Tracker.getInstance(context, "test_api_key");
  }

  @Test
  public void testGetTracker() throws Exception {
    Context context = InstrumentationRegistry.getContext();
    Tracker tracker = Tracker.getInstance(context, "test_api_key");
    Assert.assertEquals(tracker, Tracker.getInstance(context, "test_api_key"));
  }

}