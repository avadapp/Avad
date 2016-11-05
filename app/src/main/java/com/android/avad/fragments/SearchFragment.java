package com.android.avad.fragments;

import android.app.Dialog;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v7.widget.AppCompatButton;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.avad.R;
import com.android.avad.adapters.ItemListAdapter;
import com.android.avad.models.Search;
import com.android.avad.utilities.ErrorDialogFragment;
import com.android.avad.utilities.NetworkUtil;
import com.android.avad.utilities.Utils;
import com.facebook.AccessToken;
import com.facebook.AccessTokenTracker;
import com.facebook.CallbackManager;
import com.facebook.FacebookSdk;
import com.facebook.Profile;
import com.facebook.ProfileTracker;
import com.facebook.login.widget.ProfilePictureView;
import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by sagar_000 on 8/7/2016.
 */
public class SearchFragment extends Fragment implements LocationListener,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener{

    private final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;

    public static final String TAG = SearchFragment.class.getSimpleName();

    /*
     * Constants for location update parameters
     */
    // Milliseconds per second
    private static final int MILLISECONDS_PER_SECOND = 1000;

    // The update interval
    private static final int UPDATE_INTERVAL_IN_SECONDS = 5;

    // A fast interval ceiling
    private static final int FAST_CEILING_IN_SECONDS = 1;

    // Update interval in milliseconds
    private static final long UPDATE_INTERVAL_IN_MILLISECONDS = MILLISECONDS_PER_SECOND
            * UPDATE_INTERVAL_IN_SECONDS;

    // A fast ceiling of update intervals, used when the app is visible
    private static final long FAST_INTERVAL_CEILING_IN_MILLISECONDS = MILLISECONDS_PER_SECOND
            * FAST_CEILING_IN_SECONDS;

    Location lastLocation;
    private Location currentLocation;

    private CallbackManager mCallbackManager;


    private static String[] PERMISSIONS_LOCATION = {android.Manifest.permission.ACCESS_FINE_LOCATION,
            android.Manifest.permission.ACCESS_FINE_LOCATION};

    private static int REQUEST_LOCATION = 1;

    private static final String REQUIRED = "Required";

    // A request to connect to Location Services
    private LocationRequest locationRequest;

