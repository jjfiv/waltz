package jfoley.vocabpress.mem;

import ciir.jfoley.chai.collections.list.IntList;
import ciir.jfoley.chai.collections.util.MapFns;
import jfoley.vocabpress.feature.Feature;
import jfoley.vocabpress.feature.FeatureMover;
import jfoley.vocabpress.postings.CountPosting;
import jfoley.vocabpress.postings.positions.PositionsPosting;
import jfoley.vocabpress.dociter.ListBlockPostingsIterator;
import jfoley.vocabpress.postings.impl.SimplePositionsPosting;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author jfoley.
 */
public class MemoryPositionsIndex {
	public Map<Integer, List<PositionsPosting>> positions;
	public Map<Integer, int[]> corpus;

	public InternSpace<String> terms;
	public InternSpace<String> docNames;

	public int nextDocumentId = 0;

  public MemoryPositionsIndex() {
    this.positions = new HashMap<>();
    this.corpus = new HashMap<>();
    this.terms = new DoubleMapInternSpace<>();
    this.docNames = new DoubleMapInternSpace<>();
  }

	public void addDocument(String documentName, List<String> termVector) {
		int documentId = nextDocumentId++;
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

		for (Map.Entry<Integer, List<Integer>> kv : postings.entrySet()) {
			SimplePositionsPosting posting = new SimplePositionsPosting(nextDocumentId, kv.getValue());
			MapFns.extendListInMap(positions, kv.getKey(), posting);
		}
	}

	public Feature<? extends PositionsPosting> getPositions(String term) {
		int termId = terms.getId(term);
		if(termId < 0) return null;
		return new FeatureMover<>(new ListBlockPostingsIterator<>(positions.get(termId)));
	}

	public Feature<? extends CountPosting> getCounts(String term) {
		return getPositions(term);
	}

  public String getDocumentName(int id) {
    return docNames.getValue(id);
  }
}
