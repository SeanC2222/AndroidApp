package com.example.seanc.assignment4;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.Notification;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.app.LoaderManager.LoaderCallbacks;

import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;

import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.TextUtils;
import android.util.AndroidException;
import android.util.JsonReader;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.JsonRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.permanentlocationlistener.permanentLocationListener;
import com.google.gson.Gson;
import com.google.gson.JsonArray;

import org.json.*;

import java.io.InvalidObjectException;
import java.io.UnsupportedEncodingException;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.transform.ErrorListener;

import com.seanc.permanentrequestqueue.permanentRequestQueue;
import com.seanc.fullJSONRequest.fullJSONRequest;

import static android.Manifest.permission.ACCESS_COARSE_LOCATION;
import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static android.Manifest.permission.INTERNET;

/**
 * A login screen that offers login via user/password.
 */
public class loginActivity extends AppCompatActivity {

    /**
     * Current Lat and Long
     */
    public static double D_CUR_LAT = 0.0;
    public static double D_CUR_LON = 0.0;

    /**
     * Id to identity Internet permission request.
     */
    private static final int REQUEST_INTERNET = 1;
    private static final int REQUEST_FINE_LOCATION = 2;
    private static final int REQUEST_COARSE_LOCATION = 3;

    private AutoCompleteTextView mUserView;
    private EditText mPasswordView;
    private View mProgressView;
    private View mLoginFormView;
    private TextView mTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if(!permanentLocationListener.exists()){
            permanentLocationListener.newPermLocationListern(getApplicationContext());
        }
        if(!permanentRequestQueue.exists()){
            permanentRequestQueue.newPermRequestQueue(getApplicationContext());
        }

        setContentView(R.layout.activity_login);

        mUserView = (AutoCompleteTextView) findViewById(R.id.user);
        if(!mayRequestFeatures()){
            Snackbar.make(mUserView, "Permissions Required. Exiting.", Snackbar.LENGTH_INDEFINITE)
                    .setAction(android.R.string.ok, new View.OnClickListener() {
                        @Override
                        @TargetApi(Build.VERSION_CODES.M)
                        public void onClick(View v) {
                            return;
                        }
                    });
            return;
        };

