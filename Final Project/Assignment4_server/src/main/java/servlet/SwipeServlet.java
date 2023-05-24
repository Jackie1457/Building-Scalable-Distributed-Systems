package servlet;

import com.google.gson.Gson;
import dao.SwipeDataDynamoDao;
import java.io.IOException;
import javax.servlet.*;
import javax.servlet.http.*;
import model.SwipeDetails;

public class SwipeServlet extends HttpServlet {

  private static final int SWIPER_ID_LOWER_BOUND = 1;
  private static final int SWIPER_ID_UPPER_BOUND = 5000;
  private static final int SWIPEE_ID_LOWER_BOUND = 1;
  private static final int SWIPEE_ID_UPPER_BOUND = 1000000;
  private static final int COMMENT_MAX_LENGTH = 256;
  private SwipeDataDynamoDao swipeDataDynamoDao;

  @Override
  public void init() {
    try {
      swipeDataDynamoDao = new SwipeDataDynamoDao();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  @Override
  protected void doGet(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
  }

  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse res)
      throws IOException {
    processRequest(req, res);
  }

  private void processRequest(HttpServletRequest req, HttpServletResponse res) throws IOException {
    res.setContentType("application/json");
    String urlPath = req.getPathInfo();
    if (urlPath == null || urlPath.isEmpty()) {
      res.setStatus(HttpServletResponse.SC_NOT_FOUND);
      res.getWriter().write("Missing parameters!");
      return;
    }
    String[] urlParts = urlPath.split("/");
    if (urlParts.length != 2 || !(urlParts[1].equals("left") || urlParts[1].equals("right"))) {
      res.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      res.getOutputStream().println("Invalid inputs!");
      res.getOutputStream().flush();
      return;
    }
    Gson gson = new Gson();
    try {
      StringBuilder sb = new StringBuilder();
      String s;
      while ((s = req.getReader().readLine()) != null) {
        sb.append(s);
      }
      SwipeDetails swipeDetails = gson.fromJson(sb.toString(), SwipeDetails.class);
      String swiper = swipeDetails.getSwiper();
      if (!(Integer.parseInt(swiper) >= SWIPER_ID_LOWER_BOUND
          && Integer.parseInt(swiper) <= SWIPER_ID_UPPER_BOUND)) {
        res.setStatus(HttpServletResponse.SC_NOT_FOUND);
        res.getOutputStream().println("User not found!");
        res.getOutputStream().flush();
        return;
      }
      String swipee = swipeDetails.getSwipee();
      if (!(Integer.parseInt(swipee) >= SWIPEE_ID_LOWER_BOUND
          && Integer.parseInt(swipee) <= SWIPEE_ID_UPPER_BOUND)) {
        res.setStatus(HttpServletResponse.SC_NOT_FOUND);
        res.getOutputStream().println("User not found!");
        res.getOutputStream().flush();
        return;
      }
      if (swipeDetails.getComment().length() > COMMENT_MAX_LENGTH) {
        res.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        res.getOutputStream().println("Invalid inputs!");
        res.getOutputStream().flush();
      }
      String leftOrRight = urlParts[1];
      swipeDataDynamoDao.updateUserLikesDislikes(swiper, leftOrRight);
      if (leftOrRight.equals("right")) {
        swipeDataDynamoDao.insertUserSwipeRight(swiper, swipee);
      }
      res.setStatus(HttpServletResponse.SC_CREATED);
      res.getOutputStream().println("Write successfully!");
      res.getOutputStream().flush();
    } catch (Exception ex) {
      res.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      res.getOutputStream().println("Invalid inputs!");
      res.getOutputStream().flush();
    }
  }
}