package MyConsumer1;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.DeliverCallback;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;

public class RabbitMQChannel implements Runnable{
  private final Connection connection;
  private HashMap<String, Integer[]> numOfLeftOrRight;
  private static final String QUEUE_NAME = "TWINDER_DATA";
  private static final String EXCHANGE_NAME = "EXCHANGE";
  private int count = 0;
  private Integer[] leftOrRightNum = {0, 0};

  public RabbitMQChannel(Connection connection,
      HashMap<String, Integer[]> numOfLeftOrRight) {
    this.connection = connection;
    this.numOfLeftOrRight = numOfLeftOrRight;
  }

  @Override
  public void run(){

      try {
        Channel channel = connection.createChannel();
        channel.exchangeDeclare(EXCHANGE_NAME, "fanout");
        channel.queueDeclare(QUEUE_NAME, false, false, false, null);
        channel.queueBind(QUEUE_NAME, EXCHANGE_NAME, "fanout");
        System.out.println(" [*] Waiting for messages. To exit press CTRL+C");
        channel.basicQos(1); // Per consumer limit, receive a maximum of 10 unacknowledged messages at once,accept only 1 unacknowledged message
        Gson gson = new Gson();
        DeliverCallback threadCallback = (consumerTag, delivery) -> {
          String message = new String(delivery.getBody(), StandardCharsets.UTF_8);
//          System.out.println(" [x] Received '" + message + "'");
//          count ++;
//          System.out.println(" [x] Count '" + count + "'");
          JsonObject json = gson.fromJson(message, JsonObject.class);
          if (numOfLeftOrRight == null || !numOfLeftOrRight.containsKey(
              String.valueOf(json.get("swiper")))) {
            if (String.valueOf(json.get("leftorright")).equals("left")) {
              leftOrRightNum[0] = leftOrRightNum[0] + 1;
              numOfLeftOrRight.put(String.valueOf(json.get("swiper")), leftOrRightNum);

            } else {
              leftOrRightNum[1] = leftOrRightNum[1] + 1;
              numOfLeftOrRight.put(String.valueOf(json.get("swiper")), leftOrRightNum);
            }
          } else {
            if (String.valueOf(json.get("leftorright")).equals("left")) {
              numOfLeftOrRight.get(String.valueOf(json.get("swiper")))[0] =
                  numOfLeftOrRight.get(String.valueOf(json.get("swiper")))[0] + 1;
            } else {
              numOfLeftOrRight.get(String.valueOf(json.get("swiper")))[1] =
                  numOfLeftOrRight.get(String.valueOf(json.get("swiper")))[1] + 1;
            }
          }
          channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
        };
        boolean autoAck = false;
        channel.basicConsume(QUEUE_NAME, autoAck, threadCallback, consumerTag -> { });
      } catch (IOException e) {
        e.printStackTrace();
      }
  }
}

