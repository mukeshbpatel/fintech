package com.amp.fintech.utility;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class DateUtil {
    
    public static DateTimeFormatter getDateFormatter() {
        return DateTimeFormatter.ofPattern("yyyy-MM-dd");
    }

    public static LocalDate getLocalDate(String date) {
        return LocalDate.parse(date);
    }

    public static String dateToString(LocalDate ldate) {
        return ldate.format(getDateFormatter());
    }

    public static String getCurrentDate() {
       return dateToString(LocalDate.now());
    }

    public static String getCurrentDate(int addDays) {
       return addDays(dateToString(LocalDate.now()),addDays);
    }

    public static String addDays(String date, int days) {
       LocalDate ldate = getLocalDate(date);
       ldate = ldate.plusDays(days);
       return dateToString(ldate);
    }
}
