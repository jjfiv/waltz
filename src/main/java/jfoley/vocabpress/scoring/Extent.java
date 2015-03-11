package jfoley.vocabpress.scoring;

/**
 * @author jfoley
 */
public final class Extent {
  public final int begin;
  public final int end;

  public Extent(int begin, int end) {
    this.begin = begin;
    this.end = end;
  }

  public static Extent of(int begin, int end) {
    return new Extent(begin, end);
  }

  @Override
  public boolean equals(Object other) {
    if(other instanceof Extent) {
      Extent rhs = (Extent) other;
      return (rhs.begin == begin) && (rhs.end == end);
    }
    return false;
  }

  @Override
  public int hashCode() {
    return Integer.hashCode(begin) ^ Integer.hashCode(end);
  }
}
