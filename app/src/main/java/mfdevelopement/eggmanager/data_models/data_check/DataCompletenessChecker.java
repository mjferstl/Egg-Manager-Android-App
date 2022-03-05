package mfdevelopement.eggmanager.data_models.data_check;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import mfdevelopement.eggmanager.data_models.HasDateInterface;
import mfdevelopement.eggmanager.data_models.expandable_list.ChildInfo;
import mfdevelopement.eggmanager.data_models.expandable_list.ExpandableListCompatible;
import mfdevelopement.eggmanager.data_models.expandable_list.GroupInfo;

public class DataCompletenessChecker implements ExpandableListCompatible {

    private final Date startDate;
    private final Date endDate;

    private final List<HasDateInterface> data;

    private static final String LOG_TAG = "DataCompletenessChecker";

    private static final SimpleDateFormat sdfYear = new SimpleDateFormat("yyyy", Locale.getDefault());
    private static final SimpleDateFormat sdfMonthYear = new SimpleDateFormat("MMMM yyyy", Locale.getDefault());
    private static final SimpleDateFormat sdfDayMonth = new SimpleDateFormat("dd. MMMM", Locale.getDefault());
    private static final SimpleDateFormat sdfDayMonthYear = new SimpleDateFormat("dd. MMMM yyyy", Locale.getDefault());

    public DataCompletenessChecker(@NonNull Calendar startDate, @NonNull Calendar endDate, @NonNull List<HasDateInterface> databaseItems) {
        Date d = new Date();
        d.setTime(startDate.getTimeInMillis());
        this.startDate = d;
        Date d2 = new Date();
        d2.setTime(endDate.getTimeInMillis());
        this.endDate = d2;
        this.data = databaseItems;
    }

    public DataCompletenessChecker(@NonNull Date startDate, @NonNull Date endDate, @NonNull List<HasDateInterface> databaseItems) {
        this.startDate = startDate;
        this.endDate = endDate;
        this.data = databaseItems;
    }

    private List<Date> checkDataCompleteness() {
        List<Date> missingDates = getMissingDates(this.startDate, this.endDate, this.data);

        // sort the dates in ascending order
        return sortDates(missingDates);
    }

    private List<Date> sortDates(List<Date> dateList) {
        Collections.sort(dateList);
        return dateList;
    }

    /**
     * Get all missing dates within the start date and end date in the list
     *
     * @param startDate first date of the observed time range
     * @param endDate   last date of the observed time range
     * @param data      list of objects, which contain dates
     * @return list of {@link Date} objects, which represent the dates, where no object with the same date has not been found in {@code data}
     */
    private List<Date> getMissingDates(Date startDate, Date endDate, List<HasDateInterface> data) {

        // create Calendar instances for the first and the last date
        Calendar firstDay = Calendar.getInstance();
        firstDay.setTimeInMillis(startDate.getTime());
        Calendar lastDay = Calendar.getInstance();
        lastDay.setTimeInMillis(endDate.getTime());

        // calculate the difference in days between the first and last date
        int diffDays = (int) ((lastDay.getTimeInMillis() - firstDay.getTimeInMillis()) / (1000 * 60 * 60 * 24));

        // check every day between the start and end date, if it exists in the date keys list
        Calendar currDate = Calendar.getInstance();
        currDate.setTimeInMillis(firstDay.getTimeInMillis());

        // get all dates from input data
        List<Long> existingDates = new ArrayList<>();
        for (HasDateInterface di : data) {
            Calendar c = Calendar.getInstance();
            c.setTime(di.getDate());
            if (!existingDates.contains(c.getTimeInMillis())) {
                existingDates.add(c.getTimeInMillis());
            }
        }

        List<Date> missingDates = new ArrayList<>();

        // loop over all days between start and end
        // check if all dates exist
        for (int d = 0; d <= diffDays; d++) {
            if (!existingDates.contains(currDate.getTimeInMillis())) {
                missingDates.add(new Date(currDate.getTimeInMillis()));
            }
            currDate.add(Calendar.DAY_OF_MONTH, 1);
        }

        return missingDates;
    }

