package com.amp.fintech.model;

import java.io.Serializable;
import java.util.Date;

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
public class Candle implements Serializable {
    
   private Date date;
   private double open;
   private double high;
   private double low;
   private double close;
  private int volume;
  private int oi;
}
