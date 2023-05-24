package model;

public class StatsResponse {
  private Integer numLikes;
  private Integer numDislikes;

  public StatsResponse(Integer numLikes, Integer numDislikes) {
    this.numLikes = numLikes;
    this.numDislikes = numDislikes;
  }
}
