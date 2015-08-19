package edu.cmu.ml.rtw.nominals.util;


public class RelationSequence {

  String arg0;
  String arg1;
  String arg2;

  public int pos1;
  public int pos2;
  double freq;

  WordSequence taggedSentence;

  public RelationSequence() {
    ;
  }

  public RelationSequence(String arg0, String arg1, String arg2, int pos1, int pos2, double freq) {
    this.arg0 = arg0;
    this.arg1 = arg1;
    this.arg2 = arg2;
    this.pos1 = pos1;
    this.pos2 = pos2;
    this.freq = freq;

  }

  public RelationSequence(String arg0, String arg1, String arg2, int pos1, int pos2, double freq, WordSequence taggedSentence) {
    this.arg0 = arg0;
    this.arg1 = arg1;
    this.arg2 = arg2;
    this.pos1 = pos1;
    this.pos2 = pos2;
    this.freq = freq;
    this.taggedSentence = taggedSentence;
  }

  public String toString() {
    String SEP = "\t";
    return arg0.trim() + SEP + arg1.trim() + SEP + arg2.trim();
  }

}
