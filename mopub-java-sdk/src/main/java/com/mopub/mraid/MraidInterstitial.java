package com.mopub.mraid;


import android.content.Intent;
import android.net.Uri;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;

import com.mopub.common.logging.MoPubLog;
import com.mopub.mobileads.AdConfiguration;
import com.mopub.mobileads.ResponseBodyInterstitial;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.Map;

import com.mopub.mobileads.MraidActivity;
import com.mopub.mraid.MraidController.UseCustomCloseListener;

import static com.mopub.mobileads.AdFetcher.HTML_RESPONSE_BODY_KEY;
import static com.mopub.mobileads.BaseInterstitialActivity.JavaScriptWebViewCallbacks.WEB_VIEW_DID_APPEAR;
import static com.mopub.mobileads.BaseInterstitialActivity.JavaScriptWebViewCallbacks.WEB_VIEW_DID_CLOSE;
import static com.mopub.mobileads.EventForwardingBroadcastReceiver.ACTION_INTERSTITIAL_CLICK;
import static com.mopub.mobileads.EventForwardingBroadcastReceiver.ACTION_INTERSTITIAL_SHOW;

class MraidInterstitial extends ResponseBodyInterstitial {
    
    static final String BROADCAST_IDENTIFIER_KEY = "broadcastIdentifier";
    static final String ACTION_INTERSTITIAL_FAIL = "com.mopub.action.interstitial.fail";
    static final String ACTION_INTERSTITIAL_SHOW = "com.mopub.action.interstitial.show";
    static final String ACTION_INTERSTITIAL_DISMISS = "com.mopub.action.interstitial.dismiss";
    static final String ACTION_INTERSTITIAL_CLICK = "com.mopub.action.interstitial.click";
       
    private String mHtmlData;
    private CustomEventInterstitialListener interstitialListener;
    
    @Nullable private MraidController mMraidController;
    @Nullable private MraidWebViewDebugListener mDebugListener;

    @Override
    protected void extractExtras(@NonNull Map<String, String> serverExtras) {
        mHtmlData = Uri.decode(serverExtras.get(HTML_RESPONSE_BODY_KEY));
    }

    @Override
    protected void preRenderHtml(@NonNull CustomEventInterstitialListener
            customEventInterstitialListener) {
        MraidActivity.preRenderHtml(mContext, customEventInterstitialListener, mHtmlData);
        this.interstitialListener = customEventInterstitialListener;
        
    }

    @Override
    public void showInterstitial() {
        MraidActivity.start(mContext, mHtmlData, mAdConfiguration);
    }

    @Override
    public View showInterstitialView(ViewGroup holder) {     

            mMraidController = new MraidController(
                    mContext, mAdConfiguration, PlacementType.INTERSTITIAL);

            mMraidController.setDebugListener(mDebugListener);
            mMraidController.setMraidListener(new MraidController.MraidListener() {
                @Override
                public void onLoaded(View view) {
                    // This is only done for the interstitial. Banners have a different mechanism
                    // for tracking third party impressions.
                    mMraidController.loadJavascript(WEB_VIEW_DID_APPEAR.getJavascript());
                    
                    final long broadcastIdentifier = mAdConfiguration.getBroadcastIdentifier();
                    
                    Intent intent = new Intent(ACTION_INTERSTITIAL_SHOW);
                    intent.putExtra(BROADCAST_IDENTIFIER_KEY, broadcastIdentifier);
                    LocalBroadcastManager.getInstance(mContext.getApplicationContext()).sendBroadcast(intent);

                }

                @Override
                public void onFailedToLoad() {
                }

                public void onClose() {
                    mMraidController.loadJavascript(WEB_VIEW_DID_CLOSE.getJavascript());
                    //finish();
                }

                @Override
                public void onExpand() {
                    // No-op. The interstitial is always expanded.
                }

                @Override
                public void onOpen() {
//                    broadcastAction(MraidActivity.this, getBroadcastIdentifier(),
//                            ACTION_INTERSTITIAL_CLICK);
                    
                    final long broadcastIdentifier = mAdConfiguration.getBroadcastIdentifier();
                  
                    Intent intent = new Intent(ACTION_INTERSTITIAL_CLICK);
                    intent.putExtra(BROADCAST_IDENTIFIER_KEY, broadcastIdentifier);
                    LocalBroadcastManager.getInstance(mContext.getApplicationContext()).sendBroadcast(intent);
                    
                }

                @Override
                public void onVideoPlayed(String url) {
                    if(url != null) {
                        interstitialListener.onVideoPlayed(url);          
                    }
                }
            });

            // Needed because the Activity provides the close button, not the controller. This
            // gets called if the creative calls mraid.useCustomClose.
            mMraidController.setUseCustomCloseListener(new UseCustomCloseListener() {
                public void useCustomCloseChanged(boolean useCustomClose) {
                    if (useCustomClose) {
                        //hideInterstitialCloseButton();
                    } else {
                        //showInterstitialCloseButton();
                    }
                }
            });

            
            mMraidController.loadContent(mHtmlData);
            
            mMraidController.setRootViewGroup(holder);
            
            return mMraidController.getAdContainer();
        
        
    }
    
	public void onInterstitialShown() {
		interstitialListener.onInterstitialShown();
	}
	
	public void onInterstitialClicked() {
	    
	    
		interstitialListener.onInterstitialClicked();	
	}

	public void onClosePostitialSession() {
		interstitialListener.onClosePostitialSession();	
	}

	public void onVideoPlayed(String url) {
		if(url != null) {
			interstitialListener.onVideoPlayed(url);
		}
	}

}