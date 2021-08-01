package mfdevelopement.eggmanager.list_adapters;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.util.Log;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import mfdevelopement.eggmanager.R;
import mfdevelopement.eggmanager.data_models.daily_balance.DailyBalance;

public class DailyBalanceListAdapter extends RecyclerView.Adapter<DailyBalanceListAdapter.DailyBalanceViewHolder> {

    private final String LOG_TAG = "DailyBalanceListAdapter";
    private final SimpleDateFormat sdf = new SimpleDateFormat("EE, dd.MM.yyyy", Locale.getDefault());
    private final Context context;
    private final List<Boolean> selectedItemPosition = new ArrayList<>();
    private final List<OnItemActionClickListener> onItemActionClickListeners = new ArrayList<>();
    private List<DailyBalance> mDailyBalances;
    private ActionMode actionMode;
    private final ActionMode.Callback actionModeCallback = new DatabaseActionModeCallback() {

        // Called when the user selects a contextual menu item
        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            int indexSelectedItem = selectedItemPosition.indexOf(true);
            int itemId = item.getItemId();
            if (itemId == R.id.action_edit) {
                Log.d(LOG_TAG, "user wants to edit the selected item at position " + indexSelectedItem);
                editEntity(indexSelectedItem);
                mode.finish(); // Action picked, so close the CAB
                return true;
            } else if (itemId == R.id.action_delete) {
                Log.d(LOG_TAG, "user wants to delete the selected item at position " + indexSelectedItem);
                showDeleteDialog(indexSelectedItem);
                mode.finish(); // Action picked, so close the CAB
                return true;
            }
            return false;
        }

