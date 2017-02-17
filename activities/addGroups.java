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
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

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

public class addGroups extends AppCompatActivity {

    //Code from Stack Overflow answer here:
        //http://stackoverflow.com/questions/17525886/listview-with-add-and-delete-buttons-in-each-row-in-android
    public class groupItemsAdapter extends BaseAdapter implements ListAdapter {
        private ArrayList<String> list = new ArrayList<String>();
        private Context context;

        public groupItemsAdapter(ArrayList<String> list, Context context) {
            this.list = list;
            this.context = context;
        }

        @Override
        public int getCount() {
            return list.size();
        }

        @Override
        public Object getItem(int pos) {
            return list.get(pos);
        }

        public long getItemId(int pos) {
            return 0;
            //just return 0 if your list items do not have an Id variable.
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            View view = convertView;
            if (view == null) {
                LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                view = inflater.inflate(R.layout.add_groups_listview, null);
            }
            //Handle buttons and add onClickListeners
            Button addButton = (Button)view.findViewById(R.id.add_button);

            addButton.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View v) {
                    addButtonAction(list.get(position));
                    notifyDataSetChanged();

                }
            });

            TextView index = (TextView)view.findViewById(R.id.add_group_index);
            index.setText("Group " + String.valueOf(position+1) + ": ");
            TextView name = (TextView)view.findViewById(R.id.add_group);
            name.setText(list.get(position));
            name.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View view) {
                    Intent viewGroup = new Intent(getApplicationContext(), viewGroup.class);
                    viewGroup.putExtra("group_name",list.get(position));
                    startActivity(viewGroup);
                }
            });

            return view;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_groups);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if(!permanentLocationListener.exists()){
            permanentLocationListener.newPermLocationListern(getApplicationContext());
        }
        if(!permanentRequestQueue.exists()){
            permanentRequestQueue.newPermRequestQueue(getApplicationContext());
        }
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setImageResource(R.drawable.myfab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent makeNewGroup = new Intent(getBaseContext(), makeGroup.class);
                startActivity(makeNewGroup);
            }
        });

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        final String sessID = prefs.getString("sessionID", null);
        final String userName = prefs.getString("username", null);

        final ListView mListView = (ListView) findViewById(R.id.groups_list);

        String url = "http://www.sean-mulholland.com:3003/groups/";

        JSONObject body = null;

        Map<String, String> headers = new HashMap<String, String>();
        headers.put("content-type", "application/json");
        headers.put("cookie", sessID);

        // Request a JSON response from the provided URL.
        fullJSONRequest groupRequest = new fullJSONRequest(Request.Method.GET, url, body, headers,

                new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            if(response.getJSONObject("response").getBoolean("status")){
                                final JSONArray data = response.getJSONObject("response").getJSONArray("data");
                                final ArrayList<String> group_list = new ArrayList<String>();
                                final groupItemsAdapter adapter = new groupItemsAdapter(group_list, getApplicationContext());
                                for(int i = 0; i < data.length(); i++){
                                    JSONObject temp = data.getJSONObject(i);
                                    group_list.add(temp.getString("group"));
                                }

                                mListView.setAdapter(adapter);

                            } else {
                                    Toast toast=Toast.makeText(getApplicationContext(),"Must log-in again!",Toast.LENGTH_SHORT);
                                    toast.show();
                                    Intent returnLogin = new Intent(getBaseContext(), LoginActivity.class);
                                    startActivity(returnLogin);
                            }
                        } catch (JSONException e){
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
            permanentRequestQueue.getPermRequestQueue().addToMyRequestQueue(groupRequest);
        } catch (InvalidObjectException e){
            System.err.println(e);
            e.printStackTrace();
        }
    }

    public void addButtonAction(final String groupName){

        if(!permanentLocationListener.exists()){
            permanentLocationListener.newPermLocationListern(getApplicationContext());
        }

        if(!permanentRequestQueue.exists()){
            permanentRequestQueue.newPermRequestQueue(getApplicationContext());
        }


        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        final String sessID = prefs.getString("sessionID", null);
        final String userName = prefs.getString("username", null);

        JSONObject body = new JSONObject();
        JSONArray groups = new JSONArray();
        JSONObject group = new JSONObject();
        try {
            body.put("user", userName);
            group.put("group", groupName);
            groups.put(group);
            body.put("groups", groups);
        } catch (JSONException e){
            e.printStackTrace();
        }

        Map<String, String> headers = new HashMap<String, String>();
        headers.put("content-type", "application/json");
        headers.put("cookie", sessID);

        final String url = "http://www.sean-mulholland.com:3003/users/" + userName;

        fullJSONRequest groupAddRequest = new fullJSONRequest (Request.Method.PUT, url, body, headers,

                new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        try {

                            if(response.getJSONObject("response").getBoolean("status") == false){
                                Toast toast=Toast.makeText(getApplicationContext(),"Must log-in again!",Toast.LENGTH_SHORT);
                                toast.show();
                                Intent returnLogin = new Intent(getBaseContext(), LoginActivity.class);
                                startActivity(returnLogin);
                            } else {
                                Toast toast = Toast.makeText(getApplicationContext(), "Added group " + groupName, Toast.LENGTH_SHORT);
                                toast.show();
                            }

                        } catch (JSONException e){
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

        try{
            permanentRequestQueue.getPermRequestQueue().addToMyRequestQueue(groupAddRequest);
        } catch (InvalidObjectException e){
            System.err.println(e);
            e.printStackTrace();
        }
    }

}
