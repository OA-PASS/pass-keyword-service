package org.dataconservancy.pass.keyword.service;

import cc.mallet.util.*;
import cc.mallet.types.*;
import cc.mallet.pipe.*;
import cc.mallet.pipe.iterator.*;
import cc.mallet.topics.*;

import java.util.*;
import java.util.regex.*;
import java.io.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;

import java.io.IOException;
import java.lang.Exception;

/** Represents PASS-Keyword service which uses MALLET's Parallel Topic Model to extract keywords from a given manuscript
 * @author Jihoon Kim
 */
public class PassKeywordMalletService implements PassKeywordService {
    String dirManuscript = "\\\\wsl.localhost\\Ubuntu-18.04\\home\\jkim25\\PASS\\pass-keyword-service\\src\\main\\java\\org\\dataconservancy\\pass\\keyword\\service\\data\\manuscript.txt";

    /** Builds pipe that manipulates manuscript for extraction
     *
     * @return A Pipe object that converts a manuscript to UTF-8 encoding and transforms manuscript to lowercase, tokens, and without stopwords
     */
    private Pipe buildPipe() {
        String stopList = "\\\\wsl.localhost\\Ubuntu-18.04\\home\\jkim25\\PASS\\pass-keyword-service\\src\\main\\resources\\en.txt";
        ArrayList pipeList = new ArrayList();

        pipeList.add(new Input2CharSequence("UTF-8"));
        pipeList.add( new CharSequenceLowercase() );
        pipeList.add( new CharSequence2TokenSequence(Pattern.compile("\\p{L}[\\p{L}\\p{P}]+\\p{L}")) );
        pipeList.add( new TokenSequenceRemoveStopwords(new File(stopList), "UTF-8", false, false, false) ); // use inputstream apachecommonutils
        pipeList.add( new TokenSequence2FeatureSequence() );

        return new SerialPipes(pipeList);
    }

    /** Loads a saved ParallelTopicModel to evaulate the keywords of given manuscript
     *
     * @param modelName Path of model to load
     * @return An ArrayList of the topTopics (top 5 words in each topic)
     * @throws IOException
     * @throws Exception
     */
    public ArrayList<String> evaluateKeywords() throws IOException, Exception {
        // Create object used to pipe through and clean manuscript (UTF-8 encoding, Lowercase, Tokenize, Remove Stopwords)
        Pipe pipe = buildPipe();

        // Create InstanceList which is used to store manuscript and go through pipes for keyword extraction
        InstanceList instances = new InstanceList(pipe);
        Reader fileReader = new InputStreamReader(new FileInputStream(new File(dirManuscript)), "UTF-8");
        instances.addThruPipe(new CsvIterator (fileReader, Pattern.compile("^(\\S*)[\\s,]*(\\S*)[\\s,]*(.*)$"),
            3, 2, 1)); // data, label, name fields

        // Create a model with 100 topics, alpha_t = 0.01, beta_w = 0.01
        //  Note that the first parameter is passed as the sum over topics, while
        //  the second is the parameter for a single dimension of the Dirichlet prior.
        int numTopics = 5;
        ParallelTopicModel model = new ParallelTopicModel(numTopics, 1.0, 0.01);
        model.addInstances(instances);

        // Use two parallel samplers, which each look at one half the corpus and combine
        //  statistics after every iteration.
        model.setNumThreads(2);

        // Run the model for iterations and stop (50 is for testing only,
        //  for real applications, use 1000 to 2000 iterations)
        int iterations = 100;
        model.setNumIterations(iterations);
        model.estimate();

        // The data alphabet maps word IDs to strings
        Alphabet dataAlphabet = instances.getDataAlphabet();

        // Estimate the topic distribution of the first instance,
        //  given the current Gibbs state.
        double[] topicDistribution = model.getTopicProbabilities(0);

        // Get an array of sorted sets of word ID/count pairs
        ArrayList<TreeSet<IDSorter>> topicSortedWords = model.getSortedWords();

        // Show top 5 words in topics with proportions for the first document
        ArrayList<String> topTopics = new ArrayList<String>();
        for (int topic = 0; topic < numTopics; topic++) {
            Iterator<IDSorter> iterator = topicSortedWords.get(topic).iterator();
            int rank = 0;
            while (iterator.hasNext() && rank < 5) {
                IDSorter idCountPair = iterator.next();
                topTopics.add(dataAlphabet.lookupObject(idCountPair.getID()).toString());
                rank++;
            }
        }
        return topTopics;
    }
}