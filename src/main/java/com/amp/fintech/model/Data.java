package com.amp.fintech.model;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Data {
    public ArrayList<ArrayList<Object>> candles;

    public List<Candle> getData() {
        List<Candle> data = new ArrayList<>();
        data = candles.stream().map(m -> Candle.builder()
        .open((double) m.get(0))
        .high((double) m.get(1))
        .low((double) m.get(2))
        .close((double) m.get(3))
        .volume((int) m.get(4))
        .oi((int) m.get(5))
        .build()
        ).collect(Collectors.toList());
        return data;
    }

}
