package edu.umass.cs.ciir.waltz.dbindex;

import ciir.jfoley.chai.collections.list.IntList;
import ciir.jfoley.chai.io.IO;
import com.j256.ormlite.dao.CloseableWrappedIterable;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.jdbc.JdbcConnectionSource;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;
import edu.umass.cs.ciir.waltz.dbindex.kinds.DocumentEntry;
import edu.umass.cs.ciir.waltz.dociter.movement.PostingMover;
import edu.umass.cs.ciir.waltz.feature.Feature;
import edu.umass.cs.ciir.waltz.index.MutableIndex;
import edu.umass.cs.ciir.waltz.postings.positions.PositionsList;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

/**
 * @author jfoley.
 */
public class DBIndex implements MutableIndex {
	private final ConnectionSource source;
	private final Dao<DocumentEntry, Integer> documents;
	private int nextDocumentId = 0;

	public DBIndex(DBConfig cfg) throws SQLException {
		this.source = new JdbcConnectionSource(cfg.getJDBCURL());
		this.documents = DaoManager.createDao(source, DocumentEntry.class);
		setup();
	}

	private void setup() throws SQLException {
		TableUtils.createTableIfNotExists(source, DocumentEntry.class);

		// figure out the nextDocumentId to assign:
		int maxId = 0;
		for (DocumentEntry x : documents.queryForAll()) {
			if(x.id > maxId) {
				maxId = x.id;
			}
		}
		nextDocumentId = maxId+1;
	}

	@Override
	public void addDocument(String documentName, List<String> termVector) {
		try {
			DocumentEntry entry = new DocumentEntry(nextDocumentId++, documentName, termVector.size());
			documents.create(entry);
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public int getCollectionLength() {
		try {
			int total = 0;
			for (DocumentEntry entry : documents.queryForAll()) {
				total += entry.length;
			}
			return total;
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public int getDocumentCount() {
		try {
			return (int) documents.countOf();
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public List<Integer> getAllDocumentIds() {
		IntList ids = new IntList();
		CloseableWrappedIterable<DocumentEntry> iter = documents.getWrappedIterable();
		for (DocumentEntry entry : iter) {
			ids.add(entry.id);
		}
		IO.close(iter);
		return ids;
	}

	@Override
	public PostingMover<Integer> getCountsMover(String term) {
		return null;
	}

	@Override
	public PostingMover<PositionsList> getPositionsMover(String term) {
		return null;
	}

	@Override
	public Feature<Integer> getCounts(String term) {
		return null;
	}

	@Override
	public Feature<PositionsList> getPositions(String term) {
		return null;
	}

	@Override
	public String getDocumentName(int id) {
		try {
			return documents.queryForId(id).name;
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public Feature<Integer> getLengths() {
		return null;
	}

	@Override
	public void close() throws IOException {
		try {
			source.close();
		} catch (SQLException e) {
			throw new IOException(e);
		}
	}
}
