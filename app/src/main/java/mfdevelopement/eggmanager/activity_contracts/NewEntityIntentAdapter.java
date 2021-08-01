package mfdevelopement.eggmanager.activity_contracts;

import android.content.Context;
import android.content.Intent;

import androidx.annotation.NonNull;

import mfdevelopement.eggmanager.DatabaseActions;
import mfdevelopement.eggmanager.activities.NewEntityActivity;
import mfdevelopement.eggmanager.data_models.daily_balance.DailyBalance;
import mfdevelopement.eggmanager.fragments.DatabaseFragment;

public class NewEntityIntentAdapter {

    /*
     * Variable for storing the code of the request
     */
    private final long requestCode;

    /*
     * Variable for storing the {@link DailyBalance} object to pass to the activity
     */
    private DailyBalance dailyBalance = null;

    /**
     * Default constructor for creating an intent to create a new database entity
     */
    public NewEntityIntentAdapter() {
        this(DatabaseActions.Request.NEW_ENTITY);
    }

    /**
     * Constructor to specify the request type to pass to NewEntityActivity
     *
     * @param request type of request
     */
    public NewEntityIntentAdapter(final DatabaseActions.Request request) {
        this.requestCode = request.id;
    }

    /**
     * Constructor with a DailyBalance object as argument.
     * This automatically sets the request code to EDIT_ENTITY
     *
     * @param data DailyBalance object to edit
     */
    public NewEntityIntentAdapter(@NonNull final DailyBalance data) {
        this.requestCode = DatabaseActions.Request.EDIT_ENTITY.id;
        this.dailyBalance = data;
    }

    /**
     * Constructor to specify the request as well as an additional DailyBalance object to pass to NewEntityActivity
     *
     * @param request type of request
     * @param data    DailyBalance object to pass to the activity
     */
    public NewEntityIntentAdapter(final DatabaseActions.Request request, @NonNull final DailyBalance data) {
        this.requestCode = request.id;
        this.dailyBalance = data;
    }

    /**
     * Convert to an object of class {@link Intent}
     *
     * @param context {@link Context}
     * @return {@link Intent}
     */
    public Intent toIntent(@NonNull Context context) {
        Intent intent = new Intent(context, NewEntityActivity.class);
        intent.putExtra(DatabaseFragment.EXTRA_REQUEST_CODE_NAME, this.requestCode);
        if (this.dailyBalance != null) {
            intent.putExtra(DatabaseFragment.EXTRA_DAILY_BALANCE, this.dailyBalance);
        }
        return intent;
    }
}
