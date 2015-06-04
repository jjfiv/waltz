package edu.umass.cs.ciir.waltz.dbindex.kinds;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import edu.umass.cs.ciir.waltz.dbindex.SQLIterable;
import edu.umass.cs.ciir.waltz.dociter.IterableBlockPostingsIterator;
import edu.umass.cs.ciir.waltz.dociter.movement.BlockPostingsMover;
import edu.umass.cs.ciir.waltz.dociter.movement.PostingMover;
import edu.umass.cs.ciir.waltz.postings.Posting;

import javax.annotation.Nonnull;
import java.sql.SQLException;

/**
 * @author jfoley.
 */
@DatabaseTable(tableName = "counts")
public class CountEntry implements Posting<Integer> {
	@DatabaseField()
	public String term;
	@DatabaseField(canBeNull=false, throwIfNull = true, columnName = "document")
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

	@Nonnull
	@Override
	public Integer getValue() {
		return count;
	}

	@Override
	@SuppressWarnings("NullableProblems")
	public int compareTo(Posting<Integer> o) {
		return Integer.compare(document, o.getKey());
	}

	public static PostingMover<Integer> FromTable(String term, Dao<CountEntry, String> table) throws SQLException {
		return new BlockPostingsMover<>(
				new IterableBlockPostingsIterator<>(
						new SQLIterable<>(
								// Find all entries for the matching term.
								table.queryBuilder()
										.orderBy("document", true)
										.where().idEq(term).prepare(),
								table)));
	}
}
