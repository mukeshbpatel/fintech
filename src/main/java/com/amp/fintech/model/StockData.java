package com.amp.fintech.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.ArrayUtils;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.jackson.Jacksonized;

@Builder
@Getter
@Setter
@Jacksonized
@JsonIgnoreProperties(ignoreUnknown = true)
public class StockData implements Serializable {
    private final Script script;
    private final List<Candle> candles;

    public void updateSMA() {

        ArrayList<Double> sma9 = new ArrayList<Double>();

        candles.stream().forEach(candle -> {

            if (sma9.size() == 9)
                sma9.remove(0);
            sma9.add(candle.getClose());

            double s9 = candle.getClose();
            if (sma9.size() == 9)
                s9 = sma9.stream().mapToDouble(d->d).sum()/9;

                candle.setSma9(s9);
            
        });
    }
}
