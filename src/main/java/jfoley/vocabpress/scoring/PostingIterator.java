package jfoley.vocabpress.scoring;

/**
 * @author jfoley
 */
public interface PostingIterator {
  public static final int NO_MORE_POSTINGS = Integer.MAX_VALUE;

  /**
   * What is the key of the current posting?
   * @return NO_MORE_POSTINGS if done, or the id if pointing at the last document.
   */
  public int currentKey();

  /**
   * What is the key of the next posting? i.e. what is the next document number for which this list has a posting?
   *
   * @return NO_MORE_POSTINGS if no next posting, or the id if one is available.
   */
  public int nextKey();

  /**
   * Get the posting for the given document if possible.
   * @return Posting for document id = 0, or null if already past, or null if not present.
   */
  public Posting getPosting(int id);
}
