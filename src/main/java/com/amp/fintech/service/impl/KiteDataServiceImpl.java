package com.amp.fintech.service.impl;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.amp.fintech.model.Script;
import com.amp.fintech.model.StockData;
import com.amp.fintech.model.KiteData.Instrument;
import com.amp.fintech.model.KiteData.KiteResponse;
import com.amp.fintech.service.KiteDataService;
import com.amp.fintech.service.Utility;
import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.dataformat.csv.*;

@Service
public class KiteDataServiceImpl implements KiteDataService {

        public StockData geStockData(Script script) {

                String uri = Utility.ZerodhaApiUrl;
                uri = uri.replace("{key}", script.getApiKey()).replace("{from}", script.getStartDate()).replace("{to}",
                                script.getEndDate());

                HttpHeaders headers = new HttpHeaders();
                headers.add("Authorization", script.getAuth());

                RestTemplate restTemplate = new RestTemplate();
                HttpEntity<String> entity = new HttpEntity<String>("parameters", headers);
                ResponseEntity<KiteResponse> response = restTemplate.exchange(uri, HttpMethod.GET, entity,
                                KiteResponse.class);
                KiteResponse kiteResponse = response.getBody();

                StockData stockData = StockData.builder()
                                .candles(kiteResponse.getData().getData())
                                .script(script)
                                .build();

                stockData.updateSMA();

                return stockData;
        }

        public List<StockData> geStockData(List<Script> scripts) {
                List<StockData> stockDatas = new ArrayList<>();
                scripts.stream().parallel().forEach(script -> {
                        String uri = Utility.ZerodhaApiUrl;
                        uri = uri.replace("{key}", script.getApiKey()).replace("{from}", script.getStartDate()).replace(
                                        "{to}",
                                        script.getEndDate());

                        HttpHeaders headers = new HttpHeaders();
                        headers.add("Authorization", script.getAuth());

                        RestTemplate restTemplate = new RestTemplate();
                        HttpEntity<String> entity = new HttpEntity<String>("parameters", headers);
                        ResponseEntity<KiteResponse> response = restTemplate.exchange(uri, HttpMethod.GET, entity,
                                        KiteResponse.class);
                        KiteResponse kiteResponse = response.getBody();

                        StockData stockData = StockData.builder()
                                        .candles(kiteResponse.getData().getData())
                                        .script(script)
                                        .build();

                        stockData.updateSMA();
                        stockDatas.add(stockData);
                });

                return stockDatas;
        }

        @Override
        public List<Instrument> csvToMap(String expiry) {
                try {
                        URL src = new URL(Utility.ZerodhaInstrumentUrl);
                        CsvSchema csv = CsvSchema.emptySchema().withHeader();
                        CsvMapper csvMapper = new CsvMapper();
                        MappingIterator<Instrument> mappingIterator = csvMapper.reader().forType(Instrument.class)
                                        .with(csv).readValues(src);
                        List<Instrument> list = mappingIterator.readAll();
                        return list.stream()
                                        .filter(f -> ((f.getName().equals("NIFTY") && f.getStrike()>=19000 && f.getStrike()<=19200)
                                                        || (f.getName().equals("BANKNIFTY") && f.getStrike()>=45500 && f.getStrike()<=45900))
                                                        && f.getExpiry().equals(expiry))
                                        .collect(Collectors.toList());
                } catch (Exception e) {
                        e.printStackTrace();
                        return null;
                }
        }

}