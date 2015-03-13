package jfoley.vocabpress.postings;

/**
 * @author jfoley
 */
public abstract class APosting<V> implements Posting<V> {
  protected final int document;

  protected APosting(int document) {
    this.document = document;
  }

  @Override
  public int getKey() {
    return document;
  }

  @Override
  public int compareTo(Posting<V> o) {
    assert(o != null);
    return Integer.compare(document, o.getKey());
  }
}
