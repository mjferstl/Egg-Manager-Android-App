package mfdevelopement.eggmanager.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ExpandableListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import mfdevelopement.eggmanager.DatabaseActions;
import mfdevelopement.eggmanager.R;
import mfdevelopement.eggmanager.data_models.DailyBalance;
import mfdevelopement.eggmanager.data_models.HasDateInterface;
import mfdevelopement.eggmanager.data_models.HasDateInterfaceObject;
import mfdevelopement.eggmanager.data_models.data_check.DataCompletenessChecker;
import mfdevelopement.eggmanager.data_models.expandable_list.GroupInfo;
import mfdevelopement.eggmanager.dialog_fragments.DatePickerFragment;
import mfdevelopement.eggmanager.fragments.DatabaseFragment;
import mfdevelopement.eggmanager.list_adapters.DataCompletenessCheckExpandableListAdapter;
import mfdevelopement.eggmanager.utils.DateFormatter;
import mfdevelopement.eggmanager.viewmodels.DataCheckViewModel;

public class DataCompletenessCheckActivity extends AppCompatActivity implements DatePickerFragment.OnAddDateListener {

    private DataCheckViewModel viewModel;

    private TextView txtv_start_date, txtv_end_date, txtv_info_no_data_missing;

    private final String LOG_TAG = "DataCompletenessCheckAc";

    /**
     * Strings which are used for storing the UI data during orientation changes etc.
     */
    private final String KEY_START_DATE = "startDate";
    private final String KEY_END_DATE = "endDate";

    // Constants to determine the date, which the user wants to change
    private final int START_DATE_PICKER_ID = 1;
    private final int END_DATE_PICKER_ID = 2;

    private int datePickerId;

    private DataCompletenessCheckExpandableListAdapter expandableListAdapter;

    private ExpandableListView expandableListView;

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
            Log.d(LOG_TAG, "user clicked on the start date");
            datePickerId = START_DATE_PICKER_ID;
            showDatePickerDialog(txtv_start_date.getText().toString());
        });
        txtv_end_date.setOnClickListener(v -> {
            Log.d(LOG_TAG, "user clicked on the end date");
            datePickerId = END_DATE_PICKER_ID;
            showDatePickerDialog(txtv_end_date.getText().toString());
        });

        txtv_info_no_data_missing = findViewById(R.id.txtv_no_missing_data);
        txtv_info_no_data_missing.setVisibility(View.GONE);

        initObservers();
        initListView();
    }

    private void initListView() {
        expandableListView = findViewById(R.id.elv_data_completeness_check);
        expandableListAdapter = new DataCompletenessCheckExpandableListAdapter(getApplicationContext(), new ArrayList<>());
        expandableListView.setAdapter(expandableListAdapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateList();
    }

    private void updateList() {
        if (viewModel.getAllDateKeys().getValue() != null)
            updateList(convertDateKeys(viewModel.getAllDateKeys().getValue()));
    }

    private void updateList(List<Date> dateList) {

        if (txtv_start_date.getText().toString().isEmpty())
            txtv_start_date.setText(DateFormatter.getHumanReadableDate(dateList.get(0)));
        if (txtv_end_date.getText().toString().isEmpty())
            txtv_end_date.setText(DateFormatter.getHumanReadableDate(dateList.get(dateList.size() - 1)));

        try {
            // parse the dates from the text views
            Calendar startDate = getDateFromTextView(txtv_start_date);
            Calendar endDate = getDateFromTextView(txtv_end_date);

            // create a list with objects implementing the HasDateInterface
            List<HasDateInterface> hasDateInterfaceObjectList = new ArrayList<>();
            for (Date d : dateList)
                hasDateInterfaceObjectList.add(HasDateInterfaceObject.createFromDate(d));

            // Check the data for completeness within the start date and end date
            DataCompletenessChecker dcc = new DataCompletenessChecker(startDate, endDate, hasDateInterfaceObjectList);
            List<GroupInfo> groupInfoList = dcc.getGroupInfoList();

            // update the expandable list view
            expandableListAdapter.setData(groupInfoList);

            // hide the list, if it is empty
            if (expandableListAdapter.getGroupCount() == 0) {
                expandableListView.setVisibility(View.GONE);
                txtv_info_no_data_missing.setVisibility(View.VISIBLE);
            } else {
                expandableListView.setVisibility(View.VISIBLE);
                txtv_info_no_data_missing.setVisibility(View.GONE);
            }

        } catch (ParseException e) {
            String errorMsg = "Cannot parse \"" + txtv_start_date.getText().toString() + "\" and \"" + txtv_end_date.getText().toString() + "\" to dates";
            Toast.makeText(this.getApplicationContext(), errorMsg, Toast.LENGTH_LONG).show();
            Log.e(LOG_TAG, errorMsg);
            e.printStackTrace();
        }
    }

    private List<Date> convertDateKeys(List<String> dateKeyList) {
        List<Date> dateList = new ArrayList<>();
        for (String s : dateKeyList) {
            try {
                dateList.add(DailyBalance.getDateByDateKey(s));
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        return dateList;
    }

    private void initObservers() {
        viewModel.getAllDateKeys().observe(this, allDateKeysList -> updateList(convertDateKeys(allDateKeysList)));
    }

    private void showDatePickerDialog(String initialDateString) {

        Log.d(LOG_TAG, "showing DatePicker dialog with initial date \"" + initialDateString + "\"");

        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        Fragment prev = getSupportFragmentManager().findFragmentByTag("dialog");
        if (prev != null) {
            ft.remove(prev);
        }
        ft.addToBackStack(null);
        ft.commit();

        Date initialDate = null;
        try {
            initialDate = DateFormatter.parseHumanReadableDateString(initialDateString);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        if (initialDate != null) {
            DialogFragment newFragment = DatePickerFragment.newInstance(initialDate);
            newFragment.show(getSupportFragmentManager(), "datePicker");
        }
    }

    private Calendar getDateFromTextView(TextView textView) throws ParseException {
        Calendar cal = Calendar.getInstance();
        cal.setTime(DateFormatter.parseHumanReadableDateString(textView.getText().toString()));
        return cal;
    }

    @Override
    public void onAddDateSubmit(Calendar calendar) {
        String dateSubmitted = DateFormatter.getHumanReadableDate(calendar);
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

    private void createNewEntity(String dateString) {
        Intent intent = new Intent(this, NewEntityActivity.class);
        intent.putExtra(DatabaseFragment.EXTRA_REQUEST_CODE_NAME, DatabaseActions.Request.NEW_ENTITY.ordinal());
        intent.putExtra(DatabaseFragment.EXTRA_ENTITY_DATE, dateString);
        startActivityForResult(intent, DatabaseActions.Request.NEW_ENTITY.ordinal());
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        // save the current specified start date from the UI
        if (txtv_start_date != null) {
            outState.putString(KEY_START_DATE, txtv_start_date.getText().toString());
        }
        // save the current specified end date from the UI
        if (txtv_end_date != null) {
            outState.putString(KEY_END_DATE, txtv_end_date.getText().toString());
        }
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        // get the saved start date and update the text view
        String savedStartDate = savedInstanceState.getString(KEY_START_DATE, null);
        if (savedStartDate != null) {
            txtv_start_date.setText(savedStartDate);
        }
        // get the saved end date and update the text view
        String savedEndDate = savedInstanceState.getString(KEY_END_DATE, null);
        if (savedEndDate != null) {
            txtv_end_date.setText(savedEndDate);
        }
        super.onRestoreInstanceState(savedInstanceState);
    }
}
