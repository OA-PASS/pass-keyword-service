package org.dataconservancy.pass.keyword.service;

import cc.mallet.util.*;
import cc.mallet.types.*;
import cc.mallet.pipe.*;
import cc.mallet.pipe.iterator.*;
import cc.mallet.topics.*;

import java.util.*;
import java.util.regex.*;
import java.io.*;

import java.io.IOException;
import java.lang.Exception;

public class PassKeywordService {

    public String trainParallelTopicModel(int numTopics, InstanceList instances) throws IOException {
        // Create a model with 100 topics, alpha_t = 0.01, beta_w = 0.01
        //  Note that the first parameter is passed as the sum over topics, while
        //  the second is the parameter for a single dimension of the Dirichlet prior.
        ParallelTopicModel model = new ParallelTopicModel(numTopics, 1.0, 0.01);

        model.addInstances(instances);

        // Use two parallel samplers, which each look at one half the corpus and combine
        //  statistics after every iteration.
        model.setNumThreads(2);

        int iterations = 500;
        // Run the model for iterations and stop (50 is for testing only,
        //  for real applications, use 1000 to 2000 iterations)
        model.setNumIterations(iterations);
        model.estimate();

        // Save model after iterations
        String modelPath = "\\\\wsl.localhost\\Ubuntu-18.04\\home\\jkim25\\PASS\\pass-keyword-service\\src\\main\\java\\org\\dataconservancy\\pass\\keyword\\service\\model.dat";
        model.write(new File(modelPath));
        return modelPath;
        //instances.save(new File("\\\\wsl.localhost\\Ubuntu-18.04\\home\\jkim25\\PASS\\pass-keyword-service\\src\\main\\java\\org\\dataconservancy\\pass\\keyword\\service\\pipeline.dat"));
    }

    public ArrayList<String> evaluateKeywords(int numTopics, String modelName, InstanceList instances) throws IOException, Exception {
        Alphabet dataAlphabet = instances.getDataAlphabet();
        ParallelTopicModel model = ParallelTopicModel.read(new File(modelName));
        model.addInstances(instances);
        model.estimate();

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