    @Override
    public List<GroupInfo> getGroupInfoList() {

        // check the data completeness
        List<Date> missingDates = checkDataCompleteness();

        return convertToGroupInfo(missingDates);
    }

    private List<GroupInfo> convertToGroupInfo(List<Date> dateList) {

        List<GroupInfo> groupInfoList = new ArrayList<>();
        if (dateList.isEmpty()) return groupInfoList;

        // loop over the list to get the number of different years
        List<Integer> yearsList = new ArrayList<>();
        Calendar c = Calendar.getInstance();
        for (Date date : dateList) {
            c.setTime(date);
            if (!yearsList.contains(c.get(Calendar.YEAR))) yearsList.add(c.get(Calendar.YEAR));
        }

        // Create GroupInfo objects
        GroupInfo groupInfo = new GroupInfo("");
        Date currentDate;
        String groupName = "";
        for (int i = 0; i < dateList.size(); i++) {

            currentDate = dateList.get(i);

            // Create the name for the current group
            // This depends on the number of groups, which will be created
            // if the list contains data for at minimum two different years, the data will be grouped by years.
            // Otherwise it will be grouped by months
            String currentGroupName;
            if (yearsList.size() > 1) {
                currentGroupName = sdfYear.format(currentDate);
            } else {
                currentGroupName = sdfMonthYear.format(currentDate);
            }

            // Check if the data belongs to a new group
            if (i == 0 || !currentGroupName.equals(groupName)) {
                // save the created group info
                if (i != 0) groupInfoList.add(groupInfo);

                // create a ew group info
                groupInfo = new GroupInfo(currentGroupName);
                groupName = currentGroupName;
            }

            // add the current date as child for the group info
            groupInfo.addChildInfo(new ChildInfo(sdfDayMonth.format(currentDate)));
        }

        // add the last group info list
        groupInfoList.add(groupInfo);

        return groupInfoList;
    }

    public static @Nullable
    Calendar convertToDate(@NonNull GroupInfo groupInfo) {
        Calendar date = convertToDate(groupInfo.getName(), sdfMonthYear);
        if (date == null) {
            Log.d(LOG_TAG, "Could not convert \"" + groupInfo.getName() + "\" using format \"" + sdfMonthYear.toPattern() + "\". Trying to convert to year instead.");
            date = convertToDate(groupInfo.getName(), sdfYear);
        }
        return date;
    }

    public static @Nullable
    Calendar convertToDate(@NonNull GroupInfo groupInfo, @NonNull ChildInfo childInfo) {
        if (convertToDate(groupInfo.getName(), sdfYear) != null) {
            // This is a year
            // So the child contains the information about day and month
            return convertToDate(childInfo.getName() + " " + groupInfo.getName(), sdfDayMonthYear);
        } else if (convertToDate(groupInfo.getName(), sdfMonthYear) != null) {
            // The group is a month and a year
            // Get the year from the name and add it to the child name to get the complete date
            Calendar calMonthYear = convertToDate(groupInfo.getName(), sdfMonthYear);
            if (calMonthYear != null) {
                String yearString = String.valueOf(calMonthYear.get(Calendar.YEAR));
                return convertToDate(childInfo.getName() + " " + yearString, sdfDayMonthYear);
            }
        } else {
            Log.e(LOG_TAG, "This case is not handeled! groupInfo.getName() = " + groupInfo.getName() + "; childInfo.getName() = " + childInfo.getName());
        }
        return null;
    }

    private static @Nullable
    Calendar convertToDate(@NonNull String string, @NonNull SimpleDateFormat sdf) {
        try {
            Calendar cal = Calendar.getInstance();
            Date parsedDate = sdf.parse(string);
            if (parsedDate != null) {
                cal.setTime(parsedDate);
                return cal;
            } else {
                return null;
            }
        } catch (ParseException e) {
            Log.d(LOG_TAG, "Could not convert " + string + " to a date");
            return null;
        }
    }
}
