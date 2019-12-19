package mfdevelopement.eggmanager.dialog_fragments;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;

import java.util.List;

import mfdevelopement.eggmanager.R;
import mfdevelopement.eggmanager.data_models.SortingItem;
import mfdevelopement.eggmanager.data_models.SortingItemCollection;
import mfdevelopement.eggmanager.list_adapters.SortingDialogListAdapter;

public class SortingDialogFragment extends DialogFragment {

    private static final String LOG_TAG = "SortingDialogFragment";
    private static SortingItemCollection sortingItems;

    private OnSortingItemClickListener listener;

    public interface OnSortingItemClickListener {
        void OnSortingItemClicked(String sortingOrder);
    }

    /**
     * Create a new instance of SortingDialogFragment
     */
    public static SortingDialogFragment newInstance(SortingItemCollection collection) {
        Log.d(LOG_TAG,"creating new instance of SortingDialogFragment with SortingItemCollection");
        sortingItems = collection;
        return new SortingDialogFragment();
    }

    public static SortingDialogFragment newInstance(List<SortingItem> sortingItemList) {
        Log.d(LOG_TAG,"creating new instance of SortingDialogFragment with List<SotringItem>");
        sortingItems = new SortingItemCollection(sortingItemList);
        return new SortingDialogFragment();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof OnSortingItemClickListener) {
            listener = (OnSortingItemClickListener) context;
        } else {
            throw new ClassCastException(context.toString()
                    + " must implement SortingDialogFragment.OnSortingItemClickListener");
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

//        int width = (int) (getResources().getDisplayMetrics().widthPixels);
//        if (getDialog() != null && getDialog().getWindow() != null)
//            getDialog().getWindow().setLayout(width, ViewGroup.LayoutParams.WRAP_CONTENT);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_sorting_dialog, container, false);

        ListView listView = v.findViewById(R.id.lv_sorting_dialog);

        SortingDialogListAdapter adapter = new SortingDialogListAdapter(sortingItems);
        listView.setAdapter(adapter);

        listView.setOnItemClickListener((parent, view, position, id) -> {
            SortingItem item = adapter.getItem(position);
            Log.d(LOG_TAG,"user selected item with name \"" + item.getSortingName() + "\"");
            listener.OnSortingItemClicked(item.getSortingOrder());
            if (getDialog() != null) getDialog().dismiss();
        });

        return v;
    }

    @Override
    public void dismiss() {
        super.dismiss();
        Log.d(LOG_TAG,"dialog gets dismissed");
    }
}
