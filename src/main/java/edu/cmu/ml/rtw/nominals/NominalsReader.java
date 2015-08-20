package edu.cmu.ml.rtw.nominals;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import edu.cmu.ml.rtw.generic.data.annotation.AnnotationType;
import edu.cmu.ml.rtw.generic.data.annotation.nlp.AnnotationTypeNLP;
import edu.cmu.ml.rtw.generic.data.annotation.nlp.DocumentNLP;
import edu.cmu.ml.rtw.generic.data.annotation.nlp.PoSTag;
import edu.cmu.ml.rtw.generic.data.annotation.nlp.TokenSpan;
import edu.cmu.ml.rtw.generic.data.annotation.nlp.AnnotationTypeNLP.Target;
import edu.cmu.ml.rtw.generic.data.annotation.nlp.micro.Annotation;
import edu.cmu.ml.rtw.generic.model.annotator.nlp.AnnotatorTokenSpan;
import edu.cmu.ml.rtw.generic.util.Pair;
import edu.cmu.ml.rtw.generic.util.Triple;
import edu.cmu.ml.rtw.micro.cat.data.annotation.nlp.AnnotationTypeNLPCat;
import edu.cmu.ml.rtw.nominals.util.RelationSequence;
import edu.cmu.ml.rtw.nominals.util.WordSequence;

public class NominalsReader implements AnnotatorTokenSpan<BinaryRelationInstance> {

  public static final AnnotationTypeNLP<BinaryRelationInstance> NOMINALRELATIONS = new AnnotationTypeNLP<BinaryRelationInstance>("nell-nom",
      BinaryRelationInstance.class, Target.TOKEN_SPAN);

  static final AnnotationType<?>[] REQUIRED_ANNOTATIONS = new AnnotationType<?>[] {
      AnnotationTypeNLP.TOKEN,
      AnnotationTypeNLP.SENTENCE,
      AnnotationTypeNLP.POS };

  public CompoundNounBasicExtractor basicextractor;
  static HashMap<String, String> countryMap = new HashMap<String, String>();
  HashMap<String, HashSet<String>> npCategoriesInContext;
  static String SEP = "\t";
  String NELL_typePrefix = "NELL_";
  String WordNet_typePrefix = "WDN_";
  String NELLX = "NELL_X";
  String NELLY = "NELL_Y";
  String NELLZ = "NELL_Z";

  public NominalsReader() {
    //    if (basicextractor == null) {
    //      basicextractor = new CompoundNounBasicExtractor();
    //      countryMap = basicextractor.countryMap;
    //    }
  }

