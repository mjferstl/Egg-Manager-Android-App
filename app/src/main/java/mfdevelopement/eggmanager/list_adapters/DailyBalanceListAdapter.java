package mfdevelopement.eggmanager.list_adapters;

import android.app.Activity;
import android.app.ActivityOptions;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
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

    public class DailyBalanceViewHolder extends RecyclerView.ViewHolder {
        private final TextView txtv_date, txtv_eggs_collected, txtv_eggs_sold, txtv_money_earned, txtv_money_earned_currency;
        private final ImageButton imgbtn_delete_item, imgbtn_edit_item;
        private final ImageView imgv_eggs_collected, imgv_eggs_sold;

        private DailyBalanceViewHolder(View itemView) {
            super(itemView);
            txtv_date = itemView.findViewById(R.id.txtv_date);
            txtv_eggs_collected = itemView.findViewById(R.id.txtv_recycler_item_eggs_collected);
            txtv_eggs_sold = itemView.findViewById(R.id.txtv_recycler_item_eggs_sold);
            txtv_money_earned = itemView.findViewById(R.id.txtv_earned_money);
            txtv_money_earned_currency = itemView.findViewById(R.id.txtv_earned_money_currency);
            imgbtn_delete_item = itemView.findViewById(R.id.imgbtn_delete);
            imgbtn_edit_item = itemView.findViewById(R.id.imgbtn_edit);
            imgv_eggs_collected = itemView.findViewById(R.id.imgv_eggs_collected);
            imgv_eggs_sold = itemView.findViewById(R.id.imgv_eggs_sold);
        }
    }

    public DailyBalanceListAdapter(Context context, SharedViewModel viewModel) {
        this.viewModel = viewModel;
        mInflater = LayoutInflater.from(context);
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

            // sortingOrderChangedListener for image button
            holder.imgbtn_delete_item.setOnClickListener(v -> new AlertDialog.Builder(v.getContext())
                    .setTitle("Eintrag löschen")
                    .setMessage("Möchten Sie den Eintrag löschen?")

                    // Specifying a sortingOrderChangedListener allows you to take an action before dismissing the dialog.
                    // The dialog is automatically dismissed when a dialog button is clicked.
                    .setPositiveButton(android.R.string.yes, (dialog, which) -> {
                        // Continue with delete operation
                        Log.d(LOG_TAG,"removing item at position " + position);
                        viewModel.delete(current);
                        notifyDataSetChanged();
                    })

                    // A null sortingOrderChangedListener allows the button to dismiss the dialog and take no further action.
                    .setNegativeButton(android.R.string.no, null)
                    .setIcon(R.drawable.ic_warning_black_24dp)
                    .show());

            holder.imgbtn_edit_item.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.d(LOG_TAG,"editing item at position " + position);

                    Context context = v.getContext();

                    Intent intent = new Intent(v.getContext(), NewEntityActivity.class);
                    intent.putExtra(DatabaseFragment.EXTRA_REQUEST_CODE_NAME, DatabaseFragment.EDIT_ENTITY_REQUEST_CODE);
                    intent.putExtra(DatabaseFragment.EXTRA_DAILY_BALANCE,current);

                    int numPairs = 1;
                    if (current.getEggsSold() != DailyBalance.NOT_SET) {numPairs = 2;}

                    Pair[] pairs = new Pair[numPairs];
                    pairs[0] = new Pair<View, String>(holder.imgv_eggs_collected,context.getString(R.string.transition_imgv_eggs_collected));
                    //pairs[1] = new Pair<View, String>(holder.txtv_eggs_collected,context.getString(R.string.transition_txtv_eggs_collected));

                    if (numPairs == 2) {
                        pairs[1] = new Pair<View, String>(holder.imgv_eggs_sold, context.getString(R.string.transition_imgv_eggs_sold));
                        //pairs[3] = new Pair<View, String>(holder.txtv_eggs_sold, context.getString(R.string.transition_txtv_eggs_sold));
                    }

                    ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation((Activity) context,pairs);
                    ((Activity) context).startActivityForResult(intent, DatabaseFragment.EDIT_ENTITY_REQUEST_CODE ,options.toBundle());
                }
            });
        } else {
            // Covers the case of data not being ready yet.
            holder.txtv_date.setText("Kein Datum");
        }
    }

    public void setDailyBalances(List<DailyBalance> dailyBalances){
        mDailyBalances = dailyBalances;
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
}
