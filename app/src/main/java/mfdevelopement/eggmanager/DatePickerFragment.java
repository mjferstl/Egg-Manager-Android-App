package mfdevelopement.eggmanager;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;

import java.util.Calendar;
import java.util.Date;

public class DatePickerFragment extends DialogFragment {

    private final String LOG_TAG = "DatePickerFragment";
    private static Date initialDate;

    // Define the listener of the interface type
    // listener will the activity instance containing fragment
    private OnAddDateListener listener;


    // interface for updating parent activity
    public interface OnAddDateListener {
        void onAddDateSubmit(Calendar calendar);
    }

    public DatePickerFragment() {
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof OnAddDateListener) {
            listener = (OnAddDateListener) context;
        } else {
            throw new ClassCastException(context.toString()
                    + " must implement DatePickerFragment.OnAddDateListener");
        }
    }

    public static DatePickerFragment newInstance() {
        return new DatePickerFragment();
    }

    public static DatePickerFragment newInstance(Date date) {
        initialDate = date;
        return new DatePickerFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_date_picker, container);

        final DatePicker datePicker = v.findViewById(R.id.datepicker);

        if (initialDate != null) {
            Calendar cal = Calendar.getInstance();
            cal.setTime(initialDate);
            datePicker.updateDate(cal.get(Calendar.YEAR),cal.get(Calendar.MONTH),cal.get(Calendar.DAY_OF_MONTH));
        }

        Button btn_cancel = v.findViewById(R.id.btn_datepicker_cancel);
        btn_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(LOG_TAG,"Dialog canceled");
                dismiss();
            }
        });

        Button btn_ok = v.findViewById(R.id.btn_datepicker_ok);
        btn_ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar cal = getDatePickerDate(datePicker);
                listener.onAddDateSubmit(cal);
                Log.d(LOG_TAG,"Date selected");
                dismiss();
            }
        });
        return v;
    }

    private Calendar getDate(int year, int month, int dayOfMonth) {
        Calendar c = Calendar.getInstance();
        c.set(year, month, dayOfMonth);
        return c;
    }

    private Calendar getDatePickerDate(DatePicker datePicker) {
        return getDate(datePicker.getYear(), datePicker.getMonth(), datePicker.getDayOfMonth());
    }
}
