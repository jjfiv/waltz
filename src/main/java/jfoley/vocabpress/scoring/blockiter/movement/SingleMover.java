package jfoley.vocabpress.scoring.blockiter.movement;

import jfoley.vocabpress.scoring.Posting;
import jfoley.vocabpress.scoring.blockiter.BlockPostingsIterator;
import jfoley.vocabpress.scoring.blockiter.IKeyBlock;
import jfoley.vocabpress.scoring.blockiter.IValueBlock;

import java.util.Iterator;

/**
 * This mover keeps a pointer to a given BlockPostingsIterator, and allows document-at-a-time (DAAT) access to it with
 * lazy movement policies sufficient for AnyOf and AllOf.
 *
 * This mover is *not* share-safe. It cannot be in an AllOf and an AnyOf simultaneously.
 * It expects that it can consume blocks greedily, and no other users are using the same BlockPostingsIterator.
 *
 * @author jfoley
 */
public class SingleMover<X extends Posting> implements Mover, Feature<X> {
  private final BlockPostingsIterator<X> iter;
  public IKeyBlock currentKeyBlock;
  public IValueBlock<X> currentValueBlock;
  public int index;

  public SingleMover(BlockPostingsIterator<X> iter) {
    this.iter = iter;
    moveToNextBlock();
  }

  void moveToNextBlock() {
    currentKeyBlock = iter.nextKeyBlock();
    currentValueBlock = null;
    index = 0;
  }

  public int getKey() {
    return currentKeyBlock.getKey(index);
  }

  /** get the value at the current index, loading values if needed. */
  private X getValue() {
    //System.out.println("getValue("+(currentKeyBlock == null)+","+index+")");
    if(currentValueBlock == null) {
      currentValueBlock = iter.nextValueBlock();
    }
    return currentValueBlock.getValue(index);
  }

  /** Block maximum key. */
  public int maxKey() {
    if(isDone()) return Mover.DONE_ID;
    return currentKeyBlock.maxKey();
  }
  /** a.k.a. minimum key */
  @Override
  public int currentKey() {
    //System.out.println("currentKey("+(currentKeyBlock == null)+","+index+")");
    if(isDone()) return Mover.DONE_ID;
    return currentKeyBlock.getKey(index);
  }

  @Override
  public boolean isDone() {
    //System.out.println("isDone("+(currentKeyBlock == null)+")");
    return currentKeyBlock == null;
  }

  /** Move to the index where key is stored, if possible, or the one right after it. */
  @Override
  public void moveTo(int key) {
    while(!isDone() && currentKeyBlock.maxKey() < key) {
      moveToNextBlock();
    }
    if(isDone()) return;

    for (index = 0; index < currentKeyBlock.size(); index++) {
      int ckey = currentKeyBlock.get(index);
      if (ckey >= key) {
        break;
      }
    }
  }

  @Override
  public void movePast(int key) {
    //System.out.println("movePast("+key+")");
    moveTo(key + 1);
  }

  @Override
  public void next() {
    movePast(currentKey());
  }

  @Override
  public int compareTo(Mover o) {
    return Integer.compare(currentKey(), o.currentKey());
  }

  @Override
  public boolean hasFeature(int id) {
    moveTo(id);
    return (currentKey() == id);
  }

  @Override
  public X getFeature(int id) {
    if(!hasFeature(id)) {
      return null;
    }
    return getValue();
  }
}
