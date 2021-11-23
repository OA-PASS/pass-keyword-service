package org.dataconservancy.pass.keyword.service;

import okhttp3.Call;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.dataconservancy.pass.client.PassClient;
import org.dataconservancy.pass.client.PassClientFactory;
import org.dataconservancy.pass.client.PassJsonAdapter;
import org.dataconservancy.pass.client.adapter.PassJsonAdapterBasic;
import org.dataconservancy.pass.model.Journal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.json.stream.JsonParsingException;
import javax.servlet.ServletOutputStream;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringReader;
import java.net.URI;
import java.net.URL;
import java.net.MalformedURLException;

import static java.lang.Thread.sleep;
import static java.util.concurrent.TimeUnit.SECONDS;

public class PassKeywordServlet extends HttpServlet {
  String hostUrl;
  String contextPath;

  @Override
  public void init(ServletConfig config) throws ServletException {
    super.init(config);
    if ((hostUrl = getInitParameter("hostUrl")) == null) {
      hostUrl = "pass.local";
    }
    if ((contextPath = getInitParameter("contextPath")) == null) {
      contextPath = "/fcrepo/rest/submissions";
    }

  }

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    response.setContentType("application/json");
    response.setCharacterEncoding("utf-8");
    ServletOutputStream out = response.getOutputStream();

    String url = request.getParameter("file");


    /* Step 1: Check if manuscript is a valid file (i.e. not blank, valid format) */
    if (!verify(url)) {
      JsonObject jsonObject = Json.createObjectBuilder()
          .add("error", "Supplied manuscript file is not in valid format.")
          .build();
      out.write(jsonObject.toString().getBytes("UTF-8"));
      response.setStatus(400);
      return;
    }

    /* Step 2: Try to get keywords */
    String keywords = url; // TODO: Change to empty string ""
    JsonObject jsonObject = Json.createObjectBuilder()
        .add("keywords", keywords)
        .build();
    out.write(jsonObject.toString().getBytes("UTF-8"));
    response.setStatus(200);
    return;
  }

  /**
   *
   * @param manuscript  manuscript of GET request
   * @return            true = valid manuscript, false = invalid manuscript (empty, unsupported file)
   */
  private boolean verify(String urlString) throws MalformedURLException {
    if (urlString == null) {
      return false;
    }
    try {
      URL url = new URL(urlString); // validates initial scheme of URL
      String protocol = url.getProtocol();
      String authority = url.getAuthority();
      String file = url.getFile();
      String fileContextPath = file.substring(0, contextPath.length());

      if (!(("http".equals(protocol)) || "https".equals(protocol))) { // Check protocol
        return false;
      }
      else if (!(hostUrl.equals(authority))) { // Check authority = "pass.local"
        return false;
      } else if (!(contextPath.equals(fileContextPath))) { // check context path = "/fcrepo/rest/submisions"
        System.out.println(fileContextPath);
        return false;
      }
    } catch (MalformedURLException e) { // catch if URL cannot be made
      return false;
    }
    return true;
  }
}