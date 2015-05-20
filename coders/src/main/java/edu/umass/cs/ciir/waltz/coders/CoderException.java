package edu.umass.cs.ciir.waltz.coders;

/**
 * @author jfoley
 */
public class CoderException extends RuntimeException {
  public CoderException(Throwable e, Class<? extends Coder> klazz) {
    super("CodecException in class="+klazz.getName(), e);
  }
}
