package mfdevelopement.eggmanager.data_models.expandable_list;

import java.util.ArrayList;
import java.util.List;

public class GroupInfo {

    private final String name;
    private List<ChildInfo> childInfoList;

    public GroupInfo(String name) {
        this(name, new ArrayList<>());
    }

    public GroupInfo(String name, List<ChildInfo> childInfoList) {
        this.name = name;
        setChildInfoList(childInfoList);
    }

    private void setChildInfoList(List<ChildInfo> childInfoList) {
        if (childInfoList != null) this.childInfoList = childInfoList;
    }

    public String getName() {
        return name;
    }

    public List<ChildInfo> getChildInfoList() {
        return childInfoList;
    }

    public void addChildInfo(ChildInfo childInfo) {
        if (this.childInfoList == null) this.childInfoList = new ArrayList<>();
        this.childInfoList.add(childInfo);
    }

    public long getChildCount() {
        return this.childInfoList.size();
    }
}
