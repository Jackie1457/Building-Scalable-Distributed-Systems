import Models.DataGeneration;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ThreadLocalRandom;

public class Producer implements Runnable {
  private int totalRequests;
  private final BlockingQueue<DataGeneration> buffer;

  public Producer(int totalRequests,
      BlockingQueue<DataGeneration> buffer) {
    this.totalRequests = totalRequests;
    this.buffer = buffer;
  }

  @Override
  public void run() {
    int count = 0;
    while (count < totalRequests){
      try{
        this.buffer.put(random());
        count++;
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
    }
  }

  private DataGeneration random(){
    String swipe;
    if((int)(Math.random()*2) == 0){
      swipe = "left";
    } else{
      swipe = "right";
    }
    String swiper = String.valueOf(ThreadLocalRandom.current().nextInt(1, 5000));
    String swipee = String.valueOf(ThreadLocalRandom.current().nextInt(1, 1000000));
    String comment = "You are not my type!";
    DataGeneration dataGeneration = new DataGeneration(swipe, swiper, swipee, comment);
    return dataGeneration;
  }
}
