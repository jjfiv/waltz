package jfoley.vocabpress.dociter;

/**
 * @author jfoley
 */
public interface IValueBlock<X> {
  public int size();
  public X getValue(int index);
}
