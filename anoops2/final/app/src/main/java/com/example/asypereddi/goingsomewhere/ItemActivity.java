package com.example.asypereddi.goingsomewhere;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
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
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

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
 * Activity which showcases the items in each specific Trip. Allows for users to add additional items
 * and people to the given trip.
 */
public class ItemActivity extends AppCompatActivity {
    private static final String TAG = "ItemActivity";
    ItemAdapter mItemAdapter;
    DatabaseReference mRootRef = FirebaseDatabase.getInstance().getReference();
    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    private String tripKey;
    private FirebaseUtils firebaseUtils = new FirebaseUtils();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(com.example.asypereddi.goingsomewhere.R.layout.activity_list);
        Intent intent = getIntent();
        tripKey = intent.getStringExtra(TripAdapter.TRIP_KEY);
        String tripTitle = intent.getStringExtra(TripAdapter.TRIP_NAME);
        this.setTitle(tripTitle); //Sets the title for the app
        RecyclerView itemListView = (RecyclerView) findViewById(R.id.personal_item_list);
        Button addItemButton = (Button) findViewById(R.id.add_item);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        itemListView.setLayoutManager(layoutManager);
        itemListView.setHasFixedSize(false);
        List<Item> items = new ArrayList<>();
        List<String> itemKeys = new ArrayList<>();
        mItemAdapter = new ItemAdapter(items, itemKeys, tripKey);
        itemListView.setAdapter(mItemAdapter);
        listenForItem();
        /**
         * Adds an additional item to the list and logs it into the firebase database under "items".
         */
        addItemButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final AlertDialog.Builder mBuilder = new AlertDialog.Builder(ItemActivity.this);
                View mView = getLayoutInflater().inflate(com.example.asypereddi.goingsomewhere.R.layout.dialog_additem, null);
                final EditText mItemName = (EditText) mView.findViewById(com.example.asypereddi.goingsomewhere.R.id.add_item_name);
                final Button mAddItem = (Button) mView.findViewById(com.example.asypereddi.goingsomewhere.R.id.add_item_btn);
                final TextView mItemImportanceLabel = (TextView) mView.findViewById(com.example.asypereddi.goingsomewhere.R.id.add_item_importance_label);
                mBuilder.setView(mView);
                final AlertDialog dialog = mBuilder.create();
                dialog.show();
                final SeekBar mItemImportance = (SeekBar) mView.findViewById(com.example.asypereddi.goingsomewhere.R.id.add_item_importance);
                /**
                 * Keeps track of the importance and displays it on a TextView which updates on any
                 * changes to the seekbar.
                 */
                mItemImportance.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                    @Override
                    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                        Log.d(TAG, "onProgressChanged: " + (progress + 1));
                        mItemImportanceLabel.setText(String.valueOf(progress + 1));
                    }

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {
                        Log.d(TAG, "Start Tracking");
                    }

                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {
                        Log.d(TAG, "End Tracking");
                    }
                });

                mAddItem.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View w) {
                        if (!mItemName.getText().toString().isEmpty()) {
                            String itemName = mItemName.getText().toString();
                            int itemImportance = mItemImportance.getProgress() + 1; //progress starts at 1 and ends at 3
                            firebaseUtils.addItem(itemName, itemImportance, tripKey);
                            dialog.dismiss();
                        } else {
                            //No input put into the EditText
                            mItemName.setError(getString(com.example.asypereddi.goingsomewhere.R.string.empty_edit_text));
                        }
                    }
                });

            }
        });
    }

    @Override
    protected void onStart() {
        listenForItem();
        super.onStart();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater mMenuInflater = getMenuInflater();
        mMenuInflater.inflate(R.menu.item_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        //Creates a dialog consisting of the users which can be added to the Trip and on selection
        // adds them to the trip.
        if (item.getItemId() == R.id.add_friend_setting) {
            final List<String> userIds = new ArrayList<>();
            final List<CharSequence> usernames = new ArrayList<>();
            mRootRef.child("users").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    Iterable<DataSnapshot> children = dataSnapshot.getChildren();
                    for (DataSnapshot child : children) {
                        String username = child.child("username").getValue(String.class);
                        if (!user.getUid().equals(child.getKey())) {
                            //displays all users who are not you
                            userIds.add(child.getKey());
                            usernames.add(username);
                        }
                    }
                    AlertDialog.Builder builder = new AlertDialog.Builder(ItemActivity.this);
                    Log.d(TAG, usernames.toString());
                    builder.setItems(usernames.toArray(new CharSequence[usernames.size()]),
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    firebaseUtils.addFriend(tripKey, userIds.get(which));
                                    Toast.makeText(getApplicationContext(), "Added " + usernames.get(which) +
                                            " to the Trip!", Toast.LENGTH_SHORT).show();
                                }
                            });
                    builder.show();
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Log.d(TAG, databaseError.toString());
                }
            });
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Checks if the item has been changed and on change updates the adapter with all items under
     * the given trip, assosiated by tripkey.
     */
    public void listenForItem() {
        DatabaseReference mItemRef = mRootRef.child("items").child(tripKey);
        mItemRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Iterable<DataSnapshot> children = dataSnapshot.getChildren();
                mItemAdapter.clearItems();
                //populates the recycler view with items from the specific trip
                for (DataSnapshot child : children) {
                    Item item = child.getValue(Item.class);
                    String itemKey = child.getKey();
                    mItemAdapter.addItem(item, itemKey);
                }
                mItemAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d(TAG, databaseError.toString());
            }
        });
    }
}
