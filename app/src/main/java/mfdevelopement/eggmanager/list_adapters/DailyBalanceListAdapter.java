package mfdevelopement.eggmanager.list_adapters;

import android.app.Activity;
import android.app.ActivityOptions;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.util.Pair;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
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
import mfdevelopement.eggmanager.activities.NewEntityActivity;
import mfdevelopement.eggmanager.data_models.DailyBalance;
import mfdevelopement.eggmanager.fragments.DatabaseFragment;
import mfdevelopement.eggmanager.viewmodels.SharedViewModel;

public class DailyBalanceListAdapter extends RecyclerView.Adapter<DailyBalanceListAdapter.DailyBalanceViewHolder> {

    private SharedViewModel viewModel;
    private final String LOG_TAG = "DailyBalanceListAdapter";
    private final SimpleDateFormat sdf = new SimpleDateFormat("EE, dd.MM.yyyy", Locale.getDefault());
    private final LayoutInflater mInflater;
    private List<DailyBalance> mDailyBalances; // Cached copy of words
    private Context context;
    private ActionMode actionMode;
    private List<Boolean> selectedItemPosition = new ArrayList<>();

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
                Log.d(LOG_TAG,"long click on item " + getAdapterPosition());
                
                // Start the CAB using the ActionMode.Callback defined above
                if (!itemView.isSelected()) {
                    initSelectedItemPositions();
                    setItemSelected(getAdapterPosition());
                    notifyDataSetChanged();
                    Log.d(LOG_TAG,"item " + getAdapterPosition() + " is not selected yet and will be selected now");
                    actionMode = ((Activity) context).startActionMode(actionModeCallback);
                    itemView.setSelected(true); // set the clicked item to "selected"
                    Log.d(LOG_TAG,"action mode started for item at position " + getAdapterPosition());
                } else {
                    Log.d(LOG_TAG,"selected item has been selected before");
                    initSelectedItemPositions();
                    notifyDataSetChanged();
                    if (actionMode != null)
                        actionMode.finish();
                    itemView.setSelected(false);
                    Log.d(LOG_TAG,"action mode is finished");
                }

                return true;
            });
        }
    }

    public DailyBalanceListAdapter(Context context, SharedViewModel viewModel) {
        this.viewModel = viewModel;
        this.context = context;
        mInflater = LayoutInflater.from(context);
        initSelectedItemPositions();
    }

    @Override
    @NonNull
    public DailyBalanceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = mInflater.inflate(R.layout.recyclerview_item_database_item, parent, false);
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
                holder.txtv_money_earned.setText(String.format(Locale.getDefault(),"%.2f",current.getMoneyEarned()));
                holder.txtv_money_earned.setVisibility(View.VISIBLE);
                holder.txtv_money_earned_currency.setVisibility(View.VISIBLE);
            } else {
                holder.txtv_money_earned.setText("");
                holder.txtv_money_earned.setVisibility(View.GONE);
                holder.txtv_money_earned_currency.setVisibility(View.GONE);
            }
        } else {
            // Covers the case of data not being ready yet.
            holder.txtv_date.setText("Kein Datum");
        }
    }

    private void showDeleteDialog(int itemPosition) {
        new AlertDialog.Builder(this.context)
                .setTitle("Eintrag löschen")
                .setMessage("Möchten Sie den Eintrag löschen?")

                // Specifying a sortingOrderChangedListener allows you to take an action before dismissing the dialog.
                // The dialog is automatically dismissed when a dialog button is clicked.
                .setPositiveButton(android.R.string.yes, (dialog, which) -> {
                    // Continue with delete operation
                    Log.d(LOG_TAG,"removing item at position " + itemPosition);
                    viewModel.delete(getItem(itemPosition));
                    notifyDataSetChanged();
                })

                // A null sortingOrderChangedListener allows the button to dismiss the dialog and take no further action.
                .setNegativeButton(android.R.string.no, null)
                .setIcon(R.drawable.ic_warning_black_24dp)
                .show();
    }

    private void initSelectedItemPositions() {
        this.selectedItemPosition.clear();
        if (this.mDailyBalances != null) {
            for (int i=0; i<this.mDailyBalances.size(); i++)
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

    private void editEntity(int itemPosition) { editEntity(itemPosition, null);}

    private void editEntity(int itemPosition, DailyBalanceViewHolder holder) {
        Log.d(LOG_TAG,"editing item at position " + itemPosition);

        Context context = this.context;

        Intent intent = new Intent(this.context, NewEntityActivity.class);
        intent.putExtra(DatabaseFragment.EXTRA_REQUEST_CODE_NAME, DatabaseFragment.EDIT_ENTITY_REQUEST_CODE);
        intent.putExtra(DatabaseFragment.EXTRA_DAILY_BALANCE,getItem(itemPosition));

        int numPairs = 1;
        if (getItem(itemPosition).getEggsSold() != DailyBalance.NOT_SET) {numPairs = 2;}

        if (holder != null) {
            Pair[] pairs = new Pair[numPairs];
            pairs[0] = new Pair<View, String>(holder.imgv_eggs_collected, context.getString(R.string.transition_imgv_eggs_collected));
            if (numPairs == 2)
                pairs[1] = new Pair<View, String>(holder.imgv_eggs_sold, context.getString(R.string.transition_imgv_eggs_sold));

            ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation((Activity) context, pairs);
            ((Activity) context).startActivityForResult(intent, DatabaseFragment.EDIT_ENTITY_REQUEST_CODE, options.toBundle());
        } else {
            ((Activity) context).startActivityForResult(intent, DatabaseFragment.EDIT_ENTITY_REQUEST_CODE);
        }
    }

    public void setDailyBalances(List<DailyBalance> dailyBalances){
        mDailyBalances = dailyBalances;
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

    public List<DailyBalance> getItems() {
        return mDailyBalances;
    }

    private DailyBalance getItem(int index) {
        return mDailyBalances.get(index);
    }

    private ActionMode.Callback actionModeCallback = new ActionMode.Callback() {

        // Called when the action mode is created; startActionMode() was called
        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            // Inflate a menu resource providing context menu items
            MenuInflater inflater = mode.getMenuInflater();
            inflater.inflate(R.menu.context_menu_database_entry, menu);
            return true;
        }

        // Called each time the action mode is shown. Always called after onCreateActionMode, but
        // may be called multiple times if the mode is invalidated.
        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return false; // Return false if nothing is done
        }

        // Called when the user selects a contextual menu item
        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            int indexSelectedItem = selectedItemPosition.indexOf(true);
            switch (item.getItemId()) {
                case R.id.action_edit:
                    Log.d(LOG_TAG,"user wants to edit the selected item at position " + indexSelectedItem);
                    editEntity(indexSelectedItem);
                    mode.finish(); // Action picked, so close the CAB
                    return true;
                case R.id.action_delete:
                    Log.d(LOG_TAG,"user wants to delete the selected item at position " + indexSelectedItem);
                    showDeleteDialog(indexSelectedItem);
                    mode.finish(); // Action picked, so close the CAB
                    return true;
                default:
                    return false;
            }
        }

        // Called when the user exits the action mode
        @Override
        public void onDestroyActionMode(ActionMode mode) {
            actionMode = null;
            Log.d(LOG_TAG,"action mode destroyed");
        }
    };
}
