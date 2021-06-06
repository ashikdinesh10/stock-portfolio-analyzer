package com.crio.warmup.stock.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDate;
import java.util.Map;
import java.util.HashMap;

@JsonIgnoreProperties(ignoreUnknown = true)
public class AlphavantageDailyResponse {

  @JsonProperty(value = "Time Series (Daily)")
  private HashMap<LocalDate, AlphavantageCandle> candles;

  public HashMap<LocalDate, AlphavantageCandle> getCandles() {
    return candles;
  }

  public void setCandles(HashMap<LocalDate, AlphavantageCandle> candles) {
    this.candles = candles;
  }
  
}
