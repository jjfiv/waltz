package jfoley.vocabpress.scoring.blockiter.movement;

/**
 * @author jfoley.
 */
public interface BlockMover {

	/** The max key in the current block. */
	public int maxKey();

	/** The current key in the current block. */
	public int currentKey();

	/** returns true if it is done */
	public boolean isDone();

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

	/** rewind the current block */
	public void rewind();
}
