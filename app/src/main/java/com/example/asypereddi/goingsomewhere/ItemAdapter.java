package com.example.asypereddi.goingsomewhere;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

/**
 * RecyclerView's Adapter which binds each of the items with specifications regarding the item objects
 * display in each individual tab.
 */
public class ItemAdapter extends RecyclerView.Adapter<ItemAdapter.ItemViewHolder> {
    private int mNumberItems;
    private List<Item> mItems;
    private List<String> mItemKeys;
    private String mTripKey;
    final private String colorImportanceOne = "#ffe082";
    final private String colorImportanceTwo = "#ffca28";
    final private String colorImportanceThree = "#ffa000";
    FirebaseUtils firebaseUtils = new FirebaseUtils();

    public ItemAdapter(List<Item> items, List<String> itemKeys, String tripKey) {
        mNumberItems = items.size();
        mItemKeys = itemKeys;
        mItems = items;
        mTripKey = tripKey;
    }

    /**
     * Method to allow faster adding of items to a list of items.
     * @param newItem
     */
    public void addItem(Item newItem, String newKey) {
        mItems.add(newItem);
        mItemKeys.add(newKey);
        mNumberItems++;
    }

    public void clearItems() {
        mItems.clear();
        mNumberItems = 0;
        mItemKeys.clear();
    }

    @Override
    public ItemViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        Context context = viewGroup.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(com.example.asypereddi.goingsomewhere.R.layout.items_list_item, viewGroup, false);
        return new ItemViewHolder(view);
    }

    /**
     * Calls the bind method on the specific position which is currently overwriting the specific
     * view holder.
     * @param holder
     * @param position
     */
    @Override
    public void onBindViewHolder(final ItemAdapter.ItemViewHolder holder, int position) {
        final Item item = mItems.get(position);
        final String itemKey = mItemKeys.get(position);
        String name = item.getName();
        boolean packed = item.isPacked();
        int importance = item.getImportance();
        int color = 0;
        switch (importance) {
            case 1:
                color = Color.parseColor(colorImportanceOne);
                break;
            case 2:
                color = Color.parseColor(colorImportanceTwo);
                break;
            case 3:
                color = Color.parseColor(colorImportanceThree);
                break;
        }
        holder.itemView.setBackgroundColor(color);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            //Checks off that the item has been found
            @Override
            public void onClick(View v) {
                firebaseUtils.setAsPacked(itemKey, mTripKey);
            }
        });

        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                //Opens up a dialog which allows a user to delete an item
                CharSequence options[] = new CharSequence[]{"Delete Item"};
                AlertDialog.Builder builder = new AlertDialog.Builder(holder.itemView.getContext());
                builder.setItems(options, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (which == 0) {
                            firebaseUtils.deleteItem(itemKey, mTripKey);
                        }
                    }
                });
                builder.show();
                return false;
            }
        });
        holder.bind(name, packed);
    }
    /**
     * The number of items within the recycler view. AKA the number of items within the list.
     * @return number of items in the list
     */
    @Override
    public int getItemCount() {
        return mNumberItems;
    }

    /**
     * Class which sets each view as the user scrolls through the recycler view using calls to
     * the bind command.
     */
    class ItemViewHolder extends RecyclerView.ViewHolder {
        TextView nameTextView;
        ImageView packedImageView;

        /**
         * Initializes the view holder for all the items which are being set in the view holder.
         * @param itemView
         */
        public ItemViewHolder(View itemView) {
            super(itemView);
            nameTextView = (TextView) itemView.findViewById(R.id.item_name);
            packedImageView = (ImageView) itemView.findViewById(R.id.item_packed);
        }

        /**
         * Sets all the information within the view holder including the text and then if the item
         * has been packed makes the imageView visible.
         * @param name
         */
        void bind(String name, boolean packed) {
            nameTextView.setText(name);
            if (packed) {
                packedImageView.setVisibility(View.VISIBLE);
            }
            else {
                packedImageView.setVisibility(View.INVISIBLE);
            }
        }
    }
}
