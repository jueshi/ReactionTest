package org.fanchuan.coursera.reactiontest;

import android.app.Dialog;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

public class MainActivity extends ActionBarActivity implements ReactionTimerObserver {

    final String TAG = MainActivity.class.getSimpleName();
    //Persistent items saved to preferences; currently best reaction time only
    private ReactionTimer reactionTimer;
    private SharedPreferences prefs;
    private long bestTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Retrieve preferences (saved best reaction time)
        try {
            prefs = PreferenceManager.getDefaultSharedPreferences(this);
            bestTime = prefs.getLong(getResources().getString(R.string.keyBestTime), 10000); //default reaction time if not saved
            Log.d(TAG, "bestTime: " + bestTime);
            showBestTime();
        } catch (Exception e) {
            Log.e(TAG, "Unable to get best time", e);
        }

        //Each state has its own unique text description
        this.reactionTimer = new ReactionTimer(this, getResources().getStringArray(R.array.state_descriptions));

        /*
        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mSectionsPagerAdapter);
        */
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        // Inflate the menu; this adds items to the action bar if it is present.
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        //Check preferences to see if user wants to remove best time
        if (prefs.getBoolean(getResources().getString(R.string.keyClearBest), false)) {
            SharedPreferences.Editor editor = prefs.edit();
            editor.remove(getResources().getString(R.string.keyBestTime));
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD)
                editor.apply();
            else
                editor.commit();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        switch (item.getItemId()) {
            case R.id.action_help:
                Dialog help = new Dialog(this);
                help.setContentView(R.layout.dialog_help);
                help.show();
                break;
            case R.id.action_settings:
                Intent i = new Intent(this, SettingsActivity.class);
                startActivity(i);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    public void showEndTestNotification(String reactionTimeText) {
        /* Generates a sample notification, notifying of the reaction time.
        Worked in several Genymotion API18 emulators and Moto Xoom tablet but threw Exception on LG API10 phone (??) */
        try {
            NotificationCompat.Builder notifyReactionTimeBuilder = new NotificationCompat.Builder(this)
                    .setSmallIcon(R.drawable.ic_launcher)
                    .setContentText(reactionTimeText)
                    .setContentTitle(this.getString(R.string.app_name));
            NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            mNotificationManager.notify(0, notifyReactionTimeBuilder.build());
        } catch (Exception e) {
            Log.w(TAG, "Unable to display score notification", e);
        }
    }

    private void showBestTime() {
        // If test complete, run submitLatestTime() first; Assumes bestTime is already populated
        TextView vw = (TextView) findViewById(R.id.bestTime);
        String bestTimeText = String.format(getResources().getString(R.string.bestTime), bestTime);
        vw.setText(bestTimeText);
    }

    public void submitLatestTime(long latestTime, String reactionTimeText) {
        /* Check latestTime versus bestTime, if so, update bestTime and persist in preferences
            Check Shared Preferences: does user wants a notification displayed?
         */
        if (latestTime < bestTime) {
            bestTime = latestTime;
            Log.d(TAG, "submitLatestTime: " + latestTime);
            try {
                SharedPreferences.Editor editor = prefs.edit();
                editor.putLong(getResources().getString(R.string.keyBestTime), latestTime);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD)
                    editor.apply();
                else
                    editor.commit();
            } catch (Exception e) {
                Log.e(TAG, "Unable to save best time", e);
            }
            Log.i(TAG, "Committed to preferences latestTime: " + latestTime);
            showBestTime();
        }
        //If notification setting is on, displays whether latestTime is bestTime or not
        String keyNotification = getResources().getString(R.string.keyNotification);
        boolean settingNotification = prefs.getBoolean(keyNotification, false);
        Log.d(TAG, keyNotification + ": " + String.valueOf(settingNotification));
        if (settingNotification)
            showEndTestNotification(reactionTimeText);
    }

    public synchronized void updateUiStatus(String statusText) {
        final TextView VW_STATUS = (TextView) findViewById(R.id.status);
        VW_STATUS.setText(statusText);
    }

    @SuppressWarnings("unused")
    public void clickGoButton(View vw) {
        reactionTimer.click();
    }

}