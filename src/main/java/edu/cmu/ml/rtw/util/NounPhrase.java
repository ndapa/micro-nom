package edu.cmu.ml.rtw.util;

import java.util.List;

/**
 * @author ndapa
 * 
 */

public class NounPhrase {

  int startInWordSequence;

  int endInWordSequence;

  boolean commonNoun;
  boolean numberNoun;
  boolean personalPronounNoun;
  boolean ProperNoun;
  boolean percentNoun;

  private String nounphrase;

  private String tagseq;

  public NounPhrase() {

  }

  public NounPhrase(String nounphrase, int start, int end) {
    this.commonNoun = false;
    this.numberNoun = false;
    this.percentNoun = false;
    this.personalPronounNoun = false;
    this.ProperNoun = true;
    this.nounphrase = nounphrase;
    this.startInWordSequence = start;
    this.endInWordSequence = end;
  }

  public boolean overlapsWith(NounPhrase np) {
    NounPhrase XY1;
    NounPhrase XY2;

    if (startInWordSequence <= np.startInWordSequence) {
      XY1 = this;
      XY2 = np;
      if (startInWordSequence == np.startInWordSequence) {
        if (endInWordSequence > np.endInWordSequence) {
          XY1 = np;
          XY2 = this;
        }
      }
    } else {
      XY1 = np;
      XY2 = this;
    }
    return (XY1.startInWordSequence <= XY2.endInWordSequence && XY2.startInWordSequence <= XY1.endInWordSequence);
  }

  public boolean sameNP(NounPhrase np) {
    return (endInWordSequence == np.endInWordSequence && startInWordSequence == np.startInWordSequence);
  }

  public int sizeInText() {
    return (endInWordSequence - startInWordSequence);

  }

  public int startInWordSequence() {
    return startInWordSequence;
  }

  public int endInWordSequence() {
    return endInWordSequence;
  }

  public void SetstartInWordSequence(int start) {
    startInWordSequence = start;
  }

  public void SetendInWordSequence(int end) {
    endInWordSequence = end;
  }

  public void Settext(String text) {
    nounphrase = text;
  }

  public String toString() {
    return nounphrase;
  }

  public static NounPhrase[] sort(List<NounPhrase> nounphrases) {
    NounPhrase[] sorted = new NounPhrase[nounphrases.size()];
    for (int i = 0; i < sorted.length; i++)
      sorted[i] = new NounPhrase("-1", -1, 0);

    sorted[0] = nounphrases.remove(0);
    for (int i = 1; (nounphrases.size() > 0); i++) {
      NounPhrase current = nounphrases.remove(0);

      // find correct position in array
      boolean insertion = false;
      for (int j = 0; j < i; j++) {
        if (!insertion && current.startInWordSequence <= sorted[j].startInWordSequence) {

          while (j < i) {
            NounPhrase temp = sorted[j];
            sorted[j] = current;
            current = temp;
            j++;
          }
          if (current.startInWordSequence >= 0) sorted[j] = current;

          insertion = true;
        }

      }
      if (!insertion) sorted[i] = current;

    }
    return sorted;
  }

  public boolean isCommonNoun() {
    return commonNoun;
  }

  public boolean isDateNoun() {
    return numberNoun;
  }

  public boolean isPercentNoun() {
    return percentNoun;
  }

  public void setTagSequence(String tags) {
    tagseq = tags;
  }

  public String getTagSequence() {
    return tagseq;
  }

  static class DateNounPhrase extends NounPhrase {

    public DateNounPhrase(String nounphrase, int start, int end) {
      super(nounphrase, start, end);
    }
  }
}
