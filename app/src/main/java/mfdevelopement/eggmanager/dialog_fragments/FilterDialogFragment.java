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
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import mfdevelopement.eggmanager.R;
import mfdevelopement.eggmanager.list_adapters.FilterDialogListAdapter;

public class FilterDialogFragment extends DialogFragment {

    private final String LOG_TAG = "FilterDialogFragment";
    private static List<String> dateKeys;
    private List<String> monthNames;
    private Context parentContext;
    private TextView txtv_caption_months;
    public static final String NOT_SET_FILTER_STRING = "0000";
    private String selectedFilterString = NOT_SET_FILTER_STRING;
    private RecyclerView recyclerViewMonths;
    private OnClickListener clickListener;
    private FilterDialogListAdapter monthsAdapter, yearsAdapter;
    private ImageButton imgbtn_filter_remove;

    public interface OnClickListener {
        void onOkClicked(String filterString);
        void onCancelClicked();
    }

    public FilterDialogFragment() {
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        parentContext = context;
        monthsAdapter = new FilterDialogListAdapter(parentContext);
        Log.d(LOG_TAG,"context of FilterDialogFragment: " + parentContext);
        monthNames = Arrays.asList(context.getResources().getStringArray(R.array.month_names));

        if (context instanceof OnClickListener) {
            clickListener = (OnClickListener) context;
        } else {
            throw new ClassCastException(context.toString()
                    + " must implement FilterDialogFragment.OnClickListener");
        }
    }

    public static FilterDialogFragment newInstance(List<String> dateKeysList) {
        dateKeys = getUniqueYearMonthKeys(dateKeysList);
        return new FilterDialogFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_filter_dialog, container);

        imgbtn_filter_remove = v.findViewById(R.id.imgbtn_filter_remove);
        imgbtn_filter_remove.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(LOG_TAG,"button to remove filter clicked");
                selectedFilterString = NOT_SET_FILTER_STRING;
                Log.d(LOG_TAG,"selectedFilterString = " + selectedFilterString);
                setNoButtonSelected();
            }
        });

        Button btn_ok = v.findViewById(R.id.btn_ok);
        btn_ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clickListener.onOkClicked(selectedFilterString);
            }
        });

        Button btn_cancel = v.findViewById(R.id.btn_cancel);
        btn_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clickListener.onCancelClicked();
            }
        });

        txtv_caption_months = v.findViewById(R.id.txtv_filter_dialog_caption_months);
        txtv_caption_months.setVisibility(View.GONE);

        RecyclerView recyclerViewYears = v.findViewById(R.id.filter_dialog_recycler_view_years);
        yearsAdapter = new FilterDialogListAdapter(parentContext);
        yearsAdapter.setOnItemClickListener((FilterDialogListAdapter.OnFilterSelectListener) parentContext);
        recyclerViewYears.setAdapter(yearsAdapter);
        recyclerViewYears.setLayoutManager(new GridLayoutManager(v.getContext(),2));

        recyclerViewMonths = v.findViewById(R.id.filter_dialog_recycler_view_months);
        recyclerViewMonths.setLayoutManager(new GridLayoutManager(v.getContext(),2));

        List<String> uniqueYearKeys = getUniqueYearKeys(dateKeys);
        List<String> yearFilterList = new ArrayList<>();

        for (int iYear=0; iYear<uniqueYearKeys.size(); iYear++) {
            String currYear = uniqueYearKeys.get(iYear);
            Log.d(LOG_TAG,"sortTest: " + currYear);
            yearFilterList.add(currYear);

            // all keys of the selected year --> all months with data
            List<String> currYearDateKeys = getDateKeysByYear(dateKeys,uniqueYearKeys.get(iYear));
            List<String> currMonthKeys = getMonthKeys(currYearDateKeys);

            // loop over all months
            for (int iMonth=0; iMonth<currMonthKeys.size(); iMonth++) {
                int indexMonth = Integer.valueOf(currMonthKeys.get(iMonth))-1;
                Log.d(LOG_TAG,"sortTestMonth:" + monthNames.get(indexMonth));
            }
        }

        yearsAdapter.setFilterStrings(yearFilterList);

        return v;
    }

    private static List<String> getUniqueYearMonthKeys(List<String> dateKeysList) {

        List<String> yearMonthKeys = new ArrayList<>();
        for (int i=0; i<dateKeysList.size(); i++) {
            yearMonthKeys.add(dateKeysList.get(i).substring(0,6));
        }
        return new ArrayList<>(new HashSet<>(sortReverse(yearMonthKeys)));
    }

    private List<String> getDateKeysByYear(List<String> dateKeyList, String year) {
        List<String> itemsCurrentYear = new ArrayList<>();
        for (int i=0; i<dateKeyList.size(); i++) {
            if (dateKeyList.get(i).substring(0,4).equals(year)) {
                itemsCurrentYear.add(dateKeyList.get(i));
            }
        }
        return itemsCurrentYear;
    }

    private List<String> getMonthKeys(List<String> stringList) {
        List<String> monthKeys = new ArrayList<>();
        for (int i=0; i<stringList.size(); i++) {
            monthKeys.add(stringList.get(i).substring(4,6));
        }
        Collections.sort(monthKeys);
        return new ArrayList<>(new HashSet<>(monthKeys));
    }

    private List<String> getUniqueYearKeys(List<String> stringList) {
        List<String> yearKeys = new ArrayList<>();
        for (int i=0; i<stringList.size(); i++) {
            yearKeys.add(stringList.get(i).substring(0,4));
        }
        return new ArrayList<>(new HashSet<>(sortReverse(yearKeys)));
    }

    private static List<String> sortReverse(List<String> stringList) {
        // sort list in descending order
        Collections.sort(stringList);
        Collections.reverse(stringList);
        return stringList;
    }

    public void setButtonSelected(String selectedString, int buttonPosition, boolean isSelected) {

        Log.d(LOG_TAG, "setButtonSelected::selectedString = " + selectedString);
        Log.d(LOG_TAG,"before execution: setButtonSelected::selectedFilterString = " + selectedFilterString);

        if (selectedString.length() == 4) {
            if (!isSelected) {
                txtv_caption_months.setVisibility(View.GONE);
                recyclerViewMonths.setVisibility(View.GONE);
                yearsAdapter.updateButtons(buttonPosition);
                selectedFilterString = NOT_SET_FILTER_STRING;
            } else {
                //updateYears
                yearsAdapter.updateButtons(buttonPosition);
                txtv_caption_months.setVisibility(View.VISIBLE);
                recyclerViewMonths.setVisibility(View.VISIBLE);
                monthsAdapter.setOnItemClickListener((FilterDialogListAdapter.OnFilterSelectListener) parentContext);
                recyclerViewMonths.setAdapter(monthsAdapter);
                monthsAdapter.setFilterStrings(getDateKeysByYear(dateKeys, selectedString));
                selectedFilterString = selectedString;
            }
        }

        // month
        if (selectedString.length() == 6) {
            if (!isSelected) {
                monthsAdapter.updateButtons(buttonPosition);
                selectedFilterString = selectedString.substring(0,4);
            } else {
                monthsAdapter.updateButtons(buttonPosition);
                selectedFilterString = selectedString;
            }
        }

        Log.d(LOG_TAG,"after execution: setButtonSelected::selectedFilterString = " + selectedFilterString);
    }

    private void setNoButtonSelected() {
        yearsAdapter.setNoButtonSelected();
        monthsAdapter.setNoButtonSelected();

        txtv_caption_months.setVisibility(View.GONE);
        recyclerViewMonths.setVisibility(View.GONE);
    }
}
