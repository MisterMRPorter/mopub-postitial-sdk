package com.mopub.mobileads;

import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;
import static com.mopub.common.DataKeys.BROADCAST_IDENTIFIER_KEY;
import static com.mopub.mobileads.VastVideoViewController.VAST_VIDEO_CONFIGURATION;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.VideoView;

import com.mopub.common.CacheService;
import com.mopub.common.DataKeys;
import com.mopub.mobileads.factories.VastManagerFactory;
import com.mopub.mobileads.util.vast.VastManager;
import com.mopub.mobileads.util.vast.VastVideoConfiguration;

import java.util.Map;

class VastVideoInterstitial extends ResponseBodyInterstitial implements VastManager.VastManagerListener, BaseVideoViewController.BaseVideoViewControllerListener {
    private CustomEventInterstitialListener mCustomEventInterstitialListener;
    private String mVastResponse;
    private VastManager mVastManager;
    private VastVideoConfiguration mVastVideoConfiguration;
    
    public String vastVidNetworkUrl = null;

    static final String VIDEO_CLASS_EXTRAS_KEY = "video_view_class_name";
    static final String VIDEO_URL = "video_url";
    
    @Override
    protected void extractExtras(Map<String, String> serverExtras) {
        mVastResponse = serverExtras.get(DataKeys.HTML_RESPONSE_BODY_KEY);
    }

    @Override
    protected void preRenderHtml(CustomEventInterstitialListener customEventInterstitialListener) {
        mCustomEventInterstitialListener = customEventInterstitialListener;

        if (!CacheService.initializeDiskCache(mContext)) {
            mCustomEventInterstitialListener.onInterstitialFailed(MoPubErrorCode.VIDEO_CACHE_ERROR);
            return;
        }

        mVastManager = VastManagerFactory.create(mContext);
        mVastManager.prepareVastVideoConfiguration(mVastResponse, this);
    }

    @Override
    public void showInterstitial() {
        MraidVideoPlayerActivity.startVast(mContext, mVastVideoConfiguration, mBroadcastIdentifier);
    }

    @Override
    public void onInvalidate() {
        if (mVastManager != null) {
            mVastManager.cancel();
        }

        super.onInvalidate();
    }

    /*
     * VastManager.VastManagerListener implementation
     */

    @Override
    public void onVastVideoConfigurationPrepared(final VastVideoConfiguration vastVideoConfiguration) {
        if (vastVideoConfiguration == null) {
            mCustomEventInterstitialListener.onInterstitialFailed(MoPubErrorCode.VIDEO_DOWNLOAD_ERROR);
            return;
        }

        mVastVideoConfiguration = vastVideoConfiguration;
        mCustomEventInterstitialListener.onInterstitialLoaded();
    }


    @Deprecated // for testing
    String getVastResponse() {
        return mVastResponse;
    }

    @Deprecated // for testing
    void setVastManager(VastManager vastManager) {
        mVastManager = vastManager;
    }

	@Override
	public View showInterstitialView(ViewGroup holder, AdViewController adViewController) {
		
		Intent intentVideoPlayerActivity = startVastPostitial(mContext, mVastVideoConfiguration, mBroadcastIdentifier);				
		
		VastVideoViewController vastCont = new VastVideoViewController(mContext, intentVideoPlayerActivity.getExtras(), mBroadcastIdentifier, this);
		 
		String vastVidUrl = mVastVideoConfiguration.getDiskMediaFileUrl();		
		Log.d("postitialzz", "vastVidUrl = " + vastVidUrl);
		
		vastVidNetworkUrl = mVastVideoConfiguration.getNetworkMediaFileUrl();		
		Log.d("postitialzz", "vastVidNetworkUrl = " + vastVidNetworkUrl);
		
		adViewController.setVastVideoConfiguration(mVastVideoConfiguration);
		
		return vastCont.getVideoView();
		
	}
	
	static Intent startVastPostitial(final Context context,
            final VastVideoConfiguration vastVideoConfiguration,
            final long broadcastIdentifier) {
        final Intent intentVideoPlayerActivity = createIntentVast(context, vastVideoConfiguration, broadcastIdentifier);
        try {
            //context.startActivity(intentVideoPlayerActivity);
        } catch (ActivityNotFoundException e) {
            Log.d("MoPub", "Activity MraidVideoPlayerActivity not found. Did you declare it in your AndroidManifest.xml?");
        }
		return intentVideoPlayerActivity;
    }

    static Intent createIntentVast(final Context context,
            final VastVideoConfiguration vastVideoConfiguration,
            final long broadcastIdentifier) {
        final Intent intentVideoPlayerActivity = new Intent(context, MraidVideoPlayerActivity.class);
        intentVideoPlayerActivity.setFlags(FLAG_ACTIVITY_NEW_TASK);
        intentVideoPlayerActivity.putExtra(VIDEO_CLASS_EXTRAS_KEY, "vast");
        intentVideoPlayerActivity.putExtra(VAST_VIDEO_CONFIGURATION, vastVideoConfiguration);
        intentVideoPlayerActivity.putExtra(BROADCAST_IDENTIFIER_KEY, broadcastIdentifier);
        return intentVideoPlayerActivity;
    }

	@Override
	public void onSetContentView(View view) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onSetRequestedOrientation(int requestedOrientation) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onFinish() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onStartActivityForResult(Class<? extends Activity> clazz,
			int requestCode, Bundle extras) {
		// TODO Auto-generated method stub
		
	}

}
