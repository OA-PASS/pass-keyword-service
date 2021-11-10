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

import static java.lang.Thread.sleep;
import static java.util.concurrent.TimeUnit.SECONDS;

public class PassKeywordServlet extends HttpServlet {

  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    PrintWriter out = response.getWriter();
    response.setContentType("application/json");

    String file = request.getParameter("file");

    /* Step 1: Check if manuscript is a valid file (i.e. not blank, valid format) */
    if (!verify(file)) {
      JsonObject jsonObject = Json.createObjectBuilder()
          .add("error", "Supplied manuscript file is not in valid format.")
          .build();
      out.write(jsonObject.toString());
      response.setStatus(400);
      return;
    }

    /* Step 2: Try to get keywords */
    String keywords = file; // TODO: Change to empty string ""
    JsonObject jsonObject = Json.createObjectBuilder()
        .add("keywords", keywords)
        .build();

    out.write(jsonObject.toString());
    response.setStatus(200);

    out.close();
  }

  /**
   *
   * @param manuscript  manuscript of GET request
   * @return            true - valid manuscript, false - invalid manuscript (empty, unsupported file)
   */
  private boolean verify(String manuscript) {
    if (manuscript == null) {
      return false;
    }
    // TODO: Add more verification
    return true;
  }
}