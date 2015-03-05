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
  public void movePast(int key) {
    moveTo(key+1);
  }

  @Override
  public void moveTo(int key) {
    while(hasCurrent() && currentKey() < key) {
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

  @Override
  public int compareTo(PostingIterator o) {
    return Integer.compare(currentKey(), o.currentKey());
  }
}
