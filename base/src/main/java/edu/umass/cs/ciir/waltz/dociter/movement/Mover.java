package edu.umass.cs.ciir.waltz.dociter.movement;

/**
 * @author jfoley.
 */
public interface Mover {

	/** We use Integer.MAX_VALUE here so that we can determine the next in AnyOf and AllOf by using min() */
	int DONE_ID = Integer.MAX_VALUE;

	/** The max key in the current block. */
	int maxKey();

	/** The current key in the current block. */
	int currentKey();

	/** returns true if it is done */
	boolean isDone();

	/** Simply the opposite of isDone(), in case it helps you reason about your algorithm */
	default boolean hasNext() { return !isDone(); }

	/** Abstracts the block movement away from a user perspective */
	void next();

	/** returns true if we've consumed all of the current block */
	boolean isDoneWithBlock();

	/** Load up the next block */
	void nextBlock();

	/** Move to next key within this block */
	void nextKey();

	/** Move to the given posting-key within this block. */
	void moveTo(int key);

	/** Move past the given posting-key */
	void movePast(int key);

	/** rewind the current block to a specific key */
	void rewindBlock(int key);

  /** reset the current iterator to the lowest available document id. */
	void reset();

  /** returns true iff the mover currently matches a given key */
	boolean matches(int key);

  /** Move the iterator to before a given key, resetting if need be. */
	void moveToAbsolute(int key);

  /** The total number of hits, or an overestimate if not possible (AnyOf). */
	int totalKeys();
}
