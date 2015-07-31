package edu.umass.cs.ciir.waltz.compat.galago;

import ciir.jfoley.chai.IntMath;
import ciir.jfoley.chai.collections.list.IntList;
import edu.umass.cs.ciir.waltz.compat.galago.impl.GalagoCountMover;
import edu.umass.cs.ciir.waltz.compat.galago.impl.GalagoLengthsFeature;
import edu.umass.cs.ciir.waltz.compat.galago.impl.GalagoPositionsMover;
import edu.umass.cs.ciir.waltz.dociter.movement.PostingMover;
import edu.umass.cs.ciir.waltz.feature.Feature;
import edu.umass.cs.ciir.waltz.index.AbstractIndex;
import edu.umass.cs.ciir.waltz.index.Index;
import edu.umass.cs.ciir.waltz.postings.positions.PositionsList;
import org.lemurproject.galago.core.index.disk.*;
import org.lemurproject.galago.core.retrieval.iterator.CountIterator;
import org.lemurproject.galago.core.retrieval.iterator.ExtentIterator;
import org.lemurproject.galago.utility.Parameters;

import javax.annotation.Nonnull;
import java.io.Closeable;
import java.io.IOException;
import java.util.List;

/**
 * A wrapper around a Galago Index (doesn't support long docids yet) that should give us some nice backwards-compatibility.
 * @author jfoley
 */
public class GalagoIndex extends AbstractIndex implements Index, Closeable {

  private final DiskIndex inner;
  private final DiskNameReader names;
  private final PositionIndexReader postings;
  private final DiskLengthsReader lengths;
  private final DiskNameReverseReader namesRev;

  public GalagoIndex(String path, Parameters cfg) throws IOException {
    this(new DiskIndex(path), cfg);
  }
  public GalagoIndex(DiskIndex galagoDiskIndex, Parameters cfg) {
    try {
      this.inner = galagoDiskIndex;
      this.names = (DiskNameReader) inner.getIndexPart("names");
      this.namesRev = (DiskNameReverseReader) inner.getIndexPart("names.reverse");
      this.lengths = (DiskLengthsReader) inner.getIndexPart("lengths");
      this.postings = (PositionIndexReader) inner.getIndexPart(cfg.get("positionsPart", "postings"));
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public int getCollectionLength() {
    return IntMath.fromLong(inner.getIndexPartStatistics("lengths").collectionLength);
  }

  @Override
  public int getDocumentCount() {
    return IntMath.fromLong(inner.getIndexPartStatistics("lengths").highestDocumentCount);
  }

  @Nonnull
  @Override
  public List<Integer> getAllDocumentIds() {
    IntList docIds = new IntList();
    try {
      DiskNameReader names = (DiskNameReader) inner.getIndexPart("names");
      DiskNameReader.KeyIterator iter = names.getIterator();
      while(!iter.isDone()) {
        docIds.add((int) iter.getCurrentIdentifier());
      }
    } catch (IOException e) {
      throw new RuntimeException(e);
    }

    return docIds;
  }

  @Override
  public PostingMover<Integer> getCountsMover(String term) {
    try {
      CountIterator forTerm = postings.getTermCounts(term);
      if(forTerm != null) {
        return new GalagoCountMover(forTerm);
      }
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    return null;
  }

  @Override
  public PostingMover<PositionsList> getPositionsMover(String term) {
    try {
      ExtentIterator forTerm = postings.getTermExtents(term);
      if(forTerm != null) {
        return new GalagoPositionsMover(forTerm);
      }
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    return null;
  }

  @Override
  public String getDocumentName(int id) {
    try {
      return names.getDocumentName(id);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public int getDocumentId(String documentName) {
    try {
      return IntMath.fromLong(namesRev.getDocumentIdentifier(documentName));
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public Feature<Integer> getLengths() {
    return new GalagoLengthsFeature(lengths);
  }

  @Override
  public void close() throws IOException {
    inner.close();
  }
}
