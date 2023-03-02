import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeoutException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet(name = "TwinderServlet", value = "/swipe/*")
public class TwinderServlet extends HttpServlet {
  private Gson gson = new Gson();
  private final static int SWPIER_MIN = 1;
  private final static int SWPIER_MAX = 5000;
  private final static int SWIPEE_MIN = 1;
  private final static int SWIPEE_MAX = 1000000;
  private static final String QUEUE_NAME = "TWINDER_DATA";
  private static final String EXCHANGE_NAME = "EXCHANGE";

  private static final int SIZE = 1000;
  private RabbitMQChannelPool rabbitMQChannelPool;

  public void init() {
    try {
      ConnectionFactory factory = new ConnectionFactory(); // connects to a RabbitMQ node using the given parameters

//      factory.setHost("localhost");
//      factory.setUsername("guest");
//      factory.setPassword("guest");
      factory.setHost("54.184.113.242");
      factory.setUsername("test");
      factory.setPassword("test");
//      factory.setVirtualHost("EXCHANGE");
      factory.setPort(5672);
      Connection connection = factory.newConnection();
      RabbitMQChannelFactory rabbitMQChannelFactory = new RabbitMQChannelFactory(connection);
      rabbitMQChannelPool = new RabbitMQChannelPool(SIZE, rabbitMQChannelFactory);
    } catch (IOException e) {
      throw new RuntimeException(e);
    } catch (TimeoutException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  protected void doGet(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {

  }

  @Override
  protected void doPost(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
    response.setContentType("application/json");
    String urlPath = request.getPathInfo();

    // check we have a URL!
    if (urlPath == null || urlPath.isEmpty()) {
      response.setStatus(HttpServletResponse.SC_NOT_FOUND);
      response.getWriter().write("missing parameters");
      return;
    }

    String[] urlParts = urlPath.split("/");

    if (isUrlValid(urlParts)) {
      StringBuilder sb = new StringBuilder();
      String s;
      while ((s = request.getReader().readLine()) != null) {
        sb.append(s);
      }
      SwipeDetails swipeDetails = gson.fromJson(sb.toString(), SwipeDetails.class);
      if (!swipeDetails.getSwiper().equals(null) && !swipeDetails.getSwiper().isEmpty() &&
          SWPIER_MIN <= Integer.parseInt(swipeDetails.getSwiper()) &&
          Integer.parseInt(swipeDetails.getSwiper()) <= SWPIER_MAX &&
          !swipeDetails.getSwipee().equals(null) && !swipeDetails.getSwipee().isEmpty() &&
          SWIPEE_MIN <= Integer.parseInt(swipeDetails.getSwipee()) &&
          Long.parseLong(swipeDetails.getSwipee()) <= SWIPEE_MAX &&
          swipeDetails.getComment().length() > 0 && swipeDetails.getComment().length() <= 256) {
        JsonObject swipeInfo = new JsonObject();
        swipeInfo.addProperty("leftorright", urlParts[1]);
        swipeInfo.addProperty("swiper", swipeDetails.getSwiper());
        swipeInfo.addProperty("swipee", swipeDetails.getSwipee());
        swipeInfo.addProperty("comment", swipeDetails.getComment());

        if(sendMessageToQueue(swipeInfo)){
          response.setStatus(HttpServletResponse.SC_CREATED);
          response.getOutputStream().println("Write successful!");
        }else{
          response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
          response.getOutputStream().println("Bad Request!");
        }
      } else {
        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        response.getOutputStream().println("Bad Request!");
      }
      response.getOutputStream().print(gson.toJson(swipeDetails));
      response.getOutputStream().flush();
    } else {
      response.setStatus(HttpServletResponse.SC_NOT_FOUND);
      response.getOutputStream().print("Invalid url!");
      response.getOutputStream().flush();
    }
  }

  private boolean isUrlValid(String[] urlPath) {
    if (urlPath.length != 2){
      return false;
    }
    if (!(urlPath[1].equals("left") || urlPath[1].equals("right"))){
      return false;
    }
    return true;
  }

  private boolean sendMessageToQueue(JsonObject message){
    try {
      Channel channel = rabbitMQChannelPool.borrowObject(); // get a channel from the pool
      channel.queueDeclare(QUEUE_NAME, false, false, false, null); // publish message
      channel.exchangeDeclare(EXCHANGE_NAME, "fanout");
      channel.basicPublish(EXCHANGE_NAME, "", null,
          message.toString().getBytes(StandardCharsets.UTF_8)); // To publish a message
      System.out.println(" [x] Sent '" + message.toString() + "'");
      rabbitMQChannelPool.returnObject(channel); // return channel to the pool
      return true;
    } catch (Exception e) {
      System.out.println("Failed to send message to RabbitMQ");
      return false;
    }
  }
}
