package jfoley.vocabpress.scoring.blockiter;

import jfoley.vocabpress.postings.Posting;

/**
 * @author jfoley
 */
public interface BlockPostingsIterator<X extends Posting> {
  /** return a set of keys until no more blocks are found */
  public IKeyBlock nextKeyBlock();
  /** return a set of values; call nextKeyBlock first! */
  public IValueBlock<X> nextValueBlock();

	public static BlockPostingsIterator EMPTY = new BlockPostingsIterator() {
		@Override public IKeyBlock nextKeyBlock() { return null; }
    @Override public IValueBlock nextValueBlock() { return null; }
	};
}

