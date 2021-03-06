package edu.umass.cs.ciir.waltz.coders.tuple;

import ciir.jfoley.chai.collections.Pair;
import ciir.jfoley.chai.collections.util.Comparing;
import edu.umass.cs.ciir.waltz.coders.Coder;
import edu.umass.cs.ciir.waltz.coders.data.ByteBuilder;
import edu.umass.cs.ciir.waltz.coders.data.DataChunk;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.io.InputStream;
import java.util.Comparator;

/**
 * @author jfoley
 */
public class DiskMapAtom<K, V> extends Pair<K,V> implements Comparable<DiskMapAtom<K,V>> {
  public DiskMapAtom(K left, V right) {
    super(left, right);
  }

  public Comparator<? super DiskMapAtom<K,V>> getComparator() {
    return Pair.cmpLeft();
  }

  public static <K, V> Coder<DiskMapAtom<K, V>> getCoder(Coder<K> keyCoder, Coder<V> valCoder) {
    return new DiskMapAtomCoder<>(keyCoder, valCoder);
  }

  @Override
  public int compareTo(@Nonnull DiskMapAtom<K, V> o) {
    return Comparing.defaultComparator().compare(this.left, o.left);
  }

  private static class DiskMapAtomCoder<K, V> extends Coder<DiskMapAtom<K, V>> {
    private final Coder<K> keyCoder;
    private final Coder<V> valCoder;

    public DiskMapAtomCoder(Coder<K> keyCoder, Coder<V> valCoder) {
      this.keyCoder = keyCoder.lengthSafe();
      this.valCoder = valCoder.lengthSafe();
    }

    @Nonnull
    @Override
    public Class<?> getTargetClass() {
      return DiskMapAtom.class;
    }

    @Override
    public boolean knowsOwnSize() {
      return true;
    }

    @Nonnull
    @Override
    public DataChunk writeImpl(DiskMapAtom<K, V> obj) throws IOException {
      ByteBuilder bl = new ByteBuilder();
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
}
