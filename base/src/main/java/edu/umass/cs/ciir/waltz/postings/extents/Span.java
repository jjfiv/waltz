package edu.umass.cs.ciir.waltz.postings.extents;

/**
 * @author jfoley
 */
public final class Span {
  /** This is inclusive, so a span of <pre>[0,3)</pre> includes <pre>{0,1,2}</pre> (Calculus notation). */
  public final int begin;
  /** This is exclusive, so a span of <pre>[0,3)</pre> includes <pre>{0,1,2}</pre> (Calculus notation). */
  public final int end;

  /** No-args constructor used only for reflection */
  private Span() {
    begin = end = -1;
  }
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
    return "[" + begin + ',' + end + ')';
  }

  @Override
  public int hashCode() {
    return Integer.hashCode(begin) ^ Integer.hashCode(end);
  }

  public boolean contains(int pos) {
    return pos >= begin && pos < end;
  }


  public boolean overlaps(Span other) {
    return overlaps(other.begin, other.end);
  }
  public boolean overlaps(int begin, int end) {
    int al = this.begin;
    int bl = begin;
    int ar = this.end-1;
    int br = end-1;
    return ar >= bl && br >= al;
  }
}
