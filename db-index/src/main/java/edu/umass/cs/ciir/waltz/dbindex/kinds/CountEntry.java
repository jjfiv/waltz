package edu.umass.cs.ciir.waltz.dbindex.kinds;

import ciir.jfoley.chai.collections.iters.ClosingIterator;
import com.j256.ormlite.dao.CloseableIterator;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.stmt.PreparedQuery;
import com.j256.ormlite.table.DatabaseTable;
import edu.umass.cs.ciir.waltz.dociter.movement.BlockPostingsMover;
import edu.umass.cs.ciir.waltz.dociter.movement.PostingMover;
import edu.umass.cs.ciir.waltz.postings.Posting;

import java.sql.SQLException;
import java.util.Iterator;

/**
 * @author jfoley.
 */
@DatabaseTable(tableName = "counts")
public class CountEntry implements Posting<Integer> {
	@DatabaseField()
	public String term;
	@DatabaseField(canBeNull=false, throwIfNull = true)
	public int document;
	@DatabaseField(throwIfNull = true, canBeNull = false)
	public int count;

	/** Needed for reflection. */
	@SuppressWarnings("unused")
	public CountEntry() {
		this(null, -1, 0);
	}
	public CountEntry(String term, int document, int count) {
		this.term = term;
		this.document = document;
		this.count = count;
	}

	@Override
	public int getKey() {
		return document;
	}

	@Override
	public Integer getValue() {
		return count;
	}

	@Override
	public int compareTo(Posting<Integer> o) {
		return Integer.compare(document, o.getKey());
	}

	public static class CountEntryIterable implements Iterable<Posting<Integer>> {

		private final Dao<CountEntry, String> table;
		private final String term;
		private final PreparedQuery<CountEntry> prep;

		public CountEntryIterable(String term, Dao<CountEntry, String> table) throws SQLException {
			this.term = term;
			this.table = table;
			this.prep = table.queryBuilder()
					.where()
					.idEq(term)
					.prepare();
		}

		@Override
		public Iterator<Posting<Integer>> iterator() {
			try {
				final CloseableIterator<CountEntry> iter = table.iterator(prep);
				return new ClosingIterator<Posting<Integer>>() {
					@Override
					public boolean hasNext() {
						return iter.hasNext();
					}

					@Override
					public Posting<Integer> next() {
						return iter.next();
					}

					public void close() throws SQLException {
						iter.close();
					}
				};
			} catch (SQLException e) {
				throw new RuntimeException(e);
			}
		}
	}

	public static PostingMover<Integer> FromTable(String term, Dao<CountEntry, String> table) throws SQLException {
		return new BlockPostingsMover<>(new IterableBlockPostingsIterator<>(new CountEntryIterable(term, table)));
	}
}
