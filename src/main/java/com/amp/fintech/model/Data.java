package com.amp.fintech.model;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

public class Data {
    public ArrayList<ArrayList<String>> candles;

    SimpleDateFormat formatter1=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public List<Candle> getData() {
        List<Candle> data = new ArrayList<>();
        data = candles.stream().map(m -> Candle.builder()
        .date(stringToDate(m.get(0)))
        .open(stringToDouble(m.get(1)))
        .high(stringToDouble(m.get(2)))
        .low(stringToDouble(m.get(3)))
        .close(stringToDouble(m.get(4)))
         .volume(stringToInteger(m.get(5)))
        .oi(stringToInteger(m.get(6)))
        .build()
        ).collect(Collectors.toList());
        return data;
    } 

    public Date stringToDate(String object) {
        try {
            return formatter1.parse(object.replace("T","").replace("+0530", ""));
        } catch (ParseException e) {
            return new Date();
        }
    }

    public Double stringToDouble(String object) {
        try {
            return Double.parseDouble(object);
        } catch (Exception e) {
            return 0d;
        }
    }

    public Integer stringToInteger(String object) {
            return Integer.parseInt(object);
    }

}
