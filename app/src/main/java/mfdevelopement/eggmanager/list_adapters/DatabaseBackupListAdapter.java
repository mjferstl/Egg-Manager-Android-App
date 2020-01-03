package mfdevelopement.eggmanager.list_adapters;

import android.content.Context;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import mfdevelopement.eggmanager.R;
import mfdevelopement.eggmanager.data_models.DatabaseBackup;

public class DatabaseBackupListAdapter extends RecyclerView.Adapter<DatabaseBackupListAdapter.DatabaseBackupViewHolder> {

    private final String LOG_TAG = "DatabaseBackupListAdapt";
    private List<DatabaseBackup> data;
    private final LayoutInflater mInflater;
    private Context context;

    public class DatabaseBackupViewHolder extends RecyclerView.ViewHolder implements View.OnCreateContextMenuListener{

        private TextView txtv_name, txtv_date;

        private DatabaseBackupViewHolder(View itemView) {
            super(itemView);
            txtv_name = itemView.findViewById(R.id.txtv_backup_item_name);
            txtv_date = itemView.findViewById(R.id.txtv_backup_item_timestamp);
            itemView.setOnCreateContextMenuListener(this);
        }

        @Override
        public void onCreateContextMenu(ContextMenu contextMenu, View v, ContextMenu.ContextMenuInfo menuInfo) {
            Log.d(LOG_TAG,"onCreateContextMenu(): creating context menu for item at position " + getAdapterPosition());
            contextMenu.setHeaderTitle(data.get(getAdapterPosition()).getBackupName());
            contextMenu.add(0, v.getId(), getAdapterPosition(), context.getString(R.string.txt_option_import));
            contextMenu.add(0, v.getId(), getAdapterPosition(), context.getString(R.string.txt_option_delete));
        }
    }

    public DatabaseBackupListAdapter(Context context, List<DatabaseBackup> data) {
        this.context = context;
        mInflater = LayoutInflater.from(context);
        this.data = data;
        Log.d(LOG_TAG,"creating new adapter with " + getItemCount() + " items");
    }

    @Override
    @NonNull
    public DatabaseBackupViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = mInflater.inflate(R.layout.recyclerview_backup_item, parent, false);
        return new DatabaseBackupViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull final DatabaseBackupViewHolder holder, final int position) {
        DatabaseBackup backup = data.get(position);

        holder.txtv_name.setText(backup.getBackupName());
        String fileInfo = backup.getFormattedSaveDate() + " - " + backup.getFileSizeFormatted();
        holder.txtv_date.setText(fileInfo);
    }

    public void setDatabaseBackupList(List<DatabaseBackup> backupList){
        this.data = backupList;
        notifyDataSetChanged();
    }

    /**
     * Get a item of the recycler view
     * @param index position of the item, starting at 0
     * @return DatabaseBackup
     */
    public DatabaseBackup getItem(int index) {
        return this.data.get(index);
    }

    // getItemCount() is called many times, and when it is first called,
    // mWords has not been updated (means initially, it's null, and we can't return null).
    @Override
    public int getItemCount() {
        if (data != null)
            return data.size();
        else
            return 0;
    }
}
