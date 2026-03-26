package org.braun.cookbook.web.converter;

import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.convert.ConverterException;
import jakarta.faces.convert.FacesConverter;
import jakarta.faces.convert.Converter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import org.braun.cookbook.util.DateWrapper;
import org.braun.cookbook.util.UncompleteDateTime;

/**
 *
 * @author mbraun
 */
@FacesConverter(value = "dateWrapperConverter")
public class DateWrapperConverter implements Converter<DateWrapper> {

    private static final int[] monthes = new int[]{31, 29, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31};

    @Override
    public DateWrapper getAsObject(FacesContext context, UIComponent component, String value) {
        if (value == null || value.isBlank()) {
            return new DateWrapper();
        }
        DateWrapper dateWrapper;
        String[] parts = value.trim().split(" ");
        switch (parts.length) {
            case 1 -> {
                String date = parseDate(parts[0]);
                dateWrapper = new DateWrapper(date);
            }
            case 2 -> {
                String date = parseDate(parts[0]);
                if (date.indexOf('-') > - 1) {
                    dateWrapper = new DateWrapper(date);
                } else {
                    String time = parseTime(parts[1]);
                    dateWrapper = new DateWrapper(date + time);
                }
            }
            default -> throw new ConverterException("Ungültiges Datum");
        }
        return dateWrapper;
    }

    @Override
    public String getAsString(FacesContext context, UIComponent component, DateWrapper date) {
        if (date == null || date.isEmpty()) {
            return "";
        }
        UncompleteDateTime udt = date.getUncompleteDateTime();
        StringBuilder res = new StringBuilder();
        res.append((udt.getDay() == null) ? "--" : String.format("%02d", udt.getDay()));
        res.append(".").append((udt.getMonth() == null) ? "--" : String.format("%02d", udt.getMonth()));
        res.append(".").append(String.format("%04d", udt.getYear()));
        if (udt.getHour() == null) {
            return res.toString();
        }
        res.append(" ").append(String.format("%02d", udt.getHour()));
        res.append(".").append((udt.getMinute() == null) ? "--" : String.format("%02d", udt.getMinute()));
        res.append(".").append((udt.getSecond() == null) ? "--" : String.format("%02d", udt.getSecond()));
        return "";
    }

    private String parseDate(String value) {
        List<String> parts = getSplittedValues(value, "\\.");
        int year = 0;
        int month = 0;
        int day = 0;
        switch (parts.size()) {
            case 1 -> year = parseNumber(parts.get(0), 0, 9999, "Jahr");
            case 2 -> {
                if (!parts.get(0).equals("-") && !parts.get(0).equals("--")) {
                    month = parseNumber(parts.get(0), 1, 12, "Monat");
                }
                year = parseNumber(parts.get(1), 0, 9999, "Jahr");
            }
            case 3 -> {
                if (!parts.get(0).equals("-") && !parts.get(0).equals("--")) {
                    day = parseNumber(parts.get(0), 1, 31, "Tag");
                }
                if (!parts.get(1).equals("-") && !parts.get(1).equals("--")) {
                    month = parseNumber(parts.get(1), 1, 12, "Monat");
                }
                year = parseNumber(parts.get(2), 0, 9999, "Jahr");
                if (day > 0 && month == 0) {
                    throw new ConverterException("Tagesangabe ohne Monat ist nicht zulässig");
                }
                if (day > 0) {
                    if (day > monthes[month - 1]) {
                        throw new ConverterException("Diese Tagesangabe ist für diesen Monat nicht zulässig");
                    }
                    if (month == 2 && year % 4 != 0 && day > 28) {
                        throw new ConverterException("Diese Tagesangabe ist für diesen Monat nicht zulässig");
                    }
                }
            }
            default -> throw new ConverterException("Ungültige Datumangabe dd.mm.jjjj");
        }
        if (year < 100) {
            Calendar today = Calendar.getInstance();
            int y23 = today.get(Calendar.YEAR) % 100;
            int y01 = today.get(Calendar.YEAR) / 100;
            if (y23 < year) {
                y01--;
            }
            year = y01 * 100 + year;
        }
        StringBuilder res = new StringBuilder();
        res.append(String.format("%04d", year));
        res.append((month == 0) ? "--" : String.format("%02d", month));
        res.append((day == 0) ? "--" : String.format("%02d", day));
        return res.toString();
    }

    private String parseTime(String value) {
        List<String> parts = getSplittedValues(value, ":");
        int hour = -1;
        int minute = -1;
        int second = -1;
        switch (parts.size()) {
            case 1 -> hour = parseNumber(parts.get(0), 0, 23, "Stunde");
            case 2 -> {
                hour = parseNumber(parts.get(0), 0, 23, "Stunde");
                minute = parseNumber(parts.get(1), 0, 59, "Minute");
            }
            case 3 -> {
                hour = parseNumber(parts.get(0), 0, 23, "Stunde");
                minute = parseNumber(parts.get(1), 0, 59, "Minute");
                second = parseNumber(parts.get(2), 0, 59, "Sekunde");
            }
            default -> throw new ConverterException("Ungültige Zeitangabe hh.mm.ss");
        }
        if (hour < 0 && minute >= 0) {
            throw new ConverterException("Minutenangaben ohne Stunden sind unzulässig.");
        }
        if (minute < 0 && second >= 0) {
            throw new ConverterException("Sekundenangaben ohne Minuten sind unzulässig.");
        }
        StringBuilder res = new StringBuilder();
        res.append((hour < 0) ? "--" : String.format("%02d", hour));
        res.append((minute < 0) ? "--" : String.format("%02d", minute));
        res.append((second < 0) ? "--" : String.format("%02d", second));
        return res.toString();
    }

    private int parseNumber(String value, int from, int to, String type) {
        try {
            int i = Integer.parseInt(value);
            if (i < from || i > to) {
                throw new ConverterException("Ungültiger Wertebreich für " + type);
            }
            return i;
        } catch (NumberFormatException e) {
            throw new ConverterException("Ungültige Angabe für " + type);
        }
    }

    private List<String> getSplittedValues(String value, String token) {
        String[] x = value.split(token);
        List<String> parts = new ArrayList<>();
        for (String s : x) {
            if (s != null) {
                parts.add(s);
            }
        }
        return parts;
    }

}
