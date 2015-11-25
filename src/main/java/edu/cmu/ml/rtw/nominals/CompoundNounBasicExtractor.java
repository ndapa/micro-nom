package edu.cmu.ml.rtw.nominals;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import edu.cmu.ml.rtw.generic.util.FileUtil;
import edu.cmu.ml.rtw.nominals.util.NounPhrase;
import edu.cmu.ml.rtw.nominals.util.RelationSequence;
import edu.cmu.ml.rtw.nominals.util.WordSequence;

public class CompoundNounBasicExtractor {

  // works for both proper nouns and common nous as discovered by the simple chunker
  protected final String nominals = "(NNP\\s){1,}(NN\\s){1}(NNP\\s){1,}"; //NNP NNP NN NNP NNP
  protected final String nominals2 = "(NN\\s){1,3}(NNP\\s){1,}"; //NN NN NNP NNP NNP Or  NN NNP NNP
  protected final String nominals3 = "(JJ\\s){1,3}(NN\\s){1}(NNP\\s){1,}"; // JJ NN NNP NNP OR JJ JJ JJ NN NNP NNP Or  JJ NNP NNP

  String commonNoun = "NN";
  String SEP = "\t";
  protected List<String> nominalsList = new ArrayList<String>(Arrays.asList(new String[] { nominals, nominals3 }));

  static final String countries = "country_adjectivenames.tsv";
  static final String entityTypes = "typed_nps.tsv";
  final String relationsToSequences = "nominals_sequences.tsv";

  HashMap<String, String> countryMap = new HashMap<String, String>();
  HashMap<String, HashMap<String, RelationSequence>> nominalsequences = new HashMap<String, HashMap<String, RelationSequence>>();
  HashMap<String, HashSet<String>> nellTypes = new HashMap<String, HashSet<String>>();

