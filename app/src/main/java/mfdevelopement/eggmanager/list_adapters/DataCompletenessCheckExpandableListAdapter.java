package mfdevelopement.eggmanager.list_adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import mfdevelopement.eggmanager.R;
import mfdevelopement.eggmanager.data_models.expandable_list.ChildInfo;
import mfdevelopement.eggmanager.data_models.expandable_list.GroupInfo;

public class DataCompletenessCheckExpandableListAdapter extends BaseExpandableListAdapter {

    private final Context context;
    private final String LOG_TAG = "DataCompChckExpListAdap";
    private final List<OnChildAddButtonClickListener> listenerList = new ArrayList<>();
    private List<GroupInfo> groupInfoList;

    public DataCompletenessCheckExpandableListAdapter(Context context, GroupInfo groupInfo) {
        this(context, Collections.singletonList(groupInfo));
    }

    public DataCompletenessCheckExpandableListAdapter(Context context, List<GroupInfo> groupInfoList) {
        this.context = context;
        this.groupInfoList = groupInfoList;
    }

    @Override
    public int getGroupCount() {
        return groupInfoList.size();
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return groupInfoList.get(groupPosition).getChildInfoList().size();
    }

    @Override
    public GroupInfo getGroup(int groupPosition) {
        return groupInfoList.get(groupPosition);
    }

    @Override
    public ChildInfo getChild(int groupPosition, int childPosition) {
        return groupInfoList.get(groupPosition).getChildInfoList().get(childPosition);
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {

        GroupInfo headerInfo = getGroup(groupPosition);
        if (convertView == null) {
            LayoutInflater inf = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inf.inflate(R.layout.list_item_group_data_completeness_check, parent, false);
        }

        TextView heading = convertView.findViewById(R.id.txtv_recycler_item_completeness_check_name);
        heading.setText(headerInfo.getName().trim());

        return convertView;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {

        ChildInfo childInfo = getChild(groupPosition, childPosition);
        if (convertView == null) {
            LayoutInflater inf = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inf.inflate(R.layout.list_item_child_data_completeness_check, parent, false);
        }

        TextView heading = convertView.findViewById(R.id.txtv_child_name);
        heading.setText(childInfo.getName().trim());

        ImageButton addButton = convertView.findViewById(R.id.btn_add_database_entry);
        addButton.setOnClickListener(v -> {
            for (OnChildAddButtonClickListener listener : listenerList) {
                listener.childAddClicked(groupPosition, childPosition);
            }
        });

        return convertView;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }

    public void setData(List<GroupInfo> groupInfoList) {
        if (groupInfoList != null) this.groupInfoList = groupInfoList;
        notifyDataSetChanged();
    }

    public void addOnChildAddButtonClickListener(@NonNull OnChildAddButtonClickListener listener) {
        listenerList.add(listener);
    }

    public interface OnChildAddButtonClickListener {
        void childAddClicked(int groupPosition, int childPosition);
    }
}
