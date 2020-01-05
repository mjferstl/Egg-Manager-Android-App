package mfdevelopement.eggmanager.activities;

import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;

import mfdevelopement.eggmanager.R;
import mfdevelopement.eggmanager.data_models.DailyBalance;
import mfdevelopement.eggmanager.data_models.DataCheckMonthly;
import mfdevelopement.eggmanager.dialog_fragments.DatePickerFragment;
import mfdevelopement.eggmanager.list_adapters.DataCompletenessCheckListAdapter;
import mfdevelopement.eggmanager.viewmodels.DataCheckViewModel;

import static mfdevelopement.eggmanager.data_models.DailyBalance.DATE_KEY_FORMAT;

public class DataCompletenessCheckActivity extends AppCompatActivity implements DatePickerFragment.OnAddDateListener {

    private DataCheckViewModel viewModel;

    private DataCompletenessCheckListAdapter adapter;

    private TextView txtv_start_date, txtv_end_date;

    // date formatter for the date keys
    private SimpleDateFormat sdf_date_keys = new SimpleDateFormat(DATE_KEY_FORMAT, Locale.getDefault());
    private SimpleDateFormat sdf_human_readable = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault());

    private List<String> allDateKeys;

    private final String LOG_TAG = "DataCompletenessCheckAc";

    private final int START_DATE_PICKER_ID = 1;
    private final int END_DATE_PICKER_ID = 2;

    private int datePickerId;

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
        txtv_start_date.setOnClickListener(v -> {
            Log.d(LOG_TAG,"user clicked on the start date");
            datePickerId = START_DATE_PICKER_ID;
            showDatePickerDialog(txtv_start_date.getText().toString());
        });
        txtv_end_date.setOnClickListener(v -> {
            Log.d(LOG_TAG,"user clicked on the end date");
            datePickerId = END_DATE_PICKER_ID;
            showDatePickerDialog(txtv_end_date.getText().toString());
        });

        initRecyclerView();
        initObservers();
    }

    private void initRecyclerView() {
        RecyclerView recyclerView = findViewById(R.id.recv_data_completeness_check);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new DataCompletenessCheckListAdapter(this);
        recyclerView.setAdapter(adapter);
    }

    private void updateList() {

        List<DataCheckMonthly> dataCheckMonthlyList;

        try {
            Calendar startDate = getDateFromTextView(txtv_start_date);
            Calendar endDate = getDateFromTextView(txtv_end_date);
            dataCheckMonthlyList = checkCompleteness(this.allDateKeys, startDate, endDate);
        } catch (Exception e) {
            e.printStackTrace();
            dataCheckMonthlyList = checkCompleteness(this.allDateKeys);
        }

        // update recycler view
        if (!dataCheckMonthlyList.isEmpty() && adapter != null)
            adapter.setData(dataCheckMonthlyList);
    }

    private void initObservers() {
        viewModel.getAllDateKeys().observe(this, allDateKeysList -> {
            this.allDateKeys = allDateKeysList;
            updateList();
        });
    }

    private void showDatePickerDialog(String initialDateString) {

        Log.d(LOG_TAG,"showing DatePicker dialog with initial date \"" + initialDateString + "\"");

        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        Fragment prev = getSupportFragmentManager().findFragmentByTag("dialog");
        if (prev != null) {
            ft.remove(prev);
        }
        ft.addToBackStack(null);
        ft.commit();

        Date initialDate = null;
        try {
            initialDate = sdf_human_readable.parse(initialDateString);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        if (initialDate != null) {
            DialogFragment newFragment = DatePickerFragment.newInstance(initialDate);
            newFragment.show(getSupportFragmentManager(), "datePicker");
        }
    }

    private List<DataCheckMonthly> checkCompleteness(List<String> allDateKeys) {
        // sort Strings in ascending order
        Collections.sort(allDateKeys);

        // create Calendar instances for the first and the last date
        Calendar startDate = Calendar.getInstance();
        Calendar endDate = Calendar.getInstance();
        try {
            startDate.setTime(sdf_date_keys.parse(allDateKeys.get(0)));
            endDate.setTime(sdf_date_keys.parse(allDateKeys.get(allDateKeys.size()-1)));
        } catch (ParseException e) {
            e.printStackTrace();
            return new ArrayList<>();
        }

        return checkCompleteness(allDateKeys, startDate, endDate);
    }


    private List<DataCheckMonthly> checkCompleteness(List<String> allDateKeys, @NonNull Calendar startDate, @NonNull Calendar endDate) {

        if (allDateKeys.isEmpty())
            return new ArrayList<>();

        // create empty String array lists for the found and the missing date keys
        List<String> matchingDateKeys = new ArrayList<>();
        List<String> missingDateKeys = new ArrayList<>();
        List<String> dateKeysInRange = new ArrayList<>();

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
            String currDateKey = sdf_date_keys.format(currDate.getTimeInMillis());
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

    private Calendar getDateFromTextView(TextView textView) throws ParseException {
        Calendar cal = Calendar.getInstance();
        cal.setTime(sdf_human_readable.parse(textView.getText().toString()));
        return cal;
    }

    @Override
    public void onAddDateSubmit(Calendar calendar) {
        String dateSubmitted = sdf_human_readable.format(calendar.getTimeInMillis());
        Log.d(LOG_TAG,"date submitted: " + dateSubmitted);

        if (datePickerId == START_DATE_PICKER_ID) {
            txtv_start_date.setText(dateSubmitted);
            updateList();
        } else if (datePickerId == END_DATE_PICKER_ID) {
            txtv_end_date.setText(dateSubmitted);
            updateList();
        } else
            Log.e(LOG_TAG,"onAddDateSubmit(): datePickerId is not START_DATE_PICKER_ID or END_DATE_PICKER_ID ");
    }
}
