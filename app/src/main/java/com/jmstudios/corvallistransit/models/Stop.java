package com.jmstudios.corvallistransit.models;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.Period;

public class Stop {
    public String name;
    public String road;
    public double bearing;
    public boolean adherehancePoint;
    public double latitude;
    public double longitude;
    public int id;
    public double distance;

    public Route route;
    public DateTime expectedTime;
    public DateTime scheduledTime;

    public int eta() {
        Period period = new Period(this.expectedTime, DateTime.now());
        int eta =  period.getMinutes();
        return (eta >= 1) ? eta : 1;
    }
    /*
     * Overridden equals() and hashCode() here for comparison purposes.
     */
    @Override
    public boolean equals(Object obj) {
        return (obj != null) && (obj instanceof Stop)
                && ((Stop) obj).id == this.id;
    }

    @Override
    public int hashCode() {
        return this.id;
    }
}