  public List<BinaryRelationInstance> process(TriNominal Nom, HashMap<String, HashMap<String, RelationSequence>> sequences,
      HashMap<String, HashSet<String>> nellTypes, HashMap<String, HashSet<String>> npCategoriesInContext) throws NumberFormatException, IOException {

    HashMap<String, HashSet<String>> relationstoInstances = new HashMap<String, HashSet<String>>();
    for (String relation : sequences.keySet()) {
      relationstoInstances.put(relation, new HashSet<String>());
    }

    List<BinaryRelationInstance> result = new ArrayList<BinaryRelationInstance>();

    HashSet<String> uniqueTriplesSet = new HashSet<String>();

    String arg1Orginal = Nom.arg1;
    String arg2Orginal = Nom.arg2;
    String arg3Original = Nom.arg3;

    String arg1 = Nom.arg1.toLowerCase().trim();
    String arg2 = Nom.arg2.toLowerCase().trim();
    // if (arg2.length() <= 1) continue;
    String arg3 = Nom.arg3.toLowerCase().trim();

    String triple = arg1Orginal + SEP + arg2Orginal + SEP + arg3Original;

    HashSet<String> N1TypesNELL = nellTypes.get(arg1) == null ? new HashSet<String>() : nellTypes.get(arg1);
    HashSet<String> N2TypesNELL = nellTypes.get(arg3) == null ? new HashSet<String>() : nellTypes.get(arg3);

    if (npCategoriesInContext.get(arg1) != null) {
      N1TypesNELL.addAll(npCategoriesInContext.get(arg1));
      //      for (String found : npCategoriesInContext.get(arg1)) {
      //      }
    }
    if (npCategoriesInContext.get(arg3) != null) {
      N2TypesNELL.addAll(npCategoriesInContext.get(arg3));
      //      for (String found : npCategoriesInContext.get(arg3)) {
      //      }
    }

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
          relationstoInstances.get(relation).add(relation + SEP + a1 + SEP + a2);
          result.add(new BinaryRelationInstance(relation, a1, a2));

        }
      }
    }
    uniqueTriplesSet.add(triple);

    return result;
  }

  public List<BinaryRelationInstance> extract(TriNominal nom, HashMap<String, HashSet<String>> npCategoriesInContext) throws Exception {
    if (basicextractor == null) {
      basicextractor = new CompoundNounBasicExtractor();
      countryMap = basicextractor.countryMap;
    }
    HashMap<String, HashMap<String, RelationSequence>> nominalsequences = basicextractor.nominalsequences;
    HashMap<String, HashSet<String>> nellTypes = basicextractor.nellTypes;
    List<BinaryRelationInstance> result = process(nom, nominalsequences, nellTypes, npCategoriesInContext);

    return result;
  }

  public String getName() {
    return "cmunell_nom-0.0.1";
  }

  public boolean measuresConfidence() {
    return false;
  }

  public AnnotationType<BinaryRelationInstance> produces() {
    return NOMINALRELATIONS;
  }

  public AnnotationType<?>[] requires() {
    return REQUIRED_ANNOTATIONS;
  }

  public List<Triple<TokenSpan, BinaryRelationInstance, Double>> annotate(DocumentNLP document) {
    List<Triple<TokenSpan, BinaryRelationInstance, Double>> nominalsAnnotations = new ArrayList<Triple<TokenSpan, BinaryRelationInstance, Double>>();

    // get all   noun phrase types found by the CAT annotator
    HashMap<String, HashSet<String>> npCategoriesInContext = new HashMap<String, HashSet<String>>();
    Collection<AnnotationTypeNLP<?>> col = new ArrayList<AnnotationTypeNLP<?>>();
    col.add(AnnotationTypeNLPCat.NELL_CATEGORY);
    List<Annotation> annotations = document.toMicroAnnotation(col).getAllAnnotations();
    for (Annotation annotation : annotations) {
      int startTokenIndex = -1;
      int endTokenIndex = -1;
      int sentenceIndex = -1;
      int sentCount = document.getSentenceCount();
      for (int j = 0; j < sentCount; j++) {
        int tokenCount = document.getSentenceTokenCount(j);
        for (int i = 0; i < tokenCount; i++) {
          if (document.getToken(j, i).getCharSpanStart() == annotation.getSpanStart()) startTokenIndex = i;
          if (document.getToken(j, i).getCharSpanEnd() == annotation.getSpanEnd()) {
            endTokenIndex = i + 1;
            sentenceIndex = j;
            break;
          }
        }
      }

      if (startTokenIndex < 0 || endTokenIndex < 0) {
      } else {
        TokenSpan np = new TokenSpan(document, sentenceIndex, startTokenIndex, endTokenIndex);
        String val = annotation.getStringValue();
        if (npCategoriesInContext.get(np.toString().toLowerCase()) == null) {
          npCategoriesInContext.put(np.toString().toLowerCase(), new HashSet<String>());
        }
        npCategoriesInContext.get(np.toString().toLowerCase()).add(val);

      }
    }

    for (int sentenceIndex = 0; sentenceIndex < document.getSentenceCount(); sentenceIndex++) {
      List<PoSTag> tags = document.getSentencePoSTags(sentenceIndex);
      List<String> words = document.getSentenceTokenStrs(sentenceIndex);

      WordSequence wordSequence = new WordSequence();
      for (int j = 0; j < words.size(); j++) {
        wordSequence.appendTag(tags.get(j).name());
        wordSequence.appendWord(words.get(j));
      }

      if (basicextractor == null) {
        basicextractor = new CompoundNounBasicExtractor();
        countryMap = basicextractor.countryMap;
      }
      List<TriNominal> nominalsList = basicextractor.extractNominals(wordSequence);

      if (nominalsList.size() > 0) {
        List<Pair<TokenSpan, String>> NER = document.getNer();
        for (Pair<TokenSpan, String> span : NER) {
          if (span.getSecond().toString().equals("O")) continue;

          String entity = span.getFirst().toString();
          if (npCategoriesInContext.get(entity) == null) {
            npCategoriesInContext.put(entity, new HashSet<String>());
          }
          npCategoriesInContext.get(entity).add(span.getSecond().toString().toLowerCase());

          entity = span.getFirst().toString().toLowerCase();
          if (npCategoriesInContext.get(entity) == null) {
            npCategoriesInContext.put(entity, new HashSet<String>());
          }
          npCategoriesInContext.get(entity).add(span.getSecond().toString().toLowerCase());

        }
      }
      for (TriNominal nom : nominalsList) {
        try {
          List<BinaryRelationInstance> result = extract(nom, npCategoriesInContext);
          TokenSpan tokenspan = new TokenSpan(document, sentenceIndex, nom.spanStart, nom.spanEnd);
          for (BinaryRelationInstance item : result) {
            nominalsAnnotations.add(new Triple<TokenSpan, BinaryRelationInstance, Double>(tokenspan, item, 0.8));
          }

        } catch (Exception e) {
          e.printStackTrace();
        }

      }

    }

    return nominalsAnnotations;
  }
}
