package jfoley.vocabpress.mem;

import ciir.jfoley.chai.Checked;
import ciir.jfoley.chai.collections.util.MapFns;
import jfoley.vocabpress.scoring.CountPosting;
import jfoley.vocabpress.scoring.PositionsPosting;
import jfoley.vocabpress.scoring.blockiter.BlockPostingsIterator;
import jfoley.vocabpress.scoring.blockiter.ListBlockPostingsIterator;
import jfoley.vocabpress.scoring.impl.SimplePositionsPosting;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author jfoley.
 */
public class MemoryPositionsIndex {
	public Map<Integer, List<PositionsPosting>> positions;
	public Map<Integer, String> corpus;

	public InternSpace<Integer, String> terms;
	public InternSpace<Integer, String> docNames;

	public int nextDocumentId = 0;

	public void addDocument(String documentName, List<String> termVector) {
		int documentId = nextDocumentId++;
		docNames.put(documentId, documentName);

		Map<Integer, List<Integer>> postings = new HashMap<>();
		for (int pos = 0; pos < termVector.size(); pos++) {
			String k = termVector.get(pos);
			int idk = terms.getId(k);
			MapFns.extendListInMap(postings, idk, pos);
		}

		for (Map.Entry<Integer, List<Integer>> kv : postings.entrySet()) {
			SimplePositionsPosting posting = new SimplePositionsPosting(nextDocumentId, kv.getValue());
			MapFns.extendListInMap(positions, kv.getKey(), posting);
		}
	}

	public BlockPostingsIterator<? extends PositionsPosting> getPositions(String term) {
		Integer termId = terms.getId(term);
		if(termId == null) {
			return Checked.cast(BlockPostingsIterator.EMPTY);
		}
		return new ListBlockPostingsIterator<>(positions.get(termId));
	}

	public BlockPostingsIterator<? extends CountPosting> getCounts(String term) {
		return getPositions(term);
	}
}
