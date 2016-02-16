package com.dimoapp.app;

import android.app.Fragment;
import android.os.Bundle;
import android.util.Log;

import java.io.File;


public class MainFragment extends Fragment
{

    private String mMessageText = "";
    private double mLat;
    private double mLon;
    private File mPhotoFile;

    public MainFragment ()
    {
    }

    @Override
    public void onActivityCreated ( Bundle savedInstanceState )
    {
        super.onActivityCreated( savedInstanceState );

        if ( savedInstanceState != null )
        {
            // Restore last state for checked position.
            this.mMessageText = savedInstanceState.getString( "messageText" );
            this.mLat = savedInstanceState.getDouble( "lat" );
            this.mLon = savedInstanceState.getDouble( "lon" );
        }
        Log.i( "LocationLog", "onActivityCreated (frag) invoked" );
        // TODO: 07/02/2016 initialise front
    }

    @Override
    public void onSaveInstanceState ( Bundle outState )
    {
        super.onSaveInstanceState( outState );
        outState.putString( "messageText", this.mMessageText );
        outState.putDouble( "lat", this.mLat );
        outState.putDouble( "lat", this.mLon );

        Log.d( "LocationLog", "onSave (frag) invoked" );
    }

    public String getmMessageText ()
    {
        return mMessageText;
    }

    public double getmLat ()
    {
        return mLat;
    }

    public double getmLon ()
    {
        return mLon;
    }

    public void setmMessageText ( String mMessageText )
    {
        this.mMessageText = mMessageText;
    }

    public void setmLat ( double mLat )
    {
        this.mLat = mLat;
    }

    public void setmLon ( double mLon )
    {
        this.mLon = mLon;
    }

    public File getmPhotoFile ()
    {
        return mPhotoFile;
    }

    public void setmPhotoFile ( File mPhotoFile )
    {
        this.mPhotoFile = mPhotoFile;
    }
}
