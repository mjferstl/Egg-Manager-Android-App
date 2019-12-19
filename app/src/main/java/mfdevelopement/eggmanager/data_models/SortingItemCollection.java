package mfdevelopement.eggmanager.data_models;

import java.util.ArrayList;
import java.util.List;

public class SortingItemCollection {

    private List<SortingItem> items;

    public SortingItemCollection() {
        this.items = new ArrayList<>();
    }

    public SortingItemCollection(List<SortingItem> sortingItems) {
        this.items = sortingItems;
    }

    public void addItem(SortingItem item) {
        this.items.add(item);
    }

    public void removeItem(int position) {
        this.items.remove(position);
    }

    public List<SortingItem> getItems() {
        return items;
    }
}
