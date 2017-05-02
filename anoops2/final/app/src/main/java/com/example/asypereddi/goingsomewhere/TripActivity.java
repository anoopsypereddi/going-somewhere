package com.example.asypereddi.goingsomewhere;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

/**
 * Activity user is sent to after log-in, contains a recycler view of all of the users trips loaded
 * from the Firebase database. Within this view the user may add Trips to their list.
 * <p>
 * TO-DO: Implement Clicking on each Trip and long clicking to access additional options.
 */
public class TripActivity extends AppCompatActivity {
    String TAG = "TripActivity";
    RecyclerView tripView;
    TripAdapter mTripAdapter;
    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    FirebaseUtils firebaseUtils = new FirebaseUtils();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        checkUserValidity();
        super.onCreate(savedInstanceState);
        setContentView(com.example.asypereddi.goingsomewhere.R.layout.activity_trip);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        tripView = (RecyclerView) findViewById(com.example.asypereddi.goingsomewhere.R.id.trips_list);
        tripView.setLayoutManager(layoutManager);
        tripView.setHasFixedSize(false);
        List<Trip> trips = new ArrayList<>();
        List<String> tripKeys = new ArrayList<>();
        mTripAdapter = new TripAdapter(trips, tripKeys);
        tripView.setAdapter(mTripAdapter);
        FloatingActionButton fab = (FloatingActionButton) findViewById(com.example.asypereddi.goingsomewhere.R.id.add_trip);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Pop up dialog to allow user to add a new trip
                AlertDialog.Builder mBuilder = new AlertDialog.Builder(TripActivity.this);
                View mView = getLayoutInflater().inflate(com.example.asypereddi.goingsomewhere.R.layout.dialog_addtrip, null);
                mBuilder.setView(mView);
                final EditText tripNameEditText = (EditText) mView.findViewById(com.example.asypereddi.goingsomewhere.R.id.add_trip_name);
                final EditText tripDateEditText = (EditText) mView.findViewById(com.example.asypereddi.goingsomewhere.R.id.add_trip_date);
                final EditText tripLocationEditText = (EditText) mView.findViewById(com.example.asypereddi.goingsomewhere.R.id.add_trip_location);
                final Button tripAddButton = (Button) mView.findViewById(com.example.asypereddi.goingsomewhere.R.id.add_trip_btn);
                final AlertDialog alertDialog = mBuilder.create();
                alertDialog.show();
                tripAddButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (!isTripParameterEmpty()) {
                            //Create a new trip in the Firebase Database
                            String name = tripNameEditText.getText().toString();
                            String date = tripDateEditText.getText().toString();
                            String location = tripLocationEditText.getText().toString();
                            firebaseUtils.addTrip(name, date, location);
                            alertDialog.dismiss();
                        }
                    }

                    /**
                     * Checks if any of the parameters are empty and if so sets the default empty
                     * edit text error.
                     * @return are any of the parameters empty
                     */
                    private boolean isTripParameterEmpty() {
                        if (tripNameEditText.getText().toString().isEmpty()) {
                            tripNameEditText.setError(getString(com.example.asypereddi.goingsomewhere.R.string.empty_edit_text));
                            return true;
                        } else if (tripLocationEditText.getText().toString().isEmpty()) {
                            tripLocationEditText.setError(getString(com.example.asypereddi.goingsomewhere.R.string.empty_edit_text));
                            return true;
                        } else if (tripDateEditText.getText().toString().isEmpty()) {
                            tripDateEditText.setError(getString(com.example.asypereddi.goingsomewhere.R.string.empty_edit_text));
                            return true;
                        }
                        return false;
                    }
                });
            }
        });
    }

    /**
     * On starting up app updates user's interface with most recent trips and changes to the
     * database. Then loads the Trips specific to the given user.
     */
    @Override
    protected void onStart() {
        listenForTrip();
        super.onStart();
    }

    /**
     * If user is not authenticated, redirects the user to the login screen.
     */
    public void checkUserValidity() {
        if (user == null) {
            //If user is not authenticated
            Intent intent = new Intent(this, FacebookLoginActivity.class);
            startActivity(intent);
            finish();
        }
    }

    /**
     * Creates the menu which contains the sign-out option.
     *
     * @param menu
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater mMenuInflater = getMenuInflater();
        mMenuInflater.inflate(com.example.asypereddi.goingsomewhere.R.menu.trip_menu, menu);
        return true;
    }

    /**
     * Allows the users to select specific options:
     * > Sign Out: Logs Out of the user and returns the user to the Login Page.
     *
     * @param item
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == com.example.asypereddi.goingsomewhere.R.id.sign_out_setting) {
            FirebaseAuth.getInstance().signOut();
            startActivity(new Intent(getApplicationContext(), FacebookLoginActivity.class));
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Checks for modifications to the "trips" in the firebase database and on change runs through
     * and adds the ones of the specific user to the recyclerview.
     */
    private void listenForTrip() {
        checkUserValidity();
        DatabaseReference mRootRef = FirebaseDatabase.getInstance().getReference();
        DatabaseReference mTripRef = mRootRef.child("trips");
        mTripRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Iterable<DataSnapshot> children = dataSnapshot.getChildren();
                mTripAdapter.clearAdapter();
                for (DataSnapshot child : children) {
                    String tripKey = child.getKey();
                    Trip trip = child.getValue(Trip.class);
                    if (trip.getUserIds().contains(user.getUid())) {
                        //adds only the user's id
                        mTripAdapter.addTrip(trip, tripKey);
                    }
                }
                mTripAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d(TAG, databaseError.toString());
            }
        });
    }
}
