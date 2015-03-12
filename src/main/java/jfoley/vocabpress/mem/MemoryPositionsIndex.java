package jfoley.vocabpress.mem;

import ciir.jfoley.chai.collections.IntRange;
import ciir.jfoley.chai.collections.list.IntList;
import ciir.jfoley.chai.collections.util.ListFns;
import ciir.jfoley.chai.collections.util.MapFns;
import jfoley.vocabpress.dociter.ListBlockPostingsIterator;
import jfoley.vocabpress.dociter.movement.BlockPostingsMover;
import jfoley.vocabpress.dociter.movement.PostingMover;
import jfoley.vocabpress.feature.CompactLengthsFeature;
import jfoley.vocabpress.feature.Feature;
import jfoley.vocabpress.feature.MoverFeature;
import jfoley.vocabpress.postings.CountPosting;
import jfoley.vocabpress.postings.impl.SimplePositionsPosting;
import jfoley.vocabpress.postings.positions.PositionsPosting;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author jfoley.
 */
public class MemoryPositionsIndex implements Index {
	public Map<Integer, List<PositionsPosting>> positions;
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
			SimplePositionsPosting posting = new SimplePositionsPosting(documentId, kv.getValue());
			MapFns.extendListInMap(positions, kv.getKey(), posting);
		}
	}

  @Override
  public List<Integer> getAllDocumentIds() {
    return IntRange.exclusive(0, lengths.size());
  }

  @Override
  public PostingMover<CountPosting> getCountsMover(String term) {
    int termId = terms.getId(term);
    if(termId < 0) return null;
    return new BlockPostingsMover<>(new ListBlockPostingsIterator<>(ListFns.castView(positions.get(termId))));
  }
  @Override
  public PostingMover<PositionsPosting> getPositionsMover(String term) {
    int termId = terms.getId(term);
    if(termId < 0) return null;
    return new BlockPostingsMover<>(new ListBlockPostingsIterator<>(positions.get(termId)));
  }

  @Override
  public Feature<CountPosting> getCounts(String term) {
    return new MoverFeature<>(getCountsMover(term));
  }

  @Override
  public Feature<PositionsPosting> getPositions(String term) {
    return new MoverFeature<>(getPositionsMover(term));
  }

  @Override
  public String getDocumentName(int id) {
    return docNames.getValue(id);
  }

  @Override
  public Feature<Integer> getLengths() {
    return new CompactLengthsFeature(lengths);
  }
}
