package mfdevelopement.eggmanager.viewmodels;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;

import com.github.mikephil.charting.data.Entry;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.TimeUnit;

import mfdevelopement.eggmanager.data_models.DailyBalance;
import mfdevelopement.eggmanager.database.EggManagerRepository;

public class LineChartViewModel extends AndroidViewModel {

    private EggManagerRepository repository;
    private final String LOG_TAG = "LineChartViewModel";
    private Calendar ref_cal;

    public LineChartViewModel(Application application) {
        super(application);
        repository = new EggManagerRepository(application);
        ref_cal = getReferenceCalendar();
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
}