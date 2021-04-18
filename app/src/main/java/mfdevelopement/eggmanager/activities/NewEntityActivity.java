package mfdevelopement.eggmanager.activities;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.snackbar.Snackbar;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import mfdevelopement.eggmanager.DatabaseActions;
import mfdevelopement.eggmanager.R;
import mfdevelopement.eggmanager.data_models.daily_balance.DailyBalance;
import mfdevelopement.eggmanager.data_models.daily_balance.DateKeyUtils;
import mfdevelopement.eggmanager.dialog_fragments.DatePickerFragment;
import mfdevelopement.eggmanager.fragments.DatabaseFragment;
import mfdevelopement.eggmanager.utils.DateFormatter;
import mfdevelopement.eggmanager.utils.InputManager;
import mfdevelopement.eggmanager.viewmodels.NewEntityViewModel;

/**
 * Class for creating an activity to create and edit the data of one day
 */
public class NewEntityActivity extends AppCompatActivity implements DatePickerFragment.OnAddDateListener {

    private static final String LOG_TAG = "NewEntityActivity";

    /**
     * GUI elements
     */
    private TextView dateTextView;
    private EditText eggsCollectedEditText, eggsSoldEditText, pricePerEggEditText, moneyEarnedEditText;
    private ImageButton btn_date_foreward, btn_date_backward;

    /**
     * Date formats
     */
    private final SimpleDateFormat sdf_key = new SimpleDateFormat(DateKeyUtils.DATE_KEY_FORMAT, Locale.getDefault());
    private final SimpleDateFormat sdf_weekday = new SimpleDateFormat("EE, dd.MM.yyyy", Locale.getDefault());

    /**
     * View model
     */
    private NewEntityViewModel newEntityViewModel;

    /**
     * Constants
     */
    private final int NOT_SET = DailyBalance.NOT_SET;
    private final String PRICE_FORMAT = "%.2f";
    private final String STATE_SAVE_KEY_PRICE_PER_EGG = "savedPricePerEgg";

    /**
     * Number for storing the type of request from the calling activity
     */
    private int requestCode;

    /**
     * Variable for storing an object of DailyBalance
     */
    private DailyBalance loadedDailyBalance;

    /**
     *
     */
    private List<String> listDateKeys;

    /**
     * Snackbar to display information to the user
     */
    private Snackbar snackbarEggsCollectedEmpty;

    /**
     * Locale
     */
    private Locale locale;

    /**
     * FragmentActivity
     */
    private FragmentActivity fragmentActivity;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(LOG_TAG, "onCreate called");
        setContentView(R.layout.activity_new_entity);

        fragmentActivity = this;

        Toolbar toolbar = findViewById(R.id.toolbar_new_entity);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        /*
         * Find views in the GUI
         */
        dateTextView = findViewById(R.id.txtv_new_entity_date);
        eggsCollectedEditText = findViewById(R.id.etxt_fetched_eggs);
        eggsSoldEditText = findViewById(R.id.etxt_sold_eggs);
        pricePerEggEditText = findViewById(R.id.etxt_egg_price);
        moneyEarnedEditText = findViewById(R.id.etxt_egg_money_earned);
        btn_date_foreward = findViewById(R.id.imgbtn_date_foreward);
        btn_date_backward = findViewById(R.id.imgbtn_date_backward);

        locale = Locale.ENGLISH;

        // create snackbar
        snackbarEggsCollectedEmpty = Snackbar.make(findViewById(R.id.new_entity_container), getString(R.string.number_eggs_collected_empty), Snackbar.LENGTH_INDEFINITE);

        // set the current date as default user selection
        updateDate(Calendar.getInstance().getTime());

        initOnClickListeners();
        initTextChangeListeners();

        // get view model
        newEntityViewModel = new ViewModelProvider(this).get(NewEntityViewModel.class);

        // set observer for date keys
        newEntityViewModel.getDateKeys().observe(this, strings -> listDateKeys = strings);

        // set keyboard for the price per egg
        //pricePerEggEditText.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);

        // get the price per egg
        Log.d(LOG_TAG, "current text: " + pricePerEggEditText.getText().toString());
        if (pricePerEggEditText.getText().toString().isEmpty()) {
            double pricePerEgg = newEntityViewModel.getPricePerEgg();
            Log.d(LOG_TAG, "loaded price per egg: " + pricePerEgg);
            pricePerEggEditText.setText(String.format(locale, PRICE_FORMAT, pricePerEgg));
        } else {
            Log.d(LOG_TAG, "The price per egg is not set, because the user already entered a value");
        }


