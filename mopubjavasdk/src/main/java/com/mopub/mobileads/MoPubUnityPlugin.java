package com.mopub.mobileads;

import android.app.Activity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.RelativeLayout;
import com.unity3d.player.UnityPlayer;

import static com.mopub.mobileads.MoPubInterstitial.InterstitialAdListener;
import static com.mopub.mobileads.MoPubView.BannerAdListener;

public class MoPubUnityPlugin implements BannerAdListener, InterstitialAdListener {
    private static MoPubUnityPlugin sInstance;

    // used for testing directly in Eclipse
    public Activity mActivity;

    private static String TAG = "MoPub";
    private MoPubInterstitial mMoPubInterstitial;
    private MoPubView mMoPubView;
    private RelativeLayout mLayout;

    public static MoPubUnityPlugin instance() {
        if (sInstance == null)
            sInstance = new MoPubUnityPlugin();
        return sInstance;
    }

    /*
     * Banners API
     */
    public void showBanner(final String adUnitId, final int alignment) {
        getActivity().runOnUiThread(new Runnable() {
            public void run() {
                if (mMoPubView != null)
                    return;

                mMoPubView = new MoPubView(getActivity());
                mMoPubView.setAdUnitId(adUnitId);
                mMoPubView.setBannerAdListener(MoPubUnityPlugin.this);
                mMoPubView.loadAd();

                prepLayout(alignment);

                mLayout.addView(mMoPubView);
                getActivity().addContentView(mLayout, new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));

                mLayout.setVisibility(RelativeLayout.VISIBLE);
            }
        });
    }

    public void hideBanner(final boolean shouldHide) {
        if (mMoPubView == null)
            return;

        getActivity().runOnUiThread(new Runnable() {
            public void run() {
                if (shouldHide) {
                    mMoPubView.setVisibility(View.GONE);
                } else {
                    mMoPubView.setVisibility(View.VISIBLE);
                }
            }
        });
    }

    public void setBannerKeywords(final String keywords) {
        getActivity().runOnUiThread(new Runnable() {
            public void run() {
                if (mMoPubView == null)
                    return;

                mMoPubView.setKeywords(keywords);
                mMoPubView.loadAd();
            }
        });
    }

    public void destroyBanner() {
        getActivity().runOnUiThread(new Runnable() {
            public void run() {
                if (mMoPubView == null || mLayout == null)
                    return;

                mLayout.removeAllViews();
                mLayout.setVisibility(LinearLayout.GONE);
                mMoPubView.destroy();
                mMoPubView = null;
            }
        });
    }

    /*
     * Interstitials API
     */
    public void requestInterstitalAd(final String adUnitId) {
        getActivity().runOnUiThread(new Runnable() {
            public void run() {
                mMoPubInterstitial = new MoPubInterstitial(getActivity(), adUnitId);
                mMoPubInterstitial.setInterstitialAdListener(MoPubUnityPlugin.this);
                mMoPubInterstitial.load();
            }
        });
    }

    public void showInterstitalAd() {
        getActivity().runOnUiThread(new Runnable() {
            public void run() {
                mMoPubInterstitial.show();
            }
        });
    }

    public void reportApplicationOpen() {
        getActivity().runOnUiThread(new Runnable() {
            public void run() {
                new MoPubConversionTracker().reportAppOpen(getActivity());
            }
        });
    }

    /*
     * BannerAdListener implementation
     */
    @Override
    public void onBannerLoaded(MoPubView banner) {
        UnitySendMessage("MoPubAndroidManager", "onAdLoaded", "");

        // re-center the ad
        int height = mMoPubView.getAdHeight();
        int width = mMoPubView.getAdWidth();
        float density = getScreenDensity();

        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) mMoPubView.getLayoutParams();
        params.width = (int) (width * density);
        params.height = (int) (height * density);

        mMoPubView.setLayoutParams(params);
    }

    @Override
    public void onBannerFailed(MoPubView banner, MoPubErrorCode errorCode) {
        UnitySendMessage("MoPubAndroidManager", "onAdFailed", "");
    }

    @Override public void onBannerClicked(MoPubView banner) {
        UnitySendMessage("MoPubAndroidManager", "onAdClicked", "");
    }

    @Override public void onBannerExpanded(MoPubView banner) {
        UnitySendMessage("MoPubAndroidManager", "onAdExpanded", "");
    }

    @Override public void onBannerCollapsed(MoPubView banner) {
        UnitySendMessage("MoPubAndroidManager", "onAdCollapsed", "");
    }

    /*
     * InterstitialAdListener implementation
     */
    @Override
    public void onInterstitialLoaded(MoPubInterstitial interstitial) {
        UnitySendMessage("MoPubAndroidManager", "onInterstitialLoaded", "");
    }

    @Override
    public void onInterstitialFailed(MoPubInterstitial interstitial, MoPubErrorCode errorCode) {
        UnitySendMessage("MoPubAndroidManager", "onInterstitialFailed", "");
    }

    @Override public void onInterstitialShown(MoPubInterstitial interstitial) {
        UnitySendMessage("MoPubAndroidManager", "onInterstitialShown", "");
    }

    @Override public void onInterstitialClicked(MoPubInterstitial interstitial) {
        UnitySendMessage("MoPubAndroidManager", "onInterstitialClicked", "");
    }

    @Override public void onInterstitialDismissed(MoPubInterstitial interstitial) {
        UnitySendMessage("MoPubAndroidManager", "onInterstitialDismissed", "");
    }



    private Activity getActivity() {
        if (mActivity != null)
            return mActivity;

        return UnityPlayer.currentActivity;
    }

    private void UnitySendMessage(String go, String m, String p) {
        if (mActivity != null) {
            Log.i(TAG, "UnitySendMessage: " + go + ", " + m + ", " + p);
        } else {
            UnityPlayer.UnitySendMessage(go, m, p);
        }
    }

    private float getScreenDensity() {
        DisplayMetrics metrics = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(metrics);

        return metrics.density;
    }

    private void prepLayout(int alignment) {
        // create a RelativeLayout and add the ad view to it
        if (mLayout == null) {
            mLayout = new RelativeLayout(getActivity());
        } else {
            // remove the layout if it has a parent
            FrameLayout parentView = (FrameLayout) mLayout.getParent();
            if (parentView != null)
                parentView.removeView(mLayout);
        }

        int gravity = 0;

        switch (alignment) {
            case 0:
                gravity = Gravity.TOP | Gravity.LEFT;
                break;
            case 1:
                gravity = Gravity.TOP | Gravity.CENTER_HORIZONTAL;
                break;
            case 2:
                gravity = Gravity.TOP | Gravity.RIGHT;
                break;
            case 3:
                gravity = Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL;
                break;
            case 4:
                gravity = Gravity.BOTTOM | Gravity.LEFT;
                break;
            case 5:
                gravity = Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL;
                break;
            case 6:
                gravity = Gravity.BOTTOM | Gravity.RIGHT;
                break;
        }

        mLayout.setGravity(gravity);
    }

	@Override
	public void onClosePostitialSession(MoPubInterstitial interstitial) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onVideoPlayed(String url) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onClosePostitialSession(MoPubView banner) {
		// TODO Auto-generated method stub
		
	}
}