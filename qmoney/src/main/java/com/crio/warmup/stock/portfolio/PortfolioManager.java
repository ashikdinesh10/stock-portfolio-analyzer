package com.crio.warmup.stock.portfolio;

import com.crio.warmup.stock.dto.AnnualizedReturn;
import com.crio.warmup.stock.dto.PortfolioTrade;
import com.crio.warmup.stock.exception.StockQuoteServiceException;
import com.fasterxml.jackson.core.JsonProcessingException;

import java.time.LocalDate;
import java.util.List;

public interface PortfolioManager {

  List<AnnualizedReturn> calculateAnnualizedReturnParallel(
      List<PortfolioTrade> portfolioTrades,
      LocalDate endDate, int numThreads) throws InterruptedException,
      StockQuoteServiceException, JsonProcessingException;

List<AnnualizedReturn> calculateAnnualizedReturn(List<PortfolioTrade> asList, LocalDate endDate) 
throws JsonProcessingException, StockQuoteServiceException;

  //CHECKSTYLE:OFF

}

