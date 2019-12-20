package mfdevelopement.eggmanager.viewmodels;

import android.app.Application;
import android.util.Log;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

import com.github.mikephil.charting.data.Entry;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.TimeUnit;

import mfdevelopement.eggmanager.FilterStringHandle;
import mfdevelopement.eggmanager.R;
import mfdevelopement.eggmanager.data_models.DailyBalance;
import mfdevelopement.eggmanager.database.EggManagerRepository;

public class SharedViewModel extends AndroidViewModel implements FilterStringHandle {

    // String for assignment when printing out logs
    private final String LOG_TAG = "DatabaseActivityViewMod";

    // Repository
    private EggManagerRepository mRepository;

    // LiveData
    private LiveData<Integer> ldFilteredEggsCollected, ldFilteredEggsSold;
    private LiveData<Double> ldFilteredMoneyEarned;
    private List<String> monthNamesReference;
    private LiveData<List<DailyBalance>> filteredDailyBalances, allDailyBalances;

    // Mutable LiveData
    private MutableLiveData<String> dataFilter = new MutableLiveData<>();
    private MutableLiveData<String> sortingOrder = new MutableLiveData<>();

    // Calendar containing the reference date
    private final Calendar referenceDate;


    // Constructor
    public SharedViewModel(Application application) {
        super(application);
        mRepository = new EggManagerRepository(application);

        // load month names
        monthNamesReference = Arrays.asList(getApplication().getResources().getStringArray(R.array.month_names));

        // sorting order
        sortingOrder.setValue(mRepository.getSortingOrder());

        // set the reference date
        referenceDate = getReferenceDate();

        allDailyBalances = mRepository.getAllDailyBalances();

        // filtered live data
        dataFilter.setValue(mRepository.getDataFilter());
        filteredDailyBalances = Transformations.switchMap(dataFilter, filter -> mRepository.getFilteredDailyBalance(filter));
        ldFilteredMoneyEarned = Transformations.switchMap(dataFilter, filter -> mRepository.getFilteredMoneyEarned(filter));
        ldFilteredEggsCollected = Transformations.switchMap(dataFilter, filter -> mRepository.getFilteredEggsCollected(filter));
        ldFilteredEggsSold = Transformations.switchMap(dataFilter, filter -> mRepository.getFilteredEggsSold(filter));
    }

    public LiveData<List<DailyBalance>> getAllDailyBalances() {
        return allDailyBalances;
    }

    /**
     * Get the entries of the database. Filtered by the saved filter string
     * @return LiveData containing a List of DailyBalance objects
     */
    public LiveData<List<DailyBalance>> getFilteredDailyBalance() {
        return filteredDailyBalances;
    }

    public LiveData<Double> getFilteredMoneyEarned() {
        return ldFilteredMoneyEarned;
    }

    public LiveData<Integer> getFilteredEggsCollected() {
        return ldFilteredEggsCollected;
    }

    public LiveData<Integer> getFilteredEggsSold() {
        return ldFilteredEggsSold;
    }

    public void setDateFilter(String dateFilter) {
        Log.d(LOG_TAG,"setDateFilter(): new filter \"" + dateFilter + "\" applied");
        dataFilter.setValue(dateFilter);
    }

    public String getDateFilter() {
        return this.dataFilter.getValue();
    }

    public void insert(DailyBalance dailyBalance) {
        mRepository.insert(dailyBalance);
    }

    public void delete(DailyBalance dailyBalance) {
        mRepository.delete(dailyBalance);
    }

    public String loadDateFilter() {
        return mRepository.getDataFilter();
    }

    /**
     * get the name of a month by its index in the range 1..12
     * @param index ranges from 1 to 12
     * @return Name of the month (January .. December)
     */
    public String getMonthNameByIndex(int index) {
        String monthName = monthNamesReference.get(index-1);
        Log.d(LOG_TAG,"getMonthNameByIndex(): index = " + index + ", month = " + monthName);
        return monthName;
    }

    /**
     * Save a new sorting order and update its value in the view models
     * The new sorting order is only saved, if it is a valid value
     * @param sortingOrder String containing the sorting order. Possible values: "ASC", "DESC"
     */
    public void setSortingOrder(String sortingOrder) {
        if (sortingOrder.equals("ASC") || sortingOrder.equals("DESC")) {
            mRepository.setSortingOrder(sortingOrder);
            this.sortingOrder.setValue(mRepository.getSortingOrder());
            Log.d(LOG_TAG,"new sorting order saved in repository: " + this.sortingOrder.getValue());
        } else {
            Log.e(LOG_TAG,"setSortingOrder(): the argument \"" + sortingOrder + "\" is not allowed. Argument needs to be \"ASC\" or \"DESC\"");
        }
    }

    /**
     * Load the saved sorting order for the items from the repository and save it to the view model
     * @return sorting order as String. Possible values: "ASC", "DESC"
     */
    public String getSortingOrder() {
        sortingOrder.setValue(mRepository.getSortingOrder());
        return sortingOrder.getValue();
    }

    public List<Entry> getDataEggsCollected(List<DailyBalance> dailyBalanceList) {
        List<Entry> entries = new ArrayList<>();
        for (DailyBalance db: dailyBalanceList) {
            long days_iterator = getDifferenceInDays(db.getDate().getTime(),referenceDate.getTimeInMillis())+1;
            entries.add(new Entry((float)days_iterator, db.getEggsCollected()));
        }
        return entries;
    }

    /**
     * Calculates the difference in days between two timestamps in milliseconds
     * result = timestamp_first - timestamp_second
     * @param timestamp_first first timestamp in milliseconds
     * @param timestamp_second second timestamp in milliseconds
     * @return difference between both timestamps in days
     */
    private long getDifferenceInDays(long timestamp_first, long timestamp_second) {
        long difference = timestamp_first-timestamp_second;
        return TimeUnit.DAYS.convert(difference, TimeUnit.MILLISECONDS);
    }

    public Calendar getReferenceDate() {
        Calendar reference = Calendar.getInstance();
        reference.set(2000, 0, 1, 0, 0, 0);
        return reference;
    }
}
