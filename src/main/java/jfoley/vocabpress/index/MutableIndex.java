package jfoley.vocabpress.index;

import java.util.List;

/**
 * @author jfoley
 */
public interface MutableIndex extends Index {

  /** add a document to the current index */
  void addDocument(String documentName, List<String> termVector);
}
