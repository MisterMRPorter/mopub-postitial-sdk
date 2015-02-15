package com.mopub.mraid;


import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.view.View;
import android.view.ViewGroup;

import com.mopub.mobileads.AdViewController;
import com.mopub.mobileads.ResponseBodyInterstitial;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.Map;

import static com.mopub.common.DataKeys.HTML_RESPONSE_BODY_KEY;

import com.mopub.mobileads.MraidActivity;
import com.mopub.mraid.MraidController.UseCustomCloseListener;

import static com.mopub.mobileads.BaseInterstitialActivity.JavaScriptWebViewCallbacks.WEB_VIEW_DID_APPEAR;
import static com.mopub.mobileads.BaseInterstitialActivity.JavaScriptWebViewCallbacks.WEB_VIEW_DID_CLOSE;

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
    protected void extractExtras(Map<String, String> serverExtras) {
        mHtmlData = serverExtras.get(HTML_RESPONSE_BODY_KEY);
    }

    @Override
    protected void preRenderHtml(@NonNull CustomEventInterstitialListener
            customEventInterstitialListener) {
        MraidActivity.preRenderHtml(mContext, customEventInterstitialListener, mHtmlData);
        this.interstitialListener = customEventInterstitialListener;
        
    }

    @Override
    public void showInterstitial() {
        MraidActivity.start(mContext, mAdReport, mHtmlData, mBroadcastIdentifier);
    }

    @Override
    public View showInterstitialView(ViewGroup holder, final AdViewController adViewController) { 

            mMraidController = new MraidController(
                    mContext, mAdReport, PlacementType.INTERSTITIAL);
            
            mMraidController.setAdViewController(adViewController);

            mMraidController.setDebugListener(mDebugListener);
            mMraidController.setMraidListener(new MraidController.MraidListener() {
                @Override
                public void onLoaded(View view) {
                    // This is only done for the interstitial. Banners have a different mechanism
                    // for tracking third party impressions.
                    mMraidController.loadJavascript(WEB_VIEW_DID_APPEAR.getJavascript());
                    
                    final long broadcastIdentifier = adViewController.getBroadcastIdentifier();
                    
                    Intent intent = new Intent(ACTION_INTERSTITIAL_SHOW);
                    intent.putExtra(BROADCAST_IDENTIFIER_KEY, broadcastIdentifier);
                    LocalBroadcastManager.getInstance(mContext.getApplicationContext()).sendBroadcast(intent);

                }

                @Override
                public void onFailedToLoad() {
                }

                public void onClose() {
                    mMraidController.loadJavascript(WEB_VIEW_DID_CLOSE.getJavascript());
                }

                @Override
                public void onExpand() {
                    // No-op. The interstitial is always expanded.
                }

                @Override
                public void onOpen() {
                    
                    final long broadcastIdentifier = adViewController.getBroadcastIdentifier();
                  
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