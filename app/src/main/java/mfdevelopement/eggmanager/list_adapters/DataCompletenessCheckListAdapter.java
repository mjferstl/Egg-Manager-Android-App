package mfdevelopement.eggmanager.list_adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

import mfdevelopement.eggmanager.R;
import mfdevelopement.eggmanager.data_models.DataCheckMonthly;

public class DataCompletenessCheckListAdapter extends RecyclerView.Adapter<DataCompletenessCheckListAdapter.DataCompletenessCheckViewHolder> {

    private final String LOG_TAG = "DataCompletenessCheckLi";
    private List<DataCheckMonthly> data;
    private final LayoutInflater mInflater;
    private Context context;

    public class DataCompletenessCheckViewHolder extends RecyclerView.ViewHolder{

        private TextView txtv_month_name;
        private ImageView imgv_icon;

        private DataCompletenessCheckViewHolder(View itemView) {
            super(itemView);

            txtv_month_name = itemView.findViewById(R.id.txtv_recycler_item_completeness_check_name);
            imgv_icon = itemView.findViewById(R.id.imgv_recycler_item_completeness_check_icon);
        }
    }

    public DataCompletenessCheckListAdapter(Context context) {
        this.context = context;
        mInflater = LayoutInflater.from(context);
    }

    @Override
    @NonNull
    public DataCompletenessCheckViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = mInflater.inflate(R.layout.recyclerview_item_completeness_check, parent, false);
        return new DataCompletenessCheckViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull final DataCompletenessCheckViewHolder holder, final int position) {

        DataCheckMonthly monthyCheck = data.get(position);

        holder.txtv_month_name.setText(getFormattedMonthName(monthyCheck.getDateKeyMonth()));
        if (monthyCheck.isComplete())
            holder.imgv_icon.setImageDrawable(this.context.getDrawable(R.drawable.ic_check_black_24dp));
        else {
            holder.imgv_icon.setImageDrawable(this.context.getDrawable(R.drawable.ic_delete_black_24dp));
        }
    }

    /**
     * Format a date in the syntax of "yyyyMM" to a date in the format "MMMM yyyy"
     * Example: "201910" gets formatted to "October 2019"
     * @param dateKeyYearMonth date in the format: yyyyMM
     * @return
     */
    private String getFormattedMonthName(String dateKeyYearMonth) {
        Log.d(LOG_TAG,"curr date key to be formatted: " + dateKeyYearMonth);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMM", Locale.getDefault());
        SimpleDateFormat sdf_result = new SimpleDateFormat("MMMM yyyy", Locale.getDefault());

        String formattedDate;
        try {
            formattedDate = sdf_result.format(sdf.parse(dateKeyYearMonth));
        } catch (ParseException e) {
            e.printStackTrace();
            formattedDate = dateKeyYearMonth;
        }
        return formattedDate;
    }

    /**
     * Set the tobe shown in the Recycler View
     * @param dataCheckMonthlyList List<DataCheckMonthly>
     */
    public void setData(List<DataCheckMonthly> dataCheckMonthlyList) {
        this.data = dataCheckMonthlyList;
        notifyDataSetChanged();
    }

    /**
     * Get a item of the recycler view
     * @param index position of the item, starting at 0
     * @return DataCheckMonthly
     */
    public DataCheckMonthly getItem(int index) {
        return this.data.get(index);
    }

    // getItemCount() is called many times, and when it is first called,
    // data has not been updated (means initially, it's null, and we can't return null).
    @Override
    public int getItemCount() {
        if (data != null)
            return data.size();
        else
            return 0;
    }
}
