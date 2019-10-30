package mfdevelopement.eggmanager;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class DailyBalanceListAdapter extends RecyclerView.Adapter<DailyBalanceListAdapter.DailyBalanceViewHolder> {

    private DailyBalanceViewModel viewModel;
    private final String LOG_TAG = "DailyBalanceListAdapter";
    private final SimpleDateFormat sdf = new SimpleDateFormat("EE, dd.MM.yyyy", Locale.getDefault());

    class DailyBalanceViewHolder extends RecyclerView.ViewHolder {
        private final TextView txtv_date, txtv_eggs_collected, txtv_eggs_sold, txtv_money_earned, txtv_money_earned_currency;
        private final ImageButton imgbtn_delete_item, imgbtn_edit_item;
        private final LinearLayout linearLayout;

        private DailyBalanceViewHolder(View itemView) {
            super(itemView);
            txtv_date = itemView.findViewById(R.id.txtv_date);
            txtv_eggs_collected = itemView.findViewById(R.id.txtv_recycler_item_eggs_collected);
            txtv_eggs_sold = itemView.findViewById(R.id.txtv_recycler_item_eggs_sold);
            txtv_money_earned = itemView.findViewById(R.id.txtv_earned_money);
            txtv_money_earned_currency = itemView.findViewById(R.id.txtv_earned_money_currency);
            imgbtn_delete_item = itemView.findViewById(R.id.imgbtn_delete);
            imgbtn_edit_item = itemView.findViewById(R.id.imgbtn_edit);
            linearLayout = itemView.findViewById(R.id.linLay_recycler_item);
        }
    }

    private final LayoutInflater mInflater;
    private List<DailyBalance> mDailyBalances; // Cached copy of words

    DailyBalanceListAdapter(Context context, DailyBalanceViewModel viewModel) {
        this.viewModel = viewModel;
        mInflater = LayoutInflater.from(context);
    }

    @Override
    @NonNull
    public DailyBalanceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = mInflater.inflate(R.layout.recyclerview_item, parent, false);
        return new DailyBalanceViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull final DailyBalanceViewHolder holder, final int position) {
        if (mDailyBalances != null) {
            final DailyBalance current = mDailyBalances.get(position);

            holder.txtv_date.setText(sdf.format(current.getDate()));
            holder.txtv_eggs_collected.setText(String.valueOf(current.getEggsCollected()));
            holder.txtv_eggs_sold.setText(String.valueOf(current.getEggsSold()));

            if (current.getMoneyEarned() == 0) {
                holder.txtv_money_earned.setVisibility(View.GONE);
                holder.txtv_money_earned_currency.setVisibility(View.GONE);
            } else {
                holder.txtv_money_earned.setText(String.format(Locale.getDefault(),"%.2f",current.getMoneyEarned()));
                holder.txtv_money_earned_currency.setVisibility(View.VISIBLE);
            }

            // listener for image button
            holder.imgbtn_delete_item.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    new AlertDialog.Builder(v.getContext())
                            .setTitle("Eintrag löschen")
                            .setMessage("Möchten Sie den Eintrag löschen?")

                            // Specifying a listener allows you to take an action before dismissing the dialog.
                            // The dialog is automatically dismissed when a dialog button is clicked.
                            .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    // Continue with delete operation
                                    Log.d(LOG_TAG,"removing item at position " + position);
                                    viewModel.delete(current);
                                    notifyDataSetChanged();
                                }
                            })

                            // A null listener allows the button to dismiss the dialog and take no further action.
                            .setNegativeButton(android.R.string.no, null)
                            .setIcon(R.drawable.ic_warning_black_24dp)
                            .show();

                }
            });

            holder.imgbtn_edit_item.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.d(LOG_TAG,"editing item at position " + position);
                    Intent intent = new Intent(v.getContext(), NewEntityActivity.class);
                    intent.putExtra(MainActivity.EXTRA_REQUEST_CODE_NAME,MainActivity.EDIT_ENTITY_ACTIVITY_REQUEST_CODE);
                    intent.putExtra("dd",current);
                    v.getContext().startActivity(intent);
                }
            });
        } else {
            // Covers the case of data not being ready yet.
            holder.txtv_date.setText("Kein Datum");
        }
    }

    void setWords(List<DailyBalance> dailyBalances){
        mDailyBalances = dailyBalances;
        notifyDataSetChanged();
    }

    // getItemCount() is called many times, and when it is first called,
    // mWords has not been updated (means initially, it's null, and we can't return null).
    @Override
    public int getItemCount() {
        if (mDailyBalances != null)
            return mDailyBalances.size();
        else return 0;
    }
}
