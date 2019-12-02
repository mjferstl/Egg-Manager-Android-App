package mfdevelopement.eggmanager.dialog_fragments;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import mfdevelopement.eggmanager.R;
import mfdevelopement.eggmanager.list_adapters.FilterDialogListAdapter;

public class FilterDialogFragment extends DialogFragment {

    private final String LOG_TAG = "FilterDialogFragment";
    private static List<String> dateKeys;
    private Context parentContext;
    private TextView txtv_caption_months;
    public static final String NOT_SET_FILTER_STRING = "0000";
    private String selectedFilterString = NOT_SET_FILTER_STRING;
    private RecyclerView recyclerViewMonths;
    private OnFilterDialogClickListener filterDialogClickListener;
    private FilterDialogListAdapter monthsAdapter, yearsAdapter;
    private static String initialFilterString = NOT_SET_FILTER_STRING;

    public interface OnFilterDialogClickListener {
        void onFilterDialogOkClicked(String filterString);
        void onFilterDialogCancelClicked();
    }

    public FilterDialogFragment() {
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        parentContext = context;
        monthsAdapter = new FilterDialogListAdapter(parentContext, initialFilterString);
        Log.d(LOG_TAG,"context of FilterDialogFragment: " + parentContext);

        if (context instanceof OnFilterDialogClickListener) {
            filterDialogClickListener = (OnFilterDialogClickListener) context;
        } else {
            throw new ClassCastException(context.toString()
                    + " must implement FilterDialogFragment.OnFilterDialogClickListener");
        }
    }

    public static FilterDialogFragment newInstance(List<String> dateKeysList, String currentFilterString) {
        dateKeys = getUniqueYearMonthKeys(dateKeysList);
        Collections.sort(dateKeys); // sort in ascending order
        initialFilterString = currentFilterString;
        return new FilterDialogFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_filter_dialog, container);

        selectedFilterString = initialFilterString;

