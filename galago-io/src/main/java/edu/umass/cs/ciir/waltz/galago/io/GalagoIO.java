package edu.umass.cs.ciir.waltz.galago.io;

import edu.umass.cs.ciir.waltz.IdMaps;
import edu.umass.cs.ciir.waltz.coders.Coder;
import edu.umass.cs.ciir.waltz.coders.map.*;
import org.lemurproject.galago.utility.Parameters;

import java.io.IOException;

/**
 * @author jfoley.
 */
public class GalagoIO {
  public static <K,V> IOMap<K,V> openIOMap(Coder<K> keyCoder, Coder<V> valCoder, String path) throws IOException {
    return new IOMapImpl<>(openRawIOMap(path), keyCoder, valCoder);
  }

  public static <K, V> IOMapWriterRawWrapper<K,V> getIOMapWriter(Coder<K> keyCoder, Coder<V> valCoder, String path, Parameters argp) throws IOException {
    return new IOMapWriterRawWrapper<>(getRawIOMapWriter(path, argp), keyCoder, valCoder);
  }

  public static <K, V> IOMapWriterRawWrapper<K,V> getIOMapWriter(Coder<K> keyCoder, Coder<V> valCoder, String path) throws IOException {
    return new IOMapWriterRawWrapper<>(getRawIOMapWriter(path), keyCoder, valCoder);
  }

  public static RawIOMap openRawIOMap(String path) throws IOException {
    return new RawGalagoDiskMap(path);
  }

  public static RawIOMapWriter getRawIOMapWriter(String path, Parameters argp) throws IOException {
    return new RawGalagoDiskMapWriter(path, argp);
  }

  public static RawIOMapWriter getRawIOMapWriter(String path) throws IOException {
    return new RawGalagoDiskMapWriter(path);
  }

  public static <V> IdMaps.Writer<V> openIdMapsWriter(String baseName, Coder<Integer> keyCoder, Coder<V> valCoder) throws IOException {
    return new IdMaps.Writer<>(
        getIOMapWriter(keyCoder, valCoder, baseName + ".fwd"),
        getIOMapWriter(valCoder, keyCoder, baseName + ".rev")
    );
  }

  public static <V> IdMaps.Reader<V> openIdMapsReader(String baseName, Coder<Integer> keyCoder, Coder<V> valCoder) throws IOException {
    return new IdMaps.Reader<>(
        openIOMap(keyCoder, valCoder, baseName + ".fwd"),
        openIOMap(valCoder, keyCoder, baseName+".rev")
    );
  }
}
