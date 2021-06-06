package com.crio.warmup.stock;

import com.crio.warmup.stock.dto.PortfolioTrade;
import com.crio.warmup.stock.dto.TiingoCandle;

import com.crio.warmup.stock.dto.AnnualizedReturn;
import com.crio.warmup.stock.dto.PortfolioTrade;
import com.crio.warmup.stock.dto.TotalReturnsDto;
import com.crio.warmup.stock.log.UncaughtExceptionHandler;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.crio.warmup.stock.portfolio.PortfolioManager;
import com.crio.warmup.stock.portfolio.PortfolioManagerFactory;
import com.crio.warmup.stock.quotes.StockQuotesService;
import com.crio.warmup.stock.quotes.TiingoService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;
import org.apache.logging.log4j.ThreadContext;
import org.springframework.web.client.RestTemplate;

public class PortfolioManagerApplication {

  public static List<String> mainReadFile(String[] args) throws IOException, URISyntaxException {
    ObjectMapper objectMapper = new ObjectMapper();
    // Read the json file provided in the argument[0]. The file will be available in
    // the classpath.
    File file = resolveFileFromResources(args[0]);
    // 2. Extract stock symbols from the json file with ObjectMapper provided by
    // #getObjectMapper.
    PortfolioTrade[] portfolioTrade = objectMapper.readValue(file, PortfolioTrade[].class);
    ArrayList<String> temp = new ArrayList<>();
    // 3. Return the list of all symbols in the same order as provided in json.
    for (int i = 0; i < portfolioTrade.length; i++) {
      temp.add(portfolioTrade[i].getSymbol());
    }
    return temp;

    // return
    // Stream.of(portfolioTrade).map(PortfolioTrade::getSymbol).collect(Collectors.toList());
  }

  // TODO: CRIO_TASK_MODULE_REST_API
  // Find out the closing price of each stock on the end_date and return the list
  // of all symbols in ascending order by its close value on end date.

  // Note:
  // 1. You may have to register on Tiingo to get the api_token.
  // 2. Look at args parameter and the module instructions carefully.
  // 2. You can copy relevant code from #mainReadFile to parse the Json.
  // 3. Use RestTemplate#getForObject in order to call the API,
  // and deserialize the results in List<Candle>

  public static List<String> mainReadQuotes(String[] args) throws IOException, URISyntaxException {
    ObjectMapper objectmapper = getObjectMapper();
    RestTemplate restTemplate = new RestTemplate();
    File newfile = resolveFileFromResources(args[0]);
    PortfolioTrade[] portfolioTrade = objectmapper.readValue(newfile, PortfolioTrade[].class);
    List<String> list = new ArrayList<>();

    if (portfolioTrade == null) {
      return Collections.emptyList();
    } else {
      for (int i = 0; i < portfolioTrade.length; i++) {
        list.add(portfolioTrade[i].getSymbol());
      }
    }

    LocalDate newdate = LocalDate.parse(args[1]);
    for (int r = 0; r < list.size(); r++) {
      if (newdate.compareTo((portfolioTrade[r].getPurchaseDate())) < 0) {
        throw new RuntimeException();
      }

    }
    
    // Creating a list of TotalReturnsDto
    List<TotalReturnsDto> newList = new ArrayList<>();
    for (int index = 0; index < list.size(); index++) {

      // Getting the response from Tingo API through rest template
      TiingoCandle[] result = restTemplate.getForObject(
          "https://api.tiingo.com/tiingo/daily/{ticker}/prices?"
              + "startDate={startdate}&endDate={enddate}&token={token}",
          TiingoCandle[].class, portfolioTrade[index].getSymbol(), portfolioTrade[index].getPurchaseDate(), args[1],
          "ba4f9b6f7ae38f0aef613a47ec5f218ba5206b8b");
      if (result != null) {

        for (int j = 0; j < result.length; j++) {
          if (j == result.length - 1) {
            // adding TotalReturnsDto to the newList based on closing price
            newList.add(new TotalReturnsDto(portfolioTrade[index].getSymbol(), result[j].getClose()));
          }
        }
      } else {
        throw new RuntimeException();
      }
    }

    // Sorting ToatlReturnsDto based on closing price by making use of comparator
    Collections.sort(newList, new Comparator<TotalReturnsDto>() {

      @Override
      public int compare(TotalReturnsDto o1, TotalReturnsDto o2) {
        return o1.getClosingPrice().compareTo(o2.getClosingPrice());
      }

    });

    List<String> sortedlist = new ArrayList<>();
    for (int p = 0; p < list.size(); p++) {
      sortedlist.add(newList.get(p).getSymbol());
    }

    return sortedlist;
  }

  private static void printJsonObject(Object object) throws IOException {
    Logger logger = Logger.getLogger(PortfolioManagerApplication.class.getCanonicalName());
    ObjectMapper mapper = new ObjectMapper();
    logger.info(mapper.writeValueAsString(object));
  }

  private static File resolveFileFromResources(String filename) throws URISyntaxException {
    return Paths.get(
        Thread.currentThread().getContextClassLoader().getResource(filename).toURI()).toFile();
  }

  private static ObjectMapper getObjectMapper() {
    ObjectMapper objectMapper = new ObjectMapper();
    objectMapper.registerModule(new JavaTimeModule());
    return objectMapper;
  }

