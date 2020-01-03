package mfdevelopement.eggmanager.activities;

import android.os.Bundle;

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
import mfdevelopement.eggmanager.data_models.DataCheckMonthly;
import mfdevelopement.eggmanager.list_adapters.DataCompletenessCheckListAdapter;
import mfdevelopement.eggmanager.viewmodels.DataCheckViewModel;

import static mfdevelopement.eggmanager.data_models.DailyBalance.DATE_KEY_FORMAT;

public class DataCompletenessCheckActivity extends AppCompatActivity {

    private DataCheckViewModel viewModel;

    private DataCompletenessCheckListAdapter adapter;

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
        viewModel.getAllDateKeys().observe(this, this::checkCompleteness);
    }

    private void checkCompleteness(List<String> allDateKeys) {

        List<String> matchingDateKeys = new ArrayList<>();
        List<String> missingDateKeys = new ArrayList<>();

        SimpleDateFormat sdf = new SimpleDateFormat(DATE_KEY_FORMAT, Locale.getDefault());

        Collections.sort(allDateKeys);

        Calendar startDate = Calendar.getInstance();
        Calendar endDate = Calendar.getInstance();
        try {
            startDate.setTime(sdf.parse(allDateKeys.get(0)));
            endDate.setTime(sdf.parse(allDateKeys.get(allDateKeys.size()-1)));
        } catch (ParseException e) {
            e.printStackTrace();
            return;
        }

        int diffDays = (int)((endDate.getTimeInMillis() - startDate.getTimeInMillis())/(1000*60*60*24));

        // check every day between the start and end date, if it exists in the date keys list
        Calendar currDate = Calendar.getInstance();
        currDate.setTimeInMillis(startDate.getTimeInMillis());
        currDate.add(Calendar.DAY_OF_MONTH, -1);
        for (int d = 0; d < diffDays; d++) {
            currDate.add(Calendar.DAY_OF_MONTH, 1);
            String currDateKey = sdf.format(currDate.getTimeInMillis());
            if (allDateKeys.contains(currDateKey))
                matchingDateKeys.add(currDateKey);
            else
                missingDateKeys.add(currDateKey);
        }

        List<String> allDateKeysYearMonth = new ArrayList<>();
        for (String dateKey : allDateKeys) {
            allDateKeysYearMonth.add(dateKey.substring(0,6));
        }

        // get unique entries
        List<String> uniqueDateKeysYearMonth = new ArrayList<>(new HashSet<>(allDateKeysYearMonth));
        Collections.sort(uniqueDateKeysYearMonth);

        // check every month for completeness of the data
        List<DataCheckMonthly> dataCheckMonthlyList = new ArrayList<>();

        for (String uniqueDateKeyYearMonth : uniqueDateKeysYearMonth) {
            List<String> currMatchingKeys = new ArrayList<>();
            List<String> currMissingKeys = new ArrayList<>();
            for (String foundDateKey : matchingDateKeys) {
                if (foundDateKey.substring(0,6).equals(uniqueDateKeyYearMonth))
                    currMatchingKeys.add(foundDateKey);
            }
            for (String missingDateKey : missingDateKeys) {
                if (missingDateKey.substring(0,6).equals(uniqueDateKeyYearMonth))
                    currMissingKeys.add(missingDateKey);
            }
            dataCheckMonthlyList.add(new DataCheckMonthly(uniqueDateKeyYearMonth, currMatchingKeys, currMissingKeys));
        }

        // update recycler view
        adapter.setData(dataCheckMonthlyList);
    }
}
