package edu.cmu.ml.rtw.nominals;

import edu.cmu.ml.rtw.generic.util.StringSerializable;

public class BinaryRelationInstance implements StringSerializable {

  String relation;
  String arg1;
  String arg2;

  public BinaryRelationInstance(String relation, String arg1, String arg2) {
    this.relation = relation;
    this.arg1 = arg1;
    this.arg2 = arg2;
  }

  public String toString() {

    return relation + "\t" + arg1 + "\t" + arg2;
  }

  public boolean fromString(String arg0) {
    String[] parts = arg0.toLowerCase().split("\t");

    try {
      new BinaryRelationInstance(parts[0], parts[1], parts[2]);
    } catch (Exception e) {
      return false;
    }

    return true;
  }

}
