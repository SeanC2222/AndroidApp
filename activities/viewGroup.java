package com.example.seanc.assignment4;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.example.permanentlocationlistener.permanentLocationListener;
import com.seanc.fullJSONRequest.fullJSONRequest;
import com.seanc.permanentrequestqueue.permanentRequestQueue;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.InvalidObjectException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class viewGroup extends AppCompatActivity {

    private String group_name;
    private int req_code = 5;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_group);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if(!permanentLocationListener.exists()) {
            permanentLocationListener.newPermLocationListern(getApplicationContext());
        }
        if(!permanentRequestQueue.exists()){
            permanentRequestQueue.newPermRequestQueue(getApplicationContext());
        }

        Bundle extras = getIntent().getExtras();
        if(extras != null) {
            group_name = extras.getString("group_name");
            setTitle(group_name);
        } else {
            group_name = "Viewing Group...";
        }

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setImageResource(R.drawable.myfab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent addItem = new Intent(getApplicationContext(), make_item.class);
                addItem.putExtra("group_name", group_name);
                startActivity(addItem);
            }
        });
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        final String sessID = prefs.getString("sessionID", null);
        final String userName = prefs.getString("username", null);

        String url = "http://www.sean-mulholland.com:3003/groups/" + group_name + "/items";


        JSONObject body = null;

        Map<String, String> headers = new HashMap<String, String>();
        headers.put("content-type", "application/json");
        headers.put("cookie", sessID);

        final ListView mItemList = (ListView)findViewById(R.id.item_list);
        final ArrayList<String> item_list = new ArrayList<String>();
        final ArrayList<String> url_list = new ArrayList<String>();
        final ArrayList<String> color_list = new ArrayList<String>();

        fullJSONRequest itemRequest = new fullJSONRequest(Request.Method.GET, url, body, headers, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    response = response.getJSONObject("response");
                    if (response.getBoolean("status")) {
                        JSONArray items = response.getJSONArray("data");

                        for(int i = 0; i < items.length(); i++){
                            int iPlusOne = i+1;
                            JSONObject item_i = items.getJSONObject(i);
                            item_list.add(item_i.getString("name"));
                            url_list.add(item_i.getString("uri"));
                            color_list.add(item_i.getString("color"));
                        }
                        final ArrayAdapter<String> adapt = new ArrayAdapter<String>(getApplicationContext(), R.layout.myitemlistview, R.id.item_name, item_list){
                            @Override
                            public View getView (final int position, View convertView, ViewGroup parent){
                                LayoutInflater inflater = (LayoutInflater)getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                                if( convertView == null ){
                                    convertView = inflater.inflate(R.layout.myitemlistview, parent, false);
                                }
                                TextView index = (TextView)convertView.findViewById(R.id.item_index);
                                index.setText("Item " + String.valueOf(position+1) + ": ");
                                index.setClickable(true);
                                TextView name = (TextView)convertView.findViewById(R.id.item_name);
                                name.setText(item_list.get(position));
                                name.setClickable(true);
                                name.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        String checkURL = url_list.get(position);
                                        if(checkURL.substring(0,6) != "http://" ||
                                                checkURL.substring(0,7) != "https://"){
                                            checkURL = "http://" + checkURL;
                                        }
                                        Uri uri = Uri.parse(checkURL);
                                        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                                        intent.setData(uri);
                                        startActivity(intent);
                                    }
                                });
                                Button delButton = (Button)convertView.findViewById(R.id.item_delete);
                                delButton.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        onDelete(item_list.get(position));
                                    }
                                });
                                Button updButton = (Button)convertView.findViewById(R.id.item_update);
                                updButton.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        Intent updateItem = new Intent(getApplicationContext(), update_item.class);
                                        updateItem.putExtra("item_name", item_list.get(position));
                                        updateItem.putExtra("group_name", group_name);
                                        updateItem.putExtra("item_uri", url_list.get(position));
                                        startActivity(updateItem);
                                    }
                                });
                                return convertView;
                            }
                        };
                        mItemList.setAdapter(adapt);
                    } else {
                        Toast toast=Toast.makeText(getApplicationContext(),"Must log-in again!",Toast.LENGTH_SHORT);
                        Intent returnLogin = new Intent(getBaseContext(), LoginActivity.class);
                        startActivity(returnLogin);
                    }
                } catch (JSONException e){
                    System.err.println(e);
                    e.printStackTrace();
                    item_list.add(e.toString());
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                System.err.println(error);
                error.printStackTrace();
                item_list.add(error.toString());
                return;
            }
        });
        try {
            permanentRequestQueue.getPermRequestQueue().addToMyRequestQueue(itemRequest);
        } catch (InvalidObjectException e){
            System.err.println(e);
            e.printStackTrace();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        if(requestCode == requestCode && resultCode == RESULT_OK){
            Bundle extras = data.getExtras();
            group_name = extras.getString("group_name");
        }
    }

    public void onDelete(String item_name){

        if(!permanentLocationListener.exists()) {
            permanentLocationListener.newPermLocationListern(getApplicationContext());
        }
        if(!permanentRequestQueue.exists()){
            permanentRequestQueue.newPermRequestQueue(getApplicationContext());
        }
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        final String sessID = prefs.getString("sessionID", null);
        final String userName = prefs.getString("username", null);


        final JSONObject body = new JSONObject();
        final JSONObject item = new JSONObject();
        try{
            item.put("name", item_name);
            body.put("item", item);
        } catch (JSONException e){
            System.err.println(e);
            e.printStackTrace();
        }

        final Map<String, String> headers = new HashMap<String, String>();
        headers.put("content-type", "application/json");
        headers.put("cookie", sessID);
        try{
            item_name = URLEncoder.encode(item_name, "utf-8");
        } catch (UnsupportedEncodingException e){
            System.err.println(e);
            e.printStackTrace();
        }
        String url = "http://www.sean-mulholland.com:3003/groups/" + group_name + "/items/" + item_name;

        System.out.println(url);
        System.out.println(body);

        fullJSONRequest delRequest = new fullJSONRequest(Request.Method.DELETE, url, body, headers,

                new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    try {
                        response = response.getJSONObject("response");
                        System.out.println(response);
                        if (response.getBoolean("status")) {
                            Toast toast=Toast.makeText(getApplicationContext(),"Item removed!",Toast.LENGTH_SHORT);
                            Intent returnGroup = new Intent(getApplicationContext(), viewGroup.class);
                            returnGroup.putExtra("group_name", group_name);
                            startActivity(returnGroup);
                        } else {
                            Toast toast=Toast.makeText(getApplicationContext(),"Must log-in again!",Toast.LENGTH_SHORT);
                            Intent returnLogin = new Intent(getApplicationContext(), LoginActivity.class);
                            startActivity(returnLogin);
                        }
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
                    return;
                }
            });

            try {
                permanentRequestQueue.getPermRequestQueue().addToMyRequestQueue(delRequest);
            } catch (InvalidObjectException e){
                System.err.println(e);
                e.printStackTrace();
            }
        }
}