        // fill in fields, if a item is edited
        requestCode = getIntent().getIntExtra(DatabaseFragment.EXTRA_REQUEST_CODE_NAME, DatabaseActions.Request.NEW_ENTITY.ordinal());
        Log.d(LOG_TAG, "activity startet with request code " + requestCode);
        if (requestCode == DatabaseActions.Request.EDIT_ENTITY.ordinal()) {
            loadedDailyBalance = (DailyBalance) getIntent().getSerializableExtra(DatabaseFragment.EXTRA_DAILY_BALANCE);
            if (loadedDailyBalance == null) {
                Toast.makeText(this, "Error while loading data", Toast.LENGTH_SHORT).show();
                return;
            }
            String dateKey = loadedDailyBalance.getDateKey();

            eggsCollectedEditText.setText(String.valueOf(loadedDailyBalance.getEggsCollected()));

            // get the saved price per egg and set the String of the EditText
            // The price is rounded to 2 decimal places for displaying
            pricePerEggEditText.setText(String.format(locale, PRICE_FORMAT, loadedDailyBalance.getPricePerEgg()));

            // get the amount f money earned and set the String of the EditText
            double loadedMoneyEarned = loadedDailyBalance.getMoneyEarned();
            // request focus to prevent an update because of an updated
            moneyEarnedEditText.requestFocus();
            moneyEarnedEditText.setText(String.format(locale, PRICE_FORMAT, loadedMoneyEarned));

            // load the number of sold eggs, if there has been a value saved
            // furthermore update the price per egg
            if (loadedDailyBalance.getEggsSold() != NOT_SET) {
                int loadedEggsSold = loadedDailyBalance.getEggsSold();
                eggsSoldEditText.setText(String.valueOf(loadedEggsSold));
                double calcedPricePerEgg = loadedMoneyEarned / loadedEggsSold;
                Log.d(LOG_TAG, "moneyEarnedEditText has content: " + moneyEarnedEditText.getText().toString());
                pricePerEggEditText.setText(String.format(locale, PRICE_FORMAT, calcedPricePerEgg));
                Log.d(LOG_TAG, "moneyEarnedEditText has content: " + moneyEarnedEditText.getText().toString());
            }

            // get the date from the dateKey String and update the TextView containing the date
            Date loadedDate;
            try {
                loadedDate = getDateFromDateKey(dateKey);
            } catch (ParseException e) {
                e.printStackTrace();
                Log.e(LOG_TAG, "Error while parsing dateKey " + dateKey + " to Date");
                Log.e(LOG_TAG, "Using current date instead");
                loadedDate = Calendar.getInstance().getTime();
            }
            updateDate(loadedDate);
        }
        // the user wants to create a new entity
        else if (requestCode == DatabaseActions.Request.NEW_ENTITY.ordinal()) {

            // update the date, if the user wants to create an entity for a special date
            String date = getIntent().getStringExtra(DatabaseFragment.EXTRA_ENTITY_DATE);
            Log.d(LOG_TAG, "starting activity with date: \"" + date + "\"");
            if (date != null) {
                try {
                    updateDate(DateFormatter.parseHumanReadableDateString(date));
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void updateDate(Date date) {
        dateTextView.setText(dateToStringWeekDay(date));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_new_entity, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();// action with ID action_save was selected
        if (itemId == R.id.action_save) {
            saveEntryAndExit();
        } else if (itemId == android.R.id.home) {
            Log.d(LOG_TAG, "User pressed home button");
            onBackPressed();
        }
        return true;
    }

    private void updatePriceColor(String priceString) {
        if (isValidDouble(priceString)) {
            pricePerEggEditText.setTextColor(ContextCompat.getColor(this, R.color.main_text_color));
        } else {
            pricePerEggEditText.setTextColor(ContextCompat.getColor(this, R.color.error));
            Log.d(LOG_TAG, "price per egg is not a valid number: " + priceString);
        }
    }

    private boolean isValidDouble(String priceString) {
        try {
            double price = string2double(priceString);
            return (!Double.isNaN(price) && !Double.isInfinite(price));
        } catch (NumberFormatException e) {
            Log.d(LOG_TAG, String.format("The string \"%s\" cannot be parsed to a double.", priceString));
            return false;
        }
    }

    private double string2double(String string) throws NumberFormatException {
        return Double.parseDouble(string.replace(',', '.'));
    }

    private DailyBalance createDailyBalance() {
        String eggsCollectedString = eggsCollectedEditText.getText().toString();
        String eggsSoldString = eggsSoldEditText.getText().toString();
        String moneyEarnedString = moneyEarnedEditText.getText().toString();
        String pricePerEggString = pricePerEggEditText.getText().toString();
        String username = newEntityViewModel.getUsername();
        Date selectedDate = null;
        try {
            selectedDate = getSelectedDate();
        } catch (ParseException e) {
            e.printStackTrace();
        }

        int eggsCollected = parseInt(eggsCollectedString);
        int eggsSold = parseInt(eggsSoldString);
        double moneyEarned = 0.0;
        if (!moneyEarnedString.isEmpty()) {
            moneyEarned = string2double(moneyEarnedString);
        }

        double pricePerEgg = string2double(pricePerEggString);
        if (moneyEarned != 0.0 && eggsSold != 0) {
            pricePerEgg = moneyEarned / eggsSold;
        }

        DailyBalance dailyBalance = new DailyBalance(dateToString(selectedDate), eggsCollected, eggsSold, pricePerEgg);
        dailyBalance.setUserCreated(username);
        return dailyBalance;
    }

    /**
     * create a Date object from a given date key
     * if the dateKey cannot be parsed, then
     *
     * @param dateKey String containing the dateKey
     * @return Date parsed from the date key
     */
    private Date getDateFromDateKey(String dateKey) throws ParseException {
        return sdf_key.parse(dateKey);
    }

    /**
     * create a date object from a String containing a date in the format EE, dd.MM.yyyy
     *
     * @param dateWeekday String containig the formatted date
     * @return Date object
     * @throws ParseException if the String parameter is not formatted correctly
     */
    private Date getDateFromString(String dateWeekday) throws ParseException {
        return sdf_weekday.parse(dateWeekday);
    }

    /**
     * create a string with date format dd.MM.yyyy from a Date object
     *
     * @param date Date object
     * @return String containing the formatted date
     */
    private String dateToString(Date date) {
        if (date == null)
            return null;
        else
            return sdf_key.format(date);
    }

    /**
     * create a string with date format EE, dd.MM.yyyy from a Date object
     *
     * @param date Date object
     * @return String containing the formatted date
     */
    private String dateToStringWeekDay(Date date) {
        return sdf_weekday.format(date);
    }

    /**
     * converts a string to an int
     * if the conversion fails, then 0 is returned
     *
     * @param string containing the integer number
     * @return int
     */
    private int parseInt(String string) {

        // if the string is empty, then 0 is returned as the user did not enter a value
        if (string.isEmpty())
            return 0;

        try {
            return Integer.parseInt(string);
        } catch (Exception e) {
            return NOT_SET;
        }
    }

    /**
     * save entries to the database and end the activity
     */
    private void saveEntryAndExit() {
        // hide keyboard
        InputManager.hideKeyboard(fragmentActivity);

        if (eggsCollectedEditText.getText().toString().isEmpty()) {
            snackbarEggsCollectedEmpty.show();
            return;
        }

        // check if price per egg is a valid number
        String priceString = pricePerEggEditText.getText().toString();
        if (!isValidDouble(priceString)) {
            Toast.makeText(fragmentActivity, getString(R.string.alter_msg_invalid_price_per_egg), Toast.LENGTH_SHORT).show();
            return;
        }

        // create new DailyBalance
        final DailyBalance dailyBalance = createDailyBalance();

        // if the date key exists, the user needs to decide, if the existing entry should be overwritten
        if (dateKeyExists(dailyBalance.getDateKey())) {
            new AlertDialog.Builder(fragmentActivity)
                    .setTitle(getString(R.string.alter_title_overwrite_entry))
                    .setMessage(getString(R.string.alert_msg_overwrite_entry))

                    // Specifying a sortingOrderChangedListener allows you to take an action before dismissing the dialog.
                    // The dialog is automatically dismissed when a dialog button is clicked.
                    .setPositiveButton(android.R.string.ok, (dialog, which) -> {
                        // Continue with delete operation
                        newEntityViewModel.deleteDailyBalance(loadedDailyBalance);
                        newEntityViewModel.addDailyBalance(dailyBalance);
                        fragmentActivity.setResult(DatabaseActions.Result.ENTITY_EDITED.ordinal());

                        fragmentActivity.finish();
                    })

                    // A null sortingOrderChangedListener allows the button to dismiss the dialog and take no further action.
                    .setNegativeButton(android.R.string.cancel, null)
                    .setIcon(R.drawable.ic_warning_black_24dp)
                    .show();
        }
        // entry with the selected date key does not exist yet
        else {

            // if the user edited an entry and selected a new date which has no data yet, then delete the selected entry and save the entry with the selected date
            if (requestCode == DatabaseActions.Request.EDIT_ENTITY.ordinal()) {
                newEntityViewModel.deleteDailyBalance(loadedDailyBalance);
                fragmentActivity.setResult(DatabaseActions.Result.ENTITY_EDITED.ordinal());
            } else {
                fragmentActivity.setResult(DatabaseActions.Result.NEW_ENTITY.ordinal());
            }

            // save created item
            newEntityViewModel.addDailyBalance(dailyBalance);

            fragmentActivity.finish();
        }
    }

    private boolean dateKeyExists(String dateKey) {
        for (int i = 0; i < listDateKeys.size(); i++) {
            if (listDateKeys.get(i).equals(dateKey)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void onAddDateSubmit(Calendar calendar) {
        updateDate(calendar.getTime());
    }

    private Date getSelectedDate() throws ParseException {
        if (this.dateTextView != null) {
            return sdf_weekday.parse(dateTextView.getText().toString());
        } else {
            Log.e(LOG_TAG, "dateTextView has not been initilized. dateTextView is null.");
            return null;
        }
    }

    /**
     * initialize OnClickListeners
     */
    private void initOnClickListeners() {

        // parse the date from the TextView
        final Date currentDate;
        try {
            currentDate = getSelectedDate();
            if (currentDate == null) {
                throw new Exception(String.format("%s: currentDate == null", LOG_TAG));
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error while loading date information", Toast.LENGTH_SHORT).show();
            onBackPressed();
            return;
        }

        // open dialog to select a date
        dateTextView.setOnClickListener(v -> {
            FragmentTransaction ft = fragmentActivity.getSupportFragmentManager().beginTransaction();
            Fragment prev = fragmentActivity.getSupportFragmentManager().findFragmentByTag("dialog");
            if (prev != null) {
                ft.remove(prev);
            }
            ft.addToBackStack(null);
            ft.commit();

            // Create and show the dialog.
            DialogFragment newFragment = DatePickerFragment.newInstance();
            if (!dateTextView.getText().toString().equals("")) {
                try {
                    newFragment = DatePickerFragment.newInstance(getDateFromString(dateTextView.getText().toString()));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            newFragment.show(fragmentActivity.getSupportFragmentManager(), "datePicker");
        });

        btn_date_foreward.setOnClickListener(v -> {
            Log.d(LOG_TAG, "switch date to the next day");
            Calendar cal = Calendar.getInstance();
            Date selectedDate;
            try {
                selectedDate = getSelectedDate();
            } catch (ParseException e) {
                Log.e(LOG_TAG,"the selected date cannot be parsed. The current date will be used instead.");
                selectedDate = currentDate;
            }
            cal.setTime(selectedDate);
            cal.add(Calendar.DAY_OF_MONTH, 1);
            updateDate(cal.getTime());
        });

        btn_date_backward.setOnClickListener(v -> {
            Log.d(LOG_TAG, "switch date to the previous day");
            Calendar cal = Calendar.getInstance();
            Date selectedDate;
            try {
                selectedDate = getSelectedDate();
            } catch (ParseException e) {
                Log.e(LOG_TAG,"the selected date cannot be parsed. The current date will be used instead.");
                selectedDate = currentDate;
            }
            cal.setTime(selectedDate);
            cal.add(Calendar.DAY_OF_MONTH, -1);
            updateDate(cal.getTime());
        });
    }

    private void changeEditText(EditText editText, String newString) {
        if (!editText.getText().toString().equals(newString)) {
            editText.setText(newString);
        }
    }

    /**
     * initialize TextChangeListeners on EditText Views
     */
    private void initTextChangeListeners() {

        // number of eggs collected
        eggsCollectedEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (!s.toString().isEmpty()) {
                    snackbarEggsCollectedEmpty.dismiss();
                }
            }
        });

        eggsSoldEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                Log.d(LOG_TAG, "updated eggsSoldEditText. New content: " + s.toString());

                if (!s.toString().equals("") && !pricePerEggEditText.getText().toString().equals("") && isValidDouble(pricePerEggEditText.getText().toString())) {
                    int eggsCollected = parseInt(s.toString());
                    double pricePerEgg = string2double(pricePerEggEditText.getText().toString());
                    double moneyEarned = eggsCollected * pricePerEgg;
                    String moneyEarnedString = String.format(locale, PRICE_FORMAT, moneyEarned);

                    if (!moneyEarnedEditText.isFocused()) {
                        changeEditText(moneyEarnedEditText, moneyEarnedString);
                    }
                } else if (s.toString().isEmpty()) {
                    // remove any previous calculated money earned
                    changeEditText(moneyEarnedEditText, "");
                }
            }
        });

        // price per egg
        pricePerEggEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {

                // get the last character
                Character lastChar = ' ';
                if (s.length() > 0) {
                    lastChar = s.charAt(s.length() - 1);
                }

                // check if multiple commas or dots are used, as only one dot or comma can be used for representing a number
                if (s.length() >= 2) {
                    String stringExclLastChar = s.toString().substring(0, s.length() - 1);
                    if ((lastChar.equals(',') || lastChar.equals('.')) && (stringExclLastChar.contains(",") || stringExclLastChar.contains("."))) {
                        Log.d(LOG_TAG, "a comma or dot too much ... The newly added character gets removed.");
                        // set the text to the previous text without the newly added character
                        pricePerEggEditText.setText(stringExclLastChar);
                        // set the cursor to the end of the string
                        pricePerEggEditText.setSelection(pricePerEggEditText.getText().length());
                        return;
                    }
                }

                // replace a comma by a dot to support the soft keyboard in landscape fullscreen mode
                if (lastChar.equals(',')) {
                    pricePerEggEditText.setText(s.toString().replace(",", "."));
                    // set the cursor to the end of the string
                    pricePerEggEditText.setSelection(pricePerEggEditText.getText().length());
                    return;
                }


                Log.d(LOG_TAG, "updated pricePerEggEditText. New content: " + s.toString());

                if (!s.toString().equals("") && isValidDouble(s.toString())) {
                    // update the color of the price
                    updatePriceColor(s.toString());

                    // check if the edit text fields are empty
                    if (eggsSoldEditText.getText().toString().isEmpty() || pricePerEggEditText.getText().toString().isEmpty()) {
                        changeEditText(moneyEarnedEditText, "");
                    } else {
                        // Calculate the amount of money earned and update the edit text field
                        int eggsCollected = parseInt(eggsSoldEditText.getText().toString());
                        double pricePerEgg = string2double(pricePerEggEditText.getText().toString());
                        double moneyEarned = eggsCollected * pricePerEgg;
                        String moneyEarnedString = String.format(locale, PRICE_FORMAT, moneyEarned);

                        if (!moneyEarnedEditText.isFocused()) {
                            changeEditText(moneyEarnedEditText, moneyEarnedString);
                        }
                    }
                } else {
                    changeEditText(moneyEarnedEditText, "");
                }
            }
        });

        moneyEarnedEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                Log.d(LOG_TAG, "updated moneyEarnedEditText. New content: " + s.toString());

                if (!s.toString().equals("") && isValidDouble(s.toString())) {
                    if (!(eggsSoldEditText.getText().toString().isEmpty() || moneyEarnedEditText.getText().toString().isEmpty())) {
                        int eggsSold = parseInt(eggsSoldEditText.getText().toString());
                        double moneyEarned = string2double(moneyEarnedEditText.getText().toString());

                        double pricePerEgg = string2double(pricePerEggEditText.getText().toString());
                        if (eggsSold != 0) {
                            pricePerEgg = moneyEarned / eggsSold;
                        }

                        // change price per egg, if it's a different number now
                        String pricePerEggString = String.format(locale, PRICE_FORMAT, pricePerEgg);

                        if (!pricePerEggEditText.isFocused()) {
                            changeEditText(pricePerEggEditText, pricePerEggString);
                        }
                    }
                }
            }
        });
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        Log.d(LOG_TAG, "saving state");
        outState.putString(STATE_SAVE_KEY_PRICE_PER_EGG, pricePerEggEditText.getText().toString());
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        Log.d(LOG_TAG, "restoring saved state");
        String savedPricePerEggString = savedInstanceState.getString(STATE_SAVE_KEY_PRICE_PER_EGG);
        if (savedPricePerEggString != null) {
            pricePerEggEditText.setText(savedPricePerEggString);
        }
        super.onRestoreInstanceState(savedInstanceState);
    }
}
