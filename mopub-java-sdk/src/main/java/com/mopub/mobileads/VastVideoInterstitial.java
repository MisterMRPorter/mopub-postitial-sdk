/*
 * Copyright (c) 2010-2013, MoPub Inc.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 *
 *  Redistributions of source code must retain the above copyright
 *   notice, this list of conditions and the following disclaimer.
 *
 *  Redistributions in binary form must reproduce the above copyright
 *   notice, this list of conditions and the following disclaimer in the
 *   documentation and/or other materials provided with the distribution.
 *
 *  Neither the name of 'MoPub Inc.' nor the names of its contributors
 *   may be used to endorse or promote products derived from this software
 *   without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
 * TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package com.mopub.mobileads;

import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;
import static com.mopub.mobileads.AdFetcher.AD_CONFIGURATION_KEY;
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
import android.widget.VideoView;

import com.mopub.common.CacheService;
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
//post
    static final String VIDEO_CLASS_EXTRAS_KEY = "video_view_class_name";
    static final String VIDEO_URL = "video_url";
    
    @Override
    protected void extractExtras(Map<String, String> serverExtras) {
        mVastResponse = Uri.decode(serverExtras.get(AdFetcher.HTML_RESPONSE_BODY_KEY));
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
    protected void showInterstitial() {
        MraidVideoPlayerActivity.startVast(mContext, mVastVideoConfiguration, mAdConfiguration);
    }

    @Override
    protected void onInvalidate() {
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
	public View showInterstitialView() {
		
		Intent intentVideoPlayerActivity = startVastPostitial(mContext, mVastVideoConfiguration, mAdConfiguration);				
		
		VastVideoViewController vastCont = new VastVideoViewController(mContext, intentVideoPlayerActivity.getExtras(), mBroadcastIdentifier, this);
		 
		String vastVidUrl = mVastVideoConfiguration.getDiskMediaFileUrl();		
		Log.d("postitialzz", "vastVidUrl = " + vastVidUrl);
		
		vastVidNetworkUrl = mVastVideoConfiguration.getNetworkMediaFileUrl();		
		Log.d("postitialzz", "vastVidNetworkUrl = " + vastVidNetworkUrl);

		mAdConfiguration.setVastVideoConfiguration(mVastVideoConfiguration);
		
		return vastCont.getVideoView();
		
	}
	
	static Intent startVastPostitial(final Context context,
            final VastVideoConfiguration vastVideoConfiguration,
            final AdConfiguration adConfiguration) {
        final Intent intentVideoPlayerActivity = createIntentVast(context, vastVideoConfiguration, adConfiguration);
        try {
            //context.startActivity(intentVideoPlayerActivity);
        } catch (ActivityNotFoundException e) {
            Log.d("MoPub", "Activity MraidVideoPlayerActivity not found. Did you declare it in your AndroidManifest.xml?");
        }
		return intentVideoPlayerActivity;
    }

    static Intent createIntentVast(final Context context,
            final VastVideoConfiguration vastVideoConfiguration,
            final AdConfiguration adConfiguration) {
        final Intent intentVideoPlayerActivity = new Intent(context, MraidVideoPlayerActivity.class);
        intentVideoPlayerActivity.setFlags(FLAG_ACTIVITY_NEW_TASK);
        intentVideoPlayerActivity.putExtra(VIDEO_CLASS_EXTRAS_KEY, "vast");
        intentVideoPlayerActivity.putExtra(VAST_VIDEO_CONFIGURATION, vastVideoConfiguration);
        intentVideoPlayerActivity.putExtra(AD_CONFIGURATION_KEY, adConfiguration);
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
