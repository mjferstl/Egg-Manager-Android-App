package mfdevelopement.eggmanager;

public enum DatabaseActions {
    NONE;

    public enum Request {
        NEW_ENTITY,
        EDIT_ENTITY,
        EDIT_FILTER
    }

    public enum Result {
        NEW_ENTITY,
        ENTITY_EDITED,
        FILTER_OK,
        FILTER_CANCEL,
        FILTER_REMOVED
    }

}