        // Called when the user exits the action mode
        @Override
        public void onDestroyActionMode(ActionMode mode) {
            // remove the selections
            setItemsUnselected();
            actionMode = null;
            Log.d(LOG_TAG, "action mode destroyed");
        }
    };

    public DailyBalanceListAdapter(Context context) {
        this.context = context;
        initSelectedItemPositions();
    }

    @Override
    @NonNull
    public DailyBalanceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.recyclerview_item_database_item, parent, false);
        return new DailyBalanceViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull final DailyBalanceViewHolder holder, final int position) {
        if (mDailyBalances != null) {
            final DailyBalance current = mDailyBalances.get(position);

            if (this.selectedItemPosition.size() > 0)
                holder.itemView.setSelected(this.selectedItemPosition.get(position));
            else
                holder.itemView.setSelected(false);

            holder.txtv_date.setText(sdf.format(current.getDate()));

            if (current.getEggsCollected() != DailyBalance.NOT_SET)
                holder.txtv_eggs_collected.setText(String.valueOf(current.getEggsCollected()));
            else
                holder.txtv_eggs_collected.setText("0");

            if (current.getEggsSold() != DailyBalance.NOT_SET) {
                holder.txtv_eggs_sold.setText(String.valueOf(current.getEggsSold()));
                holder.imgv_eggs_sold.setVisibility(View.VISIBLE);
            } else {
                holder.txtv_eggs_sold.setText("");
                holder.imgv_eggs_sold.setVisibility(View.GONE);
            }

            if (current.getMoneyEarned() > 0) {
                holder.txtv_money_earned.setText(String.format(Locale.getDefault(), "%.2f", current.getMoneyEarned()));
                holder.txtv_money_earned.setVisibility(View.VISIBLE);
                holder.txtv_money_earned_currency.setVisibility(View.VISIBLE);
            } else {
                holder.txtv_money_earned.setText("");
                holder.txtv_money_earned.setVisibility(View.GONE);
                holder.txtv_money_earned_currency.setVisibility(View.GONE);
            }
        } else {
            // Covers the case of data not being ready yet.
            holder.txtv_date.setText(context.getString(R.string.text_no_date));
        }
    }

    private void showDeleteDialog(int itemPosition) {
        new AlertDialog.Builder(this.context)
                .setTitle(context.getString(R.string.dialog_title_delete_item))
                .setMessage(context.getString(R.string.dialog_message_delete_item))

                // Specifying a sortingOrderChangedListener allows you to take an action before dismissing the dialog.
                // The dialog is automatically dismissed when a dialog button is clicked.
                .setPositiveButton(android.R.string.ok, (dialog, which) -> {
                    // call the method of all listeners until one handles it
                    for (OnItemActionClickListener listener : onItemActionClickListeners) {
                        boolean handled = listener.onDeleteClicked(itemPosition);
                        if (handled) break;
                    }

                    // dataset could have changed
                    notifyDataSetChanged();
                })

                // A null sortingOrderChangedListener allows the button to dismiss the dialog and take no further action.
                .setNegativeButton(android.R.string.cancel, null)
                .setIcon(R.drawable.ic_warning_black_24dp)
                .show();
    }

    private void initSelectedItemPositions() {
        this.selectedItemPosition.clear();
        if (this.mDailyBalances != null) {
            for (int i = 0; i < this.mDailyBalances.size(); i++)
                this.selectedItemPosition.add(false);
        }
    }

    private void setItemSelected(int itemPosition) {
        if (this.mDailyBalances != null) {
            if (itemPosition >= 0 && itemPosition < this.mDailyBalances.size()) {
                initSelectedItemPositions();
                this.selectedItemPosition.set(itemPosition, true);
            }
        }
    }

    private void editEntity(int itemPosition) {
        Log.d(LOG_TAG, "editing item at position " + itemPosition);

        for (OnItemActionClickListener listener : onItemActionClickListeners) {
            boolean handled = listener.onEditClicked(itemPosition);
            if (handled) break;
        }
    }

    public void setDailyBalances(List<DailyBalance> dailyBalances) {
        mDailyBalances = dailyBalances;
        initSelectedItemPositions();
        notifyDataSetChanged();
    }

    public void setItemsUnselected() {
        initSelectedItemPositions();
        notifyDataSetChanged();
    }

    // getItemCount() is called many times, and when it is first called,
    // mWords has not been updated (means initially, it's null, and we can't return null).
    @Override
    public int getItemCount() {
        if (mDailyBalances != null)
            return mDailyBalances.size();
        else
            return 0;
    }

    /**
     * Get all items, which the adapter holds
     *
     * @return list of DailyBalance objects
     */
    public List<DailyBalance> getItems() {
        return mDailyBalances;
    }

    /**
     * Get the object of an item at a specified adapter position
     *
     * @param index position of the item in the adapter
     * @return DailyBalance object which contains the data of this item
     */
    public DailyBalance getItem(int index) {
        return mDailyBalances.get(index);
    }

    /**
     * Add an OnItemActionClickListener to handle actions on the items of the adapter
     *
     * @param onItemClickListener object implementing OnItemActionClickListener
     */
    public void addOnItemActionClickListener(@NonNull OnItemActionClickListener onItemClickListener) {
        this.onItemActionClickListeners.add(onItemClickListener);
    }

    /**
     * Interface for handling actions on the items of the {RecyclerView.Adapter}
     */
    public interface OnItemActionClickListener {
        /**
         * Method which gets called, when the edit button of the action menu for an item has been clicked
         *
         * @param position position of the item in the adapter
         * @return true, if the action has been handled, false otherwise. If returned true, no other listener will be called.
         */
        boolean onEditClicked(final int position);

        /**
         * Method which gets called, when the delete button of the action menu for an item has been clicked
         *
         * @param position position of the item in the adapter
         * @return true, if the action has been handled, false otherwise. If returned true, no other listener will be called.
         */
        boolean onDeleteClicked(final int position);
    }

    public class DailyBalanceViewHolder extends RecyclerView.ViewHolder {
        private final TextView txtv_date, txtv_eggs_collected, txtv_eggs_sold, txtv_money_earned, txtv_money_earned_currency;
        private final ImageView imgv_eggs_collected, imgv_eggs_sold;

        private DailyBalanceViewHolder(View itemView) {
            super(itemView);
            txtv_date = itemView.findViewById(R.id.txtv_date);
            txtv_eggs_collected = itemView.findViewById(R.id.txtv_recycler_item_eggs_collected);
            txtv_eggs_sold = itemView.findViewById(R.id.txtv_recycler_item_eggs_sold);
            txtv_money_earned = itemView.findViewById(R.id.txtv_earned_money);
            txtv_money_earned_currency = itemView.findViewById(R.id.txtv_earned_money_currency);
            imgv_eggs_collected = itemView.findViewById(R.id.imgv_eggs_collected);
            imgv_eggs_sold = itemView.findViewById(R.id.imgv_eggs_sold);


            itemView.setOnLongClickListener(v -> {
                Log.d(LOG_TAG, "long click on item " + getAdapterPosition());

                // Start the CAB using the ActionMode.Callback defined above
                if (!itemView.isSelected()) {
                    initSelectedItemPositions();
                    setItemSelected(getAdapterPosition());
                    notifyDataSetChanged();
                    Log.d(LOG_TAG, "item " + getAdapterPosition() + " is not selected yet and will be selected now");
                    actionMode = ((Activity) context).startActionMode(actionModeCallback);
                    itemView.setSelected(true); // set the clicked item to "selected"
                    Log.d(LOG_TAG, "action mode started for item at position " + getAdapterPosition());
                } else {
                    Log.d(LOG_TAG, "selected item has been selected before");
                    initSelectedItemPositions();
                    notifyDataSetChanged();
                    if (actionMode != null)
                        actionMode.finish();
                    itemView.setSelected(false);
                    Log.d(LOG_TAG, "action mode is finished");
                }

                return true;
            });
        }
    }
}
