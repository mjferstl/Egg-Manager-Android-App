package mfdevelopement.eggmanager.viewmodels;

import android.app.Application;
import android.net.ParseException;
import android.util.Log;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import mfdevelopement.eggmanager.R;
import mfdevelopement.eggmanager.data_models.daily_balance.DateKeyUtils;
import mfdevelopement.eggmanager.database.EggManagerRepository;

public class FilterActivityViewModel extends AndroidViewModel {

    private final String LOG_TAG = "FilterActivityViewModel";
    private List<String> yearNamesList = new ArrayList<>();
    private List<String> uniqueYearMonthList;
    private final List<String> monthNamesReference;

    private final EggManagerRepository repository;

    public FilterActivityViewModel(Application application) {
        super(application);
        repository = new EggManagerRepository(application);
        monthNamesReference = Arrays.asList(getApplication().getResources().getStringArray(R.array.month_names));
        uniqueYearMonthList = repository.getDateKeysList();
    }

    public String getFilterString() {
        return repository.getDataFilter();
    }

    public LiveData<List<String>> getAllDateKeys() {
        return repository.getDateKeys();
    }

    public void setYearNames(List<String> stringList) {
        yearNamesList = sortYearsList(stringList);
    }

    public void setYearMonthNames(List<String> stringList) {
        // get a list of unique strings
        List<String> uniqueNames = new ArrayList<>(new HashSet<>(stringList));

        // sort in descending order
        Collections.sort(uniqueNames);
        Collections.reverse(uniqueNames);

        uniqueYearMonthList = uniqueNames;
    }

    public List<String> getYearNames() {
        return yearNamesList;
    }

    private List<String> getYearMonthNames() {
        if (uniqueYearMonthList != null) {
            return uniqueYearMonthList;
        } else {
            return repository.getDateKeysList();
        }
    }

    public List<String> getMonthsByYear(String year) {
        List<String> months = new ArrayList<>();
        for (String yearMonth : getYearMonthNames()) {
            if (DateKeyUtils.getYearByDateKey(yearMonth).equals(year)) {
                months.add(DateKeyUtils.getMonthByDateKey(yearMonth));
            }
        }

        // use only unique entries
        List<String> uniqueMonthNumbers = new ArrayList<>(new HashSet<>(months));

        // sort in ascending order
        Collections.sort(uniqueMonthNumbers);

        // convert numbers (0-11) to names (January...December) and return the List
        return getMonthNamesByIndex(uniqueMonthNumbers);
    }

    /**
     * get the name of a month by its index in the range 1..12
     * @param index ranges from 1 to 12
     * @return Name of the month (January .. December)
     */
    public String getMonthNameByIndex(int index) {
        return monthNamesReference.get(index-1);
    }

    private List<String> getMonthNamesByIndex(List<String> indexList) {

        List<String> monthNames = new ArrayList<>();
        for (String indexString : indexList) {
            int index = Integer.parseInt(indexString)-1;
            monthNames.add(monthNamesReference.get(index));
        }
        return monthNames;
    }

    public int getMonthIndexByName(String monthName) {
        for (int i=0; i<monthNamesReference.size(); i++) {
            if (monthNamesReference.get(i).equals(monthName)) {
                return i+1;
            }
        }

        // if no month has been found
        return 0;
    }

    public void setFilterString(String filterString) {
        repository.setDataFilter(filterString);
    }

    private List<String> sortYearsList(List<String> yearsList) {

        if (yearsList != null) {
            Log.d(LOG_TAG,"fetched all dateKeys from the database. Got " + yearsList.size() + " items");
            for (int i=0; i<yearsList.size(); i++) {
                try {
                    int test = Integer.parseInt(yearsList.get(i));
                } catch (ParseException e) {
                    Log.e(LOG_TAG,"Error when parsing string to int with \"" + yearsList.get(i) + "\"");
                    yearsList.remove(i);
                }
            }

            // get only unique entries
            yearsList = new ArrayList<>(new HashSet<>(yearsList));

            // sort list in ascending order
            Collections.sort(yearsList);
            Collections.reverse(yearsList);
        } else {
            Log.e(LOG_TAG,"sortYearsList() got an empty list...");
        }

        return yearsList;
    }
}
