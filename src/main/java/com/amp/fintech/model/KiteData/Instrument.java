package com.amp.fintech.model.KiteData;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.jackson.Jacksonized;

@Jacksonized
@Builder
@Getter
@Setter
public class Instrument implements Serializable {

    @JsonProperty(value = "instrument_token")
    private String instrument_token;

    @JsonProperty(value = "exchange_token")
    private String exchange_token;

    @JsonProperty(value = "tradingsymbol")
    private String tradingsymbol;

    @JsonProperty(value = "name")
    private String name;

    @JsonProperty(value = "last_price")
    private Double last_price;

    @JsonProperty(value = "expiry")
    private String expiry;

    @JsonProperty(value = "strike")
    private Double strike;

    @JsonProperty(value = "tick_size")
    private String tick_size;

    @JsonProperty(value = "lot_size")
    private String lot_size;

    @JsonProperty(value = "instrument_type")
    private String instrument_type;

    @JsonProperty(value = "segment")
    private String segment;

    @JsonProperty(value = "exchange")
    private String exchange;
}
