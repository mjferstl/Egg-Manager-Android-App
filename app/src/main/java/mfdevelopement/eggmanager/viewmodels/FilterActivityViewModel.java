package mfdevelopement.eggmanager.viewmodels;

import android.app.Application;
import android.net.ParseException;
import android.util.Log;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import mfdevelopement.eggmanager.database.EggManagerRepository;

public class FilterActivityViewModel extends AndroidViewModel {

    private final String LOG_TAG = "FilterActivityViewModel";
    private List<String> yearNamesList = new ArrayList<>();

    private EggManagerRepository repository;

    public FilterActivityViewModel(Application application) {
        super(application);
        repository = new EggManagerRepository(application);
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

    public List<String> getYearNames() {
        return yearNamesList;
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