    // Stores the current instantiation of the location client in this object
    private GoogleApiClient locationClient;
    DatabaseReference mDatabase;
    AccessTokenTracker accessTokenTracker;
    ProfileTracker profileTracker;
    EditText LikeToDo;
    FirebaseAuth mAuth;
    GeoFire geoFire;
    AppCompatButton SearchButton;
    AutoCompleteTextView InterestText;
    View rootView;
    ItemListAdapter mItemListAdapter;
    GeoQuery query;
    GeoLocation center;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        rootView = inflater.inflate(R.layout.fragment_search,
                container, false);

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference("Interests");
        LikeToDo = (EditText)rootView.findViewById(R.id.like_to_do);
        SearchButton = (AppCompatButton) rootView.findViewById(R.id.search_button);
        SearchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                submitPost();
            }
        });

        FacebookSdk.sdkInitialize(getActivity().getApplicationContext());
        // Create a new global location parameters object
        mCallbackManager = CallbackManager.Factory.create();
        accessTokenTracker = new AccessTokenTracker() {
            @Override
            protected void onCurrentAccessTokenChanged(AccessToken accessToken, AccessToken accessToken1) {

            }
        };
        accessTokenTracker.startTracking();

        profileTracker = new ProfileTracker() {
            @Override
            protected void onCurrentProfileChanged(Profile oldProfile, Profile newProfile) {
                ProfilePictureView fbDp = (ProfilePictureView)rootView.findViewById(R.id.fb_dp);
                fbDp.setProfileId(newProfile.getId());
                TextView name = (TextView)rootView.findViewById(R.id.fb_name);
                name.setText(newProfile.getName());
            }
        };
        profileTracker.startTracking();

        locationRequest = LocationRequest.create();


        // Set the update interval
        locationRequest.setInterval(UPDATE_INTERVAL_IN_MILLISECONDS);

        // Use high accuracy
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        // Set the interval ceiling to one minute
        locationRequest.setFastestInterval(FAST_INTERVAL_CEILING_IN_MILLISECONDS);

        // Create a new location client, using the enclosing class to handle callbacks.
        locationClient = new GoogleApiClient.Builder(this.getContext())
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("GeoPoints");
        geoFire = new GeoFire(ref);
        mItemListAdapter = new ItemListAdapter(mDatabase.equalTo("GeoPoints"),  R.layout.auto_complete_items, getActivity());


        return rootView;
    }

    @Override
    public void onStart(){
        super.onStart();

        locationClient.connect();
        if (NetworkUtil.isOnline(getContext())){
            Toast.makeText(getContext(),"You are online!",Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(getContext(),"You are fucking offline",Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        mCallbackManager.onActivityResult(requestCode, resultCode, data);
    }


    @Override
    public void onStop(){
        // If the client is connected
        if (locationClient.isConnected()) {
            stopPeriodicUpdates();
        }
        // After disconnect() is called, the client is considered "dead".
        locationClient.disconnect();
        accessTokenTracker.stopTracking();
        profileTracker.stopTracking();
        super.onStop();
    }


    @Override
    public void onConnected(@Nullable Bundle bundle) {
        currentLocation = getLocation();
        startPeriodicUpdates();
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onLocationChanged(Location location) {
        currentLocation = location;
        if (lastLocation != null && lastLocation.distanceTo(currentLocation)< 0.5){
            Toast.makeText(getContext(),"You have moved, Bitch!",Toast.LENGTH_SHORT).show();
        }
        startGeoQuery();
    }

    public void submitPost(){
        final String interest = InterestText.getText().toString();
        final String likeToDo = LikeToDo.getText().toString();
        final String fbUserId = Profile.getCurrentProfile().getId();
        final Object timeStamp = ServerValue.TIMESTAMP;

        if (TextUtils.isEmpty(interest)) {
            InterestText.setError(REQUIRED);
            return;
        }
        setEditingEnabled(false);


        mDatabase.child("Interests").child(interest).addListenerForSingleValueEvent(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        FirebaseUser user = mAuth.getCurrentUser();

                        if (user == null) {

                            Toast.makeText(SearchFragment.this.getContext(),
                                    "Error: could not fetch user.",
                                    Toast.LENGTH_SHORT).show();
                        } else {

                            writeNewPost(fbUserId, likeToDo, interest, timeStamp);

                        }

                        setEditingEnabled(true);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Log.w(TAG, "getUser:onCancelled", databaseError.toException());
                        setEditingEnabled(true);
                    }
                });
    }

    public void writeNewPost(String facebookId, String likeToDo, String interest, Object timeStamp){

        Search search = new Search(facebookId, likeToDo, timeStamp);
        Map<String,Object> searchValues = search.toMap();

        Map<String, Object> childUpdates = new HashMap<>();

        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        childUpdates.put("/" + interest + "/" + userId,searchValues);

        Location myLoc = (currentLocation == null) ? lastLocation : currentLocation;
        geoFire.setLocation(interest, new GeoLocation(myLoc.getLatitude(),myLoc.getLongitude()));

        mDatabase.updateChildren(childUpdates);

    }


    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        if (connectionResult.hasResolution()) {
            try {

                // Start an Activity that tries to resolve the error
                connectionResult.startResolutionForResult(getActivity(), CONNECTION_FAILURE_RESOLUTION_REQUEST);

            } catch (IntentSender.SendIntentException e) {
                e.printStackTrace();
            }
        } else {
            // If no resolution is available, display a dialog to the user with the error.
            showErrorDialog(connectionResult.getErrorCode());
        }
    }

    private void showErrorDialog(int errorCode) {
        // Get the error dialog from Google Play services
        Dialog errorDialog =
                GooglePlayServicesUtil.getErrorDialog(errorCode, getActivity(),
                        CONNECTION_FAILURE_RESOLUTION_REQUEST);

        // If Google Play services can provide an error dialog
        if (errorDialog != null) {

            // Create a new DialogFragment in which to show the error dialog
            ErrorDialogFragment errorFragment = new ErrorDialogFragment();

            // Set the dialog in the DialogFragment
            errorFragment.setDialog(errorDialog);

            // Show the error dialog in the DialogFragment
            errorFragment.show(getFragmentManager(), TAG);
        }
    }

    private void setEditingEnabled(boolean enabled) {
        InterestText.setEnabled(enabled);
        LikeToDo.setEnabled(enabled);
        if (enabled) {
            SearchButton.setVisibility(View.VISIBLE);
        } else {
            SearchButton.setVisibility(View.GONE);
        }
    }


    private void startPeriodicUpdates() {
        if (ActivityCompat.checkSelfPermission(getContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getContext(), android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestLocationPermissions();
        } else {
            LocationServices.FusedLocationApi.requestLocationUpdates(
                    locationClient, locationRequest, this);
            center = new GeoLocation(currentLocation.getLatitude(), currentLocation.getLongitude());

            if (currentLocation!=null){
                startGeoQuery();
            }
        }

    }


    private void requestLocationPermissions() {
        // BEGIN_INCLUDE(contacts_permission_request)
        if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(),
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                || ActivityCompat.shouldShowRequestPermissionRationale(getActivity(),
                android.Manifest.permission.ACCESS_COARSE_LOCATION)) {

            // Provide an additional rationale to the user if the permission was not granted
            // and the user would benefit from additional context for the use of the permission.
            // For example, if the request has been denied previously.
            Toast.makeText(getContext(),"HEY DUMBFUCK ACCEPT THE LOCATION REQUEST!!",Toast.LENGTH_LONG).show();

            // Display a SnackBar with an explanation and a button to trigger the request.
            ActivityCompat
                    .requestPermissions(getActivity(), PERMISSIONS_LOCATION,
                            REQUEST_LOCATION);
        } else {
            // Contact permissions have not been granted yet. Request them directly.
            ActivityCompat.requestPermissions(getActivity(), PERMISSIONS_LOCATION, REQUEST_LOCATION);
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == REQUEST_LOCATION) {
            // BEGIN_INCLUDE(permission_result)
            // Received permission result for camera permission.
            Log.i(TAG, "Received response for Location permission request.");

            // Check if the only required permission has been granted
            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startPeriodicUpdates();
            }
            else {
                Toast.makeText(getContext(),"Location Permission Denied Idiot !!!",Toast.LENGTH_LONG).show();
            }
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    private void stopPeriodicUpdates() {
        locationClient.disconnect();
    }

    private Location getLocation() {
        // If Google Play Services is available
        if (servicesConnected()) {
            // Get the current location
            if (ActivityCompat.checkSelfPermission(getContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getContext(), android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                Log.i(TAG, "Location permissions has NOT been granted. Requesting permissions.");
            }
            return LocationServices.FusedLocationApi.getLastLocation(locationClient);
        } else {
            return null;
        }
    }

    public boolean servicesConnected() {
        // Check that Google Play services is available
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(getContext());

        // If Google Play services is available
        if (ConnectionResult.SUCCESS == resultCode) {
            // Continue
            return true;
            // Google Play services was not available for some reason
        } else {
            // Display an error dialog
            Dialog dialog = GooglePlayServicesUtil.getErrorDialog(resultCode, getActivity(), 0);
            if (dialog != null) {
                ErrorDialogFragment errorFragment = new ErrorDialogFragment();
                errorFragment.setDialog(dialog);
                errorFragment.show(getFragmentManager(), TAG);
            }
            return false;
        }
    }

    private void startGeoQuery(){
        query = geoFire.queryAtLocation(center, 1);
        final ArrayAdapter<String> autoComplete = new ArrayAdapter<>(getActivity(),android.R.layout.simple_selectable_list_item);
        query.addGeoQueryEventListener(new GeoQueryEventListener() {
            @Override
            public void onKeyEntered(final String key, GeoLocation location) {
                Log.d(TAG, "Key " + key + " entered the search area at [" + location.latitude + "," + location.longitude + "]");
                autoComplete.add(key);
            }

            @Override
            public void onKeyExited(String key) {
                autoComplete.remove(key);

            }

            @Override
            public void onKeyMoved(String key, GeoLocation location) {

            }

            @Override
            public void onGeoQueryReady() {

            }

            @Override
            public void onGeoQueryError(DatabaseError error) {

            }
        });
        InterestText = (AutoCompleteTextView) rootView.findViewById(R.id.autocomplete);
        InterestText.setAdapter(autoComplete);
        InterestText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                updatePostButtonState();
            }
        });
    }

    private String getPostEditTextText () {
        return InterestText.getText().toString().trim();
    }

    private void updatePostButtonState () {
        int length = getPostEditTextText().length();
        boolean enabled = length > 0 && length < Utils.getPostMaxCharacterCount();
        SearchButton.setEnabled(enabled);

        if (length == 0 || length > Utils.getPostMaxCharacterCount()){
            InterestText.setError("19 Characters Only");
        }
    }

}
