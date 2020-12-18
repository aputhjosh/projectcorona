package com.buildbox.adapter.admob;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;

import com.buildbox.AdIntegratorManager;
import com.buildbox.adapter.AdIntegratorInterface;
import com.google.ads.mediation.admob.AdMobAdapter;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.google.android.gms.ads.rewarded.RewardItem;
import com.google.android.gms.ads.rewarded.RewardedAd;
import com.google.android.gms.ads.rewarded.RewardedAdCallback;
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback;

import java.lang.ref.WeakReference;
import java.util.HashMap;

public class AdIntegrator implements AdIntegratorInterface {

    private static final String TAG = "AdIntegratorAdmob";

    private static String bannerID;
    private static String interstitialID;
    private static String rewardedVideoID;

    private static String adNetworkId = "admob";
    private static WeakReference<Activity> activity;
    private static AdView adMobBanner;
    private static InterstitialAd adMobInterstitial;
    private static RewardedAd admobRewardedVideo;
    
    private RelativeLayout adContainerView;
    private boolean userConsent;
    private boolean isNewBannerLoaded;

    @Override
    public void initAds(HashMap<String, String> initValues, WeakReference<Activity> act) {
        activity = act;
        bannerID = initValues.get("Banner ID");
        interstitialID = initValues.get("Interstitial ID");
        rewardedVideoID = initValues.get("Rewarded Video ID");
        if (bannerID == null || interstitialID == null || rewardedVideoID == null) {
            Log.e(TAG, "Network sdk not configured correctly");
            Log.e(TAG, "Initializing pairs are : " + initValues);
            if (bannerID == null) {
                Log.e(TAG, "Banner ID is not found in keys");
            }
            if (interstitialID == null) {
                Log.e(TAG, "Interstitial ID is not found in keys");

            }
            if (rewardedVideoID == null) {
                Log.e(TAG, "Rewarded Video ID is not found in keys");
            }
            networkFailed();
            return;
        }

        MobileAds.initialize(activity.get(), new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {
                Log.d(TAG, "Network configured with status: " + initializationStatus);
                networkLoaded();
            }
        });
        FrameLayout frameLayout = activity.get().findViewById(android.R.id.content);
        adContainerView = new RelativeLayout(activity.get());
        frameLayout.addView(adContainerView);
        RelativeLayout.LayoutParams bannerLayoutParams = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT);
        bannerLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        bannerLayoutParams.addRule(RelativeLayout.CENTER_HORIZONTAL, RelativeLayout.TRUE);

        adMobBanner = new AdView(activity.get());
        adMobBanner.setLayoutParams(bannerLayoutParams);
        adMobBanner.setAdSize(AdSize.SMART_BANNER);
        adMobBanner.setAdUnitId(bannerID);
        adMobBanner.setAdListener(new AdListener() {
            @Override
            public void onAdClosed() {
                Log.d(TAG, "Banner closed");
            }

            @Override
            public void onAdFailedToLoad(int var1) {
                Log.d(TAG, "Banner failed to load with status: " + var1);
                bannerFailed();
            }

            @Override
            public void onAdLeftApplication() {
            }

            @Override
            public void onAdOpened() {
                Log.d(TAG, "Banner opened");
            }

            @Override
            public void onAdLoaded() {
                Log.d(TAG, "Banner loaded");
                bannerLoaded();

                if (adMobBanner.getVisibility() == View.VISIBLE) {
                    isNewBannerLoaded = false;
                    AdIntegratorManager.bannerImpression(adNetworkId);
                }
                else {
                    isNewBannerLoaded = true;
                }
            }

            @Override
            public void onAdClicked() {
            }

            @Override
            public void onAdImpression() {
                // This does not get called for AdMob Banner ads
            }
        });
        adContainerView.addView(adMobBanner);
        adMobBanner.setVisibility(View.GONE);
        adMobInterstitial = new InterstitialAd(activity.get());
        adMobInterstitial.setAdUnitId(interstitialID);
        adMobInterstitial.setAdListener(new AdListener() {
            @Override
            public void onAdClosed() {
                Log.d(TAG, "Interstitial closed");
                interstitialClosed();
            }

            @Override
            public void onAdFailedToLoad(int var1) {
                Log.d(TAG, "Interstitial failed to load with status: " + var1);
                interstitialFailed();
            }

            @Override
            public void onAdLeftApplication() {
            }

            @Override
            public void onAdOpened() {
                Log.d(TAG, "Interstitial opened");
            }

            @Override
            public void onAdLoaded() {
                interstitialLoaded();
                Log.d(TAG, "Interstitial loaded");
            }

            @Override
            public void onAdClicked() {
            }

            @Override
            public void onAdImpression() {
                AdIntegratorManager.interstitialImpression(adNetworkId);
            }
        });
    }


    @Override
    public void initBanner() {
        adMobBanner.loadAd(getAdRequest());
    }

    @Override
    public void showBanner() {
        Log.d(TAG, "showBanner");
        if (adMobBanner.getVisibility() != View.VISIBLE && isNewBannerLoaded) {
            isNewBannerLoaded = false;
            AdIntegratorManager.bannerImpression(adNetworkId);
        }

        adMobBanner.setVisibility(View.VISIBLE);
    }

    @Override
    public void hideBanner() {
        adMobBanner.setVisibility(View.GONE);
    }

    @Override
    public boolean isBannerVisible() {
        return true;
    }

    @Override
    public boolean isRewardedVideoAvailable() {
        return true;
    }

    @Override
    public void showInterstitial() {
        if (adMobInterstitial.isLoaded()) {
            adMobInterstitial.show();
        }
    }


    @Override
    public void initInterstitial() {
        adMobInterstitial.loadAd(getAdRequest());
    }

    @Override
    public void initRewardedVideo() {
        // RewardedAd is a one-time-use object, so a new instance should be created to request another ad
        admobRewardedVideo = new RewardedAd(activity.get(), rewardedVideoID);

        admobRewardedVideo.loadAd(getAdRequest(), new RewardedAdLoadCallback() {
            @Override
            public void onRewardedAdLoaded() {
                rewardedVideoLoaded();
                Log.d(TAG, "RewardedAd loaded");
            }

            @Override
            public void onRewardedAdFailedToLoad(int var1) {
                Log.d(TAG, "RewardedAd failed to load with status: " + var1);
                rewardedVideoFailed();
            }
        });
    }

    @Override
    public void setUserConsent(boolean consentGiven) {
        userConsent = consentGiven;
    }

    private AdRequest getAdRequest() {
        if (userConsent) {
            return new AdRequest.Builder().build();
        } else {
            Bundle extras = new Bundle();
            extras.putString("npa", "1");
            return new AdRequest.Builder()
                    .addNetworkExtrasBundle(AdMobAdapter.class, extras)
                    .build();
        }
    }

    @Override
    public void showRewardedVideo() {
        if (admobRewardedVideo.isLoaded()) {
            admobRewardedVideo.show(activity.get(), new RewardedAdCallback() {
                @Override
                public void onRewardedAdOpened() {
                    Log.d(TAG, "RewardedAd opened");
                    super.onRewardedAdOpened();
                    AdIntegratorManager.rewardedVideoImpression(adNetworkId);
                }

                @Override
                public void onRewardedAdClosed() {
                    Log.d(TAG, "RewardedAd closed");
                    super.onRewardedAdClosed();
                    rewardedVideoDidEnd(true);
                }

                @Override
                public void onUserEarnedReward(@NonNull RewardItem rewardItem) {
                    rewardedVideoDidReward(true);
                }

                @Override
                public void onRewardedAdFailedToShow(int errorCode) {
                    Log.d(TAG, "RewardedAd failed to show with status: " + errorCode);
                    rewardedVideoDidEnd(false);
                }
            });
        }
    }


    @Override
    public void interstitialClosed() {
        Log.d(TAG, "interstitial closed");
        AdIntegratorManager.interstitialClosed(adNetworkId);
    }

    @Override
    public void rewardedVideoDidReward(boolean value) {
        Log.d(TAG, "rewarded video did reward " + value);
        AdIntegratorManager.rewardedVideoDidReward(adNetworkId, value);
    }

    @Override
    public void rewardedVideoDidEnd(boolean value) {
        Log.d(TAG, "rewarded video did end " + value);
        AdIntegratorManager.rewardedVideoDidEnd(adNetworkId, value);
    }

    @Override
    public void networkLoaded() {
        Log.d(TAG, "Network loaded");
        AdIntegratorManager.networkLoaded(adNetworkId);
    }

    @Override
    public void bannerLoaded() {
        AdIntegratorManager.bannerLoaded(adNetworkId);
    }

    @Override
    public void interstitialLoaded() {
        Log.d(TAG, "interstitial loaded");
        AdIntegratorManager.interstitialLoaded(adNetworkId);
    }

    @Override
    public void rewardedVideoLoaded() {
        Log.d(TAG, "rewarded loaded");
        AdIntegratorManager.rewardedVideoLoaded(adNetworkId);
    }

    @Override
    public void networkFailed() {
        Log.d(TAG, "network failed");
        AdIntegratorManager.networkFailed(adNetworkId);
    }

    @Override
    public void bannerFailed() {
        Log.d(TAG, "banner failed");
        AdIntegratorManager.bannerFailed(adNetworkId);
    }

    @Override
    public void interstitialFailed() {
        Log.d(TAG, "interstitial failed");
        AdIntegratorManager.interstitialFailed(adNetworkId);
    }

    @Override
    public void rewardedVideoFailed() {
        Log.d(TAG, "rewarded video failed");
        AdIntegratorManager.rewardedVideoFailed(adNetworkId);
    }

    @Override
    public void onActivityCreated(Activity activity) {
    }

    @Override
    public void onActivityStarted(Activity activity) {
    }

    @Override
    public void onActivityResumed(Activity activity) {
        if (adMobBanner != null) {
            adMobBanner.resume();
        }
    }

    @Override
    public void onActivityPaused(Activity activity) {
        if (adMobBanner != null) {
            adMobBanner.pause();
        }
    }

    @Override
    public void onActivityStopped(Activity activity) {
    }

    @Override
    public void onActivityDestroyed(Activity activity) {
        if (adMobBanner != null) {
            adMobBanner.destroy();
        }
    }
}
