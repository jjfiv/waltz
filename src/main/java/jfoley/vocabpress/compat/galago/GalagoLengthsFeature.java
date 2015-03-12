package jfoley.vocabpress.compat.galago;

import jfoley.vocabpress.feature.Feature;
import jfoley.vocabpress.postings.CountPosting;
import org.lemurproject.galago.core.index.disk.DiskLengthSource;
import org.lemurproject.galago.core.index.disk.DiskLengthsReader;

import java.io.IOException;

/**
 * @author jfoley
 */
public class GalagoLengthsFeature implements Feature<Integer> {

  private final DiskLengthsReader lengths;

  public GalagoLengthsFeature(DiskLengthsReader lengths) {
    this.lengths = lengths;
  }

  @Override
  public boolean hasFeature(int key) {
    try {
      DiskLengthSource lengthsSource = lengths.getLengthsSource();
      lengthsSource.syncTo(key);
      return lengthsSource.hasMatch(key);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public Integer getFeature(int key) {
    try {
      return (int) lengths.getLength(key);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}
