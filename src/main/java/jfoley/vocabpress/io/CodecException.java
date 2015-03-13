package jfoley.vocabpress.io;

/**
 * @author jfoley
 */
public class CodecException extends RuntimeException {
  public CodecException(Throwable e, Class<? extends Coder> klazz) {
    super("CodecException in class="+klazz.getName(), e);
  }
}
