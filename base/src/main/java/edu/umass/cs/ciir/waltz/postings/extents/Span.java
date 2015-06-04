package edu.umass.cs.ciir.waltz.postings.extents;

/**
 * @author jfoley
 */
public final class Span {
  public final int begin;
  public final int end;

  public Span(int begin, int end) {
    this.begin = begin;
    this.end = end;
  }

  public static Span of(int begin, int end) {
    return new Span(begin, end);
  }

  @Override
  public boolean equals(Object other) {
    if(other instanceof Span) {
      Span rhs = (Span) other;
      return (rhs.begin == begin) && (rhs.end == end);
    }
    return false;
  }

  @Override
  public String toString() {
    return "[" + begin + ',' + end + ']';
  }

  @Override
  public int hashCode() {
    return Integer.hashCode(begin) ^ Integer.hashCode(end);
  }
}
