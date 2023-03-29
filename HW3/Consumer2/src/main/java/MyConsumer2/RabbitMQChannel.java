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
import java.util.List;
import java.util.Map;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

public class RabbitMQChannel implements Runnable{
  private final Connection connection;
//  private HashMap<String, ArrayList<String>> numOfUsersSwipeRightOn;
  private JedisPool pool;
  private static final String QUEUE_NAME = "TWINDER_DATA";
  private static final String EXCHANGE_NAME = "EXCHANGE";
  private ArrayList<String> swipeeRightOn = new ArrayList<>();
  private int count = 0;

  public RabbitMQChannel(Connection connection, JedisPool pool) {
    this.connection = connection;
    this.pool = pool;
  }

  @Override
  public void run(){

      try{
        Channel channel = connection.createChannel();
        channel.exchangeDeclare(EXCHANGE_NAME, "fanout", true);
        channel.queueDeclare(QUEUE_NAME, true, false, false, null);
        channel.queueBind(QUEUE_NAME, EXCHANGE_NAME, "fanout");
        System.out.println(" [*] Waiting for messages. To exit press CTRL+C");
        channel.basicQos(1); // Per consumer limit, receive a maximum of 10 unacknowledged messages at once,accept only 1 unacknowledged message
        Gson gson = new Gson();
        DeliverCallback threadCallback = (consumerTag, delivery) -> {
          String message = new String(delivery.getBody(), StandardCharsets.UTF_8);
          JsonObject json = gson.fromJson(message, JsonObject.class);
          String leftOrRight = String.valueOf(json.get("leftorright"));
          String swiper = String.valueOf(json.get("swiper"));
          swiper = swiper.substring(1, swiper.length() - 1);
          String swipee = String.valueOf(json.get("swipee"));
          swipee = swipee.substring(1,swipee.length() - 1);
          String comment = String.valueOf(json.get("comment"));
          Jedis jedis = null;
          try {
            synchronized (pool) {
              jedis = this.pool.getResource();
//              if (leftOrRight.contains("right")) {
//                String matchListKey = swiper + ":matchList";
//                jedis.lpush(matchListKey, swipee);
//                if (jedis.llen(matchListKey) > 100) {
//                  jedis.rpop(matchListKey);
//                }
//              }
              Map<String, String> matchList =
                  jedis.exists(swiper) ? jedis.hgetAll(swiper) : new HashMap<>();
              if(leftOrRight.contains("right")) {
                if (matchList.containsKey("matchList") && jedis.llen(String.valueOf(matchList)) <= 100 && jedis.llen(
                    String.valueOf(matchList)) > 0) {
                  jedis.hset(swiper, "matchList", matchList.get(swiper) + swipee);
                } else {
                  jedis.hset(swiper, "matchList", swipee);
                }
              }
            }
          } catch (Exception e) {
            System.out.println("Error while handling Redis connection: " + e.getMessage());
          } finally {
            if (jedis != null)
              jedis.close();
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

