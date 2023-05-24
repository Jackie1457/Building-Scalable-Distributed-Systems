import io.swagger.client.model.SwipeDetails;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ThreadLocalRandom;

class Producer implements Runnable {

  private static final String[] LEFT_OR_RIGHT = {"left", "right"};
  private static final int SWIPER_ID_LOWER_BOUND = 1;
  private static final int SWIPER_ID_UPPER_BOUND = 5000;
  private static final int SWIPEE_ID_LOWER_BOUND = 1;
  private static final int SWIPEE_ID_UPPER_BOUND = 1000000;
  private static final int COMMENT_MAX_LENGTH = 256;
  private final BlockingQueue<SwipeData> buffer;
  private final int total;
  private final int numOfThreads;

  public Producer(BlockingQueue<SwipeData> buffer, int total, int numOfThreads) {
    this.buffer = buffer;
    this.total = total;
    this.numOfThreads = numOfThreads;
  }

  @Override
  public void run() {
    produce();
  }

  private void produce() {
    try {
      for (int i = 0; i < total; i++) {
        SwipeData swipeData = generate();
        buffer.put(swipeData);
      }
      for (int j = 0; j < numOfThreads; j++) {
        SwipeData endOfData = new SwipeData("end", new SwipeDetails());
        buffer.put(endOfData);
      }
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
  }

  private SwipeData generate() {
    String leftOrRight = LEFT_OR_RIGHT[ThreadLocalRandom.current().nextInt(2)];
    String swiper = String.valueOf(
        ThreadLocalRandom.current().nextInt(SWIPER_ID_LOWER_BOUND, SWIPER_ID_UPPER_BOUND + 1));
    String swipee = String.valueOf(
        ThreadLocalRandom.current().nextInt(SWIPEE_ID_LOWER_BOUND, SWIPEE_ID_UPPER_BOUND + 1));
    SwipeDetails swipeDetails = new SwipeDetails();
    swipeDetails.setSwiper(swiper);
    swipeDetails.setSwipee(swipee);
    String comment = generateComment(COMMENT_MAX_LENGTH);
    swipeDetails.setComment(comment);
    return new SwipeData(leftOrRight, swipeDetails);
  }

  private String generateComment(int length) {
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < length; i++) {
      // randomly generate a printable character
      sb.append((char) ThreadLocalRandom.current().nextInt(33, 127));
    }
    return sb.toString();
  }
}
