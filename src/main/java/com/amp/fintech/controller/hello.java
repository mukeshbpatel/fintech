package com.amp.fintech.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.amp.fintech.model.Script;
import com.amp.fintech.model.StockData;
import com.amp.fintech.service.KiteDataService;

@RestController
@RequestMapping("test")
public class hello {

    @Autowired
    private KiteDataService kiteDataService;

    @GetMapping("/hello")
    public String helloWold() {
        return "Hello World!";
    }

    @GetMapping("/GetData")
    public StockData getData() {

        return kiteDataService.geStockData(Script.builder()
                .name("Nifty").apiKey("256265").startDate("2023-08-01").endDate("2023-08-20")
                .build());

    }
}
