package org.braun.cookbook.util;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlType;

/**
 *
 * @author mbraun
 */
@XmlType
@XmlAccessorType(XmlAccessType.FIELD)
public final class UncompleteDateTime {

    @XmlAttribute
    private String in;
    @XmlAttribute
    private Integer year;
    @XmlAttribute
    private Integer month;
    @XmlAttribute
    private Integer day;
    @XmlAttribute
    private Integer hour;
    @XmlAttribute
    private Integer minute;
    @XmlAttribute
    private Integer second;
    
    private static final int[] VALID_MONTHS_NORMAL = new int[] {0, 31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31};
    private static final int[] VALID_MONTHS_LEAP   = new int[] {0, 31, 29, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31};

    public UncompleteDateTime() {
        in = null;
    }
    
    public UncompleteDateTime(String in) throws ConditionParseException {
        this.in = in;
        init();
    }

    public Integer getYear() {
        return year;
    }

    public Integer getMonth() {
        return month;
    }

    public Integer getDay() {
        return day;
    }

    public Integer getHour() {
        return hour;
    }

    public Integer getMinute() {
        return minute;
    }

    public Integer getSecond() {
        return second;
    }

    private Integer toInt(String value) throws ConditionParseException {
        if ("--".equals(value) || "----".equals(value)) {
            return null;
        }
        try {
            return Integer.parseUnsignedInt(value);
        } catch (NumberFormatException e) {
            throw new ConditionParseException("Invalid date " + value);
        }
    }

    private void checkRange(String type, Integer value, int begin, int end) throws ConditionParseException {
        if (value < begin || value > end) {
            throw new ConditionParseException("Invalid value for " + type);
        }
    }

    /**
     * Ein Jahr ist ein Schaltjahr, wenn die Jahreszahl durch 4 teilbar ist. wenn die Jahreszahl durch 4, aber nicht durch 100
     * teilbar ist. wenn die Jahreszahl durch 4, durch 100 und durch 400 teilbar ist.
     */
    private boolean isLeapYear(int year) {
        if (year % 4 == 0) {
            if (year % 100 == 0 && year % 400 == 0) {
                return true;
            } else {
                if (year % 100 == 0 && year % 400 != 0) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    public boolean isEmpty() {
        return in == null || in.isBlank();
    }
    
    @Override
    public String toString() {
        return in;
    }

    public String getIn() {
        return in;
    }

    public void setIn(String in) throws ConditionParseException {
        this.in = in;
        init();
    }
    
    private void init() throws ConditionParseException {
        if (in == null || in.isEmpty()) {
            return;
        }
        switch(in.length()) {
            case 8 -> in = in + "------";
            case 12 -> in = in + "--";
            case 14 -> {
            }
            default -> throw new ConditionParseException("Invalid date. Length must be 14.");
        }
        year = toInt(in.substring(0, 4));
        month = toInt(in.substring(4, 6));
        day = toInt(in.substring(6, 8));
        hour = toInt(in.substring(8, 10));
        minute = toInt(in.substring(10, 12));
        second = toInt(in.substring(12, 14));
        if (minute == null && second != null) {
            throw new ConditionParseException("Invalid Date-Pattern. Second must be empty.");
        }
        if (hour == null && minute != null) {
            throw new ConditionParseException("Invalid Date-Pattern. Minute must be empty.");
        }
        if (day == null && hour != null) {
            throw new ConditionParseException("Invalid Date-Pattern. Hour must be empty.");
        }
        if (month == null && day != null) {
            throw new ConditionParseException("Invalid Date-Pattern. Day must be empty.");
        }
        if (getMonth() != null) {
            checkRange("Month", getMonth(), 1, 12);
            
            if (getDay() != null) {
                if (isLeapYear(getYear())) {
                    checkRange("Day", getDay(), 1, VALID_MONTHS_NORMAL[getMonth()]);
                } else {
                    checkRange("Day", getDay(), 1, VALID_MONTHS_LEAP[getMonth()]);
                }
            }
        }
        if (getHour() != null) {
            checkRange("Hour", getHour(), 0, 23);
        }
        if (getMinute() != null) {
            checkRange("Minute", getMinute(), 0, 59);
        }
        if (getSecond()!= null) {
            checkRange("Minute", getSecond(), 0, 59);
        }
    }
    
}
