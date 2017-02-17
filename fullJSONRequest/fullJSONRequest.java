package com.seanc.fullJSONRequest;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.JsonRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.Map;

/**
 * Created by SeanC on 8/7/2016.
 */
public class fullJSONRequest extends JsonRequest<JSONObject> {

    //Private Members
    private final Map<String, String> mHeaders; //Used for request AND response
    private final Response.Listener mListener; //Stores Response.Listener in object
    //Public Methods
    public fullJSONRequest(int method, String url, JSONObject body, Map<String, String> headers,
                           Response.Listener listener, Response.ErrorListener errorListener){
        super((method < 0 || method > 9) ? Request.Method.GET : method,
            (url != null) ? url : "",
            body == null ? null : body.toString(),
            listener,
            errorListener);
        mListener = listener;
        mHeaders = headers;
    }

    //Gathers returns mHeaders used in performing request   @Override
    public Map<String, String> getHeaders() throws AuthFailureError {
        return (mHeaders == null) ? super.getHeaders() : mHeaders;
    }

    @Override
    protected void deliverResponse(JSONObject response){
        mListener.onResponse(response);
    }

    @Override
    protected Response parseNetworkResponse(NetworkResponse response){
        try{
            JSONObject jsonResponse = new JSONObject(new String(response.data)); //Stores response body
            jsonResponse.put("headers",new JSONObject(response.headers)); //Adds response headers as JSONObject
            jsonResponse.put("statusCode", response.statusCode); //Adds response status code as int
            return Response.success(jsonResponse, HttpHeaderParser.parseCacheHeaders(response));
        } catch (JSONException je) {
            return Response.error(new ParseError(je));
        }
    }
}
