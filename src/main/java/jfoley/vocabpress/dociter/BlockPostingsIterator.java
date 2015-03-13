package jfoley.vocabpress.dociter;

/**
 * @author jfoley
 */
public interface BlockPostingsIterator<X> {
  /** return a set of keys until no more blocks are found */
  public IKeyBlock nextKeyBlock();
  /** return a set of values; call nextKeyBlock first! */
  public IValueBlock<X> nextValueBlock();

  /** return to the beginning of the list. */
  public void reset();
}

