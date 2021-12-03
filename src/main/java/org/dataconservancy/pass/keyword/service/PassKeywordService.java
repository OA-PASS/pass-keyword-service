package org.dataconservancy.pass.keyword.service;

import java.util.ArrayList;
import java.lang.Exception;

public interface PassKeywordService {
  ArrayList<String> evaluateKeywords(String parsedText) throws Exception;
}