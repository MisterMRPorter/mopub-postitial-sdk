package com.mopub.mobileads;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Handler;
import android.util.Log;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.RelativeLayout;

import com.IQzone.android.resource.layout.ProgressInflater;
import com.mopub.common.AdReport;
import com.mopub.common.MoPubBrowser;
import com.mopub.common.Preconditions;
import com.mopub.common.logging.MoPubLog;
import com.mopub.common.util.Intents;
import com.mopub.exceptions.IntentNotResolvableException;
import com.mopub.exceptions.UrlParseException;

import static com.mopub.mobileads.MoPubErrorCode.UNSPECIFIED;

public class HtmlWebViewClient extends WebViewClient {
    public static final String MOPUB_FINISH_LOAD = "mopub://finishLoad";
    public static final String MOPUB_FAIL_LOAD = "mopub://failLoad";

    private final Context mContext;
    private HtmlWebViewListener mHtmlWebViewListener;
    private BaseHtmlWebView mHtmlWebView;
    private final String mClickthroughUrl;
    private final String mRedirectUrl;
    private ViewGroup holder;
    private AdViewController adViewController;
	private boolean timerRunning = false;
    protected boolean timerDone = false;
	protected boolean returnBoolean = true;
	private boolean wasClicked = false;

    HtmlWebViewClient(HtmlWebViewListener htmlWebViewListener, BaseHtmlWebView htmlWebView, String clickthrough, String redirect) {
        mHtmlWebViewListener = htmlWebViewListener;
        mHtmlWebView = htmlWebView;
        mClickthroughUrl = clickthrough;
        mRedirectUrl = redirect;
        mContext = htmlWebView.getContext();
    }

    /**
     * Called upon user click, when the WebView attempts to load a new URL. Attempts to handle mopub
     * and phone-specific schemes, open mopubnativebrowser links in the device browser, deep-links
     * in the corresponding application, and all other links in the MoPub in-app browser.
     */
    @Override
    public boolean shouldOverrideUrlLoading(WebView view, String url) {
        MoPubLog.d("Ad clicked. Click URL: " + url);

        if (handleSpecialMoPubScheme(url) || handlePhoneScheme(url)) {
            return true;
        }

        MoPubLog.d("Ad clicked. Click URL: " + url);
	        
        // MoPubNativeBrowser URLs
        if (Intents.isNativeBrowserScheme(url)) {
            final String errorMessage = "Unable to load mopub native browser url: " + url;
            try {
                final Intent intent = Intents.intentForNativeBrowserScheme(url);
                launchIntentForUserClick(mContext, intent, errorMessage);
            } catch (UrlParseException e) {
                MoPubLog.d(errorMessage + ". " + e.getMessage());
            }

            return true;
        }

        // Non-http(s) URLs
        if (!Intents.isHttpUrl(url) && Intents.canHandleApplicationUrl(mContext, url)) {
            launchApplicationUrl(url);
            return true;
        }

        showMoPubBrowserForUrl(url);
        return true;
    }

    @Override
    public void onPageStarted(WebView view, String url, Bitmap favicon) {
        // If the URL being loaded shares the redirectUrl prefix, open it in the browser.
        if (mRedirectUrl != null && url.startsWith(mRedirectUrl)) {
            view.stopLoading();
            showMoPubBrowserForUrl(url);
        }
    }

    private boolean isSpecialMoPubScheme(String url) {
        return url.startsWith("mopub://");
    }

    private boolean handleSpecialMoPubScheme(String url) {
        if (!isSpecialMoPubScheme(url)) {
            return false;
        }
        Uri uri = Uri.parse(url);
        String host = uri.getHost();

        if ("finishLoad".equals(host)) {
            mHtmlWebViewListener.onLoaded(mHtmlWebView);
        } else if ("close".equals(host)) {
            mHtmlWebViewListener.onCollapsed();
        } else if ("failLoad".equals(host)) {
            mHtmlWebViewListener.onFailed(UNSPECIFIED);
        } else if ("custom".equals(host)) {
            handleCustomIntentFromUri(uri);
        }

        return true;
    }

    private boolean isPhoneScheme(String url) {
        return url.startsWith("tel:") || url.startsWith("voicemail:") ||
                url.startsWith("sms:") || url.startsWith("mailto:") ||
                url.startsWith("geo:") || url.startsWith("google.streetview:");
    }

    private boolean handlePhoneScheme(String url) {
        if (!isPhoneScheme(url)) {
            return false;
        }
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        String errorMessage = "Could not handle intent with URI: " + url
                + ". Is this intent supported on your phone?";

        launchIntentForUserClick(mContext, intent, errorMessage);

        return true;
    }

    private boolean launchApplicationUrl(String url) {
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK
        		|Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);

        String errorMessage = "Unable to open intent.";

