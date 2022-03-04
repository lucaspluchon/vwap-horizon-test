package hsoft;

import com.hsoft.codingtest.DataProvider;
import com.hsoft.codingtest.DataProviderFactory;
import com.hsoft.codingtest.MarketDataListener;
import com.hsoft.codingtest.PricingDataListener;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.util.Objects;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ConcurrentHashMap;

public class VwapTrigger {

  private static final Logger logger = Logger.getLogger(VwapTrigger.class);

  //Using ConcurrentHashMap to be thread-safe
  private static final ConcurrentHashMap<String, Double> fairValueData = new ConcurrentHashMap<>();

  //Using ArrayBlockingQueue to be thread safe
  private static final ConcurrentHashMap<String, ArrayBlockingQueue<Double[]>> transactionData = new ConcurrentHashMap<>();
  private static final ConcurrentHashMap<String, Double> vwapData = new ConcurrentHashMap<>();

  //Store a transaction in a queue of pair : transaction[0] = quantity, transaction[1] = price
  static private void storeTransaction(String productId, long quantity, double price) throws InterruptedException
  {
    if(!transactionData.containsKey(productId))
      transactionData.put(productId, new ArrayBlockingQueue<>(5));

    ArrayBlockingQueue<Double[]> transactionQueue = transactionData.get(productId);

    if (transactionQueue.remainingCapacity() == 0)
      transactionQueue.remove();
    transactionQueue.offer(new Double[]{(double) quantity, price});

  }

  static private double computeVwap(String productId)
  {
    ArrayBlockingQueue<Double[]> transactionQueue = transactionData.get(productId);
    double sumPriceQuantity = 0;
    double sumQuantity = 0;

    for (Double[] transaction : transactionQueue)
    {
      sumPriceQuantity += (transaction[0] * transaction[1]);
      sumQuantity += transaction[0];
    }

    return sumPriceQuantity / sumQuantity;
  }

  static private void checkVwapFair(String productId)
  {
    if (Objects.equals(productId, "TEST_PRODUCT") &&
            vwapData.containsKey(productId) && fairValueData.containsKey(productId))
    {
      double vwap = vwapData.get(productId);
      double fairValue = fairValueData.get(productId);
      if (vwap > fairValue)
      {
        logger.info(String.format("VWAP(%f) > FairValue(%.1f)", vwap, fairValue));
      }
    }
  }

  public static void main(String[] args)
  {
    DataProvider provider = DataProviderFactory.getDataProvider();

    provider.addMarketDataListener(new MarketDataListener()
    {
      public void transactionOccured(String productId, long quantity, double price)
      {
        try
        {
          storeTransaction(productId, quantity, price);
        }
        catch (InterruptedException e)
        {
          e.printStackTrace();
        }
          vwapData.put(productId, computeVwap(productId));
        checkVwapFair(productId);
      }
    });
    provider.addPricingDataListener(new PricingDataListener()
    {
      public void fairValueChanged(String productId, double fairValue)
      {
        fairValueData.put(productId, fairValue);
        checkVwapFair(productId);
      }
    });

    provider.listen();
    // When this method returns, the test is finished and you can check your results in the console
    LogManager.shutdown();
  }
}