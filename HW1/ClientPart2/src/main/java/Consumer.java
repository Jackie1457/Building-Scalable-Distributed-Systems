import Models.DataGeneration;
import Models.Record;
import Models.Result;
import io.swagger.client.ApiClient;
import io.swagger.client.ApiException;
import io.swagger.client.ApiResponse;
import io.swagger.client.api.SwipeApi;
import io.swagger.client.model.SwipeDetails;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;

public class Consumer implements Runnable{
  private final BlockingQueue<DataGeneration> buffer;
  private Result result;
  private CountDownLatch countDownLatch;
  private int requestsNum;
  private DataGeneration dataGeneration;
  private List<Record> records;
  private final static String BASE_PATH = "http://54.202.76.143:8080/TwinderPostApi_war/";


  public Consumer(BlockingQueue<DataGeneration> buffer, Result result,
      CountDownLatch countDownLatch, int requestsNum,
      List<Record> records) {
    this.buffer = buffer;
    this.result = result;
    this.countDownLatch = countDownLatch;
    this.requestsNum = requestsNum;
    this.records = records;
  }

  @Override
  public void run() {
    ApiClient apiClient = new ApiClient();
    apiClient.setBasePath(BASE_PATH);
    SwipeApi apiInstance = new SwipeApi(apiClient);
    for(int i = 0; i < requestsNum; i++){ // one thread to process all requests
      try{
        dataGeneration = buffer.take();
      } catch (InterruptedException e) {
        e.printStackTrace();
      }

      SwipeDetails body = new SwipeDetails(); // generate the values needed for the operation
      body.setSwiper(dataGeneration.getSwiper());
      body.setSwipee(dataGeneration.getSwipee());
      body.setComment(dataGeneration.getComment());
      String leftOrRight = "left";

      long start = System.currentTimeMillis();
      int statusCode = 0;
      try {
        ApiResponse response = apiInstance.swipeWithHttpInfo(body, leftOrRight); // execute the POST
        statusCode = response.getStatusCode();
        int retryTimes = 0;
        if(statusCode == 200 || statusCode == 201){ // wait for the HTTP response code
          result.addSuccessfulRequests(1);
          result.addTotalRequests(1);
        }else{
          while (statusCode != 200 || statusCode != 201){
            response = apiInstance.swipeWithHttpInfo(body, leftOrRight);
            statusCode = response.getStatusCode();
            retryTimes++;
            if(retryTimes >= 5){ // retry times
              result.addUnsuccessfulRequests(1);
              result.addTotalRequests(1);
            }
          }
        }
      } catch (ApiException e) {
        e.printStackTrace();
      }
      long end = System.currentTimeMillis(); //end 记录下来
      long latency = end - start; //milliseconds
      this.records.add(new Record(start, "POST",
          latency, statusCode));
    }
    this.countDownLatch.countDown();
  }
}
