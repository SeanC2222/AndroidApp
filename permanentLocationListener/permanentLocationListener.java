package com.example.permanentlocationlistener;

import android.location.LocationProvider;
import android.os.Build;
import android.os.Bundle;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;

import java.io.InvalidObjectException;

/**
 * Created by SeanC on 8/10/2016.
 */
public class permanentLocationListener {

    //Private members
    private static permanentLocationListener myPermanentLocationListener;
    private LocationManager myLocationManager;
    private LocationListener myLocationListener; //This will be contained in myPermanentRequest
    private static double D_CUR_LAT;
    private static double D_CUR_LON;
    private static Context appContext;


    //Constructor
    private permanentLocationListener(Context appCont){
        myPermanentLocationListener = null; //Can't instantiate in constructor or an endless loop forms
        appContext = appCont;
        myLocationManager = (LocationManager)appCont.getSystemService(Context.LOCATION_SERVICE);
        myLocationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                D_CUR_LAT = location.getLatitude();
                D_CUR_LON = location.getLongitude();
            }

            @Override
            public void onStatusChanged(String s, int i, Bundle bundle) {
                if(i == LocationProvider.OUT_OF_SERVICE) {
                    D_CUR_LAT = 0;
                    D_CUR_LON = 0;
                } else {
                    //do nothing
                }
            }

            @Override
            public void onProviderEnabled(String s) {

            }

            @Override
            public void onProviderDisabled(String s) {
                D_CUR_LAT = 0;
                D_CUR_LON = 0;
            }
        };
        try {
            myLocationManager.requestLocationUpdates(myLocationManager.GPS_PROVIDER, 5, 25, myLocationListener);
        } catch (SecurityException e) {
            myLocationManager = null;
            System.err.println(e);
            e.printStackTrace();
        }
    }

    //Returns the LocationListener
    private LocationListener getMyLocationListener() throws InvalidObjectException {
        if(myLocationManager == null){
            myLocationManager = (LocationManager)appContext.getSystemService(Context.LOCATION_SERVICE);
        }
        if(myLocationListener == null){
            if(appContext != null && myLocationManager != null) {
                myLocationListener = new LocationListener() {
                    @Override
                    public void onLocationChanged(Location location) {
                        D_CUR_LAT = location.getLatitude();
                        D_CUR_LON = location.getLongitude();
                    }

                    @Override
                    public void onStatusChanged(String s, int i, Bundle bundle) {
                        if(i == LocationProvider.OUT_OF_SERVICE) {
                            D_CUR_LAT = 0;
                            D_CUR_LON = 0;
                        } else {
                            //do nothing
                        }
                    }

                    @Override
                    public void onProviderEnabled(String s) {

                    }

                    @Override
                    public void onProviderDisabled(String s) {
                        D_CUR_LAT = 0;
                        D_CUR_LON = 0;
                    }
                };
                try {
                    myLocationManager.requestLocationUpdates(myLocationManager.GPS_PROVIDER, 5, 25, myLocationListener);
                } catch (SecurityException e) {
                    myLocationManager = null;
                    System.err.println(e);
                    e.printStackTrace();
                }
            } else {
                throw new InvalidObjectException(new String("No context or LocationManager"));
            }
        }
        return myLocationListener; //no error checking included
    }

    //Public Methods
    //Instantiates a new internal static permanentRequestQueue, then returns that instance
    public static synchronized permanentLocationListener newPermLocationListern(Context cont){
        myPermanentLocationListener = new permanentLocationListener(cont); //Instantiates new static member
        return myPermanentLocationListener;
    }

    //Gets current internal static instance; returns null if no instance created!
    public static synchronized permanentLocationListener getPermLocationListener() throws  InvalidObjectException{
        if(myPermanentLocationListener == null){
            if(appContext != null) {
                myPermanentLocationListener = newPermLocationListern(appContext);
            } else {
                throw new InvalidObjectException(new String("No context"));
            }
        }
        return myPermanentLocationListener; //no error checking included
    }

    public static synchronized boolean exists(){
        if(myPermanentLocationListener != null){
            return true;
        } else {
            return false;
        }
    }

    public static double getLat(){
        return D_CUR_LAT;
    }

    public static double getLon(){
        return D_CUR_LON;
    }

}
