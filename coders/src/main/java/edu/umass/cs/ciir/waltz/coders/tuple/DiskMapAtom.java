package edu.umass.cs.ciir.waltz.coders.tuple;

import ciir.jfoley.chai.collections.Pair;
import edu.umass.cs.ciir.waltz.coders.Coder;
import edu.umass.cs.ciir.waltz.coders.data.BufferList;
import edu.umass.cs.ciir.waltz.coders.data.DataChunk;
import edu.umass.cs.ciir.waltz.coders.reduce.MergeFn;
import edu.umass.cs.ciir.waltz.coders.reduce.Reducer;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.io.InputStream;
import java.util.Comparator;

/**
 * @author jfoley
 */
public class DiskMapAtom<K extends Comparable<K>,V> extends Pair<K,V> implements Comparable<DiskMapAtom<K,V>> {
  public DiskMapAtom(K left, V right) {
    super(left, right);
  }

  public Comparator<? super DiskMapAtom<K,V>> getComparator() {
    return Pair.cmpLeft();
  }


  public static <K extends Comparable<K>, V> Coder<DiskMapAtom<K, V>> getCoder(Coder<K> keyCoder, Coder<V> valCoder) {
    return new DiskMapAtomCoder<>(keyCoder, valCoder);
  }

  @Override
  public int compareTo(@Nonnull DiskMapAtom<K, V> o) {
    return left.compareTo(o.left);
  }

  public static class DiskMapAtomCoder<K extends Comparable<K>, V> extends Coder<DiskMapAtom<K, V>> {
    private final Coder<K> keyCoder;
    private final Coder<V> valCoder;

    public DiskMapAtomCoder(Coder<K> keyCoder, Coder<V> valCoder) {
      this.keyCoder = keyCoder.lengthSafe();
      this.valCoder = valCoder.lengthSafe();
    }

    @Override
    public boolean knowsOwnSize() {
      return true;
    }

    @Nonnull
    @Override
    public DataChunk writeImpl(DiskMapAtom<K, V> obj) throws IOException {
      BufferList bl = new BufferList();
      bl.add(keyCoder, obj.left);
      bl.add(valCoder, obj.right);
      return bl;
    }

    @Nonnull
    @Override
    public DiskMapAtom<K, V> readImpl(InputStream inputStream) throws IOException {
      K key = keyCoder.readImpl(inputStream);
      V val = valCoder.readImpl(inputStream);
      // super-verbose debugging:
      //System.out.println("DiskMapAtom("+key+","+val+")");
      return new DiskMapAtom<>(key, val);
   }
  }

  public static class DiskMapAtomReducer<K extends Comparable<K>, V> extends Reducer<DiskMapAtom<K,V>> {
    private final MergeFn<V> mergeFn;
    public DiskMapAtomReducer(MergeFn<V> mergeFn) {
      this.mergeFn = mergeFn;
    }

    @Override
    public boolean shouldMerge(DiskMapAtom<K, V> lhs, DiskMapAtom<K, V> rhs) {
      return lhs.getKey().equals(rhs.getKey());
    }

    @Override
    public DiskMapAtom<K, V> merge(DiskMapAtom<K, V> lhs, DiskMapAtom<K, V> rhs) {
      return new DiskMapAtom<>(lhs.left, mergeFn.merge(lhs.getValue(), rhs.getValue()));
    }
  }
}