        // initialize image button to remove the current filter
        ImageButton imgbtn_filter_remove = v.findViewById(R.id.imgbtn_filter_remove);
        imgbtn_filter_remove.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(LOG_TAG,"button to remove filter clicked");
                selectedFilterString = NOT_SET_FILTER_STRING;
                Log.d(LOG_TAG,"selectedFilterString = " + selectedFilterString);
                setNoButtonSelected();
                filterDialogClickListener.onFilterDialogOkClicked(selectedFilterString);
            }
        });

        // initialize OK-button
        Button btn_ok = v.findViewById(R.id.btn_ok);
        btn_ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                filterDialogClickListener.onFilterDialogOkClicked(selectedFilterString);
            }
        });

        // initialize cancel button
        Button btn_cancel = v.findViewById(R.id.btn_cancel);
        btn_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                filterDialogClickListener.onFilterDialogCancelClicked();
            }
        });

        txtv_caption_months = v.findViewById(R.id.txtv_filter_dialog_caption_months);

        // recycler view for annually buttons
        RecyclerView recyclerViewYears = v.findViewById(R.id.filter_dialog_recycler_view_years);
        yearsAdapter = new FilterDialogListAdapter(parentContext,initialFilterString);
        yearsAdapter.setOnItemClickListener((FilterDialogListAdapter.OnFilterSelectListener) parentContext);
        recyclerViewYears.setAdapter(yearsAdapter);
        recyclerViewYears.setLayoutManager(new GridLayoutManager(v.getContext(),2));

        // recycler view for monthly buttons
        recyclerViewMonths = v.findViewById(R.id.filter_dialog_recycler_view_months);
        recyclerViewMonths.setLayoutManager(new GridLayoutManager(v.getContext(),2));

        // adapter for months selection
        monthsAdapter.setOnItemClickListener((FilterDialogListAdapter.OnFilterSelectListener) parentContext);
        recyclerViewMonths.setAdapter(monthsAdapter);

        if (!initialFilterString.equals(NOT_SET_FILTER_STRING) &&
                (initialFilterString.length() == 6 || initialFilterString.length() == 4)) {
            txtv_caption_months.setVisibility(View.VISIBLE);
            monthsAdapter.setFilterStrings(getDateKeysByYear(dateKeys, initialFilterString.substring(0,4)));
            recyclerViewMonths.setVisibility(View.VISIBLE);
        } else {
            txtv_caption_months.setVisibility(View.GONE);
            recyclerViewMonths.setVisibility(View.GONE);
            if (!initialFilterString.equals(NOT_SET_FILTER_STRING) && initialFilterString.length() == 4)
                monthsAdapter.setNoButtonSelected();
        }

        List<String> uniqueYearKeys = getUniqueYearKeys(dateKeys);
        List<String> yearFilterList = new ArrayList<>();

        for (int iYear=0; iYear<uniqueYearKeys.size(); iYear++) {
            String currYear = uniqueYearKeys.get(iYear);
            Log.d(LOG_TAG,"sortTest: " + currYear);
            yearFilterList.add(currYear);
        }

        yearsAdapter.setFilterStrings(yearFilterList);

        return v;
    }

    private static List<String> getUniqueYearMonthKeys(List<String> dateKeysList) {

        List<String> yearMonthKeys = new ArrayList<>();
        for (int i=0; i<dateKeysList.size(); i++)
            yearMonthKeys.add(dateKeysList.get(i).substring(0,6));

        return new ArrayList<>(new HashSet<>(sortDescending(yearMonthKeys)));
    }

    private List<String> getDateKeysByYear(List<String> dateKeyList, String year) {
        List<String> itemsCurrentYear = new ArrayList<>();
        for (int i=0; i<dateKeyList.size(); i++) {
            if (dateKeyList.get(i).substring(0,4).equals(year))
                itemsCurrentYear.add(dateKeyList.get(i));
        }
        return itemsCurrentYear;
    }

    private List<String> getUniqueYearKeys(List<String> stringList) {
        List<String> yearKeys = new ArrayList<>();
        for (int i=0; i<stringList.size(); i++) {
            yearKeys.add(stringList.get(i).substring(0,4));
        }
        return new ArrayList<>(new HashSet<>(sortDescending(yearKeys)));
    }

    /**
     * sort a list containing strings in descending order
     * @param stringList List<String>
     * @return list with items in descending order
     */
    private static List<String> sortDescending(List<String> stringList) {
        Collections.sort(stringList);
        Collections.reverse(stringList);
        return stringList;
    }

    public void setButtonSelected(String selectedString, int buttonPosition, boolean isSelected) {

        Log.d(LOG_TAG,"setButtonSelected::selectedString = " + selectedString);
        Log.d(LOG_TAG,"before execution: setButtonSelected::selectedFilterString = " + selectedFilterString);

        // a button containing a year is clicked
        if (selectedString.length() == 4) {

            yearsAdapter.updateButtons(buttonPosition);
            monthsAdapter.setNoButtonSelected();

            if (!isSelected) {
                txtv_caption_months.setVisibility(View.GONE);
                recyclerViewMonths.setVisibility(View.GONE);
                selectedFilterString = NOT_SET_FILTER_STRING;
            } else {
                //updateYears
                txtv_caption_months.setVisibility(View.VISIBLE);
                recyclerViewMonths.setVisibility(View.VISIBLE);
                monthsAdapter.setFilterStrings(getDateKeysByYear(dateKeys, selectedString));
                selectedFilterString = selectedString;
            }
        }
        // a button containing a month is clicked
        if (selectedString.length() == 6) {
            if (!isSelected)
                selectedFilterString = selectedString.substring(0,4);
            else
                selectedFilterString = selectedString;
            monthsAdapter.updateButtons(buttonPosition);
        }

        Log.d(LOG_TAG,"after execution: setButtonSelected::selectedFilterString = " + selectedFilterString);
    }

    /**
     * set all items (buttons) in the recycler views to be not pressed
     */
    private void setNoButtonSelected() {
        yearsAdapter.setNoButtonSelected();
        monthsAdapter.setNoButtonSelected();

        txtv_caption_months.setVisibility(View.GONE);
        recyclerViewMonths.setVisibility(View.GONE);
    }
}
