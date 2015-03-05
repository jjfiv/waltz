package jfoley.vocabpress.scoring.iter;

import jfoley.vocabpress.scoring.Posting;

/**
 * @author jfoley
 */
public interface PostingIterator extends Comparable<PostingIterator> {
  public static final int NO_MORE_POSTINGS = Integer.MAX_VALUE;

  public boolean hasCurrent();
  public boolean hasNext();

  /**
   * What is the key of the current posting?
   * @return NO_MORE_POSTINGS if done, or the id if pointing at the last document.
   */
  public int currentKey();

  /**
   * Get the posting for the given document if possible.
   * @return Posting for document id = 0, or null if already past, or null if not present.
   */
  public Posting getPosting(int id);

  /**
   * Move past the given key in the list.
   * @param key the document id or other integer key on which the posting list is sorted.
   */
  public void movePast(int key);

  /**
   * Move to the given key in the list, if it's present.
   * @param key the document id or other integer key on which the posting list is sorted.
   */
  public void moveTo(int key);
}
