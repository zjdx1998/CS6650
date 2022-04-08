package servlets;

import com.google.gson.Gson;
import constants.Constant;
import io.swagger.client.model.APIStats;
import io.swagger.client.model.APIStatsEndpointStats;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
/**
 * @author jeromy zhang
 * @date Feb-17-22
 */
@WebServlet(name = "StatisticsServlet")
public class StatisticsServlet extends HttpServlet {
    private final Gson gson  = new Gson();

    protected void doGet(HttpServletRequest req,
                         HttpServletResponse res)
            throws IOException {
        res.setContentType("application/json");
        res.setCharacterEncoding("UTF-8");
        String urlPath = req.getPathInfo();

        if (urlPath == null || urlPath.isEmpty() || urlPath.equals(Constant.SPLIT)) {
            res.setStatus(HttpServletResponse.SC_OK);
            res.getWriter().write(gson.toJson(Constant.STATISTICS));
            return;
        }
        res.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        res.getWriter().write(gson.toJson(
                new APIStats().addEndpointStatsItem(
                        new APIStatsEndpointStats()
                                .max(Constant.DAY_COUNTS)
                                .mean(Constant.ID)
                                .URL(Constant.SPLIT)
                                .operation(Constant.SEASONS))));
    }
}