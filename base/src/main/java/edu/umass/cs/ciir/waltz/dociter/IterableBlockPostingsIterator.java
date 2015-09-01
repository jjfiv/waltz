package edu.umass.cs.ciir.waltz.dociter;

import ciir.jfoley.chai.collections.list.IntList;
import ciir.jfoley.chai.io.IO;
import edu.umass.cs.ciir.waltz.postings.Posting;
import edu.umass.cs.ciir.waltz.sys.KeyMetadata;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * @author jfoley.
 */
public class IterableBlockPostingsIterator<X> implements BlockPostingsIterator<X>, AutoCloseable {

	private final int blockSize;
	private final Iterable<? extends Posting<X>> iterGen;
	private Iterator<? extends Posting<X>> iter;
	List<Posting<X>> currentBlock;
	public KeyMetadata<X> metadata;

	public IterableBlockPostingsIterator(Iterable<? extends Posting<X>> postings) {
		this(postings, 16);
	}

	public IterableBlockPostingsIterator(Iterable<? extends Posting<X>> postings, int blockSize) {
		this.iterGen = postings;
		this.blockSize = blockSize;
		reset();
	}

	@Override
	public IKeyBlock nextKeyBlock() {
		currentBlock.clear();
		IntList keys = new IntList();
		for (int i = 0; i < blockSize && iter.hasNext(); i++) {
			Posting<X> entry = iter.next();
			currentBlock.add(entry);
			keys.add(entry.getKey());
		}
		return new KeyBlock(keys);
	}

	@Override
	public ValueBlock<X> nextValueBlock() {
		List<X> bufferedValues = new ArrayList<>();
		for (Posting<X> xPosting : currentBlock) {
			bufferedValues.add(xPosting.getValue());
		}
		return new ValueBlock<>(bufferedValues);
	}

	@Override
	public void reset() {
		IO.close(iter);
		this.iter = iterGen.iterator();
		this.currentBlock = new ArrayList<>();
	}

	@Override
	public int totalKeys() {
		//TODO
		throw new UnsupportedOperationException();
	}

	@Nullable
	@Override
	public KeyMetadata<X> getMetadata() {
		return metadata;
	}

	@Override
	public void close() throws Exception {
		IO.close(iter);
		this.iter = null;
	}
}
