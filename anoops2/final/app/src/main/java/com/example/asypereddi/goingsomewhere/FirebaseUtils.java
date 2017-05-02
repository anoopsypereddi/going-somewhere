package com.example.asypereddi.goingsomewhere;

import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

/**
 * Completes all firebase operations including adding, deleting, and modifying references.
 */

public class FirebaseUtils {
    final private DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference();
    final private DatabaseReference tripRef = rootRef.child("trips");
    final private DatabaseReference itemRef = rootRef.child("items");
    final private DatabaseReference userRef = rootRef.child("users");
    final private FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    final private String TAG = "FirebaseUtils";

    /**
     * Creates a new trip and stores the trip within the database.
     *
     * @param name
     * @param date
     * @param location
     */
    public void addTrip(String name, String date, String location) {
        DatabaseReference mNewTripRef = tripRef.push();
        Trip newTrip = new Trip(name, date, location, user.getUid());
        mNewTripRef.setValue(newTrip);
    }

    /**
     * Creates the new Item object and adds it into the firebase database under the specific trip's
     * key in the tab for "items".
     *
     * @param newItemName
     * @param newItemImportance
     */
    public void addItem(String newItemName, int newItemImportance, String tripKey) {
        DatabaseReference tripItemRef = itemRef.child(tripKey);
        DatabaseReference newItemRef = tripItemRef.push();
        newItemRef.setValue(new Item(newItemName, newItemImportance));
    }

    /**
     * Add user to the firebase database.
     *
     * @param uid
     * @param email
     * @param username
     */
    public void addUser(String uid, String username, String email) {
        DatabaseReference newUserRef = userRef.child(uid);
        newUserRef.child("username").setValue(username);
        newUserRef.child("email").setValue(email);
    }

    /**
     * Deletes the trip and its items from the firebase database if the user is the only user invited
     * to the trip, otherwise removes the trip only for the specific user who called the delete function.
     *
     * @param tripKey
     */
    public void deleteTrip(final String tripKey) {
        tripRef.child(tripKey).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Trip trip = dataSnapshot.getValue(Trip.class);
                if (trip.getUserIds().size() == 1) {
                    tripRef.child(tripKey).removeValue();
                    itemRef.child(tripKey).removeValue();
                } else {
                    trip.removeUserId(user.getUid());
                    tripRef.child(tripKey).setValue(trip);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    /**
     * If the friend can be added and is added, the function adds them into the trip.
     *
     * @param tripKey
     * @param user
     */
    public void addFriend(String tripKey, final String user) {
        final DatabaseReference currentTripRef = tripRef.child(tripKey);
        currentTripRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Trip trip = dataSnapshot.getValue(Trip.class);
                if (!trip.getUserIds().contains(user)) {
                    trip.getUserIds().add(user);
                    currentTripRef.setValue(trip);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d(TAG, databaseError.toString());
            }
        });
    }

    /**
     * Deletes the specific item from the trip.
     *
     * @param itemKey
     * @param tripKey
     */
    public void deleteItem(String itemKey, String tripKey) {
        itemRef.child(tripKey).child(itemKey).removeValue();
    }

    /**
     * Toggles the item from packed to unpacked or vice-versa.
     *
     * @param itemKey
     * @param tripKey
     */
    public void setAsPacked(String itemKey, String tripKey) {
        final DatabaseReference specificItemRef = itemRef.child(tripKey).child(itemKey);
        specificItemRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Item item = dataSnapshot.getValue(Item.class);
                item.setPacked(!item.isPacked());
                specificItemRef.setValue(item);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d(TAG, databaseError.toString());
            }
        });
    }
}
