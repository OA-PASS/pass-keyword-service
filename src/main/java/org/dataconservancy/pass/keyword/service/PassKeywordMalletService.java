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
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;

import java.io.IOException;
import java.lang.Exception;

/** Represents PASS-Keyword service which uses MALLET's Parallel Topic Model to extract keywords from a given manuscript
 * @author Jihoon Kim
 */
public class PassKeywordMalletService {
    /** Represents number of topics to extract from manuscript */
    int numTopics = 5;
    String dirManuscript = "\\\\wsl.localhost\\Ubuntu-18.04\\home\\jkim25\\PASS\\pass-keyword-service\\src\\main\\java\\org\\dataconservancy\\pass\\keyword\\service\\data";
    /** Object used to store manuscript text file */
    InstanceList instances;
    /** Object used to pipe through and clean manuscript for keyword extraction */
    Pipe pipe;

    /** Creates PassKeywrodMalletService by building the pipe and instances */
    public PassKeywordMalletService() {
        pipe = buildPipe();
        instances = readDirectory(new File(dirManuscript));
    }

    /** Builds pipe that manipulates manuscript for extraction
     *
     * @return A Pipe object that converts a manuscript to UTF-8 encoding and transforms manuscript to lowercase, tokens, and without stopwords
     */
    public Pipe buildPipe() {
        String stopList = "\\\\wsl.localhost\\Ubuntu-18.04\\home\\jkim25\\PASS\\pass-keyword-service\\src\\main\\resources\\en.txt";
        ArrayList pipeList = new ArrayList();

        pipeList.add(new Input2CharSequence("UTF-8"));
        pipeList.add( new CharSequenceLowercase() );
        pipeList.add( new CharSequence2TokenSequence(Pattern.compile("\\p{L}[\\p{L}\\p{P}]+\\p{L}")) );
        pipeList.add( new TokenSequenceRemoveStopwords(new File(stopList), "UTF-8", false, false, false) );
        pipeList.add( new TokenSequence2FeatureSequence() );

        return new SerialPipes(pipeList);
    }

    /** Reads the directory in which the manuscript is contained and runs through pipes to clean and sort manuscript
     * @param directory The directory containing file
     * @return InstanceList of manuscript
     */
    public InstanceList readDirectory(File directory) {
        return readDirectories(new File[] {directory});
    }

    /** Helper function to read directories of manuscript
     * @param directories The directories containing manuscript
     * @return InstanceList of manuscript
     */
    private InstanceList readDirectories(File[] directories) {

        // Construct a file iterator, starting with the
        //  specified directories, and recursing through subdirectories.
        // The second argument specifies a FileFilter to use to select
        //  files within a directory.
        // The third argument is a Pattern that is applied to the
        //   filename to produce a class label. In this case, I've
        //   asked it to use the last directory name in the path.
        FileIterator iterator =
            new FileIterator(directories,
                new TxtFilter(),
                FileIterator.LAST_DIRECTORY);

        // Construct a new instance list, passing it the pipe
        //  we want to use to process instances.
        InstanceList instances = new InstanceList(pipe);

        // Now process each instance provided by the iterator.
        instances.addThruPipe(iterator);

        return instances;
    }

    /** This class illustrates how to build a simple file filter */
    class TxtFilter implements FileFilter {

        /** Test whether the string representation of the file
         *   ends with the correct extension. Note that {@ref FileIterator}
         *   will only call this filter if the file is not a directory,
         *   so we do not need to test that it is a file.
         */
        public boolean accept(File file) {
            return file.toString().endsWith(".txt");
        }
    }

    /** Trains a ParallelTopicModel and saves model to path
     *
     * @return modelPath Path of model saved
     * @throws IOException
     */
    public String trainParallelTopicModel() throws IOException {
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
        String modelPath = "src/main/resources/model.dat";
        model.write(new File(modelPath));
        return modelPath;
    }

    /** Loads a saved ParallelTopicModel to evaulate the keywords of given manuscript
     *
     * @param modelName Path of model to load
     * @return An ArrayList of the topTopics (top 5 words in each topic)
     * @throws IOException
     * @throws Exception
     */
    public ArrayList<String> evaluateKeywords(String modelName) throws IOException, Exception {
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