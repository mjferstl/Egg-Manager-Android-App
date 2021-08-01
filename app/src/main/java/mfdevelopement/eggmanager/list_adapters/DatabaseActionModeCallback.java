package mfdevelopement.eggmanager.list_adapters;

import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuInflater;

import mfdevelopement.eggmanager.R;

/**
 * Abstract class which implements the methods, which have no dependency to the class, which uses the Callback
 */
abstract class DatabaseActionModeCallback implements ActionMode.Callback {

    // Called when the action mode is created; startActionMode() was called
    @Override
    public boolean onCreateActionMode(ActionMode mode, Menu menu) {
        // Inflate a menu resource providing context menu items
        MenuInflater inflater = mode.getMenuInflater();
        inflater.inflate(R.menu.context_menu_database_entry, menu);
        return true;
    }

    // Called each time the action mode is shown. Always called after onCreateActionMode, but
    // may be called multiple times if the mode is invalidated.
    @Override
    public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
        return false; // Return false if nothing is done
    }
}
