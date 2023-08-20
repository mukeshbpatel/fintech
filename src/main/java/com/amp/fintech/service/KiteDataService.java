package com.amp.fintech.service;

import com.amp.fintech.model.Script;
import com.amp.fintech.model.StockData;

public interface KiteDataService {
    StockData geStockData(Script script);
}
