package servlet;

import com.google.gson.Gson;
import dao.SwipeDataRdsDao;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import javax.servlet.http.*;
import model.MatchesResponse;
import model.Message;

public class MatchesServlet extends HttpServlet {

  private SwipeDataRdsDao swipeDataRdsDao;
  private Gson gson;

  @Override
  public void init() {
    try {
      this.swipeDataRdsDao = new SwipeDataRdsDao();
    } catch (SQLException e) {
      e.printStackTrace();
    }
    gson = new Gson();
  }

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse res)
      throws IOException {
    processRequest(req, res);
  }

  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse res)
      throws IOException {
  }

  private void processRequest(HttpServletRequest req, HttpServletResponse res) throws IOException {
    res.setContentType("application/json");
    String urlPath = req.getPathInfo();
    if (urlPath == null || urlPath.isEmpty()) {
      res.setStatus(HttpServletResponse.SC_NOT_FOUND);
      res.getWriter().println(gson.toJson(new Message("Missing parameters!")));
      return;
    }
    String[] urlParts = urlPath.split("/");
    if (urlParts.length != 2) {
      res.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      res.getWriter().println(gson.toJson(new Message("Invalid inputs!")));
      return;
    }
    String userID = urlParts[1];
    try {
      List<String> matches = swipeDataRdsDao.getMatches(userID);
      if (matches.isEmpty()) {
        res.setStatus(HttpServletResponse.SC_NOT_FOUND);
        res.getWriter().println(gson.toJson(new Message("User not found!")));
        res.getOutputStream().flush();
      } else {
        res.setStatus(HttpServletResponse.SC_OK);
        res.getWriter().println(gson.toJson(new MatchesResponse(matches)));
      }
    } catch (Exception e) {
      res.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      res.getWriter().println(gson.toJson(new Message("Invalid inputs!")));
    }
  }
}