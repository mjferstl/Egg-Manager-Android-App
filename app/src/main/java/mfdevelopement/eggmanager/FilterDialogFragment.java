package mfdevelopement.eggmanager;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

public class FilterDialogFragment extends DialogFragment {

    private final String LOG_TAG = "FilterDialogFragment";
    private static List<String> dateKeys;
    private OnAddFilterListener listener;
    private static DailyBalanceViewModel viewModel;
    private List<String> monthNames;

    // interface for updating parent activity
    public interface OnAddFilterListener {
        void onAddFilterListenerSubmit(String filterString);
    }

    public FilterDialogFragment() {
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof FilterDialogFragment.OnAddFilterListener) {
            listener = (OnAddFilterListener) context;
            monthNames = Arrays.asList(context.getResources().getStringArray(R.array.month_names));
        } else {
            throw new ClassCastException(context.toString()
                    + " must implement FilterDialogFragment.OnAddFilterListener");
        }
    }

    public static FilterDialogFragment newInstance(List<String> dateKeysList, DailyBalanceViewModel dailyBalanceViewModel) {
        dateKeys = dateKeysList;
        viewModel = dailyBalanceViewModel;
        return new FilterDialogFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_filter_dialog, container);

        RecyclerView recyclerView = v.findViewById(R.id.filter_dialog_recycler_view);
        final FilterDialogListAdapter adapter = new FilterDialogListAdapter(v.getContext(), viewModel);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(v.getContext()));

        List<String> unqiueYearMonthKeys = getUniqueYearMonthKeys();
        List<String> uniqueYearKeys = getUniqueYearKeys(dateKeys);
        List<String> yearMonthFilterList = new ArrayList<>();

        for (int iYear=0; iYear<uniqueYearKeys.size(); iYear++) {
            String currYear = uniqueYearKeys.get(iYear);
            Log.d(LOG_TAG,"sortTest: " + currYear);
            yearMonthFilterList.add(currYear);

            // all keys of the selected year --> all months with data
            List<String> currYearDateKeys = getDateKeysByYear(unqiueYearMonthKeys,uniqueYearKeys.get(iYear));
            List<String> currMonthKeys = getMonthKeys(currYearDateKeys);

            // loop over all months
            for (int iMonth=0; iMonth<currMonthKeys.size(); iMonth++) {
                int indexMonth = Integer.valueOf(currMonthKeys.get(iMonth))-1;
                String currMonthName = monthNames.get(indexMonth);
                Log.d(LOG_TAG,"sortTestMonth:" + currMonthName);
                yearMonthFilterList.add(currMonthName);
            }
        }

        adapter.setFilterStrings(yearMonthFilterList);

        return v;
    }

    private List<String> getUniqueYearMonthKeys() {

        List<String> yearMonthKeys = new ArrayList<>();
        for (int i=0; i<dateKeys.size(); i++) {
            yearMonthKeys.add(dateKeys.get(i).substring(0,6));
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

    private List<String> sortReverse(List<String> stringList) {

        // sort list in descending order
        Collections.sort(stringList);
        Collections.reverse(stringList);
        return stringList;
    }
}
