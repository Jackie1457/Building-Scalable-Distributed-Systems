import Models.DataGeneration;
import Models.Result;
import io.swagger.client.ApiClient;
import io.swagger.client.ApiException;
import io.swagger.client.ApiResponse;
import io.swagger.client.api.SwipeApi;
import io.swagger.client.model.SwipeDetails;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;

public class Consumer implements Runnable{
  private final BlockingQueue<DataGeneration> buffer;
  private Result result;
  private CountDownLatch countDownLatch;
  private int requestsNum;
  private DataGeneration dataGeneration;
  private final static String BASE_PATH = "http://34.208.186.90:8080/TwinderPostApi_war/";


  public Consumer(BlockingQueue<DataGeneration> buffer, Result result,
      CountDownLatch countDownLatch, int requestsNum) {
    this.buffer = buffer;
    this.result = result;
    this.countDownLatch = countDownLatch;
    this.requestsNum = requestsNum;
  }

  @Override
  public void run() {
    ApiClient apiClient = new ApiClient();
    apiClient.setBasePath(BASE_PATH);
    SwipeApi apiInstance = new SwipeApi(apiClient);
    for(int i = 0; i < requestsNum; i++){ // one thread to process all requests
      try{
        dataGeneration = this.buffer.take();
      } catch (InterruptedException e) {
        e.printStackTrace();
      }

      SwipeDetails body = new SwipeDetails(); // generate the values needed for the operation
      body.setSwiper(dataGeneration.getSwiper());
      body.setSwipee(dataGeneration.getSwipee());
      body.setComment(dataGeneration.getComment());
      String leftOrRight = "left";

      try {
        ApiResponse<Void> response = apiInstance.swipeWithHttpInfo(body, leftOrRight); // execute the POST
        int retryTimes = 0;
        if(response.getStatusCode() == 200 || response.getStatusCode() == 201){ // wait for the HTTP response code
          result.addSuccessfulRequests(1);
          result.addTotalRequests(1);
        }else{
          while (response.getStatusCode() != 200 || response.getStatusCode() != 201){
            response = apiInstance.swipeWithHttpInfo(body, leftOrRight);
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
    }
    this.countDownLatch.countDown();
  }
}
