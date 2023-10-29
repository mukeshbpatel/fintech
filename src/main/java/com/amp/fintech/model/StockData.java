package com.amp.fintech.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

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
        ArrayList<Double> sma20 = new ArrayList<Double>();
        ArrayList<Double> sma50 = new ArrayList<Double>();
        ArrayList<Double> sma100 = new ArrayList<Double>();
        ArrayList<Double> sma200 = new ArrayList<Double>();

        candles.stream().forEach(candle -> {
            candle.setSma9(calculateSMA(candle, sma9, 9));
            candle.setSma20(calculateSMA(candle, sma20, 20));
            candle.setSma50(calculateSMA(candle, sma50, 50));
            candle.setSma100(calculateSMA(candle, sma100, 100));
            candle.setSma200(calculateSMA(candle, sma200, 200));
        });
    }

    private double calculateSMA(Candle candle,ArrayList<Double> smaList, int sma) {

        //sma9 calculation
            smaList.add(candle.getClose());
            if (smaList.size() > sma)
                smaList.remove(0);
            double s = candle.getClose();
            if (smaList.size() == sma && candle.getRank() > sma)
                s = smaList.stream().mapToDouble(d -> d).average().getAsDouble();
            
            return s;
    }
}
