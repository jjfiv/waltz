package jfoley.vocabpress.scoring.list;

/**
 * @author jfoley
 */
public interface PositionalPostingList extends CountPostingList {
  public Iterable<PositionalPostingList> getPositionalPostings();
}
