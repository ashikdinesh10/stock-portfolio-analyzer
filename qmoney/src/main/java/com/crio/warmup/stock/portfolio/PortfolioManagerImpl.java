package com.crio.warmup.stock.portfolio;

import com.crio.warmup.stock.dto.AnnualizedReturn;
import com.crio.warmup.stock.dto.Candle;
import com.crio.warmup.stock.dto.PortfolioTrade;
import com.crio.warmup.stock.exception.StockQuoteServiceException;
import com.crio.warmup.stock.quotes.StockQuotesService;
import com.fasterxml.jackson.core.JsonProcessingException;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.springframework.web.client.RestTemplate;

public class PortfolioManagerImpl implements PortfolioManager {

  private final Logger LOGGER = Logger.getLogger(PortfolioManagerImpl.class.getName());
  public RestTemplate restTemplate;

  public StockQuotesService stockQuotesService;
  
  // Caution: Do not delete or modify the constructor, or else your build will break!
  // This is absolutely necessary for backward compatibility
  protected PortfolioManagerImpl(RestTemplate restTemplate) {
    this.restTemplate = restTemplate;
  }

  protected PortfolioManagerImpl(StockQuotesService stockQuotesService) {
    this.stockQuotesService = stockQuotesService;
  }

  //TODO: CRIO_TASK_MODULE_REFACTOR
  // Now we want to convert our code into a module, so we will not call it from main anymore.
  // Copy your code from Module#3 PortfolioManagerApplication#calculateAnnualizedReturn
  // into #calculateAnnualizedReturn function here and make sure that it
  // follows the method signature.
  // Logic to read Json file and convert them into Objects will not be required further as our
  // clients will take care of it, going forward.
  // Test your code using Junits provided.
  // Make sure that all of the tests inside PortfolioManagerTest using command below -
  // ./gradlew test --tests PortfolioManagerTest
  // This will guard you against any regressions.
  // run ./gradlew build in order to test yout code, and make sure that
  // the tests and static code quality pass.

  //CHECKSTYLE:OFF

  private Comparator<AnnualizedReturn> getComparator() {
    return Comparator.comparing(AnnualizedReturn::getAnnualizedReturn).reversed();
  }

  public List<AnnualizedReturn> calculateAnnualizedReturn(List<PortfolioTrade> portfolioTrades,
        LocalDate endDate) throws JsonProcessingException, StockQuoteServiceException {

    List<AnnualizedReturn> annual = new ArrayList<>();
    for (int index = 0; index < portfolioTrades.size(); index++) {

      List<Candle> newlist = new ArrayList<>();
      newlist = getStockQuote(portfolioTrades.get(index).getSymbol(), portfolioTrades.get(index).getPurchaseDate(),
          endDate);
    if (newlist != null) {

      Double open = newlist.get(0).getOpen();
      Double close = newlist.get(newlist.size() - 1).getClose();
      Double totalReturns = (close - open) / open;
      LocalDate purchasedate = portfolioTrades.get(index).getPurchaseDate();
      Double days = (double) ChronoUnit.DAYS.between(purchasedate, endDate);
    
      Double y = days / 365;
      Double annualizedReturns = Math.pow(1 + totalReturns, 1 / y) - 1;
      annual.add(new AnnualizedReturn(portfolioTrades.get(index).getSymbol(), annualizedReturns, totalReturns));

    } else {
      throw new RuntimeException();
    }
      
  }
  Collections.sort(annual, getComparator());
    return annual;

  }

  public List<Candle> getStockQuote(String symbol, LocalDate startDate, LocalDate endDate) 
  throws JsonProcessingException {
    List<Candle> candles = new ArrayList<>();
    try {
      candles =  stockQuotesService.getStockQuote(symbol, startDate, endDate);
    } catch (StockQuoteServiceException e) {
      LOGGER.log(Level.SEVERE, "Exception occured while retrieving " + stockQuotesService.getClass().toString()
                   + "API" + e.getMessage());
    }

    return candles;
  }

  public AnnualizedReturn getAnnualizedReturn(PortfolioTrade trade, LocalDate endDate)
    throws StockQuoteServiceException {
  LocalDate startDate = trade.getPurchaseDate();
  String symbol = trade.getSymbol(); 
 
  Double buyPrice = 0.0, sellPrice = 0.0;
 
  try {
    LocalDate startLocalDate = trade.getPurchaseDate();
	List<Candle> stocksStartToEndFull = getStockQuote(symbol, startLocalDate, endDate);
 
    Collections.sort(stocksStartToEndFull, (candle1, candle2) -> { 
      return candle1.getDate().compareTo(candle2.getDate()); 
    });
    
    Candle stockStartDate = stocksStartToEndFull.get(0);
    Candle stocksLatest = stocksStartToEndFull.get(stocksStartToEndFull.size() - 1);
 
    buyPrice = stockStartDate.getOpen();
    sellPrice = stocksLatest.getClose();
    endDate = stocksLatest.getDate();
 
  } catch (JsonProcessingException e) {
    throw new RuntimeException();
  }
  Double totalReturn = (sellPrice - buyPrice) / buyPrice;
 
  long daysBetweenPurchaseAndSelling = ChronoUnit.DAYS.between(startDate, endDate);
  Double totalYears = (double) (daysBetweenPurchaseAndSelling) / 365;
 
  Double annualizedReturn = Math.pow((1 + totalReturn), (1 / totalYears)) - 1;
  return new AnnualizedReturn(symbol, annualizedReturn, totalReturn);
 
}

  @Override
public List<AnnualizedReturn> calculateAnnualizedReturnParallel(
    List<PortfolioTrade> portfolioTrades, LocalDate endDate, int numThreads) 
    throws InterruptedException, StockQuoteServiceException {

  List<AnnualizedReturn> annualizedReturns = new ArrayList<AnnualizedReturn>();
  List<Future<AnnualizedReturn>> futureReturnsList = new ArrayList<Future<AnnualizedReturn>>();
  final ExecutorService pool = Executors.newFixedThreadPool(numThreads);
  
   for (int i = 0; i < portfolioTrades.size(); i++) {
    PortfolioTrade trade = portfolioTrades.get(i);
    Callable<AnnualizedReturn> callableTask = () -> {
      return getAnnualizedReturn(trade, endDate);
    };
    Future<AnnualizedReturn> futureReturns = pool.submit(callableTask);
    futureReturnsList.add(futureReturns);
  }

  for (int i = 0; i < portfolioTrades.size(); i++) {
    Future<AnnualizedReturn> futureReturns = futureReturnsList.get(i);
    try {
      AnnualizedReturn returns = futureReturns.get();
      annualizedReturns.add(returns);
    } catch (ExecutionException e) {
      throw new StockQuoteServiceException("Error when calling the API", e);

    }
  }
  Collections.sort(annualizedReturns, getComparator());
  return annualizedReturns;
}


}




