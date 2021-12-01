package org.dataconservancy.pass.keyword.service;

import java.io.*;
import java.util.*;
import java.util.regex.*;

import cc.mallet.pipe.*;
import cc.mallet.pipe.iterator.*;
import cc.mallet.types.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;



public class PassKeywordImport {

  Pipe pipe;

  public PassKeywordImport() {
    pipe = buildPipe();
  }

  public Pipe buildPipe() {
    String stopList = "\\\\wsl.localhost\\Ubuntu-18.04\\home\\jkim25\\PASS\\pass-keyword-service\\src\\main\\java\\org\\dataconservancy\\pass\\keyword\\service\\en.txt";
    ArrayList pipeList = new ArrayList();

    // Read data from File objects
    pipeList.add(new Input2CharSequence("UTF-8"));
    pipeList.add( new CharSequenceLowercase() );
    pipeList.add( new CharSequence2TokenSequence(Pattern.compile("\\p{L}[\\p{L}\\p{P}]+\\p{L}")) );
    pipeList.add( new TokenSequenceRemoveStopwords(new File(stopList), "UTF-8", false, false, false) );
    pipeList.add( new TokenSequence2FeatureSequence() );

    return new SerialPipes(pipeList);
  }

  public InstanceList readDirectory(File directory) {
    return readDirectories(new File[] {directory});
  }

  public InstanceList readDirectories(File[] directories) {

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

//  public static void main (String[] args) throws IOException {
//
//    PassKeywordImport importer = new PassKeywordImport();
//    InstanceList instances = importer.readDirectory(new File(args[0]));
//    instances.save(new File(args[1]));
//
//  }

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

}