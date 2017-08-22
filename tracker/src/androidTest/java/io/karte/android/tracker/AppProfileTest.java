package io.karte.android.tracker;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;
import android.test.suitebuilder.annotation.SmallTest;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
@SmallTest
public class AppProfileTest {
  private static final String TEST_PREF_NAME = "APP PROFILE TEST PREF";

  @Before
  public void setUp() throws Exception {
    Context context = InstrumentationRegistry.getContext();
    SharedPreferences sharedPrefs = context.getSharedPreferences(TEST_PREF_NAME, Context.MODE_PRIVATE);
    SharedPreferences.Editor editor = sharedPrefs.edit();
    editor.clear();
    editor.commit();
  }

  @Test
  public void testAppInstallation () throws Exception {
    Context context = InstrumentationRegistry.getContext();
    SharedPreferences sharedPrefs = context.getSharedPreferences(TEST_PREF_NAME, Context.MODE_PRIVATE);

    AppProfile appProfile = new AppProfile(context, sharedPrefs);

    Assert.assertEquals(appProfile.prevVersionCode, -1);
  }

  @Test
  public void testAppUpdate () throws Exception {
    Context context = InstrumentationRegistry.getContext();
    SharedPreferences sharedPrefs = context.getSharedPreferences(TEST_PREF_NAME, Context.MODE_PRIVATE);

    AppProfile appProfile = new AppProfile(context, sharedPrefs);
    AppProfile appProfile2 = new AppProfile(context, sharedPrefs);

    Assert.assertEquals(appProfile.versionName, appProfile2.prevVersionName);
    Assert.assertEquals(appProfile.versionCode, appProfile2.prevVersionCode);
  }
}
