package org.fanchuan.coursera.reactiontest;

import android.app.Dialog;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import java.util.Random;

public class MainActivity extends ActionBarActivity {

    final String TAG = MainActivity.class.getSimpleName();
    //The activity can be in one of three states
    final short STATE_IDLE = 0; // Idle, waiting for user to press button
    private short activityState = STATE_IDLE;
    final short STATE_DELAY = 1; // User pressed begin test button, random delay
    final short STATE_TESTING = 2; // Reaction timer is running, waiting for button press
    final short STATE_FINISHED = 3; // Congratulate user
    String[] stateDescriptions;
    final Runnable UPDATE_UI_STATUS = new Runnable() {
        public void run() {
            final TextView VW_STATUS = (TextView) findViewById(R.id.status);
            VW_STATUS.setText(stateDescriptions[activityState]);
        }
    };
    //Persistent items saved to preferences; currently best reaction time only
    private SharedPreferences prefs;
    private long bestTime;
    private long timeTestStart = 0;
    private Handler handlerTest = new Handler(); //must maintain handler state, used for reaction timing
    private Random randTimer = new Random();

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
        stateDescriptions = getResources().getStringArray(R.array.state_descriptions);
        handlerTest.post(UPDATE_UI_STATUS);

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
            Log.e(TAG, "Unable to display score notification", e);
        }
    }

    private void showBestTime() {
        // If test complete, run submitLatestTime() first; Assumes bestTime is already populated
        TextView vw = (TextView) findViewById(R.id.bestTime);
        String bestTimeText = String.format(getResources().getString(R.string.bestTime), bestTime);
        vw.setText(bestTimeText);
    }

    private void submitLatestTime(long latestTime) {
        //Check latestTime versus bestTime, if so, update bestTime and persist in preferences
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
        }
    }

    public void clickGoButton(View vw) {

        final Runnable BEGIN_TEST = new Runnable() {
            public void run() {
                activityState = STATE_TESTING;
                timeTestStart = SystemClock.elapsedRealtime();
                handlerTest.post(UPDATE_UI_STATUS);
            }
        };
        final Runnable END_TEST = new

                Runnable() {
                    public void run() {
                        activityState = STATE_FINISHED;
                        long timeElapsed = SystemClock.elapsedRealtime() - timeTestStart;
                        final TextView VW_STATUS = (TextView) findViewById(R.id.status);
                        String reactionTimeText = String.format(stateDescriptions[STATE_FINISHED], timeElapsed);
                        submitLatestTime(timeElapsed);
                        showBestTime();
                        VW_STATUS.setText(reactionTimeText);
                        String keyNotification = getResources().getString(R.string.keyNotification);
                        boolean settingNotification = prefs.getBoolean(keyNotification, false);
                        Log.d(TAG, keyNotification + ": " + String.valueOf(settingNotification));
                        if (settingNotification)
                            showEndTestNotification(reactionTimeText);
                    }
                };

        switch (activityState) {
            case STATE_IDLE:
                activityState = STATE_DELAY;
                handlerTest.post(UPDATE_UI_STATUS);
                int flagDelay_ms = Math.round(1500 * randTimer.nextFloat() + 1000);
                handlerTest.postDelayed(BEGIN_TEST, flagDelay_ms);
                break;
            case STATE_DELAY:
                //If user clicks during the delay period, that resets the test.
                handlerTest.removeCallbacksAndMessages(null);
                activityState = STATE_IDLE;
                handlerTest.post(UPDATE_UI_STATUS);
                break;
            case STATE_TESTING:
                //Reaction testing in progress
                handlerTest.post(END_TEST);
                break;
            case STATE_FINISHED:
                activityState = STATE_IDLE;
                handlerTest.post(UPDATE_UI_STATUS);
                break;
        }
    }
}