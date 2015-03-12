package jfoley.vocabpress.compat.galago;

import ciir.jfoley.chai.collections.list.IntList;
import jfoley.vocabpress.compat.galago.impl.GalagoCountMover;
import jfoley.vocabpress.compat.galago.impl.GalagoLengthsFeature;
import jfoley.vocabpress.compat.galago.impl.GalagoPositionsMover;
import jfoley.vocabpress.dociter.movement.PostingMover;
import jfoley.vocabpress.feature.Feature;
import jfoley.vocabpress.feature.MoverFeature;
import jfoley.vocabpress.index.Index;
import jfoley.vocabpress.postings.CountPosting;
import jfoley.vocabpress.postings.positions.PositionsPosting;
import org.lemurproject.galago.core.index.disk.DiskIndex;
import org.lemurproject.galago.core.index.disk.DiskLengthsReader;
import org.lemurproject.galago.core.index.disk.DiskNameReader;
import org.lemurproject.galago.core.index.disk.PositionIndexReader;
import org.lemurproject.galago.core.retrieval.iterator.CountIterator;
import org.lemurproject.galago.core.retrieval.iterator.ExtentIterator;
import org.lemurproject.galago.utility.Parameters;

import java.io.Closeable;
import java.io.IOException;
import java.util.List;

/**
 * A wrapper around a Galago Index (doesn't support long docids yet) that should give us some nice backwards-compatibility.
 * @author jfoley
 */
public class GalagoIndex implements Index, Closeable {

  private final DiskIndex inner;
  private final DiskNameReader names;
  private final PositionIndexReader postings;
  private final DiskLengthsReader lengths;

  public GalagoIndex(String path, Parameters cfg) throws IOException {
    this(new DiskIndex(path), cfg);
  }
  public GalagoIndex(DiskIndex galagoDiskIndex, Parameters cfg) {
    try {
      this.inner = galagoDiskIndex;
      this.names = (DiskNameReader) inner.getIndexPart("names");
      this.lengths = (DiskLengthsReader) inner.getIndexPart("lengths");
      this.postings = (PositionIndexReader) inner.getIndexPart(cfg.get("positionsPart", "postings"));
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public int getCollectionLength() {
    return (int) inner.getIndexPartStatistics("lengths").collectionLength;
  }

  @Override
  public int getDocumentCount() {
    return (int) inner.getIndexPartStatistics("lengths").highestDocumentCount;
  }

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
  public PostingMover<CountPosting> getCountsMover(String term) {
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
  public PostingMover<PositionsPosting> getPositionsMover(String term) {
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
  public Feature<CountPosting> getCounts(String term) {
    return new MoverFeature<>(getCountsMover(term));
  }

  @Override
  public Feature<PositionsPosting> getPositions(String term) {
    return new MoverFeature<>(getPositionsMover(term));
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
  public Feature<Integer> getLengths() {
    return new GalagoLengthsFeature(lengths);
  }

  @Override
  public void close() throws IOException {
    inner.close();
  }
}
