package edu.cmu.ml.rtw.nominals;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;

import edu.cmu.ml.rtw.nominals.util.RelationSequence;

public class NominalsReader {

  public CompoundNounBasicExtractor basicextractor;
  static HashMap<String, String> countryMap = new HashMap<String, String>();
  static String SEP = "\t";
  String NELL_typePrefix = "NELL_";
  String WordNet_typePrefix = "WDN_";
  String NELLX = "NELL_X";
  String NELLY = "NELL_Y";
  String NELLZ = "NELL_Z";

  public NominalsReader() {
    if (basicextractor == null) {
      basicextractor = new CompoundNounBasicExtractor();
      countryMap = basicextractor.countryMap;
    }
  }

  public void process(TriNominal Nom, HashMap<String, HashMap<String, RelationSequence>> sequences, HashMap<String, HashSet<String>> nellTypes)
      throws NumberFormatException, IOException {

    HashMap<String, HashSet<String>> relationstoInstances = new HashMap<String, HashSet<String>>();
    for (String relation : sequences.keySet()) {
      relationstoInstances.put(relation, new HashSet<String>());
    }

    HashSet<String> uniqueTriplesSet = new HashSet<String>();

    String arg1Orginal = Nom.arg1;
    String arg2Orginal = Nom.arg2;
    String arg3Original = Nom.arg3;

    String arg1 = Nom.arg1.toLowerCase().trim();
    String arg2 = Nom.arg2.toLowerCase().trim();
    // if (arg2.length() <= 1) continue;
    String arg3 = Nom.arg3.toLowerCase().trim();

    // 4 is POS tagged
    //        String phraseBegin = parts[5].trim();
    //        String phraseEnd = parts[6].trim();
    //        

    // NY and WKP nouns mixed up
    //    String[] arg1P = arg1.split(" ");
    //    String[] arg1PO = arg1Orginal.split(" ");
    //
    //    String arg1R = "";
    //    String arg1RO = "";
    //    for (int k = 0; k < arg1P.length; k++) {
    //      String word = arg1P[k];
    //      if (Character.isLowerCase(word.charAt(0)) && arg2.equals(word.trim())) break;
    //      arg1R = arg1R + " " + word;
    //      arg1RO = arg1RO + " " + arg1PO[k];
    //    }
    //    arg1Orginal = arg1RO.trim();

    String triple = arg1Orginal + SEP + arg2Orginal + SEP + arg3Original;
    //System.out.println(triple);
    //if (uniqueTriplesSet.contains(triple)) continue;
    //if (FinalData.monthsLowerCase.contains(arg3) || FinalData.daysOfWeek.contains(arg3)) continue;

    HashSet<String> N1TypesNELL = nellTypes.get(arg1) == null ? new HashSet<String>() : nellTypes.get(arg1);
    HashSet<String> N2TypesNELL = nellTypes.get(arg3) == null ? new HashSet<String>() : nellTypes.get(arg3);

    HashSet<String> N1Types = new HashSet<String>();
    HashSet<String> N2Types = new HashSet<String>();

    N2Types.add(NELLY);
    N1Types.add(NELLX);

    for (String narg : N1TypesNELL) {
      N1Types.add(NELL_typePrefix + narg);
    }
    for (String narg : N2TypesNELL) {
      N2Types.add(NELL_typePrefix + narg);
    }

    if (countryMap.get(arg1) != null) {
      //arg1 = countryMap.get(arg1);
      N1Types.clear();
      N1Types.add("WKP_country");
      arg1 = countryMap.get(arg1);
      arg1Orginal = arg1;
    }

    String rel = arg2.trim();
    HashSet<String> NRelTypesNELL = new HashSet<String>();
    NRelTypesNELL.add(rel);
    NRelTypesNELL.add(NELLZ);
    //        HashSet<String> wordNetTypes = WordnetThesaurus.getParentsRecursivelyNounsOnly(rel);
    //        if (wordNetTypes.contains("person")) {
    //          NRelTypesNELL.add(WordNet_typePrefix + "person");
    //        } else if (wordNetTypes.contains("group")) {
    //          NRelTypesNELL.add(WordNet_typePrefix + "group");
    //        }
    HashSet<String> N3TypesNELL = nellTypes.get(rel) == null ? new HashSet<String>() : nellTypes.get(rel);
    for (String type : N3TypesNELL) {
      NRelTypesNELL.add(NELL_typePrefix + type);
    }

    HashSet<String> templates = new HashSet<String>();
    for (String n1 : N1Types) {

      for (String n2 : N2Types) {

        for (String n3 : NRelTypesNELL) {
          String template = n1 + SEP + n3 + SEP + n2;
          templates.add(template.trim());
        }
      }
    }

    //    for (String temp : templates) {
    //      System.out.println(SEP + SEP + temp);
    //    }
    //    System.out.println();

    HashMap<Integer, String> idsToArgs = new HashMap<Integer, String>();
    idsToArgs.put(1, arg1Orginal);
    idsToArgs.put(2, arg2Orginal);
    idsToArgs.put(3, arg3Original);

    // check which type signatures are satisified to generate new relation instances
    for (String relation : sequences.keySet()) {

      for (String template : templates) {
        if (sequences.get(relation).containsKey(template)) {

          RelationSequence seq = sequences.get(relation).get(template);
          String a1 = idsToArgs.get(seq.pos1);
          String a2 = idsToArgs.get(seq.pos2);
          //relationstoInstances.get(relation).add(relation + "(" + a1 + "," + a2 + ")" + SEP + item + SEP + template);
          relationstoInstances.get(relation).add(relation + SEP + a1 + SEP + a2 + SEP + triple);
        }
      }
    }
    uniqueTriplesSet.add(triple);

    for (String relation : relationstoInstances.keySet()) {

      //System.out.println(relationstoInstances.get(relation).size());
      for (String instance : relationstoInstances.get(relation)) {
        System.out.println(SEP + instance);
      }
    }

  }

  public void extract(TriNominal nom) throws Exception {
    if (basicextractor == null) {
      basicextractor = new CompoundNounBasicExtractor();
      countryMap = basicextractor.countryMap;
    }
    HashMap<String, HashMap<String, RelationSequence>> nominalsequences = basicextractor.nominalsequences;
    HashMap<String, HashSet<String>> nellTypes = basicextractor.nellTypes;
    process(nom, nominalsequences, nellTypes);

    //          // find properties in sentence
    //          if (wordseq.size() > 300) continue;
    //
    //          List<TriNominal> nominalsList = cmp.extractNominals(wordseq);
    //          for (TriNominal nom : nominalsList) {
    //
    //            
    //          }

  }

}
