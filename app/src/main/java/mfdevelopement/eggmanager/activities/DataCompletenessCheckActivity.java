package mfdevelopement.eggmanager.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ExpandableListView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
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
import mfdevelopement.eggmanager.data_models.HasDateInterface;
import mfdevelopement.eggmanager.data_models.HasDateInterfaceObject;
import mfdevelopement.eggmanager.data_models.daily_balance.DateKeyUtils;
import mfdevelopement.eggmanager.data_models.data_check.DataCompletenessChecker;
import mfdevelopement.eggmanager.data_models.expandable_list.ChildInfo;
import mfdevelopement.eggmanager.data_models.expandable_list.GroupInfo;
import mfdevelopement.eggmanager.dialog_fragments.DatePickerFragment;
import mfdevelopement.eggmanager.fragments.DatabaseFragment;
import mfdevelopement.eggmanager.list_adapters.DataCompletenessCheckExpandableListAdapter;
import mfdevelopement.eggmanager.utils.DateFormatter;
import mfdevelopement.eggmanager.viewmodels.DataCheckViewModel;

public class DataCompletenessCheckActivity extends AppCompatActivity implements DatePickerFragment.OnAddDateListener {

    /**
     * String used as identifier for logging
     */
    private final String LOG_TAG = "DataCompletenessCheckAc";
    /**
     * Strings which are used for storing the UI data during orientation changes etc.
     */
    private final String KEY_START_DATE = "startDate";
    private final String KEY_END_DATE = "endDate";

