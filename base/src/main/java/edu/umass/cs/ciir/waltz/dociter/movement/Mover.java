package edu.umass.cs.ciir.waltz.dociter.movement;

/**
 * @author jfoley.
 */
public interface Mover {

	/** We use Integer.MAX_VALUE here so that we can determine the next in AnyOf and AllOf by using min() */
	public static final int DONE_ID = Integer.MAX_VALUE;

	/** The max key in the current block. */
	public int maxKey();

	/** The current key in the current block. */
	public int currentKey();

	/** returns true if it is done */
	public boolean isDone();

	/** Abstracts the block movement away from a user perspective */
	public void next();

	/** returns true if we've consumed all of the current block */
	public boolean isDoneWithBlock();

	/** Load up the next block */
	public void nextBlock();

	/** Move to next key within this block */
	public void nextKey();

	/** Move to the given posting-key within this block. */
	public void moveTo(int key);

	/** Move past the given posting-key */
	public void movePast(int key);

	/** rewind the current block to a specific key */
	public void rewindBlock(int key);

  /** reset the current iterator to the lowest available document id. */
  public void reset();

  /** returns true iff the mover currently matches a given key */
  public boolean matches(int key);

  /** Move the iterator to before a given key, resetting if need be. */
  public void moveToAbsolute(int key);

  /** The total number of hits, or an overestimate if not possible (AnyOf). */
  public int totalKeys();
}
