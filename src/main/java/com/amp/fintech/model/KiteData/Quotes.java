package com.amp.fintech.model.KiteData;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.jackson.Jacksonized;

@Builder
@Getter
@Setter
@Jacksonized
@JsonIgnoreProperties(ignoreUnknown = true)
public class Quotes implements Serializable {
    @JsonProperty(value = "instrument")
    private final String instrument;
    @JsonProperty(value = "instrument_token")
    private final String instrumentToken;
    @JsonProperty(value = "last_price")
    private final  double lastPrice;
    @JsonProperty(value = "ohlc")
    private final Ohlc ohlc;
}
