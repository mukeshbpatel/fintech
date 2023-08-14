package com.amp.fintech.controller;

import java.util.List;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import com.amp.fintech.model.Candle;
import com.amp.fintech.model.StockData;
import com.amp.fintech.utility.Constant;

@RestController
@RequestMapping("test")
public class hello {
    
    @GetMapping("/hello")
    public String helloWold() {
        return "Hello World!";
    }

    @GetMapping("/GetData") 
    public List<Candle> getData() {
        String uri = Constant.ZerodhaApiUrl;
        uri = uri.replace("{key}", "263689").replace("{from}", "2023-08-14").replace("{to}", "2023-08-14");

        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "enctoken TdyLVyCYRAf6a03vBttIhla2q5OEOcF9lGsWvQuu8Tw/FBVFJgvZQUb+L+tOJqU3A3tjEcm6mNf0R/rjhpQ7y+uRJfJzY/PphahgWLSTnm1h9LZORmsETg==");

        RestTemplate restTemplate = new RestTemplate();
        HttpEntity<String> entity = new HttpEntity<String>("parameters", headers);
        ResponseEntity<StockData> response = restTemplate.exchange(uri, HttpMethod.GET, entity, StockData.class);

        return response.getBody().data.getData();
    }
}
