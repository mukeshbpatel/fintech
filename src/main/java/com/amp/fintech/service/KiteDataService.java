package com.amp.fintech.service;

import java.util.List;
import java.util.Map;

import com.amp.fintech.model.Script;
import com.amp.fintech.model.StockData;
import com.amp.fintech.model.KiteData.Instrument;


public interface KiteDataService {
    StockData geStockData(Script script);

    List<StockData> geStockData(List<Script> script);

    List<Instrument> csvToMap(String file);
}
