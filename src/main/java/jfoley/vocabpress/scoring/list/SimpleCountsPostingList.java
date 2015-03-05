package jfoley.vocabpress.scoring.list;

import jfoley.vocabpress.scoring.CountPosting;
import jfoley.vocabpress.scoring.iter.CountIterator;
import jfoley.vocabpress.scoring.iter.ListPostingIterator;
import jfoley.vocabpress.scoring.iter.PostingIterator;

import java.util.List;

/**
 * @author jfoley
 */
public class SimpleCountsPostingList implements CountPostingList {
  public static class Iter extends ListPostingIterator<CountPosting> implements CountIterator {
    public Iter(List<CountPosting> postings) {
      super(postings);
    }
    @Override
    public CountPosting getCount(int id) {
      return getPosting(id);
    }
  }

  private final List<CountPosting> postings;

  public SimpleCountsPostingList(List<CountPosting> postings) {
    this.postings = postings;
  }

  @Override
  public CountIterator getCountIterator() {
    return new Iter(postings);
  }

  @Override
  public PostingIterator getPostings() {
    return getCountIterator();
  }
}
