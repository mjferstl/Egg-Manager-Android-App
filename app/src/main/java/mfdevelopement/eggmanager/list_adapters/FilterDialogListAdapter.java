package mfdevelopement.eggmanager.list_adapters;

import android.content.Context;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.Arrays;
import java.util.List;

import mfdevelopement.eggmanager.R;
import mfdevelopement.eggmanager.dialog_fragments.FilterDialogFragment;

public class FilterDialogListAdapter extends RecyclerView.Adapter<FilterDialogListAdapter.FilterDialogViewHolder> {

    private final String LOG_TAG = "FilterDialogListAdaptYe";
    private String initialFilterString = FilterDialogFragment.NOT_SET_FILTER_STRING;
    private final String notSetFilterString = initialFilterString + initialFilterString;

    private List<String> filterList, monthNames;

    private final LayoutInflater mInflater;

    private OnFilterSelectListener listener;

    private Context parentContext;

    private SparseBooleanArray sSelectedItems;


    // interface for updating parent activity
    public interface OnFilterSelectListener {
        void onFilterSelected(String filterString, int buttonPosition, boolean isSelected);
    }

    public class FilterDialogViewHolder extends RecyclerView.ViewHolder{
        private Button btn;

        FilterDialogViewHolder(View itemView) {
            super(itemView);
            btn = itemView.findViewById(R.id.txtv_recycler_item_filter_dialog_year);
        }
    }

    public void setOnItemClickListener(OnFilterSelectListener clickListener) {
        listener = clickListener;
    }

    public FilterDialogListAdapter(Context context) {
        mInflater = LayoutInflater.from(context);
        monthNames = Arrays.asList(context.getResources().getStringArray(R.array.month_names));
        parentContext = context;

        if (context instanceof OnFilterSelectListener) {
            listener = (OnFilterSelectListener) context;
        } else {
            throw new ClassCastException(context.toString()
                    + " must implement FilterDialogListAdapter.OnFilterSelectListener");
        }

        sSelectedItems = new SparseBooleanArray();
    }

    public FilterDialogListAdapter(Context context, String currentFilterString) {
        // set inital filter string
        initialFilterString = currentFilterString;

        mInflater = LayoutInflater.from(context);
        monthNames = Arrays.asList(context.getResources().getStringArray(R.array.month_names));
        parentContext = context;

        if (context instanceof OnFilterSelectListener) {
            listener = (OnFilterSelectListener) context;
        } else {
            throw new ClassCastException(context.toString()
                    + " must implement FilterDialogListAdapter.OnFilterSelectListener");
        }

        sSelectedItems = new SparseBooleanArray();
    }

    @Override
    @NonNull
    public FilterDialogViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = mInflater.inflate(R.layout.recyclerview_filter_dialog_item, parent, false);
        return new FilterDialogViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull final FilterDialogViewHolder holder, final int position) {
        if (filterList != null) {
            final String current = filterList.get(position);

            if (!isNumeric(current)) return;

            // if item is a year
            if (current.length() == 4) {
                holder.btn.setText(current);
                holder.btn.setContentDescription(current);

                // if the initalFilterString is targeting the current year-button or a month of this year, the button state is changed to "pressed"
                if (current.equals(initialFilterString) || (initialFilterString.length() == 6 && current.equals(initialFilterString.substring(0,4)))) {
                    sSelectedItems.put(position,true);
                }
            }
            // item is a month name
            else if (current.length() == 6) {
                int monthIndex = Integer.valueOf(current.substring(4,6))-1;
                holder.btn.setText(monthNames.get(monthIndex));
                holder.btn.setContentDescription(current);

                // if the initalFilterString is targeting the current button, then the button state is changed to "pressed"
                if (current.equals(initialFilterString)) {
                    sSelectedItems.put(position,true);
                }
            } else {
                Log.e(LOG_TAG, "onBindViewHolder::String has not 4 or 6 characters; current = " + current);
            }

            if (sSelectedItems.get(position,false)) {
                setButtonPressed(holder.btn);
            } else {
                setButtonNotPressed(holder.btn);
            }

            holder.btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.onFilterSelected(current,position,!holder.btn.isSelected());

                    // set initialFilterString to NOT_SET to prevent a button being selected although the user has manually chosen a different one
                    // the initialFilterString is only used to display the first selection
                    initialFilterString = FilterDialogFragment.NOT_SET_FILTER_STRING;
                }
            });
        } else {
            Log.e(LOG_TAG,"filterList = null");
        }
    }

    private void setButtonPressed(Button btn) {
        btn.setSelected(true);
        btn.setTextColor(parentContext.getResources().getColor(R.color.colorAccent));
    }

    private void setButtonNotPressed(Button btn) {
        btn.setSelected(false);
        btn.setTextColor(parentContext.getResources().getColor(R.color.colorPrimary));
    }

    public void setFilterStrings(List<String> yearMonthFilterList) {
        if (filterList != yearMonthFilterList) {
            filterList = yearMonthFilterList;
            notifyDataSetChanged();
            Log.d(LOG_TAG, "setFilterStrings::notifyDataSetChanged");
        }
    }

    public void setNoButtonSelected() {
        sSelectedItems.clear();
        notifyDataSetChanged();
        initialFilterString = notSetFilterString;
    }

    public void updateButtons(int posSelectedButton) {
        if (!sSelectedItems.get(posSelectedButton)) {
            sSelectedItems.clear();
            sSelectedItems.put(posSelectedButton, true);
        } else {
            sSelectedItems.clear();
        }
        notifyDataSetChanged();
    }

    // getItemCount() is called many times, and when it is first called,
    // mWords has not been updated (means initially, it's null, and we can't return null).
    @Override
    public int getItemCount() {
        if (filterList != null)
            return filterList.size();
        else return 0;
    }

    private boolean isNumeric(String string) {
        try {
            Integer.valueOf(string);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}
