package mfdevelopement.eggmanager;

public enum IntentCodes {
    NONE;

    /**
     * Coded for {@link android.content.Intent} requests.
     * To be used when starting another activity to specify a desired action
     */
    public enum Request {
        NEW_ENTITY(10),
        EDIT_ENTITY(11),
        EDIT_FILTER(12);

        public final long id;

        Request(long id) {
            this.id = id;
        }
    }

    /**
     * Coded for {@link android.app.Activity} results.
     * To be used when finishing an activity to specify the result code for the caller activity
     */
    public enum Result {
        NEW_ENTITY(100),
        ENTITY_EDITED(101),
        FILTER_OK(102),
        FILTER_CANCEL(103),
        FILTER_REMOVED(104);

        public final long id;

        Result(long id) {
            this.id = id;
        }
    }

    /**
     * Codes for Notification click actions
     */
    public enum NotificationActions {
        OPEN_MAIN_ACTIVITY("OpenMainActivity"),
        OPEN_BACKUP_ACTIVITY("OpenBackupActivity");

        public final String actionName;

        NotificationActions(String action) {
            this.actionName = action;
        }
    }
}
