import io.swagger.client.model.SwipeDetails;

public class SwipeData {

  private String leftOrRight;
  private SwipeDetails swipeDetails;

  public SwipeData(String leftOrRight, SwipeDetails swipeDetails) {
    this.leftOrRight = leftOrRight;
    this.swipeDetails = swipeDetails;
  }

  public String getLeftOrRight() {
    return leftOrRight;
  }

  public SwipeDetails getSwipeDetails() {
    return swipeDetails;
  }

  public void setLeftOrRight(String leftOrRight) {
    this.leftOrRight = leftOrRight;
  }

  public void setSwipeDetails(SwipeDetails swipeDetails) {
    this.swipeDetails = swipeDetails;
  }
}
