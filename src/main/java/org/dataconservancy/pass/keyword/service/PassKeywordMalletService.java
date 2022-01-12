package org.dataconservancy.pass.keyword.service;

import java.io.Reader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.io.StringBufferInputStream;
import java.io.IOException;
import java.lang.Exception;
import java.util.ArrayList;
import java.util.TreeSet;
import java.util.Iterator;
import java.util.regex.Pattern;

import cc.mallet.types.InstanceList;
import cc.mallet.types.Alphabet;
import cc.mallet.types.IDSorter;
import cc.mallet.pipe.Pipe;
import cc.mallet.pipe.TokenSequenceRemoveStopwords;
import cc.mallet.pipe.Input2CharSequence;
import cc.mallet.pipe.CharSequenceLowercase;
import cc.mallet.pipe.CharSequence2TokenSequence;
import cc.mallet.pipe.TokenSequence2FeatureSequence;
import cc.mallet.pipe.SerialPipes;
import cc.mallet.pipe.iterator.CsvIterator;
import cc.mallet.topics.ParallelTopicModel;

/** Represents PASS-Keyword service which uses MALLET's Parallel Topic Model to extract keywords from a given manuscript
 * @author Jihoon Kim
 */
public class PassKeywordMalletService implements PassKeywordService {

    /**
     * Creates a ParallelTopicModel from MALLET to evaulate the keywords of given manuscript.
     *
     * Uses a Pipe object to filter and clean manuscript which is stored in an InstanceList, which is then fed
     * to the ParallelTopicModel. The model is configured then ran to retrieve the top keywords of the manuscript for
     * each topic.
     *
     * @param   parsedText   parsed text of manuscript
     * @return               an ArrayList of the topTopics (top 5 words in each topic)
     * @throws  IOException  if input error exists for creating Pipe or InputstreamReader and inability to run or retrieve keywords from model
     */
    public ArrayList<String> evaluateKeywords(String parsedText) throws IOException {
        // Create object used to pipe through and clean manuscript (UTF-8 encoding, Lowercase, Tokenize, Remove Stopwords)
        Pipe pipe;
        try {
            pipe = buildPipe();
        } catch (IOException e) {
            throw new IOException();
        }
        // Create InstanceList which is used to store manuscript and go through pipes for keyword extraction
        InstanceList instances = new InstanceList(pipe);
        Reader fileReader;
        try {
            fileReader = new InputStreamReader(new StringBufferInputStream(parsedText), "UTF-8");
        } catch (IOException e2) {
            throw new IOException();
        }
        instances.addThruPipe(new CsvIterator (fileReader, Pattern.compile("^(\\S*)[\\s,]*(\\S*)[\\s,]*(.*)$"),
            3, 2, 1)); // data, label, name fields

        // Create a model with 1 topic(s), alpha_t = 0.01, beta_w = 0.01
        //  Note that the first parameter is passed as the sum over topics, while
        //  the second is the parameter for a single dimension of the Dirichlet prior.
        int numTopics = 1;
        ParallelTopicModel model = new ParallelTopicModel(numTopics, 1.0, 0.01);
        model.addInstances(instances);

        // Use two parallel samplers, which each look at one half the corpus and combine
        //  statistics after every iteration.
        model.setNumThreads(2);

        // Run the model for iterations and stop (50 is for testing only,
        //  for real applications, use 1000 to 2000 iterations)
        int iterations = 100;
        model.setNumIterations(iterations);
        try {
            model.estimate();
        } catch (Exception e3) {
            throw new IOException(); // return IOException for inability to estimate/run model
        }

        // The data alphabet maps word IDs to strings
        Alphabet dataAlphabet = instances.getDataAlphabet();

        // Estimate the topic distribution of the first instance,
        //  given the current Gibbs state.
        double[] topicDistribution;
        try {
            topicDistribution = model.getTopicProbabilities(0);
        } catch (Exception e4) {
            throw new IOException(); // return IOException if topicDistribution cannot be retrieved (no topics)
        }
        // Get an array of sorted sets of word ID/count pairs
        ArrayList<TreeSet<IDSorter>> topicSortedWords = model.getSortedWords();

        // Show top 10 words in topics with proportions for the first document
        ArrayList<String> topTopics = new ArrayList<String>();
        for (int topic = 0; topic < numTopics; topic++) {
            Iterator<IDSorter> iterator = topicSortedWords.get(topic).iterator();
            int rank = 0;
            while (iterator.hasNext() && rank < 25) {
                IDSorter idCountPair = iterator.next();
                topTopics.add(dataAlphabet.lookupObject(idCountPair.getID()).toString());
                rank++;
            }
        }
        return topTopics;
    }

    /**
     * Builds pipe that manipulates manuscript for extraction.
     * Pipe manipulates the manuscript to the following: UTF-8 encoding, lowercase form, tokenized, without stopwords.
     *
     * @return               Pipe object that converts a manuscript to UTF-8 encoding and transforms manuscript to lowercase, tokens, and without stopwords
     * @throws  IOException  if getStopList throws IOException for input error when retrieving stoplist from en.txt
     */
    private Pipe buildPipe() throws IOException {
        String[] stopList = getStopList();
        TokenSequenceRemoveStopwords stopwordsRemover = new TokenSequenceRemoveStopwords(false, false);
        stopwordsRemover.addStopWords(stopList);

        ArrayList pipeList = new ArrayList();
        pipeList.add(new Input2CharSequence("UTF-8"));
        pipeList.add(new CharSequenceLowercase());
        pipeList.add(new CharSequence2TokenSequence(Pattern.compile("\\p{L}[\\p{L}\\p{P}]+\\p{L}")));
        pipeList.add(stopwordsRemover);
        pipeList.add(new TokenSequence2FeatureSequence());

        return new SerialPipes(pipeList);
    }

    /**
     * Get array of stopwords from src/main/resources/en.txt (text of stopwords in English).
     *
     * @return  stopList     string array of stopwords
     * @throws  IOException  if en.txt cannot be read or converted as an array of Strings
     */
    private String[] getStopList() throws IOException {
        InputStream inputStream = this.getClass().getResourceAsStream("/en.txt");
        InputStreamReader isReader = new InputStreamReader(inputStream);
        BufferedReader reader = new BufferedReader(isReader);
        StringBuffer stopWords = new StringBuffer();
        String str;
        while((str = reader.readLine())!= null){
            stopWords.append(str);
        }
        String stopList[] = stopWords.toString().split("\n");
        return stopList;
    }
}