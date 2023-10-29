package com.amp.fintech.controller;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
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
    public ResponseEntity<StockData> getScriptData(String authorization, Script script) {
        kiteDataService.setAuthorization(authorization);
        return ResponseEntity.status(HttpStatus.OK)
                .body(kiteDataService.geStockData(script));
    }

    @GetMapping("/getInstruments")
    public ResponseEntity<List<Instrument>> getInstruments(@RequestHeader(value = "Authorization", required = true) String authorization, String weeklyExpiry, String monthlyExpiry) {
        kiteDataService.setAuthorization(authorization);
        return ResponseEntity.status(HttpStatus.OK)
                .body(kiteDataService.getInstruments(weeklyExpiry, monthlyExpiry));
    }

    @RequestMapping("/GetScriptDatas")
    public ResponseEntity<List<StockData>> getScriptData(@RequestHeader(value = "Authorization", required = true) String authorization, @RequestBody List<Script> scripts) {
        kiteDataService.setAuthorization(authorization);
        List<StockData> stockDatas = kiteDataService.geStockData(scripts);
        return ResponseEntity.status(HttpStatus.OK)
                .body(stockDatas);
    }

    @GetMapping("/GetLiveDatas")
    public ResponseEntity<List<StockData>> getLiveData(
            @RequestHeader(value = "Authorization", required = true) String authorization, String weeklyExpiry, String monthlyExpiry) {
        kiteDataService.setAuthorization(authorization);
        List<StockData> stockDatas = kiteDataService.geLiveData(weeklyExpiry, monthlyExpiry);
        return ResponseEntity.status(HttpStatus.OK)
                .body(stockDatas);

    }
}
