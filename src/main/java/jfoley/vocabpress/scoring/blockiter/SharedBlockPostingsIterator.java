package jfoley.vocabpress.scoring.blockiter;

import jfoley.vocabpress.scoring.Posting;

/**
 * @author jfoley
 */
public class SharedBlockPostingsIterator<X extends Posting> {
  public static class SharedBlockPostingsIteratorView<X extends Posting> implements BlockPostingsIterator<X> {
    @Override
    public IKeyBlock nextKeyBlock() {
      return null;
    }
    @Override
    public IValueBlock<X> nextValueBlock() {
      return null;
    }
  }

  public SharedBlockPostingsIterator(BlockPostingsIterator<X> inner) {
    assert(!(inner instanceof SharedBlockPostingsIterator));

  }

  public SharedBlockPostingsIteratorView<X> getView() {

  }

}
