package mfdevelopement.eggmanager.viewmodels;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;

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

public class ChartViewModel extends AndroidViewModel  implements FilterStringHandle {

    private EggManagerRepository repository;
    private final String LOG_TAG = "ChartViewModel";
    private Calendar ref_cal;
    private List<String> monthNamesReference;

    public ChartViewModel(Application application) {
        super(application);
        repository = new EggManagerRepository(application);
        ref_cal = getReferenceCalendar();
        monthNamesReference = Arrays.asList(getApplication().getResources().getStringArray(R.array.month_names));
    }

    private String getDataFilterString() {
        return repository.getDataFilter();
    }

    public List<DailyBalance> getFilteredDailyBalance() {
        return repository.getDailyBalancesByDateKey(getDataFilterString());
    }

    public List<Entry> getDataEggsCollected(List<DailyBalance> dailyBalanceList) {
        List<Entry> entries = new ArrayList<>();
        for (DailyBalance db: dailyBalanceList) {
            long days_iterator = getDifferenceInDays(db.getDate().getTime(),ref_cal.getTimeInMillis())+1;
            entries.add(new Entry((float)days_iterator, db.getEggsCollected()));
        }
        return entries;
    }

    private long getDifferenceInDays(long timestamp_first, long timestamp_second) {
        long difference = Math.abs(timestamp_first-timestamp_second);
        return TimeUnit.DAYS.convert(difference, TimeUnit.MILLISECONDS);
    }

    public static Calendar getReferenceCalendar() {
        Calendar referenceCalendar = Calendar.getInstance();
        referenceCalendar.set(2000,0,1,0,0,0);
        return referenceCalendar;
    }

    @Override
    public String loadDateFilter() {
        return repository.getDataFilter();
    }

    /**
     * get the name of a month by its index in the range 1..12
     * @param index ranges from 1 to 12
     * @return Name of the month (January .. December)
     */
    public String getMonthNameByIndex(int index) {
        return monthNamesReference.get(index-1);
    }
}