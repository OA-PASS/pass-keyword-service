package org.dataconservancy.pass.keyword.service;

import java.util.ArrayList;
import java.io.IOException;

/**
 * Service that evaluates the top keywords from a manuscript.
 *
 */
public interface PassKeywordService {
  /**
   * Service interface contains method evaluateKeywords which should be overridden to implement keyword extraction from
   * an inputted parsed manuscript, which then outputs an ArrayList of String objects containing the top keywords from said
   * parsed manuscript.
   * @param  parsedText   string of parsed text extracted from manuscript
   * @return              an ArrayList of the top keywords extracted from the inputted manuscript
   * @throws IOException  if input or output during the method results in an error
   */
  ArrayList<String> evaluateKeywords(String parsedText) throws IOException;
}