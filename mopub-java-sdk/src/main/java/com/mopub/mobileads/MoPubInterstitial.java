package com.mopub.mobileads;

import android.app.Activity;
import android.content.Context;
import android.location.Location;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import com.mopub.common.MoPub;
import com.mopub.common.logging.MoPubLog;
import com.mopub.mobileads.factories.CustomEventInterstitialAdapterFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.mopub.common.LocationService.LocationAwareness;
import static com.mopub.common.util.ResponseHeader.CUSTOM_EVENT_DATA;
import static com.mopub.common.util.ResponseHeader.CUSTOM_EVENT_NAME;
import static com.mopub.mobileads.MoPubErrorCode.ADAPTER_NOT_FOUND;

public class MoPubInterstitial implements CustomEventInterstitialAdapter.CustomEventInterstitialAdapterListener {

    private enum InterstitialState {
        CUSTOM_EVENT_AD_READY,
        NOT_READY;

        boolean isReady() {
            return this != InterstitialState.NOT_READY;
        }
    }

    private MoPubInterstitialView mInterstitialView;
    private CustomEventInterstitialAdapter mCustomEventInterstitialAdapter;
    private InterstitialAdListener mInterstitialAdListener;
    private Context mActivity;
    private String mAdUnitId;
    private InterstitialState mCurrentInterstitialState;
    private boolean mIsDestroyed;

    public interface InterstitialAdListener {
        public void onInterstitialLoaded(MoPubInterstitial interstitial);
        public void onInterstitialFailed(MoPubInterstitial interstitial, MoPubErrorCode errorCode);
        public void onInterstitialShown(MoPubInterstitial interstitial);
        public void onInterstitialClicked(MoPubInterstitial interstitial);
        public void onInterstitialDismissed(MoPubInterstitial interstitial);
        public void onClosePostitialSession(MoPubInterstitial interstitial);
		public void onVideoPlayed(String url);
    }

    private MoPubInterstitialListener mListener;

    @Deprecated
    public interface MoPubInterstitialListener {
        public void OnInterstitialLoaded();
        public void OnInterstitialFailed();
    }

    public MoPubInterstitial(Context context, String id) {
        mActivity = context;
        mAdUnitId = id;

        mInterstitialView = new MoPubInterstitialView(mActivity);
        mInterstitialView.setAdUnitId(mAdUnitId);

        mCurrentInterstitialState = InterstitialState.NOT_READY;

    }

    public void load() {
        resetCurrentInterstitial();
        mInterstitialView.loadAd();
    }

    public void forceRefresh() {
        resetCurrentInterstitial();
        mInterstitialView.forceRefresh();
    }

    private void resetCurrentInterstitial() {
        mCurrentInterstitialState = InterstitialState.NOT_READY;

        if (mCustomEventInterstitialAdapter != null) {
            mCustomEventInterstitialAdapter.invalidate();
            mCustomEventInterstitialAdapter = null;
        }

        mIsDestroyed = false;
    }

    public boolean isReady() {
        return mCurrentInterstitialState.isReady();
    }

    boolean isDestroyed() {
        return mIsDestroyed;
    }

    public boolean show() {
        switch (mCurrentInterstitialState) {
            case CUSTOM_EVENT_AD_READY:
                showCustomEventInterstitial();
                return true;
        }
        return false;
    }

    private void showCustomEventInterstitial() {
        if (mCustomEventInterstitialAdapter != null) mCustomEventInterstitialAdapter.showInterstitial();
    }
    
    public View showView(ViewGroup holder) {
        switch (mCurrentInterstitialState) {
            case CUSTOM_EVENT_AD_READY:
                return showCustomEventInterstitialView(holder);
        }
        return null;
    }

    private View showCustomEventInterstitialView(ViewGroup holder) {
        if (mCustomEventInterstitialAdapter != null) {
            return mCustomEventInterstitialAdapter.showInterstitialView(holder);
        }
        else {
            return null;
        }
    }
    

    Integer getAdTimeoutDelay() {
        return mInterstitialView.getAdTimeoutDelay();
    }

