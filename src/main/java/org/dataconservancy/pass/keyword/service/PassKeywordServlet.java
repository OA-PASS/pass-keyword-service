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

import org.json.simple.JSONArray;
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
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.File;
import java.lang.Exception;
import java.net.URI;
import java.net.URL;
import java.net.MalformedURLException;
import java.util.ArrayList;

import static java.lang.Thread.sleep;
import static java.util.concurrent.TimeUnit.SECONDS;

import org.apache.pdfbox.cos.COSDocument;
import org.apache.pdfbox.io.RandomAccessFile;
import org.apache.pdfbox.pdfparser.PDFParser;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

import cc.mallet.util.*;
import cc.mallet.types.*;
import cc.mallet.pipe.*;
import cc.mallet.pipe.iterator.*;
import cc.mallet.topics.*;


//import package.PassKeywordService;
//import package.PassKeywordImport;

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

    /* Step 2: Convert manuscript to .txt */
    // TODO: manuscript file
    String manuscript = "\\\\wsl.localhost\\Ubuntu-18.04\\home\\jkim25\\PASS\\pass-keyword-service\\src\\main\\java\\org\\dataconservancy\\pass\\keyword\\service\\nature.pdf";
    boolean textGenerated = generateTextFromPDF(manuscript);
    if (!textGenerated) {
      JsonObject jsonObject = Json.createObjectBuilder()
          .add("error", "Supplied manuscript file cannot be converted to .txt file.")
          .build();
      out.write(jsonObject.toString().getBytes("UTF-8"));
      response.setStatus(400);
      return;
    }

    /* Step 3: Try to get keywords */
    String dir = "\\\\wsl.localhost\\Ubuntu-18.04\\home\\jkim25\\PASS\\pass-keyword-service\\src\\main\\java\\org\\dataconservancy\\pass\\keyword\\service\\data";
    PassKeywordImport importer = new PassKeywordImport();
    InstanceList instances = importer.readDirectory(new File(dir));

    PassKeywordService keywordService = new PassKeywordService();
    int numTopics = 10;
    // Train model and save path name to modelPath || TODO: set modelPath if model exists
    //String modelPath = "\\\\wsl.localhost\\Ubuntu-18.04\\home\\jkim25\\PASS\\pass-keyword-service\\src\\main\\java\\org\\dataconservancy\\pass\\keyword\\service\\model.dat";
    String modelPath = keywordService.trainParallelTopicModel(numTopics, instances);
    ArrayList<String> keywords;
    try {
      keywords = keywordService.evaluateKeywords(numTopics, modelPath, instances);
    } catch (Exception e) {
      JsonObject jsonObject = Json.createObjectBuilder()
          .add("error", "Cannot find keywords")
          .build();
      out.write(jsonObject.toString().getBytes("UTF-8"));
      response.setStatus(400);
      return;
    }


    /* Step 4: Output keywords to JSON object */
    String keys = String.join(", ", keywords);
    JsonObject jsonObject = Json.createObjectBuilder()
        .add("keywords", keys)
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

  /** Private function that converts PDF manuscript to .txt */
  private static boolean generateTextFromPDF(String filename) throws FileNotFoundException, IOException {
    if (!filename.toString().endsWith(".pdf")) {
      return false;
    }
    // Load PDF File
    File f = new File(filename);
    String parsedText;
    PDFParser parser = new PDFParser(new RandomAccessFile(f, "r"));
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

    // Save Text File as manuscript.txt
    File outputFile = new File("\\\\wsl.localhost\\Ubuntu-18.04\\home\\jkim25\\PASS\\pass-keyword-service\\src\\main\\java\\org\\dataconservancy\\pass\\keyword\\service\\sample-data\\00manuscript.txt");
    PrintWriter pw = new PrintWriter(outputFile);
    pw.print(parsedText);
    pw.close();
    return true;
  }
}