        return launchIntentForUserClick(mContext, intent, errorMessage);
    }

    private void showMoPubBrowserForUrl(String url) {
        if (url == null || url.equals("")) {
            url = "about:blank";
        }
        MoPubLog.d("Final URI to show in browser: " + url);

        final Bundle extras = new Bundle();
        extras.putString(MoPubBrowser.DESTINATION_URL_KEY, url);
        Intent intent = Intents.getStartActivityIntent(mContext, MoPubBrowser.class, extras);
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK
				|Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);

        String errorMessage = "Could not handle intent action. "
                + ". Perhaps you forgot to declare com.mopub.common.MoPubBrowser"
                + " in your Android manifest file.";

        boolean handledByMoPubBrowser = launchIntentForUserClick(mContext, intent, errorMessage);

        if (!handledByMoPubBrowser) {
            intent = new Intent(Intent.ACTION_VIEW, Uri.parse("about:blank"));
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
            		|Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
            launchIntentForUserClick(mContext, intent, null);
        }
    }

    private void handleCustomIntentFromUri(Uri uri) {
        String action;
        String adData;
        try {
            action = uri.getQueryParameter("fnc");
            adData = uri.getQueryParameter("data");
        } catch (UnsupportedOperationException e) {
            MoPubLog.w("Could not handle custom intent with uri: " + uri);
            return;
        }

        Intent customIntent = new Intent(action);
        customIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK
        		|Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
        customIntent.putExtra(HtmlBannerWebView.EXTRA_AD_CLICK_DATA, adData);

        String errorMessage = "Could not handle custom intent: " + action
                + ". Is your intent spelled correctly?";

        launchIntentForUserClick(mContext, customIntent, errorMessage);
    }

    boolean launchIntentForUserClick(@Nullable final Context context, @NonNull final Intent intent,
            @Nullable final String errorMessage) {
        Preconditions.NoThrow.checkNotNull(intent);
        
        if(wasClicked == true) {
        	return false;
        }

        if (context == null) {
            MoPubLog.d(errorMessage);
            return false;
        }

        if (!mHtmlWebView.wasClicked()) {
            return false;
        }      
        
        if(adViewController.isPostitial()){
        	
            wasClicked  = true;
        	
	        final RelativeLayout progressSpin = (RelativeLayout) new ProgressInflater(mContext).getView();   
	        final RelativeLayout blackBackground = new RelativeLayout(mContext); 
	        final RelativeLayout.LayoutParams wrapParams =
                    new RelativeLayout.LayoutParams(
                        LayoutParams.MATCH_PARENT,
                        LayoutParams.MATCH_PARENT
                    );                
            wrapParams.addRule(RelativeLayout.CENTER_IN_PARENT);
	        blackBackground.setBackgroundColor(Color.BLACK);
	        holder.addView(blackBackground);
	        holder.addView(progressSpin);
	                  
	        final Handler handler = new Handler();
	              
	        final Runnable progressSpinCheck = new Runnable() {
	    
	                    @Override
	                    public void run() {
	                        
	                    	if(timerDone) {
	                        	
	                    		if(isAppOnForeground(context)) {	                    		
		            		        try {		            		        	
		            		            Intents.startActivity(context, intent);
		            		            mHtmlWebViewListener.onClicked();
		            		            mHtmlWebView.onResetUserClick();
		            		            returnBoolean  = true;
		            		            final Runnable wasClickedRunner = new Runnable() {   
		            	                    @Override
		            	                    public void run() {
		            	                    	wasClicked = false;
		            	                    }
		            		            };
		            		            handler.postDelayed(wasClickedRunner, 2000);
		            		        } catch (IntentNotResolvableException e) {
		            		            MoPubLog.d(errorMessage);
		            		            returnBoolean = false;
		            		        }                     		
	                    		}
	                    		else{
	                    			returnBoolean = false;
	                    		}
	                            
	                        }
	                        else {
	                            handler.postDelayed(this, 100);
	                        }
	                        
	                    }
	        };
	                    
	        handler.postDelayed(progressSpinCheck, 100);
	        
	        return returnBoolean;

        }
        else{
	        try {
	            Intents.startActivity(context, intent);
	            mHtmlWebViewListener.onClicked();
	            mHtmlWebView.onResetUserClick();
	            return true;
	        } catch (IntentNotResolvableException e) {
	            MoPubLog.d(errorMessage);
	            return false;
	        } 
        }

    }
    
    //POSTITIAL TIMER
    public void timerStart() {
        
    	timerRunning = true;
        timerDone = false;
        
        Timer timer = new Timer();
        
        timer.schedule(new TimerTask() {
            @Override
            public void run() {           
               timerDone = true;
               timerRunning = false;
            }
        }, 7000);
        
    }
    
    public boolean getWasClicked() {
    	return wasClicked;
    }

    public void setHolder(ViewGroup holder) {
        this.holder = holder;
    }

	public void setAdViewController(AdViewController adViewController) {
		this.adViewController = adViewController;	
	}
	
	public boolean getTimerRunning() {
		return timerRunning;
	}
	
	public boolean getTimerDone() {
		return timerDone;
	}
	
	private boolean isAppOnForeground(Context context) {
	    ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
	    List<RunningAppProcessInfo> appProcesses = activityManager.getRunningAppProcesses();
	    if (appProcesses == null) {
	      return false;
	    }
	    final String packageName = context.getPackageName();
	    for (RunningAppProcessInfo appProcess : appProcesses) {
	      if (appProcess.importance == RunningAppProcessInfo.IMPORTANCE_FOREGROUND && appProcess.processName.equals(packageName)) {
	        return true;
	      }
	    }
	    return false;
	 }
	
	
	
}
