package edu.umass.cs.ciir.waltz.index.mem;

import ciir.jfoley.chai.collections.IntRange;
import ciir.jfoley.chai.collections.list.IntList;
import ciir.jfoley.chai.collections.util.ListFns;
import ciir.jfoley.chai.collections.util.MapFns;
import edu.umass.cs.ciir.waltz.dociter.ListBlockPostingsIterator;
import edu.umass.cs.ciir.waltz.dociter.movement.BlockPostingsMover;
import edu.umass.cs.ciir.waltz.dociter.movement.PostingMover;
import edu.umass.cs.ciir.waltz.feature.CompactLengthsFeature;
import edu.umass.cs.ciir.waltz.feature.Feature;
import edu.umass.cs.ciir.waltz.index.AbstractIndex;
import edu.umass.cs.ciir.waltz.index.MutableIndex;
import edu.umass.cs.ciir.waltz.index.intern.InternSpace;
import edu.umass.cs.ciir.waltz.postings.Posting;
import edu.umass.cs.ciir.waltz.postings.SimplePosting;
import edu.umass.cs.ciir.waltz.postings.positions.PositionsList;
import edu.umass.cs.ciir.waltz.postings.positions.SimplePositionsList;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author jfoley.
 */
public class MemoryPositionsIndex extends AbstractIndex implements MutableIndex {
	public Map<Integer, List<Posting<PositionsList>>> positions;
	public Map<Integer, int[]> corpus;
  public IntList lengths;
  int collectionLength;

	public InternSpace<String> terms;
	public InternSpace<String> docNames;

  public MemoryPositionsIndex() {
    this.positions = new HashMap<>();
    this.corpus = new HashMap<>();
    this.terms = new DoubleMapInternSpace<>();
    this.docNames = new DoubleMapInternSpace<>();
    this.lengths = new IntList();
    this.collectionLength = 0;
  }

  // TODO longs here?
  @Override
  public int getCollectionLength() {
    return collectionLength;
  }

  @Override
  public int getDocumentCount() {
    return lengths.size();
  }

  @Override
	public void addDocument(String documentName, List<String> termVector) {
		int documentId = lengths.size();
		docNames.put(documentId, documentName);

    IntList id_terms = new IntList();
		Map<Integer, List<Integer>> postings = new HashMap<>();
		for (int pos = 0; pos < termVector.size(); pos++) {
			String k = termVector.get(pos);
      int idk = terms.insertOrGet(k);
      id_terms.add(idk);
			MapFns.extendListInMap(postings, idk, pos);
		}
    corpus.put(documentId, id_terms.asArray());
    lengths.add(id_terms.size());
    collectionLength += id_terms.size();

		for (Map.Entry<Integer, List<Integer>> kv : postings.entrySet()) {
			SimplePosting<PositionsList> posting = new SimplePosting<>(documentId, new SimplePositionsList(kv.getValue()));
			MapFns.extendListInMap(positions, kv.getKey(), posting);
		}
	}

  @Nonnull
  @Override
  public List<Integer> getAllDocumentIds() {
    return IntRange.exclusive(0, lengths.size());
  }

  @Override
  public PostingMover<Integer> getCountsMover(String term) {
    int termId = terms.getId(term);
    if(termId < 0) return null;
    return new CountsOfPositionsMover(new BlockPostingsMover<>(new ListBlockPostingsIterator<>(ListFns.castView(positions.get(termId)))));
  }
  @Override
  public PostingMover<PositionsList> getPositionsMover(String term) {
    int termId = terms.getId(term);
    if(termId < 0) return null;
    return new BlockPostingsMover<>(new ListBlockPostingsIterator<>(positions.get(termId)));
  }

  @Override
  public String getDocumentName(int id) {
    return docNames.getValue(id);
  }

  @Override
  public int getDocumentId(String documentName) {
    return docNames.getId(documentName);
  }

  @Override
  public Feature<Integer> getLengths() {
    return new CompactLengthsFeature(lengths);
  }

  @Override
  public void close() throws IOException {
    // Since we're purely in-memory here, nothing to do here.
  }
}
