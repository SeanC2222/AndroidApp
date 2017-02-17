package com.example.seanc.assignment4;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.InvalidObjectException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class makeGroup extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_make_group);
        if(!permanentLocationListener.exists()){
            permanentLocationListener.newPermLocationListern(getApplicationContext());
        }
        if(!permanentRequestQueue.exists()){
            permanentRequestQueue.newPermRequestQueue(getApplicationContext());
        }

    }

    public void buttonClick(View v){

        if(!permanentLocationListener.exists()){
            permanentLocationListener.newPermLocationListern(getApplicationContext());
        }
        if(!permanentRequestQueue.exists()){
            permanentRequestQueue.newPermRequestQueue(getApplicationContext());
        }

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        final String sessID = prefs.getString("sessionID", null);
        final String userName = prefs.getString("username", null);


        TextView mGroupName = (TextView) findViewById(R.id.new_group_name);

        final String group_name = mGroupName.getText().toString();

        boolean cancel = false;
        View focusView = null;

        if (TextUtils.isEmpty(group_name))  {
            mGroupName.setError("Field required");
            focusView = mGroupName;
            cancel = true;
        }

        if (cancel) {
            focusView.requestFocus();
        } else {
            final String url = "http://www.sean-mulholland.com:3003/groups/" + mGroupName.getText();

            JSONObject group = new JSONObject();
            try {
                group.put("items", new JSONArray());
            } catch (JSONException e) {
                e.printStackTrace();
            }

            Map<String, String> headers = new HashMap<String, String>();
            headers.put("content-type", "application/json");
            headers.put("cookie", sessID);

            fullJSONRequest makeGroupRequest = new fullJSONRequest(Request.Method.POST, url, group, headers,

                    new Response.Listener<JSONObject>() {

                        @Override
                        public void onResponse(JSONObject response) {
                            try {
                                if (response.getJSONObject("response").getBoolean("status")) {
                                    Toast toast = Toast.makeText(getApplicationContext(), "Group Created!", Toast.LENGTH_SHORT);
                                    toast.show();
                                    Intent returnGroups = new Intent(getApplicationContext(), add_groups.class);
                                    startActivity(returnGroups);
                                } else {
                                    Toast toast = Toast.makeText(getApplicationContext(), "Must log-in again!", Toast.LENGTH_SHORT);
                                    toast.show();
                                    Intent returnLogin = new Intent(getBaseContext(), LoginActivity.class);
                                    startActivity(returnLogin);
                                }
                                ;

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
                }
            });

            // Add the request to the RequestQueue.
            try {
                permanentRequestQueue.getPermRequestQueue().addToMyRequestQueue(makeGroupRequest);
            } catch (InvalidObjectException e) {
                System.err.println(e);
                e.printStackTrace();
            }
        }
    }
}
