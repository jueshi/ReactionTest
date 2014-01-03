package org.fanchuan.coursera.reactiontest;

import java.util.Random;
import android.app.Activity;
import android.os.Handler;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.View;
import android.widget.TextView;

public class MainActivity extends Activity {

    //The activity can be in one of three states
    final short STATE_IDLE = 0; // Idle, waiting for user to press button
    final short STATE_DELAY = 1; // User pressed begin test button, random delay
    final short STATE_TESTING = 2; // Reaction timer is running, waiting for button press
    final short STATE_FINISHED = 3; // Congratulate user
    long timeTestStart = 0;
    final Runnable UPDATE_UI_STATUS = new Runnable() {
        public void run() {
            final TextView VW_STATUS = (TextView) findViewById(R.id.status);
            VW_STATUS.setText(stateDescriptions[activityState]);
        }
    };
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

    /*
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    */

    /*
    private void startTest() {
        Long currentTime = SystemClock.elapsedRealtime();
        //in progress
    }
    */

    public void clickGoButton(View vw) {

        final Runnable BEGIN_TEST = new Runnable() {
            public void run() {
            activityState = STATE_TESTING;
            timeTestStart = SystemClock.elapsedRealtime();
            handlerTest.post(UPDATE_UI_STATUS);
            }
        };
        final Runnable END_TEST = new Runnable() {
            public void run() {
                activityState = STATE_FINISHED;
                long timeElapsed = SystemClock.elapsedRealtime() - timeTestStart;
                final TextView VW_STATUS = (TextView) findViewById(R.id.status);
                String reactionTimeText = String.format(stateDescriptions[STATE_FINISHED], timeElapsed);
                    VW_STATUS.setText(reactionTimeText);
            }
        };

        if (activityState == STATE_IDLE) {
            activityState = STATE_DELAY;
            handlerTest.post(UPDATE_UI_STATUS);
            int flagDelay_ms = Math.round(1500 * randTimer.nextFloat() + 1000);
            handlerTest.postDelayed(BEGIN_TEST, flagDelay_ms);
        }
        else if(activityState == STATE_DELAY) {
            //If user clicks during the delay period, that resets the test.
            handlerTest.removeCallbacksAndMessages(null);
            activityState = STATE_IDLE;
            handlerTest.post(UPDATE_UI_STATUS);
        }
        else if(activityState == STATE_TESTING) {
            //Reaction testing in progress
            handlerTest.post(END_TEST);
        }
        else {
            activityState = STATE_IDLE;
            handlerTest.post(UPDATE_UI_STATUS);
        }
    };
}