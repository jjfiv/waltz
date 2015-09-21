package edu.umass.cs.ciir.waltz.postings.extents;

import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @author jfoley
 */
public class SpanTest {

  @Test
  public void testIntersection() {
      Span a = Span.of(0, 4);
      Span b = Span.of(2, 6);
      Span c = Span.of(4, 8);
      Span d = Span.of(6, 10);

      Span all = Span.of(0, 11);

      assertFalse(a.overlaps(c));
      assertFalse(c.overlaps(a));

      assertFalse(a.overlaps(d));
      assertFalse(d.overlaps(a));

      assertFalse(b.overlaps(d));
      assertFalse(d.overlaps(b));

      assertTrue(a.overlaps(b));
      assertTrue(b.overlaps(a));

      assertTrue(b.overlaps(c));
      assertTrue(c.overlaps(b));

      assertTrue(c.overlaps(d));
      assertTrue(d.overlaps(c));

      for (Span span : Arrays.asList(a, b, c, d)) {
          assertTrue(span.overlaps(all));
          assertTrue(all.overlaps(span));
      }
  }

}