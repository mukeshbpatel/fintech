package com.amp.fintech.model.KiteData;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.jackson.Jacksonized;

@Builder
@Getter
@Setter
@Jacksonized
@JsonIgnoreProperties(ignoreUnknown = true)
public class Ohlc implements Serializable {
  private final double open;
  private final double high;
  private final double low;
  private final double close;
}
