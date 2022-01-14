package org.dataconservancy.pass.keyword.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.InputStream;
import java.io.IOException;
import java.net.URL;
import java.net.MalformedURLException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class PassKeywordServletTest {

  protected PassKeywordServlet passKeywordServlet;

  @Mock
  ServletConfig config;

  @Mock
  HttpServletRequest request;

  @Mock
  HttpServletResponse response;

  @BeforeEach
  public void setupPassKeywordServlet() throws ServletException {
    MockitoAnnotations.initMocks(this);
    config = Mockito.mock(ServletConfig.class);
    passKeywordServlet = new PassKeywordServlet();
    passKeywordServlet.init(config);
  }

  @Test
  @DisplayName("verify() method handles URL verification correctly")
  public void testVerify() {
    String url;

    // null string
    url = "";
    assertFalse(passKeywordServlet.verify(url));

    // invalid URL
    url = "Hello world";
    assertFalse(passKeywordServlet.verify(url));

    // valid URL, invalid authority
    url = "https://testing.com/this/is/a/test/pdf";
    assertFalse(passKeywordServlet.verify(url));

    // valid URL, invalid fileContetPath
    url = "https://pass.local/this/is/incorrect";
    assertFalse(passKeywordServlet.verify(url));

    // valid URL, valid configuration
    url = "https://pass.local/fcrepo/rest/submissions/aa/11/22/helloworld";
    assertTrue(passKeywordServlet.verify(url));
  }

  @Test
  @DisplayName("generateTextToPDF() comprehensive test")
  public void testTextFromPDF() {
    // general case: PDF URL input
    try {
      String url = "https://www.clickdimensions.com/links/TestPDFfile.pdf";
      URL manuscript = new URL(url);
      InputStream manuscriptInput = manuscript.openStream();
      String parsedText = passKeywordServlet.generateTextFromPDF(manuscriptInput);
      assertEquals("This is a test PDF file \n", parsedText);
    } catch(Exception e){
      fail("Should not throw IOException");
    }

    // null input
    try {
      String parsedText = passKeywordServlet.generateTextFromPDF(null);
      fail("IOException not caught for empty InputStream");
    } catch(IOException e) {
    }

    // invalid supported URL
    try {
      String url = "http://www.example.com/index.html";
      URL manuscript = new URL(url);
      InputStream manuscriptInput = manuscript.openStream();
      String parsedText = passKeywordServlet.generateTextFromPDF(manuscriptInput);
      fail("parsedText with HTML input");
    } catch(IOException e){
    }
    return;
  }

  @Test
  @DisplayName("doGet() has correct response given invalid URL request (invalid config)")
  public void testDoGetResponse() {
    try{
      ServletOutputStream servletOut = Mockito.mock(ServletOutputStream.class);
      when(request.getParameter("file")).thenReturn("https://www.clickdimensions.com/links/TestPDFfile.pdf");
      when(response.getOutputStream()).thenReturn(servletOut);
      passKeywordServlet.doGet(request, response);

      Mockito.verify(request).getParameter("file");

      Mockito.verify(response).setContentType("application/json");
      Mockito.verify(response).setCharacterEncoding("utf-8");
      Mockito.verify(response).setStatus(400);
      return;
    } catch(IOException e) {
      fail("IOException should not be caught");
    }
  }
}