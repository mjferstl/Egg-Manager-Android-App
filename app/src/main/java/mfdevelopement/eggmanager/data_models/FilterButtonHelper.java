package mfdevelopement.eggmanager.data_models;

public class FilterButtonHelper {

    private String filterString;
    private int buttonPosition;
    private boolean selected;

    public FilterButtonHelper() {}

    public FilterButtonHelper(String filterString, int position, boolean selected) {
        this.filterString = filterString;
        this.buttonPosition = position;
        this.selected = selected;
    }

    public String getFilterString() {
        return filterString;
    }

    public void setFilterString(String filterString) {
        this.filterString = filterString;
    }

    public int getButtonPosition() {
        return buttonPosition;
    }

    public void setButtonPosition(int buttonPosition) {
        this.buttonPosition = buttonPosition;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }
}
