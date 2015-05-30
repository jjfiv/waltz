package edu.umass.cs.ciir.waltz.coders.tuple;

import edu.umass.cs.ciir.waltz.coders.Coder;
import edu.umass.cs.ciir.waltz.coders.data.BufferList;
import edu.umass.cs.ciir.waltz.coders.data.DataChunk;
import edu.umass.cs.ciir.waltz.coders.kinds.VarUInt;

import java.io.IOException;
import java.io.InputStream;

/**
 * For ease of use when your keys are already Comparable.
 * @author jfoley
 */
public class MapPostingAtom<K extends Comparable<K>,V> implements Comparable<MapPostingAtom<K,V>> {
  private final K term;
  /** Right now, Waltz's scoring "movers" are limited to integer number of documents. */
  private final int document;
  private final V value;

  public MapPostingAtom(K term, int document, V value) {
    this.term = term;
    this.document = document;
    this.value = value;
  }

  public K getTerm() { return term; }
  public int getDocument() { return document; }
  public V getValue() { return value; }

  @Override
  public String toString() {
    return "MapPostingAtom("+term+","+document+","+value+")";
  }

  @Override
  public int compareTo(MapPostingAtom<K, V> o) {
    int cmp = term.compareTo(o.term);
    if(cmp != 0) return cmp;
    return Integer.compare(document, o.document);
  }

  public static class MPACoder<K extends Comparable<K>,V> extends Coder<MapPostingAtom<K,V>> {
    private final Coder<K> keyCoder;
    private final Coder<Integer> docCoder;
    private final Coder<V> valCoder;

    public MPACoder(Coder<K> keyCoder, Coder<Integer> docCoder, Coder<V> valCoder) {
      this.keyCoder = keyCoder.lengthSafe();
      this.docCoder = docCoder.lengthSafe();
      this.valCoder = valCoder.lengthSafe();
    }
    public MPACoder(Coder<K> keyCoder, Coder<V> valCoder) {
      this(keyCoder, VarUInt.instance, valCoder);
    }


    @Override
    public boolean knowsOwnSize() {
      return true;
    }

    @Override
    public DataChunk writeImpl(MapPostingAtom<K, V> obj) throws IOException {
      BufferList output = new BufferList();
      output.add(keyCoder, obj.getTerm());
      output.add(docCoder, obj.getDocument());
      output.add(valCoder, obj.getValue());
      return output;
    }

    @Override
    public MapPostingAtom<K, V> readImpl(InputStream inputStream) throws IOException {
      K term = keyCoder.readImpl(inputStream);
      int document = docCoder.readImpl(inputStream);
      V value = valCoder.readImpl(inputStream);
      return new MapPostingAtom<>(term, document, value);
    }
  }
}
