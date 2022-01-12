package org.dataconservancy.pass.keyword.service;

import java.util.ArrayList;
import java.io.IOException;

/**
 * Service that evaluates the top keywords from a manuscript.
 *
 * Service interface contains method evaluateKeywords which should be overridden to implement keyword extraction from
 * an inputted parsed manuscript, which then outputs an ArrayList of String objects containing the top keywords from said
 * parsed manuscript.
 */
public interface PassKeywordService {
  ArrayList<String> evaluateKeywords(String parsedText) throws IOException;
}