    public MoPubInterstitialView getMoPubInterstitialView() {
        return mInterstitialView;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////

    public void setKeywords(String keywords) {
        mInterstitialView.setKeywords(keywords);
    }

    public String getKeywords() {
        return mInterstitialView.getKeywords();
    }

    public Context getActivity() {
        return mActivity;
    }

    public Location getLocation() {
        return mInterstitialView.getLocation();
    }

    public void destroy() {
        mIsDestroyed = true;

        if (mCustomEventInterstitialAdapter != null) {
            mCustomEventInterstitialAdapter.invalidate();
            mCustomEventInterstitialAdapter = null;
        }

        mInterstitialView.setBannerAdListener(null);
        mInterstitialView.destroy();
    }

    public void setInterstitialAdListener(InterstitialAdListener listener) {
        mInterstitialAdListener = listener;
    }

    public InterstitialAdListener getInterstitialAdListener() {
        return mInterstitialAdListener;
    }

    public void setTesting(boolean testing) {
        mInterstitialView.setTesting(testing);
    }

    public boolean getTesting() {
        return mInterstitialView.getTesting();
    }

    public void setLocalExtras(Map<String, Object> extras) {
        mInterstitialView.setLocalExtras(extras);
    }

    public Map<String, Object> getLocalExtras() {
        return mInterstitialView.getLocalExtras();
    }

    /*
     * Implements CustomEventInterstitialAdapter.CustomEventInterstitialListener
     */

    @Override
    public void onCustomEventInterstitialLoaded() {
        if (mIsDestroyed) return;

        mCurrentInterstitialState = InterstitialState.CUSTOM_EVENT_AD_READY;

        if (mInterstitialAdListener != null) {
            mInterstitialAdListener.onInterstitialLoaded(this);
        } else if (mListener != null) {
            mListener.OnInterstitialLoaded();
        }
    }

    @Override
    public void onCustomEventInterstitialFailed(MoPubErrorCode errorCode) {
        if (isDestroyed()) return;

        mCurrentInterstitialState = InterstitialState.NOT_READY;
        mInterstitialView.loadFailUrl(errorCode);
    }

    @Override
    public void onCustomEventInterstitialShown() {
        if (isDestroyed()) return;

        mInterstitialView.trackImpression();

        if (mInterstitialAdListener != null) {
            mInterstitialAdListener.onInterstitialShown(this);
        }
    }

    @Override
    public void onCustomEventInterstitialClicked() {
        if (isDestroyed()) return;

        mInterstitialView.registerClick();

        if (mInterstitialAdListener != null) {
            mInterstitialAdListener.onInterstitialClicked(this);
        }
    }

    @Override
    public void onCustomEventInterstitialDismissed() {
        if (isDestroyed()) return;

        mCurrentInterstitialState = InterstitialState.NOT_READY;

        if (mInterstitialAdListener != null) {
            mInterstitialAdListener.onInterstitialDismissed(this);
        }
    }

    @Deprecated
    public void setLocationAwareness(LocationAwareness locationAwareness) {
        MoPub.setLocationAwareness(locationAwareness.getNewLocationAwareness());
    }

    @Deprecated
    public LocationAwareness getLocationAwareness() {
        return LocationAwareness.fromMoPubLocationAwareness(MoPub.getLocationAwareness());
    }

    @Deprecated
    public void setLocationPrecision(int precision) {
        MoPub.setLocationPrecision(precision);
    }

    @Deprecated
    public int getLocationPrecision() {
        return MoPub.getLocationPrecision();
    }

	@Override
    public void onClosePostitialSession() {

        if (mInterstitialAdListener != null) {
            mInterstitialAdListener.onClosePostitialSession(this);
        }
    }
    
	@Override
	public void onVideoPlayed(String url) {
		
		if (mInterstitialAdListener != null) {
			mInterstitialAdListener.onVideoPlayed(url);
		}
		
	}

    
    ////////////////////////////////////////////////////////////////////////////////////////////////

    public class MoPubInterstitialView extends MoPubView {

        public MoPubInterstitialView(Context context) {
            super(context);
            setAutorefreshEnabled(false);
        }

        @Override
        protected void loadCustomEvent(Map<String, String> paramsMap) {
            if (paramsMap == null) {
                MoPubLog.d("Couldn't invoke custom event because the server did not specify one.");
                loadFailUrl(ADAPTER_NOT_FOUND);
                return;
            }

            if (mCustomEventInterstitialAdapter != null) {
                mCustomEventInterstitialAdapter.invalidate();
            }

            MoPubLog.d("Loading custom event interstitial adapter.");

            mCustomEventInterstitialAdapter = CustomEventInterstitialAdapterFactory.create(
                    MoPubInterstitial.this,
                    paramsMap.get(CUSTOM_EVENT_NAME.getKey()),
                    paramsMap.get(CUSTOM_EVENT_DATA.getKey()));
            mCustomEventInterstitialAdapter.setAdapterListener(MoPubInterstitial.this);
            
            Log.d("MoPub", "Custom event name = " + paramsMap.get(CUSTOM_EVENT_NAME.getKey()));
            
            List<String> unsupportedPostitialNetworks = new ArrayList<String>();
            unsupportedPostitialNetworks.add("com.mopub.mobileads.GooglePlayServices");
            unsupportedPostitialNetworks.add("com.mopub.mobileads.Millennial");
            unsupportedPostitialNetworks.add("com.mopub.mobileads.AdColony");
            unsupportedPostitialNetworks.add("com.mopub.mobileads.Chartboost");
            unsupportedPostitialNetworks.add("com.mopub.mobileads.Facebook");
            unsupportedPostitialNetworks.add("com.mopub.mobileads.Greystripe");
            unsupportedPostitialNetworks.add("com.mopub.mobileads.Vungle");  
            unsupportedPostitialNetworks.add("com.mopub.mobileads.InMobi"); 
            
            if(this.mAdViewController.getAdConfiguration().isPostitial()) {
                for(String iterateHolderVariable : unsupportedPostitialNetworks) {
                    if(paramsMap.get(CUSTOM_EVENT_NAME.getKey()).toLowerCase().startsWith(iterateHolderVariable.toLowerCase())) {
                        Log.d("MoPub", "Postitial does not currently support this network");
                        loadFailUrl(ADAPTER_NOT_FOUND);
                        return;
                    }
                }
            }
            
            mCustomEventInterstitialAdapter.loadInterstitial();
        }

        public void trackImpression() {
            MoPubLog.d("Tracking impression for interstitial.");
            if (mAdViewController != null) mAdViewController.trackImpression();
        }

        @Override
        protected void adFailed(MoPubErrorCode errorCode) {
            if (mInterstitialAdListener != null) {
                mInterstitialAdListener.onInterstitialFailed(MoPubInterstitial.this, errorCode);
            }
        }
    }

    @Deprecated // for testing
    void setInterstitialView(MoPubInterstitialView interstitialView) {
        mInterstitialView = interstitialView;
    }

    @Deprecated
    public void setListener(MoPubInterstitialListener listener) {
        mListener = listener;
    }

    @Deprecated
    public MoPubInterstitialListener getListener() {
        return mListener;
    }

    @Deprecated
    public void customEventDidLoadAd() {
        if (mInterstitialView != null) mInterstitialView.trackImpression();
    }

    @Deprecated
    public void customEventDidFailToLoadAd() {
        if (mInterstitialView != null) mInterstitialView.loadFailUrl(MoPubErrorCode.UNSPECIFIED);
    }

    @Deprecated
    public void customEventActionWillBegin() {
        if (mInterstitialView != null) mInterstitialView.registerClick();
    }

	public void setPostitial(boolean isPostitial) {
		this.mInterstitialView.mAdViewController.getAdConfiguration().setPostitial(isPostitial);
	}


    /**
     * @deprecated As of release 2.4
     */
    @Deprecated
    public void setFacebookSupported(boolean enabled) {}

    /**
     * @deprecated As of release 2.4
     */
    @Deprecated
    public boolean isFacebookSupported() {
        return false;
    }
}
