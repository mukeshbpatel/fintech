package com.amp.fintech.service;

import java.util.List;

import com.amp.fintech.model.Script;
import com.amp.fintech.model.StockData;
import com.amp.fintech.model.KiteData.Instrument;


public interface KiteDataService {
    StockData geStockData(Script script);

    List<StockData> geStockData(List<Script> script);

    List<Instrument> getInstruments(String weeklyExpiry,String  monthlyExpiry);

    List<StockData> geLiveData(String weeklyExpiry,String  monthlyExpiry);
}
