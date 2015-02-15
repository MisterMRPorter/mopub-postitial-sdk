package com.mopub.mobileads;

import android.content.Context;

import com.mopub.common.AdReport;

import static com.mopub.mobileads.CustomEventBanner.CustomEventBannerListener;

public class HtmlBannerWebView extends BaseHtmlWebView {
    public static final String EXTRA_AD_CLICK_DATA = "com.mopub.intent.extra.AD_CLICK_DATA";
	private HtmlWebViewClient mHtmlWebViewClient;

    public HtmlBannerWebView(Context context, AdReport adReport) {
        super(context, adReport);
    }

    public void init(CustomEventBannerListener customEventBannerListener, boolean isScrollable, String redirectUrl, String clickthroughUrl) {
        super.init(isScrollable);

        HtmlWebViewClient htmlWebViewClient = new HtmlWebViewClient(new HtmlBannerWebViewListener(customEventBannerListener), this, clickthroughUrl, redirectUrl);
        mHtmlWebViewClient = htmlWebViewClient;
        setWebViewClient(mHtmlWebViewClient);
     
    }  
    
    public HtmlWebViewClient getHtmlWebViewClient(){
    	return mHtmlWebViewClient;
    }

    static class HtmlBannerWebViewListener implements HtmlWebViewListener {
        private final CustomEventBannerListener mCustomEventBannerListener;

        public HtmlBannerWebViewListener(CustomEventBannerListener customEventBannerListener) {
            mCustomEventBannerListener = customEventBannerListener;
        }

        @Override
        public void onLoaded(BaseHtmlWebView htmlWebView) {
            mCustomEventBannerListener.onBannerLoaded(htmlWebView);
        }

        @Override
        public void onFailed(MoPubErrorCode errorCode) {
            mCustomEventBannerListener.onBannerFailed(errorCode);
        }

        @Override
        public void onClicked() {
            mCustomEventBannerListener.onBannerClicked();
        }

        @Override
        public void onCollapsed() {
            mCustomEventBannerListener.onBannerCollapsed();
        }

		@Override
		public void closePostitialSession() {		
			mCustomEventBannerListener.onClosePostitialSession();
		}

		@Override
		public void onVideoPlayed(String url) {
			mCustomEventBannerListener.onVideoPlayed(url);			
		}

    }

}
