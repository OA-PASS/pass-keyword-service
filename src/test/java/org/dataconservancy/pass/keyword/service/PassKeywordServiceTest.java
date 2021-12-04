package org.dataconservancy.pass.keyword.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.io.IOException;
import java.lang.Exception;

import static org.junit.jupiter.api.Assertions.*;

public class PassKeywordServiceTest {

  protected PassKeywordService passKeywordService;

  @BeforeEach
  public void setupPassKeywordService() {
    this.passKeywordService = new PassKeywordMalletService();
  }

  @Test
  @DisplayName("evaluateKeywords() correctly evaluates keywords given simple input")
  public void testEvaluateKeywordsSimpleInput() {
    String parsedText = "the quick brown fox jumped over the lazy dog";
    try {
      ArrayList<String> keywords = passKeywordService.evaluateKeywords(parsedText);
      assertTrue(keywords.size() > 0);
      return;
    } catch (IOException e) {
      fail("Incorrectly caught IOException for valid input!");
    }
  }

  @Test
  @DisplayName("evaluateKeywords() returns null given empty input")
  public void testEvaluateKeywordsEmptyInput() {
    String parsedText = "";
    try {
      ArrayList<String> keywords = passKeywordService.evaluateKeywords(parsedText);
      assertEquals(keywords, null);
      return;
    } catch (IOException e) {
      fail("Incorrectly caught IOException for empty input!");
    }
  }

}