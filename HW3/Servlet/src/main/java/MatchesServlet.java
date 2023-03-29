import com.google.gson.Gson;
import java.util.List;
import java.util.Map;
import javax.servlet.*;
import javax.servlet.http.*;
import javax.servlet.annotation.*;
import java.io.IOException;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

@WebServlet(name = "MatchesServlet", value = "/matches/*")
public class MatchesServlet extends HttpServlet {
  private JedisPool jedisPool;
  private Gson gson = new Gson();
  @Override
  public void init() {
    JedisPoolConfig config = new JedisPoolConfig();
    config.setMaxTotal(1024);
//    this.jedisPool = new JedisPool(config, "52.35.159.86", 6379);
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
      try {
        Jedis jedis = jedisPool.getResource();
        String userId = urlParts[1];
//        String matchListKey = userId + ":matchList";
//        if (result != null) {
//          response.setStatus(HttpServletResponse.SC_OK);
//          response.getWriter().write("{matches:" + result +"}");
//        } else {
//          response.setStatus(HttpServletResponse.SC_NOT_FOUND);
//          response.getWriter().write("{No match found}");
//        }
        Map<String, String> fields = jedis.hgetAll(userId);
        if(fields.get("matchList") != null){
          response.setStatus(HttpServletResponse.SC_OK);
          response.getWriter().write("{matches:" + fields.get("matchList")+ "}");
        }else{
          response.setStatus(HttpServletResponse.SC_NOT_FOUND);
          response.getWriter().write("{No match found}");
        }

      }catch (Exception e){
        e.printStackTrace();
      }
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
