package com.example.seanc.assignment4;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.permanentlocationlistener.permanentLocationListener;
import com.seanc.fullJSONRequest.fullJSONRequest;
import com.seanc.permanentrequestqueue.permanentRequestQueue;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.InvalidObjectException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static android.Manifest.permission.INTERNET;

public class createUser extends AppCompatActivity  {


    private AutoCompleteTextView mUserView;
    private EditText mPasswordView;
    private View mProgressView;
    private View mLoginFormView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_user);
        mUserView = (AutoCompleteTextView) findViewById(R.id.new_user);

        if (!permanentLocationListener.exists()) {
            permanentLocationListener.newPermLocationListern(getApplicationContext());
        }
        if(!permanentRequestQueue.exists()){
            permanentRequestQueue.newPermRequestQueue(getApplicationContext());
        }
        mPasswordView = (EditText) findViewById(R.id.new_password);
        mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == R.id.new_password || id == EditorInfo.IME_NULL) {
                    createUser();
                    return true;
                }
                return false;
            }
        });

        Button mCreateUserButton = (Button) findViewById(R.id.create_user_button);
        mCreateUserButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                createUser();
            }
        });

        mLoginFormView = findViewById(R.id.login_form);
        mProgressView = findViewById(R.id.login_progress);
    }

    private void createUser() {

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

        final TextView mTextView = (TextView) findViewById(R.id.create_text);

        boolean cancel = false;
        View focusView = null;

        if (TextUtils.isEmpty(password)) {
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

            String url = "http://www.sean-mulholland.com:3003/createUser";
            final JSONObject newUserObject = new JSONObject();
            try {
                newUserObject.put("adminUser", adminAccess.getAdminUser());
                newUserObject.put("adminPass", adminAccess.getAdminPass());
                newUserObject.put("user", user);
                newUserObject.put("password", password);
            } catch (JSONException e) {
                System.err.println(e);
                e.printStackTrace();
            }
            Map<String, String> headers = new HashMap<String, String>();
            headers.put("content-type", "application/json");

            fullJSONRequest newUser = new fullJSONRequest(Request.Method.POST, url, newUserObject, headers,

                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            try {
                                if (response.getJSONObject("response").getBoolean("status")) {
                                    Toast toast = Toast.makeText(getApplicationContext(), "User created!", Toast.LENGTH_SHORT);
                                    toast.show();
                                    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                                    SharedPreferences.Editor edit = prefs.edit();
                                    edit.putString("username", user);
                                    edit.commit();
                                    Intent next = new Intent(getBaseContext(), LoginActivity.class);
                                    startActivity(next);
                                } else {
                                    mTextView.setText(response.getJSONObject("response").getString("msg"));
                                }
                            } catch (JSONException e) {
                                mTextView.setText("JSON exception");
                            }
                        }
                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    System.err.println(error);
                    error.printStackTrace();
                    mTextView.setText("That didn't work!");
                }
            });
            try {
                permanentRequestQueue.getPermRequestQueue().addToMyRequestQueue(newUser);
            } catch (InvalidObjectException e) {
                System.err.println(e);
                e.printStackTrace();
            }
        }
    }

    private void goToLogin() {
        Intent next = new Intent(getApplicationContext(), LoginActivity.class);
        startActivity(next);
    }

}