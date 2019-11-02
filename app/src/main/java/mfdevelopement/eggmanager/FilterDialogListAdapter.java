package mfdevelopement.eggmanager;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class FilterDialogListAdapter extends RecyclerView.Adapter<FilterDialogListAdapter.FilterDialogViewHolder> {

    private DailyBalanceViewModel viewModel;
    private final String LOG_TAG = "FilterDialogListAdapter";
    private List<String> filterList;
    private final LayoutInflater mInflater;

    class FilterDialogViewHolder extends RecyclerView.ViewHolder {
        private Button btn_year, btn_month;

        private FilterDialogViewHolder(View itemView) {
            super(itemView);
            btn_year = itemView.findViewById(R.id.txtv_recycler_item_filter_dialog_year);
            btn_month = itemView.findViewById(R.id.txtv_recycler_item_filter_dialog_month);
        }
    }

    FilterDialogListAdapter(Context context, DailyBalanceViewModel viewModel) {
        this.viewModel = viewModel;
        mInflater = LayoutInflater.from(context);
    }

    @Override
    @NonNull
    public FilterDialogViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = mInflater.inflate(R.layout.recyclerview_filter_dialog_item, parent, false);
        return new FilterDialogViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull final FilterDialogViewHolder holder, final int position) {
        if (filterList != null) {
            final String current = filterList.get(position);

            Log.d(LOG_TAG,"currentTextForRecyclerItem " + current);

            // if item is a year
            if (isNumeric(current)) {
                holder.btn_month.setVisibility(View.GONE);
                holder.btn_year.setVisibility(View.VISIBLE);
                holder.btn_year.setText(current);
            }
            // item is a month name
            else {
                holder.btn_month.setVisibility(View.VISIBLE);
                holder.btn_year.setVisibility(View.GONE);
                holder.btn_month.setText(current);

                // if the next item is another month --> change the background
                if (filterList.size() > position+1) {
                    String nextItem = filterList.get(position+1);
                    if (!isNumeric(nextItem)) { holder.btn_month.setBackgroundResource(R.drawable.recycler_item_middle_background); }
                }
            }
        } else {
            Log.e(LOG_TAG,"filterList = null");
        }
    }

    public void setFilterStrings(List<String> yearMonthFilterList) {
        filterList = yearMonthFilterList;
        notifyDataSetChanged();
        Log.d(LOG_TAG,"setFilterStrings::notifyDataSetChanged");
    }

    // getItemCount() is called many times, and when it is first called,
    // mWords has not been updated (means initially, it's null, and we can't return null).
    @Override
    public int getItemCount() {
        if (filterList != null)
            return filterList.size();
        else return 0;
    }

    private boolean isNumeric(String string) {
        try {
            Integer.valueOf(string);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}
