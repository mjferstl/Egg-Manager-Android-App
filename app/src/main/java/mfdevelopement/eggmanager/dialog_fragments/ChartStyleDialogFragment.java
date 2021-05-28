package mfdevelopement.eggmanager.dialog_fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import java.util.List;

import mfdevelopement.eggmanager.R;
import mfdevelopement.eggmanager.data_models.TextWithIconItem;
import mfdevelopement.eggmanager.list_adapters.ChartStyleListAdapter;

public class ChartStyleDialogFragment extends DialogFragment {

    private final List<TextWithIconItem> textWithIconItemList;
    private ChartStyleListAdapter chartStyleListAdapter;
    private int positionSelected = ChartStyleListAdapter.NO_ITEM_SELECTED_POSITION;
    private ChartStyleListAdapter.OnItemClickListener onItemClickListener;

    public ChartStyleDialogFragment(@NonNull List<TextWithIconItem> itemList) {
        this.textWithIconItemList = itemList;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_chart_style, container, false);

        ListView listView = view.findViewById(R.id.lv_chart_styles);
        chartStyleListAdapter = new ChartStyleListAdapter(this.textWithIconItemList);
        if (positionSelected != ChartStyleListAdapter.NO_ITEM_SELECTED_POSITION) {
            chartStyleListAdapter.setSelectedPosition(positionSelected);
        }
        if (onItemClickListener != null) {
            chartStyleListAdapter.setOnItemClickListener(onItemClickListener);
        }
        listView.setAdapter(chartStyleListAdapter);
        return view;
    }

    public void setItemSelected(int position) {
        if (chartStyleListAdapter == null) {
            positionSelected = position;
        } else {
            chartStyleListAdapter.setSelectedPosition(position);
        }
    }

    public void setOnItemClickListener(ChartStyleListAdapter.OnItemClickListener onItemClickListener) {
        if (chartStyleListAdapter == null) {
            this.onItemClickListener = onItemClickListener;
        } else {
            chartStyleListAdapter.setOnItemClickListener(onItemClickListener);
        }
    }
}
