import java.util.Map;
import javax.servlet.*;
import javax.servlet.http.*;
import javax.servlet.annotation.*;
import java.io.IOException;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

@WebServlet(name = "StatsServlet", value = "/stats/*")
public class StatsServlet extends HttpServlet {
  private JedisPool jedisPool;

  @Override
  public void init() {
    JedisPoolConfig config = new JedisPoolConfig();
    config.setMaxTotal(1024);
//    this.jedisPool = new JedisPool(config, "52.35.159.86", 6379, 0);
    this.jedisPool = new JedisPool(config, "localhost", 6379);

  }

  @Override
  protected void doGet(HttpServletRequest request, HttpServletResponse response)
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
    if (!getUrlValid(urlParts)) {
      response.setStatus(HttpServletResponse.SC_NOT_FOUND);
      response.getOutputStream().print("Invalid url!");
      response.getOutputStream().flush();
    } else {
      Jedis jedis = jedisPool.getResource();
      String userId = urlParts[1];
      response.setStatus(HttpServletResponse.SC_OK);
      Map<String, String> fields = jedis.hgetAll(userId);
      response.getWriter().write("{numLikes:"+ fields.get("numLikes") + "," + "numDislikes:" + fields.get("numDislikes") +"}");
    }
  }

  private boolean getUrlValid(String[] urlPath) {
    if (urlPath.length != 2){
      return false;
    }
    try {
      Integer userId = Integer.parseInt(urlPath[1]);
    } catch (NumberFormatException nfe) {
      return false;
    }
    return true;
  }

  @Override
  protected void doPost(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {

  }
}
