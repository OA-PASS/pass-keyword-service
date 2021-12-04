package org.dataconservancy.pass.keyword.service;

import java.util.ArrayList;
import java.io.IOException;

public interface PassKeywordService {
  ArrayList<String> evaluateKeywords(String parsedText) throws IOException;
}