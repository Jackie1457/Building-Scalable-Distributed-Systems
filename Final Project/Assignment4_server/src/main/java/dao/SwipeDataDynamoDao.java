package dao;

import software.amazon.awssdk.auth.credentials.AwsSessionCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.DynamoDbException;
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest;
import software.amazon.awssdk.services.dynamodb.model.ReturnValue;
import software.amazon.awssdk.services.dynamodb.model.UpdateItemRequest;
import software.amazon.awssdk.utils.ImmutableMap;

public class SwipeDataDynamoDao {

  private DynamoDbClient dynamoDbClient;

  public SwipeDataDynamoDao() {
    String accessKeyId = "ASIA5G5IHQAC26M7B6P4";
    String secretAccessKey = "g0il/FBkJB3ti9GDefxAb3tENqPGJlqQyBTbkCwm";
    String sessionToken = "FwoGZXIvYXdzEBAaDKGRLnFuSuXj4HIAGCLLARiuVAzu11EHr9diKkvAGGSo2+rg7ANh/8YKf2NNWSICONYqd9Kn7Qpicz7Pk8bR8D7Qk2wI5c3mN3KZj6h8o0SPygUYsU7+4ZlMeXlVok8iUa4/qJZOez0UuH8GLSphUeJQ8KIXBDZ98W7ecB15d8Aao3MQ+w+s9iKOxLHFQF9UGWSPaKwvJdfxFjUKHEqo/wtc1l+vL5/8iWLQKFxScHwybGtgKf3LIlDKkqvQu0VISo5YQmr+0cPpyquY9CPIGhAzFOcoEBZJssEiKKHx46EGMi3jxFwfAqbjSJocXvIw+LS2KSmVQyf8wqOiLHOH1yFlEEBqKdKH3aA6N7h6mqU=";
    AwsSessionCredentials awsCredentials = AwsSessionCredentials.create(accessKeyId,
        secretAccessKey, sessionToken);
    StaticCredentialsProvider credentialsProvider = StaticCredentialsProvider.create(
        awsCredentials);
    dynamoDbClient = DynamoDbClient.builder()
        .region(Region.US_WEST_2)
        .credentialsProvider(credentialsProvider)
        .build();
  }

    public void updateUserLikesDislikes(String swiper, String leftOrRight) {
    String tableName = "user_likes_dislikes";
    String columnName = leftOrRight.equals("right") ? "numLikes" : "numDislikes";
    UpdateItemRequest updateRequest = UpdateItemRequest.builder()
        .tableName(tableName)
        .key(java.util.Collections.singletonMap("swiper",
            AttributeValue.builder().s(swiper).build()))
        .updateExpression("ADD " + columnName + " :incr")
        .expressionAttributeValues(
            java.util.Collections.singletonMap(":incr", AttributeValue.builder().n("1").build()))
        .returnValues(ReturnValue.UPDATED_NEW)
        .build();
    try {
      dynamoDbClient.updateItem(updateRequest);
    } catch (DynamoDbException e) {
      System.err.println("Unable to update user likes/dislikes for swiper: " + swiper);
      System.err.println(e.getMessage());
    }
  }

  public void insertUserSwipeRight(String swiper, String swipee) {
    String tableName = "user_swipe_right";
    long timestamp = System.currentTimeMillis();
    PutItemRequest putRequest = PutItemRequest.builder()
        .tableName(tableName)
        .item(ImmutableMap.of(
            "swiper", AttributeValue.builder().s(swiper).build(),
            "swipee", AttributeValue.builder().s(swipee).build(),
            "timestamp", AttributeValue.builder().n(String.valueOf(timestamp)).build()))
        .build();
    try {
      dynamoDbClient.putItem(putRequest);
    } catch (DynamoDbException e) {
      System.err.println(
          "Unable to insert user swipe right event for swiper: " + swiper + " and swipee: "
              + swipee);
      System.err.println(e.getMessage());
    }
  }
}
