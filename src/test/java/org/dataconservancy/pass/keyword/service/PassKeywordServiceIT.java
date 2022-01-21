package org.dataconservancy.pass.keyword.service;

import okhttp3.Call;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.junit.Test;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;

import java.io.IOException;

import static org.junit.Assert.*;

public class PassKeywordServiceIT {

  /*
  - Test for nonsense url
  - Test for incorrect host/contextpath
  - Test for correct host/contextpath but not pdf
  - Test for valid use case

   */

  private static final String keywordServiceUrl = "http://localhost:8080/pass-keyword-service/keywords";

  OkHttpClient client = new OkHttpClient.Builder()
      .retryOnConnectionFailure(true)
      .build();
  /**
   * Expect 400 error from an invalid URL (Hello World)
   * @throws IOException if input error in request
   */
  @Test
  public void invalidUrlTest() throws IOException {
    HttpUrl.Builder urlBuilder = HttpUrl.parse(keywordServiceUrl).newBuilder().addQueryParameter("file", "HelloWorld");
    String url = urlBuilder.build().toString();

    Request okhttpRequest = new Request.Builder()
        .url(url)
        .addHeader("Connection","close")
        .build();
    Call call = client.newCall(okhttpRequest);

    try (Response okHttpResponse = call.execute()) {
      assertEquals(400, okHttpResponse.code());
    }
    return;
  }

  /**
   * Expect 400 error from incorrect host
   * @throws IOException if input error in request
   */
  @Test
  public void invalidHostUrlTest() throws IOException {
    HttpUrl.Builder urlBuilder = HttpUrl.parse(keywordServiceUrl).newBuilder().addQueryParameter("file", "https://testing.com/this/is/a/test/pdf");
    String url = urlBuilder.build().toString();

    Request okhttpRequest = new Request.Builder()
        .url(url)
        .addHeader("Connection","close")
        .build();
    Call call = client.newCall(okhttpRequest);

    try (Response okHttpResponse = call.execute()) {
      assertEquals(400, okHttpResponse.code());
    }
    return;
  }

  /**
   * Expect 400 error from incorrect context path
   * @throws IOException if input error in request
   */
  @Test
  public void invalidContextPathUrlTest() throws IOException {
  HttpUrl.Builder urlBuilder = HttpUrl.parse(keywordServiceUrl).newBuilder().addQueryParameter("file", "https://pass.local/this/is/incorrect");
    String url = urlBuilder.build().toString();

    Request okhttpRequest = new Request.Builder()
        .url(url)
        .addHeader("Connection","close")
        .build();
    Call call = client.newCall(okhttpRequest);

    try (Response okHttpResponse = call.execute()) {
      assertEquals(400, okHttpResponse.code());
    }
    return;
  }

  /**
   * Expect 415 error from valid url but not valid PDF format
   * @throws IOException if input error in request
   */
  @Test
  public void invalidPdfUrlTest() throws IOException {
    HttpUrl.Builder urlBuilder = HttpUrl.parse(keywordServiceUrl).newBuilder().addQueryParameter("file", "https://www.clickdimensions.com/links/TestPDFfile");
    String url = urlBuilder.build().toString();

    Request okhttpRequest = new Request.Builder()
        .url(url)
        .addHeader("Connection","close")
        .build();
    Call call = client.newCall(okhttpRequest);

    try (Response okHttpResponse = call.execute()) {
      assertEquals(415, okHttpResponse.code());
    }
    return;
  }

  /**
   * Expect 200 success for valid url pdf submission
   * @throws IOException if input error in request
   */
  @Test
  public void validPdfUrlTest() throws IOException {
    HttpUrl.Builder urlBuilder = HttpUrl.parse(keywordServiceUrl).newBuilder().addQueryParameter("file", "https://www.clickdimensions.com/links/TestPDFfile.pdf");
    String url = urlBuilder.build().toString();

    Request okhttpRequest = new Request.Builder()
        .url(url)
        .addHeader("Connection","close")
        .build();
    Call call = client.newCall(okhttpRequest);

    try (Response okHttpResponse = call.execute()) {
      assertEquals(200, okHttpResponse.code());
    }
    return;
  }
}