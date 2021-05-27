package mfdevelopement.eggmanager.charts;

import java.util.Calendar;

public class ReferenceDate {

    public static Calendar getReferenceDate() {
        Calendar reference = Calendar.getInstance();
        reference.set(2000, 0, 1, 0, 0, 0);
        return reference;
    }
}
