package edu.cmu.ml.rtw.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Aligns tag sequences to text sequence
 * 
 * @author ndapa
 *
 */
public class WordSequence {

  public List<String> words = new ArrayList<String>();

  public List<String> tags = new ArrayList<String>();

  public Map<Integer, Integer> TagStringPosToSequencePos = new HashMap<Integer, Integer>();

  public Map<Integer, Integer> WordStringPosToSequencePos = new HashMap<Integer, Integer>();

  public String tagString = "";

  //map word positions to words

  public void appendWord(String word) {
    words.add(word);
    getWordStringPositions();
  }

  public void appendTag(String word) {
    tags.add(word);
    getTagStringPositions();
  }

  public void setTagString(String tagsOfSentence) {
    tagString = tagsOfSentence;
  }

  public void setTag(String word) {
    tags.add(word);
    getTagStringPositions();
  }

  public String getTagString() {
    return tagString;
  }

  public String getWord(int pos) {
    return words.get(pos);

  }

  public String getTag(int pos) {
    return tags.get(pos);

  }

  public void getTagStringPositions() {
    TagStringPosToSequencePos.clear();
    int begin = 0;
    for (int i = 0; i < tags.size(); i++) {
      TagStringPosToSequencePos.put(begin, i);
      begin += (tags.get(i).length() + 1);
    }
  }

  public void getWordStringPositions() {
    WordStringPosToSequencePos.clear();
    int begin = 0;
    for (int i = 0; i < words.size(); i++) {
      WordStringPosToSequencePos.put(begin, i);
      begin += (words.get(i).length() + 1);
    }
  }

  public void printPosToSeq() {
    int[] keys = new int[TagStringPosToSequencePos.keySet().size()];
    int i = 0;
    for (int key : TagStringPosToSequencePos.keySet()) {
      keys[i++] = key;
    }
    Arrays.sort(keys);
    for (i = 0; i < keys.length; i++) {
      System.out.println(keys[i] + "/" + TagStringPosToSequencePos.get(keys[i]));
    }

  }

  public String WordtoString() {
    StringBuilder sb = new StringBuilder();
    for (String word : words) {
      sb.append(word + " ");
    }
    return sb.toString().trim();
  }

  public String WordTagToString() {
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < words.size(); i++) {
      String word = words.get(i);
      String tag = tags.get(i);
      sb.append(word + "/" + tag + " ");
    }
    return sb.toString().trim();
  }
  
  public String WordTagToStringVerbose() {
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < words.size(); i++) {
      String word = words.get(i);
      String tag = tags.get(i);
      sb.append(word + "\t" + tag + "\n");
    }
    return sb.toString().trim();
  }

  public String TagtoString() {
    StringBuilder sb = new StringBuilder();
    for (String word : tags) {
      sb.append(word + " ");
    }
    return sb.toString().trim();
  }

  public int size() {
    return words.size();
  }

}