        mPasswordView = (EditText) findViewById(R.id.password);
        mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == R.id.login || id == EditorInfo.IME_NULL) {
                    attemptLogin();
                    return true;
                }
                return false;
            }
        });

        Button mUserSignInButton = (Button) findViewById(R.id.user_sign_in_button);
        mUserSignInButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptLogin();
            }
        });

        Button mCreateUserButton = (Button) findViewById(R.id.create_user_button);
        mCreateUserButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                createNewUser();
            }
        });

        mLoginFormView = findViewById(R.id.login_form);
        mProgressView = findViewById(R.id.login_progress);
        mTextView = (TextView) findViewById(R.id.login_text);

        final SharedPreferences checkPrefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        final String checkSession = checkPrefs.getString("sessionID", null);

        String checkURL = "http://www.sean-mulholland.com:3003/groups/";

        JSONObject body = null;

        Map<String, String> headers = new HashMap<String, String>();
        headers.put("cookie", checkSession);
        headers.put("content-type", "application/json");

        fullJSONRequest userCheck = new fullJSONRequest(Request.Method.GET, checkURL, body, headers,
                new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {

                        try {
                            if(response.getJSONObject("response").getBoolean("status")) {
                                final String username = checkPrefs.getString("username",null);
                                Intent moveAhead = new Intent(getBaseContext(), userScreen.class);
                                startActivity(moveAhead);
                            } else {
                                return;
                            }
                        } catch (JSONException e) {
                            System.err.println(e);
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            System.err.println(error);
                            error.printStackTrace();
                            mTextView.setText("App: API can't be reached");
                            //Error checking not necessary yet
                            return;
                        }
                    });
        try {
            permanentRequestQueue.getPermRequestQueue().addToMyRequestQueue(userCheck);
        } catch (InvalidObjectException e){
            System.err.println(e);
            e.printStackTrace();
        }
    }

    private boolean mayRequestFeatures() {

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return true;
        }

        if (checkSelfPermission(INTERNET) == PackageManager.PERMISSION_GRANTED &&
            checkSelfPermission(ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
            checkSelfPermission(ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            return true;
        }

        //Request Internet As Necessary
        if (shouldShowRequestPermissionRationale(INTERNET)) {
            Snackbar.make(mUserView,R.string.int_permission_rationale, Snackbar.LENGTH_INDEFINITE)
                    .setAction(android.R.string.ok, new View.OnClickListener() {
                        @Override
                        @TargetApi(Build.VERSION_CODES.M)
                        public void onClick(View v) {
                            requestPermissions(new String[] {INTERNET}, REQUEST_INTERNET);
                        }
                    });
        } else  {
            requestPermissions(new String[]{INTERNET}, REQUEST_INTERNET);
        }

        if(shouldShowRequestPermissionRationale(ACCESS_FINE_LOCATION)){
            Snackbar.make(mUserView,R.string.fine_loc_permission_rationale, Snackbar.LENGTH_INDEFINITE)
                    .setAction(android.R.string.ok, new View.OnClickListener() {
                        @Override
                        @TargetApi(Build.VERSION_CODES.M)
                        public void onClick(View v) {
                            requestPermissions(new String[] {ACCESS_FINE_LOCATION}, REQUEST_FINE_LOCATION);
                        }
                    });
        } else  {
            requestPermissions(new String[]{ACCESS_FINE_LOCATION}, REQUEST_FINE_LOCATION);
        }

        if(shouldShowRequestPermissionRationale(ACCESS_COARSE_LOCATION)){
            Snackbar.make(mUserView,R.string.coarse_loc_permission_rationale, Snackbar.LENGTH_INDEFINITE)
                    .setAction(android.R.string.ok, new View.OnClickListener() {
                        @Override
                        @TargetApi(Build.VERSION_CODES.M)
                        public void onClick(View v) {
                            requestPermissions(new String[] {ACCESS_COARSE_LOCATION}, REQUEST_COARSE_LOCATION);
                        }
                    });
        } else  {
            requestPermissions(new String[]{ACCESS_COARSE_LOCATION}, REQUEST_COARSE_LOCATION);
        }
        return false;
    }

    /**
     * Callback received when a permissions request has been completed.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == REQUEST_INTERNET) {
            if (grantResults.length == 1 &&
                grantResults[0] == PackageManager.PERMISSION_GRANTED){
                mayRequestFeatures();
            }
        }
        if (requestCode == REQUEST_COARSE_LOCATION) {
            if (grantResults.length == 1 &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED){
                mayRequestFeatures();
            }
        }

        if(requestCode == REQUEST_FINE_LOCATION){
            if(grantResults.length == 1 &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED){
                mayRequestFeatures();
            }
        }
    }

    private void attemptLogin() {

        if(!permanentLocationListener.exists()){
            permanentLocationListener.newPermLocationListern(getApplicationContext());
        }
        if(!permanentRequestQueue.exists()){
            permanentRequestQueue.newPermRequestQueue(getApplicationContext());
        }

        mUserView.setError(null);
        mPasswordView.setError(null);

        final String user = mUserView.getText().toString();
        final String password = mPasswordView.getText().toString();

        final TextView mTextView = (TextView) findViewById(R.id.login_text);

        boolean cancel = false;
        View focusView = null;

        if (TextUtils.isEmpty(password))  {
            mPasswordView.setError("Field required");
            focusView = mPasswordView;
            cancel = true;
        }

        if (TextUtils.isEmpty(user)) {
            mUserView.setError("Field required");
            focusView = mUserView;
            cancel = true;
        }

        if (cancel) {
            focusView.requestFocus();
        } else {


            String url = "http://www.sean-mulholland.com:3003/login";
            JSONObject body = new JSONObject();

            try {
                body.put("user", user);
                body.put("password", password);
            } catch (JSONException e) {
                System.err.println(e);
                e.printStackTrace();
            }
            Map<String, String> headers = new HashMap<String, String>();
            headers.put("content-type", "application/json");

            fullJSONRequest loginRequest = new fullJSONRequest(Request.Method.POST, url, body, headers,

                    new Response.Listener<JSONObject>() {

                        @Override
                        public void onResponse(JSONObject response) {

                            try {
                                System.out.println(response);
                                if (response.getJSONObject("response").getBoolean("status")) {
                                    String sessID = response.getJSONObject("headers").getString("set-cookie");
                                    Intent next = new Intent(getBaseContext(), userScreen.class);
                                    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                                    SharedPreferences.Editor edit = prefs.edit();
                                    edit.putString("sessionID", sessID);
                                    edit.putString("username", user);
                                    edit.putString("curLat", String.valueOf(permanentLocationListener.getLat()));
                                    edit.putString("curLon", String.valueOf(permanentLocationListener.getLon()));
                                    edit.commit();
                                    startActivity(next);
                                } else {
                                    mTextView.setText(response.getJSONObject("response").getString("msg"));
                                    showProgress(false);
                                }
                            } catch (JSONException e) {
                                System.err.println(e);
                                e.printStackTrace();
                                mTextView.setText("JSON exception");
                            }
                        }
                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    System.err.println(error);
                    error.printStackTrace();
                    mTextView.setText("App: API can't be reached");
                    showProgress(false);
                }
            });
            try {
                permanentRequestQueue.getPermRequestQueue().addToMyRequestQueue(loginRequest);
            } catch (InvalidObjectException e) {
                System.err.println(e);
                e.printStackTrace();
            }
            showProgress(true);
        }
    }

    private void createNewUser(){
        Intent next = new Intent(getBaseContext(), createUser.class);
        startActivity(next);
    }
    /**
     * Shows the progress UI and hides the login form.
     * Auto written by Android Studio
     * NOT the work of Sean Mulholland********
     *
     *
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            mLoginFormView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mProgressView.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

}

