package jfoley.vocabpress.postings;

/**
 * @author jfoley
 */
public abstract class APosting implements Posting {
  protected final int document;

  protected APosting(int document) {
    this.document = document;
  }

  @Override
  public int getKey() {
    return document;
  }

  @Override
  public int compareTo(Posting o) {
    assert(o != null);
    return Integer.compare(document, o.getKey());
  }
}
