package edu.cmu.ml.rtw.nominals;

public class TriNominal {

  String arg1;
  String arg2;
  String arg3;

  int spanStart;
  int spanEnd;
  public TriNominal(String arg1, String arg2, String arg3, int spanStart, int spanEnd) {
    this.arg1 = arg1;
    this.arg2 = arg2;
    this.arg3 = arg3;
    this.spanStart= spanStart;
    this.spanEnd= spanEnd;
  }
 

  public String arg1() {
    return arg1;
  }

  public String arg2() {
    return arg2;
  }

  public String arg3() {
    return arg3;
  }
  
  public int getSpandStart(){
    return spanStart;
  }
  public int getSpandEnd(){
    return spanEnd;
  }

  public String toString() {
    String SEP = "\t";
    return arg1 + SEP + arg2 + SEP + arg3;
  }

}
