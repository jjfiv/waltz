package edu.umass.cs.ciir.waltz.dbindex.kinds;

import ciir.jfoley.chai.collections.list.IntList;
import ciir.jfoley.chai.collections.util.IterableFns;
import ciir.jfoley.chai.fn.CompareFn;
import ciir.jfoley.chai.fn.TransformFn;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import edu.umass.cs.ciir.waltz.dbindex.SQLIterable;
import edu.umass.cs.ciir.waltz.dociter.movement.BlockPostingsMover;
import edu.umass.cs.ciir.waltz.dociter.movement.PostingMover;
import edu.umass.cs.ciir.waltz.postings.Posting;
import edu.umass.cs.ciir.waltz.postings.SimplePosting;
import edu.umass.cs.ciir.waltz.postings.positions.PositionsList;
import edu.umass.cs.ciir.waltz.postings.positions.SimplePositionsList;

import java.sql.SQLException;
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


  public static PostingMover<PositionsList> FromTable(String term, Dao<PositionEntry, String> table) throws SQLException {
    Iterable<PositionEntry> infos = new SQLIterable<>(
                        table.queryBuilder()
                            .orderByRaw("document, position")
                            .where().idEq(term).prepare(),
                        table);

    Iterable<Posting<PositionsList>> postings =
        IterableFns.map(
            IterableFns.groupBy(infos, sameDocumentFn),
            toPositionsFn);

    return BlockPostingsMover.ofIterable(postings);
  }
}
