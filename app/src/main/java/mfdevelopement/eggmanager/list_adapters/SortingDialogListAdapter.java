package mfdevelopement.eggmanager.list_adapters;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import mfdevelopement.eggmanager.R;
import mfdevelopement.eggmanager.data_models.SortingItem;
import mfdevelopement.eggmanager.data_models.SortingItemCollection;

public class SortingDialogListAdapter extends BaseAdapter {

    private final String LOG_TAG = "SortingDialogListAdapte";
    private List<SortingItem> mData;

    public SortingDialogListAdapter(SortingItemCollection list) {
        Log.d(LOG_TAG,"creating new instance");
        mData = list.getItems();
    }


    @Override
    public int getCount() {
        return mData.size();
    }

    @Override
    public SortingItem getItem(int position) {
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
            result = LayoutInflater.from(parent.getContext()).inflate(R.layout.listview_sorting_dialog_item, parent, false);
        } else {
            result = convertView;
        }

        SortingItem item = getItem(position);

        TextView txtv_item_name = result.findViewById(R.id.txtv_sorting_dialog_item);
        txtv_item_name.setText(item.getSortingName());

        ImageView imgv_item_checkmark = result.findViewById(R.id.imgv_sorting_dialog_item);
        if (item.isSelected())
            imgv_item_checkmark.setVisibility(View.VISIBLE);
        else
            imgv_item_checkmark.setVisibility(View.GONE);

        return result;
    }
}
