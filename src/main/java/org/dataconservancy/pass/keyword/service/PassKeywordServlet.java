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
import java.lang.System;
import java.net.MalformedURLException;

import org.apache.pdfbox.cos.COSDocument;
import org.apache.pdfbox.io.RandomAccessBuffer;
import org.apache.pdfbox.pdfparser.PDFParser;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PassKeywordServlet extends HttpServlet {
  private static final Logger LOG = LoggerFactory.getLogger(PassKeywordServlet.class);

  String hostUrl;
  String contextPath;
  String maxKeywords;
  PassKeywordService passKeywordService;

  /**
   * Initializes PassKeywordServlet by loading in configurations and initializing the PassKeywordService.
   *
   * @param  config            a Servlet Config object containing the servlet's configuration and initialization parameters
   * @throws ServletException  if an exception has occurred that interferes with the servlet's normal operation
   */
  @Override
  public void init(ServletConfig config) throws ServletException {
    super.init(config);

    if ((hostUrl = System.getenv("HOSTURL")) == null) {
      LOG.info("Environment variable: HOSTURL not found");
      hostUrl = "pass.local";
    }
    if ((contextPath = System.getenv("CONTEXTPATH")) == null) {
      LOG.info("Environment variable: CONTEXTPATH not found");
      contextPath = "/fcrepo/rest/submissions";
    }
    if ((maxKeywords = System.getenv("MAXKEYWORDS")) == null) {
      LOG.info("Environment variable: MAXKEYWORDS not found");
      maxKeywords = "25";
    }

    passKeywordService = new PassKeywordMalletService();
  }

  /**
   * Called by serviced to allow PassKeywordService to handle a GET request.
   * Upon receiving the GET request, the GET request is verified to be a valid URL according to the servlet configuration,
   * then converted from a URL to InputStream to a String of parsed text for keyword evaluation with the PassKeywordService.
   * The outputted keywords are then stored as a JSON array to the response output.
   *
   * @param request       an HTTPServletRequest object that contains the URL of the manuscript sent from the client
   * @param response      an HTTPServletResponse object that contains the JSONObject of keywords extracted from manuscript
   * @throws IOException  if an input or output error is detected when the servlet handles the GET request
   */
  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    response.setContentType("application/json");
    response.setCharacterEncoding("utf-8");
    ServletOutputStream out = response.getOutputStream();
    LOG.info("Servicing new request ... ");

    String url = request.getParameter("file");

    /* Step 1: Check if manuscript is a valid file (i.e. not blank, valid format) */
    if (!verify(url)) {
      String message = "Supplied URL is not in valid format.";
      JsonObject jsonObject = Json.createObjectBuilder()
          .add("error", message)
          .build();
      out.write(jsonObject.toString().getBytes("UTF-8"));
      response.setStatus(400);
      LOG.info(message);
      return;
    }

    /* Step 2: Convert manuscript from url to InputStream to String of parsed text for model input */
    URL manuscript = new URL(url);
    // get InputStream of url
    InputStream manuscriptInput = manuscript.openStream();
    // Parse text from InputStream
    LOG.info("Parsing text from requested URL");
    String parsedText;
    try {
      parsedText = generateTextFromPDF(manuscriptInput);
    } catch (IOException e) {
      String message = "Supplied manuscript file cannot be parsed. Must be PDF Format.";
      JsonObject jsonObject = Json.createObjectBuilder()
          .add("error", message)
          .build();
      out.write(jsonObject.toString().getBytes("UTF-8"));
      response.setStatus(415);
      LOG.info(message);
      LOG.debug("IOException thrown. URL file must be valid PDF format.");
      return;
    }
    if (parsedText == null) {
      String message = "Supplied manuscript file cannot be parsed. No Text Parsed.";
      JsonObject jsonObject = Json.createObjectBuilder()
          .add("error", message)
          .build();
      out.write(jsonObject.toString().getBytes("UTF-8"));
      response.setStatus(415);
      LOG.info(message);
      return;
    }

    /* Step 3: Evaluate keywords */
    ArrayList<String> keywords;
    try {
      keywords = passKeywordService.evaluateKeywords(parsedText);
    } catch (IOException e) {
      String message = "IOException thrown: cannot evaluate keywords.";
      JsonObject jsonObject = Json.createObjectBuilder()
          .add("error", message)
          .build();
      out.write(jsonObject.toString().getBytes("UTF-8"));
      response.setStatus(422);
      LOG.info(message);
      LOG.debug("Note: MALLET based function exceptions (Java.lang.Exception) are masked as IOException");
      return;
    }
    if (keywords == null) { // This should almost never be the case since IOException is thrown for empty manuscript
      String message = "No keywords found.";
      JsonObject jsonObject = Json.createObjectBuilder()
          .add("error", message)
          .build();
      out.write(jsonObject.toString().getBytes("UTF-8"));
      response.setStatus(500);
      LOG.info(message);
      LOG.debug("This should almost never be the case.");
      return;
    }

    /* Step 4: Output keywords to JSON object */
    JsonArrayBuilder jsonKeywordArrayBuilder = Json.createArrayBuilder();
    for(int i = 0; i < keywords.size() && i < Integer.parseInt(maxKeywords); i++) {
      jsonKeywordArrayBuilder.add(i, keywords.get(i));
    }
    JsonArray jsonKeywordArray = jsonKeywordArrayBuilder.build();

    JsonObject jsonObject = Json.createObjectBuilder()
        .add("keywords", jsonKeywordArray)
        .build();
    out.write(jsonObject.toString().getBytes("UTF-8"));
    response.setStatus(200);
    LOG.info("Successfully extracted keywords");
    return;
  }

  /**
   * Verifies URL with web.xml configuration
   *
   * @param  manuscript  manuscript from GET request
   * @return             true = valid manuscript, false = invalid manuscript (empty, unsupported file)
   */
  protected boolean verify(String urlString) {
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
        LOG.info("Invalid Protocol");
        LOG.debug("Protocol must be http or https | Request protocol: " + protocol);
        return false;
      }
      else if (!(hostUrl.equals(authority))) { // Check authority = "pass.local"
        LOG.info("Invalid Authority");
        LOG.debug("web.xml configuration: " + hostUrl + " | Request Authority: " + authority);
        return false;
      } else if (!(contextPath.equals(fileContextPath))) { // check context path = "/fcrepo/rest/submisions"
        LOG.info("Invalid Context Path");
        LOG.debug("web.xml context path: " + contextPath + " | Request context path: " + fileContextPath);
        return false;
      }
    } catch (MalformedURLException e) { // catch if URL cannot be made
      LOG.info("Malformed URL");
      LOG.debug("MalformedURLException caught");
      return false;
    } catch (Exception e) {
      return false; // refers to line 177, IndexOutOfBounds exception
    }
    return true;
  }

  /**
   * Private function that converts PDF manuscript to String of parsed manuscript
   *
   * @param   input        an InputStream object to take PDF input from
   * @return  parsedText   String of text parsed from input
   * @throws  IOException  if an input error is detected while generating text from PDF
   */
  protected static String generateTextFromPDF(InputStream input) throws IOException {
    try {
      String parsedText;
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

      return parsedText;
    } catch (Exception e) {
      throw new IOException(); // IOException thrown if any exception thrown in this method
    }
  }
}