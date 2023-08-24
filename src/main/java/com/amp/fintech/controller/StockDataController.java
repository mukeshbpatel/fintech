package com.amp.fintech.controller;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.amp.fintech.model.Script;
import com.amp.fintech.model.StockData;
import com.amp.fintech.model.KiteData.Instrument;
import com.amp.fintech.service.KiteDataService;

@RestController
@RequestMapping("api")
public class StockDataController {

    @Autowired
    private KiteDataService kiteDataService;

    @GetMapping("/GetData")
    public StockData getData() {

        return kiteDataService.geStockData(Script.builder()
                .name("Nifty").apiKey("256265").startDate("2023-08-01").endDate("2023-08-20")
                .build());

    }

    @GetMapping("/GetScriptData")
    public StockData getScriptData(Script script) {

        return kiteDataService.geStockData(script);

    }

    @GetMapping("/GetCsvData")
    public List<Instrument> getCsvData(String fileName) {
        return kiteDataService.csvToMap(fileName);
    }

    @RequestMapping("/GetScriptDatas")
    public List<StockData> getScriptData(@RequestBody List<Script> scripts) {
        List<StockData> stockDatas = kiteDataService.geStockData(scripts);
        return stockDatas;

    }
}
