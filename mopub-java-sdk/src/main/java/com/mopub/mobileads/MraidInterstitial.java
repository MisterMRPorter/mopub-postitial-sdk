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


import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.view.View;

import java.util.*;

import com.mopub.mobileads.MraidActivity;
import com.mopub.mobileads.MraidView.ExpansionStyle;
import com.mopub.mobileads.MraidView.MraidListener;
import com.mopub.mobileads.MraidView.NativeCloseButtonStyle;
import com.mopub.mobileads.MraidView.PlacementType;
import com.mopub.mobileads.MraidView.ViewState;

import static com.mopub.mobileads.MoPubErrorCode.MRAID_LOAD_ERROR;

import static com.mopub.mobileads.AdFetcher.HTML_RESPONSE_BODY_KEY;

class MraidInterstitial extends ResponseBodyInterstitial {
    private String mHtmlData;
    CustomEventInterstitialListener interstitialListener;

    @Override
    protected void extractExtras(Map<String, String> serverExtras) {
        mHtmlData = Uri.decode(serverExtras.get(HTML_RESPONSE_BODY_KEY));
    }

    @Override
    protected void preRenderHtml(CustomEventInterstitialListener customEventInterstitialListener) {
        MraidActivity.preRenderHtml(mContext, customEventInterstitialListener, mHtmlData);
    }

    @Override
    protected void showInterstitial() {
        MraidActivity.start(mContext, mHtmlData, mAdConfiguration);
    }

    @Override
    public View showInterstitialView() {
        
//        final Intent intent = new Intent(mContext, MraidActivity.class);
//        intent.putExtra(HTML_RESPONSE_BODY_KEY, mHtmlData);
//        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
//        		|Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
//        try {
//            mContext.startActivity(intent);
//        } catch (ActivityNotFoundException anfe) {
//            Log.d("MraidInterstitial", "MraidActivity.class not found. Did you declare MraidActivity in your manifest?");
//        }
        
        MraidView mMraidView = 
            new MraidView(mContext, mAdConfiguration, ExpansionStyle.DISABLED, NativeCloseButtonStyle.AD_CONTROLLED, PlacementType.INTERSTITIAL);


        mMraidView.setMraidListener(new MraidView.BaseMraidListener(){
            public void onReady(MraidView view) {
                //showInterstitialCloseButton();
            }
            public void onClose(MraidView view, ViewState newViewState) {
                //finish();
            }

        });
        
        

        mMraidView.setOnCloseButtonStateChange(new MraidView.OnCloseButtonStateChangeListener() {
            public void onCloseButtonStateChange(MraidView view, boolean enabled) {
                if (enabled) {
//                    showInterstitialCloseButton();
                } else {
//                    hideInterstitialCloseButton();
                }
            }
        });
        
        mMraidView.loadHtmlData(mHtmlData);

        return mMraidView;
        
    }

}