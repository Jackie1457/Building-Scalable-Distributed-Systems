package MyConsumer2;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.DeliverCallback;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;

public class RabbitMQChannel implements Runnable{
  private final Connection connection;
  private HashMap<String, ArrayList<String>> numOfUsersSwipeRightOn;
  private static final String QUEUE_NAME = "TWINDER_DATA";
  private static final String EXCHANGE_NAME = "EXCHANGE";
  private ArrayList<String> swipeeRightOn = new ArrayList<>();
  private int count = 0;

  public RabbitMQChannel(Connection connection,
      HashMap<String, ArrayList<String>> numOfUsersSwipeRightOn) {
    this.connection = connection;
    this.numOfUsersSwipeRightOn = numOfUsersSwipeRightOn;
  }

  @Override
  public void run(){

      try {
        Channel channel = connection.createChannel();
        channel.exchangeDeclare(EXCHANGE_NAME, "fanout");
        String queueName = channel.queueDeclare().getQueue();
//        channel.queueDeclare(QUEUE_NAME, false, false, false, null);
        channel.queueBind(queueName, EXCHANGE_NAME, "fanout");
        System.out.println(" [*] Waiting for messages. To exit press CTRL+C");
        channel.basicQos(1); // Per consumer limit, receive a maximum of 10 unacknowledged messages at once,accept only 1 unacknowledged message
        Gson gson = new Gson();
        DeliverCallback threadCallback = (consumerTag, delivery) -> {
          String message = new String(delivery.getBody(), StandardCharsets.UTF_8);
//          System.out.println(" [x] Received '" + message + "'");
          count ++;
          System.out.println(" [x] Count '" + count + "'");
          JsonObject json = gson.fromJson(message, JsonObject.class);
          if (numOfUsersSwipeRightOn.size() == 0 || !numOfUsersSwipeRightOn.containsKey(
              String.valueOf(json.get("swiper")))) {
            if (String.valueOf(json.get("leftorright")).equals("right")) {
              swipeeRightOn.add(String.valueOf(json.get("swipee")));
              numOfUsersSwipeRightOn.put(String.valueOf(json.get("swiper")), swipeeRightOn);
            }
          } else {
            if (String.valueOf(json.get("leftorright")).equals("right")) {
              if (swipeeRightOn.size() == 0){
                swipeeRightOn.add(String.valueOf(json.get("swipee")));
                numOfUsersSwipeRightOn.put(String.valueOf(json.get("swiper")), swipeeRightOn);
              }
              if (swipeeRightOn.size() > 0 && swipeeRightOn.size() <= 100) {
                swipeeRightOn.add(String.valueOf(json.get("swipee")));
                numOfUsersSwipeRightOn.put(String.valueOf(json.get("swiper")), swipeeRightOn);
              }
              if (swipeeRightOn.size() > 100) {
                swipeeRightOn.remove(0);
                numOfUsersSwipeRightOn.put(String.valueOf(json.get("swiper")), swipeeRightOn);
              }
            }
          }
          channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
        };
        boolean autoAck = false;
        channel.basicConsume(queueName, autoAck, threadCallback, consumerTag -> { });
      } catch (IOException e) {
        e.printStackTrace();
      }
  }
}

