package mfdevelopement.eggmanager.list_adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.TextView;

import java.text.ParseException;
import java.util.Calendar;
import java.util.List;

import mfdevelopement.eggmanager.R;

import static mfdevelopement.eggmanager.activities.DataCompletenessCheckActivity.sdf_human_readable;

public class MissingDateListAdapter extends BaseAdapter {

    private final String LOG_TAG = "MissingDateListAdapter";
    private List<String> mData;
    private Context context;
    private OnCreateEntityClickListener listener;

    public interface OnCreateEntityClickListener {
        void onCreateEntityClicked(Calendar newDate);
    }

    public MissingDateListAdapter(Context context, List<String> list) {
        Log.d(LOG_TAG,"creating new instance");
        this.mData = list;

        if (context instanceof OnCreateEntityClickListener) {
            listener = (OnCreateEntityClickListener) context;
        } else {
            throw new ClassCastException(context.toString()
                    + " must implement MissingDateListAdapter.OnCreateEntityClickListener");
        }
    }

    @Override
    public int getCount() {
        return mData.size();
    }

    @Override
    public String getItem(int position) {
        return mData.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        final View result;

        if (convertView == null) {
            result = LayoutInflater.from(parent.getContext()).inflate(R.layout.listview_item_missing_date_item, parent, false);
        } else {
            result = convertView;
        }

        String item = getItem(position);

        TextView txtv_date = result.findViewById(R.id.txtv_recycler_item_missing_date);
        txtv_date.setText(item);

        ImageButton button = result.findViewById(R.id.imgv_recycler_item_missing_date);
        button.setOnClickListener(v -> {
            Log.d(LOG_TAG,"user wants to create a new entry for the date \"" + item + "\"");

            Calendar cal = Calendar.getInstance();
            try {
                cal.setTime(sdf_human_readable.parse(item));
                listener.onCreateEntityClicked(cal);
            } catch (ParseException e) {
                Log.e(LOG_TAG,"ParseException when parsing \""+ item +"\"");
            }
        });

        return result;
    }
}
