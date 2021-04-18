package mfdevelopement.eggmanager.data_models;

import java.util.Date;

public class HasDateInterfaceObject implements HasDateInterface {

    private final Date date;

    public static HasDateInterfaceObject createFromDate(Date date) {
        return new HasDateInterfaceObject(date);
    }

    private HasDateInterfaceObject(Date date) {
        this.date = date;
    }

    @Override
    public Date getDate() {
        return this.date;
    }
}
