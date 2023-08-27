package mfdevelopement.eggmanager.utils;

import androidx.annotation.NonNull;

import java.util.List;

public abstract class DataImportManager<T> {

    abstract List<T> getImportData();

    abstract void setImportData(@NonNull List<T> importData);

    /**
     * Start the import of the data
     */
    abstract void importData();

}
