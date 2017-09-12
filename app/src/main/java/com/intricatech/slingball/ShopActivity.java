package com.intricatech.slingball;

import android.app.Activity;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.android.vending.billing.IInAppBillingService;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Created by Bolgbolg on 10/05/2017.
 */
public class ShopActivity extends Activity
        implements IabBroadcastReceiver.IabBroadcastListener{

    private final String TAG = getClass().getSimpleName();
    private static final boolean DEBUG = false;
    static final String AD_DATA = "AD_DATA";
    static final String PERMANENT_PLAYER_DATA = "PERMANENT_PLAYER_DATA";
    static final String SHOW_ADS = "SHOW_ADS";
    static final String AUDIO_PREFERENCES = "AUDIO";
    static final String LEVEL_DATA = "LEVEL_DATA";
    static String RESUME_ALLOWED_STRING;
    String HARD_UNLOCKED;
    private String LIVES_REMAINING;
    private String LIVES_REMAINING_REPORTER_START, LIVES_REMAINING_REPORTER_END,
            LIVES_REMAINING_REPORTER_SINGULAR, LIVES_REMAINING_REPORTER_PLURAL;
    private String SHOULD_RESUME_AFTER_GAMEOVER;
    private final static float ALPHA_AVAILABLE = 1.0f;
    private final static float ALPHA_UNAVAILABLE = 0.18f;
    private SharedPreferences adData;
    private SharedPreferences.Editor adDataEditor;
    private SharedPreferences permanentPlayerData;
    private SharedPreferences.Editor permanentPlayerDataEditor;
    private SharedPreferences audioPrefs;
    private SharedPreferences.Editor audioPrefsEditor;
    private SharedPreferences levelData;
    private SharedPreferences.Editor levelDataEditor;

    private int numberOfLives;
    private boolean showAds;
    private boolean hardUnlocked;
    private boolean resumeAfterGameOver;
    private boolean cameFromGameOver;

    private TableRow noAdsTablerow, buy5Tableow, buy10Tablerow, buy25Tablerow, unlockHardTableRow;
    private TextView cost5TextView, cost10TextView, cost25TextView, costRemoveAdsTextView, costUnlockHardTextView;
    private TextView livesRemainingReporterTextView;
    private LinearLayout activeScreen, waitScreen, devPanel;

    IInAppBillingService mService;
    IabHelper iabHelper;
    IabBroadcastReceiver iabBroadcastReceiver;
    static final String SKU_REMOVE_ADS = "remove_ads";

    static final String SKU_BUY_5_LIVES = "buy_5_lives";
    static final String SKU_BUY_10_LIVES = "buy_10_lives";
    static final String SKU_BUY_25_LIVES = "buy_25_lives";
    static final String SKU_UNLOCK_HARD = "unlock_hard_level";
    static final String TEST_SKU = "android.test.purchased";
    private String purchase_uuid;

    static final int RC_REQUEST = 10001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        LIVES_REMAINING = getResources().getString(R.string.lives_remaining_tag);
        LIVES_REMAINING_REPORTER_START = getResources().getString(R.string._lives_remaining_text_start);
        LIVES_REMAINING_REPORTER_END = getResources().getString(R.string._lives_remaining_text_end);
        LIVES_REMAINING_REPORTER_SINGULAR = getResources().getString(R.string._lives_remaining_singular);
        LIVES_REMAINING_REPORTER_PLURAL = getResources().getString(R.string._lives_remaining_plural);
        SHOULD_RESUME_AFTER_GAMEOVER = getResources().getString(R.string.should_restart_after_gameOver);
        HARD_UNLOCKED = getResources().getString(R.string._hard_level_available);
        RESUME_ALLOWED_STRING = getResources().getString(R.string.resume_allowed_string);

        setContentView(R.layout.shop_layout2);

        Intent intent = getIntent();
        resumeAfterGameOver = intent.getBooleanExtra(SHOULD_RESUME_AFTER_GAMEOVER, false);
        cameFromGameOver = intent.getBooleanExtra("CAME_FROM_GAMEOVER", false);

        adData = getSharedPreferences(AD_DATA, MODE_PRIVATE);
        adDataEditor = adData.edit();
        permanentPlayerData = getSharedPreferences(PERMANENT_PLAYER_DATA, MODE_PRIVATE);
        permanentPlayerDataEditor = permanentPlayerData.edit();
        audioPrefs = getSharedPreferences(AUDIO_PREFERENCES, MODE_PRIVATE);
        audioPrefsEditor = audioPrefs.edit();
        levelData = getSharedPreferences(LEVEL_DATA, MODE_PRIVATE);
        levelDataEditor = levelData.edit();


        showAds = adData.getBoolean(SHOW_ADS, true);
        Log.d(TAG, "onCreate() : showAds == " + String.valueOf(showAds));
        hardUnlocked = audioPrefs.getBoolean(HARD_UNLOCKED, false);
        numberOfLives = permanentPlayerData.getInt(LIVES_REMAINING, IntRepConsts.DEFAULT_NUMBER_OF_LIVES);

        cost5TextView = (TextView) findViewById(R.id._5_lives_cost_textview);
        cost10TextView = (TextView) findViewById(R.id._10_lives_cost_textview);
        cost25TextView = (TextView) findViewById(R.id._25_lives_cost_textview);
        costRemoveAdsTextView = (TextView) findViewById(R.id.no_ad_cost_textview);
        costUnlockHardTextView = (TextView) findViewById(R.id.unlock_hard_cost_textview);

        noAdsTablerow = (TableRow) findViewById(R.id.no_ads_tablerow);
        buy5Tableow = (TableRow) findViewById(R.id._5_lives_tablerow);
        buy10Tablerow = (TableRow) findViewById(R.id._10_lives_tablerow);
        buy25Tablerow = (TableRow) findViewById(R.id._25_lives_tablerow);
        unlockHardTableRow = (TableRow) findViewById(R.id.unlock_hard_tablerow);

        activeScreen = (LinearLayout) findViewById(R.id.active_shop_screen);
        waitScreen = (LinearLayout) findViewById(R.id.waiting_shop_screen);
        devPanel = (LinearLayout) findViewById(R.id.dev_panel);

        setRemoveAdsUIAvailable(showAds);
        setHardUnlockedUIAvailable(!hardUnlocked);

        livesRemainingReporterTextView = (TextView) findViewById(R.id.lives_remaining_reporter);
        setLivesReporterTextView(numberOfLives);

        String base64EncodedPublicKey = assemblePublicKey();
        iabHelper = new IabHelper(this, base64EncodedPublicKey);
        iabHelper.enableDebugLogging(true);
        iabHelper.startSetup(new IabHelper.OnIabSetupFinishedListener() {
            public void onIabSetupFinished(IabResult result) {
                if (DEBUG) {
                    Log.d(TAG, "Setup finished.");
                }

                if (!result.isSuccess()) {
                    // Oh noes, there was a problem.
                    if (DEBUG) {
                        Log.d(TAG, "Problem setting up in-app billing: " + result);
                    }
                    return;
                }

                // Have we been disposed of in the meantime? If so, quit.
                if (iabHelper == null) return;

                // Important: Dynamically register for broadcast messages about updated purchases.
                // We register the receiver here instead of as a <receiver> in the Manifest
                // because we always call getPurchases() at startup, so therefore we can ignore
                // any broadcasts sent while the app isn't running.
                // Note: registering this listener in an Activity is a bad idea, but is done here
                // because this is a SAMPLE. Regardless, the receiver must be registered after
                // IabHelper is setup, but before first call to getPurchases().
                iabBroadcastReceiver = new IabBroadcastReceiver(ShopActivity.this);
                IntentFilter broadcastFilter = new IntentFilter(IabBroadcastReceiver.ACTION);
                registerReceiver(iabBroadcastReceiver, broadcastFilter);

                // IAB is fully set up. Now, let's get an inventory of stuff we own.
                if (DEBUG) {
                    Log.d(TAG, "Setup successful. Querying inventory.");
                }
                List<String> skuList = new ArrayList<String>();
                skuList.add(SKU_REMOVE_ADS);
                skuList.add(SKU_BUY_5_LIVES);
                skuList.add(SKU_BUY_10_LIVES);
                skuList.add(SKU_BUY_25_LIVES);
                skuList.add(SKU_UNLOCK_HARD);

                try {
                    iabHelper.queryInventoryAsync(true, skuList, null, getAvailableProducts);
                } catch (IabHelper.IabAsyncInProgressException e) {
                    Log.d(TAG, "Error querying inventory. Another async operation in progress.");
                }
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // very important:
        if (iabBroadcastReceiver != null) {
            unregisterReceiver(iabBroadcastReceiver);
        }

        // very important:
        if (DEBUG) {
            Log.d(TAG, "Destroying helper.");
        }
        if (iabHelper != null) {
            iabHelper.disposeWhenFinished();
            iabHelper = null;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode,
                                    Intent data)
    {
        if (!iabHelper.handleActivityResult(requestCode,
                resultCode, data)) {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    public void onBuy5LivesButtonClick(View view) {
        setWaitScreen(true);
        Toast.makeText(this, "buy5LivesButton clicked", Toast.LENGTH_SHORT).show();
        purchase_uuid = UUID.randomUUID().toString();
        try {
            iabHelper.launchPurchaseFlow(
                    this,
                    SKU_BUY_5_LIVES,
                    RC_REQUEST,
                    purchaseFinishedListener,
                    purchase_uuid
            );
        } catch (IabHelper.IabAsyncInProgressException e) {
            e.printStackTrace();
            Log.d(TAG, "Error launching purchase flow. Another async operation in progress.");
        }
    }

    public void onBuy10LivesButtonClick(View view) {
        setWaitScreen(true);
        Toast.makeText(this, "buy10LivesButton clicked", Toast.LENGTH_SHORT).show();
        purchase_uuid = UUID.randomUUID().toString();
        try {
            iabHelper.launchPurchaseFlow(
                    this,
                    SKU_BUY_10_LIVES,
                    RC_REQUEST,
                    purchaseFinishedListener,
                    purchase_uuid
            );
        } catch (IabHelper.IabAsyncInProgressException e) {
            e.printStackTrace();
            Log.d(TAG, "Error launching purchase flow. Another async operation in progress.");
        }
    }

    public void onBuy25LivesButtonClick(View view) {
        setWaitScreen(true);
        Toast.makeText(this, "buy25LivesButton clicked", Toast.LENGTH_SHORT).show();
        purchase_uuid = UUID.randomUUID().toString();
        try {
            iabHelper.launchPurchaseFlow(
                    this,
                    SKU_BUY_25_LIVES,
                    RC_REQUEST,
                    purchaseFinishedListener,
                    purchase_uuid
            );
        } catch (IabHelper.IabAsyncInProgressException e) {
            e.printStackTrace();
            Log.d(TAG, "Error launching purchase flow. Another async operation in progress.");
        }
    }

    public void onUnlockHardButtonClick(View view) {
        setWaitScreen(true);
        Toast.makeText(this, "unlockHardButton clicked", Toast.LENGTH_SHORT).show();
        try {
            iabHelper.launchPurchaseFlow(
                    this,
                    SKU_UNLOCK_HARD,
                    RC_REQUEST,
                    purchaseFinishedListener,
                    purchase_uuid
            );
        } catch (IabHelper.IabAsyncInProgressException e) {
            e.printStackTrace();
            Log.d(TAG, "Error launching purchase flow. Another async operation in progress.");
        }
    }

    public void onRemoveAdsButtonClicked(View view) {
        Toast.makeText(this, "removeAdsButton clicked", Toast.LENGTH_SHORT).show();
        setWaitScreen(true);
        try {
            iabHelper.launchPurchaseFlow(
                    this,
                    SKU_REMOVE_ADS,
                    RC_REQUEST,
                    purchaseFinishedListener,
                    purchase_uuid
            );
        } catch (IabHelper.IabAsyncInProgressException e) {
            e.printStackTrace();
            Log.d(TAG, "Error launching purchase flow. Another async operation in progress.");
        }
    }

    public void onPurgeLivesButtonClicked(View view) {
        numberOfLives = 0;
        permanentPlayerDataEditor.putInt(LIVES_REMAINING, numberOfLives);
        permanentPlayerDataEditor.commit();
        setLivesReporterTextView(numberOfLives);
        Toast.makeText(this, "All lives purged", Toast.LENGTH_LONG).show();
    }

    public void onConsumeRemoveAds(View view) {
        List<String> skuList = new ArrayList<String>();
        skuList.add(SKU_REMOVE_ADS);
        try {
            iabHelper.queryInventoryAsync(true, skuList, null, consumeRemoveAdsListener);
        } catch (IabHelper.IabAsyncInProgressException e) {
            Log.d(TAG, "Error querying inventory. Another async operation in progress.");
        }
    }

    public void onConsumeHardUnlocked(View view) {
        List<String> skuList = new ArrayList<String>();
        skuList.add(SKU_UNLOCK_HARD);
        try {
            iabHelper.queryInventoryAsync(true, skuList, null, consumeHardUnlockedListener);
        } catch (IabHelper.IabAsyncInProgressException e) {
            Log.d(TAG, "Error querying inventory. Another async operation in progress.");
        }
    }

    public void onTitleClick(View view) {
        if (!IntRepConsts.IS_RELEASE_VERSION) {
            if (devPanel.getVisibility() == View.GONE) {
                devPanel.setVisibility(View.VISIBLE);
            } else if (devPanel.getVisibility() == View.VISIBLE) {
                devPanel.setVisibility(View.GONE);
            }
        }

    }

    @Override
    public void receivedBroadcast() {
        // Received a broadcast notification that the inventory of items has changed
        if (DEBUG) {
            Log.d(TAG, "Received broadcast notification. Querying inventory.");
        }
        try {
            iabHelper.queryInventoryAsync(getAvailableProducts);
        } catch (IabHelper.IabAsyncInProgressException e) {
            Log.d(TAG, "Error querying inventory. Another async operation in progress.");
        }
    }

    private String assemblePublicKey() {
        StringBuilder sb = new StringBuilder();
        Resources res = getResources();
        sb.append(res.getString(R.string.key_evie));
        sb.append(res.getString(R.string.key_josh));
        sb.append(res.getString(R.string.key_eddie));
        sb.append(res.getString(R.string.key_mathew));
        sb.append(IntRepConsts.RUBY);
        sb.append(IntRepConsts.LISA);
        sb.append(IntRepConsts.JESSE);

        return sb.toString();
    }

    // Temp -
    IabHelper.QueryInventoryFinishedListener consumeRemoveAdsListener = new IabHelper.QueryInventoryFinishedListener() {
        @Override
        public void onQueryInventoryFinished(IabResult result, Inventory inv) {
            if (iabHelper == null) {
                return;
            }
            if (result.isFailure()) {
                if (DEBUG) {
                    Log.d(TAG, "Failed to query inventory (to consume non-consumeables");
                }
            } else {
                Purchase removeAdsPurch = inv.getPurchase(SKU_REMOVE_ADS);
                if (removeAdsPurch != null) {
                    Log.d(TAG, "consuming removeAds Purchase");
                    consumeItem(removeAdsPurch);
                } else {
                    Log.d(TAG, "removeAds Purchase is null");
                }
            }
        }
    };

    IabHelper.QueryInventoryFinishedListener consumeHardUnlockedListener = new IabHelper.QueryInventoryFinishedListener() {
        @Override
        public void onQueryInventoryFinished(IabResult result, Inventory inv) {
            if (iabHelper == null) {
                return;
            }
            if (result.isFailure()) {
                if (DEBUG) {
                    Log.d(TAG, "Failed to query inventory (to consume non-consumeables");
                }
            } else {
                Purchase unlockHard = inv.getPurchase(SKU_UNLOCK_HARD);
                if (unlockHard != null) {
                    Log.d(TAG, "consuming unlockHard Purchase");
                    consumeItem(unlockHard);

                } else {
                    Log.d(TAG, "unlockHard Purchase is null");
                }
            }
        }
    };

    IabHelper.QueryInventoryFinishedListener getAvailableProducts = new IabHelper.QueryInventoryFinishedListener() {
        @Override
        public void onQueryInventoryFinished(IabResult result, Inventory inv) {
            if (DEBUG) {
                Log.d(TAG, "Query Inventory Finished");
            }

            if (iabHelper == null) {
                return;
            }
            if (result.isFailure()) {
                if (DEBUG) {
                    Log.d(TAG, "Failed to query inventory : " + result);
                }
            } else {

                Purchase removeAdsPurchase = inv.getPurchase(SKU_REMOVE_ADS);
                if (removeAdsPurchase != null) {
                    if (showAds) {
                        setShowAdsInApp(false);
                        setRemoveAdsUIAvailable(false);
                    }
                }

                Purchase unlockHardPurchase = inv.getPurchase(SKU_UNLOCK_HARD);
                if (unlockHardPurchase != null) {
                    setHardUnlockedInApp(true);
                    setHardUnlockedUIAvailable(false);
                }

                Purchase buy5LivesPurchasePending = inv.getPurchase(SKU_BUY_5_LIVES);
                if (buy5LivesPurchasePending != null) {
                    consumeItem(buy5LivesPurchasePending);
                }
                Purchase buy10LivesPurchasePending = inv.getPurchase(SKU_BUY_10_LIVES);
                if (buy10LivesPurchasePending != null) {
                    consumeItem(buy10LivesPurchasePending);
                }
                Purchase buy25LivesPurchasePending = inv.getPurchase(SKU_BUY_25_LIVES);
                if (buy25LivesPurchasePending != null) {
                    consumeItem(buy25LivesPurchasePending);
                }

                String price = inv.getSkuDetails(SKU_BUY_5_LIVES).getPrice();
                cost5TextView.setText(price);
                price = inv.getSkuDetails(SKU_BUY_10_LIVES).getPrice();
                cost10TextView.setText(price);
                price = inv.getSkuDetails(SKU_BUY_25_LIVES).getPrice();
                cost25TextView.setText(price);
                price = inv.getSkuDetails(SKU_REMOVE_ADS).getPrice();
                costRemoveAdsTextView.setText(price);
                price = inv.getSkuDetails(SKU_UNLOCK_HARD).getPrice();
                costUnlockHardTextView.setText(price);
            }


        }
    };

    /** Verifies the developer payload of a purchase. */
    boolean verifyDeveloperPayload(Purchase p) {
        String payload = p.getDeveloperPayload();

        if (payload == purchase_uuid) {
            return true;
        } else {
            if (DEBUG) {
                Log.d(TAG, "developer payload does not match randomly generated key");
            }
            return false;
        }

        /*
         * TODO: verify that the developer payload of the purchase is correct. It will be
         * the same one that you sent when initiating the purchase.
         *
         * WARNING: Locally generating a random string when starting a purchase and
         * verifying it here might seem like a good approach, but this will fail in the
         * case where the user purchases an item on one device and then uses your app on
         * a different device, because on the other device you will not have access to the
         * random string you originally generated.
         *
         * So a good developer payload has these characteristics:
         *
         * 1. If two different users purchase an item, the payload is different between them,
         *    so that one user's purchase can't be replayed to another user.
         *
         * 2. The payload must be such that you can verify it even when the app wasn't the
         *    one who initiated the purchase flow (so that items purchased by the user on
         *    one device work on other devices owned by the user).
         *
         * Using your own server to store and verify developer payloads across app
         * installations is recommended.
         */
    }


    // Callback for when a purchase is finished
    IabHelper.OnIabPurchaseFinishedListener purchaseFinishedListener = new IabHelper.OnIabPurchaseFinishedListener() {
        public void onIabPurchaseFinished(IabResult result, Purchase purchase) {
            if (DEBUG) {
                Log.d(TAG, "Purchase finished: " + result + ", purchase: " + purchase);
            }

            // if we were disposed of in the meantime, quit.
            if (iabHelper == null) {
                setWaitScreen(false);
                return;
            }

            if (result.isFailure()) {
                if (DEBUG) {
                    Log.d(TAG, "Error purchasing: " + result);
                }
                setWaitScreen(false);
                return;
            }
            /*if (!verifyDeveloperPayload(purchase)) {
                Toast.makeText(ShopActivity.this, "Error purchasing. Authenticity verification failed.", Toast.LENGTH_SHORT).show();
                setWaitScreen(false);
                return;
            }*/
            if (purchase.getSku().equals(SKU_REMOVE_ADS)) {
                setShowAdsInApp(false);
                setRemoveAdsUIAvailable(false);
                setWaitScreen(false);
            } else if (purchase.getSku().equals(SKU_UNLOCK_HARD)) {
                setHardUnlockedInApp(true);
                setHardUnlockedUIAvailable(false);
                setWaitScreen(false);
            }
            if (purchase.getSku().equals(SKU_BUY_5_LIVES)){
                consumeItem(purchase);
            }
            if (purchase.getSku().equals(SKU_BUY_10_LIVES)) {
                consumeItem(purchase);
            }
            if (purchase.getSku().equals(SKU_BUY_25_LIVES)) {
                consumeItem(purchase);
            }
        }
    };

    private void consumeItem(Purchase purchase) {
        try {
            iabHelper.consumeAsync(purchase, consumeFinishedListener);
        } catch (IabHelper.IabAsyncInProgressException e) {
            e.printStackTrace();
        }
    }

    IabHelper.OnConsumeFinishedListener consumeFinishedListener = new IabHelper.OnConsumeFinishedListener() {
        @Override
        public void onConsumeFinished(Purchase purchase, IabResult result) {
            if (result.isSuccess()) {
                if (DEBUG) {
                    Log.d(TAG, "Success!");
                }
                if (purchase.getSku().equals(SKU_BUY_5_LIVES)) {
                    addLivesToTotal(5);
                } else if (purchase.getSku().equals(SKU_BUY_10_LIVES)) {
                    addLivesToTotal(10);

                } else if (purchase.getSku().equals(SKU_BUY_25_LIVES)) {
                    addLivesToTotal(25);
                }
                if (purchase.getSku().equals(SKU_REMOVE_ADS)) {
                    setShowAdsInApp(true);
                    setRemoveAdsUIAvailable(true);
                }
                if (purchase.getSku().equals(SKU_UNLOCK_HARD)) {
                    setHardUnlockedInApp(false);
                    setHardUnlockedUIAvailable(true);
                }
            } else {
                Toast.makeText(
                        ShopActivity.this,
                        "There was a problem ...... purchase not consumed",
                        Toast.LENGTH_LONG).show();
            }
            setWaitScreen(false);
        }
    };

    private void setRemoveAdsUIAvailable(boolean available) {
        if (available) {
            noAdsTablerow.setAlpha(ALPHA_AVAILABLE);
            setFamilyClickable(noAdsTablerow);
        } else {
            noAdsTablerow.setAlpha(ALPHA_UNAVAILABLE);
            setFamilyNotClickable(noAdsTablerow);
        }
    }

    private void setHardUnlockedUIAvailable(boolean available) {
        if (!available) {
            unlockHardTableRow.setAlpha(ALPHA_UNAVAILABLE);
            setFamilyNotClickable(unlockHardTableRow);
        } else {
            unlockHardTableRow.setAlpha(ALPHA_AVAILABLE);
            setFamilyClickable(unlockHardTableRow);
        }
    }

    private void setShowAdsInApp(boolean showAdsInApp) {
        showAds = showAdsInApp;
        adDataEditor.putBoolean(SHOW_ADS, showAdsInApp);
        adDataEditor.commit();
    }

    private void setHardUnlockedInApp(boolean hardUnlockedInApp) {
        hardUnlocked = hardUnlockedInApp;
        audioPrefsEditor.putBoolean(HARD_UNLOCKED, hardUnlocked);
        audioPrefsEditor.commit();
        setHardUnlockedUIAvailable(false);
    }

    private void addLivesToTotal(int numberOfLivesToAdd) {
        numberOfLives += numberOfLivesToAdd;
        permanentPlayerDataEditor.putInt(LIVES_REMAINING, numberOfLives);
        permanentPlayerDataEditor.commit();
        setLivesReporterTextView(numberOfLives);
        if (cameFromGameOver) {
            resumeAfterGameOver = true;
        }
    }

    private void setLivesReporterTextView(int numberOfLives) {
        String text =
                LIVES_REMAINING_REPORTER_START + " "
                        + (numberOfLives == 0 ? "no" : String.valueOf(numberOfLives)) + " "
                        + (numberOfLives == 1 ? LIVES_REMAINING_REPORTER_SINGULAR : LIVES_REMAINING_REPORTER_PLURAL) + " "
                        + LIVES_REMAINING_REPORTER_END;
        livesRemainingReporterTextView.setText(text);
    }

    public void setFamilyClickable(View view) {
        if (view != null) {
            view.setClickable(true);
            if (view instanceof ViewGroup) {
                ViewGroup vg = ((ViewGroup) view);
                for (int i = 0; i < vg.getChildCount(); i++) {
                    setFamilyClickable(vg.getChildAt(i));
                }
            }
        }
    }
    public void setFamilyNotClickable(View view) {
        if (view != null) {
            view.setClickable(false);
            if (view instanceof ViewGroup) {
                ViewGroup vg = ((ViewGroup) view);
                for (int i = 0; i < vg.getChildCount(); i++) {
                    setFamilyNotClickable(vg.getChildAt(i));
                }
            }
        }
    }

    @Override
    public void onBackPressed() {
        if (resumeAfterGameOver && numberOfLives > 0) {
            numberOfLives--;
            permanentPlayerDataEditor.putInt(LIVES_REMAINING, numberOfLives);
            permanentPlayerDataEditor.commit();
            setLivesReporterTextView(numberOfLives);
            Intent intent = new Intent(this, GameActivity.class);
            intent.putExtra("RESUME_LAST_GAME", true);
            startActivity(intent);
            finish();
        } else {
            levelDataEditor.putBoolean(RESUME_ALLOWED_STRING, false);
            levelDataEditor.commit();
            super.onBackPressed();
        }

    }

    private void setWaitScreen(boolean set) {
        waitScreen.setVisibility(set ? View.VISIBLE : View.GONE);
        activeScreen.setVisibility(set ? View.GONE : View.VISIBLE);
    }
}
