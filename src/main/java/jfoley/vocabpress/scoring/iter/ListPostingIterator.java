package jfoley.vocabpress.scoring.iter;

import jfoley.vocabpress.scoring.Posting;

import java.util.List;

/**
 * @author jfoley
 */
public class ListPostingIterator<P extends Posting> implements PostingIterator {
  protected int currentIndex;
  protected final List<P> postings;

  public ListPostingIterator(List<P> postings) {
    this.postings = postings;
    this.currentIndex = 0;
  }

  @Override
  public boolean hasCurrent() {
    return currentIndex < postings.size();
  }

  @Override
  public boolean hasNext() {
    return currentIndex + 1 < postings.size();
  }

  @Override
  public int currentKey() {
    return hasCurrent() ? postings.get(currentIndex).getKey() : NO_MORE_POSTINGS;
  }

  @Override
  public int nextKey() {
    return hasNext() ? postings.get(currentIndex+1).getKey() : NO_MORE_POSTINGS;
  }

  @Override
  public void movePast(int key) {
    while(hasCurrent() && currentKey() <= key) {
      currentIndex++;
    }
  }

  @Override
  public P getPosting(int id) {
    while(hasNext() && currentKey() < id) {
      currentIndex++;
    }
    if(currentKey() == id) {
      return postings.get(currentIndex);
    }
    return null;
  }
}
