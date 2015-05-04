package edu.umass.cs.ciir.waltz.dbindex.kinds;

import ciir.jfoley.chai.collections.iters.GroupByIterator;
import ciir.jfoley.chai.collections.iters.MappingIterator;
import ciir.jfoley.chai.collections.list.IntList;
import ciir.jfoley.chai.fn.CompareFn;
import ciir.jfoley.chai.fn.TransformFn;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.stmt.PreparedQuery;
import com.j256.ormlite.table.DatabaseTable;
import edu.umass.cs.ciir.waltz.postings.Posting;
import edu.umass.cs.ciir.waltz.postings.SimplePosting;
import edu.umass.cs.ciir.waltz.postings.positions.PositionsList;
import edu.umass.cs.ciir.waltz.postings.positions.SimplePositionsList;

import java.sql.SQLException;
import java.util.Iterator;
import java.util.List;

/**
 * @author jfoley
 */
@DatabaseTable(tableName = "positions")
public class PositionEntry {
  @DatabaseField(canBeNull = true)
  public String term;
  @DatabaseField(throwIfNull = true, canBeNull = true, columnName = "document")
  public int document;
  @DatabaseField(throwIfNull = true, canBeNull = true, columnName = "position")
  public int position;

  @SuppressWarnings("unused")
  public PositionEntry() {
    this(null, -1, -1);
  }
  public PositionEntry(String term, int document, int position) {
    this.term = term;
    this.document = document;
    this.position = position;
  }

  public static class PositionsEntryIterable implements Iterable<Posting<PositionsList>> {

    private final PreparedQuery<PositionEntry> prep;
    private final Dao<PositionEntry, String> table;

    public PositionsEntryIterable(String term, Dao<PositionEntry, String> table) throws SQLException {
      this.table = table;
      this.prep = this.table.queryBuilder()
          .orderByRaw("document, position")
          .where().idEq(term)
          .prepare();
    }

    private static CompareFn<PositionEntry> sameDocumentFn = new CompareFn<PositionEntry>() {
      @Override
      public boolean compare(PositionEntry lhs, PositionEntry rhs) {
        return lhs.document == rhs.document;
      }
    };
    private static TransformFn<List<PositionEntry>, Posting<PositionsList>> toPositionsFn = new TransformFn<List<PositionEntry>, Posting<PositionsList>>() {
      @Override
      public Posting<PositionsList> transform(List<PositionEntry> input) {
        IntList raw = new IntList();
        for (PositionEntry entry : input) {
          raw.add(entry.position);
        }
        return new SimplePosting<>(input.get(0).document, new SimplePositionsList(raw));
      }
    };

    @Override
    public Iterator<Posting<PositionsList>> iterator() {
      try {
        // Group by document and then convert to the right type.
        return new MappingIterator<>(
            new GroupByIterator<>(table.iterator(prep), sameDocumentFn),
            toPositionsFn);
      } catch (SQLException e) {
        throw new RuntimeException(e);
      }
    }
  }

}
