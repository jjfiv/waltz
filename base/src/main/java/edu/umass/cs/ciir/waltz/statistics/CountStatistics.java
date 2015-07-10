package edu.umass.cs.ciir.waltz.statistics;

import edu.umass.cs.ciir.waltz.coders.Coder;
import edu.umass.cs.ciir.waltz.coders.data.ByteBuilder;
import edu.umass.cs.ciir.waltz.coders.data.DataChunk;
import edu.umass.cs.ciir.waltz.coders.kinds.VarUInt;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.io.InputStream;

/**
 * @author jfoley
 */
public class CountStatistics {
  public int documentFrequency;
  public int termFrequency;
  public int minCount;
  public int maxCount;

  public CountStatistics() { this(0,0,Integer.MAX_VALUE,Integer.MIN_VALUE); }
  public CountStatistics(int documentFrequency, int termFrequency, int minCount, int maxCount) {
    this.documentFrequency = documentFrequency;
    this.termFrequency = termFrequency;
    this.minCount = minCount;
    this.maxCount = maxCount;
  }

  public void add(int count) {
    documentFrequency++;
    termFrequency++;
    this.minCount = Math.min(count, minCount);
    this.maxCount = Math.max(count, maxCount);
  }

  public void add(CountStatistics other) {
    documentFrequency += other.documentFrequency;
    termFrequency += other.termFrequency;
    this.minCount = Math.min(other.minCount, minCount);
    this.maxCount = Math.max(other.maxCount, maxCount);
  }

  @Nonnull
  public CountStatistics copy() {
    return new CountStatistics(documentFrequency,termFrequency,minCount,maxCount);
  }

  public static Coder<CountStatistics> coder = new Coder<CountStatistics>() {
    @Override
    public boolean knowsOwnSize() {
      return true;
    }

    @Nonnull
    @Override
    public DataChunk writeImpl(CountStatistics obj) throws IOException {
      ByteBuilder output = new ByteBuilder();
      output.add(VarUInt.instance, obj.documentFrequency);
      output.add(VarUInt.instance, obj.termFrequency);
      output.add(VarUInt.instance, obj.minCount);
      output.add(VarUInt.instance, obj.maxCount);
      return output;
    }

    @Nonnull
    @Override
    public CountStatistics readImpl(InputStream inputStream) throws IOException {
      int documentFrequency = VarUInt.instance.readImpl(inputStream);
      int termFrequency = VarUInt.instance.readImpl(inputStream);
      int minCount = VarUInt.instance.readImpl(inputStream);
      int maxCount = VarUInt.instance.readImpl(inputStream);
      return new CountStatistics(documentFrequency,termFrequency,minCount,maxCount);
    }
  };
}
