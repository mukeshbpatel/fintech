package com.amp.fintech.service.impl;

import java.util.Date;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.amp.fintech.model.Script;
import com.amp.fintech.model.StockData;
import com.amp.fintech.model.KiteData.KiteResponse;
import com.amp.fintech.service.KiteDataService;
import com.amp.fintech.service.Utility;

@Service
public class KiteDataServiceImpl implements KiteDataService {

    public StockData geStockData(Script script) {

        String uri = Utility.ZerodhaApiUrl;
        uri = uri.replace("{key}", script.getApiKey()).replace("{from}", script.getStartDate()).replace("{to}",
                script.getEndDate());

        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization",
                "enctoken zkJJf8bcHZR9jG5RlUPcDLwxhTqj1GQ2mA9VfiepIh8A+7s5Mx2zkQKCY2s7o+VAvPUEldrpole02slYF7CYY14D4i5/HXDK3lePpL/1PMFSJcvg2UHgPA==");

        RestTemplate restTemplate = new RestTemplate();
        HttpEntity<String> entity = new HttpEntity<String>("parameters", headers);
        ResponseEntity<KiteResponse> response = restTemplate.exchange(uri, HttpMethod.GET, entity, KiteResponse.class);
        KiteResponse kiteResponse = response.getBody();

        StockData stockData = StockData.builder()
                .candles(kiteResponse.getData().getData())
                .script(script)
                .build();

        stockData.updateSMA();

        return stockData;
    }

}