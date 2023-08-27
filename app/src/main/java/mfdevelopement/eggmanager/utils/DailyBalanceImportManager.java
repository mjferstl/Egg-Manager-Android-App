package mfdevelopement.eggmanager.utils;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import kotlinx.coroutines.Dispatchers;
import mfdevelopement.eggmanager.coroutines.MyCoroutines;
import mfdevelopement.eggmanager.data_models.daily_balance.DailyBalance;
import mfdevelopement.eggmanager.dialog_fragments.ImportDataProgressDialog;
import mfdevelopement.eggmanager.viewmodels.DailyBalanceViewModel;

public class DailyBalanceImportManager extends DataImportManager<DailyBalance> {

    private final DailyBalanceViewModel viewModel;
    protected boolean showDialog = true;
    protected List<DailyBalance> importData = new ArrayList<>();
    protected boolean overwriteExisting = false;
    protected ImportDataProgressDialog dialog = null;
    private FragmentManager fragmentManager = null;
    private OnDataImportListener listener = null;

    /**
     * Main constructor
     *
     * @param viewModel: {@link androidx.lifecycle.ViewModel} to handle basic database actions for {@link DailyBalance} objects
     */
    public DailyBalanceImportManager(DailyBalanceViewModel viewModel) {
        this.viewModel = viewModel;
    }

    public void disableDialog() {
        this.showDialog = false;
        if (dialog != null)
            dialog.dismiss();
    }

    public void enableDialog(FragmentManager fragmentManager) {
        this.showDialog = true;
        this.fragmentManager = fragmentManager;
    }

    @Override
    public List<DailyBalance> getImportData() {
        return this.importData;
    }

    @Override
    public void setImportData(@NonNull List<DailyBalance> dailyBalanceList) {
        this.importData = dailyBalanceList;
    }

    public void setOverwriteExisting(boolean overwriteExisting) {
        this.overwriteExisting = overwriteExisting;
    }

    @Override
    public void importData() {
        // Create the dialog
        if (this.showDialog) {
            dialog = new ImportDataProgressDialog();

            // Initialize the progress in the dialog
            dialog.setMaxProgress(this.importData.size());
            dialog.setProgress(0);
            //dialog.show(fragmentManager, "ImportDataProgressDialog");

            String dialogFragmentTag = "ImportDataProgressDialog";
            FragmentTransaction ft = fragmentManager.beginTransaction();
            Fragment prev = fragmentManager.findFragmentByTag(dialogFragmentTag);
            if (prev != null) {
                ft.remove(prev);
            }
            ft.addToBackStack(null);
            dialog.show(ft, dialogFragmentTag);


        }


        MyCoroutines.Companion.doAsync(() -> {
            doIt();
            return null;
        }, Dispatchers.getMain());

    }

    private void doIt() {
        // Get the data, which is already in the database, if overwrite existing is false
        HashMap<String, DailyBalance> existingDailyBalancesMap = new HashMap<>();
        if (!this.overwriteExisting) {
            List<DailyBalance> existingDailyBalances = viewModel.getAllDailyBalances().getValue();
            if (existingDailyBalances != null) {
                for (DailyBalance item : existingDailyBalances) {
                    existingDailyBalancesMap.put(item.getDateKey(), item);
                }
            }
        }

        // Do the import
        List<String> existingDateKeysList = new ArrayList<>(existingDailyBalancesMap.keySet());
        for (int i = 0; i < this.importData.size(); i++) {
            DailyBalance currentDailyBalance = this.importData.get(i);

            // Do not insert the item if the flag is false and an item already exists
            boolean insertItem = !(!overwriteExisting && existingDateKeysList.contains(currentDailyBalance.getDateKey()));
            if (insertItem) {
                viewModel.insert(currentDailyBalance);
            }

            updateDialogProgress(i + 1);
/*            Log.d("test", String.format(Locale.getDefault(), "%d", i + 1));
            try {
                Thread.sleep(10L);
            } catch (InterruptedException e) {

            }*/
        }
        if (listener != null)
            listener.onImportFinished();
    }

    private void updateDialogProgress(int progress) {
        if (this.showDialog && dialog != null) {
            dialog.setProgress(progress);
        }
    }

    public void setOnDataImportListener(OnDataImportListener listener) {
        this.listener = listener;
    }

    public interface OnDataImportListener {
        void onImportFinished();
    }
}
