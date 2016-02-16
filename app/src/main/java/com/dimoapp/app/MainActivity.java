package com.dimoapp.app;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.entity.StringEntity;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;


public class MainActivity extends Activity implements LocationListener
{

    static final int REQUEST_IMAGE_CAPTURE = 1;
    static final String SERVER_IP = "";
    static final String FRAGMENT_TAG = "mainFragment";
    private LocationManager locationManager;
    private Location location;
    private File photoFile;

    @Override
    protected void onStart ()
    {
        super.onStart();
    }

    @Override
    public void onCreate ( Bundle savedInstanceState )
    {
        super.onCreate( savedInstanceState );

        locationManager = ( LocationManager )getSystemService( Context.LOCATION_SERVICE );
        boolean isGPSEnabled = locationManager.isProviderEnabled( LocationManager.GPS_PROVIDER );
        Log.i( "DimoLog", "isGpsEnabled? : " + isGPSEnabled );

        locationManager.requestLocationUpdates( LocationManager.GPS_PROVIDER, 0, 10, this );

        if ( ContextCompat.checkSelfPermission( MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION ) != PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission( MainActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION ) != PackageManager.PERMISSION_GRANTED )
        {
            // TODO: Consider calling
            //    public void requestPermissions(@NonNull String[] permissions, int requestCode)
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for Activity#requestPermissions for more details.
            throw new RuntimeException( "Permissions not granted" );
        }
        location = locationManager.getLastKnownLocation( LocationManager.GPS_PROVIDER );

        Log.d( "DimoLog", "Location obj is : " + location );

        if ( savedInstanceState == null )
        {
            // During initial setup, plug in the mainFragment fragment.
            MainFragment mainFragment = new MainFragment();
            mainFragment.setArguments( getIntent().getExtras() );
            getFragmentManager().beginTransaction()
                    .add( android.R.id.content, mainFragment, FRAGMENT_TAG ).commit();
        }

        setContentView( R.layout.main_fragment_layout );

        final Button button = ( Button )findViewById( R.id.button_send );
        button.setOnClickListener( new View.OnClickListener()
        {

            public void onClick ( View v )
            {
                Log.d( "DimoLog", "button send clicked" );
                final MainFragment fragment = ( MainFragment )getFragmentManager().findFragmentByTag( FRAGMENT_TAG );

                final AsyncHttpClient ticketClient = new AsyncHttpClient();
                JSONObject jsonParams = new JSONObject();
                StringEntity entity = null;
                try
                {
                    EditText editText = ( EditText )findViewById( R.id.edit_message );
                    String messageAsUtf8 = new String( editText.getText().toString().getBytes() );
                    jsonParams.put( "message", messageAsUtf8 );
                    jsonParams.put( "latitude", fragment.getmLat() );
                    jsonParams.put( "longitude", fragment.getmLon() );
                    entity = new StringEntity( jsonParams.toString(), "UTF-8" );
                    editText.setEnabled( false );
                } catch ( JSONException e )
                {
                    e.printStackTrace();
                }

                Button sendButton = ( Button )findViewById( R.id.button_send );
                sendButton.setClickable( false );
                ButtonSkinUtil.setButtonLoading( sendButton );

                ticketClient.post( getApplicationContext(), "http://" + SERVER_IP + ":8080/api/newticket", entity, "application/json", new AsyncHttpResponseHandler()
                {

                    @Override
                    public void onStart ()
                    {
                        Log.d( "DimoLog", "on start" );
                    }

                    @Override
                    public void onSuccess ( int statusCode, Header[] headers, byte[] response )
                    {
                        Log.i( "DimoLog", "response is 200" );
                        Log.d( "DimoLog", "pic internal url is : " + fragment.getmPhotoFile().getAbsolutePath() );

                        AsyncHttpClient picClient = new AsyncHttpClient();
                        RequestParams params = new RequestParams();

                        String responseAsString = new String( response );
                        Log.i( "DimoLog", "response string is : " + responseAsString );
                        JSONObject jsonObject = null;
                        try
                        {
                            jsonObject = new JSONObject( responseAsString );
                        } catch ( JSONException e )
                        {
                            Log.e( "DimoLog", "Failed to map response to JSONObject" );
                            e.printStackTrace();
                        }

                        try
                        {
                            params.put( "ticketId", jsonObject.getLong( "id" ) );
                            params.put( "multipartFile", fragment.getmPhotoFile() );
                        } catch ( FileNotFoundException e )
                        {
                            Log.e( "DimoLog", "pic File url not found." );
                            e.printStackTrace();
                        } catch ( JSONException e )
                        {
                            Log.e( "DimoLog", "Failed to get id from JSONObject" );
                            e.printStackTrace();
                        }

                        picClient.post( "http://" + SERVER_IP + ":8080/api/newticketimage", params, new AsyncHttpResponseHandler()
                        {

                            @Override
                            public void onSuccess ( int statusCode, Header[] headers, byte[] responseBody )
                            {
                                Log.i( "DimoLog", "pic uploaded." );
                                Button sendButton = ( Button )findViewById( R.id.button_send );
                                ButtonSkinUtil.setButtonSuccess( sendButton );
                            }

                            @Override
                            public void onFailure ( int statusCode, Header[] headers, byte[] responseBody, Throwable error )
                            {
                                Log.e( "DimoLog", "pic upload failed." );
                                Button sendButton = ( Button )findViewById( R.id.button_send );
                                ButtonSkinUtil.setButtonFailure( sendButton );
                            }
                        } );
                    }

                    @Override
                    public void onFailure ( int statusCode, Header[] headers, byte[] errorResponse, Throwable e )
                    {
                        Log.e( "DimoLog", "Failure - response is :" + statusCode );
                    }

                    @Override
                    public void onRetry ( int retryNo )
                    {
                        Log.w( "DimoLog", "on retry, number : " + retryNo );
                    }
                } );
            }
        } );
        this.dispatchTakePictureIntent();
    }

