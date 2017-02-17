package com.seanc.permanentrequestqueue;

import android.content.Context;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

import java.io.InvalidObjectException;

/**
 * Created by SeanC on 8/7/2016.
 */

public class permanentRequestQueue {

    //Private members
    private static permanentRequestQueue myPermanentRequestQueue;
    private RequestQueue myRequestQueue; //This will be contained in myPermanentRequest
    private static Context appContext;

    //Private Methods
    //Constructor
    private permanentRequestQueue(Context appCont){
        myPermanentRequestQueue = null; //Can't instantiate in constructor or an endless loop forms
        appContext = appCont;
        myRequestQueue = Volley.newRequestQueue(appContext); //getMyRequestQueue() defined below
    }

    //Returns the RequestQueue
    private RequestQueue getMyRequestQueue() throws InvalidObjectException{
        if(myRequestQueue == null){
            if(appContext != null) {
                myRequestQueue = Volley.newRequestQueue(appContext);
            } else {
                throw new InvalidObjectException(new String("No context"));
            }
        }
        return myRequestQueue; //no error checking included
    }

    //Public Methods
    //Instantiates a new internal static permanentRequestQueue, then returns that instance
    public static synchronized permanentRequestQueue newPermRequestQueue(Context cont){
        myPermanentRequestQueue = new permanentRequestQueue(cont); //Instantiates new static member
        return myPermanentRequestQueue;
    }

    //Gets current internal static instance; returns null if no instance created!
    public static synchronized permanentRequestQueue getPermRequestQueue() throws  InvalidObjectException{
        if(myPermanentRequestQueue == null){
            if(appContext != null) {
                myPermanentRequestQueue = newPermRequestQueue(appContext);
            } else {
                throw new InvalidObjectException(new String("No context"));
            }
        }
        return myPermanentRequestQueue; //no error checking included
    }

    public static synchronized boolean exists(){
        if(myPermanentRequestQueue != null){
            return true;
        } else {
            return false;
        }
    }
    //Gets the RequestQueue internal to the static instance and adds the request to that RequestQueue
    public void addToMyRequestQueue(Request request) throws InvalidObjectException{
        try {
            getMyRequestQueue().add(request); //Adds request to the internal RequestQueue of myInstance
        } catch (InvalidObjectException e) {
            throw e;
        }
    }

}