  public static List<String> debugOutputs() {

     String valueOfArgument0 = "trades.json";
     String resultOfResolveFilePathArgs0 = "/home/crio-user/workspace/dineshashik9-ME_QMONEY/qmoney/src/main/resources/trades.json";
     String toStringOfObjectMapper = "com.fasterxml.jackson.databind.ObjectMapper@7fc4780b";
     String functionNameFromTestFileInStackTrace = "mainReadFile()";
     String lineNumberFromTestFileInStackTrace = "35:1";


    return Arrays.asList(new String[]{valueOfArgument0, resultOfResolveFilePathArgs0,
        toStringOfObjectMapper, functionNameFromTestFileInStackTrace,
        lineNumberFromTestFileInStackTrace});
  }


  // TODO: CRIO_TASK_MODULE_CALCULATIONS
  //  Now that you have the list of PortfolioTrade and their data, calculate annualized returns
  //  for the stocks provided in the Json.
  //  Use the function you just wrote #calculateAnnualizedReturns.
  //  Return the list of AnnualizedReturns sorted by annualizedReturns in descending order.

  // Note:
  // 1. You may need to copy relevant code from #mainReadQuotes to parse the Json.
  // 2. Remember to get the latest quotes from Tiingo API.

  public static List<AnnualizedReturn> mainCalculateSingleReturn(String[] args)
  throws IOException, URISyntaxException {
    ObjectMapper objectMapper = getObjectMapper();
    File trades = resolveFileFromResources(args[0]);
    LocalDate endDate = LocalDate.parse(args[1]);
    List<AnnualizedReturn> annualizedReturns = new ArrayList();

    PortfolioTrade[] portfolioTrades = objectMapper.readValue(trades, PortfolioTrade[].class);

    for(int i = 0; i < portfolioTrades.length; i++) {
      annualizedReturns.add(getAnnualizedReturn(portfolioTrades[i], endDate));
    }

    Comparator<AnnualizedReturn> sortByAnnualizedReturn = Comparator.comparing(AnnualizedReturn::getAnnualizedReturn)
     .reversed();
    Collections.sort(annualizedReturns, sortByAnnualizedReturn);
    
    return annualizedReturns;
  }

  private static AnnualizedReturn getAnnualizedReturn(PortfolioTrade trade, LocalDate endDate) {

    String TOKEN = "ba4f9b6f7ae38f0aef613a47ec5f218ba5206b8b";
    RestTemplate restTemplate = new RestTemplate();
    LocalDate startDate = trade.getPurchaseDate();
    String symbol = trade.getSymbol();
    AnnualizedReturn annualizedReturn = new AnnualizedReturn(symbol, Double.NaN, Double.NaN);

    if(startDate.compareTo(endDate) >= 0) {
      throw new RuntimeException();
    }

    //create url for Rest API call
    String url = String.format("https://api.tiingo.com/tiingo/daily/%s/prices?" + "startDate=%s&endDate=%s&token=%s", symbol,
                              startDate.toString(), endDate.toString(), TOKEN);

    //rest api call
    TiingoCandle[] stocksStartToEndDate = restTemplate.getForObject(url, TiingoCandle[].class);

    if(stocksStartToEndDate!= null) {
      TiingoCandle stockStartDate = stocksStartToEndDate[0];
      TiingoCandle stockEndDate = stocksStartToEndDate[stocksStartToEndDate.length - 1];

      Double buyPrice = stockStartDate.getOpen();
      Double sellPrice = stockEndDate.getClose();

      annualizedReturn = calculateAnnualizedReturns(endDate, trade, buyPrice, sellPrice);
  
    }
    return annualizedReturn;

  }

  public static AnnualizedReturn calculateAnnualizedReturns(LocalDate endDate,
      PortfolioTrade trade, Double buyPrice, Double sellPrice) {
      
        //Calculate totalReturn = (sell_value - buy_value) / buy_value.
        Double totalReturns = (sellPrice - buyPrice) / buyPrice;
        Double days = (double) ChronoUnit.DAYS.between(trade.getPurchaseDate(), endDate);
        
        Double y = days / 365;
        //Calculate extrapolated annualized returns by scaling the same in years span.
        //The formula is:
        //annualized_returns = (1 + total_returns) ^ (1 / total_num_years) - 1        
        Double annualizedReturns = Math.pow(1 + totalReturns, 1 / y) - 1;
        return new AnnualizedReturn(trade.getSymbol(), annualizedReturns, totalReturns);
  }


  public static List<AnnualizedReturn> mainCalculateReturnsAfterRefactor(String[] args)
      throws Exception {
        RestTemplate restTemplate = new RestTemplate();
        PortfolioManager portfolioManager = PortfolioManagerFactory.getPortfolioManager("tiingo", restTemplate);
        
        // String file = args[0];
        ObjectMapper objectmapper = getObjectMapper();
        File newfile = resolveFileFromResources(args[0]);
        PortfolioTrade[] portfolioTrades = objectmapper.readValue(newfile, PortfolioTrade[].class);
        
        LocalDate endDate = LocalDate.parse(args[1]);
        // String contents = readFileAsString(file);
        return portfolioManager.calculateAnnualizedReturn(Arrays.asList(portfolioTrades), endDate);
  }


  public static void main(String[] args) throws Exception {
    Thread.setDefaultUncaughtExceptionHandler(new UncaughtExceptionHandler());
    ThreadContext.put("runId", UUID.randomUUID().toString());

   printJsonObject(mainReadFile(args));

   debugOutputs();

   printJsonObject(mainReadQuotes(args));
   printJsonObject(mainCalculateSingleReturn(args));
   printJsonObject(mainCalculateReturnsAfterRefactor(args));
  }
}

