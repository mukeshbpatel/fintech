package com.amp.fintech.service.impl;

import java.io.File;
import java.net.URL;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.ta4j.core.BarSeries;
import org.ta4j.core.BaseBarSeriesBuilder;
import org.ta4j.core.indicators.EMAIndicator;
import org.ta4j.core.indicators.RSIIndicator;
import org.ta4j.core.indicators.SMAIndicator;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;

import com.amp.fintech.model.Script;
import com.amp.fintech.model.StockData;
import com.amp.fintech.model.KiteData.Instrument;
import com.amp.fintech.model.KiteData.KiteResponse;
import com.amp.fintech.service.KiteDataService;
import com.amp.fintech.service.Utility;
import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.dataformat.csv.*;

import lombok.extern.slf4j.Slf4j;


@Service
@Slf4j
public class KiteDataServiceImpl implements KiteDataService {

        @Override
        public StockData geStockData(Script script) {
                try {
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

                        BarSeries series = new BaseBarSeriesBuilder().withName(script.getName()).build();
                        BarSeries vseries = new BaseBarSeriesBuilder().withName("V" + script.getName()).build();
                        stockData.getCandles().forEach(c -> {
                                series.addBar(c.getDate().atZone(ZoneId.systemDefault()),
                                                c.getOpen(), c.getHigh(), c.getLow(), c.getClose(), c.getVolume());
                                vseries.addBar(c.getDate().atZone(ZoneId.systemDefault()),
                                                c.getVolume(), c.getVolume(), c.getVolume(), c.getVolume(), c.getVolume());
                        });

                        // stockData.updateSMA();

                        stockData = setTechnicalIndicator(stockData, series, vseries);
                        return stockData;
                } catch (Exception ex) {
                        log.error("Exception", ex);
                        return null;
                }

        }

        private StockData setTechnicalIndicator(StockData stockData, BarSeries series, BarSeries vseries) {
                ClosePriceIndicator closePrice = new ClosePriceIndicator(series);
                ClosePriceIndicator vclosePrice = new ClosePriceIndicator(vseries);

                EMAIndicator ema9 = new EMAIndicator(closePrice, 9);
                EMAIndicator ema20 = new EMAIndicator(closePrice, 20);
                EMAIndicator ema50 = new EMAIndicator(closePrice, 50);
                EMAIndicator ema100 = new EMAIndicator(closePrice, 100);
                EMAIndicator ema200 = new EMAIndicator(closePrice, 200);

                SMAIndicator shortSma = new SMAIndicator(vclosePrice, 200);

                RSIIndicator rsi = new RSIIndicator(closePrice, 14);

                final int bars = series.getBarCount();
                for (int i = 0; i < bars; i++) {
                        var candle = stockData.getCandles().get(i);
                        candle.setSma9(Double.parseDouble(ema9.getValue(i).toString()));
                        candle.setSma20(Double.parseDouble(ema20.getValue(i).toString()));
                        candle.setSma50(Double.parseDouble(ema50.getValue(i).toString()));
                        candle.setSma100(Double.parseDouble(ema100.getValue(i).toString()));
                        candle.setSma200(Double.parseDouble(ema200.getValue(i).toString()));
                        candle.setVolumeSma200(Double.parseDouble(shortSma.getValue(i).toString()));
                        candle.setRsi(Double.parseDouble(rsi.getValue(i).toString()));
                        stockData.getCandles().set(i, candle);
                }
                return stockData;
        }

        @Override
        public List<StockData> geStockData(List<Script> scripts) {
                List<StockData> stockDatas = new ArrayList<>();
                log.info("Get Script Data Started");
                scripts.stream().forEach(script -> {
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
                log.info("Get Script Data Completed");
                return stockDatas;
        }

        @Override
        public List<Instrument> csvToMap(String weeklyExpiry, String monthlyExpiry) {
                try {
                        URL src = new URL(Utility.ZerodhaInstrumentUrl);
                        CsvSchema csv = CsvSchema.emptySchema().withHeader();
                        CsvMapper csvMapper = new CsvMapper();
                        MappingIterator<Instrument> mappingIterator = csvMapper.reader().forType(Instrument.class)
                                        .with(csv).readValues(src);
                        List<Instrument> list = mappingIterator.readAll();
                        var filteredList = list.stream().filter(f -> ((f.getExchange().equals("NSE")
                                        && f.getName().equals("NIFTY 50") && f.getInstrument_type().equals("EQ")) ||
                                        (f.getExchange().equals("NSE") && f.getName().equals("NIFTY BANK")
                                                        && f.getInstrument_type().equals("EQ"))
                                        ||
                                        (f.getExchange().equals("NFO") && f.getExpiry().equals(monthlyExpiry)
                                                        && f.getName().equals("NIFTY")
                                                        && f.getInstrument_type().equals("FUT"))
                                        ||
                                        (f.getExchange().equals("NFO") && f.getExpiry().equals(monthlyExpiry)
                                                        && f.getName().equals("BANKNIFTY")
                                                        && f.getInstrument_type().equals("FUT"))
                                        ||
                                        (f.getExchange().equals("NFO") && f.getExpiry().equals("2023-10-12")
                                                        && f.getName().equals("NIFTY") && f.getStrike() >= 19400
                                                        && f.getStrike() <= 19700)
                                        ||
                                        (f.getExchange().equals("NFO") && f.getExpiry().equals("2023-10-11")
                                                        && f.getName().equals("BANKNIFTY") && f.getStrike() >= 43500
                                                        && f.getStrike() <= 44400)))
                                        .collect(Collectors.toList());
                        return filteredList.stream()
                                        .sorted((object1, object2) -> object1.getSegment()
                                                        .compareTo(object2.getSegment()))
                                        .toList();

                } catch (Exception e) {
                        e.printStackTrace();
                        return null;
                }
        }

        @Override
        public List<StockData> geLiveData(String weeklyExpiry, String monthlyExpiry) {

                List<Script> scripts = new ArrayList<>();
                List<Instrument> instruments = csvToMap(weeklyExpiry, monthlyExpiry);
                instruments.forEach(instrument -> {
                        scripts.add(Script.builder()
                                        .apiKey(instrument.getInstrument_token())
                                        .name(instrument.getTradingsymbol())
                                        .startDate("2023-10-01")
                                        .endDate(monthlyExpiry)
                                        .auth("enctoken U0OhDcGK2PgRm+86mRWaiRbKAV07I2GXQXJezy4iRLe2Xwc8rhkLSwvSGnGV7mhdnfu5mwo+Lc7X4QGGghqDqiypvDNlFfTlE3j8yzUZu5y0Alexm74Atw==")
                                        .build());
                });

                log.info("Script Count : {}", scripts.size());

                return geStockData(scripts);
        }

}