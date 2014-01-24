package org.fanchuan.coursera.reactiontest;

import android.app.Dialog;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import java.util.Random;

public class MainActivity extends ActionBarActivity {

    //The activity can be in one of three states
    final short STATE_IDLE = 0; // Idle, waiting for user to press button
    final short STATE_DELAY = 1; // User pressed begin test button, random delay
    final short STATE_TESTING = 2; // Reaction timer is running, waiting for button press
    final short STATE_FINISHED = 3; // Congratulate user
    final Runnable UPDATE_UI_STATUS = new Runnable() {
        public void run() {
            final TextView VW_STATUS = (TextView) findViewById(R.id.status);
            VW_STATUS.setText(stateDescriptions[activityState]);
        }
    };
    long timeTestStart = 0;
    short activityState = STATE_IDLE;
    String[] stateDescriptions;
    private Handler handlerTest = new Handler(); //must maintain handler state, used for reaction timing
    private Random randTimer = new Random();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
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
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.help_action) {
            showHelp();
            return true;
        } else {
            showSettings();
            return true;
        }
    }

    public void showEndTestNotification(String reactionTimeText) {
        /* Generates a sample notification, notifying of the reaction time.
        If there is a problem with notifications we will skip it */
        try {
            NotificationCompat.Builder notifyReactionTimeBuilder = new NotificationCompat.Builder(this)
                    .setSmallIcon(R.drawable.ic_launcher)
                    .setContentText(reactionTimeText)
                    .setContentTitle(this.getString(R.string.app_name));
            NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            mNotificationManager.notify(0, notifyReactionTimeBuilder.build());
        } catch (Exception e) {
            Log.e(e.getMessage(), e.toString());
        }
    }


    public void showHelp() {
        Dialog help = new Dialog(this);
        help.setContentView(R.layout.dialog_help);
        help.show();
    }

    public void showSettings() {
        Dialog help = new Dialog(this);
        help.setContentView(R.layout.dialog_settings);
        help.show();
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
                        VW_STATUS.setText(reactionTimeText);
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