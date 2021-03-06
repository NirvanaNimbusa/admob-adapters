package com.gemserk.google.ads.mediation.millenialmedia;

import java.util.Hashtable;

import android.app.Activity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;

import com.google.ads.AdSize;
import com.google.ads.mediation.MediationAdRequest;
import com.google.ads.mediation.customevent.CustomEventBanner;
import com.google.ads.mediation.customevent.CustomEventBannerListener;
import com.millennialmedia.android.MMAdView;
import com.millennialmedia.android.MMAdView.MMAdListener;
import com.millennialmedia.android.MMAdViewSDK;

public class MillenialMediaCustomEventBanner implements CustomEventBanner {

	class MillenialMediaBannerListener implements MMAdListener {

		private CustomEventBannerListener listener;
		private FrameLayout wrappedView;

		public MillenialMediaBannerListener(CustomEventBannerListener listener, FrameLayout wrappedView) {
			this.listener = listener;
			this.wrappedView = wrappedView;
		}

		@Override
		public void MMAdCachingCompleted(MMAdView arg0, boolean arg1) {
			Log.d(TAG, "Ad caching completed: " + arg1);
			if (arg1)
				onReceivedAd(arg0);
			else
				listener.onFailedToReceiveAd();
		}

		@Override
		public void MMAdClickedToOverlay(MMAdView arg0) {
			Log.d(TAG, "Ad clicked to overlay");
			listener.onClick();
			listener.onLeaveApplication();
		}

		@Override
		public void MMAdFailed(MMAdView arg0) {
			if (arg0.check()) {
				Log.d(TAG, "Ad failed but ad already cached");
				onReceivedAd(arg0);
			} else {
				Log.d(TAG, "Ad failed and no ad was cached");
				listener.onFailedToReceiveAd();
			}
		}

		private void onReceivedAd(MMAdView adView) {
			listener.onReceivedAd(wrappedView);
		}

		@Override
		public void MMAdOverlayLaunched(MMAdView adView) {
			Log.d(TAG, "Ad overlay launched");
			listener.onPresentScreen();
		}

		@Override
		public void MMAdRequestIsCaching(MMAdView adView) {
			Log.d(TAG, "Ad request is caching, no admob listener call");
		}

		@Override
		public void MMAdReturned(MMAdView adView) {
			Log.d(TAG, "Ad returned");
			onReceivedAd(adView);
		}
	}

	private static final String TAG = "MillenialMediaCustomEventBanner";

	public static String defaultAdType = MMAdView.BANNER_AD_TOP;

	public static final Hashtable<String, String> metadata = new Hashtable<String, String>();

	@Override
	public void destroy() {

	}

	@Override
	public void requestBannerAd(CustomEventBannerListener listener, Activity activity, String label, String serverParameter, //
			AdSize size, MediationAdRequest mediationAdRequest, Object customEventExtra) {

		String apId = serverParameter;

		if (mediationAdRequest.isTesting()) {
			apId = MMAdViewSDK.DEFAULT_APID;
			Log.d(TAG, "changing to testing apId: " + apId + " since mediationAdRequest.isTesting()");
		}

		if (apId == null || "".equals(apId)) {
			Log.e(TAG, "failed with invalid serverParameter: " + serverParameter);
			listener.onFailedToReceiveAd();
			return;
		}

		Log.d(TAG, "request received with serverParameter: " + serverParameter);

		DisplayMetrics metrics = new DisplayMetrics();
		activity.getWindowManager().getDefaultDisplay().getMetrics(metrics);

		int adWidth = size.getWidth();
		int adHeight = size.getHeight();

		int width = (int) (adWidth * metrics.density);
		int height = (int) (adHeight * metrics.density);

		metadata.put("width", Integer.toString(adWidth));
		metadata.put("height", Integer.toString(adHeight));

		FrameLayout wrappedView = new FrameLayout(activity);

		FrameLayout.LayoutParams wrappedLayoutParams = new FrameLayout.LayoutParams(width, adHeight == 50 ? (int) ((adHeight + 3) * metrics.density) : height);
		// FrameLayout.LayoutParams wrappedLayoutParams = new FrameLayout.LayoutParams(width, height);
		wrappedView.setLayoutParams(wrappedLayoutParams);

		MMAdView adView = new MMAdView(activity, apId, defaultAdType, MMAdView.REFRESH_INTERVAL_OFF, metadata);
		adView.setId(MMAdViewSDK.DEFAULT_VIEWID);

		wrappedView.addView(adView, new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT));

		adView.setListener(new MillenialMediaBannerListener(listener, wrappedView));

		adView.callForAd();
	}

}
