package com.example.asypereddi.goingsomewhere;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

/**
 * RecyclerView's Adapter which binds each of the items with specifications as the user goes through
 * the list.
 * To Do: On Click feature to allow for access to the ItemActivity which is currently in progress.
 */

public class TripAdapter extends RecyclerView.Adapter<TripAdapter.TripViewHolder> {
    public static final String TRIP_KEY = "TRIP_KEY";
    public static final String TRIP_NAME = "TRIP_NAME";
    private final String TAG = "TRIP_ADAPTER";
    private int mNumberTrips;
    private List<Trip> mTrips;
    private List<String> mTripKeys;
    private FirebaseUtils firebaseUtils = new FirebaseUtils();

    public TripAdapter(List<Trip> trips, List<String> tripKeys) {
        mNumberTrips = trips.size();
        mTrips = trips;
        mTripKeys = tripKeys;
    }

    /**
     * Method to allow faster adding of items to a list of items.
     *
     * @param newTrip
     */
    public void addTrip(Trip newTrip, String newKey) {
        mTrips.add(newTrip);
        mTripKeys.add(newKey);
        mNumberTrips++;
    }

    @Override
    public TripViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        Context context = viewGroup.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(com.example.asypereddi.goingsomewhere.R.layout.trips_list_item, viewGroup, false);
        return new TripViewHolder(view);
    }

    /**
     * Calls the bind method on the specific position which is currently overwriting the specific
     * view holder.
     *
     * @param holder
     * @param position
     */
    @Override
    public void onBindViewHolder(final TripAdapter.TripViewHolder holder, int position) {
        final Trip trip = mTrips.get(position);
        final String tripKey = mTripKeys.get(position);
        holder.bind(trip.getName(), trip.getLocation(), trip.getDate());
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(v.getContext(), ItemActivity.class);
                intent.putExtra(TRIP_KEY, tripKey);
                Log.d(tripKey, "onClick:");
                intent.putExtra(TRIP_NAME, trip.getName());
                v.getContext().startActivity(intent);
            }
        });

        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                CharSequence options[] = new CharSequence[]{"Delete Trip", "Rename Trip", "Rename Date", "Rename Location"};
                AlertDialog.Builder builder = new AlertDialog.Builder(holder.itemView.getContext());
                builder.setItems(options, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (which == 0) {
                            firebaseUtils.deleteTrip(tripKey);
                        }
                    }
                });
                builder.show();
                return false;
            }
        });
    }

    @Override
    public int getItemCount() {
        return mNumberTrips;
    }

    /**
     * Removes trips from the adapter.
     */
    public void clearAdapter() {
        mNumberTrips = 0;
        mTrips.clear();
        mTripKeys.clear();
    }

    /**
     * Class which sets each view as the user scrolls through the recycler view using calls to
     * the bind command.
     */
    class TripViewHolder extends RecyclerView.ViewHolder {
        TextView tripNameTextView;
        TextView tripLocationTextView;
        TextView tripDateTextView;

        /**
         * Initializes the view holder for all the items which are being set in the view holder.
         *
         * @param tripView
         */
        public TripViewHolder(View tripView) {
            super(tripView);
            tripNameTextView = (TextView) tripView.findViewById(com.example.asypereddi.goingsomewhere.R.id.trip_name);
            tripLocationTextView = (TextView) tripView.findViewById(com.example.asypereddi.goingsomewhere.R.id.trip_location);
            tripDateTextView = (TextView) tripView.findViewById(com.example.asypereddi.goingsomewhere.R.id.trip_date);
        }

        /**
         * Sets all the information within the view holder.
         */
        void bind(String name, String location, String date) {
            tripNameTextView.setText(name);
            tripLocationTextView.setText(location);
            tripDateTextView.setText(date);
        }
    }
}

