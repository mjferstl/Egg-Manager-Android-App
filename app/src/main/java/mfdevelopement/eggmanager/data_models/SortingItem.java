package mfdevelopement.eggmanager.data_models;

public class SortingItem {

    private String sortingName, sortingOrder;
    private boolean isSelected;

    public SortingItem() {
        this.sortingName = "";
        this.sortingOrder = "";
        this.isSelected = false;
    }

    public SortingItem(String sortingName, String sortingOrder, boolean isSelected) {
        this.sortingName = sortingName;
        this.sortingOrder = sortingOrder;
        this.isSelected = isSelected;
    }

    public String getSortingName() {
        return sortingName;
    }

    public void setSortingName(String sortingName) {
        this.sortingName = sortingName;
    }

    public String getSortingOrder() {
        return sortingOrder;
    }

    public void setSortingOrder(String sortingOrder) {
        this.sortingOrder = sortingOrder;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }
}
