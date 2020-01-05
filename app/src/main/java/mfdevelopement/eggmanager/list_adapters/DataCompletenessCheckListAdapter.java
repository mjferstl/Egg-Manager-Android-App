package mfdevelopement.eggmanager.list_adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import mfdevelopement.eggmanager.R;
import mfdevelopement.eggmanager.data_models.DataCheckMonthly;

import static mfdevelopement.eggmanager.data_models.DailyBalance.DATE_KEY_FORMAT;

public class DataCompletenessCheckListAdapter extends RecyclerView.Adapter<DataCompletenessCheckListAdapter.DataCompletenessCheckViewHolder> {

    private final String LOG_TAG = "DataCompletenessCheckLi";
    private List<DataCheckMonthly> data;
    private final LayoutInflater mInflater;
    private Context context;

    public class DataCompletenessCheckViewHolder extends RecyclerView.ViewHolder{

        private TextView txtv_month_name, txtv_extra_info;
        private ImageView imgv_icon;
        private ImageButton imgbtn_expand;
        private ListView lv_details;

        private DataCompletenessCheckViewHolder(View itemView) {
            super(itemView);

            txtv_month_name = itemView.findViewById(R.id.txtv_recycler_item_completeness_check_name);
            imgv_icon = itemView.findViewById(R.id.imgv_recycler_item_completeness_check_icon);
            imgbtn_expand = itemView.findViewById(R.id.imgbtn_recycler_item_completeness_check_expand);
            txtv_extra_info = itemView.findViewById(R.id.txtv_recycler_item_completeness_check_extra_info);
            lv_details = itemView.findViewById(R.id.lv_recycler_item_completeness_check_details);

            imgbtn_expand.setTag(R.drawable.ic_expand_more_black_24dp);
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
        if (monthyCheck.isComplete()) {
            holder.imgv_icon.setImageDrawable(this.context.getDrawable(R.drawable.ic_success));
            holder.imgbtn_expand.setVisibility(View.GONE);
        } else {
            holder.imgv_icon.setImageDrawable(this.context.getDrawable(R.drawable.ic_error));
            holder.imgbtn_expand.setVisibility(View.VISIBLE);

            // fill in the list view
            List<String> missingDates = getFormattedDateKey(monthyCheck.getMissingDates());
            MissingDateListAdapter adapter = new MissingDateListAdapter(missingDates);
            holder.lv_details.setAdapter(adapter);
            Log.d(LOG_TAG,"adapter contains " + adapter.getCount() + " items");
        }
        
        // list view is hidden until the user wants to display it
        holder.lv_details.setVisibility(View.GONE);
        holder.imgbtn_expand.setTag(R.drawable.ic_expand_more_black_24dp);

        holder.imgbtn_expand.setOnClickListener(v -> {
            ImageButton imgbtn = (ImageButton) v;

            switch((int)imgbtn.getTag()) {
                case R.drawable.ic_expand_more_black_24dp:
                    imgbtn.setImageDrawable(this.context.getDrawable(R.drawable.ic_expand_less_black_24dp));
                    imgbtn.setTag(R.drawable.ic_expand_less_black_24dp);
                    holder.lv_details.setVisibility(View.VISIBLE);
                    break;
                case R.drawable.ic_expand_less_black_24dp:
                    imgbtn.setImageDrawable(this.context.getDrawable(R.drawable.ic_expand_more_black_24dp));
                    imgbtn.setTag(R.drawable.ic_expand_more_black_24dp);
                    holder.lv_details.setVisibility(View.GONE);
                    break;
            }
        });

        // create an extra info showing how many days of the month within the time period are in the database
        int daysOfMonth = monthyCheck.getFoundDates().size() + monthyCheck.getMissingDates().size();
        String extraInfo = monthyCheck.getFoundDates().size() + "/" + daysOfMonth;
        holder.txtv_extra_info.setText(extraInfo);
    }

    /**
     * Format a date in the syntax of "yyyyMM" to a date in the format "MMMM yyyy"
     * Example: "201910" gets formatted to "October 2019"
     * @param dateKeyYearMonth date in the format: yyyyMM
     * @return String containing the formatted date
     */
    private String getFormattedMonthName(String dateKeyYearMonth) {
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

    private List<String> getFormattedDateKey(List<String> dateKeyList) {

        List<String> formattedDates = new ArrayList<>();
        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault());
        SimpleDateFormat sdf_date_keys = new SimpleDateFormat(DATE_KEY_FORMAT, Locale.getDefault());

        for (String dateKey : dateKeyList) {
            String formattedDate;

            try {
                formattedDate = sdf.format(sdf_date_keys.parse(dateKey));
            } catch (ParseException e) {
                e.printStackTrace();
                formattedDate = "error";
            }
            formattedDates.add(formattedDate);
        }

        return formattedDates;
    }

    /**
     * Set the tobe shown in the Recycler View
     * @param dataCheckMonthlyList List<DataCheckMonthly>
     */
    public void setData(@NonNull List<DataCheckMonthly> dataCheckMonthlyList) {
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
