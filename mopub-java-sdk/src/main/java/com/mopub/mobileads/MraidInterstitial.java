package com.mopub.mobileads;


import android.content.Intent;
import android.net.Uri;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.View;

import java.util.Map;

import com.mopub.mobileads.MraidActivity;
import com.mopub.mobileads.MraidView.ExpansionStyle;
import com.mopub.mobileads.MraidView.MraidListener;
import com.mopub.mobileads.MraidView.NativeCloseButtonStyle;
import com.mopub.mobileads.MraidView.PlacementType;
import com.mopub.mobileads.MraidView.ViewState;

import static com.mopub.mobileads.AdFetcher.HTML_RESPONSE_BODY_KEY;
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

    @Override
    protected void extractExtras(Map<String, String> serverExtras) {
        mHtmlData = Uri.decode(serverExtras.get(HTML_RESPONSE_BODY_KEY));
    }

    @Override
    protected void preRenderHtml(CustomEventInterstitialListener customEventInterstitialListener) {
        
        MraidActivity.preRenderHtml(mContext, customEventInterstitialListener, mHtmlData);
        this.interstitialListener = customEventInterstitialListener;
        
    }

    @Override
    protected void showInterstitial() {
        MraidActivity.start(mContext, mHtmlData, mAdConfiguration);
    }

    @Override
    public View showInterstitialView() {
        
        final MraidView mMraidView = 
            new MraidView(mContext, mAdConfiguration, ExpansionStyle.DISABLED, NativeCloseButtonStyle.ALWAYS_HIDDEN, PlacementType.INTERSTITIAL);
        

         	mMraidView.setMraidListener(new MraidView.BaseMraidListener() {
                public void onReady(MraidView view) {
                //	MraidInterstitial.this.onReady();
                	mMraidView.loadUrl(WEB_VIEW_DID_APPEAR.getUrl());
                }
                public void onFailure(MraidView view) {
                    
                    {
                        
                        final long broadcastIdentifier = mAdConfiguration.getBroadcastIdentifier();
                        
                        Intent intent = new Intent(ACTION_INTERSTITIAL_FAIL);
                        intent.putExtra(BROADCAST_IDENTIFIER_KEY, broadcastIdentifier);
                        LocalBroadcastManager.getInstance(mContext.getApplicationContext()).sendBroadcast(intent);
                        
                    }
                //    onFail();
                }
                public void onExpand(MraidView view) {
               // 	MraidInterstitial.this.onExpand();
                }
                public void onOpen(MraidView view) {
                //	MraidInterstitial.this.onOpen();
                	MraidInterstitial.this.onInterstitialClicked();
                }
                public void onClose(MraidView view, ViewState newViewState) {
                //	MraidInterstitial.this.onClose();
                	mMraidView.loadUrl(WEB_VIEW_DID_CLOSE.getUrl());
                }
    			@Override
    			public void onVideoPlayed(String url) {
    				MraidInterstitial.this.onVideoPlayed(url);
    			}
    			@Override
    			public void onClosePostitialSession(MraidView mView) {
    				MraidInterstitial.this.onClosePostitialSession();
    			}
            });
    
         	
        mMraidView.setOnCloseButtonStateChange(new MraidView.OnCloseButtonStateChangeListener() {
            public void onCloseButtonStateChange(MraidView view, boolean enabled) {
                if (enabled) {
                    //showInterstitialCloseButton();
                } else {
                	//hideInterstitialCloseButton();
                }
            }
        });
        
   
        mMraidView.loadHtmlData(mHtmlData);

        return mMraidView;
        
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