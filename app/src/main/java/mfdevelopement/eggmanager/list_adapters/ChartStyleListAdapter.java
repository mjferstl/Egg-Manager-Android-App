package mfdevelopement.eggmanager.list_adapters;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.core.content.ContextCompat;

import com.google.android.material.radiobutton.MaterialRadioButton;

import java.util.List;

import mfdevelopement.eggmanager.R;
import mfdevelopement.eggmanager.data_models.TextWithIconItem;

public class ChartStyleListAdapter extends BaseAdapter {

    private static final String LOG_TAG = "ChartStyleListAdapter";
    private final List<TextWithIconItem> textWithIconItemList;
    public final static int NO_ITEM_SELECTED_POSITION = -1;
    private int positionSelected = NO_ITEM_SELECTED_POSITION;

    public OnItemClickListener onItemClickListener;

    public interface OnItemClickListener {
        void onItemClicked(TextWithIconItem textWithIconItem, int position);
    }

    public ChartStyleListAdapter(@NonNull List<TextWithIconItem> itemList) {
        this.textWithIconItemList = itemList;
    }

    @Override
    public int getCount() {
        return this.textWithIconItemList.size();
    }

    @Override
    public TextWithIconItem getItem(int position) {
        return this.textWithIconItemList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        final View view;

        if (convertView == null) {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_chart_style, parent, false);
        } else {
            view = convertView;
        }

        TextWithIconItem item = getItem(position);

        MaterialRadioButton radioButton = view.findViewById(R.id.radio_chart_style);
        radioButton.setChecked(position == positionSelected);
        radioButton.setOnClickListener(v -> handleClick(item, position));

        ImageView imageView = view.findViewById(R.id.imgv_chart_style_icon);
        imageView.setImageDrawable(AppCompatResources.getDrawable(parent.getContext(), item.getImageId()));
        imageView.setColorFilter(ContextCompat.getColor(parent.getContext(), R.color.gray));

        TextView textView = view.findViewById(R.id.txtv_chart_style);
        textView.setText(item.getName());
        textView.setTextColor(ContextCompat.getColor(parent.getContext(), R.color.gray));

        view.setOnClickListener(v -> handleClick(item, position));

        return view;
    }

    private void handleClick(TextWithIconItem item, int position) {
        if (onItemClickListener != null) {
            onItemClickListener.onItemClicked(item, position);
        } else {
            Log.d(LOG_TAG, "no OnItemClickListener has been set yet");
        }
    }

    public void setSelectedPosition(int position) {
        if (position >= this.textWithIconItemList.size()) {
            throw new IndexOutOfBoundsException("The item at position " + position + " cannot be selected, as the list contains only " + this.textWithIconItemList.size() + " items");
        }
        this.positionSelected = position;
        notifyDataSetChanged();
    }

    public void setOnItemClickListener(@NonNull OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }
}
