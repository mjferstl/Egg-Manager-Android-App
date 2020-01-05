package mfdevelopement.eggmanager.list_adapters;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.TextView;

import java.util.List;

import mfdevelopement.eggmanager.R;

public class MissingDateListAdapter extends BaseAdapter {

    private final String LOG_TAG = "MissingDateListAdapter";
    private List<String> mData;

    public MissingDateListAdapter(List<String> list) {
        Log.d(LOG_TAG,"creating new instance");
        this.mData = list;
        Log.d(LOG_TAG,"list view contains " + list.size() + " items");
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
        });

        return result;
    }
}
