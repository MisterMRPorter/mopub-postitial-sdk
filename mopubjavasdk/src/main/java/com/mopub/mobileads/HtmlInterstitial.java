package com.mopub.mobileads;

//new imports
import java.util.Map;

import static com.mopub.common.DataKeys.CLICKTHROUGH_URL_KEY;
import static com.mopub.common.DataKeys.HTML_RESPONSE_BODY_KEY;
import static com.mopub.common.DataKeys.REDIRECT_URL_KEY;
import static com.mopub.common.DataKeys.SCROLLABLE_KEY;

//old imports
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import com.mopub.mobileads.MoPubActivity;
import com.mopub.mobileads.factories.HtmlInterstitialWebViewFactory;

import static com.mopub.mobileads.BaseInterstitialActivity.JavaScriptWebViewCallbacks.WEB_VIEW_DID_APPEAR;

public class HtmlInterstitial extends ResponseBodyInterstitial {
    
//old stuff
    static final String BROADCAST_IDENTIFIER_KEY = "broadcastIdentifier";
    static final String ACTION_INTERSTITIAL_FAIL = "com.mopub.action.interstitial.fail";
    static final String ACTION_INTERSTITIAL_SHOW = "com.mopub.action.interstitial.show";
    static final String ACTION_INTERSTITIAL_DISMISS = "com.mopub.action.interstitial.dismiss";
    static final String ACTION_INTERSTITIAL_CLICK = "com.mopub.action.interstitial.click";
    
    private String mHtmlData;
    private boolean mIsScrollable;
    private String mRedirectUrl;
    private String mClickthroughUrl;
    private CustomEventInterstitialListener listener;
	private AdViewController adViewController;
   
    @Override
    protected void extractExtras(Map<String, String> serverExtras) {
        mHtmlData = serverExtras.get(HTML_RESPONSE_BODY_KEY);
        mIsScrollable = Boolean.valueOf(serverExtras.get(SCROLLABLE_KEY));
        mRedirectUrl = serverExtras.get(REDIRECT_URL_KEY);
        mClickthroughUrl = serverExtras.get(CLICKTHROUGH_URL_KEY);
    }

    @Override
    protected void preRenderHtml(CustomEventInterstitialListener customEventInterstitialListener) {
        
        this.listener = customEventInterstitialListener;
        
        MoPubActivity.preRenderHtml(mContext, mAdReport, customEventInterstitialListener, mHtmlData);
    }

    @Override
    public void showInterstitial() {
		MoPubActivity.start(mContext, mHtmlData, mAdReport, mIsScrollable, mRedirectUrl, mClickthroughUrl, mBroadcastIdentifier);
    }

    @Override
    public View showInterstitialView(ViewGroup holder, AdViewController adViewController) {
        
        boolean isScrollable = mIsScrollable;
        String redirectUrl = mRedirectUrl;
        String clickthroughUrl = mClickthroughUrl;
        String htmlResponse = mHtmlData;
        
        this.adViewController = adViewController;
        
        final BroadcastingInterstitialListener broadcastingListener = new BroadcastingInterstitialListener(mContext);
        
        HtmlInterstitialWebView htmlInterstitialWebView = HtmlInterstitialWebViewFactory.create(mContext, mAdReport, broadcastingListener, isScrollable, redirectUrl, clickthroughUrl);
        
        broadcastingListener.setWebView(htmlInterstitialWebView);
        
        htmlInterstitialWebView.enablePlugins(true);     
        
        htmlInterstitialWebView.loadHtmlResponse(htmlResponse);
        
        htmlInterstitialWebView.getHtmlWebViewClient().timerStart();
        
        htmlInterstitialWebView.getHtmlWebViewClient().setHolder(holder);
        
        htmlInterstitialWebView.getHtmlWebViewClient().setAdViewController(adViewController);
        
        adViewController.setHtmlWebViewClient(htmlInterstitialWebView.getHtmlWebViewClient());
        
        return htmlInterstitialWebView;
        
    }

    
    private class BroadcastingInterstitialListener implements CustomEventInterstitialListener {
        private HtmlInterstitialWebView mHtmlInterstitialWebView;
        private Context context;

        public BroadcastingInterstitialListener(final Context context) {
            this.context = context;          
        }

        @Override
        public void onInterstitialLoaded() {
        	
            mHtmlInterstitialWebView.loadUrl(WEB_VIEW_DID_APPEAR.getUrl());
            
        	listener.onInterstitialLoaded(); 
        	
        }

        @Override
        public void onInterstitialFailed(MoPubErrorCode errorCode) {
        	
            {
                
                final long broadcastIdentifier = adViewController.getBroadcastIdentifier();
                
                Intent intent = new Intent(ACTION_INTERSTITIAL_FAIL);
                intent.putExtra(BROADCAST_IDENTIFIER_KEY, broadcastIdentifier);
                LocalBroadcastManager.getInstance(context.getApplicationContext()).sendBroadcast(intent);
                
            }
            
        	listener.onInterstitialFailed(errorCode);
        	
        }

        @Override
        public void onInterstitialShown() {
        	
        }

        @Override
        public void onInterstitialClicked() {
        	
            
            {
                
                final long broadcastIdentifier = adViewController.getBroadcastIdentifier();
                
                Intent intent = new Intent(ACTION_INTERSTITIAL_CLICK);
                intent.putExtra(BROADCAST_IDENTIFIER_KEY, broadcastIdentifier);
                LocalBroadcastManager.getInstance(context.getApplicationContext()).sendBroadcast(intent);
                
            }
        	
        }

        @Override
        public void onLeaveApplication() {
        	      	
        }

        @Override
        public void onInterstitialDismissed() {     	
        	
        }

		@Override
		public void onClosePostitialSession() {		
		    
		    listener.onClosePostitialSession();
		    
		}

		@Override
		public void onVideoPlayed(String url) {
			
			listener.onVideoPlayed(url);
			
		}
		
        public void setWebView(HtmlInterstitialWebView htmlInterstitialWebView) {
            this.mHtmlInterstitialWebView = htmlInterstitialWebView;
            
        }

    }
}
