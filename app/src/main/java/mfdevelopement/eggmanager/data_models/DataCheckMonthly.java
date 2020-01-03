package mfdevelopement.eggmanager.data_models;

import androidx.annotation.NonNull;

import java.util.List;

public class DataCheckMonthly {

    private String dateKeyMonth;
    private boolean isComplete;
    private List<String> missingDates, foundDates;

    public DataCheckMonthly(@NonNull String dateKeyYearMonth, @NonNull List<String> foundDates, @NonNull List<String> missingDates) {
        this.dateKeyMonth = dateKeyYearMonth;
        this.foundDates = foundDates;
        this.missingDates = missingDates;
        checkCompleteness();
    }

    private void checkCompleteness() {
        this.isComplete = this.missingDates.isEmpty();
    }


    public String getDateKeyMonth() {
        if (this.dateKeyMonth == null)
            return "";
        else
            return dateKeyMonth;
    }

    public void setDateKeyMonth(@NonNull String dateKeyMonth) {
        this.dateKeyMonth = dateKeyMonth;
    }

    public boolean isComplete() {
        checkCompleteness();
        return isComplete;
    }

    public void setComplete(boolean complete) {
        isComplete = complete;
    }

    public List<String> getMissingDates() {
        return missingDates;
    }

    public void setMissingDates(List<String> missingDates) {
        this.missingDates = missingDates;
    }

    public List<String> getFoundDates() {
        return foundDates;
    }

    public void setFoundDates(List<String> foundDates) {
        this.foundDates = foundDates;
    }
}
