package org.dataconservancy.pass.keyword.service;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonArrayBuilder;
import javax.servlet.ServletOutputStream;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.InputStream;

import java.net.URL;
import java.util.ArrayList;

import java.io.IOException;
import java.lang.Exception;
import java.net.MalformedURLException;

import org.apache.pdfbox.cos.COSDocument;
import org.apache.pdfbox.io.RandomAccessBuffer;
import org.apache.pdfbox.pdfparser.PDFParser;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

public class PassKeywordServlet extends HttpServlet {
  String hostUrl;
  String contextPath;
  PassKeywordService passKeywordService;

  @Override
  public void init(ServletConfig config) throws ServletException {
    super.init(config);
    if ((hostUrl = getInitParameter("hostUrl")) == null) {
      hostUrl = "pass.local";
    }
    if ((contextPath = getInitParameter("contextPath")) == null) {
      contextPath = "/fcrepo/rest/submissions";
    }
    passKeywordService = new PassKeywordMalletService();
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
          .add("error", "Supplied URL is not in valid format.")
          .build();
      out.write(jsonObject.toString().getBytes("UTF-8"));
      response.setStatus(400);
      return;
    }

    /* Step 2: Convert manuscript from url to InputStream to String of parsed text for model input */
    URL manuscript = new URL(url);
    // get InputStream of url
    InputStream manuscriptInput = manuscript.openStream();
    // Parse text from InputStream
    String parsedText = generateTextFromPDF(manuscriptInput);

    if (parsedText == null) {
      JsonObject jsonObject = Json.createObjectBuilder()
          .add("error", "Supplied manuscript file cannot be parsed.")
          .build();
      out.write(jsonObject.toString().getBytes("UTF-8"));
      response.setStatus(400);
      return;
    }

    /* Step 3: Evaluate keywords */
    ArrayList<String> keywords;
    try {
      keywords = passKeywordService.evaluateKeywords(parsedText);
    } catch (Exception e) {
      JsonObject jsonObject = Json.createObjectBuilder()
          .add("error", "IOException thrown: Cannot evaluate keywords")
          .build();
      out.write(jsonObject.toString().getBytes("UTF-8"));
      response.setStatus(400);
      return;
    }

    if (keywords == null) {
      JsonObject jsonObject = Json.createObjectBuilder()
          .add("error", "No keywords found.")
          .build();
      out.write(jsonObject.toString().getBytes("UTF-8"));
      response.setStatus(400);
      return;
    }

    /* Step 4: Output keywords to JSON object */
    JsonArrayBuilder jsonKeywordArrayBuilder = Json.createArrayBuilder();
    for(int i = 0; i < keywords.size(); i++) {
      jsonKeywordArrayBuilder.add(i, keywords.get(i));
    }
    JsonArray jsonKeywordArray = jsonKeywordArrayBuilder.build();

    JsonObject jsonObject = Json.createObjectBuilder()
        .add("keywords", jsonKeywordArray)
        .build();
    out.write(jsonObject.toString().getBytes("UTF-8"));
    response.setStatus(200);
    return;
  }

  /** Verifies URL with configuration
   *
   * @param manuscript  manuscript of GET request
   * @return            true = valid manuscript, false = invalid manuscript (empty, unsupported file)
   */
  public boolean verify(String urlString) {
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

  /** Private function that converts PDF manuscript to String of parsed manuscript
   *
   * @param input InputStream to take PDF input from
   * @return parsedText String of text parsed from input
   * @throws IOException
   */
  public static String generateTextFromPDF(InputStream input) throws IOException {
    String parsedText;
    try {
      PDFParser parser = new PDFParser(new RandomAccessBuffer(input));
      parser.parse();

      // Extract Text from PDF File
      COSDocument cosDoc = parser.getDocument();
      PDFTextStripper pdfStripper = new PDFTextStripper();
      PDDocument pdDoc = new PDDocument(cosDoc);
      parsedText = pdfStripper.getText(pdDoc);

      if (cosDoc != null)
        cosDoc.close();
      if (pdDoc != null)
        pdDoc.close();
    } catch (IOException e) {
      return null;
    }

    return parsedText;
  }
}