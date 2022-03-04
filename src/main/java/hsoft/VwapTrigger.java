package hsoft;

import com.hsoft.codingtest.DataProvider;
import com.hsoft.codingtest.DataProviderFactory;
import com.hsoft.codingtest.MarketDataListener;
import com.hsoft.codingtest.PricingDataListener;
import org.apache.log4j.LogManager;

public class VwapTrigger {
  public static void main(String[] args) {
    DataProvider provider = DataProviderFactory.getDataProvider();
    provider.addMarketDataListener(new MarketDataListener() {
      public void transactionOccured(String productId, long quantity, double price) {
        // TODO Start to code here when a transaction occurred
      }
    });
    provider.addPricingDataListener(new PricingDataListener() {
      public void fairValueChanged(String productId, double fairValue) {
        // TODO Start to code here when a fair value changed
      }
    });

    provider.listen();
    // When this method returns, the test is finished and you can check your results in the console
    LogManager.shutdown();
  }
}