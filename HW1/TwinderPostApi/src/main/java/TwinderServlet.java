import com.google.gson.Gson;
import javax.servlet.*;
import javax.servlet.http.*;
import javax.servlet.annotation.*;
import java.io.IOException;

@WebServlet(name = "TwinderServlet", value = "/swipe/*")
public class TwinderServlet extends HttpServlet {

  private Gson gson = new Gson();
  private final static int SWPIER_MIN = 1;
  private final static int SWPIER_MAX = 5000;
  private final static int SWIPEE_MIN = 1;
  private final static int SWIPEE_MAX = 1000000;

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
        SwipeDetails swipeDetails = (SwipeDetails) gson.fromJson(sb.toString(), SwipeDetails.class);
        if (!swipeDetails.getSwiper().equals(null) && !swipeDetails.getSwiper().isEmpty() &&
            SWPIER_MIN <= Integer.parseInt(swipeDetails.getSwiper()) &&
            Integer.parseInt(swipeDetails.getSwiper()) <= SWPIER_MAX &&
            !swipeDetails.getSwipee().equals(null) && !swipeDetails.getSwipee().isEmpty() &&
            SWIPEE_MIN <= Integer.parseInt(swipeDetails.getSwipee()) &&
            Long.parseLong(swipeDetails.getSwipee()) <= SWIPEE_MAX &&
            swipeDetails.getComment().length() > 0 && swipeDetails.getComment().length() <= 256) {
          response.setStatus(HttpServletResponse.SC_CREATED);
          response.getOutputStream().println("Write successful!");
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
}
