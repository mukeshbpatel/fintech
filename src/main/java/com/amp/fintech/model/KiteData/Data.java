package com.amp.fintech.model.KiteData;

import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import com.amp.fintech.model.Candle;
import com.amp.fintech.service.Utility;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.jackson.Jacksonized;

@Jacksonized
@Builder
@Getter
@Setter
public class Data implements Serializable {

    private final ArrayList<ArrayList<String>> candles;
    
    public List<Candle> getData() {
        List<Candle> data = new ArrayList<>();
        AtomicInteger i = new AtomicInteger(0);
        data = candles.stream().map(m -> {
            return Candle.builder()
                .rank(i.incrementAndGet())
                .date(Utility.stringToDate(m.get(0)))
                .open(Utility.stringToDouble(m.get(1)))
                .high(Utility.stringToDouble(m.get(2)))
                .low(Utility.stringToDouble(m.get(3)))
                .close(Utility.stringToDouble(m.get(4)))
                .volume(Utility.stringToInteger(m.get(5)))
                .oi(Utility.stringToInteger(m.get(6)))
                .build();
                
                
            }).collect(Collectors.toList());
        return data;
    }

    

}