    /**
     * ActivityResultLauncher to start another activity
     * call with mStartActivityWithIntent.launch(new Intent())
     */
    private final ActivityResultLauncher<Intent> mStartActivityWithIntent = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    Log.d(LOG_TAG, "received RESULT_OK as activity result");
                    //Intent intent = result.getData();
                    // Handle the Intent
                    // nothing to handle yet
                }
            });
    /**
     * View model for the activity
     */
    private DataCheckViewModel viewModel;
    /**
     * TextViews used in the UI
     */
    private TextView txtv_start_date;
    private TextView txtv_end_date;
    private TextView txtv_elv_info;
    private LinearLayout linearLayoutListContainer, linearLayoutSuccessContainer;
    private PICKER_ID pickerId;
    private DataCompletenessCheckExpandableListAdapter expandableListAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_data_completeness_check);

        Toolbar toolbar = findViewById(R.id.toolbar_data_check);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        viewModel = new ViewModelProvider(this).get(DataCheckViewModel.class);

        linearLayoutListContainer = findViewById(R.id.linLay_data_completeness_check_list_container);
        linearLayoutSuccessContainer = findViewById(R.id.linLay_data_completeness_check_success_container);

        //
        initTextViews();
        initObservers();
        initExpandableListView();
    }

    /**
     * Initialize the {@link TextView} objects of the UI
     */
    private void initTextViews() {
        txtv_start_date = findViewById(R.id.txtv_data_check_start_date);
        txtv_end_date = findViewById(R.id.txtv_data_check_end_date);
        txtv_start_date.setOnClickListener(v -> {
            Log.d(LOG_TAG, "user clicked on the start date");
            pickerId = PICKER_ID.START_DATE;
            showDatePickerDialog(txtv_start_date.getText().toString());
        });
        txtv_end_date.setOnClickListener(v -> {
            Log.d(LOG_TAG, "user clicked on the end date");
            pickerId = PICKER_ID.END_DATE;
            showDatePickerDialog(txtv_end_date.getText().toString());
        });

        txtv_elv_info = findViewById(R.id.txtv_data_completeness_check_list_info);
    }

    /**
     * Initialize the {@link ExpandableListView} in the UI
     */
    private void initExpandableListView() {
        ExpandableListView expandableListView = findViewById(R.id.elv_data_completeness_check);
        expandableListAdapter = new DataCompletenessCheckExpandableListAdapter(this, new ArrayList<>());
        expandableListAdapter.addOnChildAddButtonClickListener((groupPosition, childPosition) -> {
            GroupInfo gi = expandableListAdapter.getGroup(groupPosition);
            Calendar groupDate = DataCompletenessChecker.convertToDate(gi);

            ChildInfo ci = expandableListAdapter.getChild(groupPosition, childPosition);
            Calendar childDate = DataCompletenessChecker.convertToDate(ci);

            Calendar cal = Calendar.getInstance();
            cal.set(groupDate.get(Calendar.YEAR), groupDate.get(Calendar.MONTH), childDate.get(Calendar.DAY_OF_MONTH));

            String dateKey = DateFormatter.getHumanReadableDate(cal);

            // Create a new entity
            createNewEntity(dateKey);
        });
        expandableListView.setAdapter(expandableListAdapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateListContent();
    }

    /**
     * Update the content of the {@link ExpandableListView}
     * The content for the adapter of the {@link ExpandableListView} will be queried from the {@link DataCheckViewModel}
     */
    private void updateListContent() {
        if (viewModel.getAllDateKeys().getValue() != null) {
            updateListContent(convertDateKeys(viewModel.getAllDateKeys().getValue()));
        } else {
            Log.e(LOG_TAG, "viewModel.getAllDateKeys().getValue() is null");
        }
    }

    /**
     * Update the content of the {@link ExpandableListView}
     *
     * @param dateList list of {@link Date} objects which represent the dates, where a database entry exists
     */
    private void updateListContent(List<Date> dateList) {

        if (txtv_start_date.getText().toString().isEmpty())
            txtv_start_date.setText(DateFormatter.getHumanReadableDate(dateList.get(0)));
        if (txtv_end_date.getText().toString().isEmpty()) {
            Date dateListLastDate = dateList.get(dateList.size() - 1);
            Calendar now = Calendar.getInstance();

            // check if the timestamp of the last entry is beyond now
            if (dateListLastDate.getTime() > now.getTimeInMillis()) {
                txtv_end_date.setText(DateFormatter.getHumanReadableDate(dateListLastDate));
            } else {
                txtv_end_date.setText(DateFormatter.getHumanReadableDate(now));
            }
        }

        try {
            // parse the dates from the text views
            Calendar startDate = getDateFromTextView(txtv_start_date);
            Calendar endDate = getDateFromTextView(txtv_end_date);

            // create a list with objects implementing the HasDateInterface
            List<HasDateInterface> hasDateInterfaceObjectList = new ArrayList<>();
            for (Date d : dateList) {
                hasDateInterfaceObjectList.add(HasDateInterfaceObject.createFromDate(d));
            }

            // Check the data for completeness within the start date and end date
            DataCompletenessChecker dcc = new DataCompletenessChecker(startDate, endDate, hasDateInterfaceObjectList);
            List<GroupInfo> groupInfoList = dcc.getGroupInfoList();

            // update the expandable list view
            expandableListAdapter.setData(groupInfoList);

            // Update the text for the info text
            long daysCount = 0;
            for (GroupInfo gi : groupInfoList) daysCount += gi.getChildCount();
            String infoText = String.format(getString(R.string.data_check_info), daysCount);
            txtv_elv_info.setText(infoText);

            // hide the list, if it is empty
            if (expandableListAdapter.getGroupCount() == 0) {
                linearLayoutListContainer.setVisibility(View.GONE);
                linearLayoutSuccessContainer.setVisibility(View.VISIBLE);
            } else {
                linearLayoutListContainer.setVisibility(View.VISIBLE);
                linearLayoutSuccessContainer.setVisibility(View.GONE);
            }

        } catch (ParseException e) {
            String errorMsg = "Cannot parse \"" + txtv_start_date.getText().toString() + "\" and \"" + txtv_end_date.getText().toString() + "\" to dates";
            Toast.makeText(this.getApplicationContext(), errorMsg, Toast.LENGTH_LONG).show();
            Log.e(LOG_TAG, errorMsg);
            e.printStackTrace();
        }
    }

    /**
     * Convert a list of dateKeys to {@link Date} objects
     *
     * @param dateKeyList list of dateKeys
     * @return converted dateKeys
     */
    private List<Date> convertDateKeys(List<String> dateKeyList) {
        List<Date> dateList = new ArrayList<>();
        for (String s : dateKeyList) {
            try {
                dateList.add(DateKeyUtils.getDateByDateKey(s));
            } catch (ParseException e) {
                throw new RuntimeException(e);
            }
        }
        return dateList;
    }

    /**
     * Initialize all observers
     */
    private void initObservers() {
        viewModel.getAllDateKeys().observe(this, allDateKeysList -> updateListContent(convertDateKeys(allDateKeysList)));
    }

    /**
     * Show a {@link DatePickerFragment} for the user to select a date
     *
     * @param initialDateString initial date, which the {@link DatePickerFragment} will show
     */
    private void showDatePickerDialog(String initialDateString) {

        Log.d(LOG_TAG, "showing DatePicker dialog with initial date \"" + initialDateString + "\"");

        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        Fragment prev = getSupportFragmentManager().findFragmentByTag("dialog");
        if (prev != null) {
            ft.remove(prev);
        }
        ft.addToBackStack(null);
        ft.commit();

        try {
            Date initialDate = DateFormatter.parseHumanReadableDateString(initialDateString);
            DialogFragment dialogFragment = DatePickerFragment.newInstance(initialDate);
            dialogFragment.show(getSupportFragmentManager(), "datePicker");
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    /**
     * Convert the text of a {@link TextView} to a {@link Calendar} object
     *
     * @param textView {@link TextView} which contains the date as String in the format {@code DateFormatter.HUMAN_READABLE_DATE_FORMAT}
     * @return Calendar object, which has the time set to the date parsed from {@code textView}
     * @throws ParseException if the content of {@code textView} is not in the format {@code DateFormatter.HUMAN_READABLE_DATE_FORMAT} or cannot be parsed for other reasons
     */
    private Calendar getDateFromTextView(TextView textView) throws ParseException {
        Calendar cal = Calendar.getInstance();
        cal.setTime(DateFormatter.parseHumanReadableDateString(textView.getText().toString()));
        return cal;
    }

    @Override
    public void onAddDateSubmit(Calendar calendar) {
        String dateSubmitted = DateFormatter.getHumanReadableDate(calendar);
        Log.d(LOG_TAG, "date submitted: " + dateSubmitted);

        if (pickerId == PICKER_ID.START_DATE) {
            txtv_start_date.setText(dateSubmitted);
        } else if (pickerId == PICKER_ID.END_DATE) {
            txtv_end_date.setText(dateSubmitted);
        }
        updateListContent();
    }

    private void createNewEntity(String dateString) {
        Intent intent = new Intent(this, NewEntityActivity.class);
        intent.putExtra(DatabaseFragment.EXTRA_REQUEST_CODE_NAME, DatabaseActions.Request.NEW_ENTITY.id);
        intent.putExtra(DatabaseFragment.EXTRA_ENTITY_DATE, dateString);

        mStartActivityWithIntent.launch(intent);
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

    /**
     * Enum to determine, which date the user wants to change
     */
    enum PICKER_ID {
        START_DATE,
        END_DATE
    }
}
