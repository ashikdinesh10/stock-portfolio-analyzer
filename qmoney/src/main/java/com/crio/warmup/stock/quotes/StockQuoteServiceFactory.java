package com.crio.warmup.stock.quotes;

import org.springframework.web.client.RestTemplate;

public final class StockQuoteServiceFactory {

  public static final StockQuoteServiceFactory INSTANCE = new StockQuoteServiceFactory();

  private StockQuoteServiceFactory() {

  }

  // TODO: CRIO_TASK_MODULE_ADDITIONAL_REFACTOR
  //  Make sure that you have implemented TiingoService and AlphavantageService
  //  as per the instructions and the tests are passing for them.
  //  Implement the factory function such that it will return TiingoService instance when
  //  provider == 'tiingo' (Case insensitive)
  //  Return new instance of AlphavantageService in all other cases.
  //  RestTemplate is passed as a parameter along, and you will have to pass the same to
  //  the constructor of corresponding class.
  //  Run the tests using command below and make sure it passes
  //  ./gradlew test --tests StockQuoteServiceFactory

  public static StockQuotesService getService(String provider,  RestTemplate restTemplate) {

    if(provider == null) {
      return new AlphavantageService(restTemplate);
    }
    else if(provider.equalsIgnoreCase("tiingo")) {
      return new TiingoService(restTemplate);
    }
    else  
      return new AlphavantageService(restTemplate);
  }
}
