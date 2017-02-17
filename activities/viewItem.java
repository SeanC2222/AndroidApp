package com.example.seanc.assignment4;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.seanc.fullJSONRequest.fullJSONRequest;
import com.seanc.permanentrequestqueue.permanentRequestQueue;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.InvalidObjectException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class viewItem extends AppCompatActivity {

    private String group_name;
    private int req_code = 6;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_group);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        Bundle extras = getIntent().getExtras();
        if(extras != null) {
            group_name = extras.getString("item_name");
            setTitle(group_name);
        }

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent addItem = new Intent(getApplicationContext(), makeItem.class);
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
                        final ArrayAdapter<String> adapt = new ArrayAdapter<String>(getApplicationContext(), R.layout.mylistview, R.id.group_name, item_list){
                            @Override
                            public View getView (final int position, View convertView, ViewGroup parent){
                                LayoutInflater inflater = (LayoutInflater)getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                                if( convertView == null ){
                                    convertView = inflater.inflate(R.layout.mylistview, parent, false);
                                }
                                TextView index = (TextView)convertView.findViewById(R.id.group_index);
                                index.setText("Item " + String.valueOf(position+1) + ": ");
                                TextView name = (TextView)convertView.findViewById(R.id.group_name);
                                name.setText(item_list.get(position));
                                name.setOnClickListener(new View.OnClickListener(){
                                    @Override
                                    public void onClick(View view) {
                                        Intent viewGroup = new Intent(getApplicationContext(), viewGroup.class);
                                        viewGroup.putExtra("group_name",item_list.get(position));
                                        startActivityForResult(viewGroup, req_code);
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

}
