package com.example.seanc.assignment4;

/**
 * Created by SeanC on 8/12/2016.
 */

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Html;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.widget.CheckBox;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.example.permanentlocationlistener.permanentLocationListener;
import com.google.gson.JsonObject;
import com.seanc.fullJSONRequest.fullJSONRequest;
import com.seanc.permanentrequestqueue.permanentRequestQueue;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.InvalidObjectException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.CheckBox;
import android.widget.RadioButton;
import android.widget.RadioGroup;
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
import java.util.HashMap;
import java.util.Map;

public class updateItem extends Activity {


    private TextView mItemName;
    private TextView mURI;
    private RadioGroup mColor;
    private CheckBox mFavorite;
    private RadioGroup mType;
    private String group_name;
    private String item_name;
    private String item_uri;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_item);
        if(!permanentLocationListener.exists()){
            permanentLocationListener.newPermLocationListern(getApplicationContext());
        }
        if(!permanentRequestQueue.exists()){
            permanentRequestQueue.newPermRequestQueue(getApplicationContext());
        }


        Bundle extras = getIntent().getExtras();
        if(extras != null) {
            group_name = extras.getString("group_name");
            item_name = extras.getString("item_name");
            item_uri = extras.getString("item_uri");
        } else {

            setTitle(item_name);
        };

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        final String sessID = prefs.getString("sessionID", null);
        final String userName = prefs.getString("username", null);

        mItemName = (TextView) findViewById(R.id.new_item_name);
        mItemName.setText(item_name);
        mURI = (TextView) findViewById(R.id.new_item_uri);
        mURI.setText(item_uri);

        mColor = (RadioGroup) findViewById(R.id.color);
        mType = (RadioGroup) findViewById(R.id.type);
        mFavorite = (CheckBox) findViewById(R.id.new_item_f);

        try {
            item_name = URLEncoder.encode(item_name, "utf-8");
            group_name = URLEncoder.encode(group_name, "utf-8");
        } catch (UnsupportedEncodingException e){
            System.err.println(e);
            e.printStackTrace();
        }
        String url = "http://www.sean-mulholland.com:3003/groups/" + group_name + "/items/" + item_name;

        JSONObject body = null;

        final Map<String, String> headers = new HashMap<String, String>();
        headers.put("content-type", "application/json");
        headers.put("cookie", sessID);

        System.out.println(url);

        JsonObjectRequest getData = new JsonObjectRequest(Request.Method.GET, url, body,
                new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            response = response.getJSONObject("response");
                            JSONArray data = response.getJSONArray("data");
                            JSONObject item = data.getJSONObject(0);
                            System.out.println(item);
                            if (response.getBoolean("status")) {
                                if(item.getString("color").equals("red")){
                                    RadioButton red = (RadioButton)findViewById(R.id.red);
                                    red.setChecked(true);
                                } else if (item.getString("color").equals("blue")){
                                    RadioButton blue = (RadioButton)findViewById(R.id.blue);
                                    blue.setChecked(true);
                                } else if (item.getString("color").equals("yellow")){
                                    RadioButton yellow = (RadioButton)findViewById(R.id.yellow);
                                    yellow.setChecked(true);
                                } else {
                                    RadioButton none = (RadioButton)findViewById(R.id.none);
                                    none.setChecked(true);
                                }
                                if(item.getString("type").equals("image")){
                                    RadioButton image = (RadioButton)findViewById(R.id.image);
                                    image.setChecked(true);
                                } else if (item.getString("type").equals("video")){
                                    RadioButton video = (RadioButton)findViewById(R.id.video);
                                    video.setChecked(true);
                                } else if (item.getString("type").equals("gif")){
                                    RadioButton gif = (RadioButton)findViewById(R.id.gif);
                                    gif.setChecked(true);
                                } else if (item.getString("type").equals("link")){
                                    RadioButton link = (RadioButton)findViewById(R.id.link);
                                    link.setChecked(true);
                                } else {
                                    RadioButton other = (RadioButton)findViewById(R.id.other);
                                    other.setChecked(true);
                                }
                                if(item.getBoolean("favorite")){
                                    mFavorite.setChecked(true);
                                } else {
                                    mFavorite.setChecked(false);
                                }
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
        }){
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                return headers;
            }
        };
        try {
            permanentRequestQueue.getPermRequestQueue().addToMyRequestQueue(getData);
        } catch (InvalidObjectException e) {
            System.err.println(e);
            e.printStackTrace();
        }

    }

    public void updateButtonClick(View v) {


        if (!permanentLocationListener.exists()) {
            permanentLocationListener.newPermLocationListern(getApplicationContext());
        }
        if (!permanentRequestQueue.exists()) {
            permanentRequestQueue.newPermRequestQueue(getApplicationContext());
        }

        mItemName.setError(null);
        mURI.setError(null);


        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        final String sessID = prefs.getString("sessionID", null);
        final String userName = prefs.getString("username", null);

        final String item_name = mItemName.getText().toString();
        final String item_uri = mURI.getText().toString();

        RadioGroup colorGroup = (RadioGroup) findViewById(R.id.color);

        int color_radio_id = colorGroup.getCheckedRadioButtonId();
        String item_color = new String();
        if(color_radio_id == R.id.red){
            item_color = "red";
        } else if (color_radio_id == R.id.blue){
            item_color = "blue";
        } else if (color_radio_id == R.id.yellow){
            item_color = "yellow";
        } else {
            item_color = "none";
        }

        RadioGroup typeGroup = (RadioGroup) findViewById(R.id.type);

        int type_radio_id = typeGroup.getCheckedRadioButtonId();
        String item_type = new String();
        if(type_radio_id == R.id.image){
            item_type = "image";
        } else if (type_radio_id == R.id.video){
            item_type = "video";
        } else if (type_radio_id == R.id.gif){
            item_type = "gif";
        } else if (type_radio_id == R.id.link){
            item_type = "link";
        } else {
            item_type = "other";
        }

        boolean item_favorite = false;
        CheckBox favorite = (CheckBox) findViewById(R.id.new_item_f);
        if(favorite.isChecked()){
            item_favorite = true;
        }

        boolean cancel = false;
        View focusView = null;

        if (TextUtils.isEmpty(item_name)) {
            mItemName.setError("Field required");
            focusView = mItemName;
            cancel = true;
        }

        if (TextUtils.isEmpty(item_uri)) {
            mURI.setError("Field required");
            focusView = mURI;
            cancel = true;
        }


        if (cancel) {
            focusView.requestFocus();
        } else {

            String url = "http://www.sean-mulholland.com:3003/groups/" + group_name + "/items/";

            JSONObject item = new JSONObject();
            JSONObject item_package = new JSONObject();

            try {
                item.put("name", item_name);
                item.put("uri", item_uri);
                item.put("favorite", item_favorite);
                item.put("type", item_type);
                item.put("color", item_color);
                item_package.put("item", item);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            System.out.println(item_package.toString());

            Map<String, String> headers = new HashMap<String, String>();
            headers.put("content-type", "application/json");
            headers.put("cookie", sessID);

            if(item_package != null) {
                fullJSONRequest makeItemRequest = new fullJSONRequest(Request.Method.PUT, url, item_package, headers,

                        new Response.Listener<JSONObject>() {

                            @Override
                            public void onResponse(JSONObject response) {
                                System.out.println(response.toString());
                                try {
                                    if (response.getJSONObject("response").getBoolean("status")) {
                                        Toast toast = Toast.makeText(getApplicationContext(), "Item Updated!", Toast.LENGTH_SHORT);
                                        toast.show();
                                        Intent returnGroup = new Intent(getApplicationContext(), viewGroup.class);
                                        returnGroup.putExtra("group_name", group_name);
                                        startActivity(returnGroup);
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
                try {
                    permanentRequestQueue.getPermRequestQueue().addToMyRequestQueue(makeItemRequest);
                } catch (InvalidObjectException e) {
                    System.err.println(e);
                    e.printStackTrace();
                }
            }
        }
    }
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event)  {
        if (Integer.parseInt(android.os.Build.VERSION.SDK) > 5
                && keyCode == KeyEvent.KEYCODE_BACK
                && event.getRepeatCount() == 0) {
            onBackPressed();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onBackPressed() {

        Intent intent = new Intent(getApplicationContext(), viewGroup.class);
        intent.putExtra("group_name", group_name);
        startActivity(intent);
    }
}
