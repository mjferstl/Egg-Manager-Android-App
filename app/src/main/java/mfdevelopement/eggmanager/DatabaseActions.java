package mfdevelopement.eggmanager;

public enum DatabaseActions {
    NONE;

    public enum Request {
        NEW_ENTITY(10),
        EDIT_ENTITY(11),
        EDIT_FILTER(12);

        public final long id;

        private Request(long id) {
            this.id = id;
        }
    }

    public enum Result {
        NEW_ENTITY(100),
        ENTITY_EDITED(101),
        FILTER_OK(102),
        FILTER_CANCEL(103),
        FILTER_REMOVED(104);

        public final long id;

        private Result(long id) {
            this.id = id;
        }
    }
}
