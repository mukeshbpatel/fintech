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

import com.amp.fintech.model.Candle;
import com.amp.fintech.model.Script;
import com.amp.fintech.model.StockData;
import com.amp.fintech.model.KiteData.Instrument;
import com.amp.fintech.model.KiteData.KiteResponse;
import com.amp.fintech.model.KiteData.Quotes;
import com.amp.fintech.service.KiteDataService;
import com.amp.fintech.service.Utility;
import com.amp.fintech.utility.DateUtil;
import com.amp.fintech.utility.JsonUtil;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.csv.*;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class KiteDataServiceImpl implements KiteDataService {

        private final String auth = "enctoken dgk5Yj4V7s6MWBph3pQ+aQrC0vUTwvXwTPss9NHrI6XB1WDpbfn+QKATuO8hN9myohuFTDM0CHP4nveEVNlrm3XN1s+kz8xPXH5Anqci5bGX+RlUalxrRw==";

        @Override
        public List<Quotes> getQuotes(List<Instrument> instruments) {
                try {
                        StringBuilder uri = new StringBuilder();
                        uri.append(Utility.ZerodhaQuotesUrl);
                        instruments.forEach(instrument-> {
                                uri.append("i=" + instrument.getExchange() + ":" + instrument.getTradingsymbol()  + "&");
                        });

                        HttpHeaders headers = new HttpHeaders();
                        headers.add("Authorization", auth);
                        RestTemplate restTemplate = new RestTemplate();
                        HttpEntity<String> entity = new HttpEntity<String>("parameters", headers);
                        ResponseEntity<String> response = restTemplate.exchange(uri.toString(), HttpMethod.GET, entity,String.class);
                        String kiteResponse = response.getBody()
                                                .replace("{\"status\":\"success\",\"data\":{","[")
                                                .replace("}}}}","}}]").replace("NSE:","");

                        
                        for(int i=0; i< instruments.size(); i++) {
                                Instrument instrument = instruments.get(i);
                                kiteResponse = kiteResponse.replace("\"" + instrument.getTradingsymbol()  + "\":{\"instrument_token\":", "{\"instrument\":\"" + instrument.getTradingsymbol()  + "\",\"instrument_token\":");
                        } 
                        
                        ObjectMapper mapper = JsonUtil.getMapper();
                        List<Quotes> result = mapper.readValue(kiteResponse,new TypeReference<List<Quotes>> () {});
                        instruments.forEach(instrument-> {
                                List<Quotes> quotes = result.stream().filter(f-> f.getInstrumentToken().equals(instrument.getInstrument_token())).toList();
                                if (quotes!=null && !quotes.isEmpty()) {
                                        instrument.setQuotes(quotes.get(0));
                                }
                        });

                        return result;

                } catch (Exception ex) {
                        log.error("Exception", ex);
                        return null;
                }
        }

        @Override
        public StockData geStockData(Script script) {
                try {
                        String uri = Utility.ZerodhaApiUrl;
                        uri = uri.replace("{key}", script.getApiKey())
                                        .replace("{from}", script.getStartDate())
                                        .replace("{to}", script.getEndDate())
                                        .replace("{timeFrame}", script.getTimeFrame());

                        HttpHeaders headers = new HttpHeaders();
                        headers.add("Authorization", auth);
                        RestTemplate restTemplate = new RestTemplate();
                        HttpEntity<String> entity = new HttpEntity<String>("parameters", headers);
                        ResponseEntity<KiteResponse> response = restTemplate.exchange(uri, HttpMethod.GET, entity,KiteResponse.class);
                        KiteResponse kiteResponse = response.getBody();

                        StockData stockData = StockData.builder()
                                        .candles(kiteResponse.getData().getData())
                                        .script(script)
                                        .build();

                        if (script.isFillTechnicalAnalysis()) {
                                BarSeries series = new BaseBarSeriesBuilder().withName(script.getName()).build();
                                BarSeries vseries = new BaseBarSeriesBuilder().withName("V" + script.getName()).build();
                                stockData.getCandles().forEach(c -> {
                                        series.addBar(c.getDate().atZone(ZoneId.systemDefault()),
                                                        c.getOpen(), c.getHigh(), c.getLow(), c.getClose(),
                                                        c.getVolume());
                                        vseries.addBar(c.getDate().atZone(ZoneId.systemDefault()),
                                                        c.getVolume(), c.getVolume(), c.getVolume(), c.getVolume(),
                                                        c.getVolume());
                                });
                                // stockData.updateSMA();
                                stockData = setTechnicalIndicator(stockData, series, vseries);
                        }
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
                        stockDatas.add(geStockData(script));
                });
                log.info("Get Script Data Completed");
                return stockDatas;
        }

        @Override
        public List<Instrument> getInstruments(String weeklyExpiry, String monthlyExpiry) {
                try {
                        URL src = new URL(Utility.ZerodhaInstrumentUrl);
                        CsvSchema csv = CsvSchema.emptySchema().withHeader();
                        CsvMapper csvMapper = new CsvMapper();
                        
                        log.info("Getting Intrument list from Kite API");
                        MappingIterator<Instrument> mappingIterator = csvMapper.reader().forType(Instrument.class)
                                        .with(csv).readValues(src);
                        log.info("Intrument list sucessfully downloaded");

                        List<Instrument> result = new ArrayList<>();

                        List<Instrument> list = mappingIterator.readAll().stream()
                                        .filter(f -> f.getExchange().equals("NSE") || f.getExchange().equals("NFO"))
                                        .collect(Collectors.toList());
                        
                        log.info("Full List count {}", list.size());

                        // Get Future List
                        List<Instrument> futureList = list.stream().filter(f -> f.getSegment().equals("NFO-FUT")
                                        && f.getExpiry().equals(monthlyExpiry))
                                        .collect(Collectors.toList());

                        log.info("Future List count {}", futureList.size());

                        // Get Future - Equity
                        List<String> equities = futureList.stream().map(Instrument::getName).distinct()
                                        .collect(Collectors.toList());

                        // Get Equity List
                        List<Instrument> equityList = list.stream().filter(f -> f.getInstrument_type().equals("EQ")
                                        && equities.contains(f.getTradingsymbol()))
                                        .collect(Collectors.toList());

                        log.info("Equity List count {}", equityList.size());

                        // Get index List (Nifty, Bank, Fin)
                        List<Instrument> indexList = list.stream().filter(f -> f.getSegment().equals("INDICES")
                                        && f.getExchange().equals("NSE")
                                        && (f.getName().equals("NIFTY 50") || f.getName().equals("NIFTY BANK")
                                                        || f.getName().equals("NIFTY FIN SERVICE")))
                                        .collect(Collectors.toList());

                        log.info("Index List count {}", indexList.size());

                        // Get Future List
                        List<Instrument> optionList = list.stream().filter(f -> f.getSegment().equals("NFO-OPT")
                                        && f.getExpiry().startsWith(monthlyExpiry.substring(0, 7)))
                                        .collect(Collectors.toList());

                        getQuotes(indexList);
                        log.info("Index Quotes count {}", indexList.size());

                        indexList.forEach(index -> {
                                if (index.getName().equals("NIFTY 50")) {
                                        index.setName("NIFTY");
                                } else if (index.getName().equals("NIFTY BANK")) {
                                        index.setName("BANKNIFTY");
                                } else if (index.getName().equals("NIFTY FIN SERVICE")) {
                                        index.setName("FINNIFTY");
                                }

                                List<Instrument> future = futureList.stream()
                                                .filter(f -> f.getName().equals(index.getName()))
                                                .collect(Collectors.toList());

                                if (future != null && !future.isEmpty()) {
                                        index.setFuture(future.get(0));
                                }

                                index.setOption(extractOption(optionList, Script.builder()
                                                .apiKey(index.getInstrument_token())
                                                .name(index.getName())
                                                .startDate(DateUtil.getCurrentDate(-7))
                                                .endDate(DateUtil.getCurrentDate())
                                                .timeFrame("day")
                                                .build(), weeklyExpiry, monthlyExpiry, index));
                                result.add(index);
                        });

                        log.info("Index option list completed");
                        
                        getQuotes(equityList);
                        log.info("Stock Quotes count {}", equityList.size());

                        equityList.forEach(equity -> {
                                List<Instrument> eqyList = futureList.stream()
                                                .filter(f -> f.getName().equals(equity.getTradingsymbol()))
                                                .collect(Collectors.toList());

                                if (eqyList != null && !eqyList.isEmpty()) {
                                        equity.setFuture(eqyList.get(0));
                                }

                                equity.setOption(extractOption(optionList, Script.builder()
                                                .apiKey(equity.getInstrument_token())
                                                .name(equity.getTradingsymbol())
                                                .startDate(DateUtil.getCurrentDate(-7))
                                                .endDate(DateUtil.getCurrentDate())
                                                .timeFrame("day")
                                                .build(), weeklyExpiry, monthlyExpiry, equity));

                                result.add(equity);

                        });

                        log.info("Stock option list completed");

                        return result;

                } catch (Exception e) {
                        e.printStackTrace();
                        return null;
                }
        }

        private List<Instrument> extractOption(List<Instrument> list, Script script, String weeklyExpiry, String monthlyExpiry, Instrument instrument) {
                
                List<Instrument> result = new ArrayList<>();
                double close = (double) 0;
                
                if (instrument.getQuotes()!=null && instrument.getQuotes().getLastPrice()>0) {
                        close = instrument.getQuotes().getLastPrice();
                } else {

                StockData stockData = geStockData(script);
                if (stockData.getCandles() != null && !stockData.getCandles().isEmpty()) {

                        Candle candle = stockData.getCandles().get(stockData.getCandles().size() - 1);
                        close = candle.getClose();
                        log.info("{} : Close {} ", stockData.getScript().getName(), candle.getClose());
                }
        }
                if (close>0) {
                        String exp = monthlyExpiry;
                        if (!weeklyExpiry.equals(monthlyExpiry) && script.getName().equals("BANKNIFTY")) {
                                exp = DateUtil.addDays(weeklyExpiry, -1);
                        }
                        if (!weeklyExpiry.equals(monthlyExpiry) && script.getName().equals("FINNIFTY")) {
                                exp = DateUtil.addDays(weeklyExpiry, -2);
                        } else if (!weeklyExpiry.equals(monthlyExpiry) && script.getName().equals("NIFTY")) {
                                exp = weeklyExpiry;
                        }

                        final String expiry = exp;

                        List<Instrument> fullList = list.stream().filter(f -> f.getSegment().equals("NFO-OPT")
                                        && f.getName().equals(script.getName())
                                        && f.getExpiry().equals(expiry)).collect(Collectors.toList());

                        final Double fclose = close;
                        result.addAll(fullList.stream().filter(f -> f.getStrike() >= fclose).limit(4)
                                        .collect(Collectors.toList()));

                        result.addAll(fullList.stream()
                                        .sorted((object1, object2) -> object2.getStrike()
                                                        .compareTo(object1.getStrike()))
                                        .filter(f -> f.getStrike() <= fclose).limit(4)
                                        .collect(Collectors.toList()));

                        return result.stream().sorted(
                                        (object1, object2) -> object1.getStrike().compareTo(object2.getStrike()))
                                        .toList();
                } else {
                        return null;
                }

        }

        @Override
        public List<StockData> geLiveData(String weeklyExpiry, String monthlyExpiry) {

                List<Script> scripts = new ArrayList<>();
                List<Instrument> instruments = getInstruments(weeklyExpiry, monthlyExpiry);
                instruments.forEach(instrument -> {
                        scripts.add(Script.builder()
                                        .apiKey(instrument.getInstrument_token())
                                        .name(instrument.getTradingsymbol())
                                        .startDate("2023-10-01")
                                        .endDate(monthlyExpiry)
                                        .build());
                });

                log.info("Script Count : {}", scripts.size());
                return geStockData(scripts);
        }

}