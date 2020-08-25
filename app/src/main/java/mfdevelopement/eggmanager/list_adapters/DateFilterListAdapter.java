package mfdevelopement.eggmanager.list_adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import mfdevelopement.eggmanager.R;

public class DateFilterListAdapter extends RecyclerView.Adapter<DateFilterListAdapter.DateFilterViewHolder> {

    private List<String> dateStrings;
    private String currentSelection = "";
    private final String LOG_TAG = "DateFilterListAdapter";

    private final LayoutInflater mInflater;

    private OnButtonClickListener listener;

    public interface OnButtonClickListener {
        void OnButtonClicked(String buttonText, boolean isSelected);
    }

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    static class DateFilterViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        Button btn;
        private DateFilterViewHolder(View v) {
            super(v);
            btn = itemView.findViewById(R.id.txtv_recycler_item_filter_dialog_year);
        }
    }

    // Provide a suitable constructor (depends on the kind of dataset)
    public DateFilterListAdapter(Context context, List<String> dateStringList, String initialSelectedDate) {

        Log.d(LOG_TAG,"starting list adapter with " + dateStringList.size() + " items and the initial filter key \"" + initialSelectedDate + "\"");

        mInflater = LayoutInflater.from(context);
        dateStrings = dateStringList;
        currentSelection = initialSelectedDate;

        if (context instanceof OnButtonClickListener) {
            listener = (OnButtonClickListener) context;
        } else {
            throw new ClassCastException(context.toString()
                    + " must implement DateFilterListAdapter.OnButtonClickListener");
        }

        Log.d(LOG_TAG,"finished DateFilterListAdapter constructor");
    }

    // Create new views (invoked by the layout manager)
    @Override
    @NonNull
    public DateFilterListAdapter.DateFilterViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // create a new view
        View itemView = mInflater.inflate(R.layout.recyclerview_filter_dialog_item, parent, false);
        return new DateFilterViewHolder(itemView);
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(final DateFilterViewHolder holder, int position) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element
//        Log.d(LOG_TAG,"starting onBindViewHolder()");

        final String buttonText = String.format("%s", dateStrings.get(position));

        holder.btn.setText(String.format("%s", buttonText));
        if (buttonText.equals(currentSelection)) {
            holder.btn.setSelected(true);
        } else {
            holder.btn.setSelected(false);
        }
        holder.btn.setOnClickListener(v -> {
            Log.d(LOG_TAG,"user clicked on button \"" + buttonText + "\"");
            if (holder.btn.isSelected()) {
                currentSelection = "";
            } else {
                currentSelection = buttonText;
            }
            listener.OnButtonClicked(buttonText, !holder.btn.isSelected());
            notifyDataSetChanged();
        });
    }

    /**
     * method to set the content of the recycler view
     * @param datesList list of strings containing the data to be displayed
     * @param clearSelection flag, if the current selected button should be unselected
     */
    public void setDatesList(List<String> datesList, boolean clearSelection) {

        dateStrings = datesList;
        // delete the last selection
        if (clearSelection) {
            currentSelection = "";
        }
        notifyDataSetChanged();
    }

    public String getCurrentSelection() {
        return currentSelection;
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return dateStrings.size();
    }
}
