package com.kickstarter.libs;

import android.content.Context;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Log;
import android.view.accessibility.AccessibilityManager;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.util.Base64Utils;
import com.google.firebase.iid.FirebaseInstanceId;
import com.kickstarter.BuildConfig;
import com.kickstarter.R;
import com.kickstarter.libs.qualifiers.ApplicationContext;
import com.kickstarter.libs.utils.ConfigUtils;
import com.kickstarter.libs.utils.MapUtils;
import com.kickstarter.models.User;
import com.kickstarter.services.KoalaBackgroundService;
import com.kickstarter.services.firebase.DispatcherKt;
import com.kickstarter.ui.IntentKey;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Map;

import javax.inject.Inject;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import io.fabric.sdk.android.Fabric;
import timber.log.Timber;

public final class KoalaTrackingClient extends TrackingClientType {
  private static final String TAG = KoalaTrackingClient.class.getSimpleName();
  @Inject CurrentUserType currentUser;
  @Inject Build build;
  @Inject CurrentConfigType currentConfig;
  @Nullable private User loggedInUser;
  @Nullable private Config config;
  private final @NonNull Context context;

  public KoalaTrackingClient(
    final @ApplicationContext @NonNull Context context,
    final @NonNull CurrentUserType currentUser,
    final @NonNull Build build,
    final @NonNull CurrentConfigType currentConfig) {

    this.context = context;
    this.currentUser = currentUser;
    this.build = build;
    this.currentConfig = currentConfig;

    // Cache the most recent logged in user for default Koala properties.
    this.currentUser.observable().subscribe(u -> this.loggedInUser = u);

    // Cache the most recent config for default Koala properties.
    this.currentConfig.observable().subscribe(c -> this.config = c);
  }

  @Override
  public void track(final @NonNull String eventName, final @NonNull Map<String, Object> additionalProperties) {
    try {
      final String trackingData = getTrackingData(eventName, combinedProperties(additionalProperties));
      final String encodedTrackingData = Base64Utils.encodeUrlSafe(trackingData
        .getBytes());
      final Bundle bundle = new Bundle();
      bundle.putString(IntentKey.KOALA_EVENT_NAME, eventName);
      bundle.putString(IntentKey.KOALA_EVENT, encodedTrackingData);

      final String uniqueJobName = KoalaBackgroundService.BASE_JOB_NAME + System.currentTimeMillis();
      DispatcherKt.dispatchJob(this.context, KoalaBackgroundService.class, uniqueJobName, bundle);
      if (this.build.isDebug()) {
        Log.d(TAG, "Queued event:" + trackingData);
      }
    } catch (JSONException e) {
      if (this.build.isDebug()) {
        Timber.e("Failed to encode event: " + eventName);
      }
      Fabric.getLogger().e(KoalaTrackingClient.TAG, "Failed to encode event: " + eventName);
    }
  }

  private String getTrackingData(final @NonNull String eventName, final @NonNull Map<String, Object> newProperties) throws JSONException {
    final JSONObject trackingEvent = new JSONObject();
    trackingEvent.put("event", eventName);

    final Map<String, Object> compactProperties = MapUtils.compact(newProperties);
    final JSONObject propertiesJSON = new JSONObject();
    for (Map.Entry<String, Object> entry : compactProperties.entrySet()) {
      propertiesJSON.put(entry.getKey(), entry.getValue());
    }
    trackingEvent.put("properties", propertiesJSON);
    final JSONArray trackingArray = new JSONArray();
    trackingArray.put(trackingEvent);

    return trackingArray.toString();
  }

  @Override
  protected String androidUUID() {
    return FirebaseInstanceId.getInstance().getId();
  }

  @Override
  protected String brand() {
    return android.os.Build.BRAND;
  }

  /**
   * Derives the device's orientation (portrait/landscape) from the `context`.
   */
  protected @NonNull String deviceOrientation() {
    if (this.context.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
      return "landscape";
    }
    return "portrait";
  }

  @NonNull
  protected String deviceFormat() {
    return this.context.getResources().getBoolean(R.bool.isTablet) ? "tablet" : "phone";
  }

  @Override
  protected JSONArray enabledFeatureFlags() {
    return ConfigUtils.INSTANCE.enabledFeatureFlags(this.config);
  }

  /**
   * Derives the availability of google play services from the `context`.
   */
  protected boolean isGooglePlayServicesAvailable() {
    return GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(this.context) == ConnectionResult.SUCCESS;
  }

  @Override
  protected boolean isTalkBackOn() {
    final AccessibilityManager am = (AccessibilityManager) this.context.getSystemService(Context.ACCESSIBILITY_SERVICE);
    return am != null && am.isTouchExplorationEnabled();
  }

  @Override
  protected String manufacturer() {
    return android.os.Build.MANUFACTURER;
  }

  @Override
  protected String model() {
    return android.os.Build.MODEL;
  }

  @Override
  protected String OSVersion() {
    return android.os.Build.VERSION.RELEASE;
  }

  @Override
  protected Long time() {
    return System.currentTimeMillis() / 1000;
  }

  @Override
  protected User loggedInUser() {
    return this.loggedInUser;
  }

  @Override
  protected String versionName() {
    return BuildConfig.VERSION_NAME;
  }

}
