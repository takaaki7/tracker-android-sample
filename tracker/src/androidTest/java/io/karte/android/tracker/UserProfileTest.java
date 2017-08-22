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
public class UserProfileTest {
  private static final String TEST_PREF_NAME = "USER PROFILE TEST PREF";

  @Before
  public void setUp() throws Exception {
    Context context = InstrumentationRegistry.getContext();
    SharedPreferences sharedPrefs = context.getSharedPreferences(TEST_PREF_NAME, Context.MODE_PRIVATE);
    SharedPreferences.Editor editor = sharedPrefs.edit();
    editor.clear();
    editor.commit();
  }

  @Test
  public void testInitialization () throws Exception {
    Context context = InstrumentationRegistry.getContext();
    SharedPreferences sharedPrefs = context.getSharedPreferences(TEST_PREF_NAME, Context.MODE_PRIVATE);
    UserProfile userProfile = new UserProfile(sharedPrefs);

    String visitorId = userProfile.getVisitorId();

    Assert.assertNotNull(visitorId);

    // 再取得しても同じvisitor_idが返る
    Assert.assertEquals(visitorId, userProfile.getVisitorId());
  }

  @Test
  public void testVisitorIdGeneration () throws Exception {
    Context context = InstrumentationRegistry.getContext();
    SharedPreferences sharedPrefs = context.getSharedPreferences(TEST_PREF_NAME, Context.MODE_PRIVATE);
    UserProfile userProfile = new UserProfile(sharedPrefs);

    String visitorId = userProfile.getVisitorId();
    Assert.assertNotNull(visitorId);

    sharedPrefs.edit().clear().commit();

    String newVisitorId = userProfile.getVisitorId();
    Assert.assertNotNull(newVisitorId);
    Assert.assertNotSame(visitorId, newVisitorId);
  }

}
