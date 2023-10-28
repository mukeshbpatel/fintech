package com.amp.fintech.service;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.TimeZone;

import org.joda.time.LocalDate;

public class Utility {

    public final static String ZerodhaApiUrl = "https://kite.zerodha.com/oms/instruments/historical/{key}/{timeFrame}?user_id=BX3771&oi=1&from={from}&to={to}";

    public final static String ZerodhaInstrumentUrl = "https://api.kite.trade/instruments";

    private final static SimpleDateFormat formatter  = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    public static String dateToString(Date date, String pattern) {
        SimpleDateFormat formatter = new SimpleDateFormat(pattern);
        return formatter.format(date);
    }

    public static LocalDateTime stringToDate(String object) {
        try {
            //return formatter.parse(object.replace("T", " ").replace("+0530", ""));
            //return object.replace("T", " ").replace("+0530", "");
            return LocalDateTime.parse(object.replace("+0530", ""));
        } catch (Exception e) {
            return LocalDateTime.now();
        }
    }

    public static Double stringToDouble(String object) {
        try {
            return Double.parseDouble(object);
        } catch (Exception e) {
            return 0d;
        }
    }

    public static Integer stringToInteger(String object) {
        return Integer.parseInt(object);
    }

}
