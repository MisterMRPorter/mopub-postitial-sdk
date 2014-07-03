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

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import java.util.*;

import com.mopub.mobileads.MoPubActivity;
import com.mopub.mobileads.factories.HtmlInterstitialWebViewFactory;

import static com.mopub.mobileads.AdFetcher.CLICKTHROUGH_URL_KEY;
import static com.mopub.mobileads.AdFetcher.HTML_RESPONSE_BODY_KEY;
import static com.mopub.mobileads.AdFetcher.REDIRECT_URL_KEY;
import static com.mopub.mobileads.AdFetcher.SCROLLABLE_KEY;
import static com.mopub.mobileads.BaseInterstitialActivity.JavaScriptWebViewCallbacks.WEB_VIEW_DID_APPEAR;
import static com.mopub.mobileads.EventForwardingBroadcastReceiver.ACTION_INTERSTITIAL_CLICK;
import static com.mopub.mobileads.EventForwardingBroadcastReceiver.ACTION_INTERSTITIAL_FAIL;
import static com.mopub.mobileads.EventForwardingBroadcastReceiver.broadcastAction;
import static com.mopub.mobileads.HtmlWebViewClient.MOPUB_FAIL_LOAD;
import static com.mopub.mobileads.HtmlWebViewClient.MOPUB_FINISH_LOAD;

public class HtmlInterstitial extends ResponseBodyInterstitial {
    
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
   
    @Override
    protected void extractExtras(Map<String, String> serverExtras) {
        mHtmlData = Uri.decode(serverExtras.get(HTML_RESPONSE_BODY_KEY));
        mIsScrollable = Boolean.valueOf(serverExtras.get(SCROLLABLE_KEY));
        mRedirectUrl = serverExtras.get(REDIRECT_URL_KEY);
        mClickthroughUrl = serverExtras.get(CLICKTHROUGH_URL_KEY);
    }

    @Override
    protected void preRenderHtml(CustomEventInterstitialListener customEventInterstitialListener) {
        
        this.listener = customEventInterstitialListener;
        
        MoPubActivity.preRenderHtml(mContext, customEventInterstitialListener, mHtmlData);
    }

    @Override
    protected void showInterstitial() {
        MoPubActivity.start(mContext, mHtmlData, mIsScrollable, mRedirectUrl, mClickthroughUrl, mAdConfiguration);
    }

    @Override
    public View showInterstitialView() {
        
        boolean isScrollable = mIsScrollable;
        String redirectUrl = mRedirectUrl;
        String clickthroughUrl = mClickthroughUrl;
        String htmlResponse = mHtmlData;

        final BroadcastingInterstitialListener broadcastingListener = new BroadcastingInterstitialListener(mContext, mAdConfiguration);
        
        HtmlInterstitialWebView htmlInterstitialWebView = HtmlInterstitialWebViewFactory.create(mContext, broadcastingListener, isScrollable, redirectUrl, clickthroughUrl, mAdConfiguration);
        
        broadcastingListener.setWebView(htmlInterstitialWebView);
        
        htmlInterstitialWebView.enablePlugins(true);
        
        htmlInterstitialWebView.loadHtmlResponse(htmlResponse);
        
        return htmlInterstitialWebView;
        
    }

    
    private class BroadcastingInterstitialListener implements CustomEventInterstitialListener {
        private HtmlInterstitialWebView mHtmlInterstitialWebView;
        private final AdConfiguration mAdConfiguration;
        private Context context;

        public BroadcastingInterstitialListener(
            final Context context,
            final AdConfiguration mAdConfiguration
        ) {
            
            this.mAdConfiguration = mAdConfiguration;
            this.context = context;
            
        }

        @Override
        public void onInterstitialLoaded() {
        	
            mHtmlInterstitialWebView.loadUrl(WEB_VIEW_DID_APPEAR.getUrl());
            
        	listener.onInterstitialLoaded(); 
        	
        }

        public void setWebView(HtmlInterstitialWebView htmlInterstitialWebView) {
            this.mHtmlInterstitialWebView = htmlInterstitialWebView;
            
        }

        @Override
        public void onInterstitialFailed(MoPubErrorCode errorCode) {
        	
            {
                
                final long broadcastIdentifier = mAdConfiguration.getBroadcastIdentifier();
                
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
                
                final long broadcastIdentifier = mAdConfiguration.getBroadcastIdentifier();
                
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

    }
}
