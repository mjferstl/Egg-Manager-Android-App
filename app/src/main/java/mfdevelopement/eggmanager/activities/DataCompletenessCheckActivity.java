package mfdevelopement.eggmanager.activities;

import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;

import mfdevelopement.eggmanager.R;
import mfdevelopement.eggmanager.data_models.DailyBalance;
import mfdevelopement.eggmanager.data_models.DataCheckMonthly;
import mfdevelopement.eggmanager.list_adapters.DataCompletenessCheckListAdapter;
import mfdevelopement.eggmanager.viewmodels.DataCheckViewModel;

import static mfdevelopement.eggmanager.data_models.DailyBalance.DATE_KEY_FORMAT;

public class DataCompletenessCheckActivity extends AppCompatActivity {

    private DataCheckViewModel viewModel;

    private DataCompletenessCheckListAdapter adapter;

    private TextView txtv_start_date, txtv_end_date;

    private final String LOG_TAG = "DataCompletenessCheckAc";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_data_completeness_check);

        Toolbar toolbar = findViewById(R.id.toolbar_data_check);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        viewModel = new ViewModelProvider(this).get(DataCheckViewModel.class);

        txtv_start_date = findViewById(R.id.txtv_data_check_start_date);
        txtv_end_date = findViewById(R.id.txtv_data_check_end_date);
        txtv_start_date.setOnClickListener(v -> { Log.d(LOG_TAG,"user clicked on the start date"); });
        txtv_end_date.setOnClickListener(v -> { Log.d(LOG_TAG,"user clicked on the end date"); });

        initRecyclerView();
        initObservers();
    }

    private void initRecyclerView() {
        RecyclerView recyclerView = findViewById(R.id.recv_data_completeness_check);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new DataCompletenessCheckListAdapter(this);
        recyclerView.setAdapter(adapter);
    }

    private void initObservers() {
        viewModel.getAllDateKeys().observe(this, allDateKeysList -> {
            List<DataCheckMonthly> dataCheckMonthlyList = checkCompleteness(allDateKeysList);

            // update recycler view
            if (!dataCheckMonthlyList.isEmpty())
                adapter.setData(dataCheckMonthlyList);
        });
    }


    private List<DataCheckMonthly> checkCompleteness(List<String> allDateKeys) {

        // create empty String array lists for the found and the missing date keys
        List<String> matchingDateKeys = new ArrayList<>();
        List<String> missingDateKeys = new ArrayList<>();
        List<String> dateKeysInRange = new ArrayList<>();

        // date formatter for the date keys
        SimpleDateFormat sdf = new SimpleDateFormat(DATE_KEY_FORMAT, Locale.getDefault());
        SimpleDateFormat sdf_human_readable = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault());

        // sort Strings in ascending order
        Collections.sort(allDateKeys);

        // create Calendar instances for the first and the last date
        Calendar startDate = Calendar.getInstance();
        Calendar endDate = Calendar.getInstance();
        try {
            startDate.setTime(sdf.parse(allDateKeys.get(0)));
            endDate.setTime(sdf.parse(allDateKeys.get(allDateKeys.size()-1)));
        } catch (ParseException e) {
            e.printStackTrace();
            return new ArrayList<>();
        }

        if (txtv_start_date != null)
            txtv_start_date.setText(sdf_human_readable.format(startDate.getTimeInMillis()));
        if (txtv_end_date != null)
            txtv_end_date.setText(sdf_human_readable.format(endDate.getTimeInMillis()));

        // calculate the difference in days between the first and last date
        int diffDays = (int)((endDate.getTimeInMillis() - startDate.getTimeInMillis())/(1000*60*60*24));

        // check every day between the start and end date, if it exists in the date keys list
        Calendar currDate = Calendar.getInstance();
        currDate.setTimeInMillis(startDate.getTimeInMillis());
        currDate.add(Calendar.DAY_OF_MONTH, -1);
        for (int d = 0; d <= diffDays; d++) {
            currDate.add(Calendar.DAY_OF_MONTH, 1);
            String currDateKey = sdf.format(currDate.getTimeInMillis());
            dateKeysInRange.add(currDateKey);
            if (allDateKeys.contains(currDateKey))
                matchingDateKeys.add(currDateKey);
            else
                missingDateKeys.add(currDateKey);
        }

        // get all unique date keys in the format yyyyMM within the range
        List<String> allDateKeysYearMonth = new ArrayList<>();
        for (String dateKey : dateKeysInRange) {
            allDateKeysYearMonth.add(getYearAndMonthOfDateKey(dateKey));
        }
        // get unique entries
        List<String> uniqueDateKeysYearMonth = new ArrayList<>(new HashSet<>(allDateKeysYearMonth));
        Collections.sort(uniqueDateKeysYearMonth);


        // check every month for data completeness
        List<DataCheckMonthly> dataCheckMonthlyList = new ArrayList<>();

        for (String uniqueDateKeyYearMonth : uniqueDateKeysYearMonth) {
            List<String> currMatchingKeys = new ArrayList<>();
            List<String> currMissingKeys = new ArrayList<>();
            for (String foundDateKey : matchingDateKeys) {
                if (getYearAndMonthOfDateKey(foundDateKey).equals(uniqueDateKeyYearMonth))
                    currMatchingKeys.add(foundDateKey);
            }
            for (String missingDateKey : missingDateKeys) {
                if (getYearAndMonthOfDateKey(missingDateKey).equals(uniqueDateKeyYearMonth))
                    currMissingKeys.add(missingDateKey);
            }
            dataCheckMonthlyList.add(new DataCheckMonthly(uniqueDateKeyYearMonth, currMatchingKeys, currMissingKeys));
        }

        return dataCheckMonthlyList;
    }

    private String getYearAndMonthOfDateKey(String dateKey) {
        return DailyBalance.getYearByDateKey(dateKey) + DailyBalance.getMonthByDateKey(dateKey);
    }
}