  public CompoundNounBasicExtractor() {
    try {
      countryMap = countryNames(countries);
      nellTypes = getNELLTypes(entityTypes);
      nominalsequences = readRelationSequences(relationsToSequences);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public HashMap<String, String> countryNames(String filename) throws IOException {

    HashMap<String, String> result = new HashMap<String, String>();

    BufferedReader bf = FileUtil.getFileReader(filename);
    String line;
    while ((line = bf.readLine()) != null) {

      String[] parts = line.split(SEP);
      if (parts.length < 2) continue;
      String[] names = parts[1].split(", ");
      // String [] names2 = parts[2];
      for (String name : names) {
        String val = name.trim();
        result.put(val, parts[0].trim());
        result.put(val.toLowerCase(), parts[0].trim());
      }

    }

    return result;
  }

  public HashMap<String, HashSet<String>> getNELLTypes(String filename) throws NumberFormatException, IOException {
    HashMap<String, HashSet<String>> nellTypes = new HashMap<String, HashSet<String>>();
    String line;
    BufferedReader br = FileUtil.getFileReader(filename);
    while ((line = br.readLine()) != null) {
      String[] parts = line.split("\t");
      if (parts.length < 2) continue;
      int i = 0;
      String entity = parts[i++];
      HashSet<String> types = new HashSet<String>();
      while (i < parts.length) {
        types.add(parts[i++]);
      }
      nellTypes.put(entity, types);
    }

    return nellTypes;
  }

  public HashMap<String, HashMap<String, RelationSequence>> readRelationSequences(String relationsToSequences) throws NumberFormatException,
      IOException {

    HashSet<String> relationsOfInterest = new HashSet<String>();

    relationsOfInterest.add("concept:bookwriter");
    relationsOfInterest.add("concept:athletewinsawardtrophytournament");
    relationsOfInterest.add("concept:citylocatedincountry");
    relationsOfInterest.add("concept:coachesteam");
    relationsOfInterest.add("concept:athleteplaysforteam");
    relationsOfInterest.add("concept:musicianplaysinstrument");
    relationsOfInterest.add("concept:worksfor");
    relationsOfInterest.add("concept:personhasjobposition");
    relationsOfInterest.add("[CitizenOfCountry]");
    relationsOfInterest.add("star");
    //    relationsOfInterest.add("biographer");
    relationsOfInterest.add("author");
    //    relationsOfInterest.add("founder");
    relationsOfInterest.add("subsidiary");
    //    relationsOfInterest.add("legend");
    //    relationsOfInterest.add("owner");
    relationsOfInterest.add("resident");
    relationsOfInterest.add("alumnus");
    //    relationsOfInterest.add("aide");
    //    relationsOfInterest.add("fan");
    //
    //    lowCountRelations.add("concept:athleteplayssport");
    //    lowCountRelations.add("concept:athleteplaysinleague");
    //    lowCountRelations.add("concept:acquired");
    //    lowCountRelations.add("concept:musicartistgenre");
    //    lowCountRelations.add("concept:actorstarredinmovie");

    HashMap<String, HashMap<String, RelationSequence>> relationsToSequenceMap = new HashMap<String, HashMap<String, RelationSequence>>();
    int rel = 0;
    String lineItem;
    BufferedReader bf = FileUtil.getFileReader(relationsToSequences);
    while ((lineItem = bf.readLine()) != null) {
      String[] parts = lineItem.split(SEP);
      String relation = parts[0];
      //System.out.println("\t\t" + relation + "\t" + lineItem + SEP + parts.length);
      int numSeqs = Integer.parseInt(parts[1]);

      //System.out.println(++rel + "Rela: " + relation);
      if (relationsOfInterest.contains(relation)) relationsToSequenceMap.put(relation, new HashMap<String, RelationSequence>());

      // for (int j = i; j < (i + numSeqs); j++) {
      String line = bf.readLine();
      while (line != null && line.trim().length() != 0) {

        String[] items = line.split(SEP);
        //System.out.println("\t" + relation + "\t" + line + SEP + items.length);
        int pos1 = Integer.parseInt(items[3]);
        int pos2 = Integer.parseInt(items[4]);

        double freq = Double.parseDouble(items[5]);

        //          if(items[1].equals("outsider")){
        // System.out.println("\t\t\t" + relation + "\t" + line + "| " + freq + "| " + (freq < 15));
        //          }
        // if (freq < 14 && !lowCountRelations.contains(relation)) continue;
        String args = items[0] + SEP + items[1] + SEP + items[2];
        // System.out.println(args);

        if (relationsOfInterest.contains(relation)) {
          relation = relation.trim();
          //          if (relation.equals("[CitizenOfCountry]")) {
          //            String relation2 = "per:country_of_birth";
          //            if (relationsToSequenceMap.get(relation2) == null) {
          //              relationsToSequenceMap.put(relation2, new HashMap<String, RelationSequence>());
          //            }
          //            // relationsToSequenceMap.get(relation.trim()).put(args.trim(), new RelationSequence(items[0], items[1], items[2], pos1, pos2, freq));
          //            relationsToSequenceMap.get(relation2.trim()).put(args.trim(), new RelationSequence(items[0], items[1], items[2], pos1, pos2, freq));
          //            relation = "per:countries_of_residence";
          //
          //          }
          //
          //          if (relation.equals("concept:personhasjobposition")) {
          //            String relation2 = "per:title";
          //            if (relationsToSequenceMap.get(relation2) == null) {
          //              relationsToSequenceMap.put(relation2, new HashMap<String, RelationSequence>());
          //            }
          //
          //            relationsToSequenceMap.get(relation2.trim()).put(args.trim(), new RelationSequence(items[0], items[1], items[2], pos1, pos2, freq));
          //          }
          //
          //          if (relation.equals("concept:athletewinsawardtrophytournament")) {
          //            String relation2 = "per:awards_won";
          //            if (relationsToSequenceMap.get(relation2) == null) {
          //              relationsToSequenceMap.put(relation2, new HashMap<String, RelationSequence>());
          //            }
          //
          //            relationsToSequenceMap.get(relation2.trim()).put(args.trim(), new RelationSequence(items[0], items[1], items[2], pos1, pos2, freq));
          //          }
          //
          //          if (relation.equals("founder")) {
          //            String relation2 = "org:founded_by";
          //            if (relationsToSequenceMap.get(relation2) == null) {
          //              relationsToSequenceMap.put(relation2, new HashMap<String, RelationSequence>());
          //            }
          //
          //            relationsToSequenceMap.get(relation2.trim()).put(args.trim(), new RelationSequence(items[0], items[1], items[2], pos1, pos2, freq));
          //          }
          //          if (relation.equals("concept:worksfor")) {
          //            String relation2 = "per:employee_or_member_of";
          //            if (relationsToSequenceMap.get(relation2) == null) {
          //              relationsToSequenceMap.put(relation2, new HashMap<String, RelationSequence>());
          //            }
          //
          //            relationsToSequenceMap.get(relation2.trim()).put(args.trim(), new RelationSequence(items[0], items[1], items[2], pos1, pos2, freq));
          //          }

          if (relation.trim().startsWith("concept")) {
            relation = relation.substring(relation.indexOf(":") + 1);
          }

          if (relationsToSequenceMap.get(relation) == null) {
            relationsToSequenceMap.put(relation, new HashMap<String, RelationSequence>());
          }

          relationsToSequenceMap.get(relation.trim()).put(args.trim(), new RelationSequence(items[0], items[1], items[2], pos1, pos2, freq));

        }
        // System.out.println(SEP + args + SEP + pos1 + SEP + pos2);
        //}
        line = bf.readLine();
      }

    }
    return relationsToSequenceMap;
  }

  public List<TriNominal> extractNominals(WordSequence wordSequence) {
    // add stop tag to ensure space at the end of the tag sequence
    wordSequence.tags.add("STOP");

    List<TriNominal> nominalNPList = new ArrayList<TriNominal>();

    for (int patternId = 0; patternId < nominalsList.size(); patternId++) {
      String nounphrasePattern = nominalsList.get(patternId);
      Pattern familyNameSuffixPattern = Pattern.compile(nounphrasePattern);
      Matcher matcher = familyNameSuffixPattern.matcher(wordSequence.TagtoString());

      while (matcher.find()) {

        // start and end of character starting positions
        int start = matcher.start();
        int end = matcher.end();

        // find actual word starting positions
        String phrase = wordSequence.TagtoString().substring(start, end);
        int count = new StringTokenizer(phrase).countTokens();

        int wordSeqStart = wordSequence.TagStringPosToSequencePos.get(start);
        String entityPhrase = "";
        while (count > 0) {
          entityPhrase = entityPhrase + wordSequence.words.get(wordSeqStart++) + " ";
          count--;
        }
        NounPhrase nominalNP = new NounPhrase(entityPhrase, wordSequence.TagStringPosToSequencePos.get(start).intValue(), (wordSeqStart - 1));
        String[] text = entityPhrase.trim().split(" ");
        String[] tags = phrase.trim().split(" ");
        WordSequence wordseqNP = new WordSequence();
        StringBuilder np1 = new StringBuilder();
        StringBuilder np2 = new StringBuilder();
        StringBuilder np3 = new StringBuilder();
        boolean improperCommonNoun = false;
        boolean CommonNounFound = false;
        for (int i = 0; i < tags.length; i++) {
          wordseqNP.appendTag(tags[i]);
          wordseqNP.appendWord(text[i]);

          if (tags[i].equals(commonNoun)) {
            CommonNounFound = true;
            np2.append(text[i]);
            if (Character.isUpperCase(text[i].charAt(0))) {
              //&& !PhraseOps.isAllUpperCase(text[i])) {
              improperCommonNoun = true;
            }
          } else {
            if (!CommonNounFound) {
              np1.append(text[i]).append(" ");
            } else {
              np3.append(text[i]).append(" ");
            }
          }
        }
        if (improperCommonNoun) continue;
        if (nounphrasePattern.equals(nominals3) && !countryMap.containsKey(np1.toString().trim().toLowerCase())) {
          continue;
        }
        TriNominal found = new TriNominal(np1.toString(), np2.toString(), np3.toString(), nominalNP.startInWordSequence(),
            nominalNP.endInWordSequence());
        nominalNPList.add(found);
        //        String instanceFound = nominalNP.toString() + SEP + np1.toString() + SEP + np2.toString() + SEP + np3.toString() + SEP
        //            + wordseqNP.WordTagToString() + SEP + nominalNP.startInWordSequence() + SEP + nominalNP.endInWordSequence() + SEP
        //            + wordSequence.WordtoString();
        // System.out.println(found);
        //+ SEP + np1.toString() + SEP + np2.toString() + SEP + np3.toString() + SEP + wordSequence.WordtoString());
      }
    }

    return nominalNPList;
  }

}
