package com.example.seanc.assignment4;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.MenuItem;
import android.support.v4.app.NavUtils;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.example.permanentlocationlistener.permanentLocationListener;
import com.seanc.fullJSONRequest.fullJSONRequest;
import com.seanc.permanentrequestqueue.permanentRequestQueue;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.InvalidObjectException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class userScreen extends AppCompatActivity {

    /**
     * Whether or not the system UI should be auto-hidden after
     * {@link #AUTO_HIDE_DELAY_MILLIS} milliseconds.
     */
    private static final boolean AUTO_HIDE = true;

    /**
     * If {@link #AUTO_HIDE} is set, the number of milliseconds to wait after
     * user interaction before hiding the system UI.
     */
    private static final int AUTO_HIDE_DELAY_MILLIS = 3000;

    /**
     * Some older devices needs a small delay between UI widget updates
     * and a change of the status and navigation bar.
     */
    private static final int UI_ANIMATION_DELAY = 300;
    private final Handler mHideHandler = new Handler();
    private View mContentView;

    private final Runnable mHidePart2Runnable = new Runnable() {
        @SuppressLint("InlinedApi")
        @Override
        public void run() {
            // Delayed removal of status and navigation bar

            // Note that some of these constants are new as of API 16 (Jelly Bean)
            // and API 19 (KitKat). It is safe to use them, as they are inlined
            // at compile-time and do nothing on earlier devices.
            mContentView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
        }
    };
    private View mControlsView;
    private final Runnable mShowPart2Runnable = new Runnable() {
        @Override
        public void run() {
            // Delayed display of UI elements
            ActionBar actionBar = getSupportActionBar();
            if (actionBar != null) {
                actionBar.show();
            }
            mControlsView.setVisibility(View.VISIBLE);
        }
    };
    private boolean mVisible;
    private final Runnable mHideRunnable = new Runnable() {
        @Override
        public void run() {
            hide();
        }
    };
    /**
     * Touch listener to use for in-layout UI controls to delay hiding the
     * system UI. This is to prevent the jarring behavior of controls going away
     * while interacting with activity UI.
     */
    private final View.OnTouchListener mDelayHideTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            if (AUTO_HIDE) {
                delayedHide(AUTO_HIDE_DELAY_MILLIS);
            }
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_user_screen);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        if(!permanentLocationListener.exists()){
            permanentLocationListener.newPermLocationListern(getApplicationContext());
        }
        if(!permanentRequestQueue.exists()){
            permanentRequestQueue.newPermRequestQueue(getApplicationContext());
        }
        mVisible = true;
        mControlsView = findViewById(R.id.fullscreen_content_controls);
        mContentView = findViewById(R.id.fullscreen_content);

        TextView mWelcomeBannerView = (TextView) findViewById(R.id.welcome_banner);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        final String sessID = prefs.getString("sessionID", null);
        final String userName = prefs.getString("username", null);

        mWelcomeBannerView.setText("Welcome, " + userName + "!");

        final TextView mCurLat = (TextView) findViewById(R.id.curLat);
        final TextView mCurLon = (TextView) findViewById(R.id.curLon);

        mCurLat.setText("CurLat = " + permanentLocationListener.getLat());
        mCurLon.setText("CurLon = " + permanentLocationListener.getLon());

        // Set up the user interaction to manually show or hide the system UI.
        mContentView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toggle();
            }
        });

        final ListView mListView = (ListView) findViewById(R.id.groups);

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent viewGroup = new Intent(getApplicationContext(), viewGroup.class);
                viewGroup.putExtra("group_name", view.toString());
                startActivity(viewGroup);
            }
        });

        String url = "http://www.sean-mulholland.com:3003/users/" + userName;
        JSONObject body = null;
        Map<String, String> headers = new HashMap<String, String>();
        headers.put("content-type", "application/json");
        headers.put("cookie", sessID);

        final ArrayList<String> groups_list = new ArrayList<String>();

        fullJSONRequest userRequest = new fullJSONRequest(Request.Method.GET, url, body, headers,

                new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            response = response.getJSONObject("response");
                            if(response.getBoolean("status")) {
                                final JSONArray data = response.getJSONArray("data");
                                final JSONObject user = data.getJSONObject(0);
                                final JSONArray userGroups = user.getJSONArray("groups");

                                for(int i = 0; i < userGroups.length(); i++) {
                                    int iPlusOne = i + 1;
                                    groups_list.add(userGroups.getJSONObject(i).getString("group"));
                                }


                                final ArrayAdapter<String> adapt = new ArrayAdapter<String>(getApplicationContext(), R.layout.mylistview, R.id.group_name, groups_list){
                                    @Override
                                    public View getView (final int position, View convertView, ViewGroup parent){
                                        LayoutInflater inflater = (LayoutInflater)getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                                        if( convertView == null ){
                                            convertView = inflater.inflate(R.layout.mylistview, parent, false);
                                        }
                                        TextView index = (TextView)convertView.findViewById(R.id.group_index);
                                        index.setText("Group " + String.valueOf(position+1) + ": ");
                                        TextView name = (TextView)convertView.findViewById(R.id.group_name);
                                        name.setText(groups_list.get(position));
                                        name.setOnClickListener(new View.OnClickListener(){
                                            @Override
                                            public void onClick(View view) {
                                                Intent viewGroup = new Intent(getApplicationContext(), viewGroup.class);
                                                viewGroup.putExtra("group_name",groups_list.get(position));
                                                startActivity(viewGroup);
                                            }
                                        });
                                        return convertView;
                                    }
                                };
                                mListView.setAdapter(adapt);
                            } else {

                                Toast toast=Toast.makeText(getApplicationContext(),"Must log-in again!",Toast.LENGTH_SHORT);
                                toast.show();
                                Intent returnLogin = new Intent(getBaseContext(), loginActivity.class);
                                startActivity(returnLogin);
                            }

                        } catch (JSONException e){
                            e.printStackTrace();
                        }

                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        System.err.println("ERROR!");
                        error.printStackTrace();
                    }
                }){
                    @Override
                    public Map<String, String> getHeaders() throws AuthFailureError {
                        Map<String, String>  params = new HashMap<String, String>();
                        params.put("Content-Type", "application/json");
                        params.put("cookie", sessID);

                        return params;
                    }
                };

        try {
            permanentRequestQueue.getPermRequestQueue().addToMyRequestQueue(userRequest);
        } catch (InvalidObjectException e){
            System.err.println(e);
            e.printStackTrace();
        }

        JSONObject location = new JSONObject();
        try {
            location.put("user", userName);
            location.put("curLat", String.valueOf(permanentLocationListener.getLat()));
            location.put("curLon", String.valueOf(permanentLocationListener.getLon()));
        } catch (JSONException e){
            System.err.print(e);
            e.printStackTrace();
        }
        fullJSONRequest locationRequest = new fullJSONRequest(Request.Method.PUT, url, location, headers,

                new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            response = response.getJSONObject("response");
                            System.out.println(response.toString());
                            if(response.getBoolean("status")) {
                                mCurLat.setText(response.getJSONObject("entity").getString("curLat"));
                                mCurLon.setText(response.getJSONObject("entity").getString("curLon"));
                            } else {
                                System.out.print(response.toString());
                            }
                        } catch (JSONException e){
                            System.err.print(e);
                            e.printStackTrace();
                        }

                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        System.err.println(error);
                        error.printStackTrace();
                    }
                });
        try {
            permanentRequestQueue.getPermRequestQueue().addToMyRequestQueue(locationRequest);
        } catch (InvalidObjectException e) {
            System.err.print(e);
            e.printStackTrace();
        }
    }
    //The following were auto created by Android Studio
    //NOT WRITTEN BY SEAN MULHOLLAND
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        // Trigger the initial hide() shortly after the activity has been
        // created, to briefly hint to the user that UI controls
        // are available.
        delayedHide(100);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            // This ID represents the Home or Up button.
            NavUtils.navigateUpFromSameTask(this);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void toggle() {
        if (mVisible) {
            hide();
        } else {
            show();
        }
    }

    private void hide() {
        // Hide UI first
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }
        mControlsView.setVisibility(View.GONE);
        mVisible = false;

        // Schedule a runnable to remove the status and navigation bar after a delay
        mHideHandler.removeCallbacks(mShowPart2Runnable);
        mHideHandler.postDelayed(mHidePart2Runnable, UI_ANIMATION_DELAY);
    }

    @SuppressLint("InlinedApi")
    private void show() {
        // Show the system bar
        mContentView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
        mVisible = true;

        // Schedule a runnable to display UI elements after a delay
        mHideHandler.removeCallbacks(mHidePart2Runnable);
        mHideHandler.postDelayed(mShowPart2Runnable, UI_ANIMATION_DELAY);
    }

    /**
     * Schedules a call to hide() in [delay] milliseconds, canceling any
     * previously scheduled calls.
     */
    private void delayedHide(int delayMillis) {
        mHideHandler.removeCallbacks(mHideRunnable);
        mHideHandler.postDelayed(mHideRunnable, delayMillis);
    }

    /**
     * Event action for add group button
     * Starts add group activity
     * Sean Mulholland wrote this method
     */
    public void addGroup(View v){
        Intent addGroup = new Intent(getBaseContext(), addGroups.class);
        startActivity(addGroup);
    }

    /**
     * Event action for the logout button
     * Erases SharedPreferences, and returns to login screen
     * Sean Mulholland Wrote this method
     */
    public void logout(View v){

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        final String sessID = prefs.getString("sessionID", null);

        final SharedPreferences.Editor edit = prefs.edit();

        if(!permanentLocationListener.exists()){
            permanentLocationListener.newPermLocationListern(getApplicationContext());
        }
        if(!permanentRequestQueue.exists()){
            permanentRequestQueue.newPermRequestQueue(getApplicationContext());
        }
        String url = "http://www.sean-mulholland.com:3003/logout";
        JSONObject body = null;
        Map<String, String> headers = new HashMap<String, String>();

        // Request a JSON response from the provided URL.
        fullJSONRequest logoutRequest = new fullJSONRequest (Request.Method.POST, url, body, headers,

                new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            response = response.getJSONObject("response");
                            Toast toast;
                            if(response.getBoolean("status")){
                                toast = Toast.makeText(getApplicationContext(), "Logged out successfully", Toast.LENGTH_SHORT);
                                edit.clear();
                                edit.commit();
                            } else {
                                toast = Toast.makeText(getApplicationContext(),"Error logging out/already logged out",Toast.LENGTH_SHORT);
                            }
                            toast.show();
                            Intent returnLogin = new Intent(getBaseContext(), loginActivity.class);
                            startActivity(returnLogin);

                        } catch (JSONException e){
                            System.err.println(e);
                            e.printStackTrace();
                        }

                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        System.err.println(error);
                        error.printStackTrace();
                    }
                });
        // Add the request to the RequestQueue.
    try{
        permanentRequestQueue.getPermRequestQueue().addToMyRequestQueue(logoutRequest);
    } catch (InvalidObjectException e){
        System.err.println(e);
        e.printStackTrace();
    }

}

}
