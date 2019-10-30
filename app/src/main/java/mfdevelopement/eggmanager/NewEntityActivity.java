package mfdevelopement.eggmanager;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class NewEntityActivity extends AppCompatActivity implements DatePickerFragment.OnAddDateListener {

    private static final String LOG_TAG = "NewEntityActivity";
    private Date selectedDate;

    private TextView dateTextView;
    private EditText eggsCollectedEditText, eggsSoldEditText, pricePerEggEditText;
    private final SimpleDateFormat sdf_key = new SimpleDateFormat(DailyBalance.dateKeyFormat, Locale.getDefault());
    private final SimpleDateFormat sdf_weekday = new SimpleDateFormat("EE, dd.MM.yyyy", Locale.getDefault());
    private NewEntityViewModel newEntityViewModel;
    private final int NOT_SET = -1;
    private final String PRICE_FORMAT = "%.2f";
    private int requestCode;
    private DailyBalance loadedDailyBalance;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_entity);

        dateTextView = findViewById(R.id.txtv_new_entity_date);
        eggsCollectedEditText = findViewById(R.id.etxt_fetched_eggs);
        eggsSoldEditText = findViewById(R.id.etxt_sold_eggs);
        pricePerEggEditText = findViewById(R.id.etxt_egg_price);

        // set the current date as default user selection
        Calendar currentDate = Calendar.getInstance();
        updateDate(currentDate.getTime());

        // open dialog to select a date
        dateTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
                Fragment prev = getSupportFragmentManager().findFragmentByTag("dialog");
                if (prev != null) {
                    ft.remove(prev);
                }
                ft.addToBackStack(null);
                ft.commit();

                // Create and show the dialog.
                DialogFragment newFragment = DatePickerFragment.newInstance();
                //newFragment.setTargetFragment(this, 1);
                newFragment.show(getSupportFragmentManager(), "datePicker");
            }
        });

        // text change listener pro price per egg
        pricePerEggEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                updatePriceColor(s.toString());
            }
        });

        newEntityViewModel = new ViewModelProvider(this).get(NewEntityViewModel.class);
        double pricePerEgg = newEntityViewModel.getPricePerEgg();
        pricePerEggEditText.setText(String.format(Locale.getDefault(), PRICE_FORMAT, pricePerEgg));

        // fill in fields, if a item is edited
        requestCode = getIntent().getIntExtra(MainActivity.EXTRA_REQUEST_CODE_NAME,MainActivity.NEW_ENTITY_ACTIVITY_REQUEST_CODE);
        Log.d(LOG_TAG,"activity startet with request code " + requestCode);
        if (requestCode == MainActivity.EDIT_ENTITY_ACTIVITY_REQUEST_CODE) {
            loadedDailyBalance = (DailyBalance)getIntent().getSerializableExtra("dd");
            String dateKey = loadedDailyBalance.getDateKey();

            eggsCollectedEditText.setText(String.valueOf(loadedDailyBalance.getEggsCollected()));
            pricePerEggEditText.setText(String.format(Locale.getDefault(),PRICE_FORMAT,loadedDailyBalance.getPricePerEgg()));
            if (loadedDailyBalance.getEggsSold() != NOT_SET)
                eggsSoldEditText.setText(String.valueOf(loadedDailyBalance.getEggsSold()));

            // get the date from the dateKey String and update the TextView containing the date
            Date loadedDate;
            try {
                loadedDate = getDateFromDateKey(dateKey);
            } catch (ParseException e) {
                e.printStackTrace();
                Log.e(LOG_TAG,"Error while parsing dateKey " + dateKey + " to Date");
                Log.e(LOG_TAG,"Using current date instead");
                loadedDate = Calendar.getInstance().getTime();
            }
            updateDate(loadedDate);
        }
    }

    private void updateDate(Date date) {
        selectedDate = date;
        dateTextView.setText(dateToStringWeekDay(date));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_new_entity, menu);
        return true;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if (requestCode == MainActivity.EDIT_ENTITY_ACTIVITY_REQUEST_CODE)
            Toast.makeText(getApplicationContext(),R.string.changes_not_saved,Toast.LENGTH_SHORT).show();
        else
            Toast.makeText(getApplicationContext(),R.string.empty_not_saved,Toast.LENGTH_SHORT).show();
        finish();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // action with ID action_save was selected
            case R.id.action_save:

                // hide keyboard
                InputManager.hideKeyboard(this);

                // check if price per egg is a valid number
                String priceString = pricePerEggEditText.getText().toString();
                if (!isValidPrice(priceString)) {
                    Toast.makeText(this, "Der eingegebene Preis pro Ei ist ungültig", Toast.LENGTH_SHORT).show();
                    break;
                }

                // delete loaded item
                if (requestCode == MainActivity.EDIT_ENTITY_ACTIVITY_REQUEST_CODE)
                    newEntityViewModel.deleteDailyBalance(loadedDailyBalance);
                // save created item
                DailyBalance dailyBalance = createDailyBalance();
                newEntityViewModel.addDailyBalance(dailyBalance);

                Toast.makeText(getApplicationContext(), R.string.new_entity_saved,Toast.LENGTH_SHORT).show();

                // send intent with result to parent activity
                Intent replyIntent = new Intent();
                setResult(RESULT_OK, replyIntent);

                // finish activity
                finish();
                break;
            default:
                break;
        }
        return true;
    }

    private void updatePriceColor(String priceString) {
        if (isValidPrice(priceString)) {
            pricePerEggEditText.setTextColor(ContextCompat.getColor(this, R.color.main_text_color));
            Log.d(LOG_TAG, "price per egg is a valid number. price = " + priceString);
        } else {
            pricePerEggEditText.setTextColor(ContextCompat.getColor(this, R.color.error));
            Log.d(LOG_TAG, "price per egg is not a valid number.");
        }
    }

    private boolean isValidPrice(String priceString) {
        double price = createValidDouble(priceString);
        return (price != 0.00);
    }

    private double createValidDouble(String string) {
        return Double.parseDouble(string.replace(',', '.'));
    }

    private DailyBalance createDailyBalance() {
        String eggsCollectedString = eggsCollectedEditText.getText().toString();
        String eggsSoldString = eggsSoldEditText.getText().toString();
        String pricePerEggString = pricePerEggEditText.getText().toString();

        int eggsCollected = parseInt(eggsCollectedString);
        int eggsSold = parseInt(eggsSoldString);
        double pricePerEgg = createValidDouble(pricePerEggString);

        return new DailyBalance(dateToString(selectedDate), eggsCollected, eggsSold, pricePerEgg);
    }

    /**
     * create a Date object from a given date key
     * if the dateKey cannot be parsed, then
     * @param dateKey String containing the dateKey
     * @return Date parsed from the date key
     */
    private Date getDateFromDateKey(String dateKey) throws ParseException{
        return sdf_key.parse(dateKey);
    }

    private String dateToString(Date date) {
        return sdf_key.format(date);
    }

    private String dateToStringWeekDay(Date date) { return sdf_weekday.format(date); }

    /**
     * converts a string to an int
     * if the conversion fails, then 0 is returned
     * @param string containing the integer number
     * @return int
     */
    private int parseInt(String string) {
        try {
            return Integer.parseInt(string);
        } catch (Exception e) {
            return NOT_SET;
        }
    }

    @Override
    public void onAddDateSubmit(Calendar calendar) {
        updateDate(calendar.getTime());
    }
}
