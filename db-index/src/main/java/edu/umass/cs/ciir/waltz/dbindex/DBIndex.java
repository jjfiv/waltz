package edu.umass.cs.ciir.waltz.dbindex;

import ciir.jfoley.chai.collections.list.IntList;
import ciir.jfoley.chai.io.IO;
import com.j256.ormlite.dao.CloseableWrappedIterable;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.jdbc.JdbcConnectionSource;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;
import edu.umass.cs.ciir.waltz.dbindex.kinds.CountEntry;
import edu.umass.cs.ciir.waltz.dbindex.kinds.DocumentEntry;
import edu.umass.cs.ciir.waltz.dbindex.kinds.PositionEntry;
import edu.umass.cs.ciir.waltz.dociter.movement.PostingMover;
import edu.umass.cs.ciir.waltz.feature.Feature;
import edu.umass.cs.ciir.waltz.feature.MoverFeature;
import edu.umass.cs.ciir.waltz.index.MutableIndex;
import edu.umass.cs.ciir.waltz.postings.positions.PositionsList;
import gnu.trove.map.hash.TObjectIntHashMap;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

/**
 * @author jfoley.
 */
public class DBIndex implements MutableIndex {
  private final ConnectionSource source;
  private final Dao<DocumentEntry, Integer> documents;
  private final Dao<CountEntry, String> counts;
  private final Dao<PositionEntry, String> positions;
  private int nextDocumentId = 0;

  public DBIndex(ConnectionSource source) throws SQLException {
    this.source = source;
    this.documents = DaoManager.createDao(source, DocumentEntry.class);
    this.counts = DaoManager.createDao(source, CountEntry.class);
    this.positions = DaoManager.createDao(source, PositionEntry.class);
    setup();
  }

  public DBIndex(DBConfig cfg) throws SQLException {
    this(new JdbcConnectionSource(cfg.getJDBCURL(), cfg.getUser(), cfg.getPassword()));
  }

  private void setup() throws SQLException {
    TableUtils.createTableIfNotExists(source, DocumentEntry.class);
    TableUtils.createTableIfNotExists(source, CountEntry.class);
    TableUtils.createTableIfNotExists(source, PositionEntry.class);

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
  public void addDocument(String documentName, List<String> termVector) throws Exception {
    try {
      DocumentEntry docInfo = new DocumentEntry(nextDocumentId++, documentName, termVector.size());
      documents.create(docInfo);

      // batch-insert a bunch of counts.
      counts.callBatchTasks(() -> {
        frequencies(termVector).forEachEntry((key, count) -> {
          try {
            counts.create(new CountEntry(key, docInfo.id, count));
          } catch (SQLException e) {
            throw new RuntimeException(e);
          }
          return true;
        });
        return null;
      });

    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }

  public static <T> TObjectIntHashMap<T> frequencies(List<T> termVector) {
    TObjectIntHashMap<T> freqs = new TObjectIntHashMap<>();
    for (T t : termVector) {
      freqs.adjustOrPutValue(t, 1, 1);
    }
    return freqs;
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
    try {
      return CountEntry.FromTable(term, counts);
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public PostingMover<PositionsList> getPositionsMover(String term) {
    try {
      return PositionEntry.FromTable(term, positions);
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public Feature<Integer> getCounts(String term) {
    return new MoverFeature<>(getCountsMover(term));
  }

  @Override
  public Feature<PositionsList> getPositions(String term) {
    return new MoverFeature<>(getPositionsMover(term));
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
  public int getDocumentId(String documentName) {
    try {
      return documents.queryForFirst(
          documents.queryBuilder()
              .where().eq("name", documentName).prepare()
      ).id;
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public Feature<Integer> getLengths() {
    return new Feature<Integer>() {
      private DocumentEntry cachedEntry = null;
      @Override
      public boolean hasFeature(int key) {
        return getCached(key) != null;
      }

      private DocumentEntry getCached(int key) {
        try {
          if(cachedEntry != null && cachedEntry.id == key) {
            return cachedEntry;
          }
          cachedEntry = documents.queryForId(key);
          return cachedEntry;
        } catch (SQLException e) {
          throw new RuntimeException(e);
        }
      }

      @Override
      public Integer getFeature(int key) {
        return getCached(key).length;
      }
    };
  }

  @Override
  public void close() throws IOException {
    try {
      source.close();
    } catch (SQLException e) {
      throw new IOException(e);
    }
  }

  public boolean containsDocument(String name) {
    try {
      DocumentEntry doc = documents.queryForFirst(documents.queryBuilder().where().eq("name", name).prepare());
      return doc != null;
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }
}
