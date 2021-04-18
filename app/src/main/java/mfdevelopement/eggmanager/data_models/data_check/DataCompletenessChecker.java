package mfdevelopement.eggmanager.data_models.data_check;

import androidx.annotation.NonNull;

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
     * Get all missing dates within the start date and end date in data
     *
     * @param startDate
     * @param endDate
     * @param data
     * @return
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
            if (!existingDates.contains(c.getTimeInMillis()))
                existingDates.add(c.getTimeInMillis());
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

    private final SimpleDateFormat sdfYear = new SimpleDateFormat("yyyy", Locale.getDefault());
    private final SimpleDateFormat sdfDayMonth = new SimpleDateFormat("dd. MMMM", Locale.getDefault());

    private List<GroupInfo> convertToGroupInfo(List<Date> dateList) {

        List<GroupInfo> groupInfoList = new ArrayList<>();
        if (dateList.isEmpty()) return groupInfoList;

        //
        GroupInfo groupInfo = new GroupInfo("");
        Date currentDate;
        String lastUsedYearString = "";
        for (int i = 0; i < dateList.size(); i++) {

            currentDate = dateList.get(i);

            String yearString = sdfYear.format(currentDate);
            if (i == 0 || !yearString.equals(lastUsedYearString)) {
                // save the created group info
                if (i != 0) groupInfoList.add(groupInfo);

                // create a ew group info
                groupInfo = new GroupInfo(yearString);
                lastUsedYearString = yearString;
            }

            // add the current date as child for the group info
            groupInfo.addChildInfo(new ChildInfo(sdfDayMonth.format(currentDate)));
        }

        // add the last group info list
        groupInfoList.add(groupInfo);

        return groupInfoList;
    }
}