    @Override
    public void onLocationChanged ( Location location )
    {
        Log.d( "DimoLog", "Location changed. New Location : " + location );
    }

    @Override
    public void onStatusChanged ( String s, int i, Bundle bundle )
    {
        Log.d( "DimoLog", "(location) status changed to : " + s );
    }

    @Override
    public void onProviderEnabled ( String s )
    {
        Log.d( "DimoLog", "Provider has been enabled : " + s );
    }

    @Override
    public void onProviderDisabled ( String s )
    {
        Log.d( "DimoLog", "Provider has been disabled : " + s );
    }

    @Override
    protected void onActivityResult ( int requestCode, int resultCode, Intent data )
    {
        super.onActivityResult( requestCode, resultCode, data );
        Log.d( "DimoLog", "onActivityResult(activity) invoked" );

        if ( requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK )
        {
            MainFragment fragment = ( MainFragment )getFragmentManager().findFragmentByTag( FRAGMENT_TAG );

            if ( ContextCompat.checkSelfPermission( MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION ) != PackageManager.PERMISSION_GRANTED
                    && ContextCompat.checkSelfPermission( MainActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION ) != PackageManager.PERMISSION_GRANTED )
            {
                throw new RuntimeException( "Permissions not granted" );
            }

            this.location = this.locationManager.getLastKnownLocation( LocationManager.GPS_PROVIDER );
            if ( location != null )
            {
                fragment.setmLat( location.getLatitude() );
                fragment.setmLon( location.getLongitude() );
            }
            if ( photoFile != null )
            {
                fragment.setmPhotoFile( this.photoFile );
            }
            EditText editText = ( EditText )findViewById( R.id.edit_message );
            fragment.setmMessageText( editText.getText().toString() );
        }
    }

    private File createImageFile () throws IOException
    {
        // Create an image file name
        String timeStamp = new SimpleDateFormat( "ddMMyyyy_HHmmss" ).format( new Date() );
        String imageFileName = "DIMO_" + timeStamp + "_";
        File storageDir = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES );
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );
        return image;
    }

    private void dispatchTakePictureIntent ()
    {
        Intent takePictureIntent = new Intent( MediaStore.ACTION_IMAGE_CAPTURE );
        // Create the File where the photo should go
        // Ensure that there's a camera activity to handle the intent
        if ( takePictureIntent.resolveActivity( getPackageManager() ) != null )
        {
            try
            {
                photoFile = createImageFile();
            } catch ( IOException ex )
            {
                ex.printStackTrace();
            }
        }
        // Continue only if the File was successfully created
        if ( photoFile != null )
        {
            takePictureIntent.putExtra( MediaStore.EXTRA_OUTPUT, Uri.fromFile( photoFile ) );
            startActivityForResult( takePictureIntent, REQUEST_IMAGE_CAPTURE );
        }
    }
}
